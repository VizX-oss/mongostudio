package com.mongostudio.app.data.service

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import com.mongodb.client.model.IndexOptions
import com.mongostudio.app.data.dns.MongoDnsResolver
import com.mongostudio.app.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bson.Document
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

data class StandaloneConnectionInfo(
    val serverVersion: String,
    val pingMs: Long,
    val effectiveUri: String
)

object DirectMongoService {
    @Volatile
    private var activeClient: MongoClient? = null

    @Volatile
    var activeUri: String? = null
        private set

    @Volatile
    var activeServerVersion: String? = null
        private set

    private val gson = Gson()

    val isConnected: Boolean
        get() = activeClient != null

    suspend fun pingOnly(rawUri: String): Result<StandaloneConnectionInfo> = withContext(Dispatchers.IO) {
        var tempClient: MongoClient? = null
        try {
            val effectiveUri = MongoDnsResolver.resolveUri(rawUri)
            val connectionString = ConnectionString(effectiveUri)

            val settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .applyToSocketSettings { builder ->
                    builder.connectTimeout(10, TimeUnit.SECONDS)
                    builder.readTimeout(15, TimeUnit.SECONDS)
                }
                .applyToClusterSettings { builder ->
                    builder.serverSelectionTimeout(10, TimeUnit.SECONDS)
                }
                .build()

            tempClient = MongoClients.create(settings)
            val startTime = System.currentTimeMillis()
            val dbNameCandidate = connectionString.database?.takeIf { it.isNotBlank() } ?: "admin"
            val targetDb = try {
                val db = tempClient.getDatabase(dbNameCandidate)
                db.runCommand(Document("ping", 1))
                db
            } catch (e: Exception) {
                val admin = tempClient.getDatabase("admin")
                admin.runCommand(Document("ping", 1))
                admin
            }
            val pingMs = System.currentTimeMillis() - startTime

            val buildInfo = try {
                targetDb.runCommand(Document("buildInfo", 1))
            } catch (e: Exception) {
                Document("version", "Atlas/MongoDB")
            }
            val serverVersion = buildInfo.getString("version") ?: "Atlas/MongoDB"

            Result.success(
                StandaloneConnectionInfo(
                    serverVersion = serverVersion,
                    pingMs = pingMs,
                    effectiveUri = effectiveUri
                )
            )
        } catch (e: Throwable) {
            Result.failure(e)
        } finally {
            try {
                tempClient?.close()
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    suspend fun connect(rawUri: String): Result<StandaloneConnectionInfo> = withContext(Dispatchers.IO) {
        try {
            disconnect()

            val effectiveUri = MongoDnsResolver.resolveUri(rawUri)
            val connectionString = ConnectionString(effectiveUri)

            val settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .applyToSocketSettings { builder ->
                    builder.connectTimeout(15, TimeUnit.SECONDS)
                    builder.readTimeout(30, TimeUnit.SECONDS)
                }
                .applyToClusterSettings { builder ->
                    builder.serverSelectionTimeout(15, TimeUnit.SECONDS)
                }
                .build()

            val client = MongoClients.create(settings)

            // Test connection and measure ping
            val startTime = System.currentTimeMillis()
            val dbNameCandidate = connectionString.database?.takeIf { it.isNotBlank() } ?: "admin"
            val targetDb = try {
                val db = client.getDatabase(dbNameCandidate)
                db.runCommand(Document("ping", 1))
                db
            } catch (e: Exception) {
                val admin = client.getDatabase("admin")
                admin.runCommand(Document("ping", 1))
                admin
            }
            val pingMs = System.currentTimeMillis() - startTime

            // Query server version via buildInfo
            val buildInfo = try {
                targetDb.runCommand(Document("buildInfo", 1))
            } catch (e: Exception) {
                try {
                    client.getDatabase("admin").runCommand(Document("buildInfo", 1))
                } catch (e2: Exception) {
                    Document("version", "Atlas/MongoDB")
                }
            }
            val serverVersion = buildInfo.getString("version") ?: "Atlas/MongoDB"

            activeClient = client
            activeUri = rawUri
            activeServerVersion = serverVersion

            Result.success(
                StandaloneConnectionInfo(
                    serverVersion = serverVersion,
                    pingMs = pingMs,
                    effectiveUri = effectiveUri
                )
            )
        } catch (e: Throwable) {
            disconnect()
            Result.failure(e)
        }
    }

    suspend fun disconnect() = withContext(Dispatchers.IO) {
        try {
            activeClient?.close()
        } catch (e: Exception) {
            // ignore
        } finally {
            activeClient = null
            activeUri = null
            activeServerVersion = null
        }
    }

    suspend fun getOverview(): Result<ClusterOverview> = withContext(Dispatchers.IO) {
        val client = activeClient ?: return@withContext Result.failure(IllegalStateException("Not connected to MongoDB"))
        try {
            var rawDbsList: List<Document> = emptyList()
            var totalSize = 0L

            try {
                val adminDb = client.getDatabase("admin")
                val dbsResult = adminDb.runCommand(Document("listDatabases", 1))
                rawDbsList = dbsResult.getList("databases", Document::class.java) ?: emptyList()
                totalSize = (dbsResult["totalSize"] as? Number)?.toLong() ?: 0L
            } catch (e: Exception) {
                // When cluster user is restricted from admin listDatabases, fallback to listDatabaseNames or URI database
                val names = try {
                    client.listDatabaseNames().toList()
                } catch (e2: Exception) {
                    val uriDb = activeUri?.let { ConnectionString(it).database }
                    if (!uriDb.isNullOrBlank()) listOf(uriDb) else listOf("default")
                }
                rawDbsList = names.map { Document("name", it).append("sizeOnDisk", 0L).append("empty", false) }
            }

            var serverStatusDoc: Document? = null
            try {
                serverStatusDoc = client.getDatabase("admin").runCommand(Document("serverStatus", 1))
            } catch (e: Exception) {
                // non-admin credentials might not have serverStatus privilege
            }

            val serverStatus = serverStatusDoc?.let {
                ServerStatus(
                    version = it.getString("version") ?: activeServerVersion,
                    uptime = (it["uptime"] as? Number)?.toLong(),
                    host = it.getString("host"),
                    connections = it.get("connections", Document::class.java)?.toMap(),
                    mem = it.get("mem", Document::class.java)?.toMap()
                )
            }

            val databases = mutableListOf<DatabaseInfo>()
            for (dbDoc in rawDbsList) {
                val dbName = dbDoc.getString("name") ?: continue
                val sizeOnDisk = (dbDoc["sizeOnDisk"] as? Number)?.toLong() ?: 0L
                val empty = dbDoc.getBoolean("empty") ?: false

                try {
                    val db = client.getDatabase(dbName)
                    val colNames = db.listCollectionNames().toList()
                    val stats = try {
                        db.runCommand(Document("dbStats", 1))
                    } catch (e: Exception) {
                        Document()
                    }

                    databases.add(
                        DatabaseInfo(
                            name = dbName,
                            sizeOnDisk = sizeOnDisk,
                            empty = empty,
                            collectionsCount = colNames.size,
                            objectsCount = (stats["objects"] as? Number)?.toLong() ?: 0L,
                            dataSize = (stats["dataSize"] as? Number)?.toLong() ?: 0L,
                            storageSize = (stats["storageSize"] as? Number)?.toLong() ?: 0L,
                            indexes = (stats["indexes"] as? Number)?.toInt() ?: 0,
                            indexSize = (stats["indexSize"] as? Number)?.toLong() ?: 0L
                        )
                    )
                } catch (e: Exception) {
                    databases.add(
                        DatabaseInfo(
                            name = dbName,
                            sizeOnDisk = sizeOnDisk,
                            empty = empty,
                            collectionsCount = 0
                        )
                    )
                }
            }

            Result.success(
                ClusterOverview(
                    totalSize = totalSize,
                    databases = databases,
                    serverStatus = serverStatus
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCollections(dbName: String): Result<List<CollectionInfo>> = withContext(Dispatchers.IO) {
        val client = activeClient ?: return@withContext Result.failure(IllegalStateException("Not connected"))
        try {
            val db = client.getDatabase(dbName)
            val colNames = db.listCollectionNames().toList()
            val list = mutableListOf<CollectionInfo>()

            for (name in colNames) {
                try {
                    val col = db.getCollection(name)
                    val count = col.estimatedDocumentCount()
                    val collStats = try {
                        db.runCommand(Document("collStats", name))
                    } catch (e: Exception) {
                        Document()
                    }

                    list.add(
                        CollectionInfo(
                            name = name,
                            type = "collection",
                            docCount = count,
                            size = (collStats["size"] as? Number)?.toLong() ?: 0L,
                            storageSize = (collStats["storageSize"] as? Number)?.toLong() ?: 0L,
                            totalIndexSize = (collStats["totalIndexSize"] as? Number)?.toLong() ?: 0L,
                            nindexes = (collStats["nindexes"] as? Number)?.toInt() ?: 0
                        )
                    )
                } catch (e: Exception) {
                    list.add(CollectionInfo(name = name, docCount = 0L))
                }
            }

            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createCollection(dbName: String, colName: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val client = activeClient ?: return@withContext Result.failure(IllegalStateException("Not connected"))
        try {
            client.getDatabase(dbName).createCollection(colName)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun dropCollection(dbName: String, colName: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val client = activeClient ?: return@withContext Result.failure(IllegalStateException("Not connected"))
        try {
            client.getDatabase(dbName).getCollection(colName).drop()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun dropDatabase(dbName: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val client = activeClient ?: return@withContext Result.failure(IllegalStateException("Not connected"))
        if (dbName in listOf("admin", "local", "config")) {
            return@withContext Result.failure(IllegalArgumentException("Cannot drop system database '$dbName'"))
        }
        try {
            client.getDatabase(dbName).drop()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun queryDocuments(
        dbName: String,
        colName: String,
        filterJson: String? = null,
        sortJson: String? = null,
        projectionJson: String? = null,
        page: Int = 1,
        limit: Int = 20
    ): Result<QueryResponse> = withContext(Dispatchers.IO) {
        val client = activeClient ?: return@withContext Result.failure(IllegalStateException("Not connected"))
        try {
            val col = client.getDatabase(dbName).getCollection(colName)

            val filterDoc = parseBsonDocument(filterJson)
            val sortDoc = parseBsonDocument(sortJson)
            val projectionDoc = parseBsonDocument(projectionJson)

            val safePage = page.coerceAtLeast(1)
            val safeLimit = limit.coerceIn(1, 100)
            val skip = (safePage - 1) * safeLimit

            val total = col.countDocuments(filterDoc)
            val rawDocs = col.find(filterDoc)
                .sort(sortDoc)
                .projection(projectionDoc)
                .skip(skip)
                .limit(safeLimit)
                .toList()

            val docs = rawDocs.map { documentToMap(it) }
            val totalPages = Math.ceil(total.toDouble() / safeLimit).toInt().coerceAtLeast(1)

            Result.success(
                QueryResponse(
                    total = total,
                    page = safePage,
                    limit = safeLimit,
                    totalPages = totalPages,
                    documents = docs
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createDocument(dbName: String, colName: String, docJson: String): Result<String> = withContext(Dispatchers.IO) {
        val client = activeClient ?: return@withContext Result.failure(IllegalStateException("Not connected"))
        try {
            val col = client.getDatabase(dbName).getCollection(colName)
            val parsed = Document.parse(docJson)
            col.insertOne(parsed)
            val id = parsed.get("_id")?.toString() ?: "inserted"
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateDocument(dbName: String, colName: String, docId: String, docJson: String): Result<Long> = withContext(Dispatchers.IO) {
        val client = activeClient ?: return@withContext Result.failure(IllegalStateException("Not connected"))
        try {
            val col = client.getDatabase(dbName).getCollection(colName)
            val updatePayload = Document.parse(docJson)
            updatePayload.remove("_id")

            val filter = buildIdFilter(docId)
            val result = col.replaceOne(filter, updatePayload)
            if (result.matchedCount == 0L) {
                return@withContext Result.failure(IllegalArgumentException("Document not found with ID: $docId"))
            }
            Result.success(result.modifiedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteDocument(dbName: String, colName: String, docId: String): Result<Long> = withContext(Dispatchers.IO) {
        val client = activeClient ?: return@withContext Result.failure(IllegalStateException("Not connected"))
        try {
            val col = client.getDatabase(dbName).getCollection(colName)
            val filter = buildIdFilter(docId)
            val result = col.deleteOne(filter)
            Result.success(result.deletedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun aggregate(dbName: String, colName: String, pipelineJson: String): Result<AggregateResponse> = withContext(Dispatchers.IO) {
        val client = activeClient ?: return@withContext Result.failure(IllegalStateException("Not connected"))
        try {
            val col = client.getDatabase(dbName).getCollection(colName)
            val wrapped = Document.parse("""{"stages": $pipelineJson}""")
            val stages = wrapped.getList("stages", Document::class.java) ?: emptyList()

            val rawResults = col.aggregate(stages).toList()
            val results = rawResults.map { documentToMap(it) }

            Result.success(
                AggregateResponse(
                    success = true,
                    count = results.size,
                    results = results
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getIndexes(dbName: String, colName: String): Result<List<IndexInfo>> = withContext(Dispatchers.IO) {
        val client = activeClient ?: return@withContext Result.failure(IllegalStateException("Not connected"))
        try {
            val col = client.getDatabase(dbName).getCollection(colName)
            val list = mutableListOf<IndexInfo>()
            for (idxDoc in col.listIndexes()) {
                val name = idxDoc.getString("name") ?: ""
                val v = (idxDoc["v"] as? Number)?.toInt()
                val unique = idxDoc.getBoolean("unique")
                val keyDoc = idxDoc.get("key", Document::class.java)
                list.add(
                    IndexInfo(
                        v = v,
                        key = keyDoc?.toMap(),
                        name = name,
                        unique = unique
                    )
                )
            }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createIndex(dbName: String, colName: String, keysJson: String, isUnique: Boolean): Result<String> = withContext(Dispatchers.IO) {
        val client = activeClient ?: return@withContext Result.failure(IllegalStateException("Not connected"))
        try {
            val col = client.getDatabase(dbName).getCollection(colName)
            val keysDoc = Document.parse(keysJson)
            val options = IndexOptions().unique(isUnique)
            val name = col.createIndex(keysDoc, options)
            Result.success(name)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun dropIndex(dbName: String, colName: String, indexName: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val client = activeClient ?: return@withContext Result.failure(IllegalStateException("Not connected"))
        try {
            client.getDatabase(dbName).getCollection(colName).dropIndex(indexName)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun executeRawCommand(dbName: String, commandJson: String): Result<Map<String, Any?>> = withContext(Dispatchers.IO) {
        val client = activeClient ?: return@withContext Result.failure(IllegalStateException("Not connected"))
        try {
            val db = client.getDatabase(dbName.ifBlank { "admin" })
            val cmdDoc = Document.parse(commandJson)
            val res = db.runCommand(cmdDoc)
            Result.success(documentToMap(res))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildIdFilter(docId: String): Bson {
        return if (ObjectId.isValid(docId)) {
            Filters.or(Filters.eq("_id", ObjectId(docId)), Filters.eq("_id", docId))
        } else {
            Filters.eq("_id", docId)
        }
    }

    private fun parseBsonDocument(json: String?): Document {
        if (json.isNullOrBlank()) return Document()
        return try {
            val doc = Document.parse(json)
            convertBsonTypesInDoc(doc)
            doc
        } catch (e: Exception) {
            Document()
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun convertBsonTypesInDoc(doc: Document) {
        for (key in doc.keys.toList()) {
            val v = doc[key]
            if (v is String) {
                if (key == "_id" && ObjectId.isValid(v) && v.length == 24) {
                    doc[key] = ObjectId(v)
                } else if (v.startsWith("ObjectId(\"") && v.endsWith("\")")) {
                    val idStr = v.substring(10, v.length - 2)
                    if (ObjectId.isValid(idStr)) doc[key] = ObjectId(idStr)
                }
            } else if (v is Document) {
                if (v.containsKey("\$oid")) {
                    val oidStr = v.getString("\$oid")
                    if (oidStr != null && ObjectId.isValid(oidStr)) {
                        doc[key] = ObjectId(oidStr)
                        continue
                    }
                }
                convertBsonTypesInDoc(v)
            } else if (v is List<*>) {
                for (item in v) {
                    if (item is Document) convertBsonTypesInDoc(item)
                }
            }
        }
    }

    private fun documentToMap(doc: Document): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()
        for ((k, v) in doc) {
            map[k] = convertBsonValue(v)
        }
        return map
    }

    private fun convertBsonValue(v: Any?): Any? {
        return when (v) {
            is ObjectId -> v.toHexString()
            is Date -> SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.format(v)
            is Document -> documentToMap(v)
            is List<*> -> v.map { convertBsonValue(it) }
            else -> v
        }
    }
}

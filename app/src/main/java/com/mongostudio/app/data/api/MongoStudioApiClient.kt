package com.mongostudio.app.data.api

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.mongostudio.app.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

class MongoStudioApiClient(
    private var baseUrl: String = "http://10.0.2.2:4000"
) {
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    @Volatile
    var connectionId: String? = null
        private set

    fun setBaseUrl(url: String) {
        baseUrl = url.trimEnd('/')
    }

    fun getBaseUrl(): String = baseUrl

    fun setConnectionId(id: String?) {
        connectionId = id
    }

    private fun buildRequest(
        endpoint: String,
        method: String = "GET",
        bodyJson: String? = null
    ): Request {
        val url = if (endpoint.startsWith("http")) endpoint else "$baseUrl$endpoint"
        val builder = Request.Builder().url(url)
        
        connectionId?.let {
            builder.addHeader("x-connection-id", it)
        }

        when (method.uppercase()) {
            "POST" -> {
                val reqBody = (bodyJson ?: "{}").toRequestBody(jsonMediaType)
                builder.post(reqBody)
            }
            "PUT" -> {
                val reqBody = (bodyJson ?: "{}").toRequestBody(jsonMediaType)
                builder.put(reqBody)
            }
            "DELETE" -> {
                if (bodyJson != null) {
                    builder.delete(bodyJson.toRequestBody(jsonMediaType))
                } else {
                    builder.delete()
                }
            }
            else -> builder.get()
        }

        return builder.build()
    }

    suspend fun healthCheck(): Result<Long> = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        try {
            val req = Request.Builder().url("$baseUrl/api/health").get().build()
            client.newCall(req).execute().use { response ->
                val elapsed = System.currentTimeMillis() - start
                if (response.isSuccessful) {
                    Result.success(elapsed)
                } else {
                    Result.failure(IOException("Health check returned ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun connect(uri: String, name: String?, save: Boolean): Result<ConnectResponse> = withContext(Dispatchers.IO) {
        try {
            val reqObj = ConnectRequest(uri = uri, name = name, saveConnection = save)
            val json = gson.toJson(reqObj)
            val request = buildRequest("/api/connect", "POST", json)
            
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                val res = gson.fromJson(body, ConnectResponse::class.java)
                if (response.isSuccessful && res.success) {
                    connectionId = res.connectionId
                    Result.success(res)
                } else {
                    val errMsg = res?.error ?: parseErrorMessage(body) ?: "Connection failed (HTTP ${response.code})"
                    Result.failure(IOException(errMsg))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSavedConnections(): Result<List<SavedConnection>> = withContext(Dispatchers.IO) {
        try {
            val request = buildRequest("/api/saved-connections", "GET")
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: "[]"
                if (response.isSuccessful) {
                    val listType = object : TypeToken<List<SavedConnection>>() {}.type
                    val list: List<SavedConnection> = gson.fromJson(body, listType)
                    Result.success(list)
                } else {
                    Result.failure(IOException("Failed to load saved connections (${response.code})"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun connectSaved(id: String): Result<ConnectResponse> = withContext(Dispatchers.IO) {
        try {
            val request = buildRequest("/api/saved-connections/$id/connect", "POST", "{}")
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                val res = gson.fromJson(body, ConnectResponse::class.java)
                if (response.isSuccessful && res.success) {
                    connectionId = res.connectionId
                    Result.success(res)
                } else {
                    val errMsg = res?.error ?: parseErrorMessage(body) ?: "Failed to connect to saved cluster"
                    Result.failure(IOException(errMsg))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteSavedConnection(id: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val request = buildRequest("/api/saved-connections/$id", "DELETE")
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) Result.success(true)
                else Result.failure(IOException("Failed to delete saved connection (${response.code})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun disconnect(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val request = buildRequest("/api/disconnect", "POST", "{}")
            client.newCall(request).execute().use { _ ->
                connectionId = null
                Result.success(true)
            }
        } catch (e: Exception) {
            connectionId = null
            Result.success(true)
        }
    }

    suspend fun getOverview(): Result<ClusterOverview> = withContext(Dispatchers.IO) {
        try {
            val request = buildRequest("/api/overview", "GET")
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val overview = gson.fromJson(body, ClusterOverview::class.java)
                    Result.success(overview)
                } else {
                    Result.failure(IOException(parseErrorMessage(body) ?: "Failed to fetch overview (${response.code})"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCollections(dbName: String): Result<List<CollectionInfo>> = withContext(Dispatchers.IO) {
        try {
            val request = buildRequest("/api/databases/$dbName/collections", "GET")
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: "[]"
                if (response.isSuccessful) {
                    val listType = object : TypeToken<List<CollectionInfo>>() {}.type
                    val list: List<CollectionInfo> = gson.fromJson(body, listType)
                    Result.success(list)
                } else {
                    Result.failure(IOException(parseErrorMessage(body) ?: "Failed to fetch collections (${response.code})"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createCollection(dbName: String, colName: String): Result<GenericApiResponse> = withContext(Dispatchers.IO) {
        try {
            val payload = JsonObject().apply { addProperty("collectionName", colName) }
            val request = buildRequest("/api/databases/$dbName/collections", "POST", gson.toJson(payload))
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                val res = gson.fromJson(body, GenericApiResponse::class.java)
                if (response.isSuccessful) Result.success(res)
                else Result.failure(IOException(res?.error ?: parseErrorMessage(body) ?: "Create collection failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun dropCollection(dbName: String, colName: String): Result<GenericApiResponse> = withContext(Dispatchers.IO) {
        try {
            val request = buildRequest("/api/databases/$dbName/collections/$colName", "DELETE")
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                val res = gson.fromJson(body, GenericApiResponse::class.java)
                if (response.isSuccessful) Result.success(res)
                else Result.failure(IOException(res?.error ?: parseErrorMessage(body) ?: "Drop collection failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun dropDatabase(dbName: String): Result<GenericApiResponse> = withContext(Dispatchers.IO) {
        try {
            val request = buildRequest("/api/databases/$dbName", "DELETE")
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                val res = gson.fromJson(body, GenericApiResponse::class.java)
                if (response.isSuccessful) Result.success(res)
                else Result.failure(IOException(res?.error ?: parseErrorMessage(body) ?: "Drop database failed"))
            }
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
        try {
            val payload = JsonObject().apply {
                if (!filterJson.isNullOrBlank()) {
                    try {
                        add("filter", gson.fromJson(filterJson, JsonElement::class.java))
                    } catch (e: Exception) {
                        addProperty("filter", filterJson)
                    }
                }
                if (!sortJson.isNullOrBlank()) {
                    try {
                        add("sort", gson.fromJson(sortJson, JsonElement::class.java))
                    } catch (e: Exception) {
                        addProperty("sort", sortJson)
                    }
                }
                if (!projectionJson.isNullOrBlank()) {
                    try {
                        add("projection", gson.fromJson(projectionJson, JsonElement::class.java))
                    } catch (e: Exception) {
                        addProperty("projection", projectionJson)
                    }
                }
                addProperty("page", page)
                addProperty("limit", limit)
            }

            val request = buildRequest("/api/databases/$dbName/collections/$colName/query", "POST", gson.toJson(payload))
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val res = gson.fromJson(body, QueryResponse::class.java)
                    Result.success(res)
                } else {
                    Result.failure(IOException(parseErrorMessage(body) ?: "Query failed (${response.code})"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createDocument(dbName: String, colName: String, docJson: String): Result<GenericApiResponse> = withContext(Dispatchers.IO) {
        try {
            val docElement = gson.fromJson(docJson, JsonElement::class.java)
            val payload = JsonObject().apply {
                if (docElement.isJsonArray) {
                    add("documents", docElement)
                } else {
                    add("document", docElement)
                }
            }
            val request = buildRequest("/api/databases/$dbName/collections/$colName/documents", "POST", gson.toJson(payload))
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                val res = gson.fromJson(body, GenericApiResponse::class.java)
                if (response.isSuccessful) Result.success(res)
                else Result.failure(IOException(res?.error ?: parseErrorMessage(body) ?: "Insert document failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateDocument(dbName: String, colName: String, docId: String, docJson: String): Result<GenericApiResponse> = withContext(Dispatchers.IO) {
        try {
            val docElement = gson.fromJson(docJson, JsonElement::class.java)
            val payload = JsonObject().apply { add("document", docElement) }
            val request = buildRequest("/api/databases/$dbName/collections/$colName/documents/$docId", "PUT", gson.toJson(payload))
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                val res = gson.fromJson(body, GenericApiResponse::class.java)
                if (response.isSuccessful) Result.success(res)
                else Result.failure(IOException(res?.error ?: parseErrorMessage(body) ?: "Update document failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteDocument(dbName: String, colName: String, docId: String): Result<GenericApiResponse> = withContext(Dispatchers.IO) {
        try {
            val request = buildRequest("/api/databases/$dbName/collections/$colName/documents/$docId", "DELETE")
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                val res = gson.fromJson(body, GenericApiResponse::class.java)
                if (response.isSuccessful) Result.success(res)
                else Result.failure(IOException(res?.error ?: parseErrorMessage(body) ?: "Delete document failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun aggregate(dbName: String, colName: String, pipelineJson: String): Result<AggregateResponse> = withContext(Dispatchers.IO) {
        try {
            val pipeElement = gson.fromJson(pipelineJson, JsonElement::class.java)
            val payload = JsonObject().apply { add("pipeline", pipeElement) }
            val request = buildRequest("/api/databases/$dbName/collections/$colName/aggregate", "POST", gson.toJson(payload))
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val res = gson.fromJson(body, AggregateResponse::class.java)
                    Result.success(res)
                } else {
                    val errMsg = parseErrorMessage(body) ?: "Aggregation pipeline failed (${response.code})"
                    Result.failure(IOException(errMsg))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getIndexes(dbName: String, colName: String): Result<List<IndexInfo>> = withContext(Dispatchers.IO) {
        try {
            val request = buildRequest("/api/databases/$dbName/collections/$colName/indexes", "GET")
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: "[]"
                if (response.isSuccessful) {
                    val listType = object : TypeToken<List<IndexInfo>>() {}.type
                    val list: List<IndexInfo> = gson.fromJson(body, listType)
                    Result.success(list)
                } else {
                    Result.failure(IOException(parseErrorMessage(body) ?: "Failed to fetch indexes (${response.code})"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createIndex(dbName: String, colName: String, keysJson: String, optionsJson: String?): Result<GenericApiResponse> = withContext(Dispatchers.IO) {
        try {
            val keysElement = gson.fromJson(keysJson, JsonElement::class.java)
            val payload = JsonObject().apply {
                add("keys", keysElement)
                if (!optionsJson.isNullOrBlank()) {
                    add("options", gson.fromJson(optionsJson, JsonElement::class.java))
                }
            }
            val request = buildRequest("/api/databases/$dbName/collections/$colName/indexes", "POST", gson.toJson(payload))
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                val res = gson.fromJson(body, GenericApiResponse::class.java)
                if (response.isSuccessful) Result.success(res)
                else Result.failure(IOException(res?.error ?: parseErrorMessage(body) ?: "Create index failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun dropIndex(dbName: String, colName: String, indexName: String): Result<GenericApiResponse> = withContext(Dispatchers.IO) {
        try {
            val request = buildRequest("/api/databases/$dbName/collections/$colName/indexes/$indexName", "DELETE")
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                val res = gson.fromJson(body, GenericApiResponse::class.java)
                if (response.isSuccessful) Result.success(res)
                else Result.failure(IOException(res?.error ?: parseErrorMessage(body) ?: "Drop index failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun executeRawCommand(dbName: String, commandJson: String): Result<GenericApiResponse> = withContext(Dispatchers.IO) {
        try {
            val cmdElement = gson.fromJson(commandJson, JsonElement::class.java)
            val payload = JsonObject().apply {
                addProperty("dbName", dbName)
                add("command", cmdElement)
            }
            val request = buildRequest("/api/raw-command", "POST", gson.toJson(payload))
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                val res = gson.fromJson(body, GenericApiResponse::class.java)
                if (response.isSuccessful) Result.success(res)
                else Result.failure(IOException(res?.error ?: parseErrorMessage(body) ?: "Command execution failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseErrorMessage(json: String): String? {
        return try {
            val obj = gson.fromJson(json, JsonObject::class.java)
            obj.get("error")?.asString ?: obj.get("message")?.asString
        } catch (e: Exception) {
            null
        }
    }
}

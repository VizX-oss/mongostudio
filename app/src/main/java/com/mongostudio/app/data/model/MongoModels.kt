package com.mongostudio.app.data.model

import com.google.gson.annotations.SerializedName

data class ConnectRequest(
    @SerializedName("uri") val uri: String,
    @SerializedName("name") val name: String? = null,
    @SerializedName("saveConnection") val saveConnection: Boolean = false
)

data class ConnectResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("connectionId") val connectionId: String? = null,
    @SerializedName("serverVersion") val serverVersion: String? = null,
    @SerializedName("savedId") val savedId: String? = null,
    @SerializedName("error") val error: String? = null
)

data class SavedConnection(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("maskedUri") val maskedUri: String,
    @SerializedName("savedAt") val savedAt: String
)

data class ClusterOverview(
    @SerializedName("totalSize") val totalSize: Long = 0L,
    @SerializedName("databases") val databases: List<DatabaseInfo> = emptyList(),
    @SerializedName("serverStatus") val serverStatus: ServerStatus? = null
)

data class DatabaseInfo(
    @SerializedName("name") val name: String,
    @SerializedName("sizeOnDisk") val sizeOnDisk: Long = 0L,
    @SerializedName("empty") val empty: Boolean = false,
    @SerializedName("collectionsCount") val collectionsCount: Int = 0,
    @SerializedName("objectsCount") val objectsCount: Long = 0L,
    @SerializedName("dataSize") val dataSize: Long = 0L,
    @SerializedName("storageSize") val storageSize: Long = 0L,
    @SerializedName("indexes") val indexes: Int = 0,
    @SerializedName("indexSize") val indexSize: Long = 0L
) {
    val isSystemDb: Boolean
        get() = name in listOf("admin", "local", "config")
}

data class ServerStatus(
    @SerializedName("version") val version: String? = null,
    @SerializedName("uptime") val uptime: Long? = null,
    @SerializedName("host") val host: String? = null,
    @SerializedName("connections") val connections: Map<String, Any>? = null,
    @SerializedName("mem") val mem: Map<String, Any>? = null
)

data class CollectionInfo(
    @SerializedName("name") val name: String,
    @SerializedName("type") val type: String = "collection",
    @SerializedName("docCount") val docCount: Long = 0L,
    @SerializedName("size") val size: Long = 0L,
    @SerializedName("storageSize") val storageSize: Long = 0L,
    @SerializedName("totalIndexSize") val totalIndexSize: Long = 0L,
    @SerializedName("nindexes") val nindexes: Int = 0
)

data class QueryRequest(
    @SerializedName("filter") val filter: Any? = null,
    @SerializedName("sort") val sort: Any? = null,
    @SerializedName("projection") val projection: Any? = null,
    @SerializedName("page") val page: Int = 1,
    @SerializedName("limit") val limit: Int = 20
)

data class QueryResponse(
    @SerializedName("total") val total: Long = 0L,
    @SerializedName("page") val page: Int = 1,
    @SerializedName("limit") val limit: Int = 20,
    @SerializedName("totalPages") val totalPages: Int = 1,
    @SerializedName("documents") val documents: List<Map<String, Any?>> = emptyList(),
    @SerializedName("error") val error: String? = null
)

data class AggregateResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("count") val count: Int = 0,
    @SerializedName("results") val results: List<Map<String, Any?>> = emptyList(),
    @SerializedName("error") val error: String? = null
)

data class IndexInfo(
    @SerializedName("v") val v: Int? = null,
    @SerializedName("key") val key: Map<String, Any>? = null,
    @SerializedName("name") val name: String = "",
    @SerializedName("unique") val unique: Boolean? = null
)

data class GenericApiResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("error") val error: String? = null,
    @SerializedName("insertedId") val insertedId: Any? = null,
    @SerializedName("modifiedCount") val modifiedCount: Int? = null,
    @SerializedName("deletedCount") val deletedCount: Int? = null,
    @SerializedName("result") val result: Any? = null
)

object FormatUtils {
    fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        val index = digitGroups.coerceIn(0, units.size - 1)
        val value = bytes / Math.pow(1024.0, index.toDouble())
        return String.format("%.2f %s", value, units[index])
    }

    fun formatUptime(seconds: Long?): String {
        if (seconds == null || seconds <= 0) return "Unknown"
        val days = seconds / 86400
        val hours = (seconds % 86400) / 3600
        val mins = (seconds % 3600) / 60
        return when {
            days > 0 -> "${days}d ${hours}h"
            hours > 0 -> "${hours}h ${mins}m"
            else -> "${mins}m ${seconds % 60}s"
        }
    }
}

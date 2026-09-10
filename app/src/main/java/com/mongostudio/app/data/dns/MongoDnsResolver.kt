package com.mongostudio.app.data.dns

import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

object MongoDnsResolver {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    /**
     * Resolves a mongodb+srv:// URI to a standard mongodb:// URI with explicit hosts and SSL/options.
     * If the URI is already mongodb://, it is returned unchanged.
     */
    suspend fun resolveUri(uriString: String): String = withContext(Dispatchers.IO) {
        val trimmed = uriString.trim()
        if (!trimmed.startsWith("mongodb+srv://", ignoreCase = true)) {
            return@withContext trimmed
        }

        try {
            // Format: mongodb+srv://[user:pass@]host[:port][/database][?options]
            val afterScheme = trimmed.substring("mongodb+srv://".length)
            
            // Extract credentials if present
            val atIndex = afterScheme.indexOf('@')
            val credentials = if (atIndex != -1) afterScheme.substring(0, atIndex) else ""
            val hostAndRest = if (atIndex != -1) afterScheme.substring(atIndex + 1) else afterScheme

            // Split host from path and query
            val slashIndex = hostAndRest.indexOf('/')
            val qIndex = hostAndRest.indexOf('?')
            val hostEnd = when {
                slashIndex != -1 -> slashIndex
                qIndex != -1 -> qIndex
                else -> hostAndRest.length
            }

            val originalHost = hostAndRest.substring(0, hostEnd).trim()
            val remainingPathAndQuery = hostAndRest.substring(hostEnd)

            val srvRecordName = "_mongodb._tcp.$originalHost"
            val hosts = querySrvHosts(srvRecordName)
            if (hosts.isEmpty()) {
                // If SRV lookup fails, return original and let driver attempt connection
                return@withContext trimmed
            }

            val hostsJoined = hosts.joinToString(",")
            val txtOptions = queryTxtOptions(originalHost)

            // Combine path and query
            val pathPart: String
            val existingQuery: String
            if (remainingPathAndQuery.startsWith('/')) {
                val nextQ = remainingPathAndQuery.indexOf('?')
                if (nextQ != -1) {
                    pathPart = remainingPathAndQuery.substring(0, nextQ)
                    existingQuery = remainingPathAndQuery.substring(nextQ + 1)
                } else {
                    pathPart = remainingPathAndQuery
                    existingQuery = ""
                }
            } else if (remainingPathAndQuery.startsWith('?')) {
                pathPart = "/"
                existingQuery = remainingPathAndQuery.substring(1)
            } else {
                pathPart = "/"
                existingQuery = ""
            }

            val queryParams = mutableListOf<String>()
            queryParams.add("ssl=true")

            if (txtOptions.isNotBlank()) {
                val txtParams = txtOptions.split("&").filter { it.isNotBlank() }
                queryParams.addAll(txtParams)
            }
            if (existingQuery.isNotBlank()) {
                val userParams = existingQuery.split("&").filter { it.isNotBlank() }
                for (param in userParams) {
                    val key = param.substringBefore('=')
                    // Prevent duplicate parameters from existingQuery overriding TXT unless intended
                    if (!queryParams.any { it.startsWith("$key=", ignoreCase = true) }) {
                        queryParams.add(param)
                    }
                }
            }

            val finalQuery = queryParams.distinct().joinToString("&")
            val userInfo = if (credentials.isNotBlank()) "$credentials@" else ""

            return@withContext "mongodb://$userInfo$hostsJoined$pathPart?$finalQuery"
        } catch (e: Exception) {
            // Fallback to original URI on parsing/resolution error
            trimmed
        }
    }

    private fun querySrvHosts(recordName: String): List<String> {
        val googleUrl = "https://dns.google/resolve?name=$recordName&type=SRV"
        val cloudflareUrl = "https://cloudflare-dns.com/dns-query?name=$recordName&type=SRV"
        val quad9Url = "https://dns.quad9.net:5053/dns-query?name=$recordName&type=SRV"

        val hostsGoogle = parseSrvFromUrl(googleUrl)
        if (hostsGoogle.isNotEmpty()) return hostsGoogle

        val hostsCloudflare = parseSrvFromUrl(cloudflareUrl, isCloudflare = true)
        if (hostsCloudflare.isNotEmpty()) return hostsCloudflare

        return parseSrvFromUrl(quad9Url, isCloudflare = true)
    }

    private fun parseSrvFromUrl(url: String, isCloudflare: Boolean = false): List<String> {
        val list = mutableListOf<String>()
        try {
            val reqBuilder = Request.Builder().url(url).get()
            if (isCloudflare) {
                reqBuilder.addHeader("accept", "application/dns-json")
            }
            client.newCall(reqBuilder.build()).execute().use { response ->
                if (!response.isSuccessful) return emptyList()
                val body = response.body?.string() ?: return emptyList()
                val obj = gson.fromJson(body, JsonObject::class.java)
                val answers = obj.getAsJsonArray("Answer") ?: return emptyList()

                for (item in answers) {
                    val ans = item.asJsonObject
                    val data = ans.get("data")?.asString ?: continue
                    // SRV data format: "priority weight port target."
                    val parts = data.trim().split("\\s+".toRegex())
                    if (parts.size >= 4) {
                        val port = parts[2]
                        val target = parts[3].trimEnd('.')
                        list.add("$target:$port")
                    }
                }
            }
        } catch (e: Exception) {
            // ignore network exceptions in fallback
        }
        return list
    }

    private fun queryTxtOptions(host: String): String {
        val googleUrl = "https://dns.google/resolve?name=$host&type=TXT"
        val cloudflareUrl = "https://cloudflare-dns.com/dns-query?name=$host&type=TXT"

        val resGoogle = parseTxtFromUrl(googleUrl)
        if (resGoogle.isNotBlank()) return resGoogle

        return parseTxtFromUrl(cloudflareUrl, isCloudflare = true)
    }

    private fun parseTxtFromUrl(url: String, isCloudflare: Boolean = false): String {
        try {
            val reqBuilder = Request.Builder().url(url).get()
            if (isCloudflare) {
                reqBuilder.addHeader("accept", "application/dns-json")
            }
            client.newCall(reqBuilder.build()).execute().use { response ->
                if (!response.isSuccessful) return ""
                val body = response.body?.string() ?: return ""
                val obj = gson.fromJson(body, JsonObject::class.java)
                val answers = obj.getAsJsonArray("Answer") ?: return ""

                val sb = StringBuilder()
                for (item in answers) {
                    val ans = item.asJsonObject
                    var data = ans.get("data")?.asString ?: continue
                    data = data.replace("\"", "").trim()
                    if (data.isNotBlank()) {
                        if (sb.isNotEmpty()) sb.append("&")
                        sb.append(data)
                    }
                }
                return sb.toString()
            }
        } catch (e: Exception) {
            return ""
        }
    }
}

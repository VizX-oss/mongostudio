package com.mongostudio.app

import com.google.gson.Gson
import com.mongostudio.app.data.model.ConnectRequest
import com.mongostudio.app.data.model.DatabaseInfo
import com.mongostudio.app.data.model.SavedConnection
import org.junit.Assert.*
import org.junit.Test

class ModelTest {
    private val gson = Gson()

    @Test
    fun testConnectRequestSerialization() {
        val req = ConnectRequest(uri = "mongodb://localhost:27017", name = "Local", saveConnection = true)
        val json = gson.toJson(req)
        assertTrue(json.contains("mongodb://localhost:27017"))
        assertTrue(json.contains("Local"))
        assertTrue(json.contains("saveConnection"))
    }

    @Test
    fun testDatabaseInfoSystemDbDetection() {
        val adminDb = DatabaseInfo(name = "admin", sizeOnDisk = 1024)
        val userDb = DatabaseInfo(name = "production_users", sizeOnDisk = 50000)

        assertTrue(adminDb.isSystemDb)
        assertFalse(userDb.isSystemDb)
    }

    @Test
    fun testSavedConnectionDeserialization() {
        val json = """
        {
            "id": "abc-123",
            "name": "Atlas Cluster",
            "maskedUri": "mongodb+srv://admin:••••••••@cluster.net",
            "savedAt": "2026-09-08T00:00:00Z"
        }
        """.trimIndent()

        val item = gson.fromJson(json, SavedConnection::class.java)
        assertEquals("abc-123", item.id)
        assertEquals("Atlas Cluster", item.name)
        assertTrue(item.maskedUri.contains("••••••••"))
    }
}

package com.mongostudio.app

import com.mongostudio.app.ui.navigation.Screen
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class NavigationRoutesTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testObjectRoutesSerialization() {
        val connJson = json.encodeToString(Screen.Connection)
        val decodedConn = json.decodeFromString<Screen.Connection>(connJson)
        assertEquals(Screen.Connection, decodedConn)

        val dashJson = json.encodeToString(Screen.Dashboard)
        val decodedDash = json.decodeFromString<Screen.Dashboard>(dashJson)
        assertEquals(Screen.Dashboard, decodedDash)

        val consoleJson = json.encodeToString(Screen.Console)
        val decodedConsole = json.decodeFromString<Screen.Console>(consoleJson)
        assertEquals(Screen.Console, decodedConsole)

        val settingsJson = json.encodeToString(Screen.Settings)
        val decodedSettings = json.decodeFromString<Screen.Settings>(settingsJson)
        assertEquals(Screen.Settings, decodedSettings)
    }

    @Test
    fun testParameterizedRoutesSerialization() {
        val dbRoute = Screen.DatabaseDetail(dbName = "production_db")
        val dbJson = json.encodeToString(dbRoute)
        assertTrue(dbJson.contains("production_db"))
        val decodedDb = json.decodeFromString<Screen.DatabaseDetail>(dbJson)
        assertEquals("production_db", decodedDb.dbName)

        val docRoute = Screen.Documents(dbName = "production_db", colName = "users")
        val docJson = json.encodeToString(docRoute)
        assertTrue(docJson.contains("production_db"))
        assertTrue(docJson.contains("users"))
        val decodedDoc = json.decodeFromString<Screen.Documents>(docJson)
        assertEquals("production_db", decodedDoc.dbName)
        assertEquals("users", decodedDoc.colName)

        val aggRoute = Screen.Aggregation(dbName = "analytics_db", colName = "events")
        val aggJson = json.encodeToString(aggRoute)
        val decodedAgg = json.decodeFromString<Screen.Aggregation>(aggJson)
        assertEquals("analytics_db", decodedAgg.dbName)
        assertEquals("events", decodedAgg.colName)

        val indexRoute = Screen.Indexes(dbName = "catalog_db", colName = "products")
        val indexJson = json.encodeToString(indexRoute)
        val decodedIndex = json.decodeFromString<Screen.Indexes>(indexJson)
        assertEquals("catalog_db", decodedIndex.dbName)
        assertEquals("products", decodedIndex.colName)
    }
}

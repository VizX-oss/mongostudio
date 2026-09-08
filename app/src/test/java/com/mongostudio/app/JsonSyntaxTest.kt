package com.mongostudio.app

import com.mongostudio.app.ui.components.JsonSyntaxHighlighter
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class JsonSyntaxTest {

    @Test
    fun testHighlightSimpleJson() {
        val sampleJson = """{"name": "MongoStudio", "version": 1, "active": true}"""
        val result = JsonSyntaxHighlighter.highlightJson(sampleJson)
        assertNotNull(result)
        assertTrue(result.text.contains("MongoStudio"))
        assertTrue(result.text.contains("1"))
        assertTrue(result.text.contains("true"))
    }

    @Test
    fun testFormatAndHighlightMap() {
        val map = mapOf(
            "cluster" to "Production",
            "count" to 42,
            "replicaSet" to true,
            "tags" to listOf("database", "mobile")
        )
        val result = JsonSyntaxHighlighter.formatAndHighlight(map)
        assertNotNull(result)
        assertTrue(result.text.contains("Production"))
        assertTrue(result.text.contains("42"))
    }
}

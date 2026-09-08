package com.mongostudio.app

import com.mongostudio.app.data.dns.MongoDnsResolver
import kotlinx.coroutines.runBlocking
import org.bson.Document
import org.bson.types.ObjectId
import org.junit.Assert.*
import org.junit.Test

class StandaloneMongoTest {

    @Test
    fun testStandardUriPassthrough() = runBlocking {
        val standardUri = "mongodb://admin:pass123@host1:27017,host2:27017/myDb?replicaSet=rs0"
        val resolved = MongoDnsResolver.resolveUri(standardUri)
        assertEquals(standardUri, resolved)
    }

    @Test
    fun testBsonDocumentParsing() {
        val json = """{"name": "MongoStudio Standalone", "version": 1, "active": true}"""
        val doc = Document.parse(json)
        assertEquals("MongoStudio Standalone", doc.getString("name"))
        assertEquals(1, doc.getInteger("version"))
        assertEquals(true, doc.getBoolean("active"))
    }

    @Test
    fun testBsonObjectIdHandling() {
        val hex = "507f1f77bcf86cd799439011"
        val oid = ObjectId(hex)
        assertEquals(hex, oid.toHexString())
        assertTrue(ObjectId.isValid(hex))
        assertFalse(ObjectId.isValid("not-a-valid-hex-id"))
    }

    @Test
    fun testSaslClassesResolution() {
        val saslClientClass = javax.security.sasl.SaslClient::class.java
        assertTrue(saslClientClass.isInterface)

        val exception = javax.security.sasl.AuthenticationException("Auth failed test")
        assertTrue(exception is javax.security.sasl.SaslException)
        assertEquals("Auth failed test", exception.message)

        assertEquals("javax.security.sasl.qop", javax.security.sasl.Sasl.QOP)
    }
}


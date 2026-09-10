package com.mongostudio.app

import com.mongostudio.app.data.vault.EncryptedVault
import org.junit.Assert.*
import org.junit.Test

class EncryptedVaultTest {

    @Test
    fun testMaskUriWithPassword() {
        val vault = EncryptedVault()
        val originalUri = "mongodb+srv://user_alt:SuperSecret123@2nd-alt-ub.9zwqoto.mongodb.net/?appName=2nd-Alt-UB"
        val masked = vault.maskUri(originalUri)

        assertFalse("Masked URI must not contain the original password", masked.contains("SuperSecret123"))
        assertTrue("Masked URI must contain bullet placeholder", masked.contains("••••••••"))
        assertTrue("Masked URI must preserve username", masked.contains("user_alt"))
        assertTrue("Masked URI must preserve host", masked.contains("2nd-alt-ub.9zwqoto.mongodb.net"))
    }

    @Test
    fun testSaveAndDecryptConnection() {
        val vault = EncryptedVault()
        val originalUri = "mongodb+srv://admin:pass456@2nd-alt-ub.9zwqoto.mongodb.net/production?appName=2nd-Alt-UB"
        val saved = vault.saveConnection("Production Cluster", originalUri, colorTag = "emerald")

        assertNotNull(saved.id)
        assertEquals("Production Cluster", saved.name)
        assertEquals("emerald", saved.colorTag)
        assertTrue(saved.maskedUri.contains("••••••••"))

        val connections = vault.getSavedConnections()
        assertEquals(1, connections.size)
        assertEquals(saved.id, connections[0].id)

        val decrypted = vault.getDecryptedUri(saved.id)
        assertEquals(originalUri, decrypted)
    }

    @Test
    fun testUpdateConnection() {
        val vault = EncryptedVault()
        val originalUri = "mongodb+srv://admin:pass456@2nd-alt-ub.9zwqoto.mongodb.net/test?appName=2nd-Alt-UB"
        val saved = vault.saveConnection("Test Cluster", originalUri, colorTag = "emerald")

        val updatedUri = "mongodb+srv://admin:NewPass999@2nd-alt-ub.9zwqoto.mongodb.net/prod?appName=2nd-Alt-UB"
        val updateSuccess = vault.updateConnection(saved.id, "Renamed Cluster", updatedUri, colorTag = "blue")

        assertTrue(updateSuccess)
        val connections = vault.getSavedConnections()
        assertEquals("Renamed Cluster", connections[0].name)
        assertEquals("blue", connections[0].colorTag)

        val decrypted = vault.getDecryptedUri(saved.id)
        assertEquals(updatedUri, decrypted)
    }

    @Test
    fun testDeleteConnection() {
        val vault = EncryptedVault()
        val uri = "mongodb+srv://admin:pass@2nd-alt-ub.9zwqoto.mongodb.net/?appName=2nd-Alt-UB"
        val saved = vault.saveConnection("Temp Cluster", uri)

        assertEquals(1, vault.getSavedConnections().size)
        val deleted = vault.deleteConnection(saved.id)
        assertTrue(deleted)
        assertEquals(0, vault.getSavedConnections().size)
        assertNull(vault.getDecryptedUri(saved.id))
    }
}

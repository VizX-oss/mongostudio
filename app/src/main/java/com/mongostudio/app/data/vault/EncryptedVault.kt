package com.mongostudio.app.data.vault

import android.content.Context
import android.util.Base64
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mongostudio.app.data.model.SavedConnection
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

data class VaultEntry(
    val id: String,
    val name: String,
    val maskedUri: String,
    val encryptedUri: String,
    val savedAt: String
)

class EncryptedVault(context: Context) {
    private val prefs = context.getSharedPreferences("mongostudio_vault_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val secretKey: SecretKeySpec

    init {
        val masterPass = "MongoStudioMasterKey_2026_SecureVault_v1".toCharArray()
        val salt = "FixedSaltForDeviceLocalVault_2026".toByteArray()
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(masterPass, salt, 1000, 256)
        val tmp = factory.generateSecret(spec)
        secretKey = SecretKeySpec(tmp.encoded, "AES")
    }

    private fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val iv = ByteArray(16)
        SecureRandom().nextBytes(iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))
        val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        val combined = iv + encrypted
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    private fun decrypt(cipherText: String): String {
        val combined = Base64.decode(cipherText, Base64.NO_WRAP)
        val iv = combined.copyOfRange(0, 16)
        val encrypted = combined.copyOfRange(16, combined.size)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
        val original = cipher.doFinal(encrypted)
        return String(original, Charsets.UTF_8)
    }

    private fun maskUri(uri: String): String {
        return uri.replace(Regex("//([^:]+):([^@]+)@"), "//$1:••••••••@")
    }

    @Synchronized
    fun getSavedConnections(): List<SavedConnection> {
        val raw = prefs.getString("entries", "[]") ?: "[]"
        val listType = object : TypeToken<List<VaultEntry>>() {}.type
        val entries: List<VaultEntry> = try {
            gson.fromJson(raw, listType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
        return entries.map {
            SavedConnection(
                id = it.id,
                name = it.name,
                maskedUri = it.maskedUri,
                savedAt = it.savedAt
            )
        }
    }

    @Synchronized
    fun saveConnection(name: String, uri: String): SavedConnection {
        val raw = prefs.getString("entries", "[]") ?: "[]"
        val listType = object : TypeToken<MutableList<VaultEntry>>() {}.type
        val entries: MutableList<VaultEntry> = try {
            gson.fromJson(raw, listType) ?: mutableListOf()
        } catch (e: Exception) {
            mutableListOf()
        }

        val id = UUID.randomUUID().toString()
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val dateStr = isoFormat.format(Date())
        val masked = maskUri(uri)
        val encrypted = encrypt(uri)

        val newEntry = VaultEntry(
            id = id,
            name = name.ifBlank { "MongoDB Cluster" },
            maskedUri = masked,
            encryptedUri = encrypted,
            savedAt = dateStr
        )
        entries.add(0, newEntry)
        prefs.edit().putString("entries", gson.toJson(entries)).apply()

        return SavedConnection(
            id = id,
            name = newEntry.name,
            maskedUri = masked,
            savedAt = dateStr
        )
    }

    @Synchronized
    fun getDecryptedUri(id: String): String? {
        val raw = prefs.getString("entries", "[]") ?: "[]"
        val listType = object : TypeToken<List<VaultEntry>>() {}.type
        val entries: List<VaultEntry> = try {
            gson.fromJson(raw, listType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
        val target = entries.find { it.id == id } ?: return null
        return try {
            decrypt(target.encryptedUri)
        } catch (e: Exception) {
            null
        }
    }

    @Synchronized
    fun deleteConnection(id: String): Boolean {
        val raw = prefs.getString("entries", "[]") ?: "[]"
        val listType = object : TypeToken<MutableList<VaultEntry>>() {}.type
        val entries: MutableList<VaultEntry> = try {
            gson.fromJson(raw, listType) ?: mutableListOf()
        } catch (e: Exception) {
            mutableListOf()
        }
        val initialSize = entries.size
        entries.removeAll { it.id == id }
        if (entries.size != initialSize) {
            prefs.edit().putString("entries", gson.toJson(entries)).apply()
            return true
        }
        return false
    }
}

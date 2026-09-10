package com.mongostudio.app.data.vault

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.util.Base64
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mongostudio.app.data.model.SavedConnection
import java.security.KeyStore
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

data class VaultEntry(
    val id: String,
    val name: String,
    val maskedUri: String,
    val encryptedUri: String,
    val savedAt: String,
    val colorTag: String = "emerald"
)

class EncryptedVault(private val prefs: SharedPreferences?) {
    constructor(context: Context) : this(
        context.getSharedPreferences("mongostudio_vault_prefs", Context.MODE_PRIVATE)
    )
    constructor() : this(null)

    private var inMemoryEntriesJson: String = "[]"
    private val gson = Gson()
    private val isAndroidKeyStoreAvailable: Boolean
    private var fallbackKey: SecretKeySpec? = null

    companion object {
        private const val ANDROID_KEY_STORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "MongoStudio_MasterVault_Key_v2"
        private const val GCM_TAG_LENGTH = 128
        private const val GCM_IV_LENGTH = 12
    }

    init {
        var keyStoreReady = false
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
            keyStore.load(null)
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                val keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
                val keyGenSpec = KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
                keyGen.init(keyGenSpec)
                keyGen.generateKey()
            }
            keyStoreReady = true
        } catch (e: Throwable) {
            // AndroidKeyStore is unavailable on JVM unit test runner or older environment
            keyStoreReady = false
        }
        isAndroidKeyStoreAvailable = keyStoreReady

        if (!isAndroidKeyStoreAvailable) {
            val masterPass = "MongoStudioMasterKey_2026_SecureVault_v1".toCharArray()
            val salt = "FixedSaltForDeviceLocalVault_2026".toByteArray()
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val spec = PBEKeySpec(masterPass, salt, 1000, 256)
            val tmp = factory.generateSecret(spec)
            fallbackKey = SecretKeySpec(tmp.encoded, "AES")
        }
    }

    private fun readEntriesJson(): String {
        return prefs?.getString("entries", "[]") ?: inMemoryEntriesJson
    }

    private fun writeEntriesJson(json: String) {
        if (prefs != null) {
            prefs.edit().putString("entries", json).apply()
        } else {
            inMemoryEntriesJson = json
        }
    }

    private fun encrypt(plainText: String): String {
        return if (isAndroidKeyStoreAvailable) {
            try {
                val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
                keyStore.load(null)
                val secretKey = keyStore.getKey(KEY_ALIAS, null) as SecretKey
                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                cipher.init(Cipher.ENCRYPT_MODE, secretKey)
                val iv = cipher.iv
                val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
                val combined = iv + encrypted
                "GCM:" + Base64.getEncoder().encodeToString(combined)
            } catch (e: Exception) {
                encryptWithFallback(plainText)
            }
        } else {
            encryptWithFallback(plainText)
        }
    }

    private fun encryptWithFallback(plainText: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val iv = ByteArray(16)
        SecureRandom().nextBytes(iv)
        cipher.init(Cipher.ENCRYPT_MODE, fallbackKey, IvParameterSpec(iv))
        val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        val combined = iv + encrypted
        return "CBC:" + Base64.getEncoder().encodeToString(combined)
    }

    private fun decrypt(cipherText: String): String {
        return if (cipherText.startsWith("GCM:")) {
            val raw = cipherText.substring(4)
            val combined = Base64.getDecoder().decode(raw)
            val iv = combined.copyOfRange(0, GCM_IV_LENGTH)
            val encrypted = combined.copyOfRange(GCM_IV_LENGTH, combined.size)
            val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
            keyStore.load(null)
            val secretKey = keyStore.getKey(KEY_ALIAS, null) as SecretKey
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            String(cipher.doFinal(encrypted), Charsets.UTF_8)
        } else {
            val raw = if (cipherText.startsWith("CBC:")) cipherText.substring(4) else cipherText
            val combined = Base64.getDecoder().decode(raw)
            val iv = combined.copyOfRange(0, 16)
            val encrypted = combined.copyOfRange(16, combined.size)
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, fallbackKey, IvParameterSpec(iv))
            String(cipher.doFinal(encrypted), Charsets.UTF_8)
        }
    }

    fun maskUri(uri: String): String {
        return uri.replace(Regex("//([^:]+):([^@]+)@"), "//$1:••••••••@")
    }

    @Synchronized
    fun getSavedConnections(): List<SavedConnection> {
        val raw = readEntriesJson()
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
                savedAt = it.savedAt,
                colorTag = it.colorTag
            )
        }
    }

    @Synchronized
    fun saveConnection(name: String, uri: String, colorTag: String = "emerald"): SavedConnection {
        val raw = readEntriesJson()
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
            savedAt = dateStr,
            colorTag = colorTag
        )
        entries.add(0, newEntry)
        writeEntriesJson(gson.toJson(entries))

        return SavedConnection(
            id = id,
            name = newEntry.name,
            maskedUri = masked,
            savedAt = dateStr,
            colorTag = newEntry.colorTag
        )
    }

    @Synchronized
    fun updateConnection(id: String, newName: String, newUri: String?, colorTag: String = "emerald"): Boolean {
        val raw = readEntriesJson()
        val listType = object : TypeToken<MutableList<VaultEntry>>() {}.type
        val entries: MutableList<VaultEntry> = try {
            gson.fromJson(raw, listType) ?: mutableListOf()
        } catch (e: Exception) {
            mutableListOf()
        }

        val index = entries.indexOfFirst { it.id == id }
        if (index == -1) return false

        val existing = entries[index]
        val updatedEncrypted = if (!newUri.isNullOrBlank()) encrypt(newUri) else existing.encryptedUri
        val updatedMasked = if (!newUri.isNullOrBlank()) maskUri(newUri) else existing.maskedUri

        entries[index] = existing.copy(
            name = newName.ifBlank { existing.name },
            maskedUri = updatedMasked,
            encryptedUri = updatedEncrypted,
            colorTag = colorTag
        )

        writeEntriesJson(gson.toJson(entries))
        return true
    }

    @Synchronized
    fun getDecryptedUri(id: String): String? {
        val raw = readEntriesJson()
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
        val raw = readEntriesJson()
        val listType = object : TypeToken<MutableList<VaultEntry>>() {}.type
        val entries: MutableList<VaultEntry> = try {
            gson.fromJson(raw, listType) ?: mutableListOf()
        } catch (e: Exception) {
            mutableListOf()
        }
        val initialSize = entries.size
        entries.removeAll { it.id == id }
        if (entries.size != initialSize) {
            writeEntriesJson(gson.toJson(entries))
            return true
        }
        return false
    }
}

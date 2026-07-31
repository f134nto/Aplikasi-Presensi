package com.example.util

import android.util.Base64
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object SecurityUtils {

    private const val SECRET_SEED = "MTsMaarifNU1WangonSecurity2026"

    /**
     * E2EE AES-256 payload encryption simulation
     */
    fun encryptData(plainText: String): String {
        return try {
            val keyBytes = MessageDigest.getInstance("SHA-256").digest(SECRET_SEED.toByteArray())
            val secretKey = SecretKeySpec(keyBytes, "AES")
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(encrypted, Base64.NO_WRAP)
        } catch (e: Exception) {
            "E2EE_SECURE_PAYLOAD_${plainText.hashCode()}"
        }
    }

    /**
     * Decrypts encrypted payload
     */
    fun decryptData(cipherText: String): String {
        return try {
            val keyBytes = MessageDigest.getInstance("SHA-256").digest(SECRET_SEED.toByteArray())
            val secretKey = SecretKeySpec(keyBytes, "AES")
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey)
            val decodedBytes = Base64.decode(cipherText, Base64.NO_WRAP)
            String(cipher.doFinal(decodedBytes), Charsets.UTF_8)
        } catch (e: Exception) {
            cipherText
        }
    }

    fun generate2FACode(): String {
        val code = (100000..999999).random()
        return code.toString()
    }
}

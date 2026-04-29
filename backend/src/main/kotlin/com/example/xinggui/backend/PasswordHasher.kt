package com.example.xinggui.backend

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class PasswordHasher(
    private val iterations: Int = 120_000,
    private val keyLength: Int = 256
) {
    private val random = SecureRandom()

    fun hash(password: String): String {
        require(password.isNotEmpty()) { "password must not be empty" }
        val salt = ByteArray(16).also(random::nextBytes)
        val derived = derive(password, salt, iterations, keyLength)
        return listOf(
            FORMAT,
            iterations.toString(),
            keyLength.toString(),
            encoder.encodeToString(salt),
            encoder.encodeToString(derived)
        ).joinToString(separator = "\$")
    }

    fun verify(password: String, storedHash: String): Boolean {
        val parts = storedHash.split('$')
        if (parts.size == 5 && parts[0] == FORMAT) {
            val storedIterations = parts[1].toIntOrNull() ?: return false
            val storedKeyLength = parts[2].toIntOrNull() ?: return false
            val salt = runCatching { decoder.decode(parts[3]) }.getOrNull() ?: return false
            val expected = runCatching { decoder.decode(parts[4]) }.getOrNull() ?: return false
            val actual = derive(password, salt, storedIterations, storedKeyLength)
            return MessageDigest.isEqual(expected, actual)
        }

        return verifyLegacyHash(password, parts)
    }

    fun needsRehash(storedHash: String): Boolean {
        val parts = storedHash.split('$')
        return parts.size != 5 ||
            parts[0] != FORMAT ||
            parts[1].toIntOrNull() != iterations ||
            parts[2].toIntOrNull() != keyLength
    }

    private fun verifyLegacyHash(password: String, parts: List<String>): Boolean {
        if (parts.size != 4 || parts[0] != LEGACY_FORMAT) {
            return false
        }
        val storedIterations = parts[1].toIntOrNull() ?: return false
        val salt = runCatching { decoder.decode(parts[2]) }.getOrNull() ?: return false
        val expected = runCatching { decoder.decode(parts[3]) }.getOrNull() ?: return false
        val actual = derive(password, salt, storedIterations, expected.size * 8)
        return MessageDigest.isEqual(expected, actual)
    }

    private fun derive(
        password: String,
        salt: ByteArray,
        iterations: Int,
        keyLength: Int
    ): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, keyLength)
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            .generateSecret(spec)
            .encoded
    }

    companion object {
        private const val FORMAT = "pbkdf2_sha256"
        private const val LEGACY_FORMAT = "pbkdf2"
        private val encoder: Base64.Encoder = Base64.getEncoder()
        private val decoder: Base64.Decoder = Base64.getDecoder()
    }
}

package com.example.xinggui.backend

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordHasherTest {
    @Test
    fun hashVerifiesOriginalPasswordOnly() {
        val hasher = PasswordHasher(iterations = 1_000, keyLength = 128)

        val hash = hasher.hash("Parent001")

        assertTrue(hasher.verify("Parent001", hash))
        assertFalse(hasher.verify("parent001", hash))
    }

    @Test
    fun hashUsesRandomSalt() {
        val hasher = PasswordHasher(iterations = 1_000, keyLength = 128)

        val first = hasher.hash("Teacher001")
        val second = hasher.hash("Teacher001")

        assertNotEquals(first, second)
        assertTrue(hasher.verify("Teacher001", first))
        assertTrue(hasher.verify("Teacher001", second))
    }

    @Test
    fun verifiesLegacySeedHash() {
        val hasher = PasswordHasher()
        val legacyHash = "pbkdf2\$120000\$qCUk9YkMbqROrNsdcG8c9A==\$sDMFzzcNU4jNT4e0RqEz+YD29PHg3XiTs+uIVviUVVQ="

        assertTrue(hasher.verify("parent001", legacyHash))
        assertFalse(hasher.verify("wrong-password", legacyHash))
        assertTrue(hasher.needsRehash(legacyHash))
    }

    @Test
    fun currentHashDoesNotNeedRehash() {
        val hasher = PasswordHasher(iterations = 1_000, keyLength = 128)

        val hash = hasher.hash("parent001")

        assertTrue(hasher.verify("parent001", hash))
        assertFalse(hasher.needsRehash(hash))
    }
}

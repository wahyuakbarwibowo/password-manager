package com.aminmart.passwordmanager.data.security

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for password hashing and verification.
 * Uses PBKDF2 with SHA-256 for secure password hashing.
 */
@Singleton
class PasswordHashingService @Inject constructor() {

    companion object {
        private const val PBKDF2_ITERATIONS = 100000
        private const val SALT_LENGTH = 32 // 256 bits
        private const val HASH_LENGTH = 32 // 256 bits
        private const val ALGORITHM = "PBKDF2WithHmacSHA256"
    }

    private val secureRandom = SecureRandom()

    /**
     * Hash a master password with a random salt.
     * Returns the salt and hash for storage.
     */
    suspend fun hashPassword(password: String): PasswordHash = withContext(Dispatchers.Default) {
        val salt = ByteArray(SALT_LENGTH).apply {
            secureRandom.nextBytes(this)
        }

        val hash = pbkdf2(password.toCharArray(), salt, PBKDF2_ITERATIONS, HASH_LENGTH * 8)

        PasswordHash(
            salt = Base64.encodeToString(salt, Base64.NO_WRAP),
            hash = Base64.encodeToString(hash, Base64.NO_WRAP)
        )
    }

    /**
     * Verify a password against a stored salt and hash.
     */
    suspend fun verifyPassword(password: String, salt: String, expectedHash: String): Boolean = withContext(Dispatchers.Default) {
        val saltBytes = Base64.decode(salt, Base64.NO_WRAP)
        val expectedHashBytes = Base64.decode(expectedHash, Base64.NO_WRAP)

        val computedHash = pbkdf2(password.toCharArray(), saltBytes, PBKDF2_ITERATIONS, HASH_LENGTH * 8)

        // Constant-time comparison to prevent timing attacks
        MessageDigest.isEqual(computedHash, expectedHashBytes)
    }

    private fun pbkdf2(
        password: CharArray,
        salt: ByteArray,
        iterations: Int,
        outputLength: Int
    ): ByteArray {
        val spec = javax.crypto.spec.PBEKeySpec(password, salt, iterations, outputLength)
        try {
            val factory = javax.crypto.SecretKeyFactory.getInstance(ALGORITHM)
            return factory.generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }
}

/**
 * Stored password hash with salt.
 */
data class PasswordHash(
    val salt: String,
    val hash: String
)

package com.budgetwise.app.utils

import java.security.MessageDigest

/**
 * Cryptographic utility for password hashing.
 *
 * Uses Java's built-in MessageDigest (SHA-256) — zero external dependencies.
 * Academic note: production apps should use Argon2 or bcrypt with per-user salt.
 * For this prototype, SHA-256 is acceptable per assignment requirements.
 */
object HashUtils {

    /**
     * Compute the SHA-256 hex digest of the input string.
     *
     * Algorithm:
     *   1. Encode the input as UTF-8 bytes.
     *   2. Feed bytes into MessageDigest("SHA-256").
     *   3. Format each output byte as a 2-character lowercase hex string.
     *   4. Concatenate all 32 bytes → 64-character hex string.
     *
     * Properties:
     * - Deterministic: same input always produces the same 64-char output.
     * - One-way: cannot recover the original input from the hash.
     * - Case-sensitive: sha256("abc") ≠ sha256("ABC").
     * - Handles empty string: sha256("") returns a valid 64-char hex string.
     *
     * @param input The plain-text string to hash (typically a password).
     * @return 64-character lowercase hex string of the SHA-256 digest.
     */
    fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        // %02x formats each byte as exactly 2 hex characters (e.g. 0x0F → "0f")
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}

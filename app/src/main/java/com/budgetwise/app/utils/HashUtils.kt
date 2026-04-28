package com.budgetwise.app.utils

import java.security.MessageDigest

/**
 * Cryptographic utilities for BudgetWise.
 *
 * SHA-256 is a one-way hash function — the original password cannot be
 * recovered from its hash. This is appropriate for a local offline prototype.
 * For a server-side production app, Argon2 or bcrypt with per-user salting
 * would be the industry standard — documented here for academic transparency.
 */
object HashUtils {
    fun sha256(input: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(input.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
}
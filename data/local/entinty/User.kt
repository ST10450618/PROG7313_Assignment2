package com.budgetwise.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing a registered BudgetWise user.
 *
 * Security note: Only the SHA-256 hash of the password is persisted — never
 * the plaintext credential. The UNIQUE index on [username] enforces one account
 * per username at the database layer, not just in application logic.
 */
@Entity(
    tableName = "users",
    indices   = [Index(value = ["username"], unique = true)]
)
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username    : String,
    val passwordHash: String,   // SHA-256 hex string — never plaintext
    val createdAt   : Long = System.currentTimeMillis()
)
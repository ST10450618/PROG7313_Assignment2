package com.budgetwise.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing the `users` table.
 *
 * Schema:
 *   id           INTEGER PRIMARY KEY AUTOINCREMENT
 *   username     TEXT NOT NULL  (UNIQUE index enforced via @Index below)
 *   passwordHash TEXT NOT NULL  (SHA-256 hex, 64 chars — NEVER plaintext)
 *   createdAt    INTEGER NOT NULL  (epoch ms, System.currentTimeMillis())
 *
 * The UNIQUE constraint on username is declared as an index so Room emits the
 * correct DDL. INSERT with OnConflictStrategy.ABORT fails if the username exists,
 * which the UserRepository uses to detect duplicate registrations without a
 * separate SELECT.
 */
@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true)]
)
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    /** The user's chosen display name — must be unique across all users. */
    val username: String,

    /** SHA-256 hex digest of the user's password. Computed by HashUtils.sha256(). */
    val passwordHash: String,

    /** Timestamp (epoch ms) when the account was created. */
    val createdAt: Long = System.currentTimeMillis()
)

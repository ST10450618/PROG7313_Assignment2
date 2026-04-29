package com.budgetwise.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing the `categories` table.
 *
 * Schema:
 *   id        INTEGER PRIMARY KEY AUTOINCREMENT
 *   userId    INTEGER NOT NULL  (FK → users.id ON DELETE CASCADE)
 *   name      TEXT NOT NULL
 *   colorHex  TEXT NOT NULL  (e.g. "#1B998B", "#E16162")
 *   createdAt INTEGER NOT NULL (epoch ms)
 *
 * Foreign key ON DELETE CASCADE means deleting a user removes all their categories.
 * The index on userId speeds up the per-user queries in CategoryDao.
 */
@Entity(
    tableName = "categories",
    foreignKeys = [
        ForeignKey(
            entity           = User::class,
            parentColumns    = ["id"],
            childColumns     = ["userId"],
            onDelete         = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    /** Owner of this category — must match a valid users.id. */
    val userId: Long,

    /** Display name for the category (e.g. "Groceries", "Transport"). */
    val name: String,

    /** Hex colour string with leading # (e.g. "#1B998B"). Shown as a circle in the UI. */
    val colorHex: String = "#1B998B",

    /** Timestamp (epoch ms) when the category was created. */
    val createdAt: Long = System.currentTimeMillis()
)

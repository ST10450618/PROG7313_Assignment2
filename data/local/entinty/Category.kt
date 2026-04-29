package com.budgetwise.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A user-defined expense category (e.g. "Groceries", "Transport").
 *
 * [colorHex] stores a hex colour string rendered as a coloured dot in the UI.
 * Foreign key CASCADE ensures all categories are removed when their owner is deleted,
 * preventing orphaned records in the database.
 */
@Entity(
    tableName    = "categories",
    foreignKeys  = [ForeignKey(
        entity        = User::class,
        parentColumns = ["id"],
        childColumns  = ["userId"],
        onDelete      = ForeignKey.CASCADE
    )],
    indices = [Index("userId")]
)
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId   : Long,
    val name     : String,
    val colorHex : String = "#1B998B",
    val createdAt: Long = System.currentTimeMillis()
)
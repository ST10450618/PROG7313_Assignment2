package com.budgetwise.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing a single expense record.
 *
 * [date] is stored as startOfDay(epochMs) so BETWEEN queries work correctly
 * across timezones — see DateUtils.startOfDay().
 *
 * [categoryId] is nullable: expenses become uncategorised if their category
 * is deleted (ON DELETE SET NULL).
 *
 * [photoUri] stores a content:// URI string produced by FileProvider — nullable
 * because attaching a receipt photo is optional.
 *
 * TODO (James): Verify field names match your DAO queries and AddExpenseScreen exactly.
 */
@Entity(
    tableName   = "expenses",
    foreignKeys = [
        ForeignKey(
            entity        = User::class,
            parentColumns = ["id"],
            childColumns  = ["userId"],
            onDelete      = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity        = Category::class,
            parentColumns = ["id"],
            childColumns  = ["categoryId"],
            onDelete      = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("userId"), Index("categoryId")]
)
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId     : Long,
    val categoryId : Long?,           // nullable — SET NULL on category delete
    val amount     : Double,
    val description: String,
    val date       : Long,            // startOfDay(epochMs) — use DateUtils.startOfDay()
    val startTime  : String,          // "HH:mm" e.g. "09:30"
    val endTime    : String,          // "HH:mm" e.g. "10:15" — must be > startTime
    val photoUri   : String? = null,  // content:// URI from FileProvider, nullable
    val createdAt  : Long = System.currentTimeMillis()
)

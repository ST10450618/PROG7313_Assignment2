package com.budgetwise.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing the `expenses` table.
 *
 * Schema:
 *   id          INTEGER PRIMARY KEY AUTOINCREMENT
 *   userId      INTEGER NOT NULL  (FK → users.id ON DELETE CASCADE)
 *   categoryId  INTEGER nullable  (FK → categories.id ON DELETE SET NULL)
 *   amount      REAL NOT NULL     (ZAR, stored as Double)
 *   description TEXT NOT NULL     (max 100 chars — enforced in AddExpenseScreen UI)
 *   date        INTEGER NOT NULL  (epoch ms, normalised to startOfDay() via DateUtils)
 *   startTime   TEXT NOT NULL     ("HH:mm" e.g. "09:30" — RUBRIC MANDATORY)
 *   endTime     TEXT NOT NULL     ("HH:mm" e.g. "10:15" — RUBRIC MANDATORY)
 *   photoUri    TEXT nullable     (content:// URI string, NULL if no photo)
 *   createdAt   INTEGER NOT NULL  (epoch ms)
 *
 * Key decisions:
 * - categoryId uses SET NULL so expenses survive category deletion (they become uncategorised)
 * - date is always stored as startOfDay(pickedDateMs) so BETWEEN SQL queries work correctly
 * - startTime / endTime are stored as "HH:mm" strings for simple display and comparison
 */
@Entity(
    tableName = "expenses",
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
    indices = [
        Index(value = ["userId"]),
        Index(value = ["categoryId"])
    ]
)
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    /** Owner of this expense — references users.id. */
    val userId: Long,

    /** FK to categories.id. Null when the linked category has been deleted (SET NULL). */
    val categoryId: Long? = null,

    /** Amount in South African Rand. Must be > 0 (validated in ExpenseViewModel). */
    val amount: Double,

    /** Human-readable description, max 100 characters. */
    val description: String,

    /**
     * Calendar date of the expense, normalised to local midnight (startOfDay).
     * Stored as epoch milliseconds. Using DateUtils.startOfDay() ensures the
     * BETWEEN :startDate AND :endDate SQL filter is timezone-correct.
     */
    val date: Long,

    /**
     * Time the expense/activity started, in "HH:mm" 24-hour format.
     * Required by the PROG7313 Part 2 rubric.
     */
    val startTime: String,

    /**
     * Time the expense/activity ended, in "HH:mm" 24-hour format.
     * Must be strictly after startTime (validated in ExpenseViewModel.isEndAfterStart()).
     * Required by the PROG7313 Part 2 rubric.
     */
    val endTime: String,

    /**
     * Content URI string of the receipt photo, or null if no photo was taken.
     * Stored as content://com.budgetwise.app.fileprovider/photos/receipt_TIMESTAMP.jpg
     */
    val photoUri: String? = null,

    /** Timestamp (epoch ms) when the record was inserted. */
    val createdAt: Long = System.currentTimeMillis()
)

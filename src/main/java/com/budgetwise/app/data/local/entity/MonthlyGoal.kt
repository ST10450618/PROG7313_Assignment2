package com.budgetwise.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing the `monthly_goals` table.
 *
 * Schema:
 *   id        INTEGER PRIMARY KEY AUTOINCREMENT
 *   userId    INTEGER NOT NULL  (FK → users.id ON DELETE CASCADE)
 *   month     INTEGER NOT NULL  (1–12, January=1)
 *   year      INTEGER NOT NULL  (full 4-digit year, e.g. 2026)
 *   minGoal   REAL NOT NULL     (minimum spend target in ZAR)
 *   maxGoal   REAL NOT NULL     (maximum spend cap in ZAR)
 *   updatedAt INTEGER NOT NULL  (epoch ms, last save time)
 *
 * UNIQUE INDEX on (userId, month, year): only one goal row per user per month.
 * INSERT with OnConflictStrategy.REPLACE implements true upsert behaviour.
 * GoalRepository.upsert() pre-fetches the existing PK to avoid auto-increment runaway.
 */
@Entity(
    tableName = "monthly_goals",
    foreignKeys = [
        ForeignKey(
            entity        = User::class,
            parentColumns = ["id"],
            childColumns  = ["userId"],
            onDelete      = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId", "month", "year"], unique = true)
    ]
)
data class MonthlyGoal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    /** Owner of this goal — references users.id. */
    val userId: Long,

    /** Calendar month (1 = January … 12 = December). */
    val month: Int,

    /** Full 4-digit calendar year (e.g. 2026). */
    val year: Int,

    /**
     * Minimum monthly spend target in ZAR.
     * SpendingStatus = UNDER_MIN when actual total < minGoal.
     * Must be ≥ 0 and < maxGoal (validated in GoalsViewModel.saveGoal()).
     */
    val minGoal: Double,

    /**
     * Maximum monthly spend cap in ZAR.
     * SpendingStatus = OVER_MAX when actual total > maxGoal.
     * Must be > minGoal (validated in GoalsViewModel.saveGoal()).
     */
    val maxGoal: Double,

    /** Epoch ms timestamp of the last save/upsert. */
    val updatedAt: Long = System.currentTimeMillis()
)

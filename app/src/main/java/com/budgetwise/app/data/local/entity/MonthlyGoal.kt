package com.budgetwise.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing a user's monthly spending goal.
 *
 * The UNIQUE index on (userId, month, year) enforces one goal per user per
 * calendar month. Combined with OnConflictStrategy.REPLACE in the DAO, this
 * provides a true upsert — insert if not exists, update if exists.
 *
 * [minGoal] = minimum spending target (e.g. save at least this much)
 * [maxGoal] = maximum spending ceiling (must be > minGoal)
 *
 * TODO (Seth): Verify field names match GoalsViewModel and GoalsScreen exactly.
 */
@Entity(
    tableName   = "monthly_goals",
    foreignKeys = [ForeignKey(
        entity        = User::class,
        parentColumns = ["id"],
        childColumns  = ["userId"],
        onDelete      = ForeignKey.CASCADE
    )],
    indices = [
        Index("userId"),
        Index(value = ["userId", "month", "year"], unique = true)
    ]
)
data class MonthlyGoal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId   : Long,
    val month    : Int,    // 1–12
    val year     : Int,    // e.g. 2026
    val minGoal  : Double,
    val maxGoal  : Double, // must be > minGoal — enforced in GoalsViewModel
    val updatedAt: Long = System.currentTimeMillis()
)

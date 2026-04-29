package com.budgetwise.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.budgetwise.app.data.local.entity.MonthlyGoal
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the `monthly_goals` table.
 *
 * The UNIQUE INDEX on (userId, month, year) combined with OnConflictStrategy.REPLACE
 * implements a true upsert: inserting a goal for a month that already has one
 * replaces the existing row atomically without a separate UPDATE statement.
 *
 * GoalRepository.upsert() pre-fetches the existing row's PK and sets it in the new
 * entity before calling upsertGoal(), preventing SQLite's auto-increment counter from
 * incrementing on every save.
 */
@Dao
interface MonthlyGoalDao {

    /**
     * Insert or replace a monthly goal.
     * Due to the UNIQUE INDEX on (userId, month, year), a REPLACE conflict strategy
     * will DELETE the existing row and INSERT the new one atomically.
     * The repository preserves the PK to prevent counter runaway.
     *
     * @return the rowId of the inserted/replaced row.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGoal(goal: MonthlyGoal): Long

    /**
     * Reactive stream of the goal for a specific user/month/year combination.
     * Emits null if no goal has been set yet for that month.
     * Consumed in GoalsViewModel.currentGoal to derive SpendingStatus.
     */
    @Query("""
        SELECT * FROM monthly_goals
        WHERE userId = :userId AND month = :month AND year = :year
        LIMIT 1
    """)
    fun getGoalForMonth(userId: Long, month: Int, year: Int): Flow<MonthlyGoal?>

    /**
     * One-shot synchronous fetch of the goal for a month.
     * Used in GoalRepository.upsert() to read the existing row's PK before replacing it,
     * ensuring the auto-increment counter doesn't increment needlessly on each save.
     */
    @Query("""
        SELECT * FROM monthly_goals
        WHERE userId = :userId AND month = :month AND year = :year
        LIMIT 1
    """)
    suspend fun getGoalForMonthSync(userId: Long, month: Int, year: Int): MonthlyGoal?
}

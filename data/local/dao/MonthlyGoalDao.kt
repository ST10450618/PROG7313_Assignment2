package com.budgetwise.app.data.local.dao

import androidx.room.*
import com.budgetwise.app.data.local.entity.MonthlyGoal
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthlyGoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGoal(goal: MonthlyGoal): Long

    @Query("SELECT * FROM monthly_goals WHERE userId = :userId AND month = :month AND year = :year LIMIT 1")
    fun getGoalForMonth(userId: Long, month: Int, year: Int): Flow<MonthlyGoal?>

    @Query("SELECT * FROM monthly_goals WHERE userId = :userId AND month = :month AND year = :year LIMIT 1")
    suspend fun getGoalForMonthSync(userId: Long, month: Int, year: Int): MonthlyGoal?
}
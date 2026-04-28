package com.budgetwise.app.data.repository

import android.util.Log
import com.budgetwise.app.data.local.dao.MonthlyGoalDao
import com.budgetwise.app.data.local.entity.MonthlyGoal
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "GoalRepository"

@Singleton
class GoalRepository @Inject constructor(private val dao: MonthlyGoalDao) {

    fun getForMonth(userId: Long, month: Int, year: Int): Flow<MonthlyGoal?> =
        dao.getGoalForMonth(userId, month, year)

    suspend fun upsert(userId: Long, month: Int, year: Int, min: Double, max: Double) {
        val existing = dao.getGoalForMonthSync(userId, month, year)
        dao.upsertGoal(
            MonthlyGoal(id = existing?.id ?: 0, userId = userId,
                month = month, year = year, minGoal = min, maxGoal = max)
        )
        Log.d(TAG, "Goal upserted for $month/$year — min=R$min, max=R$max")
    }
}
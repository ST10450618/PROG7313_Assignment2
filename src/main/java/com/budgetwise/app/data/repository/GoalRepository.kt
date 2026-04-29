package com.budgetwise.app.data.repository

import com.budgetwise.app.data.local.dao.MonthlyGoalDao
import com.budgetwise.app.data.local.entity.MonthlyGoal
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for monthly goal operations.
 *
 * Implements a true upsert by pre-fetching the existing row's PK before replacing it.
 * This prevents SQLite's auto-increment counter from incrementing on every save,
 * which would cause unnecessary counter growth over time.
 *
 * Injected as @Singleton by RepositoryModule.
 */
@Singleton
class GoalRepository @Inject constructor(
    private val monthlyGoalDao: MonthlyGoalDao
) {

    /**
     * Reactive stream of the goal for a specific user/month/year.
     * Emits null if no goal has been saved for that month yet.
     * Emits the updated goal immediately after upsert() completes.
     *
     * Consumed in GoalsViewModel.currentGoal via flatMapLatest.
     *
     * @param userId The logged-in user's id.
     * @param month  Calendar month (1–12).
     * @param year   4-digit calendar year.
     */
    fun getForMonth(userId: Long, month: Int, year: Int): Flow<MonthlyGoal?> =
        monthlyGoalDao.getGoalForMonth(userId, month, year)

    /**
     * Insert or update the goal for a specific month.
     *
     * Upsert logic:
     *   1. Fetch the existing row (if any) to recover the existing PK.
     *   2. Build the new MonthlyGoal with that same PK (or 0 for a new row).
     *   3. Use OnConflictStrategy.REPLACE — the UNIQUE INDEX on (userId, month, year)
     *      ensures only one row exists per month.
     *
     * Preserving the PK prevents the auto-increment counter from jumping with each save.
     *
     * @param userId  The logged-in user's id.
     * @param month   Calendar month (1–12).
     * @param year    4-digit year.
     * @param minGoal Minimum spending target (ZAR, ≥ 0).
     * @param maxGoal Maximum spending cap (ZAR, > minGoal).
     */
    suspend fun upsert(userId: Long, month: Int, year: Int, minGoal: Double, maxGoal: Double) {
        // Pre-fetch to recover existing PK (prevents auto-increment counter runaway)
        val existing = monthlyGoalDao.getGoalForMonthSync(userId, month, year)

        val goal = MonthlyGoal(
            id        = existing?.id ?: 0L,   // reuse existing PK if present
            userId    = userId,
            month     = month,
            year      = year,
            minGoal   = minGoal,
            maxGoal   = maxGoal,
            updatedAt = System.currentTimeMillis()
        )
        monthlyGoalDao.upsertGoal(goal)
    }
}

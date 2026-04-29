package com.budgetwise.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.budgetwise.app.data.local.entity.Expense
import kotlinx.coroutines.flow.Flow

/**
 * POJO used as the result of getCategoryTotalsForPeriod().
 *
 * NOT a Room @Entity — it's a plain data class used only for query result mapping.
 * Field names MUST exactly match the SQL column aliases:
 *   categoryId → "categoryId" column alias in the GROUP BY query
 *   total      → "SUM(amount) AS total" alias
 *
 * Room maps column aliases to POJO fields by exact name at compile time.
 * See Section 17, Bug 3 in the handover document for common pitfall.
 */
data class CategoryTotal(
    val categoryId: Long,
    val total: Double
)

/**
 * Data Access Object for the `expenses` table.
 *
 * All period-filtered queries use BETWEEN :startDate AND :endDate where the values
 * are epoch-ms produced by DateUtils.startOfDay() and DateUtils.endOfDay().
 * Because expense.date is stored as startOfDay(picked), this guarantees correct
 * matching regardless of what time of day the expense was entered.
 */
@Dao
interface ExpenseDao {

    /**
     * Insert or replace an expense.
     * REPLACE handles the case where a re-submitted form tries to re-insert an
     * existing row (unlikely with auto-increment, but safe to include).
     *
     * @return auto-generated expense id.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    /**
     * Delete an expense by its primary key.
     * Called from ExpenseViewModel.deleteExpense() via a swipe-to-delete or dialog.
     */
    @Query("DELETE FROM expenses WHERE id = :expenseId")
    suspend fun deleteExpense(expenseId: Long)

    /**
     * Look up a single expense by PK — used for detail navigation (future use).
     */
    @Query("SELECT * FROM expenses WHERE id = :expenseId LIMIT 1")
    suspend fun getExpenseById(expenseId: Long): Expense?

    /**
     * Reactive stream of expenses for a user within a date range.
     * Results are ordered newest-first (date DESC), then by start time within same day.
     *
     * Consumed in ExpenseListScreen to show the filterable expense list.
     * date values must be startOfDay(epoch) for the BETWEEN filter to work correctly.
     *
     * @param userId    the logged-in user's id
     * @param startDate startOfDay(userPickedStartMs) — epoch ms at local midnight
     * @param endDate   endOfDay(userPickedEndMs)   — epoch ms at 23:59:59.999
     */
    @Query("""
        SELECT * FROM expenses
        WHERE userId = :userId
          AND date BETWEEN :startDate AND :endDate
        ORDER BY date DESC, startTime DESC
    """)
    fun getExpensesForPeriod(userId: Long, startDate: Long, endDate: Long): Flow<List<Expense>>

    /**
     * Reactive stream of per-category spend totals for a period.
     * Expenses with a NULL categoryId (deleted category) are excluded via IS NOT NULL.
     *
     * Returns a list of CategoryTotal POJOs: {categoryId, SUM(amount)}.
     * Used in ReportViewModel to build the CategoryReportRow data for ReportScreen.
     */
    @Query("""
        SELECT categoryId, SUM(amount) AS total
        FROM expenses
        WHERE userId = :userId
          AND date BETWEEN :startDate AND :endDate
          AND categoryId IS NOT NULL
        GROUP BY categoryId
    """)
    fun getCategoryTotalsForPeriod(
        userId: Long,
        startDate: Long,
        endDate: Long
    ): Flow<List<CategoryTotal>>

    /**
     * Reactive stream of the total amount spent in a period.
     * COALESCE prevents a NULL emission when there are no matching expenses —
     * returns 0.0 instead, so GoalsViewModel never has to handle null totals.
     *
     * Used by HomeViewModel (current month) and GoalsViewModel (current month).
     */
    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM expenses
        WHERE userId = :userId
          AND date BETWEEN :startDate AND :endDate
    """)
    fun getTotalForPeriod(userId: Long, startDate: Long, endDate: Long): Flow<Double>
}

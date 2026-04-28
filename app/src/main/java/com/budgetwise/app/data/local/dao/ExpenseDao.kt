package com.budgetwise.app.data.local.dao

import androidx.room.*
import com.budgetwise.app.data.local.entity.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("SELECT * FROM expenses WHERE id = :id LIMIT 1")
    suspend fun getExpenseById(id: Long): Expense?

    /**
     * Core period-filter query — powers the Expense List screen.
     * Uses epoch ms boundaries for efficient indexed range scanning.
     */
    @Query("""
        SELECT * FROM expenses
        WHERE userId = :userId AND date BETWEEN :startDate AND :endDate
        ORDER BY date DESC, startTime DESC
    """)
    fun getExpensesForPeriod(userId: Long, startDate: Long, endDate: Long): Flow<List<Expense>>

    /**
     * Aggregated category totals — powers the Reports screen.
     * Returns a lightweight [CategoryTotal] projection rather than full Expense objects.
     */
    @Query("""
        SELECT categoryId, SUM(amount) AS total
        FROM expenses
        WHERE userId = :userId AND date BETWEEN :startDate AND :endDate AND categoryId IS NOT NULL
        GROUP BY categoryId
    """)
    fun getCategoryTotalsForPeriod(userId: Long, startDate: Long, endDate: Long): Flow<List<CategoryTotal>>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE userId = :userId AND date BETWEEN :startDate AND :endDate")
    fun getTotalForPeriod(userId: Long, startDate: Long, endDate: Long): Flow<Double>
}

/** Lightweight POJO for the GROUP BY aggregation query — not a Room Entity. */
data class CategoryTotal(val categoryId: Long, val total: Double)
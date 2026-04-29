package com.budgetwise.app.data.repository

import com.budgetwise.app.data.local.dao.CategoryTotal
import com.budgetwise.app.data.local.dao.ExpenseDao
import com.budgetwise.app.data.local.entity.Expense
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for expense CRUD and period-filtered query operations.
 *
 * All date parameters are epoch-ms values produced by DateUtils utility functions.
 * The repository does not perform date conversion — that is the ViewModel's responsibility.
 *
 * Injected as @Singleton by RepositoryModule.
 */
@Singleton
class ExpenseRepository @Inject constructor(
    private val expenseDao: ExpenseDao
) {

    /**
     * Reactive stream of expenses for a user within a date range.
     * Emits whenever an expense is added or deleted within the range.
     *
     * @param userId    The logged-in user's id.
     * @param startDate Epoch ms at the start of the first day (DateUtils.startOfDay).
     * @param endDate   Epoch ms at the end of the last day (DateUtils.endOfDay).
     */
    fun getForPeriod(userId: Long, startDate: Long, endDate: Long): Flow<List<Expense>> =
        expenseDao.getExpensesForPeriod(userId, startDate, endDate)

    /**
     * Reactive stream of per-category spend totals for a period.
     * Expenses with null categoryId (deleted category) are excluded.
     * Used in ReportViewModel to build the breakdown table.
     *
     * @param userId    The logged-in user's id.
     * @param startDate Epoch ms at the start of the period.
     * @param endDate   Epoch ms at the end of the period.
     */
    fun getCategoryTotals(userId: Long, startDate: Long, endDate: Long): Flow<List<CategoryTotal>> =
        expenseDao.getCategoryTotalsForPeriod(userId, startDate, endDate)

    /**
     * Reactive stream of the total amount spent in a period.
     * Returns 0.0 (via COALESCE) when there are no matching expenses.
     * Used in HomeViewModel (current month dashboard) and GoalsViewModel.
     *
     * @param userId    The logged-in user's id.
     * @param startDate Epoch ms at the start of the period.
     * @param endDate   Epoch ms at the end of the period.
     */
    fun getTotalForPeriod(userId: Long, startDate: Long, endDate: Long): Flow<Double> =
        expenseDao.getTotalForPeriod(userId, startDate, endDate)

    /**
     * Insert a new expense into the database.
     * All validation (amount > 0, description non-blank, valid times, category selected)
     * is performed in ExpenseViewModel.saveExpense() before this is called.
     *
     * @param expense The fully-populated Expense entity to insert.
     * @return The auto-generated expense id.
     */
    suspend fun add(expense: Expense): Long =
        expenseDao.insertExpense(expense)

    /**
     * Delete an expense by its primary key.
     * Called from ExpenseViewModel after user confirms deletion.
     *
     * @param expenseId The id of the expense to delete.
     */
    suspend fun delete(expenseId: Long) {
        expenseDao.deleteExpense(expenseId)
    }
}

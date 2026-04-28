package com.budgetwise.app.data.repository

import android.util.Log
import com.budgetwise.app.data.local.dao.CategoryTotal
import com.budgetwise.app.data.local.dao.ExpenseDao
import com.budgetwise.app.data.local.entity.Expense
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ExpenseRepository"

@Singleton
class ExpenseRepository @Inject constructor(private val dao: ExpenseDao) {

    fun getForPeriod(userId: Long, start: Long, end: Long): Flow<List<Expense>> =
        dao.getExpensesForPeriod(userId, start, end)

    fun getCategoryTotals(userId: Long, start: Long, end: Long): Flow<List<CategoryTotal>> =
        dao.getCategoryTotalsForPeriod(userId, start, end)

    fun getTotalForPeriod(userId: Long, start: Long, end: Long): Flow<Double> =
        dao.getTotalForPeriod(userId, start, end)

    suspend fun add(expense: Expense): Long {
        val id = dao.insertExpense(expense)
        Log.d(TAG, "Added expense '${expense.description}' R${expense.amount} (id=$id)")
        return id
    }

    suspend fun delete(expense: Expense) {
        Log.d(TAG, "Deleting expense id=${expense.id}")
        dao.deleteExpense(expense)
    }
}
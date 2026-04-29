package com.budgetwise.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.budgetwise.app.data.local.dao.*
import com.budgetwise.app.data.local.entity.*

/**
 * Room database singleton for BudgetWise.
 *
 * Version strategy: increment [version] and provide a Migration object whenever
 * the schema changes. Setting exportSchema = false is acceptable for this prototype;
 * enable it (and configure schemaLocation in build.gradle) before production release.
 */
@Database(
    entities    = [User::class, Category::class, Expense::class, MonthlyGoal::class],
    version     = 1,
    exportSchema = false
)
abstract class BudgetWiseDatabase : RoomDatabase() {
    abstract fun userDao()       : UserDao
    abstract fun categoryDao()   : CategoryDao
    abstract fun expenseDao()    : ExpenseDao
    abstract fun monthlyGoalDao(): MonthlyGoalDao

    companion object { const val NAME = "budgetwise.db" }
}
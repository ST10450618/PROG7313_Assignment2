package com.budgetwise.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.budgetwise.app.data.local.dao.CategoryDao
import com.budgetwise.app.data.local.dao.ExpenseDao
import com.budgetwise.app.data.local.dao.MonthlyGoalDao
import com.budgetwise.app.data.local.dao.UserDao
import com.budgetwise.app.data.local.entity.Category
import com.budgetwise.app.data.local.entity.Expense
import com.budgetwise.app.data.local.entity.MonthlyGoal
import com.budgetwise.app.data.local.entity.User

/**
 * The single Room database for BudgetWise.
 *
 * Contains 4 tables: users, categories, expenses, monthly_goals.
 *
 * Technical notes:
 * - version=1: For the Final POE, increment version and provide a Migration object.
 *   For development only, DatabaseModule uses fallbackToDestructiveMigration().
 * - exportSchema=false: Acceptable for prototype. For Final POE, set to true and add
 *   ksp { arg("room.schemaLocation", "$projectDir/schemas") } to app/build.gradle.kts.
 * - Provided as @Singleton via DatabaseModule (Hilt). Never instantiate directly.
 */
@Database(
    entities  = [User::class, Category::class, Expense::class, MonthlyGoal::class],
    version   = 2,
    exportSchema = false
)
abstract class BudgetWiseDatabase : RoomDatabase() {

    /** DAO for user account operations (register, login lookup). */
    abstract fun userDao(): UserDao

    /** DAO for category CRUD operations. */
    abstract fun categoryDao(): CategoryDao

    /** DAO for expense CRUD + period-filtered queries. */
    abstract fun expenseDao(): ExpenseDao

    /** DAO for monthly goal upsert + reactive queries. */
    abstract fun monthlyGoalDao(): MonthlyGoalDao

    companion object {
        /** The SQLite database file name on device storage. */
        const val NAME = "budgetwise.db"
    }
}

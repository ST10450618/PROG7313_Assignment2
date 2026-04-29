package com.budgetwise.app.di

import android.content.Context
import androidx.room.Room
import com.budgetwise.app.data.local.BudgetWiseDatabase
import com.budgetwise.app.data.local.dao.CategoryDao
import com.budgetwise.app.data.local.dao.ExpenseDao
import com.budgetwise.app.data.local.dao.MonthlyGoalDao
import com.budgetwise.app.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt DI module that provides the Room database and all four DAO instances.
 *
 * Installed in SingletonComponent so a single BudgetWiseDatabase instance is
 * shared across the entire application process.
 *
 * Note: fallbackToDestructiveMigration() is acceptable for the prototype phase.
 * For the Final POE, replace with a proper Migration(1, 2) object.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Provide the Room database as a singleton.
     * Uses APPLICATION context (never Activity context) to avoid memory leaks.
     */
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): BudgetWiseDatabase {
        return Room.databaseBuilder(
            context,
            BudgetWiseDatabase::class.java,
            BudgetWiseDatabase.NAME
        )
            .fallbackToDestructiveMigration() // Dev only — wipes DB on schema change
            .build()
    }

    /** Provide UserDao from the singleton database instance. */
    @Provides
    @Singleton
    fun provideUserDao(db: BudgetWiseDatabase): UserDao = db.userDao()

    /** Provide CategoryDao from the singleton database instance. */
    @Provides
    @Singleton
    fun provideCategoryDao(db: BudgetWiseDatabase): CategoryDao = db.categoryDao()

    /** Provide ExpenseDao from the singleton database instance. */
    @Provides
    @Singleton
    fun provideExpenseDao(db: BudgetWiseDatabase): ExpenseDao = db.expenseDao()

    /** Provide MonthlyGoalDao from the singleton database instance. */
    @Provides
    @Singleton
    fun provideMonthlyGoalDao(db: BudgetWiseDatabase): MonthlyGoalDao = db.monthlyGoalDao()
}

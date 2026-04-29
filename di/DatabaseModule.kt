package com.budgetwise.app.di

import android.content.Context
import androidx.room.Room
import com.budgetwise.app.data.local.BudgetWiseDatabase
import com.budgetwise.app.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing all data-layer dependencies.
 *
 * SingletonComponent scope ensures exactly one [BudgetWiseDatabase] instance
 * exists for the application lifetime — critical for Room to maintain a single
 * write-ahead log and prevent concurrent write conflicts.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): BudgetWiseDatabase =
        Room.databaseBuilder(ctx, BudgetWiseDatabase::class.java, BudgetWiseDatabase.NAME).build()

    @Provides fun provideUserDao(db: BudgetWiseDatabase)       : UserDao        = db.userDao()
    @Provides fun provideCategoryDao(db: BudgetWiseDatabase)   : CategoryDao    = db.categoryDao()
    @Provides fun provideExpenseDao(db: BudgetWiseDatabase)    : ExpenseDao     = db.expenseDao()
    @Provides fun provideGoalDao(db: BudgetWiseDatabase)       : MonthlyGoalDao = db.monthlyGoalDao()
}
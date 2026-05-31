package com.budgetwise.app.di

import android.content.Context
import androidx.room.Room
import com.budgetwise.app.data.local.BudgetWiseDatabase
import com.budgetwise.app.data.local.dao.CategoryDao
import com.budgetwise.app.data.local.dao.ExpenseDao
import com.budgetwise.app.data.local.dao.MonthlyGoalDao
import com.budgetwise.app.data.local.dao.UserDao
import com.budgetwise.app.data.repository.FirestoreRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing all data-layer dependencies.
 *
 * SingletonComponent scope ensures exactly one [BudgetWiseDatabase] and one
 * [FirestoreRepository] instance exist for the entire app lifetime.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): BudgetWiseDatabase =
        Room.databaseBuilder(ctx, BudgetWiseDatabase::class.java, BudgetWiseDatabase.NAME)
            .fallbackToDestructiveMigration()  // safe for prototype; add Migrations before production
            .build()

    @Provides fun provideUserDao(db: BudgetWiseDatabase):       UserDao        = db.userDao()
    @Provides fun provideCategoryDao(db: BudgetWiseDatabase):   CategoryDao    = db.categoryDao()
    @Provides fun provideExpenseDao(db: BudgetWiseDatabase):    ExpenseDao     = db.expenseDao()
    @Provides fun provideGoalDao(db: BudgetWiseDatabase):       MonthlyGoalDao = db.monthlyGoalDao()

    /**
     * Provides the Firestore sync repository as a singleton.
     * FirestoreRepository wraps Firebase SDK calls and is safe to share
     * across the application because FirebaseFirestore.getInstance() is itself
     * a process-level singleton.
     */
    @Provides
    @Singleton
    fun provideFirestoreRepository(): FirestoreRepository = FirestoreRepository()
}
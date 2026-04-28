package com.budgetwise.app.di

import com.budgetwise.app.data.local.dao.CategoryDao
import com.budgetwise.app.data.local.dao.ExpenseDao
import com.budgetwise.app.data.local.dao.MonthlyGoalDao
import com.budgetwise.app.data.local.dao.UserDao
import com.budgetwise.app.data.repository.CategoryRepository
import com.budgetwise.app.data.repository.ExpenseRepository
import com.budgetwise.app.data.repository.GoalRepository
import com.budgetwise.app.data.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing all repository singletons.
 *
 * Repositories are scoped to SingletonComponent so that every ViewModel
 * across the application receives the same repository instance and therefore
 * the same underlying Room Flow — preventing duplicate database subscriptions
 * or stale data caused by multiple in-flight queries on different instances.
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideUserRepository(dao: UserDao): UserRepository = UserRepository(dao)

    @Provides
    @Singleton
    fun provideCategoryRepository(dao: CategoryDao): CategoryRepository =
        CategoryRepository(dao)

    @Provides
    @Singleton
    fun provideExpenseRepository(dao: ExpenseDao): ExpenseRepository =
        ExpenseRepository(dao)

    @Provides
    @Singleton
    fun provideGoalRepository(dao: MonthlyGoalDao): GoalRepository = GoalRepository(dao)
}

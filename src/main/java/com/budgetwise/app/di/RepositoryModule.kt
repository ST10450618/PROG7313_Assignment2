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
 * Hilt DI module that provides all four Repository singletons.
 *
 * Each repository receives its required DAO(s) via constructor injection.
 * The DAOs themselves are provided by DatabaseModule.
 *
 * Installed in SingletonComponent — one repository instance per process ensures
 * all ViewModels share the same cache and avoid duplicate Room subscriptions.
 *
 * Note: Because repositories use @Singleton + @Inject annotations directly,
 * Hilt can resolve them automatically. This explicit @Module is provided for
 * clarity and to centralise the DI graph documentation in one place.
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideUserRepository(userDao: UserDao): UserRepository =
        UserRepository(userDao)

    @Provides
    @Singleton
    fun provideCategoryRepository(categoryDao: CategoryDao): CategoryRepository =
        CategoryRepository(categoryDao)

    @Provides
    @Singleton
    fun provideExpenseRepository(expenseDao: ExpenseDao): ExpenseRepository =
        ExpenseRepository(expenseDao)

    @Provides
    @Singleton
    fun provideGoalRepository(monthlyGoalDao: MonthlyGoalDao): GoalRepository =
        GoalRepository(monthlyGoalDao)
}

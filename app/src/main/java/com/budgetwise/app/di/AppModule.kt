package com.budgetwise.app.di

import android.content.Context
import com.budgetwise.app.utils.SessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing application-scoped utilities.
 *
 * [SessionManager] is a singleton because the DataStore instance it wraps must
 * never be duplicated — multiple instances targeting the same file cause
 * IllegalStateException at runtime.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSessionManager(@ApplicationContext context: Context): SessionManager =
        SessionManager(context)
}

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
 * AppModule provides application-level singleton dependencies that are not
 * related to the database layer (those live in [DatabaseModule]).
 *
 * SessionManager is scoped to SingletonComponent because it wraps a DataStore
 * instance — DataStore must be a singleton per process to avoid concurrent
 * write conflicts and IOException on initialisation.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSessionManager(@ApplicationContext ctx: Context): SessionManager =
        SessionManager(ctx)
}
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
 * Hilt DI module for application-level singletons that are not database-related.
 *
 * Currently provides SessionManager (DataStore-backed session state).
 * Installed in SingletonComponent — one instance per process.
 *
 * SessionManager is also annotated with @Singleton + @Inject, but providing it here
 * through a @Module ensures it is always resolved from the singleton scope and never
 * instantiated directly (which would cause the DataStore double-init exception).
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provide SessionManager as a singleton.
     * Uses APPLICATION context to avoid leaking Activity references.
     * The DataStore delegate in SessionManager.kt is at file level to guarantee
     * a single DataStore instance even if SessionManager were recreated (it won't be).
     */
    @Provides
    @Singleton
    fun provideSessionManager(
        @ApplicationContext context: Context
    ): SessionManager = SessionManager(context)
}

package com.budgetwise.app.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FILE-LEVEL DataStore delegate — must be at file level (not inside the class).
 *
 * This guarantees a SINGLE DataStore instance per process.
 * Creating multiple instances for the same file throws:
 *   "IllegalStateException: DataStore is already being initialised"
 * The DataStore documentation explicitly requires this pattern.
 *
 * Named "bw_session" → stored at: /data/data/com.budgetwise.app/files/datastore/bw_session.preferences_pb
 */
private val Context.dataStore: DataStore<Preferences>
    by preferencesDataStore(name = "bw_session")

/**
 * Session manager backed by Jetpack DataStore<Preferences>.
 *
 * Replaces SharedPreferences because:
 *   - SharedPreferences.getXxx() blocks the calling thread on first read (StrictMode violation).
 *   - DataStore is fully coroutine-safe and Flow-based.
 *
 * Session lifecycle:
 *   - Saved in AuthViewModel.login() / register() on success.
 *   - Read in MainActivity.onCreate() via runBlocking to determine the start destination.
 *   - Cleared in HomeViewModel.logout() when the user explicitly logs out.
 *   - Read in all ViewModels via flatMapLatest to scope Room queries to the current user.
 *
 * Provided as @Singleton by AppModule — never instantiate directly.
 */
@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        /** DataStore key for the logged-in user's id (Long). */
        private val KEY_USER_ID  = longPreferencesKey("user_id")

        /** DataStore key for the logged-in user's username (String). */
        private val KEY_USERNAME = stringPreferencesKey("username")

        /**
         * Sentinel value meaning "no user is logged in".
         * All ViewModels check for this and emit empty state instead of querying Room.
         */
        const val NO_USER = -1L
    }

    /**
     * Reactive stream of the current user's id.
     * Emits NO_USER (-1L) when no session is active (logged out or first launch).
     */
    val userId: Flow<Long> = context.dataStore.data
        .map { prefs -> prefs[KEY_USER_ID] ?: NO_USER }

    /**
     * Reactive stream of the current user's username.
     * Emits empty string when no session is active.
     */
    val username: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[KEY_USERNAME] ?: "" }

    /**
     * Persist a new session after successful login or registration.
     * Replaces any previously stored session data atomically.
     *
     * @param userId   The authenticated user's primary key.
     * @param username The authenticated user's display name.
     */
    suspend fun save(userId: Long, username: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_ID]  = userId
            prefs[KEY_USERNAME] = username
        }
    }

    /**
     * Clear the session (logout).
     * After this call, userId emits NO_USER and all ViewModel flows emit empty state.
     */
    suspend fun clear() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}

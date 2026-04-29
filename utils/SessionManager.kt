package com.budgetwise.app.utils

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("bw_session")
private const val TAG = "SessionManager"

/**
 * Manages the logged-in user session via Jetpack DataStore.
 *
 * DataStore is preferred over SharedPreferences because it is:
 * - coroutine-safe (no StrictMode violations)
 * - type-safe via typed keys
 * - non-blocking on the main thread
 *
 * The session persists across app restarts until the user explicitly logs out.
 */
@Singleton
class SessionManager @Inject constructor(@ApplicationContext private val ctx: Context) {

    companion object {
        val KEY_USER_ID  = longPreferencesKey("user_id")
        val KEY_USERNAME = stringPreferencesKey("username")
        const val NO_USER = -1L
    }

    val userId  : Flow<Long>   = ctx.dataStore.data.map { it[KEY_USER_ID]  ?: NO_USER }
    val username: Flow<String> = ctx.dataStore.data.map { it[KEY_USERNAME] ?: "" }

    suspend fun save(userId: Long, username: String) {
        ctx.dataStore.edit { it[KEY_USER_ID] = userId; it[KEY_USERNAME] = username }
        Log.d(TAG, "Session saved — user='$username' id=$userId")
    }

    suspend fun clear() {
        ctx.dataStore.edit { it.clear() }
        Log.d(TAG, "Session cleared")
    }
}
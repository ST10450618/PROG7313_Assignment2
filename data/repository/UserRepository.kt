package com.budgetwise.app.data.repository

import android.util.Log
import com.budgetwise.app.data.local.dao.UserDao
import com.budgetwise.app.data.local.entity.User
import com.budgetwise.app.utils.HashUtils
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "UserRepository"

@Singleton
class UserRepository @Inject constructor(private val dao: UserDao) {

    /**
     * Registers a new user. Returns the new user's DB row ID (> 0) on success,
     * or -1L if the username is already taken.
     *
     * The password is hashed with SHA-256 before being passed to the DAO —
     * plaintext passwords never touch the database layer.
     */
    suspend fun register(username: String, password: String): Long {
        return try {
            if (dao.usernameExists(username) > 0) {
                Log.w(TAG, "Registration rejected — username '$username' already exists")
                return -1L
            }
            val id = dao.insertUser(User(username = username, passwordHash = HashUtils.sha256(password)))
            Log.d(TAG, "Registered user '$username' with ID $id")
            id
        } catch (e: Exception) {
            Log.e(TAG, "Registration error: ${e.message}", e)
            -1L
        }
    }

    /** Validates credentials. Returns the [User] on success, null on failure. */
    suspend fun login(username: String, password: String): User? {
        val user = dao.getUserByUsername(username)
        return when {
            user == null -> { Log.w(TAG, "Login failed — user '$username' not found"); null }
            user.passwordHash != HashUtils.sha256(password) -> { Log.w(TAG, "Login failed — wrong password"); null }
            else -> { Log.d(TAG, "User '$username' authenticated"); user }
        }
    }

    suspend fun getById(id: Long): User? = dao.getUserById(id)
}
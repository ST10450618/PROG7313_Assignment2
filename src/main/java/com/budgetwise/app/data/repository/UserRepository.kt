package com.budgetwise.app.data.repository

import com.budgetwise.app.data.local.dao.UserDao
import com.budgetwise.app.data.local.entity.User
import com.budgetwise.app.utils.HashUtils
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for user account operations: registration and login.
 *
 * This is the ONLY place in the codebase where plain-text passwords are hashed.
 * The DAO never sees a raw password. All reads/writes go via SHA-256 hex strings.
 *
 * Injected as @Singleton by RepositoryModule so one instance exists per process.
 */
@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao
) {

    /**
     * Register a new user account.
     *
     * Validation performed here (defensive layer, separate from ViewModel validation):
     *   1. Username must not be blank.
     *   2. Username must not already exist (checked via usernameExists before insert).
     *   3. Password is hashed with SHA-256 before storage.
     *
     * @param username The desired username.
     * @param password The plain-text password (will be hashed immediately, never stored).
     * @return The new user's id on success, or -1L if the username is already taken.
     * @throws Exception if the Room INSERT fails for any unexpected reason.
     */
    suspend fun register(username: String, password: String): Long {
        // Pre-check: avoid the ABORT conflict exception from Room for a cleaner error path
        if (userDao.usernameExists(username) > 0) {
            return -1L  // Caller (AuthViewModel) treats -1 as "username taken"
        }

        val passwordHash = HashUtils.sha256(password)
        val newUser = User(
            username     = username,
            passwordHash = passwordHash,
            createdAt    = System.currentTimeMillis()
        )
        return userDao.insertUser(newUser)
    }

    /**
     * Attempt to log in with the given credentials.
     *
     * Process:
     *   1. Look up the user by username (returns null if not found).
     *   2. Hash the supplied password and compare to the stored hash.
     *   3. Return the User entity on match, null on any mismatch.
     *
     * @param username The entered username.
     * @param password The plain-text password to verify.
     * @return The matching User entity, or null if credentials are incorrect.
     */
    suspend fun login(username: String, password: String): User? {
        val user = userDao.getUserByUsername(username) ?: return null
        val inputHash = HashUtils.sha256(password)
        return if (inputHash == user.passwordHash) user else null
    }

    /**
     * Retrieve a user by their primary key.
     * Used by HomeViewModel after a session restore to refresh the display name.
     *
     * @param userId The user's primary key.
     * @return The User entity, or null if not found.
     */
    suspend fun getById(userId: Long): User? {
        return userDao.getUserById(userId)
    }
}

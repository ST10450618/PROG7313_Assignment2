package com.budgetwise.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.budgetwise.app.data.local.entity.User

/**
 * Data Access Object for the `users` table.
 *
 * All operations that touch passwords are handled at the Repository layer
 * (UserRepository), never here — the DAO only ever sees hashed values.
 */
@Dao
interface UserDao {

    /**
     * Insert a new user row.
     * OnConflictStrategy.ABORT causes the insert to fail (throw SQLiteConstraintException)
     * if the username already exists (UNIQUE index). The repository catches this and
     * returns an appropriate error instead of crashing.
     *
     * @return the auto-generated rowId (same as id) on success.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: User): Long

    /**
     * Look up a user by username for login purposes.
     * Returns null if no matching row exists (wrong username).
     */
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    /**
     * Look up a user by their primary key.
     * Used by HomeViewModel to refresh the display name after session restore.
     */
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Long): User?

    /**
     * Check whether a username is already taken.
     * Returns 1 if the username exists, 0 otherwise.
     * Used as a pre-check in UserRepository.register() before attempting the insert.
     */
    @Query("SELECT COUNT(*) FROM users WHERE username = :username")
    suspend fun usernameExists(username: String): Int
}

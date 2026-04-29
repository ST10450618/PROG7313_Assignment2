package com.budgetwise.app.data.local.dao

import androidx.room.*
import com.budgetwise.app.data.local.entity.User

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: User): Long

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): User?

    @Query("SELECT COUNT(*) FROM users WHERE username = :username")
    suspend fun usernameExists(username: String): Int
}
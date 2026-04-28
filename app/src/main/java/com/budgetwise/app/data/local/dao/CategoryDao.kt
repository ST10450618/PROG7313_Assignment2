package com.budgetwise.app.data.local.dao

import androidx.room.*
import com.budgetwise.app.data.local.entity.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long

    @Delete
    suspend fun deleteCategory(category: Category)

    // Flow<> enables automatic UI recomposition when category list changes
    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY name ASC")
    fun getCategoriesForUser(userId: Long): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY name ASC")
    suspend fun getCategoriesSync(userId: Long): List<Category>
}
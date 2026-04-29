package com.budgetwise.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.budgetwise.app.data.local.entity.Category
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the `categories` table.
 *
 * Reactive queries return Flow<T> so the UI automatically re-renders when
 * a category is added or deleted without requiring a manual refresh call.
 */
@Dao
interface CategoryDao {

    /**
     * Insert or replace a category.
     * REPLACE strategy is safe here because categories are user-created with auto-generated PKs.
     * An accidental REPLACE would only overwrite the same logical row.
     *
     * @return auto-generated category id.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long

    /**
     * Delete a category by its primary key.
     * Room's FK ON DELETE SET NULL propagates to expenses.categoryId automatically.
     */
    @Query("DELETE FROM categories WHERE id = :categoryId")
    suspend fun deleteCategory(categoryId: Long)

    /**
     * Reactive stream of all categories owned by the given user, ordered by name.
     * Emits a new list every time any category for this user is inserted or deleted.
     * Collected in CategoryViewModel via flatMapLatest to handle session changes.
     */
    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY name ASC")
    fun getCategoriesForUser(userId: Long): Flow<List<Category>>

    /**
     * One-shot synchronous fetch of all categories for a user.
     * Used in ExpenseViewModel to populate the category dropdown on demand.
     */
    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY name ASC")
    suspend fun getCategoriesSync(userId: Long): List<Category>
}

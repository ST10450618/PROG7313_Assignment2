package com.budgetwise.app.data.repository

import com.budgetwise.app.data.local.dao.CategoryDao
import com.budgetwise.app.data.local.entity.Category
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for category CRUD operations.
 *
 * Provides reactive flows for the UI layer (CategoryScreen, AddExpenseScreen dropdown)
 * and suspend functions for mutations.
 *
 * Injected as @Singleton by RepositoryModule.
 */
@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
) {

    /**
     * Reactive stream of all categories for a user, ordered alphabetically.
     * Emits a new list whenever the user adds or deletes a category.
     * Consumed in CategoryViewModel and ExpenseViewModel via flatMapLatest.
     *
     * @param userId The logged-in user's id.
     * @return A Flow emitting the current list of categories on every change.
     */
    fun getForUser(userId: Long): Flow<List<Category>> =
        categoryDao.getCategoriesForUser(userId)

    /**
     * One-shot synchronous fetch of categories.
     * Used in ExpenseViewModel to pre-load categories once for the dropdown.
     *
     * @param userId The logged-in user's id.
     * @return The current list of categories (snapshot, not reactive).
     */
    suspend fun getForUserSync(userId: Long): List<Category> =
        categoryDao.getCategoriesSync(userId)

    /**
     * Add a new category for the given user.
     * Name validation (non-blank) is enforced in CategoryViewModel before this is called.
     *
     * @param userId   Owner's user id.
     * @param name     Display name for the category.
     * @param colorHex Hex colour string with leading # (e.g. "#E16162").
     * @return The auto-generated category id.
     */
    suspend fun add(userId: Long, name: String, colorHex: String): Long {
        val category = Category(
            userId    = userId,
            name      = name.trim(),
            colorHex  = colorHex,
            createdAt = System.currentTimeMillis()
        )
        return categoryDao.insertCategory(category)
    }

    /**
     * Delete a category by its primary key.
     * Room's FK ON DELETE SET NULL ensures expenses linked to this category
     * remain in the database with categoryId = null (uncategorised).
     *
     * @param categoryId The id of the category to delete.
     */
    suspend fun delete(categoryId: Long) {
        categoryDao.deleteCategory(categoryId)
    }
}

package com.budgetwise.app.data.repository

import android.util.Log
import com.budgetwise.app.data.local.dao.CategoryDao
import com.budgetwise.app.data.local.entity.Category
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "CategoryRepository"

@Singleton
class CategoryRepository @Inject constructor(private val dao: CategoryDao) {

    fun getForUser(userId: Long): Flow<List<Category>> = dao.getCategoriesForUser(userId)

    suspend fun getForUserSync(userId: Long): List<Category> = dao.getCategoriesSync(userId)

    suspend fun add(userId: Long, name: String, colorHex: String): Long {
        val id = dao.insertCategory(Category(userId = userId, name = name, colorHex = colorHex))
        Log.d(TAG, "Added category '$name' (id=$id) for user $userId")
        return id
    }

    suspend fun delete(category: Category) {
        Log.d(TAG, "Deleting category '${category.name}'")
        dao.deleteCategory(category)
    }
}
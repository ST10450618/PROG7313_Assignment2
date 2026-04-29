package com.budgetwise.app.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetwise.app.data.local.entity.Category
import com.budgetwise.app.data.repository.CategoryRepository
import com.budgetwise.app.utils.SessionManager
import com.budgetwise.app.utils.SessionManager.Companion.NO_USER
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for CategoryScreen.
 *
 * Provides:
 * - [categories] — reactive list of user's categories (updates live on add/delete)
 * - [successMsg] / [errorMsg] — one-shot feedback messages for Snackbar
 * - [addCategory()] — validates and inserts a new category
 * - [deleteCategory()] — removes a category (expenses become uncategorised)
 * - [clearMessages()] — reset feedback messages after Snackbar is shown
 */
@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val sessionManager:     SessionManager
) : ViewModel() {

    private val _successMsg = MutableStateFlow<String?>(null)
    val successMsg: StateFlow<String?> = _successMsg.asStateFlow()

    private val _errorMsg = MutableStateFlow<String?>(null)
    val errorMsg: StateFlow<String?> = _errorMsg.asStateFlow()

    /**
     * Reactive list of categories for the current user.
     *
     * flatMapLatest: when userId changes (login/logout), the previous subscription
     * is cancelled and a new one starts. Prevents data from one user appearing for another.
     *
     * Emits empty list when NO_USER (logged out).
     */
    val categories: StateFlow<List<Category>> = sessionManager.userId
        .flatMapLatest { userId ->
            if (userId == NO_USER) flowOf(emptyList())
            else categoryRepository.getForUser(userId)
        }
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    /**
     * Add a new category for the current user.
     *
     * Validation:
     *   1. Name must not be blank.
     *
     * @param name     The category display name.
     * @param colorHex The selected hex colour string (e.g. "#E16162").
     */
    fun addCategory(name: String, colorHex: String) {
        if (name.isBlank()) {
            _errorMsg.value = "Category name cannot be empty"
            return
        }

        viewModelScope.launch {
            val userId = sessionManager.userId.first()
            if (userId == NO_USER) {
                _errorMsg.value = "You must be logged in to add categories"
                return@launch
            }
            categoryRepository.add(userId, name.trim(), colorHex)
            _successMsg.value = "Category '${name.trim()}' added"
        }
    }

    /**
     * Delete a category by its primary key.
     * Linked expenses remain but become uncategorised (categoryId set to null by Room FK).
     *
     * @param category The Category entity to delete.
     */
    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            categoryRepository.delete(category.id)
            _successMsg.value = "'${category.name}' deleted"
        }
    }

    /** Reset both feedback messages after Snackbar/toast has been shown. */
    fun clearMessages() {
        _successMsg.value = null
        _errorMsg.value   = null
    }
}

package com.budgetwise.app.ui.category

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetwise.app.data.local.entity.Category
import com.budgetwise.app.data.repository.CategoryRepository
import com.budgetwise.app.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "CategoryViewModel"

data class CategoryUiState(val error: String? = null, val success: String? = null)

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val repo   : CategoryRepository,
    private val session: SessionManager
) : ViewModel() {

    private val _ui = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _ui.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val categories: StateFlow<List<Category>> = session.userId
        .flatMapLatest { uid -> if (uid == SessionManager.NO_USER) flowOf(emptyList()) else repo.getForUser(uid) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun add(name: String, colorHex: String) {
        if (name.isBlank()) { _ui.value = CategoryUiState(error = "Category name cannot be empty"); return }
        viewModelScope.launch {
            val uid = session.userId.first()
            val id  = repo.add(uid, name.trim(), colorHex)
            if (id > 0) { Log.d(TAG, "Category added"); _ui.value = CategoryUiState(success = "Category '${name.trim()}' created!") }
            else { _ui.value = CategoryUiState(error = "Failed to create category") }
        }
    }

    fun delete(cat: Category) = viewModelScope.launch {
        repo.delete(cat)
        _ui.value = CategoryUiState(success = "'${cat.name}' deleted")
    }

    fun clearMessages() { _ui.value = CategoryUiState() }
}
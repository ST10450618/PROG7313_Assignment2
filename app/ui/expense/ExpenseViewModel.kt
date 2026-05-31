package com.budgetwise.app.ui.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetwise.app.data.local.entity.Category
import com.budgetwise.app.data.local.entity.Expense
import com.budgetwise.app.data.repository.CategoryRepository
import com.budgetwise.app.data.repository.ExpenseRepository
import com.budgetwise.app.data.repository.FirestoreRepository
import com.budgetwise.app.utils.DateUtils
import com.budgetwise.app.utils.SessionManager
import com.budgetwise.app.utils.SessionManager.Companion.NO_USER
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/** UI state for the Add Expense form. */
data class ExpenseUiState(
    val isLoading: Boolean = false,
    val errorMsg:  String? = null,
    val isSaved:   Boolean = false
)

/**
 * Filter state for the ExpenseListScreen date range picker.
 * Defaults to the current calendar month.
 */
data class FilterState(
    val startMs: Long = DateUtils.startOfMonth(DateUtils.currentMonth(), DateUtils.currentYear()),
    val endMs:   Long = DateUtils.endOfMonth(DateUtils.currentMonth(), DateUtils.currentYear())
)

/**
 * Shared ViewModel for AddExpenseScreen and ExpenseListScreen.
 *
 * Updated for Final PoE:
 * - Calls FirestoreRepository.saveExpense() after every successful Room insert
 *   (Online database requirement — data stored in Firestore for multi-device access).
 * - Calls FirestoreRepository.deleteExpense() after Room delete.
 */
@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val expenseRepository:  ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val sessionManager:     SessionManager,
    private val firestoreRepo:      FirestoreRepository   // Final PoE: Firestore sync
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseUiState())
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    private val userIdFlow = sessionManager.userId

    /** Reactive list of user's categories (for the dropdown in AddExpenseScreen). */
    val categories: StateFlow<List<Category>> = userIdFlow
        .flatMapLatest { userId ->
            if (userId == NO_USER) flowOf(emptyList())
            else categoryRepository.getForUser(userId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Reactive expense list filtered by [filterState]. */
    val filteredExpenses = combine(userIdFlow, _filterState) { userId, filter ->
        Pair(userId, filter)
    }.flatMapLatest { (userId, filter) ->
        if (userId == NO_USER) flowOf(emptyList())
        else expenseRepository.getForPeriod(userId, filter.startMs, filter.endMs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Reactive total for the current filter period. */
    val periodTotal = combine(userIdFlow, _filterState) { userId, filter ->
        Pair(userId, filter)
    }.flatMapLatest { (userId, filter) ->
        if (userId == NO_USER) flowOf(0.0)
        else expenseRepository.getTotalForPeriod(userId, filter.startMs, filter.endMs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    // -------------------------------------------------------------------------
    // Save Expense — 6 ordered validations + Firestore sync
    // -------------------------------------------------------------------------

    /**
     * Validate and insert a new expense into Room, then sync to Firestore.
     *
     * Validations (in order):
     *   1. Amount must be a valid decimal number.
     *   2. Amount must be > 0.
     *   3. Description must not be blank.
     *   4. Start time must not be blank.
     *   5. End time must be strictly AFTER start time.
     *   6. Category must be selected (non-null).
     *
     * On success: inserts to Room → syncs to Firestore → sets isSaved=true.
     */
    fun saveExpense(
        amountStr:   String,
        description: String,
        dateMs:      Long,
        startTime:   String,
        endTime:     String,
        categoryId:  Long?,
        photoUri:    String?
    ) {
        val amount = amountStr.toDoubleOrNull()
        if (amount == null) {
            _uiState.update { it.copy(errorMsg = "Please enter a valid amount (e.g. 125.00)") }
            return
        }
        if (amount <= 0.0) {
            _uiState.update { it.copy(errorMsg = "Amount must be a valid amount greater than 0") }
            return
        }
        if (description.isBlank()) {
            _uiState.update { it.copy(errorMsg = "Description is required") }
            return
        }
        if (startTime.isBlank()) {
            _uiState.update { it.copy(errorMsg = "Please select a start time") }
            return
        }
        if (endTime.isBlank() || !isEndAfterStart(startTime, endTime)) {
            _uiState.update { it.copy(errorMsg = "End time must be after start time") }
            return
        }
        if (categoryId == null) {
            _uiState.update { it.copy(errorMsg = "Please select a category") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMsg = null) }

        viewModelScope.launch {
            val userId = sessionManager.userId.first()
            if (userId == NO_USER) {
                _uiState.update { it.copy(isLoading = false, errorMsg = "Session expired. Please log in again.") }
                return@launch
            }

            val expense = Expense(
                userId      = userId,
                categoryId  = categoryId,
                amount      = amount,
                description = description.trim(),
                date        = DateUtils.startOfDay(dateMs),
                startTime   = startTime,
                endTime     = endTime,
                photoUri    = photoUri,
                createdAt   = System.currentTimeMillis()
            )

            // 1. Insert into local Room database (source of truth)
            val newId = expenseRepository.add(expense)

            // 2. Sync to Firestore (online backup — Final PoE requirement)
            // Use the auto-generated id from Room for the Firestore document key
            firestoreRepo.saveExpense(userId, expense.copy(id = newId))

            _uiState.update { it.copy(isLoading = false, isSaved = true) }
        }
    }

    /** Delete an expense from Room and remove from Firestore. */
    fun deleteExpense(expenseId: Long) {
        viewModelScope.launch {
            val userId = sessionManager.userId.first()
            expenseRepository.delete(expenseId)
            // Sync deletion to Firestore
            if (userId != NO_USER) {
                firestoreRepo.deleteExpense(userId, expenseId)
            }
        }
    }

    /** Update the date range filter for the expense list. */
    fun updateFilter(startMs: Long, endMs: Long) {
        _filterState.update { it.copy(startMs = startMs, endMs = endMs) }
    }

    /** Reset the form state (call after successfully saving or when navigating back). */
    fun clearMessages() {
        _uiState.update { it.copy(errorMsg = null, isSaved = false) }
    }

    private fun isEndAfterStart(startTime: String, endTime: String): Boolean {
        return try {
            val (startH, startM) = startTime.split(":").map { it.toInt() }
            val (endH,   endM)   = endTime.split(":").map { it.toInt() }
            (endH * 60 + endM) > (startH * 60 + startM)
        } catch (e: Exception) { false }
    }
}
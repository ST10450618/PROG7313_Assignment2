package com.budgetwise.app.ui.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetwise.app.data.local.entity.Category
import com.budgetwise.app.data.local.entity.Expense
import com.budgetwise.app.data.repository.CategoryRepository
import com.budgetwise.app.data.repository.ExpenseRepository
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
 * Navigation Compose scopes this ViewModel to the back-stack entry, so both
 * screens share the same instance. Benefits: categories loaded once, filter
 * state persists when navigating Add→List→Back.
 *
 * Provides:
 * - [uiState] — form state (loading, error, saved)
 * - [filterState] — date range for the expense list
 * - [categories] — live list of user's categories (for AddExpense dropdown)
 * - [filteredExpenses] — live expense list within the current filter range
 * - [periodTotal] — live total for the current filter range
 * - [saveExpense()] — 6 ordered validations then inserts
 * - [deleteExpense()] — removes an expense by id
 * - [updateFilter()] — changes the date range
 */
@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val expenseRepository:  ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val sessionManager:     SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseUiState())
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    // Cache userId as a flow to avoid multiple DataStore reads
    private val userIdFlow = sessionManager.userId

    /**
     * Reactive list of user's categories (for the dropdown in AddExpenseScreen).
     * Switches to empty list when userId == NO_USER.
     */
    val categories: StateFlow<List<Category>> = userIdFlow
        .flatMapLatest { userId ->
            if (userId == NO_USER) flowOf(emptyList())
            else categoryRepository.getForUser(userId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * Reactive expense list filtered by [filterState].
     * Re-emits whenever filter changes or a new expense is added/deleted.
     */
    val filteredExpenses = combine(userIdFlow, _filterState) { userId, filter ->
        Pair(userId, filter)
    }.flatMapLatest { (userId, filter) ->
        if (userId == NO_USER) flowOf(emptyList())
        else expenseRepository.getForPeriod(userId, filter.startMs, filter.endMs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * Reactive total for the current filter period.
     * Used in ExpenseListScreen summary card.
     */
    val periodTotal = combine(userIdFlow, _filterState) { userId, filter ->
        Pair(userId, filter)
    }.flatMapLatest { (userId, filter) ->
        if (userId == NO_USER) flowOf(0.0)
        else expenseRepository.getTotalForPeriod(userId, filter.startMs, filter.endMs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    // -------------------------------------------------------------------------
    // Save Expense — 6 ordered validations
    // -------------------------------------------------------------------------

    /**
     * Validate and insert a new expense.
     *
     * Validations (in order — first failure sets error and returns):
     *   1. Amount must be a valid decimal number.
     *   2. Amount must be > 0.
     *   3. Description must not be blank.
     *   4. Start time must not be blank.
     *   5. End time must be strictly AFTER start time.
     *   6. Category must be selected (non-null).
     *
     * On success: inserts the expense and sets isSaved=true.
     *
     * @param amountStr   Raw string from the amount field (e.g. "125.50").
     * @param description Expense description.
     * @param dateMs      Epoch ms from the DatePickerDialog (will be normalised to startOfDay).
     * @param startTime   "HH:mm" string from BudgetWiseTimePickerDialog.
     * @param endTime     "HH:mm" string from BudgetWiseTimePickerDialog.
     * @param categoryId  Selected category's id, or null if nothing was chosen.
     * @param photoUri    Optional content:// URI string of the receipt photo.
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
        // Validation 1: valid decimal
        val amount = amountStr.toDoubleOrNull()
        if (amount == null) {
            _uiState.update { it.copy(errorMsg = "Please enter a valid amount (e.g. 125.00)") }
            return
        }
        // Validation 2: > 0
        if (amount <= 0.0) {
            _uiState.update { it.copy(errorMsg = "Amount must be a valid amount greater than 0") }
            return
        }
        // Validation 3: description
        if (description.isBlank()) {
            _uiState.update { it.copy(errorMsg = "Description is required") }
            return
        }
        // Validation 4: start time
        if (startTime.isBlank()) {
            _uiState.update { it.copy(errorMsg = "Please select a start time") }
            return
        }
        // Validation 5: end time after start time
        if (endTime.isBlank() || !isEndAfterStart(startTime, endTime)) {
            _uiState.update { it.copy(errorMsg = "End time must be after start time") }
            return
        }
        // Validation 6: category selected
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
                date        = DateUtils.startOfDay(dateMs),   // always normalise to midnight
                startTime   = startTime,
                endTime     = endTime,
                photoUri    = photoUri,
                createdAt   = System.currentTimeMillis()
            )
            expenseRepository.add(expense)
            _uiState.update { it.copy(isLoading = false, isSaved = true) }
        }
    }

    /** Delete an expense by its primary key. */
    fun deleteExpense(expenseId: Long) {
        viewModelScope.launch {
            expenseRepository.delete(expenseId)
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

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Returns true if [endTime] is strictly after [startTime].
     * Both times are "HH:mm" strings. Parses to total minutes for comparison.
     * Returns false for equal times (duration would be zero).
     */
    private fun isEndAfterStart(startTime: String, endTime: String): Boolean {
        return try {
            val (startH, startM) = startTime.split(":").map { it.toInt() }
            val (endH,   endM)   = endTime.split(":").map { it.toInt() }
            val startMinutes     = startH * 60 + startM
            val endMinutes       = endH   * 60 + endM
            endMinutes > startMinutes
        } catch (e: Exception) {
            false
        }
    }
}

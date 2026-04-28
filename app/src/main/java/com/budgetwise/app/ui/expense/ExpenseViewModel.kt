package com.budgetwise.app.ui.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetwise.app.data.local.entity.Category
import com.budgetwise.app.data.local.entity.Expense
import com.budgetwise.app.data.repository.CategoryRepository
import com.budgetwise.app.data.repository.ExpenseRepository
import com.budgetwise.app.utils.DateUtils
import com.budgetwise.app.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * TODO (James): Implement the full expense ViewModel logic.
 *
 * Required state:
 *  - FilterState(startMs, endMs) — defaults to current month
 *  - filteredExpenses: StateFlow<List<Expense>>
 *  - periodTotal: StateFlow<Double>
 *  - categories: StateFlow<List<Category>> — for the dropdown in AddExpenseScreen
 *  - uiMessage: StateFlow<String?> — success/error messages
 *
 * Required functions:
 *  - saveExpense(amount, description, dateMs, startTime, endTime, categoryId, photoUri?)
 *      → validates all fields, stores DateUtils.startOfDay(dateMs) as date
 *  - deleteExpense(expense: Expense)
 *  - updateFilter(startMs: Long, endMs: Long)
 *  - clearMessages()
 */
data class FilterState(
    val startMs: Long = DateUtils.startOfMonth(DateUtils.currentMonth(), DateUtils.currentYear()),
    val endMs  : Long = DateUtils.endOfMonth(DateUtils.currentMonth(), DateUtils.currentYear())
)

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val expenseRepo : ExpenseRepository,
    private val categoryRepo: CategoryRepository,
    private val session     : SessionManager
) : ViewModel() {

    private val _filter = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filter.asStateFlow()

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val categories: StateFlow<List<Category>> = session.userId
        .flatMapLatest { uid ->
            if (uid == SessionManager.NO_USER) flowOf(emptyList())
            else categoryRepo.getForUser(uid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredExpenses: StateFlow<List<Expense>> = session.userId
        .flatMapLatest { uid ->
            if (uid == SessionManager.NO_USER) flowOf(emptyList())
            else _filter.flatMapLatest { f ->
                expenseRepo.getForPeriod(uid, f.startMs, f.endMs)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val periodTotal: StateFlow<Double> = session.userId
        .flatMapLatest { uid ->
            if (uid == SessionManager.NO_USER) flowOf(0.0)
            else _filter.flatMapLatest { f ->
                expenseRepo.getTotalForPeriod(uid, f.startMs, f.endMs)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun updateFilter(startMs: Long, endMs: Long) {
        _filter.value = FilterState(
            startMs = DateUtils.startOfDay(startMs),
            endMs   = DateUtils.endOfDay(endMs)
        )
    }

    fun saveExpense(
        amountStr  : String,
        description: String,
        dateMs     : Long,
        startTime  : String,
        endTime    : String,
        categoryId : Long?,
        photoUri   : String? = null
    ) {
        // TODO (James): Add full validation matching ExpenseViewModelTest expectations:
        //  - blank description → "Description is required"
        //  - amount <= 0 or non-numeric → "Enter a valid amount"
        //  - endTime <= startTime → "End time must be after start time"
        //  - categoryId == null → "Please select a category"
        val amount = amountStr.toDoubleOrNull()
        if (description.isBlank()) { _uiMessage.value = "Description is required"; return }
        if (amount == null || amount <= 0.0) { _uiMessage.value = "Enter a valid amount greater than zero"; return }
        if (categoryId == null) { _uiMessage.value = "Please select a category"; return }
        val start = startTime.split(":").let { it[0].toInt() * 60 + it[1].toInt() }
        val end   = endTime.split(":").let { it[0].toInt() * 60 + it[1].toInt() }
        if (end <= start) { _uiMessage.value = "End time must be after start time"; return }

        viewModelScope.launch {
            val uid = session.userId.first()
            val expense = Expense(
                userId      = uid,
                categoryId  = categoryId,
                amount      = amount,
                description = description.trim(),
                date        = DateUtils.startOfDay(dateMs),
                startTime   = startTime,
                endTime     = endTime,
                photoUri    = photoUri
            )
            val id = expenseRepo.add(expense)
            if (id > 0) _uiMessage.value = "Expense saved!" else _uiMessage.value = "Failed to save expense"
        }
    }

    fun deleteExpense(expense: Expense) = viewModelScope.launch {
        expenseRepo.delete(expense)
        _uiMessage.value = "Expense deleted"
    }

    fun clearMessages() { _uiMessage.value = null }
}

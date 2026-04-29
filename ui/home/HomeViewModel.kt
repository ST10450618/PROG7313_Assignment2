package com.budgetwise.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetwise.app.data.repository.ExpenseRepository
import com.budgetwise.app.utils.DateUtils
import com.budgetwise.app.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val session     : SessionManager,
    private val expenseRepo : ExpenseRepository
) : ViewModel() {

    val username: StateFlow<String> =
        session.username.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    /** Live running total for the current calendar month — updates as expenses are added. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val monthTotal: StateFlow<Double> = session.userId.flatMapLatest { uid ->
        if (uid == SessionManager.NO_USER) flowOf(0.0)
        else expenseRepo.getTotalForPeriod(
            uid,
            DateUtils.startOfMonth(DateUtils.currentMonth(), DateUtils.currentYear()),
            DateUtils.endOfMonth(DateUtils.currentMonth(), DateUtils.currentYear())
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun logout() = viewModelScope.launch { session.clear() }
}
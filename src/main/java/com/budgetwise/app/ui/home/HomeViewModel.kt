package com.budgetwise.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetwise.app.data.repository.ExpenseRepository
import com.budgetwise.app.utils.DateUtils
import com.budgetwise.app.utils.SessionManager
import com.budgetwise.app.utils.SessionManager.Companion.NO_USER
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for HomeScreen.
 *
 * Provides:
 * - [username] — reactive display name from the session
 * - [monthTotal] — reactive running total for the current calendar month in ZAR
 * - [currentMonthLabel] — static "MMMM yyyy" string displayed in the welcome card
 * - [logout()] — clears session and triggers navigation to Login via HomeScreen
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sessionManager:    SessionManager,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    /** The current month/year label e.g. "April 2026" — computed once on ViewModel creation. */
    val currentMonthLabel: String = DateUtils.formatMonthYear(
        DateUtils.currentMonth(), DateUtils.currentYear()
    )

    /**
     * Reactive stream of the logged-in user's display name.
     * Emits "" when no session is active.
     * Converted to StateFlow so Compose can collect it with collectAsStateWithLifecycle().
     */
    val username: StateFlow<String> = sessionManager.username
        .stateIn(
            scope         = viewModelScope,
            started       = SharingStarted.WhileSubscribed(5_000),
            initialValue  = ""
        )

    /**
     * Reactive stream of the current month's total spending in ZAR.
     *
     * Uses flatMapLatest: when the userId changes (login/logout), the old Flow is
     * cancelled and a new one is created for the new user. This prevents data bleed.
     *
     * Emits 0.0 when logged out (NO_USER) or when there are no expenses this month.
     */
    val monthTotal: StateFlow<Double> = sessionManager.userId
        .flatMapLatest { userId ->
            if (userId == NO_USER) {
                flowOf(0.0)
            } else {
                val startMs = DateUtils.startOfMonth(DateUtils.currentMonth(), DateUtils.currentYear())
                val endMs   = DateUtils.endOfMonth(DateUtils.currentMonth(), DateUtils.currentYear())
                expenseRepository.getTotalForPeriod(userId, startMs, endMs)
            }
        }
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0.0
        )

    /**
     * Log out the current user.
     * Clears the DataStore session — all flows in other ViewModels will switch to NO_USER
     * state and emit empty data. Navigation to Login is handled by HomeScreen observing
     * a separate logout callback.
     */
    fun logout() {
        viewModelScope.launch {
            sessionManager.clear()
        }
    }
}

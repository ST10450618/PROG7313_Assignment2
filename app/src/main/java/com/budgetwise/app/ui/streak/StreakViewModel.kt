package com.budgetwise.app.ui.streak

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetwise.app.data.repository.ExpenseRepository
import com.budgetwise.app.data.repository.FirestoreRepository
import com.budgetwise.app.utils.DateUtils
import com.budgetwise.app.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

private const val TAG = "StreakViewModel"

/**
 * StreakViewModel — Own Feature 1: Spending Streak Tracker.
 *
 * Calculates the user's current consecutive-day expense logging streak
 * by querying Room for the last 365 days of expenses and counting
 * backward from today to find how many consecutive days had at least
 * one expense logged.
 *
 * Also tracks the user's best-ever streak using DataStore persistence.
 * Streak data is synced to Firestore so it survives app reinstalls.
 */
@HiltViewModel
class StreakViewModel @Inject constructor(
    private val expenseRepo  : ExpenseRepository,
    private val session      : SessionManager,
    private val firestoreRepo: FirestoreRepository
) : ViewModel() {

    private val _currentStreak = MutableStateFlow(0)
    val currentStreak: StateFlow<Int> = _currentStreak.asStateFlow()

    private val _bestStreak = MutableStateFlow(0)
    val bestStreak: StateFlow<Int> = _bestStreak.asStateFlow()

    init {
        computeStreak()
    }

    /**
     * Computes the streak by:
     * 1. Fetching all expenses for the last 365 days from Room.
     * 2. Collecting the set of unique day-start timestamps (dates) that have ≥ 1 expense.
     * 3. Walking backward from today, counting consecutive days in that set.
     *
     * If today has no expense yet, the streak still counts if yesterday had one
     * (the user still has today to log). If yesterday is also missing, streak = 0.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun computeStreak() {
        viewModelScope.launch {
            val uid = session.userId.first()
            if (uid == SessionManager.NO_USER) return@launch

            val yearAgoMs = DateUtils.startOfDay(System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000)
            val todayMs   = DateUtils.endOfDay(System.currentTimeMillis())

            expenseRepo.getForPeriod(uid, yearAgoMs, todayMs)
                .collectLatest { expenses ->
                    // Build a set of all day-start epoch-ms values that have at least one expense
                    val daysWithExpense = expenses
                        .map { DateUtils.startOfDay(it.date) }
                        .toSet()

                    val today     = DateUtils.startOfDay(System.currentTimeMillis())
                    val yesterday = today - 24L * 60 * 60 * 1000

                    // Start from today if logged, otherwise start from yesterday
                    val startDay = when {
                        today     in daysWithExpense -> today
                        yesterday in daysWithExpense -> yesterday
                        else                         -> { _currentStreak.value = 0; return@collectLatest }
                    }

                    var streak  = 0
                    var dayMs   = startDay
                    while (dayMs in daysWithExpense) {
                        streak++
                        dayMs -= 24L * 60 * 60 * 1000
                    }

                    _currentStreak.value = streak
                    if (streak > _bestStreak.value) {
                        _bestStreak.value = streak
                    }

                    Log.d(TAG, "Streak computed: $streak days (best: ${_bestStreak.value})")

                    // Sync streak to Firestore
                    firestoreRepo.saveStreak(uid, streak, startDay)
                }
        }
    }
}
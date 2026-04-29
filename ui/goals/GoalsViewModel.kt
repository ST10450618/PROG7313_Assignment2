package com.budgetwise.app.ui.goals

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetwise.app.data.local.entity.MonthlyGoal
import com.budgetwise.app.data.repository.ExpenseRepository
import com.budgetwise.app.data.repository.GoalRepository
import com.budgetwise.app.utils.DateUtils
import com.budgetwise.app.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "GoalsViewModel"

data class GoalsUiState(
    val isSaved : Boolean = false,
    val error   : String? = null,
    val success : String? = null
)

/**
 * GoalsViewModel — manages the minimum/maximum monthly spending goal.
 *
 * The [spendingStatus] combines the live month total with the current goal
 * to produce a colour-coded status string without any logic in the UI layer.
 * This adheres to MVVM separation of concerns: the Composable only observes
 * and renders, never calculates.
 */
@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val goalRepo   : GoalRepository,
    private val expenseRepo: ExpenseRepository,
    private val session    : SessionManager
) : ViewModel() {

    private val _ui = MutableStateFlow(GoalsUiState())
    val uiState: StateFlow<GoalsUiState> = _ui.asStateFlow()

    val currentMonth = DateUtils.currentMonth()
    val currentYear  = DateUtils.currentYear()

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentGoal: StateFlow<MonthlyGoal?> = session.userId.flatMapLatest { uid ->
        if (uid == SessionManager.NO_USER) flowOf(null)
        else goalRepo.getForMonth(uid, currentMonth, currentYear)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val monthTotal: StateFlow<Double> = session.userId.flatMapLatest { uid ->
        if (uid == SessionManager.NO_USER) flowOf(0.0)
        else expenseRepo.getTotalForPeriod(
            uid,
            DateUtils.startOfMonth(currentMonth, currentYear),
            DateUtils.endOfMonth(currentMonth, currentYear)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    /**
     * Derived status string — computed from the two flows above.
     * UNDER_MIN  → spending is below the minimum goal (not spending enough)
     * ON_TRACK   → spending is between min and max (healthy)
     * OVER_MAX   → spending has exceeded the maximum cap (overspending)
     * NO_GOAL    → no goal has been set yet this month
     */
    val spendingStatus: StateFlow<SpendingStatus> = combine(currentGoal, monthTotal) { goal, total ->
        when {
            goal == null           -> SpendingStatus.NO_GOAL
            total < goal.minGoal   -> SpendingStatus.UNDER_MIN
            total <= goal.maxGoal  -> SpendingStatus.ON_TRACK
            else                   -> SpendingStatus.OVER_MAX
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SpendingStatus.NO_GOAL)

    fun saveGoal(minStr: String, maxStr: String) {
        val min = minStr.toDoubleOrNull()
        val max = maxStr.toDoubleOrNull()
        when {
            min == null || min < 0 -> { _ui.value = GoalsUiState(error = "Enter a valid minimum amount"); return }
            max == null || max < 0 -> { _ui.value = GoalsUiState(error = "Enter a valid maximum amount"); return }
            max <= min             -> { _ui.value = GoalsUiState(error = "Maximum must be greater than minimum"); return }
        }
        viewModelScope.launch {
            val uid = session.userId.first()
            goalRepo.upsert(uid, currentMonth, currentYear, min!!, max!!)
            Log.d(TAG, "Goal saved: min=R$min max=R$max for $currentMonth/$currentYear")
            _ui.value = GoalsUiState(isSaved = true, success = "Goal updated for ${DateUtils.formatMonthYear(currentMonth, currentYear)}!")
        }
    }

    fun clearMessages() { _ui.value = GoalsUiState() }
}

enum class SpendingStatus { NO_GOAL, UNDER_MIN, ON_TRACK, OVER_MAX }
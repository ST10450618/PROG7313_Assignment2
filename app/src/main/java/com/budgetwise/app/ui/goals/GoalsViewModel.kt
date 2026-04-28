package com.budgetwise.app.ui.goals

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

/**
 * TODO (Seth): Complete the Goals screen logic.
 *
 * SpendingStatus drives the colour-coded status card in GoalsScreen:
 *  NO_GOAL   → grey  "Set a goal to track your spending"
 *  UNDER_MIN → YellowHighlight "You're spending less than your minimum"
 *  ON_TRACK  → TealPrimary    "You're on track!"
 *  OVER_MAX  → CoralAlert     "You've exceeded your budget"
 */
enum class SpendingStatus { NO_GOAL, UNDER_MIN, ON_TRACK, OVER_MAX }

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val goalRepo   : GoalRepository,
    private val expenseRepo: ExpenseRepository,
    private val session    : SessionManager
) : ViewModel() {

    private val _uiMessage = MutableStateFlow<String?>(null)
    private val _isSaved   = MutableStateFlow(false)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()
    val isSaved  : StateFlow<Boolean> = _isSaved.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentGoal: StateFlow<MonthlyGoal?> = session.userId.flatMapLatest { uid ->
        if (uid == SessionManager.NO_USER) flowOf(null)
        else goalRepo.getForMonth(uid, DateUtils.currentMonth(), DateUtils.currentYear())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val monthTotal: StateFlow<Double> = session.userId.flatMapLatest { uid ->
        if (uid == SessionManager.NO_USER) flowOf(0.0)
        else expenseRepo.getTotalForPeriod(
            uid,
            DateUtils.startOfMonth(DateUtils.currentMonth(), DateUtils.currentYear()),
            DateUtils.endOfMonth(DateUtils.currentMonth(), DateUtils.currentYear())
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val spendingStatus: StateFlow<SpendingStatus> = combine(currentGoal, monthTotal) { goal, total ->
        when {
            goal == null      -> SpendingStatus.NO_GOAL
            total < goal.minGoal -> SpendingStatus.UNDER_MIN
            total > goal.maxGoal -> SpendingStatus.OVER_MAX
            else              -> SpendingStatus.ON_TRACK
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SpendingStatus.NO_GOAL)

    fun saveGoal(minStr: String, maxStr: String) {
        // TODO (Seth): Validation must match GoalsViewModelTest:
        //  - blank min → error not null
        //  - max <= min → "Maximum must be greater than minimum"
        //  - negative min → error not null
        val min = minStr.toDoubleOrNull()
        val max = maxStr.toDoubleOrNull()
        when {
            min == null || min < 0 -> { _uiMessage.value = "Enter a valid minimum goal amount"; return }
            max == null            -> { _uiMessage.value = "Enter a valid maximum goal amount"; return }
            max <= min             -> { _uiMessage.value = "Maximum must be greater than minimum"; return }
        }
        viewModelScope.launch {
            val uid = session.userId.first()
            goalRepo.upsert(uid, DateUtils.currentMonth(), DateUtils.currentYear(), min!!, max!!)
            _isSaved.value = true
            _uiMessage.value = "Goal saved for ${DateUtils.formatMonthYear(
                DateUtils.currentMonth(), DateUtils.currentYear()
            )}"
        }
    }

    fun clearMessages() { _uiMessage.value = null; _isSaved.value = false }
}

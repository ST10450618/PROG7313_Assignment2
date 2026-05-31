package com.budgetwise.app.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetwise.app.data.local.entity.MonthlyGoal
import com.budgetwise.app.data.repository.ExpenseRepository
import com.budgetwise.app.data.repository.FirestoreRepository
import com.budgetwise.app.data.repository.GoalRepository
import com.budgetwise.app.utils.DateUtils
import com.budgetwise.app.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// =============================================================================
// State models
// =============================================================================

/** UI state for the Goals form. */
data class GoalsUiState(
    val error  : String? = null,
    val isSaved: Boolean = false
)

/**
 * Colour-coded spending status, drives GoalStatusCard in GoalsScreen:
 *  NO_GOAL   → grey   "No goal set yet"
 *  UNDER_MIN → yellow "Spending below minimum"
 *  ON_TRACK  → teal   "On track!"
 *  OVER_MAX  → coral  "Over budget!"
 */
enum class SpendingStatus { NO_GOAL, UNDER_MIN, ON_TRACK, OVER_MAX }

// =============================================================================
// ViewModel
// =============================================================================

/**
 * GoalsViewModel — updated for Final PoE to sync goals to Firestore.
 */
@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val goalRepo     : GoalRepository,
    private val expenseRepo  : ExpenseRepository,
    private val session      : SessionManager,
    private val firestoreRepo: FirestoreRepository   // Final PoE: online sync
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoalsUiState())
    val uiState: StateFlow<GoalsUiState> = _uiState.asStateFlow()

    // Convenience aliases used by GoalsScreen
    val uiMessage: StateFlow<String?> = _uiState.map { it.error }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val isSaved: StateFlow<Boolean> = _uiState.map { it.isSaved }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

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
            goal == null         -> SpendingStatus.NO_GOAL
            total < goal.minGoal -> SpendingStatus.UNDER_MIN
            total > goal.maxGoal -> SpendingStatus.OVER_MAX
            else                 -> SpendingStatus.ON_TRACK
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SpendingStatus.NO_GOAL)

    /**
     * Validates and persists the monthly goal to Room, then syncs to Firestore.
     *
     * Validation order (matches GoalsViewModelTest expectations):
     *  1. min null or negative → error
     *  2. max null or negative → error
     *  3. max ≤ min            → "Maximum must be greater than minimum"
     */
    fun saveGoal(minStr: String, maxStr: String) {
        val min = minStr.toDoubleOrNull()
        val max = maxStr.toDoubleOrNull()
        when {
            min == null || min < 0.0 -> {
                _uiState.value = GoalsUiState(error = "Enter a valid minimum amount (e.g. 500.00)")
                return
            }
            max == null || max < 0.0 -> {
                _uiState.value = GoalsUiState(error = "Enter a valid maximum amount (e.g. 3000.00)")
                return
            }
            max <= min -> {
                _uiState.value = GoalsUiState(error = "Maximum must be greater than minimum")
                return
            }
        }
        viewModelScope.launch {
            val uid = session.userId.first()

            // 1. Upsert into local Room database
            goalRepo.upsert(uid, DateUtils.currentMonth(), DateUtils.currentYear(), min!!, max!!)

            // 2. Sync to Firestore (Final PoE — online database requirement)
            val goal = MonthlyGoal(
                userId   = uid,
                month    = DateUtils.currentMonth(),
                year     = DateUtils.currentYear(),
                minGoal  = min,
                maxGoal  = max
            )
            firestoreRepo.saveGoal(uid, goal)

            _uiState.value = GoalsUiState(
                isSaved = true,
                error   = "Goal saved for ${DateUtils.formatMonthYear(
                    DateUtils.currentMonth(), DateUtils.currentYear()
                )}"
            )
        }
    }

    fun clearMessages() {
        _uiState.value = GoalsUiState(error = null, isSaved = false)
    }
}
package com.budgetwise.app.ui.tips

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetwise.app.data.repository.CategoryRepository
import com.budgetwise.app.data.repository.ExpenseRepository
import com.budgetwise.app.data.repository.GoalRepository
import com.budgetwise.app.ui.theme.*
import com.budgetwise.app.utils.DateUtils
import com.budgetwise.app.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

/**
 * SmartTipsViewModel — Own Feature 2: Personalised Budget Tips.
 *
 * Analyses the user's current month spending data and generates
 * context-aware tips. Tips are reactive — they update automatically
 * when expenses or goals change, so advice is always current.
 */
@HiltViewModel
class SmartTipsViewModel @Inject constructor(
    private val expenseRepo : ExpenseRepository,
    private val categoryRepo: CategoryRepository,
    private val goalRepo    : GoalRepository,
    private val session     : SessionManager
) : ViewModel() {

    private val month = DateUtils.currentMonth()
    private val year  = DateUtils.currentYear()
    private val start = DateUtils.startOfMonth(month, year)
    private val end   = DateUtils.endOfMonth(month, year)

    @OptIn(ExperimentalCoroutinesApi::class)
    val tips: StateFlow<List<BudgetTip>> = session.userId.flatMapLatest { uid ->
        if (uid == SessionManager.NO_USER) flowOf(emptyList())
        else combine(
            expenseRepo.getTotalForPeriod(uid, start, end),
            expenseRepo.getCategoryTotals(uid, start, end),
            categoryRepo.getForUser(uid),
            goalRepo.getForMonth(uid, month, year)
        ) { total, catTotals, cats, goal ->

            val tips = mutableListOf<BudgetTip>()
            val catMap = cats.associateBy { it.id }

            // Tip 1: Over budget warning
            if (goal != null && total > goal.maxGoal) {
                val over = total - goal.maxGoal
                tips.add(BudgetTip(
                    title     = "Over Budget!",
                    body      = "You're R${"%.2f".format(over)} over your maximum goal. " +
                            "Try to avoid non-essential purchases for the rest of the month.",
                    icon      = Icons.Filled.Warning,
                    iconColor = CoralAlert,
                    bgColor   = CoralAlert.copy(alpha = 0.08f)
                ))
            }

            // Tip 2: On track encouragement
            if (goal != null && total in goal.minGoal..goal.maxGoal) {
                tips.add(BudgetTip(
                    title     = "You're on track! 🎉",
                    body      = "You've spent R${"%.2f".format(total)} this month — well within your " +
                            "R${"%.2f".format(goal.minGoal)}–R${"%.2f".format(goal.maxGoal)} goal range. Keep it up!",
                    icon      = Icons.Filled.CheckCircle,
                    iconColor = TealPrimary,
                    bgColor   = TealPrimary.copy(alpha = 0.08f)
                ))
            }

            // Tip 3: Under minimum
            if (goal != null && total < goal.minGoal && total > 0) {
                tips.add(BudgetTip(
                    title     = "Spending below minimum",
                    body      = "You've only spent R${"%.2f".format(total)} but your minimum goal is " +
                            "R${"%.2f".format(goal.minGoal)}. Make sure essential expenses are being tracked!",
                    icon      = Icons.Filled.TrendingDown,
                    iconColor = YellowHighlight,
                    bgColor   = YellowHighlight.copy(alpha = 0.10f)
                ))
            }

            // Tip 4: Top spending category
            val topCat = catTotals.maxByOrNull { it.total }
            if (topCat != null) {
                val catName = catMap[topCat.categoryId]?.name ?: "Unknown"
                val pct     = if (total > 0) topCat.total / total * 100 else 0.0
                tips.add(BudgetTip(
                    title     = "Top spending: $catName",
                    body      = "$catName accounts for ${"%.0f".format(pct)}% of your spending " +
                            "(R${"%.2f".format(topCat.total)}). " +
                            if (pct > 50) "Consider whether you can reduce this." else "This looks balanced.",
                    icon      = Icons.Filled.BarChart,
                    iconColor = TealPrimary,
                    bgColor   = SurfaceWhite
                ))
            }

            // Tip 5: No goal set
            if (goal == null) {
                tips.add(BudgetTip(
                    title     = "Set a monthly goal",
                    body      = "You haven't set a spending goal for this month yet. " +
                            "Go to Goals to set a minimum and maximum to unlock personalised advice.",
                    icon      = Icons.Filled.TrackChanges,
                    iconColor = NavyTertiary,
                    bgColor   = NavyTertiary.copy(alpha = 0.07f)
                ))
            }

            // Tip 6: Remaining budget
            if (goal != null && total < goal.maxGoal) {
                val remaining = goal.maxGoal - total
                tips.add(BudgetTip(
                    title     = "Remaining budget: R${"%.2f".format(remaining)}",
                    body      = "You have R${"%.2f".format(remaining)} left before reaching your maximum goal. " +
                            "Consider putting some of this into savings!",
                    icon      = Icons.Filled.Savings,
                    iconColor = GreenSecondary,
                    bgColor   = GreenSecondary.copy(alpha = 0.08f)
                ))
            }

            // Tip 7: No expenses yet
            if (total == 0.0) {
                tips.add(BudgetTip(
                    title     = "No expenses logged yet",
                    body      = "Start logging your expenses to get personalised tips based on your real spending habits.",
                    icon      = Icons.Filled.AddCircle,
                    iconColor = TealPrimary,
                    bgColor   = TealPrimary.copy(alpha = 0.07f)
                ))
            }

            tips
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
package com.budgetwise.app.ui.reports

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetwise.app.data.local.dao.CategoryTotal
import com.budgetwise.app.data.local.entity.Category
import com.budgetwise.app.data.repository.CategoryRepository
import com.budgetwise.app.data.repository.ExpenseRepository
import com.budgetwise.app.utils.DateUtils
import com.budgetwise.app.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

private const val TAG = "ReportViewModel"

data class CategoryReportRow(
    val category : Category?,
    val total    : Double,
    val percent  : Float
)

data class ReportFilterState(
    val startMs: Long = DateUtils.startOfMonth(DateUtils.currentMonth(), DateUtils.currentYear()),
    val endMs  : Long = DateUtils.endOfMonth(DateUtils.currentMonth(), DateUtils.currentYear())
)

/**
 * ReportViewModel — drives the period-filtered category totals report.
 *
 * [categoryRows] is a derived flow combining the raw [CategoryTotal] aggregation
 * from Room with the categories list to produce human-readable [CategoryReportRow]
 * objects with percentage breakdowns. All computation stays in the ViewModel,
 * keeping the Composable a pure rendering concern.
 */
@HiltViewModel
class ReportViewModel @Inject constructor(
    private val expenseRepo : ExpenseRepository,
    private val categoryRepo: CategoryRepository,
    private val session     : SessionManager
) : ViewModel() {

    private val _filter = MutableStateFlow(ReportFilterState())
    val filterState: StateFlow<ReportFilterState> = _filter.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val categories: StateFlow<List<Category>> = session.userId.flatMapLatest { uid ->
        if (uid == SessionManager.NO_USER) flowOf(emptyList())
        else categoryRepo.getForUser(uid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val rawTotals: StateFlow<List<CategoryTotal>> = combine(session.userId, _filter) { uid, f ->
        Pair(uid, f)
    }.flatMapLatest { (uid, f) ->
        if (uid == SessionManager.NO_USER) flowOf(emptyList())
        else expenseRepo.getCategoryTotals(uid, f.startMs, f.endMs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val periodTotal: StateFlow<Double> = combine(session.userId, _filter) { uid, f ->
        Pair(uid, f)
    }.flatMapLatest { (uid, f) ->
        if (uid == SessionManager.NO_USER) flowOf(0.0)
        else expenseRepo.getTotalForPeriod(uid, f.startMs, f.endMs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    /** Enriched rows sorted by spend descending — highest spend category first. */
    val categoryRows: StateFlow<List<CategoryReportRow>> = combine(rawTotals, categories, periodTotal) { totals, cats, grandTotal ->
        totals.map { ct ->
            CategoryReportRow(
                category = cats.find { it.id == ct.categoryId },
                total    = ct.total,
                percent  = if (grandTotal > 0) (ct.total / grandTotal * 100).toFloat() else 0f
            )
        }.sortedByDescending { it.total }.also {
            Log.d(TAG, "Report rows computed: ${it.size} categories")
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateFilter(startMs: Long, endMs: Long) {
        Log.d(TAG, "Report filter: ${DateUtils.formatDate(startMs)} → ${DateUtils.formatDate(endMs)}")
        _filter.value = ReportFilterState(startMs = startMs, endMs = DateUtils.endOfDay(endMs))
    }
}
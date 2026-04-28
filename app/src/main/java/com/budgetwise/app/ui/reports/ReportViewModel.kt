package com.budgetwise.app.ui.reports

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

/**
 * TODO (Seth): Complete the Report screen logic.
 *
 * [CategoryReportRow] is the display model for each row in the report list.
 * [categoryRows] maps raw CategoryTotal DAOs to enriched rows with category
 * name/colour and percentage of the period total.
 */
data class CategoryReportRow(
    val category: Category,
    val total   : Double,
    val percent : Float   // 0f–100f
)

data class ReportFilterState(
    val startMs: Long = DateUtils.startOfMonth(DateUtils.currentMonth(), DateUtils.currentYear()),
    val endMs  : Long = DateUtils.endOfMonth(DateUtils.currentMonth(), DateUtils.currentYear())
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val expenseRepo : ExpenseRepository,
    private val categoryRepo: CategoryRepository,
    private val session     : SessionManager
) : ViewModel() {

    private val _filter = MutableStateFlow(ReportFilterState())

    @OptIn(ExperimentalCoroutinesApi::class)
    val periodTotal: StateFlow<Double> = session.userId.flatMapLatest { uid ->
        if (uid == SessionManager.NO_USER) flowOf(0.0)
        else _filter.flatMapLatest { f ->
            expenseRepo.getTotalForPeriod(uid, f.startMs, f.endMs)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val categoryRows: StateFlow<List<CategoryReportRow>> = session.userId.flatMapLatest { uid ->
        if (uid == SessionManager.NO_USER) flowOf(emptyList())
        else _filter.flatMapLatest { f ->
            combine(
                expenseRepo.getCategoryTotals(uid, f.startMs, f.endMs),
                categoryRepo.getForUser(uid),
                periodTotal
            ) { totals: List<CategoryTotal>, cats: List<Category>, grandTotal: Double ->
                val catMap = cats.associateBy { it.id }
                totals.mapNotNull { ct ->
                    catMap[ct.categoryId]?.let { cat ->
                        CategoryReportRow(
                            category = cat,
                            total    = ct.total,
                            percent  = if (grandTotal > 0) (ct.total / grandTotal * 100f).toFloat() else 0f
                        )
                    }
                }.sortedByDescending { it.total }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateFilter(startMs: Long, endMs: Long) {
        _filter.value = ReportFilterState(
            startMs = DateUtils.startOfDay(startMs),
            endMs   = DateUtils.endOfDay(endMs)
        )
    }
}

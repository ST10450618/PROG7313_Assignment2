package com.budgetwise.app.ui.expense

import com.budgetwise.app.data.local.entity.Category
import com.budgetwise.app.data.local.entity.Expense
import com.budgetwise.app.data.repository.CategoryRepository
import com.budgetwise.app.data.repository.ExpenseRepository
import com.budgetwise.app.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import org.mockito.kotlin.*

/**
 * Unit tests for [ExpenseViewModel].
 *
 * These tests validate the validation logic and state transitions without
 * touching the Android framework or Room database. MockK / Mockito stubs
 * replace all repository calls so tests run on the JVM (no emulator required),
 * which is why GitHub Actions CI can run these in a standard Linux runner.
 */
@ExperimentalCoroutinesApi
class ExpenseViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel   : ExpenseViewModel
    private lateinit var expenseRepo : ExpenseRepository
    private lateinit var categoryRepo: CategoryRepository
    private lateinit var session     : SessionManager

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        expenseRepo  = mock()
        categoryRepo = mock()
        session      = mock()

        // Default stubs — empty flows so ViewModel initialises cleanly
        whenever(session.userId).thenReturn(flowOf(1L))
        whenever(expenseRepo.getForPeriod(any(), any(), any())).thenReturn(flowOf(emptyList()))
        whenever(expenseRepo.getTotalForPeriod(any(), any(), any())).thenReturn(flowOf(0.0))
        whenever(categoryRepo.getForUser(any())).thenReturn(flowOf(emptyList()))

        viewModel = ExpenseViewModel(expenseRepo, categoryRepo, session)
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    // ── Validation: blank description ─────────────────────────────────────
    @Test
    fun `saveExpense with blank description sets error`() = runTest {
        viewModel.saveExpense("100.00", "", System.currentTimeMillis(), "09:00", "10:00", 1L, null)
        assertEquals("Description is required", viewModel.uiState.value.errorMsg)
    }

    // ── Validation: zero amount ───────────────────────────────────────────
    @Test
    fun `saveExpense with zero amount sets error`() = runTest {
        viewModel.saveExpense("0", "Lunch", System.currentTimeMillis(), "12:00", "13:00", 1L, null)
        assertNotNull(viewModel.uiState.value.errorMsg)
        assertTrue(viewModel.uiState.value.errorMsg!!.contains("valid amount"))
    }

    // ── Validation: non-numeric amount ────────────────────────────────────
    @Test
    fun `saveExpense with non-numeric amount sets error`() = runTest {
        viewModel.saveExpense("abc", "Fuel", System.currentTimeMillis(), "08:00", "09:00", 1L, null)
        assertNotNull(viewModel.uiState.value.errorMsg)
    }

    // ── Validation: end time before start time ────────────────────────────
    @Test
    fun `saveExpense rejects end time before start time`() = runTest {
        viewModel.saveExpense("50.00", "Coffee", System.currentTimeMillis(), "10:00", "09:00", 1L, null)
        assertEquals("End time must be after start time", viewModel.uiState.value.errorMsg)
    }

    // ── Validation: equal start and end time ──────────────────────────────
    @Test
    fun `saveExpense rejects equal start and end time`() = runTest {
        viewModel.saveExpense("50.00", "Coffee", System.currentTimeMillis(), "10:00", "10:00", 1L, null)
        assertEquals("End time must be after start time", viewModel.uiState.value.errorMsg)
    }

    // ── Validation: null category ─────────────────────────────────────────
    @Test
    fun `saveExpense with null category sets error`() = runTest {
        viewModel.saveExpense("100.00", "Groceries", System.currentTimeMillis(), "09:00", "10:00", null, null)
        assertEquals("Please select a category", viewModel.uiState.value.errorMsg)
    }

    // ── State: clearMessages resets error ─────────────────────────────────
    @Test
    fun `clearMessages resets error to null`() = runTest {
        viewModel.saveExpense("0", "Test", System.currentTimeMillis(), "09:00", "10:00", 1L, null)
        assertNotNull(viewModel.uiState.value.errorMsg)
        viewModel.clearMessages()
        assertNull(viewModel.uiState.value.errorMsg)
    }

    // ── Filter: updateFilter changes filterState ──────────────────────────
    @Test
    fun `updateFilter updates filterState correctly`() = runTest {
        val start = 1_000_000L
        val end   = 9_000_000L
        viewModel.updateFilter(start, end)
        assertEquals(start, viewModel.filterState.value.startMs)
    }
}
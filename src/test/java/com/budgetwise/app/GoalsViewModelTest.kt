package com.budgetwise.app.ui.goals

import com.budgetwise.app.data.repository.ExpenseRepository
import com.budgetwise.app.data.repository.GoalRepository
import com.budgetwise.app.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import org.mockito.kotlin.*

@ExperimentalCoroutinesApi
class GoalsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel  : GoalsViewModel
    private lateinit var goalRepo   : GoalRepository
    private lateinit var expenseRepo: ExpenseRepository
    private lateinit var session    : SessionManager

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        goalRepo    = mock()
        expenseRepo = mock()
        session     = mock()
        whenever(session.userId).thenReturn(flowOf(1L))
        whenever(goalRepo.getForMonth(any(), any(), any())).thenReturn(flowOf(null))
        whenever(expenseRepo.getTotalForPeriod(any(), any(), any())).thenReturn(flowOf(0.0))
        viewModel = GoalsViewModel(goalRepo, expenseRepo, session)
    }

    @After fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `saveGoal with blank min sets error`() = runTest {
        viewModel.saveGoal("", "5000")
        assertNotNull(viewModel.uiState.value.error)
    }

    @Test
    fun `saveGoal where max less than min sets error`() = runTest {
        viewModel.saveGoal("5000", "3000")
        assertEquals("Maximum must be greater than minimum", viewModel.uiState.value.error)
    }

    @Test
    fun `saveGoal where max equals min sets error`() = runTest {
        viewModel.saveGoal("3000", "3000")
        assertEquals("Maximum must be greater than minimum", viewModel.uiState.value.error)
    }

    @Test
    fun `saveGoal with negative min sets error`() = runTest {
        viewModel.saveGoal("-100", "5000")
        assertNotNull(viewModel.uiState.value.error)
    }

    @Test
    fun `clearMessages resets state`() = runTest {
        viewModel.saveGoal("", "")
        viewModel.clearMessages()
        assertNull(viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isSaved)
    }
}
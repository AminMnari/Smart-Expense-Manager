package com.smartexpense.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartexpense.domain.model.Expense
import com.smartexpense.domain.usecase.GetExpensesUseCase
import com.smartexpense.domain.usecase.SaveExpenseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.YearMonth
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for dashboard summaries and monthly expense tracking.
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getExpensesUseCase: GetExpensesUseCase,
    private val saveExpenseUseCase: SaveExpenseUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        DashboardUiState(
            expenses = emptyList(),
            categoryTotals = emptyMap(),
            totalSpent = 0.0,
            selectedMonth = YearMonth.now(),
            isLoading = true,
            error = null
        )
    )
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    private var observeJob: Job? = null

    init {
        observeMonth(YearMonth.now())
    }

    fun onMonthChanged(month: YearMonth) {
        observeMonth(month)
    }

    fun onDeleteExpense(expense: Expense) {
        viewModelScope.launch {
            saveExpenseUseCase.delete(expense).onFailure { exception ->
                _uiState.value = _uiState.value.copy(error = exception.message ?: "Failed to delete expense")
            }
        }
    }

    private fun observeMonth(month: YearMonth) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(selectedMonth = month, isLoading = true, error = null)
            try {
                getExpensesUseCase(month.atDay(1), month.atEndOfMonth()).collect { expenses ->
                    _uiState.value = DashboardUiState(
                        expenses = expenses,
                        categoryTotals = calculateCategoryTotals(expenses),
                        totalSpent = expenses.sumOf { it.amount },
                        selectedMonth = month,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = exception.message ?: "Failed to load dashboard"
                )
            }
        }
    }

    private fun calculateCategoryTotals(expenses: List<Expense>): Map<String, Double> {
        return expenses.groupBy { it.categoryId }
            .mapKeys { (categoryId, _) -> "Category $categoryId" }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
    }
}

/**
 * Dashboard screen UI state.
 */
data class DashboardUiState(
    val expenses: List<Expense>,
    val categoryTotals: Map<String, Double>,
    val totalSpent: Double,
    val selectedMonth: YearMonth,
    val isLoading: Boolean,
    val error: String?
)


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
    private var expensesJob: Job? = null

    init {
        val currentMonth = YearMonth.now()
        _uiState.value = _uiState.value.copy(selectedMonth = currentMonth)
        loadExpenses(currentMonth)
    }

    fun onMonthChanged(month: YearMonth) {
        _uiState.value = _uiState.value.copy(selectedMonth = month)
        loadExpenses(month)
    }

    fun onDeleteExpense(expense: Expense) {
        viewModelScope.launch {
            saveExpenseUseCase.delete(expense).onFailure { exception ->
                _uiState.value = _uiState.value.copy(error = exception.message ?: "Failed to delete expense")
            }
        }
    }

    private fun loadExpenses(month: YearMonth) {
        expensesJob?.cancel()
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        expensesJob = viewModelScope.launch {
            getExpensesUseCase(
                start = month.atDay(1),
                end = month.atEndOfMonth()
            ).collect { expenses ->
                _uiState.value = _uiState.value.copy(
                    expenses = expenses,
                    totalSpent = expenses.sumOf { it.amount },
                    categoryTotals = expenses
                        .groupBy { it.categoryId.toString() }
                        .mapValues { (_, v) -> v.sumOf { it.amount } },
                    isLoading = false,
                    error = null
                )
            }
        }
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


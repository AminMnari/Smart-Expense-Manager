package com.smartexpense.presentation.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartexpense.domain.model.Expense
import com.smartexpense.domain.usecase.GetExpensesUseCase
import com.smartexpense.domain.usecase.SaveExpenseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import kotlinx.coroutines.Job
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for loading and filtering expenses.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val getExpensesUseCase: GetExpensesUseCase,
    private val saveExpenseUseCase: SaveExpenseUseCase
) : ViewModel() {

    private val filterFlow = MutableStateFlow(ExpenseFilter())
    private val _uiState = MutableStateFlow(
        ExpenseUiState(
            expenses = emptyList(),
            isLoading = true,
            error = null,
            filter = ExpenseFilter()
        )
    )
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()
    private var observeJob: Job? = null

    init {
        observeExpenses()
    }

    fun onFilterChanged(filter: ExpenseFilter) {
        _uiState.value = _uiState.value.copy(filter = filter, isLoading = true, error = null)
        filterFlow.value = filter
    }

    fun onDeleteExpense(expense: Expense) {
        viewModelScope.launch {
            saveExpenseUseCase.delete(expense).onFailure { exception ->
                _uiState.value = _uiState.value.copy(error = exception.message ?: "Failed to delete expense")
            }
        }
    }

    fun onSaveExpense(expense: Expense, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            saveExpenseUseCase(expense)
                .onSuccess {
                    onSuccess()
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(error = exception.message ?: "Failed to save expense")
                }
        }
    }

    fun onRestoreExpense(expense: Expense) {
        onSaveExpense(expense)
    }

    private fun observeExpenses() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            filterFlow.flatMapLatest { filter -> expensesFlow(filter) }
                .collect { expenses ->
                    _uiState.value = _uiState.value.copy(
                        expenses = expenses,
                        isLoading = false,
                        error = null
                    )
                }
        }
    }

    private fun expensesFlow(filter: ExpenseFilter) = when {
        filter.categoryId != null -> getExpensesUseCase(filter.categoryId)
        filter.startDate != null && filter.endDate != null -> getExpensesUseCase(filter.startDate, filter.endDate)
        else -> getExpensesUseCase()
    }
}

/**
 * Expense filtering criteria.
 */
data class ExpenseFilter(
    val categoryId: Long? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null
)

/**
 * Expense screen UI state.
 */
data class ExpenseUiState(
    val expenses: List<Expense>,
    val isLoading: Boolean,
    val error: String?,
    val filter: ExpenseFilter
)



package com.smartexpense.presentation.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartexpense.domain.model.Budget
import com.smartexpense.domain.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.YearMonth
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for loading and editing budget entries.
 */
@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    private val currentMonth = YearMonth.now()
    private val _uiState = MutableStateFlow(
        BudgetUiState(
            budgets = emptyList(),
            isLoading = true,
            error = null
        )
    )
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()
    private var observeJob: Job? = null

    init {
        observeBudgets(currentMonth)
    }

    fun onSaveBudget(categoryId: Long, limit: Double, threshold: Int) {
        viewModelScope.launch {
            runCatching {
                budgetRepository.upsertBudget(
                    Budget(
                        categoryId = categoryId,
                        monthlyLimit = limit,
                        alertThresholdPercent = threshold,
                        month = currentMonth
                    )
                )
            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(error = exception.message ?: "Failed to save budget")
            }
        }
    }

    fun onDeleteBudget(categoryId: Long) {
        viewModelScope.launch {
            runCatching {
                budgetRepository.deleteBudget(categoryId)
            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(error = exception.message ?: "Failed to delete budget")
            }
        }
    }

    private fun observeBudgets(month: YearMonth) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                budgetRepository.getBudgets(month).collect { budgets ->
                    _uiState.value = BudgetUiState(
                        budgets = budgets,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = exception.message ?: "Failed to load budgets"
                )
            }
        }
    }
}

/**
 * Budget screen UI state.
 */
data class BudgetUiState(
    val budgets: List<Budget>,
    val isLoading: Boolean,
    val error: String?
)


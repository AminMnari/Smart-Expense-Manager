package com.smartexpense.presentation.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartexpense.domain.model.InsightResult
import com.smartexpense.domain.usecase.GenerateInsightUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.YearMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for generating and displaying monthly insights.
 */
@HiltViewModel
class InsightViewModel @Inject constructor(
    private val generateInsightUseCase: GenerateInsightUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        InsightUiState(
            insight = null,
            isLoading = false,
            error = null
        )
    )
    val uiState: StateFlow<InsightUiState> = _uiState.asStateFlow()

    fun onGenerateInsight(month: YearMonth) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            generateInsightUseCase(month)
                .onSuccess { insight ->
                    _uiState.value = InsightUiState(
                        insight = insight,
                        isLoading = false,
                        error = null
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to generate insight"
                    )
                }
        }
    }
}

/**
 * Insight screen UI state.
 */
data class InsightUiState(
    val insight: InsightResult?,
    val isLoading: Boolean,
    val error: String?
)


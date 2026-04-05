package com.smartexpense.presentation.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartexpense.domain.model.AnomalyEvent
import com.smartexpense.domain.usecase.DetectAnomaliesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for spending anomaly alerts.
 */
@HiltViewModel
class AlertViewModel @Inject constructor(
    private val detectAnomaliesUseCase: DetectAnomaliesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AlertUiState(
            events = emptyList(),
            isLoading = true,
            error = null
        )
    )
    val uiState: StateFlow<AlertUiState> = _uiState.asStateFlow()

    init {
        refreshAlerts()
    }

    private fun refreshAlerts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            runCatching { detectAnomaliesUseCase() }
                .onSuccess { events ->
                    _uiState.value = AlertUiState(
                        events = events,
                        isLoading = false,
                        error = null
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to load alerts"
                    )
                }
        }
    }
}

/**
 * Alert screen UI state.
 */
data class AlertUiState(
    val events: List<AnomalyEvent>,
    val isLoading: Boolean,
    val error: String?
)


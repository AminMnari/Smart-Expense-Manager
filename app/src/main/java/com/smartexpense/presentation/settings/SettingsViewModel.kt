package com.smartexpense.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartexpense.data.local.db.AppDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for app settings operations.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appDatabase: AppDatabase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun clearAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                appDatabase.clearAllTables()
            }.onSuccess {
                _uiState.value = _uiState.value.copy(clearSuccess = true, error = null)
            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    clearSuccess = false,
                    error = exception.message ?: "Failed to clear data"
                )
            }
        }
    }

    fun consumeClearSuccess() {
        _uiState.value = _uiState.value.copy(clearSuccess = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * UI state for settings actions.
 */
data class SettingsUiState(
    val clearSuccess: Boolean = false,
    val error: String? = null
)


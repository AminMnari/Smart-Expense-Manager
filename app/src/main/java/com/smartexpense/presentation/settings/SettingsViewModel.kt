package com.smartexpense.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import com.smartexpense.data.local.db.AppDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for app settings operations.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val database: AppDatabase
) : ViewModel() {

    fun clearAllData(context: Context, onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            database.clearAllTables()
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }
}


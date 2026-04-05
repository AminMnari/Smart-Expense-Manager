package com.smartexpense.presentation.scan

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartexpense.domain.model.Expense
import com.smartexpense.domain.model.ParsedExpense
import com.smartexpense.domain.usecase.SaveExpenseUseCase
import com.smartexpense.domain.usecase.ScanReceiptUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * ViewModel for receipt scanning, OCR, and review actions.
 */
@HiltViewModel
class ScanViewModel @Inject constructor(
    private val scanReceiptUseCase: ScanReceiptUseCase,
    private val saveExpenseUseCase: SaveExpenseUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()
    private val editedFields = mutableMapOf<String, String>()

    fun onImageCaptured(uri: Uri, context: Context) {
        viewModelScope.launch {
            _uiState.value = ScanUiState.Processing
            try {
                val bitmap = withContext(Dispatchers.IO) { loadBitmap(context, uri) }
                scanReceiptUseCase(bitmap).fold(
                    onSuccess = { parsed ->
                        editedFields.clear()
                        _uiState.value = ScanUiState.Review(parsed)
                    },
                    onFailure = { exception ->
                        _uiState.value = ScanUiState.Error(exception.message ?: "Receipt scan failed")
                    }
                )
            } catch (exception: Exception) {
                _uiState.value = ScanUiState.Error(exception.message ?: "Receipt scan failed")
            }
        }
    }

    fun onConfirmExpense(parsed: ParsedExpense, editedFields: Map<String, String>) {
        viewModelScope.launch {
            _uiState.value = ScanUiState.Processing
            val mergedFields = this@ScanViewModel.editedFields + editedFields
            val expenseResult = buildExpense(parsed, mergedFields)
            expenseResult.fold(
                onSuccess = { expense ->
                    saveExpenseUseCase(expense).fold(
                        onSuccess = { _uiState.value = ScanUiState.Saved },
                        onFailure = { exception ->
                            _uiState.value = ScanUiState.Error(
                                exception.message ?: "Failed to save expense"
                            )
                        }
                    )
                },
                onFailure = { exception ->
                    _uiState.value = ScanUiState.Error(exception.message ?: "Invalid expense data")
                }
            )
        }
    }

    fun onFieldEdited(field: String, value: String) {
        editedFields[field] = value
    }

    fun resetState() {
        editedFields.clear()
        _uiState.value = ScanUiState.Idle
    }

    private fun loadBitmap(context: Context, uri: Uri): Bitmap {
        return context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
                ?: throw IOException("Unable to decode image")
        } ?: throw IOException("Unable to open image stream")
    }

    private fun buildExpense(parsed: ParsedExpense, edits: Map<String, String>): Result<Expense> = runCatching {
        val amount = edits["amount"]?.toDoubleOrNull() ?: parsed.amount
            ?: throw IllegalArgumentException("Amount is required")
        val merchantName = edits["merchantName"] ?: parsed.merchantName
            ?: throw IllegalArgumentException("Merchant name is required")
        val date = edits["date"]?.let(LocalDate::parse) ?: parsed.date
            ?: throw IllegalArgumentException("Date is required")
        val categoryId = edits["categoryId"]?.toLongOrNull()
            ?: throw IllegalArgumentException("Category is required")

        Expense(
            amount = amount,
            merchantName = merchantName,
            categoryId = categoryId,
            date = date,
            notes = edits["notes"].orEmpty(),
            receiptImagePath = edits["receiptImagePath"],
            isAiCategorized = true,
            createdAt = LocalDateTime.now()
        )
    }
}

/**
 * Sealed UI state for the scan screen.
 */
sealed class ScanUiState {
    /** Idle state before scanning starts. */
    data object Idle : ScanUiState()

    /** Camera state used by the scan flow while the user is capturing a receipt. */
    data object CameraOpen : ScanUiState()

    /** Processing state while OCR or parsing is running. */
    data object Processing : ScanUiState()

    /** Review state showing the parsed receipt data. */
    data class Review(val parsed: ParsedExpense) : ScanUiState()

    /** Error state for recoverable scan or save failures. */
    data class Error(val message: String) : ScanUiState()

    /** Success state once the expense has been saved. */
    data object Saved : ScanUiState()
}



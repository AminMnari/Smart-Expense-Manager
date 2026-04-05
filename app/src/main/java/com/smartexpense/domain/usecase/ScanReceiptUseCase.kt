package com.smartexpense.domain.usecase

import android.graphics.Bitmap
import com.smartexpense.data.remote.gemini.GeminiApiService
import com.smartexpense.data.remote.ocr.MlKitOcrService
import com.smartexpense.domain.model.ParsedExpense
import javax.inject.Inject

/**
 * Scans a receipt bitmap and converts it into structured expense data.
 */
class ScanReceiptUseCase @Inject constructor(
    private val mlKitOcrService: MlKitOcrService,
    private val geminiApiService: GeminiApiService
) {

    suspend operator fun invoke(bitmap: Bitmap): Result<ParsedExpense> {
        return try {
            mlKitOcrService.recognizeText(bitmap).fold(
                onSuccess = { rawText -> geminiApiService.parseExpense(rawText) },
                onFailure = { exception -> Result.failure(exception) }
            )
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}



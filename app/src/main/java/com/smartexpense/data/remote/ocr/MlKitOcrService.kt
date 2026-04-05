package com.smartexpense.data.remote.ocr

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * On-device OCR service backed by ML Kit text recognition.
 */
@Singleton
class MlKitOcrService {

    suspend fun recognizeText(bitmap: Bitmap): Result<String> = suspendCoroutine { continuation ->
        try {
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val image = InputImage.fromBitmap(bitmap, 0)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    continuation.resume(Result.success(visionText.text))
                }
                .addOnFailureListener { exception ->
                    continuation.resume(Result.failure(exception))
                }
        } catch (exception: Exception) {
            continuation.resume(Result.failure(exception))
        }
    }
}



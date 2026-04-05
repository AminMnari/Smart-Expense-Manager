package com.smartexpense.data.remote.gemini

/**
 * Gemini generate-content request payload.
 */
data class GeminiRequest(
    val contents: List<GeminiContent>
)

/**
 * Gemini request content wrapper.
 */
data class GeminiContent(
    val parts: List<GeminiPart>
)

/**
 * Gemini request text part.
 */
data class GeminiPart(
    val text: String
)


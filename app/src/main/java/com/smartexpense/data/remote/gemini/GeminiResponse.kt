package com.smartexpense.data.remote.gemini

/**
 * Gemini generate-content response payload.
 */
data class GeminiResponse(
    val candidates: List<GeminiCandidate> = emptyList()
)

/**
 * Gemini response candidate.
 */
data class GeminiCandidate(
    val content: GeminiResponseContent? = null
)

/**
 * Gemini response content wrapper.
 */
data class GeminiResponseContent(
    val parts: List<GeminiResponsePart> = emptyList()
)

/**
 * Gemini response text part.
 */
data class GeminiResponsePart(
    val text: String? = null
)


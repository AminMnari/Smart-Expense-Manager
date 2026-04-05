package com.smartexpense.domain.model

import java.time.LocalDateTime
import java.time.YearMonth

/**
 * Domain model for generated monthly AI insight data.
 */
data class InsightResult(
    val month: YearMonth,
    val summary: String,
    val topCategories: List<CategorySpend>,
    val savingsTips: List<String>,
    val budgetScore: Int,
    val generatedAt: LocalDateTime
)


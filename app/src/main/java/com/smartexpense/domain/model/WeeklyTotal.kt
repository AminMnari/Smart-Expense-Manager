package com.smartexpense.domain.model

/**
 * Domain projection for grouped weekly spending totals.
 */
data class WeeklyTotal(
    val categoryId: Long,
    val week: String,
    val total: Double
)


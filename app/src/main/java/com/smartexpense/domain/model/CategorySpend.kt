package com.smartexpense.domain.model

/**
 * Domain model for category-level spend in insight output.
 */
data class CategorySpend(
    val category: String,
    val amount: Double,
    val trend: String
)


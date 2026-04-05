package com.smartexpense.domain.model

import java.time.YearMonth

/**
 * Domain model for a monthly category budget.
 */
data class Budget(
    val id: Long = 0,
    val categoryId: Long,
    val monthlyLimit: Double,
    val alertThresholdPercent: Int = 150,
    val month: YearMonth
)


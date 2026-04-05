package com.smartexpense.domain.model

import java.time.LocalDate

/**
 * Domain model for parsed receipt data before user confirmation.
 */
data class ParsedExpense(
    val merchantName: String?,
    val amount: Double?,
    val date: LocalDate?,
    val category: String?,
    val confidence: Float?
)


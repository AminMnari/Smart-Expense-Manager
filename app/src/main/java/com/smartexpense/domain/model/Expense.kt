package com.smartexpense.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Domain model for a single expense entry.
 */
data class Expense(
    val id: Long = 0,
    val amount: Double,
    val merchantName: String,
    val categoryId: Long,
    val date: LocalDate,
    val notes: String = "",
    val receiptImagePath: String? = null,
    val isAiCategorized: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now()
)


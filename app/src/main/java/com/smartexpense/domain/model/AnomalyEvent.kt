package com.smartexpense.domain.model

import java.time.LocalDateTime

/**
 * Domain model for a detected spending anomaly.
 */
data class AnomalyEvent(
    val id: Long = 0,
    val categoryId: Long,
    val detectedAt: LocalDateTime,
    val currentWeekAmount: Double,
    val averageWeekAmount: Double,
    val percentIncrease: Float,
    val isRead: Boolean = false
)


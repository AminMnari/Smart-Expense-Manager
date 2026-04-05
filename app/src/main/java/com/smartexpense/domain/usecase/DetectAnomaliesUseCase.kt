package com.smartexpense.domain.usecase

import com.smartexpense.domain.model.AnomalyEvent
import com.smartexpense.domain.model.Budget
import com.smartexpense.domain.repository.BudgetRepository
import com.smartexpense.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.time.YearMonth
import javax.inject.Inject

/**
 * Detects weekly spending spikes using recent budgeted category totals.
 */
class DetectAnomaliesUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val budgetRepository: BudgetRepository
) {

    suspend operator fun invoke(): List<AnomalyEvent> {
        val month = YearMonth.now()
        val budgets = budgetRepository.getBudgets(month).first()
        val events = mutableListOf<AnomalyEvent>()

        budgets.forEach { budget ->
            val weeklyTotals = expenseRepository.getWeeklyTotals(budget.categoryId, 5)
            if (weeklyTotals.size < 5) return@forEach

            val currentWeekAmount = weeklyTotals.first().total
            val previousWeeksAverage = weeklyTotals.drop(1).take(4).map { it.total }.average()
            val thresholdMultiplier = budget.alertThresholdPercent / 100.0
            val isAnomaly = currentWeekAmount > previousWeeksAverage * thresholdMultiplier ||
                (previousWeeksAverage == 0.0 && currentWeekAmount > 0.0)

            if (isAnomaly) {
                val percentIncrease = if (previousWeeksAverage == 0.0) {
                    100f
                } else {
                    (((currentWeekAmount - previousWeeksAverage) / previousWeeksAverage) * 100.0).toFloat()
                }
                events += AnomalyEvent(
                    categoryId = budget.categoryId,
                    detectedAt = LocalDateTime.now(),
                    currentWeekAmount = currentWeekAmount,
                    averageWeekAmount = previousWeeksAverage,
                    percentIncrease = percentIncrease,
                    isRead = false
                )
            }
        }

        return events
    }
}


package com.smartexpense.domain.usecase

import com.smartexpense.domain.model.InsightResult
import com.smartexpense.domain.repository.CategoryRepository
import com.smartexpense.domain.repository.ExpenseRepository
import com.smartexpense.domain.repository.InsightRepository
import kotlinx.coroutines.flow.first
import java.time.YearMonth
import javax.inject.Inject

/**
 * Generates monthly insights while honoring the cached result first.
 */
class GenerateInsightUseCase @Inject constructor(
    private val insightRepository: InsightRepository,
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository
) {

    suspend operator fun invoke(month: YearMonth): Result<InsightResult> {
        return try {
            insightRepository.getCachedInsight(month).first()?.let { cached ->
                return Result.success(cached)
            }

            val startDate = month.atDay(1)
            val endDate = month.atEndOfMonth()
            val expenses = expenseRepository.getByPeriod(startDate, endDate).first()
            if (expenses.isEmpty()) {
                return Result.failure(
                    Exception("No expenses found for ${month.month.name} ${month.year}. Add some expenses first.")
                )
            }

            val categories = categoryRepository.getAll().first()
            val categoryMap = categories.associate { category -> category.id to category.name }
            val insight = insightRepository.generateInsight(expenses, categoryMap)
            insightRepository.saveInsight(insight)
            Result.success(insight)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}


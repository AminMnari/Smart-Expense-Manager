package com.smartexpense.data.repository

import com.smartexpense.data.local.dao.ExpenseDao
import com.smartexpense.data.mapper.toDomain
import com.smartexpense.data.remote.gemini.GeminiApiService
import com.smartexpense.domain.model.CategorySpend
import com.smartexpense.domain.model.Expense
import com.smartexpense.domain.model.InsightResult
import com.smartexpense.domain.repository.InsightRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Insight repository implementation with in-memory caching.
 */
@Singleton
class InsightRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val geminiApiService: GeminiApiService
) : InsightRepository {

    private val cachedInsightFlow = MutableStateFlow<InsightResult?>(null)

    override suspend fun generateInsight(
        expenses: List<Expense>,
        categoryMap: Map<Long, String>
    ): InsightResult =
        try {
            val sourceExpenses = if (expenses.isEmpty()) {
                expenseDao.getAll().first().map { it.toDomain() }
            } else {
                expenses
            }

            geminiApiService.generateInsight(
                expenses = sourceExpenses,
                budgetSummary = buildBudgetSummary(sourceExpenses, categoryMap),
                categoryMap = categoryMap
            ).getOrElse {
                buildInsight(sourceExpenses, categoryMap)
            }
        } catch (_: Exception) {
            val sourceExpenses = if (expenses.isEmpty()) {
                expenseDao.getAll().first().map { it.toDomain() }
            } else {
                expenses
            }
            buildInsight(sourceExpenses, categoryMap)
        }

    override fun getCachedInsight(month: YearMonth): Flow<InsightResult?> =
        cachedInsightFlow.map { insight -> insight?.takeIf { it.month == month } }

    override suspend fun saveInsight(insight: InsightResult) {
        cachedInsightFlow.value = insight
    }

    private fun buildInsight(expenses: List<Expense>, categoryMap: Map<Long, String>): InsightResult {
        val month = expenses.maxOfOrNull { YearMonth.from(it.date) } ?: YearMonth.now()
        val total = expenses.sumOf { it.amount }
        val grouped = expenses.groupBy { it.categoryId }
            .map { (categoryId, list) ->
                CategorySpend(
                    category = categoryMap[categoryId] ?: "Other",
                    amount = list.sumOf { it.amount },
                    trend = when {
                        list.size > 8 -> "up"
                        list.size < 3 -> "down"
                        else -> "stable"
                    }
                )
            }
            .sortedByDescending { it.amount }

        return InsightResult(
            month = month,
            summary = "You spent %.2f across %d transactions this month.".format(total, expenses.size),
            topCategories = grouped.take(3),
            savingsTips = listOf(
                "Review recurring subscriptions and remove unused ones.",
                "Set a weekly spending cap for discretionary purchases.",
                "Move high-frequency spending into planned budget categories."
            ),
            budgetScore = when {
                expenses.isEmpty() -> 100
                total < 500 -> 90
                total < 1000 -> 75
                total < 2000 -> 60
                else -> 45
            },
            generatedAt = LocalDateTime.now()
        )
    }

    private fun buildBudgetSummary(expenses: List<Expense>, categoryMap: Map<Long, String>): String {
        val total = expenses.sumOf { it.amount }
        val byCategory = expenses.groupBy { it.categoryId }
            .map { (categoryId, items) ->
                (categoryMap[categoryId] ?: "Other") to items.sumOf { it.amount }
            }
            .toMap()
        return "Total spent: $total, categories: $byCategory"
    }
}


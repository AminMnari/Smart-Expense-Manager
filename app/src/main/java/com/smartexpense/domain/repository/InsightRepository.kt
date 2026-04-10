package com.smartexpense.domain.repository

import com.smartexpense.domain.model.Expense
import com.smartexpense.domain.model.InsightResult
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

/**
 * Contract for AI insight generation and caching.
 */
interface InsightRepository {
    suspend fun generateInsight(
        expenses: List<Expense>,
        categoryMap: Map<Long, String>
    ): InsightResult
    fun getCachedInsight(month: YearMonth): Flow<InsightResult?>
    suspend fun saveInsight(insight: InsightResult)
}


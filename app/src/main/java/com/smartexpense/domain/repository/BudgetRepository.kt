package com.smartexpense.domain.repository

import com.smartexpense.domain.model.Budget
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

/**
 * Contract for monthly budget operations.
 */
interface BudgetRepository {
    fun getBudgets(month: YearMonth): Flow<List<Budget>>
    suspend fun upsertBudget(budget: Budget)
    suspend fun deleteBudget(categoryId: Long)
}


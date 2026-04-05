package com.smartexpense.data.repository

import com.smartexpense.data.local.dao.BudgetDao
import com.smartexpense.data.mapper.toDomain
import com.smartexpense.data.mapper.toEntity
import com.smartexpense.domain.model.Budget
import com.smartexpense.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stub implementation of budget repository.
 */
@Singleton
class BudgetRepositoryImpl @Inject constructor(
    private val budgetDao: BudgetDao
) : BudgetRepository {

    override fun getBudgets(month: YearMonth): Flow<List<Budget>> =
        budgetDao.getByMonth(month).map { budgets ->
            budgets.map { it.toDomain() }
        }

    override suspend fun upsertBudget(budget: Budget) {
        budgetDao.upsert(budget.toEntity())
    }

    override suspend fun deleteBudget(categoryId: Long) {
        budgetDao.deleteByCategory(categoryId)
    }
}


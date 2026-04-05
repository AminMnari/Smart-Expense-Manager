package com.smartexpense.data.repository

import com.smartexpense.data.local.dao.ExpenseDao
import com.smartexpense.data.mapper.toDomain
import com.smartexpense.data.mapper.toEntity
import com.smartexpense.domain.model.Expense
import com.smartexpense.domain.model.WeeklyTotal
import com.smartexpense.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stub implementation of expense repository.
 */
@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao
) : ExpenseRepository {

    override fun getAll(): Flow<List<Expense>> = expenseDao.getAll().map { expenses ->
        expenses.map { it.toDomain() }
    }

    override fun getByPeriod(start: LocalDate, end: LocalDate): Flow<List<Expense>> =
        expenseDao.getByPeriod(start, end).map { expenses ->
            expenses.map { it.toDomain() }
        }

    override fun getByCategory(categoryId: Long): Flow<List<Expense>> =
        expenseDao.getByCategory(categoryId).map { expenses ->
            expenses.map { it.toDomain() }
        }

    override suspend fun insert(expense: Expense): Long = expenseDao.insert(expense.toEntity())

    override suspend fun update(expense: Expense) {
        expenseDao.update(expense.toEntity())
    }

    override suspend fun delete(expense: Expense) {
        expenseDao.delete(expense.toEntity())
    }

    override suspend fun getWeeklyTotals(categoryId: Long, weeks: Int): List<WeeklyTotal> =
        expenseDao.getWeeklyTotals(categoryId, weeks)
}


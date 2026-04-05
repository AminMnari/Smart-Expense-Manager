package com.smartexpense.domain.repository

import com.smartexpense.domain.model.Expense
import com.smartexpense.domain.model.WeeklyTotal
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Contract for expense data operations.
 */
interface ExpenseRepository {
    fun getAll(): Flow<List<Expense>>
    fun getByPeriod(start: LocalDate, end: LocalDate): Flow<List<Expense>>
    fun getByCategory(categoryId: Long): Flow<List<Expense>>
    suspend fun insert(expense: Expense): Long
    suspend fun update(expense: Expense)
    suspend fun delete(expense: Expense)
    suspend fun getWeeklyTotals(categoryId: Long, weeks: Int): List<WeeklyTotal>
}


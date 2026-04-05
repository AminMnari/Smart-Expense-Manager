package com.smartexpense.domain.usecase

import com.smartexpense.domain.model.Expense
import com.smartexpense.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

/**
 * Provides expense streams for different filters.
 */
class GetExpensesUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {

    operator fun invoke(): Flow<List<Expense>> = expenseRepository.getAll()

    operator fun invoke(start: LocalDate, end: LocalDate): Flow<List<Expense>> =
        expenseRepository.getByPeriod(start, end)

    operator fun invoke(categoryId: Long): Flow<List<Expense>> =
        expenseRepository.getByCategory(categoryId)
}


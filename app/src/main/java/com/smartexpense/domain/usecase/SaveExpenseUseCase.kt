package com.smartexpense.domain.usecase

import com.smartexpense.domain.model.Expense
import com.smartexpense.domain.repository.ExpenseRepository
import javax.inject.Inject

/**
 * Persists expense changes through the repository layer.
 */
class SaveExpenseUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {

    suspend operator fun invoke(expense: Expense): Result<Long> = runCatching {
        expenseRepository.insert(expense)
    }

    suspend fun update(expense: Expense): Result<Unit> = runCatching {
        expenseRepository.update(expense)
    }

    suspend fun delete(expense: Expense): Result<Unit> = runCatching {
        expenseRepository.delete(expense)
    }
}


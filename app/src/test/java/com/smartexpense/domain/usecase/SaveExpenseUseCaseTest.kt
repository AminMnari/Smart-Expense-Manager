package com.smartexpense.domain.usecase

import com.smartexpense.domain.model.Expense
import com.smartexpense.domain.repository.ExpenseRepository
import io.mockk.coEvery
import io.mockk.mockk
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for save expense operations.
 */
class SaveExpenseUseCaseTest {

    private lateinit var expenseRepository: ExpenseRepository
    private lateinit var useCase: SaveExpenseUseCase

    @Before
    fun setUp() {
        expenseRepository = mockk()
        useCase = SaveExpenseUseCase(expenseRepository)
    }

    @Test
    fun returnsSuccessWithNewIdOnSuccessfulInsert() = runTest {
        val expense = testExpense()
        coEvery { expenseRepository.insert(expense) } returns 42L

        val result = useCase(expense)

        assertTrue(result.isSuccess)
        assertEquals(42L, result.getOrNull())
    }

    @Test
    fun returnsFailureWhenRepositoryThrowsException() = runTest {
        val expense = testExpense()
        coEvery { expenseRepository.insert(expense) } throws IllegalStateException("Insert failed")

        val result = useCase(expense)

        assertTrue(result.isFailure)
        assertEquals("Insert failed", result.exceptionOrNull()?.message)
    }

    private fun testExpense(): Expense = Expense(
        id = 1L,
        amount = 10.0,
        merchantName = "Shop",
        categoryId = 1L,
        date = LocalDate.of(2026, 4, 8),
        notes = "note",
        receiptImagePath = null,
        isAiCategorized = false,
        createdAt = LocalDateTime.of(2026, 4, 8, 10, 0)
    )
}


package com.smartexpense.domain.usecase

import com.smartexpense.domain.model.Budget
import com.smartexpense.domain.model.WeeklyTotal
import com.smartexpense.domain.repository.BudgetRepository
import com.smartexpense.domain.repository.ExpenseRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import java.time.YearMonth
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for anomaly detection business logic.
 */
class DetectAnomaliesUseCaseTest {

    private lateinit var expenseRepository: ExpenseRepository
    private lateinit var budgetRepository: BudgetRepository
    private lateinit var useCase: DetectAnomaliesUseCase

    @Before
    fun setUp() {
        expenseRepository = mockk()
        budgetRepository = mockk()
        useCase = DetectAnomaliesUseCase(expenseRepository, budgetRepository)
    }

    @Test
    fun returnsEmptyListWhenNoExpensesExist() = runTest {
        every { budgetRepository.getBudgets(any()) } returns flowOf(
            listOf(Budget(categoryId = 1L, monthlyLimit = 100.0, month = YearMonth.now()))
        )
        coEvery { expenseRepository.getWeeklyTotals(1L, 5) } returns emptyList()

        val result = useCase()

        assertTrue(result.isEmpty())
    }

    @Test
    fun returnsAnomalyEventWhenCurrentWeekExceedsThreshold() = runTest {
        every { budgetRepository.getBudgets(any()) } returns flowOf(
            listOf(Budget(categoryId = 1L, monthlyLimit = 500.0, alertThresholdPercent = 150, month = YearMonth.now()))
        )
        coEvery { expenseRepository.getWeeklyTotals(1L, 5) } returns listOf(
            WeeklyTotal(1L, "2026-15", 250.0),
            WeeklyTotal(1L, "2026-14", 100.0),
            WeeklyTotal(1L, "2026-13", 100.0),
            WeeklyTotal(1L, "2026-12", 100.0),
            WeeklyTotal(1L, "2026-11", 100.0)
        )

        val result = useCase()

        assertEquals(1, result.size)
        assertEquals(1L, result.first().categoryId)
    }

    @Test
    fun returnsNoEventWhenSpendingIsBelowThreshold() = runTest {
        every { budgetRepository.getBudgets(any()) } returns flowOf(
            listOf(Budget(categoryId = 1L, monthlyLimit = 500.0, alertThresholdPercent = 150, month = YearMonth.now()))
        )
        coEvery { expenseRepository.getWeeklyTotals(1L, 5) } returns listOf(
            WeeklyTotal(1L, "2026-15", 120.0),
            WeeklyTotal(1L, "2026-14", 100.0),
            WeeklyTotal(1L, "2026-13", 100.0),
            WeeklyTotal(1L, "2026-12", 100.0),
            WeeklyTotal(1L, "2026-11", 100.0)
        )

        val result = useCase()

        assertTrue(result.isEmpty())
    }

    @Test
    fun handlesMultipleCategoriesIndependently() = runTest {
        every { budgetRepository.getBudgets(any()) } returns flowOf(
            listOf(
                Budget(categoryId = 1L, monthlyLimit = 500.0, alertThresholdPercent = 150, month = YearMonth.now()),
                Budget(categoryId = 2L, monthlyLimit = 500.0, alertThresholdPercent = 150, month = YearMonth.now())
            )
        )
        coEvery { expenseRepository.getWeeklyTotals(1L, 5) } returns listOf(
            WeeklyTotal(1L, "2026-15", 210.0),
            WeeklyTotal(1L, "2026-14", 100.0),
            WeeklyTotal(1L, "2026-13", 100.0),
            WeeklyTotal(1L, "2026-12", 100.0),
            WeeklyTotal(1L, "2026-11", 100.0)
        )
        coEvery { expenseRepository.getWeeklyTotals(2L, 5) } returns listOf(
            WeeklyTotal(2L, "2026-15", 120.0),
            WeeklyTotal(2L, "2026-14", 100.0),
            WeeklyTotal(2L, "2026-13", 100.0),
            WeeklyTotal(2L, "2026-12", 100.0),
            WeeklyTotal(2L, "2026-11", 100.0)
        )

        val result = useCase()

        assertEquals(1, result.size)
        assertEquals(1L, result.first().categoryId)
    }
}



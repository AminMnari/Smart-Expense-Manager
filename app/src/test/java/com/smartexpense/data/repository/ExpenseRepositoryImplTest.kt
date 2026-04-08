package com.smartexpense.data.repository

import com.smartexpense.data.local.dao.ExpenseDao
import com.smartexpense.data.local.entity.ExpenseEntity
import com.smartexpense.data.mapper.toEntity
import com.smartexpense.domain.model.Expense
import java.time.LocalDate
import java.time.LocalDateTime
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Tests for ExpenseRepositoryImpl mapping and DAO interaction (mocked).
 */
class ExpenseRepositoryImplTest {

    private lateinit var mockDao: ExpenseDao
    private lateinit var repository: ExpenseRepositoryImpl

    @Before
    fun setUp() {
        mockDao = mockk()
        repository = ExpenseRepositoryImpl(mockDao)
    }

    @Test
    fun insertCallsDaoInsertWithCorrectEntity() = runTest {
        val expense = testExpense()
        val entity = expense.toEntity()
        
        coEvery { mockDao.insert(entity) } returns 5L
        coEvery { mockDao.getAll() } returns flowOf(listOf(entity))

        val id = repository.insert(expense)
        assertEquals(5L, id)
        
        repository.getAll().collect { expenses ->
            assertEquals(1, expenses.size)
            val first = expenses.first()
            assertEquals(expense.amount, first.amount, 0.0)
            assertEquals(expense.merchantName, first.merchantName)
            assertEquals(expense.categoryId, first.categoryId)
            assertEquals(expense.date, first.date)
            assertEquals(expense.notes, first.notes)
            assertEquals(expense.receiptImagePath, first.receiptImagePath)
            assertEquals(expense.isAiCategorized, first.isAiCategorized)
            assertEquals(expense.createdAt, first.createdAt)
        }
    }

    @Test
    fun getAllReturnsMappedDomainModelsFromDaoFlow() = runTest {
        val entity = ExpenseEntity(
            id = 1L,
            amount = 12.5,
            merchantName = "Cafe",
            categoryId = 1L,
            date = LocalDate.of(2026, 4, 2),
            notes = "Lunch",
            receiptImagePath = null,
            isAiCategorized = false,
            createdAt = LocalDateTime.of(2026, 4, 2, 12, 0)
        )

        coEvery { mockDao.getAll() } returns flowOf(listOf(entity))

        repository.getAll().collect { result ->
            assertEquals(1, result.size)
            assertEquals(entity.id, result.first().id)
            assertEquals(entity.amount, result.first().amount, 0.0)
            assertEquals(entity.merchantName, result.first().merchantName)
            assertEquals(entity.categoryId, result.first().categoryId)
        }
    }

    private fun testExpense(): Expense = Expense(
        id = 5L,
        amount = 45.0,
        merchantName = "Market",
        categoryId = 3L,
        date = LocalDate.of(2026, 4, 5),
        notes = "Groceries",
        receiptImagePath = "/tmp/r.jpg",
        isAiCategorized = true,
        createdAt = LocalDateTime.of(2026, 4, 5, 10, 0)
    )
}


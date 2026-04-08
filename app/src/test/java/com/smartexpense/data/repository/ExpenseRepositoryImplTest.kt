package com.smartexpense.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.smartexpense.data.local.dao.ExpenseDao
import com.smartexpense.data.local.db.AppDatabase
import com.smartexpense.data.local.entity.ExpenseEntity
import com.smartexpense.domain.model.Expense
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Tests for ExpenseRepositoryImpl mapping and DAO interaction.
 */
@RunWith(RobolectricTestRunner::class)
class ExpenseRepositoryImplTest {

    private lateinit var mockDao: ExpenseDao
    private lateinit var repositoryWithMock: ExpenseRepositoryImpl

    private lateinit var db: AppDatabase
    private lateinit var repositoryWithDb: ExpenseRepositoryImpl

    @Before
    fun setUp() {
        mockDao = mockk(relaxed = true)
        repositoryWithMock = ExpenseRepositoryImpl(mockDao)

        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repositoryWithDb = ExpenseRepositoryImpl(db.expenseDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertCallsDaoInsertWithCorrectEntity() = runTest {
        val expense = testExpense()
        val captured = slot<ExpenseEntity>()
        coEvery { mockDao.insert(capture(captured)) } returns 99L

        val id = repositoryWithMock.insert(expense)

        assertEquals(99L, id)
        assertEquals(expense.id, captured.captured.id)
        assertEquals(expense.amount, captured.captured.amount, 0.0)
        assertEquals(expense.merchantName, captured.captured.merchantName)
        assertEquals(expense.categoryId, captured.captured.categoryId)
        assertEquals(expense.date, captured.captured.date)
        assertEquals(expense.notes, captured.captured.notes)
        assertEquals(expense.receiptImagePath, captured.captured.receiptImagePath)
        assertEquals(expense.isAiCategorized, captured.captured.isAiCategorized)
        assertEquals(expense.createdAt, captured.captured.createdAt)
        coVerify(exactly = 1) { mockDao.insert(any()) }
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

        db.expenseDao().insert(entity)

        val result = repositoryWithDb.getAll().first()

        assertEquals(1, result.size)
        assertEquals(entity.id, result.first().id)
        assertEquals(entity.amount, result.first().amount, 0.0)
        assertEquals(entity.merchantName, result.first().merchantName)
        assertEquals(entity.categoryId, result.first().categoryId)
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


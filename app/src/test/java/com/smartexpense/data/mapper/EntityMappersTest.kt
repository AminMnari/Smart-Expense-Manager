package com.smartexpense.data.mapper

import com.smartexpense.data.local.entity.ExpenseEntity
import java.time.LocalDate
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests for entity-domain mapper conversions.
 */
class EntityMappersTest {

    @Test
    fun expenseEntityToDomainMapsAllFieldsCorrectly() {
        val entity = testEntity()

        val domain = entity.toDomain()

        assertEquals(entity.id, domain.id)
        assertEquals(entity.amount, domain.amount, 0.0)
        assertEquals(entity.merchantName, domain.merchantName)
        assertEquals(entity.categoryId, domain.categoryId)
        assertEquals(entity.date, domain.date)
        assertEquals(entity.notes, domain.notes)
        assertEquals(entity.receiptImagePath, domain.receiptImagePath)
        assertEquals(entity.isAiCategorized, domain.isAiCategorized)
        assertEquals(entity.createdAt, domain.createdAt)
    }

    @Test
    fun expenseToEntityMapsAllFieldsCorrectly() {
        val domain = testEntity().toDomain()

        val entity = domain.toEntity()

        assertEquals(domain.id, entity.id)
        assertEquals(domain.amount, entity.amount, 0.0)
        assertEquals(domain.merchantName, entity.merchantName)
        assertEquals(domain.categoryId, entity.categoryId)
        assertEquals(domain.date, entity.date)
        assertEquals(domain.notes, entity.notes)
        assertEquals(domain.receiptImagePath, entity.receiptImagePath)
        assertEquals(domain.isAiCategorized, entity.isAiCategorized)
        assertEquals(domain.createdAt, entity.createdAt)
    }

    @Test
    fun roundTripEntityToDomainToEntityProducesEqualEntity() {
        val entity = testEntity()

        val roundTrip = entity.toDomain().toEntity()

        assertEquals(entity, roundTrip)
    }

    private fun testEntity(): ExpenseEntity = ExpenseEntity(
        id = 7L,
        amount = 55.5,
        merchantName = "Store",
        categoryId = 2L,
        date = LocalDate.of(2026, 4, 1),
        notes = "memo",
        receiptImagePath = "/tmp/receipt.jpg",
        isAiCategorized = true,
        createdAt = LocalDateTime.of(2026, 4, 1, 8, 30)
    )
}


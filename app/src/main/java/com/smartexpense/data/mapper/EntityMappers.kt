@file:Suppress("unused")

package com.smartexpense.data.mapper

import com.smartexpense.data.local.entity.AnomalyEventEntity
import com.smartexpense.data.local.entity.BudgetEntity
import com.smartexpense.data.local.entity.CategoryEntity
import com.smartexpense.data.local.entity.ExpenseEntity
import com.smartexpense.domain.model.AnomalyEvent
import com.smartexpense.domain.model.Budget
import com.smartexpense.domain.model.Category
import com.smartexpense.domain.model.Expense

/**
 * Mapper extensions between Room entities and domain models.
 */
fun ExpenseEntity.toDomain(): Expense = Expense(
    id = id,
    amount = amount,
    merchantName = merchantName,
    categoryId = categoryId,
    date = date,
    notes = notes,
    receiptImagePath = receiptImagePath,
    isAiCategorized = isAiCategorized,
    createdAt = createdAt
)

/**
 * Maps an expense domain model to its Room entity representation.
 */
fun Expense.toEntity(): ExpenseEntity = ExpenseEntity(
    id = id,
    amount = amount,
    merchantName = merchantName,
    categoryId = categoryId,
    date = date,
    notes = notes,
    receiptImagePath = receiptImagePath,
    isAiCategorized = isAiCategorized,
    createdAt = createdAt
)

/**
 * Maps a category entity to the domain model.
 */
fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name,
    iconRes = iconRes,
    colorHex = colorHex
)

/**
 * Maps a category domain model to its Room entity representation.
 */
fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    iconRes = iconRes,
    colorHex = colorHex
)

/**
 * Maps a budget entity to the domain model.
 */
fun BudgetEntity.toDomain(): Budget = Budget(
    id = id,
    categoryId = categoryId,
    monthlyLimit = monthlyLimit,
    alertThresholdPercent = alertThresholdPercent,
    month = month
)

/**
 * Maps a budget domain model to its Room entity representation.
 */
fun Budget.toEntity(): BudgetEntity = BudgetEntity(
    id = id,
    categoryId = categoryId,
    monthlyLimit = monthlyLimit,
    alertThresholdPercent = alertThresholdPercent,
    month = month
)

/**
 * Maps an anomaly event entity to the domain model.
 */
fun AnomalyEventEntity.toDomain(): AnomalyEvent = AnomalyEvent(
    id = id,
    categoryId = categoryId,
    detectedAt = detectedAt,
    currentWeekAmount = currentWeekAmount,
    averageWeekAmount = averageWeekAmount,
    percentIncrease = percentIncrease,
    isRead = isRead
)

/**
 * Maps an anomaly event domain model to its Room entity representation.
 */
fun AnomalyEvent.toEntity(): AnomalyEventEntity = AnomalyEventEntity(
    id = id,
    categoryId = categoryId,
    detectedAt = detectedAt,
    currentWeekAmount = currentWeekAmount,
    averageWeekAmount = averageWeekAmount,
    percentIncrease = percentIncrease,
    isRead = isRead
)


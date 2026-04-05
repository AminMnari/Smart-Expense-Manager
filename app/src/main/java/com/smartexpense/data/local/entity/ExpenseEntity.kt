package com.smartexpense.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Room entity for storing expense records.
 */
@Entity(
    tableName = "expenses",
    indices = [Index(value = ["category_id"])],
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"]
        )
    ]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "amount")
    val amount: Double,
    @ColumnInfo(name = "merchant_name")
    val merchantName: String,
    @ColumnInfo(name = "category_id")
    val categoryId: Long,
    @ColumnInfo(name = "date")
    val date: LocalDate,
    @ColumnInfo(name = "notes")
    val notes: String = "",
    @ColumnInfo(name = "receipt_image_path")
    val receiptImagePath: String? = null,
    @ColumnInfo(name = "is_ai_categorized")
    val isAiCategorized: Boolean = false,
    @ColumnInfo(name = "created_at")
    val createdAt: LocalDateTime
)


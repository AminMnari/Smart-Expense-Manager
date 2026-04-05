package com.smartexpense.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.YearMonth

/**
 * Room entity for per-category monthly budgets.
 */
@Entity(
    tableName = "budgets",
    indices = [Index(value = ["category_id"])],
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"]
        )
    ]
)
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "category_id")
    val categoryId: Long,
    @ColumnInfo(name = "monthly_limit")
    val monthlyLimit: Double,
    @ColumnInfo(name = "alert_threshold_percent")
    val alertThresholdPercent: Int = 150,
    @ColumnInfo(name = "month")
    val month: YearMonth
)


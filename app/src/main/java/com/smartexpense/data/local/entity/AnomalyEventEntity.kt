package com.smartexpense.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Room entity for anomaly detection events.
 */
@Entity(
    tableName = "anomaly_events",
    indices = [Index(value = ["category_id"])],
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"]
        )
    ]
)
data class AnomalyEventEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "category_id")
    val categoryId: Long,
    @ColumnInfo(name = "detected_at")
    val detectedAt: LocalDateTime,
    @ColumnInfo(name = "current_week_amount")
    val currentWeekAmount: Double,
    @ColumnInfo(name = "average_week_amount")
    val averageWeekAmount: Double,
    @ColumnInfo(name = "percent_increase")
    val percentIncrease: Float,
    @ColumnInfo(name = "is_read")
    val isRead: Boolean = false
)


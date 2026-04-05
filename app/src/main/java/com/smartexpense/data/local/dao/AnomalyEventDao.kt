package com.smartexpense.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.smartexpense.data.local.entity.AnomalyEventEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data access API for anomaly events.
 */
@Dao
interface AnomalyEventDao {

    @Insert
    suspend fun insert(event: AnomalyEventEntity): Long

    @Query("SELECT * FROM anomaly_events ORDER BY detected_at DESC")
    fun getAll(): Flow<List<AnomalyEventEntity>>

    @Query("SELECT * FROM anomaly_events WHERE is_read = 0 ORDER BY detected_at DESC")
    fun getUnread(): Flow<List<AnomalyEventEntity>>

    @Query("UPDATE anomaly_events SET is_read = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)
}


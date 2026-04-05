package com.smartexpense.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.smartexpense.data.local.entity.ExpenseEntity
import com.smartexpense.domain.model.WeeklyTotal
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Data access API for expense records.
 */
@Dao
interface ExpenseDao {

    @Query("SELECT * FROM expenses ORDER BY date DESC, created_at DESC")
    fun getAll(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE date BETWEEN :start AND :end ORDER BY date DESC, created_at DESC")
    fun getByPeriod(start: LocalDate, end: LocalDate): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE category_id = :categoryId ORDER BY date DESC, created_at DESC")
    fun getByCategory(categoryId: Long): Flow<List<ExpenseEntity>>

    @Query(
        """
        SELECT 
            category_id AS categoryId,
            strftime('%Y-%W', date) AS week,
            SUM(amount) AS total
        FROM expenses
        WHERE category_id = :categoryId
        GROUP BY category_id, week
        ORDER BY week DESC
        LIMIT :weeks
        """
    )
    suspend fun getWeeklyTotals(categoryId: Long, weeks: Int): List<WeeklyTotal>

    @Insert
    suspend fun insert(expense: ExpenseEntity): Long

    @Update
    suspend fun update(expense: ExpenseEntity)

    @Delete
    suspend fun delete(expense: ExpenseEntity)
}


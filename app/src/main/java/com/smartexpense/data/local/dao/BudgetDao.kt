package com.smartexpense.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.smartexpense.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

/**
 * Data access API for monthly budgets.
 */
@Dao
interface BudgetDao {

    @Query("SELECT * FROM budgets WHERE month = :month ORDER BY category_id ASC")
    fun getByMonth(month: YearMonth): Flow<List<BudgetEntity>>

    @Upsert
    suspend fun upsert(budget: BudgetEntity)

    @Query("DELETE FROM budgets WHERE category_id = :categoryId")
    suspend fun deleteByCategory(categoryId: Long)
}


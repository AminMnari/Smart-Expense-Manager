package com.smartexpense.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.smartexpense.data.local.dao.AnomalyEventDao
import com.smartexpense.data.local.dao.BudgetDao
import com.smartexpense.data.local.dao.CategoryDao
import com.smartexpense.data.local.dao.ExpenseDao
import com.smartexpense.data.local.entity.AnomalyEventEntity
import com.smartexpense.data.local.entity.BudgetEntity
import com.smartexpense.data.local.entity.CategoryEntity
import com.smartexpense.data.local.entity.ExpenseEntity

/**
 * Main Room database for Smart Expense Manager.
 */
@Database(
    entities = [
        ExpenseEntity::class,
        CategoryEntity::class,
        BudgetEntity::class,
        AnomalyEventEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao

    abstract fun categoryDao(): CategoryDao

    abstract fun budgetDao(): BudgetDao

    abstract fun anomalyEventDao(): AnomalyEventDao
}


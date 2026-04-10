package com.smartexpense.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabase.Callback
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.smartexpense.data.local.dao.AnomalyEventDao
import com.smartexpense.data.local.dao.BudgetDao
import com.smartexpense.data.local.dao.CategoryDao
import com.smartexpense.data.local.dao.ExpenseDao
import com.smartexpense.data.local.entity.AnomalyEventEntity
import com.smartexpense.data.local.entity.BudgetEntity
import com.smartexpense.data.local.entity.CategoryEntity
import com.smartexpense.data.local.entity.ExpenseEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

    companion object {

        fun categorySeedCallback(): Callback = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    listOf(
                        "Food & Dining" to "#FF5722",
                        "Transport" to "#2196F3",
                        "Shopping" to "#9C27B0",
                        "Health" to "#4CAF50",
                        "Utilities" to "#FF9800",
                        "Entertainment" to "#E91E63",
                        "Education" to "#00BCD4",
                        "Other" to "#607D8B"
                    ).forEach { (name, colorHex) ->
                        db.execSQL(
                            "INSERT INTO categories (name, icon_res, color_hex) VALUES ('${name.replace("'", "''")}', ${android.R.drawable.ic_menu_edit}, '$colorHex')"
                        )
                    }
                }
            }
        }
    }
}


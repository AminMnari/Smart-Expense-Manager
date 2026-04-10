package com.smartexpense.di

import android.content.Context
import androidx.room.Room
import com.smartexpense.BuildConfig
import com.smartexpense.data.local.dao.AnomalyEventDao
import com.smartexpense.data.local.dao.BudgetDao
import com.smartexpense.data.local.dao.CategoryDao
import com.smartexpense.data.local.dao.ExpenseDao
import com.smartexpense.data.local.db.AppDatabase
import com.smartexpense.data.remote.gemini.GeminiApiService
import com.smartexpense.data.remote.ocr.MlKitOcrService
import com.smartexpense.data.repository.BudgetRepositoryImpl
import com.smartexpense.data.repository.CategoryRepositoryImpl
import com.smartexpense.data.repository.ExpenseRepositoryImpl
import com.smartexpense.data.repository.InsightRepositoryImpl
import com.smartexpense.domain.repository.BudgetRepository
import com.smartexpense.domain.repository.CategoryRepository
import com.smartexpense.domain.repository.ExpenseRepository
import com.smartexpense.domain.repository.InsightRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Named
import javax.inject.Singleton

/**
 * Provides Room database, DAOs, and singleton services.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "smart_expense_manager.db"
        )
            .addCallback(AppDatabase.categorySeedCallback())
            .build()
    }


    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
        }
        return builder.build()
    }

    @Provides
    @Named("GeminiApiKey")
    fun provideGeminiApiKey(): String = BuildConfig.GEMINI_API_KEY

    @Provides
    @Singleton
    fun provideMlKitOcrService(): MlKitOcrService = MlKitOcrService()

    @Provides
    @Singleton
    fun provideGeminiApiService(
        okHttpClient: OkHttpClient,
        @Named("GeminiApiKey") apiKey: String
    ): GeminiApiService = GeminiApiService(okHttpClient, apiKey)

    @Provides
    fun provideExpenseDao(database: AppDatabase): ExpenseDao = database.expenseDao()

    @Provides
    fun provideCategoryDao(database: AppDatabase): CategoryDao = database.categoryDao()

    @Provides
    fun provideBudgetDao(database: AppDatabase): BudgetDao = database.budgetDao()

    @Provides
    fun provideAnomalyEventDao(database: AppDatabase): AnomalyEventDao = database.anomalyEventDao()
}

/**
 * Binds repository implementations to domain contracts.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindExpenseRepository(impl: ExpenseRepositoryImpl): ExpenseRepository

    @Binds
    @Singleton
    abstract fun bindBudgetRepository(impl: BudgetRepositoryImpl): BudgetRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindInsightRepository(impl: InsightRepositoryImpl): InsightRepository
}


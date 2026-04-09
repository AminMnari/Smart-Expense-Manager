package com.smartexpense.domain.repository

import com.smartexpense.domain.model.Category
import kotlinx.coroutines.flow.Flow

/**
 * Contract for loading expense categories.
 */
interface CategoryRepository {
    fun getAll(): Flow<List<Category>>
}


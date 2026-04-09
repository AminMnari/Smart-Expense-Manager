package com.smartexpense.data.repository

import com.smartexpense.data.local.dao.CategoryDao
import com.smartexpense.data.mapper.toDomain
import com.smartexpense.domain.model.Category
import com.smartexpense.domain.repository.CategoryRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Room-backed implementation of [CategoryRepository].
 */
@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override fun getAll(): Flow<List<Category>> = categoryDao.getAll().map { categories ->
        categories.map { it.toDomain() }
    }
}


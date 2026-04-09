package com.smartexpense.domain.usecase

import com.smartexpense.domain.model.Category
import com.smartexpense.domain.repository.CategoryRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * Loads all available expense categories for the UI.
 */
class GetCategoriesUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    operator fun invoke(): Flow<List<Category>> = categoryRepository.getAll()
}


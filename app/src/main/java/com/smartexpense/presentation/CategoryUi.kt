package com.smartexpense.presentation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Theaters
import androidx.compose.ui.graphics.vector.ImageVector
import com.smartexpense.R

/**
 * Static category metadata used by UI screens until category management screens are added.
 */
data class CategoryUiSpec(
    val id: Long,
    @param:StringRes val nameRes: Int,
    val icon: ImageVector
)

/**
 * Ordered category catalog aligned with Gemini category values and seeded category IDs.
 */
val CategorySpecs: List<CategoryUiSpec> = listOf(
    CategoryUiSpec(id = 1L, nameRes = R.string.category_food, icon = Icons.Default.LocalDining),
    CategoryUiSpec(id = 2L, nameRes = R.string.category_transport, icon = Icons.Default.DirectionsCar),
    CategoryUiSpec(id = 3L, nameRes = R.string.category_shopping, icon = Icons.Default.ShoppingBag),
    CategoryUiSpec(id = 4L, nameRes = R.string.category_health, icon = Icons.Default.Favorite),
    CategoryUiSpec(id = 5L, nameRes = R.string.category_utilities, icon = Icons.Default.Lightbulb),
    CategoryUiSpec(id = 6L, nameRes = R.string.category_entertainment, icon = Icons.Default.Theaters),
    CategoryUiSpec(id = 7L, nameRes = R.string.category_education, icon = Icons.Default.Book),
    CategoryUiSpec(id = 8L, nameRes = R.string.category_other, icon = Icons.Default.AutoAwesome)
)

/**
 * Finds category metadata by ID and falls back to the generic Other category.
 */
fun categorySpecById(id: Long): CategoryUiSpec = CategorySpecs.firstOrNull { it.id == id } ?: CategorySpecs.last()

/**
 * Finds category metadata by display name and falls back to the generic Other category.
 */
fun categorySpecByName(name: String): CategoryUiSpec =
    CategorySpecs.firstOrNull { it.nameResName().equals(name.trim(), ignoreCase = true) } ?: CategorySpecs.last()

private fun CategoryUiSpec.nameResName(): String = when (nameRes) {
    R.string.category_food -> "Food"
    R.string.category_transport -> "Transport"
    R.string.category_shopping -> "Shopping"
    R.string.category_health -> "Health"
    R.string.category_utilities -> "Utilities"
    R.string.category_entertainment -> "Entertainment"
    R.string.category_education -> "Education"
    else -> "Other"
}


package com.smartexpense.domain.model

/**
 * Domain model for an expense category.
 */
data class Category(
    val id: Long = 0,
    val name: String,
    val iconRes: Int,
    val colorHex: String
)


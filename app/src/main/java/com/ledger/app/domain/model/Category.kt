package com.ledger.app.domain.model

data class Category(
    val id: String,
    val name: String,
    val iconCode: String,
    val color: String,
    val type: CategoryType,
    val budget: Double? = null,
    val sortOrder: Int = 0
)

enum class CategoryType(val label: String) {
    EXPENSE("Expense"),
    INCOME("Income")
}

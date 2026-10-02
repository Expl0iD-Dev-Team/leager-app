package com.ledger.app.ui.screen.categories

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ledger.app.LedgerApplication
import com.ledger.app.domain.model.Category
import com.ledger.app.domain.model.CategoryType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

data class CategoryWithSpent(
    val category: Category,
    val spent: Double,
    val opsCount: Int
)

data class CategoriesState(
    val expenseCategories: List<CategoryWithSpent> = emptyList(),
    val incomeCategories: List<CategoryWithSpent> = emptyList(),
    val selectedTab: CategoryType = CategoryType.EXPENSE,
    val totalBudget: Double = 0.0,
    val totalSpent: Double = 0.0,
    val showDialog: Boolean = false,
    val editTarget: Category? = null
)

class CategoriesViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as LedgerApplication
    private val _state = MutableStateFlow(CategoriesState())
    val state: StateFlow<CategoriesState> = _state.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            app.categoryRepo.getAll().collect { categories ->
                val now = LocalDate.now()
                val monthStart = now.withDayOfMonth(1)
                val spentByCategory = app.transactionRepo
                    .getExpenseByCategory(monthStart, now)
                    .associate { it.categoryId to it.total }

                val allTxns = app.transactionRepo.getAll().first()
                val opsByCategory = allTxns.groupBy { it.categoryId }.mapValues { it.value.size }

                val expCats = categories.filter { it.type == CategoryType.EXPENSE }.map { cat ->
                    CategoryWithSpent(
                        category = cat,
                        spent = spentByCategory[cat.id] ?: 0.0,
                        opsCount = opsByCategory[cat.id] ?: 0
                    )
                }
                val incCats = categories.filter { it.type == CategoryType.INCOME }.map { cat ->
                    CategoryWithSpent(category = cat, spent = 0.0, opsCount = opsByCategory[cat.id] ?: 0)
                }

                _state.value = _state.value.copy(
                    expenseCategories = expCats,
                    incomeCategories = incCats,
                    totalBudget = expCats.mapNotNull { it.category.budget }.sum(),
                    totalSpent = expCats.sumOf { it.spent }
                )
            }
        }
    }

    fun selectTab(type: CategoryType) {
        _state.value = _state.value.copy(selectedTab = type)
    }

    fun openCreate() {
        _state.value = _state.value.copy(showDialog = true, editTarget = null)
    }

    fun openEdit(cat: Category) {
        _state.value = _state.value.copy(showDialog = true, editTarget = cat)
    }

    fun dismissDialog() {
        _state.value = _state.value.copy(showDialog = false, editTarget = null)
    }

    fun deleteCategory(cat: Category) {
        viewModelScope.launch {
            app.categoryRepo.delete(cat)
            dismissDialog()
        }
    }

    fun saveCategory(
        name: String,
        type: CategoryType,
        color: String,
        budget: Double?,
        iconCode: String
    ) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            val existing = _state.value.editTarget
            if (existing != null) {
                app.categoryRepo.update(existing.copy(name = trimmed, color = color, budget = budget, iconCode = iconCode))
            } else {
                val nextOrder = (_state.value.expenseCategories + _state.value.incomeCategories).size
                app.categoryRepo.save(
                    Category(
                        id = UUID.randomUUID().toString(),
                        name = trimmed,
                        iconCode = iconCode,
                        color = color,
                        type = type,
                        budget = budget,
                        sortOrder = nextOrder
                    )
                )
            }
            dismissDialog()
        }
    }
}

package com.ledger.app.ui.screen.transactions

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ledger.app.LedgerApplication
import com.ledger.app.domain.model.Category
import com.ledger.app.domain.model.Transaction
import com.ledger.app.domain.model.TransactionType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class TypeFilter(val label: String, val type: TransactionType?) {
    ALL("All", null),
    EXPENSES("Expenses", TransactionType.EXPENSE),
    INCOME("Income", TransactionType.INCOME),
    TRANSFERS("Transfers", TransactionType.TRANSFER)
}

data class TransactionsState(
    val transactions: List<Transaction> = emptyList(),
    val filteredTransactions: List<Transaction> = emptyList(),
    val categories: Map<String, Category> = emptyMap(),
    val accountNames: Map<String, String> = emptyMap(),
    val searchQuery: String = "",
    val typeFilter: TypeFilter = TypeFilter.ALL,
    val monthIncome: Double = 0.0,
    val monthExpense: Double = 0.0,
    val isLoading: Boolean = true
)

class TransactionsViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as LedgerApplication

    private val _state = MutableStateFlow(TransactionsState())
    val state: StateFlow<TransactionsState> = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                app.transactionRepo.getAll(),
                app.categoryRepo.getAll(),
                app.accountRepo.getAllAccounts()
            ) { transactions, categories, accounts ->
                Triple(transactions, categories, accounts)
            }.collect { (transactions, categories, accounts) ->
                val current = _state.value
                val categoryMap = categories.associateBy { it.id }
                val today = LocalDate.now()
                val monthStart = today.withDayOfMonth(1)
                val month = transactions.filter { !it.date.isBefore(monthStart) && !it.date.isAfter(today) }
                _state.value = current.copy(
                    transactions = transactions,
                    filteredTransactions = applyFilters(transactions, current.searchQuery, current.typeFilter, categoryMap),
                    categories = categoryMap,
                    // Archived accounts too, so old operations still show their account name
                    accountNames = accounts.associate { it.id to it.name },
                    monthIncome = month.filter { it.type == TransactionType.INCOME }.sumOf { it.amount },
                    monthExpense = month.filter { it.type == TransactionType.EXPENSE }.sumOf { kotlin.math.abs(it.amount) },
                    isLoading = false
                )
            }
        }
    }

    fun onSearch(query: String) {
        val current = _state.value
        _state.value = current.copy(
            searchQuery = query,
            filteredTransactions = applyFilters(current.transactions, query, current.typeFilter, current.categories)
        )
    }

    fun onTypeFilter(filter: TypeFilter) {
        val current = _state.value
        _state.value = current.copy(
            typeFilter = filter,
            filteredTransactions = applyFilters(current.transactions, current.searchQuery, filter, current.categories)
        )
    }

    private fun applyFilters(
        list: List<Transaction>,
        query: String,
        typeFilter: TypeFilter,
        categories: Map<String, Category>
    ): List<Transaction> {
        val byType = typeFilter.type?.let { t -> list.filter { it.type == t } } ?: list
        if (query.isBlank()) return byType
        val q = query.trim().lowercase()
        return byType.filter { tx ->
            tx.note.lowercase().contains(q) ||
                tx.tags.any { it.lowercase().contains(q) } ||
                categories[tx.categoryId]?.name?.lowercase()?.contains(q) == true ||
                "%.0f".format(java.util.Locale.US, kotlin.math.abs(tx.amount)).contains(q)
        }
    }
}

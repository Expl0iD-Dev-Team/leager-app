package com.ledger.app.ui.screen.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ledger.app.LedgerApplication
import com.ledger.app.domain.model.Account
import com.ledger.app.domain.model.AccountType
import com.ledger.app.domain.model.Category
import com.ledger.app.domain.model.CategoryType
import com.ledger.app.domain.model.Transaction
import com.ledger.app.domain.model.TransactionType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

data class HomeState(
    val accounts: List<Account> = emptyList(),
    val recentTransactions: List<Transaction> = emptyList(),
    val categories: Map<String, Category> = emptyMap(),
    val accountNames: Map<String, String> = emptyMap(),
    val netWorth: Double = 0.0,
    val netWorthCurrency: String = "RUB",
    val todayIncome: Double = 0.0,
    val todayExpense: Double = 0.0,
    val todayOpsCount: Int = 0,
    val monthIncome: Double = 0.0,
    val monthExpense: Double = 0.0,
    /** Sum of monthly budgets of expense categories; 0 when no budgets are set. */
    val monthBudget: Double = 0.0,
    val isLoading: Boolean = true,
    val showAccountDialog: Boolean = false,
    val editingAccount: Account? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as LedgerApplication

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init { loadData() }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                combine(
                    app.accountRepo.getActiveAccounts(),
                    app.transactionRepo.getAll(),
                    app.categoryRepo.getAll()
                ) { acc, txns, cats -> Triple(acc, txns, cats) },
                app.prefsManager.usdRate,
                app.prefsManager.eurRate,
                app.prefsManager.netWorthCurrency
            ) { triple, usdRate, eurRate, netWorthCurrency ->
                val accounts = triple.first
                val transactions = triple.second
                val categories = triple.third

                val catMap = categories.associateBy { it.id }
                val accMap = accounts.associateBy { it.id }

                val netWorthRub = accounts.fold(0.0) { sum, acc ->
                    if (!acc.includeInTotal) return@fold sum
                    val rate = when (acc.currency) { "USD" -> usdRate; "EUR" -> eurRate; else -> 1.0 }
                    sum + acc.balance * rate
                }
                val netWorth = when (netWorthCurrency) {
                    "USD" -> netWorthRub / usdRate
                    "EUR" -> netWorthRub / eurRate
                    else  -> netWorthRub
                }

                val today = LocalDate.now()
                val todayTxns = transactions.filter { it.date == today }
                val todayIncome = todayTxns.filter { it.amount > 0 }.sumOf { it.amount }
                val todayExpense = todayTxns.filter { it.amount < 0 }.sumOf { kotlin.math.abs(it.amount) }

                val monthStart = today.withDayOfMonth(1)
                val monthTxns = transactions.filter { !it.date.isBefore(monthStart) && !it.date.isAfter(today) }
                val monthIncome = monthTxns.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                val monthExpense = monthTxns.filter { it.type == TransactionType.EXPENSE }
                    .sumOf { kotlin.math.abs(it.amount) }
                val monthBudget = categories
                    .filter { it.type == CategoryType.EXPENSE }
                    .mapNotNull { it.budget }
                    .sum()

                _state.value.copy(
                    accounts = accounts,
                    recentTransactions = transactions.take(6),
                    categories = catMap,
                    accountNames = accMap.mapValues { it.value.name },
                    netWorth = netWorth,
                    netWorthCurrency = netWorthCurrency,
                    todayIncome = todayIncome,
                    todayExpense = todayExpense,
                    todayOpsCount = todayTxns.size,
                    monthIncome = monthIncome,
                    monthExpense = monthExpense,
                    monthBudget = monthBudget,
                    isLoading = false
                )
            }.collect { _state.value = it }
        }
    }

    fun setNetWorthCurrency(currency: String) {
        viewModelScope.launch { app.prefsManager.setNetWorthCurrency(currency) }
    }

    fun openAccountCreate() {
        _state.value = _state.value.copy(showAccountDialog = true, editingAccount = null)
    }

    fun openAccountEdit(acc: Account) {
        _state.value = _state.value.copy(showAccountDialog = true, editingAccount = acc)
    }

    fun dismissAccountDialog() {
        _state.value = _state.value.copy(showAccountDialog = false, editingAccount = null)
    }

    fun saveAccount(
        name: String,
        type: AccountType,
        currency: String,
        color: String,
        includeInTotal: Boolean,
        balance: Double
    ) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            val existing = _state.value.editingAccount
            val account = if (existing != null) {
                existing.copy(name = trimmed, type = type, currency = currency,
                    color = color, includeInTotal = includeInTotal, balance = balance)
            } else {
                Account(
                    id = UUID.randomUUID().toString(),
                    name = trimmed, type = type, currency = currency, color = color,
                    balance = balance, includeInTotal = includeInTotal,
                    sortOrder = _state.value.accounts.size
                )
            }
            if (existing != null) app.accountRepo.update(account)
            else app.accountRepo.save(account)
            dismissAccountDialog()
        }
    }

    fun archiveAccount(acc: Account) {
        viewModelScope.launch {
            app.accountRepo.archive(acc.id)
            dismissAccountDialog()
        }
    }
}

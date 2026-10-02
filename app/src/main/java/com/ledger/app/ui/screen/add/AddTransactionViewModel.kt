package com.ledger.app.ui.screen.add

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ledger.app.LedgerApplication
import com.ledger.app.domain.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

data class AddTransactionState(
    val type: TransactionType = TransactionType.EXPENSE,
    val amountText: String = "",
    val selectedCategory: Category? = null,
    val selectedAccount: Account? = null,
    val toAccount: Account? = null,
    val note: String = "",
    val date: LocalDate = LocalDate.now(),
    val time: LocalTime = LocalTime.now(),
    val recurringInterval: RecurringInterval? = null,
    val categories: List<Category> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false,
    val error: String? = null
)

class AddTransactionViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as LedgerApplication

    private val _state = MutableStateFlow(AddTransactionState())
    val state: StateFlow<AddTransactionState> = _state.asStateFlow()

    private var editingId: String? = null

    init {
        loadLookups()
    }

    private fun loadLookups() {
        viewModelScope.launch {
            combine(
                app.categoryRepo.getAll(),
                app.accountRepo.getActiveAccounts()
            ) { cats, accs -> Pair(cats, accs) }.collect { (cats, accs) ->
                val current = _state.value
                val from = current.selectedAccount ?: accs.firstOrNull()
                _state.value = current.copy(
                    categories = cats,
                    accounts = accs,
                    selectedAccount = from,
                    toAccount = resolveToAccount(current.type, from, current.toAccount, accs),
                    selectedCategory = if (current.type == TransactionType.TRANSFER) findTransferCategory(cats)
                        else current.selectedCategory ?: defaultCategoryFor(current.type, cats)
                )
            }
        }
    }

    fun loadTransaction(id: String) {
        viewModelScope.launch {
            val tx = app.transactionRepo.getById(id) ?: return@launch
            editingId = id
            val cats = app.categoryRepo.getAll().first()
            val accs = app.accountRepo.getActiveAccounts().first()
            _state.value = _state.value.copy(
                type = tx.type,
                amountText = Math.abs(tx.amount).toString(),
                selectedCategory = cats.find { it.id == tx.categoryId },
                selectedAccount = accs.find { it.id == tx.accountId },
                toAccount = tx.toAccountId?.let { id -> accs.find { it.id == id } },
                note = tx.note,
                date = tx.date,
                time = tx.time
            )
        }
    }

    fun setType(type: TransactionType) {
        val current = _state.value
        _state.value = current.copy(
            type = type,
            selectedCategory = defaultCategoryFor(type, current.categories),
            toAccount = resolveToAccount(type, current.selectedAccount, current.toAccount, current.accounts)
        )
    }

    fun setAmount(text: String) { _state.value = _state.value.copy(amountText = text) }
    fun setCategory(cat: Category) {
        if (_state.value.type == TransactionType.TRANSFER) return
        _state.value = _state.value.copy(selectedCategory = cat)
    }
    fun setAccount(acc: Account) {
        val current = _state.value
        _state.value = current.copy(
            selectedAccount = acc,
            toAccount = resolveToAccount(current.type, acc, current.toAccount, current.accounts)
        )
    }
    fun setToAccount(acc: Account) { _state.value = _state.value.copy(toAccount = acc) }
    fun setNote(note: String) { _state.value = _state.value.copy(note = note) }
    fun setDate(date: LocalDate) { _state.value = _state.value.copy(date = date) }
    fun setRecurring(interval: RecurringInterval?) { _state.value = _state.value.copy(recurringInterval = interval) }

    fun save() {
        val s = _state.value
        val amount = s.amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _state.value = s.copy(error = "Enter a valid amount")
            return
        }
        val isTransfer = s.type == TransactionType.TRANSFER
        if (!isTransfer && s.selectedCategory == null) {
            _state.value = s.copy(error = "Choose a category")
            return
        }
        val account = s.selectedAccount
        if (account == null) {
            _state.value = s.copy(error = "Choose an account")
            return
        }
        // Destination account only matters for transfers
        val toAccount = if (isTransfer) s.toAccount else null
        if (isTransfer && toAccount == null) {
            _state.value = s.copy(error = "Choose the destination account")
            return
        }
        if (isTransfer && toAccount?.id == account.id) {
            _state.value = s.copy(error = "Source and destination accounts are the same")
            return
        }

        _state.value = s.copy(isSaving = true, error = null)

        viewModelScope.launch {
            // Transfers always go to the transfer category
            val category = if (isTransfer) ensureTransferCategory() else s.selectedCategory!!

            val signedAmount = when (s.type) {
                TransactionType.EXPENSE  -> -amount
                TransactionType.INCOME   -> amount
                TransactionType.TRANSFER -> -amount
            }
            val tx = Transaction(
                id = editingId ?: UUID.randomUUID().toString(),
                amount = signedAmount,
                type = s.type,
                categoryId = category.id,
                accountId = account.id,
                toAccountId = toAccount?.id,
                note = s.note,
                date = s.date,
                time = s.time
            )

            if (editingId != null) {
                // Reverse old transaction's balance impact before applying new one
                val old = app.transactionRepo.getById(editingId!!)
                if (old != null) {
                    app.accountRepo.adjustBalance(old.accountId, -old.amount)
                    old.toAccountId?.let { toId -> app.accountRepo.adjustBalance(toId, old.amount) }
                }
            }

            app.transactionRepo.save(tx)

            // Apply new balance impact
            app.accountRepo.adjustBalance(account.id, signedAmount)
            toAccount?.let { to -> app.accountRepo.adjustBalance(to.id, amount) }

            // Schedule recurring if needed (only for new transactions)
            if (editingId == null && s.recurringInterval != null) {
                val nextDate = advanceDate(s.date, s.recurringInterval!!)
                val config = RecurringConfig(
                    id = UUID.randomUUID().toString(),
                    templateAmount = amount,
                    templateType = s.type,
                    templateCategoryId = category.id,
                    templateAccountId = account.id,
                    templateToAccountId = toAccount?.id,
                    templateNote = s.note,
                    interval = s.recurringInterval!!,
                    nextDate = nextDate
                )
                app.recurringRepo.save(config)
            }

            _state.value = _state.value.copy(isSaving = false, savedSuccessfully = true)
        }
    }

    private fun advanceDate(date: LocalDate, interval: RecurringInterval): LocalDate = when (interval) {
        RecurringInterval.DAILY   -> date.plusDays(1)
        RecurringInterval.WEEKLY  -> date.plusWeeks(1)
        RecurringInterval.MONTHLY -> date.plusMonths(1)
        RecurringInterval.YEARLY  -> date.plusYears(1)
    }

    private fun categoryTypeFor(type: TransactionType): CategoryType = when (type) {
        TransactionType.INCOME   -> CategoryType.INCOME
        else                     -> CategoryType.EXPENSE
    }

    private fun defaultCategoryFor(type: TransactionType, cats: List<Category>): Category? =
        if (type == TransactionType.TRANSFER) findTransferCategory(cats)
        else cats.firstOrNull { it.type == categoryTypeFor(type) && !isTransferCategory(it) }

    /** Keeps the destination account valid: set for transfers, different from the source account. */
    private fun resolveToAccount(
        type: TransactionType,
        from: Account?,
        current: Account?,
        accounts: List<Account>
    ): Account? {
        if (type != TransactionType.TRANSFER) return current
        if (current != null && current.id != from?.id && accounts.any { it.id == current.id }) return current
        return accounts.firstOrNull { it.id != from?.id }
    }

    private suspend fun ensureTransferCategory(): Category {
        findTransferCategory(app.categoryRepo.getAll().first())?.let { return it }
        val created = Category(
            id = TRANSFER_CATEGORY_ID,
            name = "Transfer",
            iconCode = "other",
            color = "#A0A0A0",
            type = CategoryType.EXPENSE,
            sortOrder = 99
        )
        app.categoryRepo.save(created)
        return created
    }

    companion object {
        private const val TRANSFER_CATEGORY_ID = "transfer"

        private val TRANSFER_NAMES = setOf("перевод", "transfer")

        fun isTransferCategory(cat: Category): Boolean =
            cat.id == TRANSFER_CATEGORY_ID || cat.name.trim().lowercase() in TRANSFER_NAMES

        fun findTransferCategory(cats: List<Category>): Category? =
            cats.firstOrNull { isTransferCategory(it) }
    }
}

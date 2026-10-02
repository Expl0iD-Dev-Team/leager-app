package com.ledger.app.domain.model

import java.time.LocalDate
import java.time.LocalTime

data class Transaction(
    val id: String,
    val amount: Double,
    val type: TransactionType,
    val categoryId: String,
    val accountId: String,
    val toAccountId: String? = null,
    val note: String = "",
    val date: LocalDate,
    val time: LocalTime,
    val tags: List<String> = emptyList(),
    val recurringConfigId: String? = null,
    val attachmentUri: String? = null
)

enum class TransactionType(val label: String) {
    EXPENSE("Expense"),
    INCOME("Income"),
    TRANSFER("Transfer")
}

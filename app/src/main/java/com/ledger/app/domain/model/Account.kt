package com.ledger.app.domain.model

data class Account(
    val id: String,
    val name: String,
    val currency: String,
    val balance: Double,
    val type: AccountType,
    val color: String,
    val last4: String? = null,
    val isArchived: Boolean = false,
    val sortOrder: Int = 0,
    val includeInTotal: Boolean = true
)

enum class AccountType(val label: String) {
    CARD("Card"),
    CASH("Cash"),
    DEPOSIT("Deposit"),
    SAVINGS("Savings")
}

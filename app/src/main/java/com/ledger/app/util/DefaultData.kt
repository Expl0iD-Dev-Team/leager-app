package com.ledger.app.util

import com.ledger.app.domain.model.Account
import com.ledger.app.domain.model.AccountType
import com.ledger.app.domain.model.Category
import com.ledger.app.domain.model.CategoryType

object DefaultData {

    val accounts = listOf(
        Account(id = "main",     name = "Main card",   currency = "RUB", balance = 0.0,  type = AccountType.CARD,    color = "#4C8DFF", last4 = null, sortOrder = 0),
        Account(id = "cash",     name = "Cash",   currency = "RUB", balance = 0.0,  type = AccountType.CASH,    color = "#FB923C", sortOrder = 1),
        Account(id = "savings",  name = "Savings", currency = "RUB", balance = 0.0,  type = AccountType.SAVINGS, color = "#A78BFA", sortOrder = 2)
    )

    val categories = listOf(
        Category(id = "food",      name = "Groceries",    iconCode = "food",      color = "#4ADE80", type = CategoryType.EXPENSE, budget = 25000.0, sortOrder = 0),
        Category(id = "cafe",      name = "Cafe",        iconCode = "cafe",      color = "#FB923C", type = CategoryType.EXPENSE, budget = 12000.0, sortOrder = 1),
        Category(id = "transport", name = "Transport",   iconCode = "transport", color = "#38BDF8", type = CategoryType.EXPENSE, budget = 6000.0,  sortOrder = 2),
        Category(id = "subs",      name = "Subscriptions",    iconCode = "subs",      color = "#818CF8", type = CategoryType.EXPENSE, budget = 3500.0,  sortOrder = 3),
        Category(id = "health",    name = "Health",    iconCode = "health",    color = "#F472B6", type = CategoryType.EXPENSE, budget = 8000.0,  sortOrder = 4),
        Category(id = "fun",       name = "Leisure", iconCode = "fun",       color = "#A78BFA", type = CategoryType.EXPENSE, budget = 10000.0, sortOrder = 5),
        Category(id = "home",      name = "Housing",         iconCode = "home",      color = "#FACC15", type = CategoryType.EXPENSE, budget = null,    sortOrder = 6),
        Category(id = "clothes",   name = "Clothes",      iconCode = "clothes",   color = "#E879F9", type = CategoryType.EXPENSE, budget = 6000.0,  sortOrder = 7),
        Category(id = "other_exp", name = "Other",      iconCode = "other",     color = "#9CA3AF", type = CategoryType.EXPENSE, budget = null,    sortOrder = 8),
        Category(id = "salary",    name = "Salary",    iconCode = "salary",    color = "#34D399", type = CategoryType.INCOME,  budget = null,    sortOrder = 0),
        Category(id = "freelance", name = "Freelance",     iconCode = "freelance", color = "#22D3EE", type = CategoryType.INCOME,  budget = null,    sortOrder = 1),
        Category(id = "gift",      name = "Gifts",     iconCode = "gift",      color = "#FBBF24", type = CategoryType.INCOME,  budget = null,    sortOrder = 2),
        Category(id = "other_inc", name = "Other",      iconCode = "other",     color = "#9CA3AF", type = CategoryType.INCOME,  budget = null,    sortOrder = 3)
    )
}

package com.ledger.app.domain.model

import java.time.LocalDate

data class RecurringConfig(
    val id: String,
    val templateAmount: Double,
    val templateType: TransactionType,
    val templateCategoryId: String,
    val templateAccountId: String,
    val templateToAccountId: String? = null,
    val templateNote: String,
    val interval: RecurringInterval,
    val intervalCount: Int = 1,
    val nextDate: LocalDate,
    val endDate: LocalDate? = null,
    val isActive: Boolean = true
)

enum class RecurringInterval(val label: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly")
}

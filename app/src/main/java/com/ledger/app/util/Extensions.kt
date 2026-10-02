package com.ledger.app.util

import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale

private val MONTHS_SHORT = arrayOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
)

private val MONTHS_LONG = arrayOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
)

private val WEEKDAYS_SHORT = arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

fun monthShort(month: Int): String = MONTHS_SHORT[month - 1]
fun monthLong(month: Int): String = MONTHS_LONG[month - 1]

/** "2 Oct" */
fun LocalDate.formatShort(): String = "$dayOfMonth ${MONTHS_SHORT[monthValue - 1]}"

/** "2 Oct 2026" */
fun LocalDate.formatMedium(): String = "$dayOfMonth ${MONTHS_SHORT[monthValue - 1]} $year"

/** "Fri, 2 October" */
fun LocalDate.formatFull(): String =
    "${WEEKDAYS_SHORT[dayOfWeek.value - 1]}, $dayOfMonth ${MONTHS_LONG[monthValue - 1]}"

/** "Today" / "Yesterday" / "Wed, 30 Sep" */
fun LocalDate.formatRelative(): String = when {
    isToday()     -> "Today"
    isYesterday() -> "Yesterday"
    year == LocalDate.now().year -> "${WEEKDAYS_SHORT[dayOfWeek.value - 1]}, ${formatShort()}"
    else          -> formatMedium()
}

fun LocalDate.isToday(): Boolean = this == LocalDate.now()
fun LocalDate.isYesterday(): Boolean = this == LocalDate.now().minusDays(1)

fun LocalTime.formatHm(): String = String.format(Locale.US, "%02d:%02d", hour, minute)

private fun currencySuffix(grouped: String, currency: String): String = when (currency) {
    "USD" -> "\$$grouped"
    "EUR" -> "€$grouped"
    else  -> "$grouped ₽"
}

/** "1 234 ₽" — whole units, space-grouped. */
fun Double.formatMoney(currency: String = "RUB"): String {
    val grouped = String.format(Locale("ru", "RU"), "%,.0f", Math.abs(this))
        .replace(' ', ' ')
        .replace(',', ' ')
    return currencySuffix(grouped, currency)
}

/** "1 234,50 ₽" — exact amount with kopecks. */
fun Double.formatMoneyFull(currency: String = "RUB"): String {
    val grouped = String.format(Locale("ru", "RU"), "%,.2f", Math.abs(this))
        .replace(' ', ' ')
    return currencySuffix(grouped, currency)
}

fun Double.formatSigned(currency: String = "RUB"): String {
    val prefix = if (this >= 0) "+" else "−"
    return "$prefix${Math.abs(this).formatMoney(currency)}"
}

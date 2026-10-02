package com.ledger.app.ui.screen.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ledger.app.LedgerApplication
import com.ledger.app.data.db.dao.CategoryTotal
import com.ledger.app.domain.model.Category
import com.ledger.app.ui.components.BarEntry
import com.ledger.app.ui.components.DonutSegment
import com.ledger.app.ui.components.parseHexColor
import com.ledger.app.util.formatShort
import com.ledger.app.util.monthLong
import com.ledger.app.util.monthShort
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

enum class StatsPeriod(val label: String) { DAY("Day"), WEEK("Week"), MONTH("Month"), YEAR("Year") }

data class CategoryBreakdown(val category: Category, val spent: Double, val pct: Float, val count: Int)

data class StatsState(
    val period: StatsPeriod = StatsPeriod.MONTH,
    val periodOffset: Int = 0,
    val periodLabel: String = "",
    val periodRange: String = "",
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    /** Average spending per day over the elapsed part of the period. */
    val avgPerDay: Double = 0.0,
    /** Spending change vs. the previous period, e.g. -0.084 = 8.4% less; null when there's nothing to compare. */
    val expenseChange: Double? = null,
    val previousLabel: String = "",
    val donutSegments: List<DonutSegment> = emptyList(),
    val categoryBreakdown: List<CategoryBreakdown> = emptyList(),
    val incomeDonutSegments: List<DonutSegment> = emptyList(),
    val incomeCategoryBreakdown: List<CategoryBreakdown> = emptyList(),
    val monthlyBars: List<BarEntry> = emptyList(),
    val heatmapValues: List<Float> = emptyList(),
    val isLoading: Boolean = true
)

class StatsViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as LedgerApplication
    private val _state = MutableStateFlow(StatsState())
    val state: StateFlow<StatsState> = _state.asStateFlow()

    init { load() }

    fun setPeriod(period: StatsPeriod) {
        _state.value = _state.value.copy(period = period, periodOffset = 0, isLoading = true)
        load()
    }

    fun shiftPeriod(delta: Int) {
        _state.value = _state.value.copy(periodOffset = _state.value.periodOffset + delta, isLoading = true)
        load()
    }

    private fun load() {
        viewModelScope.launch {
            val now = LocalDate.now()
            val period = _state.value.period
            val offset = _state.value.periodOffset
            val (from, to) = getPeriodRange(now, period, offset)
            val (prevFrom, prevTo) = getPeriodRange(now, period, offset - 1)

            val categories = app.categoryRepo.getAll().first()
            val catMap = categories.associateBy { it.id }

            val totalIncome  = app.transactionRepo.getTotalIncome(from, to)
            val totalExpense = app.transactionRepo.getTotalExpense(from, to)

            // Compare the elapsed part of the current period with the same span of the previous one
            val elapsedEnd = if (to.isAfter(now)) now else to
            val elapsedDays = (ChronoUnit.DAYS.between(from, elapsedEnd) + 1).coerceAtLeast(1)
            val prevSpanEnd = prevFrom.plusDays(elapsedDays - 1)
            val prevComparableEnd = if (prevSpanEnd.isAfter(prevTo)) prevTo else prevSpanEnd
            val prevExpense = app.transactionRepo.getTotalExpense(prevFrom, prevComparableEnd)
            val expenseChange = if (prevExpense > 0 && !from.isAfter(now)) (totalExpense - prevExpense) / prevExpense else null

            val (expSegments, expBreakdown) = breakdown(app.transactionRepo.getExpenseByCategory(from, to), catMap, totalExpense)
            val (incSegments, incBreakdown) = breakdown(app.transactionRepo.getIncomeByCategory(from, to), catMap, totalIncome)

            // Income vs spending, last 6 calendar months
            val monthBars = (5 downTo 0).map { monthsAgo ->
                val month = now.minusMonths(monthsAgo.toLong())
                val mStart = month.withDayOfMonth(1)
                val mEnd = month.withDayOfMonth(month.lengthOfMonth())
                val inc = app.transactionRepo.getTotalIncome(mStart, mEnd).toFloat()
                val exp = app.transactionRepo.getTotalExpense(mStart, mEnd).toFloat()
                BarEntry(income = inc, expense = exp, label = monthShort(month.monthValue))
            }

            // 30-day heatmap (always the last 30 days)
            val daily = app.transactionRepo.getDailyExpense(now.minusDays(29), now)
            val dailyMap = daily.associateBy { it.dateEpochDay }
            val maxDaily = daily.maxOfOrNull { it.total }?.coerceAtLeast(1.0) ?: 1.0
            val heatmap = (29 downTo 0).map { daysAgo ->
                val day = now.minusDays(daysAgo.toLong())
                ((dailyMap[day.toEpochDay()]?.total ?: 0.0) / maxDaily).toFloat()
            }

            _state.value = _state.value.copy(
                periodLabel = getPeriodLabel(now, period, offset),
                periodRange = getRangeLabel(from, to, period),
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                avgPerDay = totalExpense / elapsedDays,
                expenseChange = expenseChange,
                previousLabel = getPreviousLabel(now, period, offset),
                donutSegments = expSegments,
                categoryBreakdown = expBreakdown,
                incomeDonutSegments = incSegments,
                incomeCategoryBreakdown = incBreakdown,
                monthlyBars = monthBars,
                heatmapValues = heatmap,
                isLoading = false
            )
        }
    }

    private fun breakdown(
        totals: List<CategoryTotal>,
        catMap: Map<String, Category>,
        sum: Double
    ): Pair<List<DonutSegment>, List<CategoryBreakdown>> {
        val rows = totals
            .filter { it.total > 0 }
            .sortedByDescending { it.total }
            .mapNotNull { ct -> catMap[ct.categoryId]?.let { it to ct } }
        val segments = rows.map { (cat, ct) ->
            DonutSegment(value = ct.total.toFloat(), color = parseHexColor(cat.color), label = cat.name, id = cat.id)
        }
        val list = rows.map { (cat, ct) ->
            CategoryBreakdown(cat, ct.total, if (sum > 0) (ct.total / sum).toFloat() else 0f, ct.opsCount)
        }
        return segments to list
    }

    private fun getPeriodRange(now: LocalDate, period: StatsPeriod, offset: Int): Pair<LocalDate, LocalDate> = when (period) {
        StatsPeriod.DAY -> {
            val day = now.plusDays(offset.toLong())
            Pair(day, day)
        }
        StatsPeriod.WEEK -> {
            val monday = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).plusWeeks(offset.toLong())
            Pair(monday, monday.plusDays(6))
        }
        StatsPeriod.MONTH -> {
            val month = now.plusMonths(offset.toLong())
            Pair(month.withDayOfMonth(1), month.withDayOfMonth(month.lengthOfMonth()))
        }
        StatsPeriod.YEAR -> {
            val year = now.plusYears(offset.toLong())
            Pair(year.withDayOfYear(1), year.withDayOfYear(year.lengthOfYear()))
        }
    }

    private fun getPeriodLabel(now: LocalDate, period: StatsPeriod, offset: Int): String = when (period) {
        StatsPeriod.DAY -> when (offset) {
            0 -> "Today"
            -1 -> "Yesterday"
            else -> now.plusDays(offset.toLong()).let { "${it.formatShort()} ${it.year}" }
        }
        StatsPeriod.WEEK -> when (offset) {
            0 -> "This week"
            -1 -> "Last week"
            else -> {
                val monday = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).plusWeeks(offset.toLong())
                "Week of ${monday.formatShort()}"
            }
        }
        StatsPeriod.MONTH -> now.plusMonths(offset.toLong()).let { "${monthLong(it.monthValue)} ${it.year}" }
        StatsPeriod.YEAR -> now.plusYears(offset.toLong()).year.toString()
    }

    private fun getRangeLabel(from: LocalDate, to: LocalDate, period: StatsPeriod): String = when (period) {
        StatsPeriod.DAY -> "${from.formatShort()} ${from.year}"
        StatsPeriod.YEAR -> "1 Jan – 31 Dec"
        else -> "${from.formatShort()} – ${to.formatShort()}"
    }

    private fun getPreviousLabel(now: LocalDate, period: StatsPeriod, offset: Int): String = when (period) {
        StatsPeriod.DAY -> "vs previous day"
        StatsPeriod.WEEK -> "vs previous week"
        StatsPeriod.MONTH -> "vs ${monthLong(now.plusMonths((offset - 1).toLong()).monthValue)}"
        StatsPeriod.YEAR -> "vs ${now.plusYears((offset - 1).toLong()).year}"
    }
}

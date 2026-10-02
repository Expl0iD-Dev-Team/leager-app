package com.ledger.app.ui.screen.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ledger.app.ui.components.*
import com.ledger.app.ui.theme.AppFont
import com.ledger.app.ui.theme.ledger
import com.ledger.app.util.formatMoney
import com.ledger.app.util.formatMoneyFull
import java.util.Locale

@Composable
fun StatsScreen() {
    val vm: StatsViewModel = viewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    val c = MaterialTheme.ledger
    var showIncome by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        item { ScreenTitle("Statistics") }

        item {
            SegmentedControl(
                options = StatsPeriod.entries.toList(),
                selected = state.period,
                onSelect = vm::setPeriod,
                label = { it.label },
                height = 38.dp
            )
        }

        // Period navigation
        item {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                CircleIconButton(LedgerIcons.ChevronLeft, onClick = { vm.shiftPeriod(-1) }, size = 40.dp, contentDescription = "Previous period")
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.periodLabel.ifEmpty { "—" }, style = MaterialTheme.typography.titleLarge, color = c.text)
                    Text(state.periodRange, fontFamily = AppFont, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = c.muted)
                }
                CircleIconButton(LedgerIcons.ChevronRight, onClick = { vm.shiftPeriod(1) }, size = 40.dp, contentDescription = "Next period")
            }
        }

        // Donut (swipe left/right to change the period)
        item {
            var swipe by remember { mutableStateOf(0f) }
            LedgerCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragStart = { swipe = 0f },
                            onHorizontalDrag = { _, drag -> swipe += drag },
                            onDragEnd = {
                                // Page-like, matching the arrows: drag the chart to the left to bring in
                                // the next period (as ›), drag it to the right for the previous one (as ‹)
                                when {
                                    swipe < -80f -> vm.shiftPeriod(1)
                                    swipe > 80f  -> vm.shiftPeriod(-1)
                                }
                                swipe = 0f
                            },
                            onDragCancel = { swipe = 0f }
                        )
                    },
                radius = 26.dp,
                contentPadding = PaddingValues(20.dp),
                verticalSpacing = 18.dp
            ) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    SegmentedControl(
                        options = listOf(false, true),
                        selected = showIncome,
                        onSelect = { showIncome = it },
                        label = { if (it) "Income" else "Expenses" },
                        modifier = Modifier.width(220.dp),
                        height = 34.dp,
                        accentSelected = true,
                        containerColor = c.surface2,
                        bordered = false
                    )
                }
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    DonutChart(
                        segments = if (showIncome) state.incomeDonutSegments else state.donutSegments,
                        size = 200.dp,
                        thickness = 22.dp,
                        trackColor = c.surface2,
                        center = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    if (showIncome) "Earned" else "Spent",
                                    fontFamily = AppFont, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = c.muted
                                )
                                Text(
                                    (if (showIncome) state.totalIncome else state.totalExpense).formatMoney(),
                                    fontFamily = AppFont,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 24.sp,
                                    letterSpacing = (-0.5).sp,
                                    color = if (showIncome) c.income else c.text
                                )
                            }
                        }
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Kpi("Daily average", state.avgPerDay.formatMoney(), c.text, Modifier.weight(1f))
                    val change = state.expenseChange
                    Kpi(
                        state.previousLabel,
                        change?.let { formatPercent(it) } ?: "—",
                        when {
                            change == null -> c.muted
                            change <= 0.0  -> c.income
                            else           -> c.danger
                        },
                        Modifier.weight(1f)
                    )
                }
            }
        }

        // Trend
        item {
            LedgerCard(modifier = Modifier.fillMaxWidth(), radius = 26.dp, contentPadding = PaddingValues(20.dp), verticalSpacing = 16.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Income vs spending", style = MaterialTheme.typography.titleMedium, color = c.text)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        LegendDot(c.income, "In")
                        LegendDot(c.accent, "Out")
                    }
                }
                GroupedBarChart(entries = state.monthlyBars, modifier = Modifier.fillMaxWidth())
            }
        }

        // Daily activity
        item {
            LedgerCard(modifier = Modifier.fillMaxWidth(), radius = 26.dp, contentPadding = PaddingValues(20.dp), verticalSpacing = 14.dp) {
                Text("Daily spending · 30 days", style = MaterialTheme.typography.titleMedium, color = c.text)
                HeatmapGrid(values = state.heatmapValues, columns = 15, modifier = Modifier.fillMaxWidth())
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Less", fontFamily = AppFont, fontSize = 12.sp, color = c.faint)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(0.2f, 0.4f, 0.6f, 0.8f, 1f).forEach { a ->
                            Box(Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(c.accent.copy(alpha = a)))
                        }
                    }
                    Text("More", fontFamily = AppFont, fontSize = 12.sp, color = c.faint)
                }
            }
        }

        // Full category lists with exact amounts for the selected period
        categorySection(
            title = "Spending by category",
            total = state.totalExpense,
            breakdown = state.categoryBreakdown,
            isExpense = true
        )
        categorySection(
            title = "Income by category",
            total = state.totalIncome,
            breakdown = state.incomeCategoryBreakdown,
            isExpense = false
        )
    }
}

private fun formatPercent(change: Double): String {
    val sign = if (change > 0) "+" else if (change < 0) "−" else ""
    return sign + String.format(Locale("ru", "RU"), "%.1f%%", Math.abs(change) * 100)
}

@Composable
private fun Kpi(label: String, value: String, valueColor: Color, modifier: Modifier) {
    val c = MaterialTheme.ledger
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(c.surface2)
            .padding(12.dp)
    ) {
        Text(label, fontFamily = AppFont, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = c.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(value, fontFamily = AppFont, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = valueColor, maxLines = 1)
    }
}

private fun LazyListScope.categorySection(
    title: String,
    total: Double,
    breakdown: List<CategoryBreakdown>,
    isExpense: Boolean
) {
    item {
        val c = MaterialTheme.ledger
        SectionTitle(
            title = title,
            action = "${if (isExpense) "−" else "+"}${total.formatMoneyFull()}",
            actionColor = if (isExpense) c.text else c.income
        )
    }
    item {
        val c = MaterialTheme.ledger
        if (breakdown.isEmpty()) {
            LedgerCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(18.dp)) {
                Text("No operations in this period", fontFamily = AppFont, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = c.faint)
            }
        } else {
            LedgerCard(
                modifier = Modifier.fillMaxWidth(),
                radius = 24.dp,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
            ) {
                breakdown.forEach { row -> CategoryAmountRow(row, isExpense) }
            }
        }
    }
}

@Composable
private fun CategoryAmountRow(item: CategoryBreakdown, isExpense: Boolean) {
    val c = MaterialTheme.ledger
    val cat = item.category
    val color = parseHexColor(cat.color)
    val budget = if (isExpense) cat.budget else null
    val overBudget = budget != null && budget > 0 && item.spent > budget

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CategoryBubble(cat, size = 40.dp, corner = 13.dp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    cat.name,
                    fontFamily = AppFont, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = c.text,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                val sub = buildString {
                    append(if (item.count == 1) "1 operation" else "${item.count} operations")
                    if (budget != null && budget > 0) append(" · budget ${budget.formatMoney()}")
                }
                Text(sub, fontFamily = AppFont, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = c.muted)
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${if (isExpense) "−" else "+"}${item.spent.formatMoneyFull()}",
                    fontFamily = AppFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = when {
                        overBudget -> c.danger
                        isExpense  -> c.text
                        else       -> c.income
                    }
                )
                Text(
                    String.format(Locale("ru", "RU"), "%.1f%%", item.pct * 100),
                    fontFamily = AppFont, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = c.muted
                )
            }
        }
        ProgressBar(fraction = item.pct, color = color, height = 4.dp)
    }
}

package com.ledger.app.ui.screen.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ledger.app.LedgerApplication
import com.ledger.app.ui.components.*
import com.ledger.app.ui.theme.IbmPlexMonoFamily
import com.ledger.app.ui.theme.IbmPlexSansFamily
import com.ledger.app.ui.theme.ledger
import com.ledger.app.util.formatMoney
import com.ledger.app.util.formatMoneyFull
import java.util.Locale

@Composable
fun StatsScreen(
    app: LedgerApplication,
    onBackClick: () -> Unit
) {
    val vm: StatsViewModel = viewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    val c = MaterialTheme.ledger

    LazyColumn(modifier = Modifier.fillMaxSize().background(c.bg)) {

        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text("STATS", fontFamily = IbmPlexMonoFamily, fontSize = 10.sp,
                        letterSpacing = 1.2.sp, color = c.faint)
                    Text(state.periodLabel.ifEmpty { "—" }, fontFamily = IbmPlexMonoFamily,
                        fontSize = 15.sp, color = c.text, modifier = Modifier.padding(top = 6.dp))
                }
            }
        }

        // Period tabs
        item {
            Row(modifier = Modifier.fillMaxWidth().drawBehind {
                drawLine(c.border, Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx())
            }) {
                StatsPeriod.entries.forEach { period ->
                    val isSelected = state.period == period
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(if (isSelected) c.surface else androidx.compose.ui.graphics.Color.Transparent)
                            .clickable { vm.setPeriod(period) }
                            .padding(vertical = 12.dp)
                            .drawBehind {
                                if (isSelected) drawLine(c.lime,
                                    Offset(0f, size.height), Offset(size.width, size.height), 2.dp.toPx())
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(period.label, fontFamily = IbmPlexMonoFamily, fontSize = 11.sp,
                            letterSpacing = 1.4.sp, color = if (isSelected) c.text else c.muted)
                    }
                }
            }
        }

        // Expense donut + breakdown (swipe left/right to navigate periods)
        item {
            var swipeOffset by remember { mutableStateOf(0f) }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragStart = { _ -> swipeOffset = 0f },
                            onHorizontalDrag = { _, drag -> swipeOffset += drag },
                            onDragEnd = {
                                when {
                                    swipeOffset < -80f -> vm.shiftPeriod(-1)
                                    swipeOffset > 80f  -> vm.shiftPeriod(1)
                                }
                                swipeOffset = 0f
                            },
                            onDragCancel = { swipeOffset = 0f }
                        )
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 20.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("РАСХОДЫ", fontFamily = IbmPlexMonoFamily, fontSize = 10.sp,
                        letterSpacing = 1.4.sp, color = c.faint)
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("‹", fontFamily = IbmPlexMonoFamily, fontSize = 18.sp, color = c.muted,
                            modifier = Modifier.clickable { vm.shiftPeriod(-1) }.padding(horizontal = 4.dp))
                        Text("›", fontFamily = IbmPlexMonoFamily, fontSize = 18.sp, color = c.muted,
                            modifier = Modifier.clickable { vm.shiftPeriod(1) }.padding(horizontal = 4.dp))
                    }
                }
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
                    DonutChart(
                        segments = state.donutSegments,
                        size = 156.dp,
                        thickness = 22.dp,
                        trackColor = c.surface2,
                        center = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("SPENT", fontFamily = IbmPlexMonoFamily, fontSize = 9.sp,
                                    letterSpacing = 1.2.sp, color = c.faint)
                                Text(state.totalExpense.formatMoney(), fontFamily = IbmPlexMonoFamily,
                                    fontWeight = FontWeight.Medium, fontSize = 20.sp,
                                    letterSpacing = (-0.5).sp, color = c.text)
                            }
                        }
                    )
                    Spacer(Modifier.width(18.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("BREAKDOWN", fontFamily = IbmPlexMonoFamily, fontSize = 10.sp,
                            letterSpacing = 1.4.sp, color = c.faint)
                        Spacer(Modifier.height(8.dp))
                        state.categoryBreakdown.take(5).forEach { bd ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(6.dp).background(parseHexColor(bd.category.color)))
                                Spacer(Modifier.width(8.dp))
                                Text(bd.category.name, fontFamily = IbmPlexSansFamily, fontSize = 12.sp,
                                    color = c.text, modifier = Modifier.weight(1f))
                                Text("${(bd.pct * 100).toInt()}%", fontFamily = IbmPlexMonoFamily,
                                    fontSize = 11.sp, color = c.muted)
                            }
                        }
                        if (state.categoryBreakdown.size > 5) {
                            Text("+${state.categoryBreakdown.size - 5} more", fontFamily = IbmPlexMonoFamily,
                                fontSize = 11.sp, color = c.faint, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }
        }

        // Income donut + breakdown
        item {
            Column(
                modifier = Modifier.fillMaxWidth().drawBehind {
                    drawLine(c.border, Offset(0f, 0f), Offset(size.width, 0f), 1.dp.toPx())
                }
            ) {
                Text("ДОХОДЫ", fontFamily = IbmPlexMonoFamily, fontSize = 10.sp,
                    letterSpacing = 1.4.sp, color = c.faint,
                    modifier = Modifier.padding(horizontal = 20.dp).padding(top = 20.dp, bottom = 8.dp))
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
                    DonutChart(
                        segments = state.incomeDonutSegments,
                        size = 156.dp,
                        thickness = 22.dp,
                        trackColor = c.surface2,
                        center = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("EARNED", fontFamily = IbmPlexMonoFamily, fontSize = 9.sp,
                                    letterSpacing = 1.2.sp, color = c.faint)
                                Text(state.totalIncome.formatMoney(), fontFamily = IbmPlexMonoFamily,
                                    fontWeight = FontWeight.Medium, fontSize = 20.sp,
                                    letterSpacing = (-0.5).sp, color = c.lime)
                            }
                        }
                    )
                    Spacer(Modifier.width(18.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("BREAKDOWN", fontFamily = IbmPlexMonoFamily, fontSize = 10.sp,
                            letterSpacing = 1.4.sp, color = c.faint)
                        Spacer(Modifier.height(8.dp))
                        state.incomeCategoryBreakdown.take(5).forEach { bd ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(6.dp).background(parseHexColor(bd.category.color)))
                                Spacer(Modifier.width(8.dp))
                                Text(bd.category.name, fontFamily = IbmPlexSansFamily, fontSize = 12.sp,
                                    color = c.text, modifier = Modifier.weight(1f))
                                Text("${(bd.pct * 100).toInt()}%", fontFamily = IbmPlexMonoFamily,
                                    fontSize = 11.sp, color = c.muted)
                            }
                        }
                        if (state.incomeCategoryBreakdown.size > 5) {
                            Text("+${state.incomeCategoryBreakdown.size - 5} more", fontFamily = IbmPlexMonoFamily,
                                fontSize = 11.sp, color = c.faint, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }
        }

        // Monthly trend (12 months, fixed)
        item {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 20.dp)
                    .drawBehind {
                        drawLine(c.border, Offset(0f, 0f), Offset(size.width, 0f), 1.dp.toPx())
                    }
                    .padding(top = 16.dp)
            ) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("TREND · 12 MONTHS", fontFamily = IbmPlexMonoFamily, fontSize = 10.sp,
                        letterSpacing = 1.2.sp, color = c.faint)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        LegendItem("IN", c.lime)
                        LegendItem("OUT", c.red)
                    }
                }
                Spacer(Modifier.height(14.dp))
                StackedBarChart(entries = state.monthlyBars, modifier = Modifier.fillMaxWidth())
            }
        }

        // Heatmap
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text("DAILY ACTIVITY · 30D", fontFamily = IbmPlexMonoFamily, fontSize = 10.sp,
                    letterSpacing = 1.2.sp, color = c.faint,
                    modifier = Modifier.padding(bottom = 12.dp))
                HeatmapGrid(values = state.heatmapValues, columns = 15, modifier = Modifier.fillMaxWidth())
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("LESS", fontFamily = IbmPlexMonoFamily, fontSize = 9.sp, color = c.faint)
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        listOf(0.08f, 0.25f, 0.5f, 0.75f, 1f).forEach { opacity ->
                            Box(modifier = Modifier.size(10.dp).background(c.lime.copy(alpha = opacity)))
                        }
                    }
                    Text("MORE", fontFamily = IbmPlexMonoFamily, fontSize = 9.sp, color = c.faint)
                }
            }
        }

        // Full category lists with exact amounts for the selected period
        categoryListSection(
            title = "РАСХОДЫ ПО КАТЕГОРИЯМ · ${state.periodLabel}",
            total = state.totalExpense,
            breakdown = state.categoryBreakdown,
            isExpense = true
        )
        categoryListSection(
            title = "ДОХОДЫ ПО КАТЕГОРИЯМ · ${state.periodLabel}",
            total = state.totalIncome,
            breakdown = state.incomeCategoryBreakdown,
            isExpense = false
        )

        item { Spacer(Modifier.height(80.dp)) }
    }
}

private fun LazyListScope.categoryListSection(
    title: String,
    total: Double,
    breakdown: List<CategoryBreakdown>,
    isExpense: Boolean
) {
    item {
        val c = MaterialTheme.ledger
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 24.dp, bottom = 8.dp)
                .drawBehind {
                    drawLine(c.borderStrong, Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx())
                }
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(title, fontFamily = IbmPlexMonoFamily, fontSize = 10.sp, letterSpacing = 1.4.sp,
                color = c.faint, modifier = Modifier.weight(1f))
            Text("${if (isExpense) "−" else "+"}${total.formatMoneyFull()}",
                fontFamily = IbmPlexMonoFamily, fontWeight = FontWeight.Medium, fontSize = 13.sp,
                color = if (isExpense) c.text else c.lime)
        }
    }
    if (breakdown.isEmpty()) {
        item {
            val c = MaterialTheme.ledger
            Text("Нет операций за период", fontFamily = IbmPlexMonoFamily, fontSize = 11.sp,
                color = c.faint, modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp))
        }
    }
    items(breakdown, key = { "${if (isExpense) "exp" else "inc"}:${it.category.id}" }) { item ->
        CategoryAmountRow(item, isExpense)
    }
}

@Composable
private fun CategoryAmountRow(item: CategoryBreakdown, isExpense: Boolean) {
    val c = MaterialTheme.ledger
    val cat = item.category
    val budget = if (isExpense) cat.budget else null
    val budgetPct = if (budget != null && budget > 0) (item.spent / budget).toFloat() else 0f
    val over = budgetPct > 1f
    val color = parseHexColor(cat.color)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .drawBehind {
                drawLine(c.border, Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx())
            }
            .padding(bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(6.dp).height(32.dp).background(color))
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(cat.name, fontFamily = IbmPlexSansFamily, fontWeight = FontWeight.Medium,
                    fontSize = 13.sp, color = c.text, modifier = Modifier.weight(1f))
                Text("${if (isExpense) "−" else "+"}${item.spent.formatMoneyFull()}",
                    fontFamily = IbmPlexMonoFamily, fontSize = 12.sp,
                    color = when {
                        over -> c.red
                        isExpense -> c.text
                        else -> c.lime
                    })
            }
            Text(
                String.format(Locale("ru", "RU"), "%.1f%% от суммы за период", item.pct * 100),
                fontFamily = IbmPlexMonoFamily, fontSize = 9.sp, letterSpacing = 0.6.sp,
                color = c.faint, modifier = Modifier.padding(top = 2.dp)
            )
            if (budget != null) {
                Box(modifier = Modifier
                    .padding(top = 6.dp)
                    .fillMaxWidth().height(2.dp)
                    .background(c.surface2)
                ) {
                    Box(modifier = Modifier
                        .fillMaxWidth(budgetPct.coerceIn(0f, 1f)).fillMaxHeight()
                        .background(if (over) c.red else color))
                }
                Text("${(budgetPct * 100).toInt()}% of ${budget.formatMoney()}",
                    fontFamily = IbmPlexMonoFamily, fontSize = 9.sp,
                    letterSpacing = 0.6.sp, color = c.faint,
                    modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

@Composable
private fun LegendItem(label: String, color: androidx.compose.ui.graphics.Color) {
    val c = MaterialTheme.ledger
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Box(modifier = Modifier.size(8.dp).background(color))
        Text(label, fontFamily = IbmPlexMonoFamily, fontSize = 9.sp, color = c.muted, letterSpacing = 0.8.sp)
    }
}

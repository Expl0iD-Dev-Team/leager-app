package com.ledger.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ledger.app.ui.theme.AppFont
import com.ledger.app.ui.theme.ledger

data class BarEntry(val income: Float, val expense: Float, val label: String)

/** Income vs. spending, one pair of rounded bars per period. The last period is highlighted. */
@Composable
fun GroupedBarChart(
    entries: List<BarEntry>,
    modifier: Modifier = Modifier,
    chartHeight: Float = 130f
) {
    val c = MaterialTheme.ledger
    if (entries.isEmpty()) return
    val maxVal = entries.maxOf { maxOf(it.income, it.expense) }.coerceAtLeast(1f)
    val incomeColor = c.income
    val expenseColor = c.accent

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight.dp)
        ) {
            val slot = size.width / entries.size
            val barW = 12.dp.toPx()
            val gap = 4.dp.toPx()
            val radius = CornerRadius(5.dp.toPx(), 5.dp.toPx())

            entries.forEachIndexed { i, entry ->
                val alpha = if (i == entries.lastIndex) 1f else 0.45f
                val center = slot * i + slot / 2f
                listOf(
                    Triple(entry.income, incomeColor, center - gap / 2f - barW),
                    Triple(entry.expense, expenseColor, center + gap / 2f)
                ).forEach { (value, color, x) ->
                    val h = (value / maxVal) * size.height
                    if (h > 0f) {
                        drawRoundRect(
                            color = color.copy(alpha = alpha),
                            topLeft = Offset(x, size.height - h),
                            size = Size(barW, h),
                            cornerRadius = radius
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            entries.forEachIndexed { i, entry ->
                val isLast = i == entries.lastIndex
                Text(
                    text = entry.label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontFamily = AppFont,
                    fontWeight = if (isLast) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 12.sp,
                    color = if (isLast) c.text else c.muted
                )
            }
        }
    }
}

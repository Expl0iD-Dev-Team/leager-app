package com.ledger.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ledger.app.ui.theme.ledger

/** Daily spending intensity; values are normalised to 0..1. */
@Composable
fun HeatmapGrid(
    values: List<Float>,
    columns: Int = 15,
    modifier: Modifier = Modifier
) {
    val c = MaterialTheme.ledger
    val rows = (values.size + columns - 1) / columns

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(rows) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(columns) { col ->
                    val idx = row * columns + col
                    val v = if (idx < values.size) values[idx] else 0f
                    val alpha = when {
                        v <= 0f  -> 0f
                        v < 0.2f -> 0.2f
                        v < 0.4f -> 0.4f
                        v < 0.6f -> 0.6f
                        v < 0.8f -> 0.8f
                        else     -> 1f
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (alpha == 0f) c.surface2 else c.accent.copy(alpha = alpha))
                    )
                }
            }
        }
    }
}

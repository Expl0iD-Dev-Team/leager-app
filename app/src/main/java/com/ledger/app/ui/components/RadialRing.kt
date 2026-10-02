package com.ledger.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ledger.app.ui.theme.AppFont
import com.ledger.app.ui.theme.ledger

@Composable
fun RadialRing(
    value: Float,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    thickness: Dp = 4.dp,
    color: Color? = null,
    trackColor: Color? = null,
    label: String = "",
    sublabel: String = "",
    labelSize: TextUnit = 14.sp
) {
    val c = MaterialTheme.ledger
    val ringColor = color ?: c.accent
    val ringTrack = trackColor ?: c.surface2
    val clamped = value.coerceIn(0f, 1f)

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = thickness.toPx()
            val radius = this.size.minDimension / 2f - stroke / 2f
            val cx = this.size.width / 2f
            val cy = this.size.height / 2f
            val topLeft = Offset(cx - radius, cy - radius)
            val arcSize = Size(radius * 2f, radius * 2f)

            // Track
            drawArc(
                color = ringTrack,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Butt)
            )
            // Value arc
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = clamped * 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }

        if (label.isNotEmpty() || sublabel.isNotEmpty()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (label.isNotEmpty()) {
                    Text(
                        text = label,
                        fontFamily = AppFont,
                        fontWeight = FontWeight.Medium,
                        fontSize = labelSize,
                        color = c.text,
                        lineHeight = labelSize
                    )
                }
                if (sublabel.isNotEmpty()) {
                    Text(
                        text = sublabel,
                        fontFamily = AppFont,
                        fontSize = (labelSize.value - 4f).sp,
                        color = c.muted,
                        lineHeight = (labelSize.value - 2f).sp
                    )
                }
            }
        }
    }
}

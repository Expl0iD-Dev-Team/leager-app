package com.ledger.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ledger.app.ui.theme.AppFont
import com.ledger.app.ui.theme.ledger

/** Large centered amount input with a muted currency sign. */
@Composable
fun AmountInput(
    amountText: String,
    onAmountChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    currencySymbol: String = "₽"
) {
    val c = MaterialTheme.ledger
    val style = TextStyle(
        fontFamily = AppFont,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 56.sp,
        letterSpacing = (-2).sp,
        color = c.text
    )

    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Amount", fontFamily = AppFont, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = c.muted)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            BasicTextField(
                value = amountText,
                onValueChange = { raw ->
                    val filtered = raw.replace(',', '.').filter { it.isDigit() || it == '.' }
                    if (filtered.count { it == '.' } <= 1) onAmountChange(filtered)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                textStyle = style,
                singleLine = true,
                cursorBrush = SolidColor(c.text),
                modifier = Modifier.widthIn(min = 40.dp),
                decorationBox = { inner ->
                    Box {
                        if (amountText.isEmpty()) Text("0", style = style.copy(color = c.faint))
                        inner()
                    }
                }
            )
            Spacer(Modifier.width(8.dp))
            Text(currencySymbol, fontFamily = AppFont, fontWeight = FontWeight.Bold, fontSize = 34.sp, color = c.faint)
        }
    }
}

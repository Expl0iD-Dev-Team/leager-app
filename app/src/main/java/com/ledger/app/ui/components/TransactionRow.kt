package com.ledger.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ledger.app.domain.model.Category
import com.ledger.app.domain.model.Transaction
import com.ledger.app.domain.model.TransactionType
import com.ledger.app.ui.theme.AppFont
import com.ledger.app.ui.theme.ledger
import com.ledger.app.util.formatHm
import com.ledger.app.util.formatMoney
import java.time.LocalTime

/** Time of day, or null for operations without one (CSV imports are stored at 00:00). */
fun Transaction.timeLabel(): String? = if (time == LocalTime.MIDNIGHT) null else time.formatHm()

@Composable
fun TransactionRow(
    transaction: Transaction,
    category: Category?,
    accountName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    toAccountName: String? = null,
    meta: String? = transaction.timeLabel()
) {
    val c = MaterialTheme.ledger
    val type = transaction.type

    val title = transaction.note.ifBlank {
        category?.name ?: if (type == TransactionType.TRANSFER) "Transfer" else "—"
    }
    val subtitle = if (type == TransactionType.TRANSFER) {
        "Transfer · $accountName → ${toAccountName ?: "—"}"
    } else {
        listOfNotNull(category?.name, accountName.ifBlank { null }).joinToString(" · ")
    }
    val amount = Math.abs(transaction.amount).formatMoney()
    val (amountText, amountColor) = when (type) {
        TransactionType.INCOME   -> "+$amount" to c.income
        TransactionType.EXPENSE  -> "−$amount" to c.text
        TransactionType.TRANSFER -> amount to c.muted
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CategoryBubble(category)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontFamily = AppFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = c.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                subtitle,
                fontFamily = AppFont,
                fontSize = 13.sp,
                color = c.muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                amountText,
                fontFamily = AppFont,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = amountColor,
                maxLines = 1
            )
            if (meta != null) {
                Text(
                    meta,
                    fontFamily = AppFont,
                    fontSize = 12.sp,
                    color = c.faint,
                    maxLines = 1,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

fun parseHexColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        Color.Gray
    }
}

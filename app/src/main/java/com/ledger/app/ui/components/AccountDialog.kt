package com.ledger.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ledger.app.domain.model.Account
import com.ledger.app.domain.model.AccountType
import com.ledger.app.ui.theme.AppFont
import com.ledger.app.ui.theme.CATEGORY_PALETTE
import com.ledger.app.ui.theme.ledger

/** Grid of round color swatches. */
@Composable
fun ColorPicker(
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    colors: List<String> = CATEGORY_PALETTE
) {
    val c = MaterialTheme.ledger
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        colors.chunked(7).forEach { rowColors ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowColors.forEach { hex ->
                    val sel = selected.equals(hex, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .border(2.dp, if (sel) c.text else Color.Transparent, CircleShape)
                            .clickable { onSelect(hex) }
                            .padding(5.dp)
                            .clip(CircleShape)
                            .background(parseHexColor(hex))
                    )
                }
            }
        }
    }
}

@Composable
fun AccountDialog(
    editTarget: Account?,
    onDismiss: () -> Unit,
    onSave: (name: String, type: AccountType, currency: String, color: String, includeInTotal: Boolean, balance: Double) -> Unit,
    onArchive: (() -> Unit)? = null
) {
    val c = MaterialTheme.ledger
    val isEditing = editTarget != null

    var name by remember(editTarget) { mutableStateOf(editTarget?.name ?: "") }
    var type by remember(editTarget) { mutableStateOf(editTarget?.type ?: AccountType.CARD) }
    var currency by remember(editTarget) { mutableStateOf(editTarget?.currency ?: "RUB") }
    var color by remember(editTarget) { mutableStateOf(editTarget?.color ?: CATEGORY_PALETTE[0]) }
    var includeInTotal by remember(editTarget) { mutableStateOf(editTarget?.includeInTotal ?: true) }
    var balanceText by remember(editTarget) { mutableStateOf(editTarget?.balance?.let { "%.2f".format(java.util.Locale.US, it) } ?: "0") }
    var showArchiveConfirm by remember { mutableStateOf(false) }

    if (showArchiveConfirm) {
        LedgerDialog(
            title = "Archive account?",
            onDismiss = { showArchiveConfirm = false },
            confirmText = "Archive",
            onConfirm = { onArchive?.invoke() },
            destructive = true
        ) {
            Text(
                "The account will be hidden from lists. Its operation history is kept.",
                style = MaterialTheme.typography.bodyMedium,
                color = c.muted
            )
        }
        return
    }

    LedgerDialog(
        title = if (isEditing) "Edit account" else "New account",
        onDismiss = onDismiss,
        confirmText = "Save",
        onConfirm = {
            onSave(name, type, currency, color, includeInTotal, balanceText.toDoubleOrNull() ?: 0.0)
        }
    ) {
        LedgerTextField(
            value = name,
            onValueChange = { name = it },
            label = "Name",
            placeholder = "e.g. Tinkoff Black"
        )

        FormSection("Type") {
            SegmentedControl(
                options = AccountType.entries.toList(),
                selected = type,
                onSelect = { type = it },
                label = { it.label },
                height = 38.dp,
                containerColor = c.surface2,
                bordered = false
            )
        }

        FormSection("Currency") {
            SegmentedControl(
                options = listOf("RUB", "USD", "EUR"),
                selected = currency,
                onSelect = { currency = it },
                label = { it },
                height = 38.dp,
                containerColor = c.surface2,
                bordered = false
            )
        }

        FormSection("Color") {
            ColorPicker(selected = color, onSelect = { color = it })
        }

        LedgerTextField(
            value = balanceText,
            onValueChange = { s ->
                val v = s.replace(',', '.')
                if (v.isEmpty() || v.matches(Regex("-?\\d*\\.?\\d*"))) balanceText = v
            },
            label = "Balance",
            keyboardType = KeyboardType.Decimal
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(c.surface2)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text("Include in net worth", fontFamily = AppFont, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = c.text)
                Text("Count this balance in the total", fontFamily = AppFont, fontSize = 12.sp, color = c.muted)
            }
            LedgerSwitch(checked = includeInTotal, onCheckedChange = { includeInTotal = it })
        }

        if (isEditing && onArchive != null) {
            Text(
                "Archive account",
                fontFamily = AppFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = c.danger,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { showArchiveConfirm = true }
                    .padding(vertical = 4.dp)
            )
        }
    }
}

/** Caption + control, used inside dialogs and forms. */
@Composable
fun FormSection(label: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            label,
            fontFamily = AppFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = MaterialTheme.ledger.muted
        )
        content()
    }
}

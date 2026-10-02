package com.ledger.app.ui.screen.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ledger.app.LedgerApplication
import com.ledger.app.domain.model.TransactionType
import com.ledger.app.ui.components.*
import com.ledger.app.ui.theme.AppFont
import com.ledger.app.ui.theme.ledger
import com.ledger.app.util.formatMedium
import com.ledger.app.util.formatMoneyFull

@Composable
fun TransactionDetailScreen(
    app: LedgerApplication,
    transactionId: String,
    onEdit: () -> Unit,
    onBackClick: () -> Unit
) {
    val vm: TransactionDetailViewModel = viewModel(
        factory = TransactionDetailViewModel.Factory(app, transactionId)
    )
    val state by vm.state.collectAsState()
    val c = MaterialTheme.ledger
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.deleted) {
        if (state.deleted) onBackClick()
    }

    val tx = state.transaction ?: return
    val cat = state.category
    val amount = Math.abs(tx.amount).formatMoneyFull()
    val (amountText, amountColor) = when (tx.type) {
        TransactionType.INCOME   -> "+$amount" to c.income
        TransactionType.EXPENSE  -> "−$amount" to c.text
        TransactionType.TRANSFER -> amount to c.text
    }
    val whenText = listOfNotNull(tx.date.formatMedium(), tx.timeLabel()).joinToString(" · ")

    if (showDeleteDialog) {
        LedgerDialog(
            title = "Delete operation?",
            onDismiss = { showDeleteDialog = false },
            confirmText = "Delete",
            onConfirm = {
                showDeleteDialog = false
                vm.delete()
            },
            destructive = true
        ) {
            Text(
                "The account balance will be adjusted back. This can't be undone.",
                style = MaterialTheme.typography.bodyMedium,
                color = c.muted
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
    ) {
        TopBar(
            title = tx.type.label,
            onBack = onBackClick,
            trailing = { CircleIconButton(LedgerIcons.Pencil, onClick = onEdit, contentDescription = "Edit") }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            // Hero
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CategoryBubble(cat, size = 60.dp, corner = 20.dp)
                Spacer(Modifier.height(14.dp))
                Text(
                    amountText,
                    fontFamily = AppFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 40.sp,
                    letterSpacing = (-1.2).sp,
                    color = amountColor,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
                Text(whenText, fontFamily = AppFont, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = c.muted)
            }

            // Details
            LedgerCard(
                modifier = Modifier.fillMaxWidth(),
                radius = 24.dp,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
            ) {
                val rows = listOfNotNull(
                    "Category" to (cat?.name ?: "—"),
                    (if (tx.type == TransactionType.TRANSFER) "From" else "Account") to (state.account?.name ?: "—"),
                    if (tx.type == TransactionType.TRANSFER) "To" to (state.toAccount?.name ?: "—") else null,
                    "Note" to tx.note.ifBlank { "—" },
                    if (tx.tags.isNotEmpty()) "Tags" to tx.tags.joinToString(" · ") else null
                )
                rows.forEach { (key, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(key, fontFamily = AppFont, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = c.muted, modifier = Modifier.width(96.dp))
                        Text(
                            value,
                            fontFamily = AppFont,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = c.text,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DialogButton("Duplicate", onClick = vm::duplicate, modifier = Modifier.weight(1f).height(56.dp), kind = ButtonKind.SECONDARY)
            DialogButton("Delete", onClick = { showDeleteDialog = true }, modifier = Modifier.weight(1f).height(56.dp), kind = ButtonKind.DANGER)
        }
    }
}

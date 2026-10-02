package com.ledger.app.ui.screen.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ledger.app.LedgerApplication
import com.ledger.app.domain.model.Account
import com.ledger.app.ui.components.*
import com.ledger.app.ui.theme.AppFont
import com.ledger.app.ui.theme.ledger
import com.ledger.app.util.formatMoney
import com.ledger.app.util.monthLong
import java.time.LocalDate
import java.util.Locale

@Composable
fun AccountsScreen(
    app: LedgerApplication,
    onBackClick: () -> Unit
) {
    val vm: AccountsViewModel = viewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    val c = MaterialTheme.ledger
    val month = monthLong(LocalDate.now().monthValue)

    if (state.showDialog) {
        AccountDialog(
            editTarget = state.editTarget,
            onDismiss = vm::dismissDialog,
            onSave = { name, type, currency, color, includeInTotal, balance ->
                vm.saveAccount(name, type, currency, color, includeInTotal, balance)
            },
            onArchive = state.editTarget?.let { acc -> { vm.archiveAccount(acc) } }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
    ) {
        TopBar(
            title = "Accounts",
            onBack = onBackClick,
            trailing = { CircleIconButton(LedgerIcons.Plus, onClick = vm::openCreate, contentDescription = "New account") }
        )

        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                LedgerCard(modifier = Modifier.fillMaxWidth(), radius = 26.dp, contentPadding = PaddingValues(20.dp)) {
                    Text("Net worth · ₽ equivalent", fontFamily = AppFont, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = c.muted)
                    Text(
                        state.netWorth.formatMoney(),
                        style = MaterialTheme.typography.displayMedium,
                        color = c.text,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        if (state.accounts.size == 1) "1 account" else "${state.accounts.size} accounts",
                        fontFamily = AppFont, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = c.muted,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            item {
                if (state.accounts.isEmpty()) {
                    LedgerCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(0.dp)) {
                        EmptyState(LedgerIcons.Wallet, "No accounts", "Tap + to add a card, cash or savings account.")
                    }
                } else {
                    LedgerCard(
                        modifier = Modifier.fillMaxWidth(),
                        radius = 24.dp,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        state.accounts.forEach { account ->
                            AccountRow(account = account, totalRub = state.netWorth, onClick = { vm.openEdit(account) })
                        }
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatBox("$month in", "+${state.monthIncome.formatMoney()}", c.income, LedgerIcons.ArrowIn, Modifier.weight(1f))
                    StatBox("$month out", "−${state.monthExpense.formatMoney()}", c.text, LedgerIcons.ArrowOut, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun AccountRow(account: Account, totalRub: Double, onClick: () -> Unit) {
    val c = MaterialTheme.ledger
    val color = parseHexColor(account.color)
    val rate = when (account.currency) { "USD" -> 92.0; "EUR" -> 99.0; else -> 1.0 }
    val share = if (totalRub > 0 && account.includeInTotal) (account.balance * rate) / totalRub else 0.0
    val details = listOfNotNull(
        account.type.label,
        account.last4?.let { "•• $it" },
        account.currency,
        if (!account.includeInTotal) "not in total" else null
    ).joinToString(" · ")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBubble(accountIcon(account.type), color, size = 44.dp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    account.name,
                    fontFamily = AppFont, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = c.text,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text(details, fontFamily = AppFont, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = c.muted)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    account.balance.formatMoney(account.currency),
                    fontFamily = AppFont, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = c.text
                )
                Text(
                    String.format(Locale("ru", "RU"), "%.1f%%", share * 100),
                    fontFamily = AppFont, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = c.faint
                )
            }
        }
        ProgressBar(fraction = share.toFloat(), color = color, height = 4.dp)
    }
}

@Composable
private fun StatBox(label: String, value: String, valueColor: Color, icon: ImageVector, modifier: Modifier) {
    val c = MaterialTheme.ledger
    LedgerCard(modifier = modifier, radius = 20.dp, contentPadding = PaddingValues(16.dp), verticalSpacing = 10.dp) {
        Icon(icon, contentDescription = null, tint = c.muted, modifier = Modifier.size(18.dp))
        Column {
            Text(label, fontFamily = AppFont, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = c.muted)
            Text(value, fontFamily = AppFont, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = valueColor, maxLines = 1)
        }
    }
}

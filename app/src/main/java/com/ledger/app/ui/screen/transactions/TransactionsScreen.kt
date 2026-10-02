package com.ledger.app.ui.screen.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ledger.app.domain.model.TransactionType
import com.ledger.app.ui.components.*
import com.ledger.app.ui.theme.AppFont
import com.ledger.app.ui.theme.ledger
import com.ledger.app.util.formatMoney
import com.ledger.app.util.formatRelative
import com.ledger.app.util.formatSigned
import com.ledger.app.util.monthLong
import java.time.LocalDate

@Composable
fun TransactionsScreen(
    onTransactionClick: (String) -> Unit
) {
    val vm: TransactionsViewModel = viewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    val c = MaterialTheme.ledger
    val month = monthLong(LocalDate.now().monthValue)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { ScreenTitle("Activity") }

        item {
            LedgerTextField(
                value = state.searchQuery,
                onValueChange = vm::onSearch,
                placeholder = "Search by note, category or amount",
                leadingIcon = LedgerIcons.Search,
                containerColor = c.surface
            )
        }

        item {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TypeFilter.entries.forEach { f ->
                    ChoiceChip(f.label, selected = state.typeFilter == f, onClick = { vm.onTypeFilter(f) })
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MonthTotal("$month in", "+${state.monthIncome.formatMoney()}", c.income, LedgerIcons.ArrowIn, Modifier.weight(1f))
                MonthTotal("$month out", "−${state.monthExpense.formatMoney()}", c.text, LedgerIcons.ArrowOut, Modifier.weight(1f))
            }
        }

        val grouped = state.filteredTransactions.groupBy { it.date }
        grouped.entries.sortedByDescending { it.key }.forEach { (date, txns) ->
            item(key = date.toString()) {
                val net = txns.sumOf { tx ->
                    when (tx.type) {
                        TransactionType.INCOME   -> tx.amount
                        TransactionType.EXPENSE  -> -kotlin.math.abs(tx.amount)
                        TransactionType.TRANSFER -> 0.0
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(date.formatRelative(), fontFamily = AppFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = c.muted)
                        if (net != 0.0) {
                            Text(net.formatSigned(), fontFamily = AppFont, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = c.muted)
                        }
                    }
                    LedgerCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        txns.forEach { tx ->
                            TransactionRow(
                                transaction = tx,
                                category = state.categories[tx.categoryId],
                                accountName = state.accountNames[tx.accountId] ?: "",
                                toAccountName = tx.toAccountId?.let { state.accountNames[it] },
                                onClick = { onTransactionClick(tx.id) }
                            )
                        }
                    }
                }
            }
        }

        if (state.filteredTransactions.isEmpty() && !state.isLoading) {
            item {
                val filtering = state.searchQuery.isNotBlank() || state.typeFilter != TypeFilter.ALL
                EmptyState(
                    icon = if (filtering) LedgerIcons.Search else LedgerIcons.Inbox,
                    title = if (filtering) "Nothing found" else "No operations yet",
                    subtitle = if (filtering) "Try another search or filter." else "Tap + to add your first operation."
                )
            }
        }
    }
}

@Composable
private fun MonthTotal(label: String, value: String, valueColor: Color, icon: ImageVector, modifier: Modifier) {
    val c = MaterialTheme.ledger
    LedgerCard(modifier = modifier, radius = 20.dp, contentPadding = PaddingValues(16.dp), verticalSpacing = 10.dp) {
        Icon(icon, contentDescription = null, tint = c.muted, modifier = Modifier.size(18.dp))
        Column {
            Text(label, fontFamily = AppFont, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = c.muted)
            Text(value, fontFamily = AppFont, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = valueColor, maxLines = 1)
        }
    }
}

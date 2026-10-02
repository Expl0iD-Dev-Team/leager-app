package com.ledger.app.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ledger.app.domain.model.Account
import com.ledger.app.domain.model.TransactionType
import com.ledger.app.ui.components.*
import com.ledger.app.ui.theme.AppFont
import com.ledger.app.ui.theme.ledger
import com.ledger.app.util.formatFull
import com.ledger.app.util.formatMoney
import com.ledger.app.util.formatRelative
import com.ledger.app.util.monthLong
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun HomeScreen(
    onAddClick: (TransactionType) -> Unit,
    onTransactionClick: (String) -> Unit,
    onSeeAllClick: () -> Unit,
    onAccountsClick: () -> Unit,
    onCategoriesClick: () -> Unit
) {
    val vm: HomeViewModel = viewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    val c = MaterialTheme.ledger
    var hideBalances by rememberSaveable { mutableStateOf(false) }

    if (state.showAccountDialog) {
        AccountDialog(
            editTarget = state.editingAccount,
            onDismiss = vm::dismissAccountDialog,
            onSave = { name, type, currency, color, includeInTotal, balance ->
                vm.saveAccount(name, type, currency, color, includeInTotal, balance)
            },
            onArchive = state.editingAccount?.let { acc -> { vm.archiveAccount(acc) } }
        )
    }

    fun money(value: Double, currency: String = "RUB") =
        if (hideBalances) "•••••" else value.formatMoney(currency)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(26.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(c.surface2),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(LedgerIcons.User, contentDescription = null, tint = c.muted, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(greeting(), fontFamily = AppFont, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = c.muted)
                    Text(LocalDate.now().formatFull(), style = MaterialTheme.typography.titleLarge, color = c.text)
                }
                CircleIconButton(LedgerIcons.Search, onClick = onSeeAllClick, contentDescription = "Search operations")
                Spacer(Modifier.width(10.dp))
                CircleIconButton(LedgerIcons.Tag, onClick = onCategoriesClick, contentDescription = "Categories")
            }
        }

        // Balance card
        item {
            BalanceCard(
                netWorth = money(state.netWorth, state.netWorthCurrency),
                currency = state.netWorthCurrency,
                income = if (hideBalances) "•••••" else "+${state.monthIncome.formatMoney()}",
                spent = if (hideBalances) "•••••" else "−${state.monthExpense.formatMoney()}",
                hidden = hideBalances,
                onToggleHidden = { hideBalances = !hideBalances },
                onCycleCurrency = {
                    val next = when (state.netWorthCurrency) { "RUB" -> "USD"; "USD" -> "EUR"; else -> "RUB" }
                    vm.setNetWorthCurrency(next)
                }
            )
        }

        // Quick actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                QuickAction(LedgerIcons.ArrowOut, "Expense") { onAddClick(TransactionType.EXPENSE) }
                QuickAction(LedgerIcons.ArrowIn, "Income") { onAddClick(TransactionType.INCOME) }
                QuickAction(LedgerIcons.Transfer, "Transfer") { onAddClick(TransactionType.TRANSFER) }
                QuickAction(LedgerIcons.PieChart, "Budgets", onClick = onCategoriesClick)
            }
        }

        // Accounts
        item {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                SectionTitle("Accounts", action = "See all", onAction = onAccountsClick)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    state.accounts.forEach { account ->
                        AccountCard(
                            account = account,
                            balance = money(account.balance, account.currency),
                            onClick = { vm.openAccountEdit(account) }
                        )
                    }
                    AddAccountCard(onClick = vm::openAccountCreate)
                }
            }
        }

        // Monthly budget
        if (state.monthBudget > 0) {
            item {
                BudgetCard(spent = state.monthExpense, budget = state.monthBudget)
            }
        }

        // Recent activity
        item {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                SectionTitle("Recent activity", action = "See all", onAction = onSeeAllClick)
                if (state.recentTransactions.isEmpty() && !state.isLoading) {
                    LedgerCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(0.dp)) {
                        EmptyState(
                            icon = LedgerIcons.Inbox,
                            title = "No operations yet",
                            subtitle = "Tap + to add your first expense or income."
                        )
                    }
                } else {
                    LedgerCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        state.recentTransactions.forEach { tx ->
                            val date = tx.date.formatRelative()
                            TransactionRow(
                                transaction = tx,
                                category = state.categories[tx.categoryId],
                                accountName = state.accountNames[tx.accountId] ?: "",
                                toAccountName = tx.toAccountId?.let { state.accountNames[it] },
                                meta = tx.timeLabel()?.let { "$date, $it" } ?: date,
                                onClick = { onTransactionClick(tx.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun greeting(): String = when (LocalTime.now().hour) {
    in 5..11  -> "Good morning"
    in 12..17 -> "Good afternoon"
    else      -> "Good evening"
}

@Composable
private fun BalanceCard(
    netWorth: String,
    currency: String,
    income: String,
    spent: String,
    hidden: Boolean,
    onToggleHidden: () -> Unit,
    onCycleCurrency: () -> Unit
) {
    val c = MaterialTheme.ledger
    val tint = c.onAccent.copy(alpha = 0.10f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(c.accent)
            .padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onToggleHidden),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total balance", fontFamily = AppFont, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = c.onAccent)
                Spacer(Modifier.width(8.dp))
                Icon(
                    LedgerIcons.Eye,
                    contentDescription = if (hidden) "Show balances" else "Hide balances",
                    tint = c.onAccent.copy(alpha = if (hidden) 0.5f else 1f),
                    modifier = Modifier.size(16.dp)
                )
            }
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(tint)
                    .clickable(onClick = onCycleCurrency)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(currency, fontFamily = AppFont, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = c.onAccent)
                Spacer(Modifier.width(4.dp))
                Icon(LedgerIcons.ChevronDown, contentDescription = null, tint = c.onAccent, modifier = Modifier.size(14.dp))
            }
        }

        Text(
            netWorth,
            fontFamily = AppFont,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 38.sp,
            letterSpacing = (-1.2).sp,
            color = c.onAccent,
            maxLines = 1
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BalancePill("Income", income, LedgerIcons.ArrowIn, Modifier.weight(1f))
            BalancePill("Spent", spent, LedgerIcons.ArrowOut, Modifier.weight(1f))
        }
    }
}

@Composable
private fun BalancePill(label: String, value: String, icon: ImageVector, modifier: Modifier) {
    val c = MaterialTheme.ledger
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(c.onAccent.copy(alpha = 0.10f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(c.onAccent),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = c.accent, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, fontFamily = AppFont, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = c.onAccent.copy(alpha = 0.75f))
            Text(
                value,
                fontFamily = AppFont,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = c.onAccent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun QuickAction(icon: ImageVector, label: String, onClick: () -> Unit) {
    val c = MaterialTheme.ledger
    val shape = RoundedCornerShape(22.dp)
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(shape)
                .background(c.surface)
                .border(1.dp, c.border, shape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = c.text, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(label, fontFamily = AppFont, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = c.muted)
    }
}

@Composable
private fun AccountCard(account: Account, balance: String, onClick: () -> Unit) {
    val c = MaterialTheme.ledger
    val color = parseHexColor(account.color)
    val tag = account.last4?.let { "•• $it" } ?: account.currency

    Column(
        modifier = Modifier
            .width(156.dp)
            .ledgerCard()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBubble(accountIcon(account.type), color, size = 36.dp, corner = 12.dp)
            Text(tag, fontFamily = AppFont, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = c.faint)
        }
        Column {
            Text(
                account.name,
                fontFamily = AppFont,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = c.muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                balance,
                fontFamily = AppFont,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = c.text,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun AddAccountCard(onClick: () -> Unit) {
    val c = MaterialTheme.ledger
    Column(
        modifier = Modifier
            .width(96.dp)
            .height(124.dp)
            .ledgerCard()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(LedgerIcons.Plus, contentDescription = null, tint = c.muted, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(6.dp))
        Text("Account", fontFamily = AppFont, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = c.muted)
    }
}

@Composable
private fun BudgetCard(spent: Double, budget: Double) {
    val c = MaterialTheme.ledger
    val today = LocalDate.now()
    val fraction = (spent / budget).toFloat()
    val daysLeft = today.lengthOfMonth() - today.dayOfMonth + 1
    val over = fraction > 1f

    LedgerCard(modifier = Modifier.fillMaxWidth(), verticalSpacing = 12.dp) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${monthLong(today.monthValue)} budget", style = MaterialTheme.typography.titleMedium, color = c.text)
            Text(
                "${(fraction * 100).toInt()}%",
                fontFamily = AppFont,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = if (over) c.danger else c.text
            )
        }
        ProgressBar(fraction = fraction, color = if (over) c.danger else c.accent)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                "${spent.formatMoney()} of ${budget.formatMoney()}",
                fontFamily = AppFont, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = c.muted
            )
            Text(
                if (daysLeft == 1) "Last day" else "$daysLeft days left",
                fontFamily = AppFont, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = c.muted
            )
        }
    }
}

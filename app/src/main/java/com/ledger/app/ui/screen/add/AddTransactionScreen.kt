package com.ledger.app.ui.screen.add

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ledger.app.LedgerApplication
import com.ledger.app.domain.model.Account
import com.ledger.app.domain.model.Category
import com.ledger.app.domain.model.CategoryType
import com.ledger.app.domain.model.RecurringInterval
import com.ledger.app.domain.model.TransactionType
import com.ledger.app.ui.components.*
import com.ledger.app.ui.theme.AppFont
import com.ledger.app.ui.theme.ledger
import com.ledger.app.util.formatMoney
import com.ledger.app.util.formatShort
import com.ledger.app.util.isToday
import com.ledger.app.util.isYesterday
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTransactionScreen(
    app: LedgerApplication,
    initialType: String,
    transactionId: String?,
    onSaved: () -> Unit,
    onCancel: () -> Unit
) {
    val vm: AddTransactionViewModel = viewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    val c = MaterialTheme.ledger
    val isEditing = transactionId != null
    val isTransfer = state.type == TransactionType.TRANSFER

    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val initialMillis = state.date.toEpochDay() * 86_400_000L
        val dpState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dpState.selectedDateMillis?.let { millis ->
                        vm.setDate(LocalDate.ofEpochDay(millis / 86_400_000L))
                    }
                    showDatePicker = false
                }) { Text("OK", fontFamily = AppFont, fontWeight = FontWeight.Bold, color = c.text) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", fontFamily = AppFont, fontWeight = FontWeight.SemiBold, color = c.muted)
                }
            }
        ) {
            DatePicker(state = dpState)
        }
    }

    LaunchedEffect(transactionId) {
        if (transactionId != null) vm.loadTransaction(transactionId)
    }
    LaunchedEffect(initialType) {
        if (transactionId == null) {
            runCatching { TransactionType.valueOf(initialType) }.getOrNull()
                ?.let { vm.setType(it) }
        }
    }
    LaunchedEffect(state.savedSuccessfully) {
        if (state.savedSuccessfully) onSaved()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
    ) {
        TopBar(
            title = if (isEditing) "Edit operation" else "New operation",
            onBack = onCancel,
            backIcon = LedgerIcons.Close
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            SegmentedControl(
                options = TransactionType.entries.toList(),
                selected = state.type,
                onSelect = vm::setType,
                label = { it.label }
            )

            AmountInput(
                amountText = state.amountText,
                onAmountChange = vm::setAmount,
                modifier = Modifier.padding(vertical = 6.dp)
            )

            // Accounts
            if (isTransfer) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AccountPicker(
                        label = "From",
                        accounts = state.accounts,
                        selectedId = state.selectedAccount?.id,
                        onSelect = vm::setAccount
                    )
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(c.surface2)
                            .border(1.dp, c.border, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(LedgerIcons.Transfer, contentDescription = null, tint = c.text, modifier = Modifier.size(18.dp))
                    }
                    AccountPicker(
                        label = "To",
                        accounts = state.accounts.filter { it.id != state.selectedAccount?.id },
                        selectedId = state.toAccount?.id,
                        onSelect = vm::setToAccount
                    )
                }
            } else {
                AccountPicker(
                    label = "Account",
                    accounts = state.accounts,
                    selectedId = state.selectedAccount?.id,
                    onSelect = vm::setAccount
                )
            }

            // Category
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("Category")
                if (isTransfer) {
                    TransferCategoryCard(state.selectedCategory)
                } else {
                    val displayCats = state.categories.filter {
                        it.type == if (state.type == TransactionType.INCOME) CategoryType.INCOME else CategoryType.EXPENSE
                    }.filterNot { AddTransactionViewModel.isTransferCategory(it) }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        displayCats.forEach { cat ->
                            CategoryChip(
                                category = cat,
                                selected = cat.id == state.selectedCategory?.id,
                                onClick = { vm.setCategory(cat) }
                            )
                        }
                    }
                }
            }

            LedgerTextField(
                value = state.note,
                onValueChange = vm::setNote,
                placeholder = "Add a note",
                leadingIcon = LedgerIcons.Pencil,
                containerColor = c.surface
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FieldCard(
                    label = "Date",
                    value = dateLabel(state.date),
                    icon = LedgerIcons.Calendar,
                    onClick = { showDatePicker = true },
                    modifier = Modifier.weight(1f)
                )
                if (!isEditing) {
                    FieldCard(
                        label = "Repeat",
                        value = state.recurringInterval?.label ?: "Never",
                        icon = LedgerIcons.Repeat,
                        onClick = {
                            val current = state.recurringInterval
                            val next = if (current == null) RecurringInterval.MONTHLY
                            else RecurringInterval.entries.let {
                                val idx = it.indexOf(current)
                                if (idx < it.lastIndex) it[idx + 1] else null
                            }
                            vm.setRecurring(next)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            state.error?.let {
                Text(it, fontFamily = AppFont, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = c.danger)
            }
        }

        // Save
        val amount = state.amountText.toDoubleOrNull()
        val saveLabel = when {
            state.isSaving -> "Saving…"
            isEditing -> "Save changes"
            state.type == TransactionType.EXPENSE -> "Save expense"
            state.type == TransactionType.INCOME -> "Save income"
            amount != null && amount > 0 -> "Transfer ${amount.formatMoney()}"
            else -> "Transfer"
        }
        PrimaryButton(
            text = saveLabel,
            onClick = vm::save,
            enabled = !state.isSaving,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp)
        )
    }
}

private fun dateLabel(date: LocalDate): String = when {
    date.isToday()     -> "Today, ${date.formatShort()}"
    date.isYesterday() -> "Yesterday, ${date.formatShort()}"
    else               -> date.formatShort() + if (date.year != LocalDate.now().year) " ${date.year}" else ""
}

@Composable
private fun CategoryChip(category: Category, selected: Boolean, onClick: () -> Unit) {
    val c = MaterialTheme.ledger
    val color = parseHexColor(category.color)
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = Modifier
            .clip(shape)
            .background(if (selected) c.accent else c.surface)
            .then(if (selected) Modifier else Modifier.border(1.dp, c.border, shape))
            .clickable(onClick = onClick)
            .padding(start = 12.dp, end = 14.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            categoryIcon(category),
            contentDescription = null,
            tint = if (selected) c.onAccent else color,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            category.name,
            fontFamily = AppFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = if (selected) c.onAccent else c.text
        )
    }
}

/** Transfers always use the "Transfer" category — shown locked, not selectable. */
@Composable
private fun TransferCategoryCard(category: Category?) {
    val c = MaterialTheme.ledger
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .ledgerCard(20.dp)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (category != null) CategoryBubble(category, size = 40.dp)
        else IconBubble(LedgerIcons.Transfer, c.muted, size = 40.dp)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(category?.name ?: "Transfer", fontFamily = AppFont, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = c.text)
            Text("Set automatically for transfers", fontFamily = AppFont, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = c.muted)
        }
        Icon(LedgerIcons.Lock, contentDescription = null, tint = c.faint, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun AccountPicker(
    label: String,
    accounts: List<Account>,
    selectedId: String?,
    onSelect: (Account) -> Unit
) {
    val c = MaterialTheme.ledger
    var expanded by remember { mutableStateOf(false) }
    val selected = accounts.find { it.id == selectedId }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .ledgerCard(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selected != null) {
                IconBubble(accountIcon(selected.type), parseHexColor(selected.color), size = 40.dp)
            } else {
                IconBubble(LedgerIcons.Wallet, c.muted, size = 40.dp)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontFamily = AppFont, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = c.muted)
                Text(
                    selected?.name ?: "Choose account",
                    fontFamily = AppFont,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = if (selected != null) c.text else c.faint
                )
            }
            if (selected != null) {
                Text(
                    selected.balance.formatMoney(selected.currency),
                    fontFamily = AppFont,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = c.muted
                )
                Spacer(Modifier.width(8.dp))
            }
            Icon(
                LedgerIcons.ChevronDown,
                contentDescription = null,
                tint = c.faint,
                modifier = Modifier.size(18.dp)
            )
        }
        if (expanded) {
            accounts.filter { it.id != selected?.id }.forEach { acc ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(acc); expanded = false }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconBubble(accountIcon(acc.type), parseHexColor(acc.color), size = 32.dp, corner = 10.dp)
                    Spacer(Modifier.width(12.dp))
                    Text(acc.name, fontFamily = AppFont, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = c.text, modifier = Modifier.weight(1f))
                    Text(acc.balance.formatMoney(acc.currency), fontFamily = AppFont, fontSize = 13.sp, color = c.muted)
                }
            }
        }
    }
}

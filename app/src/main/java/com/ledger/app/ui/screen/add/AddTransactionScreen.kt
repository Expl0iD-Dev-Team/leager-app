package com.ledger.app.ui.screen.add

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import java.time.LocalDate
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
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
import com.ledger.app.ui.components.BigAmountDisplay
import com.ledger.app.util.formatMoney
import com.ledger.app.ui.components.parseHexColor
import com.ledger.app.ui.theme.IbmPlexMonoFamily
import com.ledger.app.ui.theme.IbmPlexSansFamily
import com.ledger.app.ui.theme.ledger

@OptIn(ExperimentalMaterial3Api::class)
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
                }) { Text("OK", fontFamily = IbmPlexMonoFamily, color = c.lime) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Отмена", fontFamily = IbmPlexMonoFamily, color = c.muted)
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
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .border(width = 0.dp, color = Color.Transparent),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "×",
                fontFamily = IbmPlexMonoFamily,
                fontSize = 18.sp,
                color = c.muted,
                modifier = Modifier.clickable(onClick = onCancel)
            )
            Text(
                "NEW OPERATION",
                fontFamily = IbmPlexMonoFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                letterSpacing = 1.4.sp,
                color = c.text
            )
            Text(
                if (state.isSaving) "…" else "SAVE",
                fontFamily = IbmPlexMonoFamily,
                fontSize = 11.sp,
                letterSpacing = 1.2.sp,
                color = c.lime,
                modifier = Modifier.clickable { if (!state.isSaving) vm.save() }
            )
        }

        // Type segmented
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .border(1.dp, c.border)
        ) {
            TransactionType.entries.forEach { type ->
                val isSelected = state.type == type
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (isSelected) c.text else Color.Transparent)
                        .clickable { vm.setType(type) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        type.label.uppercase(),
                        fontFamily = IbmPlexMonoFamily,
                        fontSize = 11.sp,
                        letterSpacing = 1.3.sp,
                        color = if (isSelected) c.bg else c.muted
                    )
                }
            }
        }

        // Scrollable form
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Amount
            BigAmountDisplay(
                amountText = state.amountText,
                onAmountChange = vm::setAmount,
                isExpense = state.type == TransactionType.EXPENSE || state.type == TransactionType.TRANSFER,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 28.dp)
            )

            // Account picker
            FieldLabel(
                if (state.type == TransactionType.TRANSFER) "FROM ACCOUNT" else "ACCOUNT",
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            AccountPicker(
                accounts = state.accounts,
                selectedId = state.selectedAccount?.id,
                onSelect = vm::setAccount,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 8.dp, bottom = 18.dp)
            )

            if (state.type == TransactionType.TRANSFER) {
                FieldLabel("TO ACCOUNT", modifier = Modifier.padding(horizontal = 20.dp))
                AccountPicker(
                    accounts = state.accounts.filter { it.id != state.selectedAccount?.id },
                    selectedId = state.toAccount?.id,
                    onSelect = vm::setToAccount,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 8.dp, bottom = 18.dp)
                )
            }

            // Category grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FieldLabel("CATEGORY")
            }

            if (state.type == TransactionType.TRANSFER) {
                // Transfers always use the "Перевод" category — no choice here
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val transferCat = state.selectedCategory
                    if (transferCat != null) {
                        CategoryCell(
                            category = transferCat,
                            isSelected = true,
                            onClick = {},
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Text(
                            "ПЕРЕВОД",
                            fontFamily = IbmPlexMonoFamily,
                            fontSize = 11.sp,
                            letterSpacing = 1.2.sp,
                            color = c.text,
                            modifier = Modifier.weight(1f).border(1.dp, c.text).padding(12.dp)
                        )
                    }
                    repeat(3) { Spacer(Modifier.weight(1f)) }
                }
            } else {
                val displayCats = state.categories.filter {
                    it.type == if (state.type == TransactionType.INCOME) CategoryType.INCOME else CategoryType.EXPENSE
                }.filterNot { AddTransactionViewModel.isTransferCategory(it) }
                val rows = (displayCats.size + 3) / 4
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(rows) { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            repeat(4) { col ->
                                val idx = row * 4 + col
                                if (idx < displayCats.size) {
                                    val cat = displayCats[idx]
                                    CategoryCell(
                                        category = cat,
                                        isSelected = cat.id == state.selectedCategory?.id,
                                        onClick = { vm.setCategory(cat) },
                                        modifier = Modifier.weight(1f)
                                    )
                                } else {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Note field
            FieldLabel("NOTE", modifier = Modifier.padding(horizontal = 20.dp))
            androidx.compose.foundation.text.BasicTextField(
                value = state.note,
                onValueChange = vm::setNote,
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = IbmPlexMonoFamily,
                    fontSize = 15.sp,
                    color = c.text
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 8.dp)
                    .border(width = 0.dp, color = Color.Transparent)
                    .drawBorderBottom(c.borderStrong)
                    .padding(vertical = 12.dp)
            )

            Spacer(Modifier.height(22.dp))

            // Date and repeat row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, c.border)
                        .clickable { showDatePicker = true }
                        .padding(10.dp, 10.dp)
                ) {
                    FieldLabel("ДАТА")
                    Text(
                        text = "${state.date.dayOfMonth}.${state.date.monthValue.toString().padStart(2, '0')}.${state.date.year % 100}",
                        fontFamily = IbmPlexMonoFamily,
                        fontSize = 13.sp,
                        color = c.lime,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, c.border)
                        .padding(10.dp, 10.dp)
                        .clickable {
                            val current = state.recurringInterval
                            val next = if (current == null) RecurringInterval.MONTHLY
                            else RecurringInterval.entries.let {
                                val idx = it.indexOf(current)
                                if (idx < it.lastIndex) it[idx + 1] else null
                            }
                            vm.setRecurring(next)
                        }
                ) {
                    FieldLabel("REPEAT")
                    Text(
                        text = state.recurringInterval?.label?.uppercase() ?: "OFF",
                        fontFamily = IbmPlexMonoFamily,
                        fontSize = 13.sp,
                        color = if (state.recurringInterval != null) c.lime else c.muted,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Error
            state.error?.let {
                Text(
                    it,
                    fontFamily = IbmPlexMonoFamily,
                    fontSize = 11.sp,
                    color = c.red,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun FieldLabel(text: String, modifier: Modifier = Modifier) {
    val c = MaterialTheme.ledger
    Text(
        text = text,
        fontFamily = IbmPlexMonoFamily,
        fontSize = 9.sp,
        letterSpacing = 1.2.sp,
        color = c.faint,
        modifier = modifier
    )
}

@Composable
private fun AccountPicker(
    accounts: List<Account>,
    selectedId: String?,
    onSelect: (Account) -> Unit,
    modifier: Modifier = Modifier
) {
    val c = MaterialTheme.ledger
    var expanded by remember { mutableStateOf(false) }
    val selected = accounts.find { it.id == selectedId }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(c.surface)
                .border(1.dp, if (expanded) c.borderStrong else c.border)
                .clickable { expanded = !expanded }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.width(8.dp).height(24.dp).background(parseHexColor(selected?.color ?: "#888888")))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(selected?.name ?: "Выберите счёт", fontFamily = IbmPlexMonoFamily, fontSize = 14.sp, color = c.text)
                if (selected != null) {
                    Text(
                        "BAL ${selected.balance.formatMoney()}",
                        fontFamily = IbmPlexMonoFamily,
                        fontSize = 11.sp,
                        color = c.muted
                    )
                }
            }
            Text(if (expanded) "▲" else "▼", fontFamily = IbmPlexMonoFamily, fontSize = 10.sp, color = c.muted)
        }
        if (expanded) {
            accounts.filter { it.id != selected?.id }.forEach { acc ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(c.surface2)
                        .border(1.dp, c.border)
                        .clickable { onSelect(acc); expanded = false }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.width(8.dp).height(20.dp).background(parseHexColor(acc.color)))
                    Spacer(Modifier.width(12.dp))
                    Text(acc.name, fontFamily = IbmPlexMonoFamily, fontSize = 13.sp, color = c.text, modifier = Modifier.weight(1f))
                    Text(acc.balance.formatMoney(), fontFamily = IbmPlexMonoFamily, fontSize = 11.sp, color = c.muted)
                }
            }
        }
    }
}

@Composable
private fun CategoryCell(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = MaterialTheme.ledger
    Box(
        modifier = modifier
            .border(1.dp, if (isSelected) c.text else c.border)
            .background(if (isSelected) c.surface else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(parseHexColor(category.color))
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = category.name.take(8).uppercase(),
                fontFamily = IbmPlexMonoFamily,
                fontSize = 8.sp,
                letterSpacing = 0.4.sp,
                color = if (isSelected) c.text else c.muted,
                maxLines = 2,
                lineHeight = 10.sp
            )
        }
        if (isSelected) {
            Text(
                "✓",
                fontFamily = IbmPlexMonoFamily,
                fontSize = 9.sp,
                color = c.lime,
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }
    }
}

private fun Modifier.drawBorderBottom(color: androidx.compose.ui.graphics.Color): Modifier =
    this.drawBehind {
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(0f, size.height),
            end = androidx.compose.ui.geometry.Offset(size.width, size.height),
            strokeWidth = 1.dp.toPx()
        )
    }


package com.ledger.app.ui.screen.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ledger.app.LedgerApplication
import com.ledger.app.domain.model.Category
import com.ledger.app.domain.model.CategoryType
import com.ledger.app.ui.components.*
import com.ledger.app.ui.theme.AppFont
import com.ledger.app.ui.theme.CATEGORY_PALETTE
import com.ledger.app.ui.theme.ledger
import com.ledger.app.util.formatMoney
import com.ledger.app.util.monthLong
import java.time.LocalDate

@Composable
fun CategoriesScreen(
    app: LedgerApplication,
    onBackClick: () -> Unit
) {
    val vm: CategoriesViewModel = viewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    val c = MaterialTheme.ledger

    if (state.showDialog) {
        CategoryDialog(
            editTarget = state.editTarget,
            defaultType = state.selectedTab,
            onDismiss = vm::dismissDialog,
            onSave = { name, type, color, budget, iconCode -> vm.saveCategory(name, type, color, budget, iconCode) },
            onDelete = state.editTarget?.let { cat -> { vm.deleteCategory(cat) } }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
    ) {
        TopBar(
            title = "Categories",
            onBack = onBackClick,
            trailing = { CircleIconButton(LedgerIcons.Plus, onClick = vm::openCreate, contentDescription = "New category") }
        )

        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                SegmentedControl(
                    options = listOf(CategoryType.EXPENSE, CategoryType.INCOME),
                    selected = state.selectedTab,
                    onSelect = vm::selectTab,
                    label = {
                        if (it == CategoryType.EXPENSE) "Expenses · ${state.expenseCategories.size}"
                        else "Income · ${state.incomeCategories.size}"
                    },
                    height = 40.dp
                )
            }

            if (state.selectedTab == CategoryType.EXPENSE && state.totalBudget > 0) {
                item {
                    val fraction = (state.totalSpent / state.totalBudget).toFloat()
                    val over = fraction > 1f
                    LedgerCard(modifier = Modifier.fillMaxWidth(), verticalSpacing = 12.dp) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                "${monthLong(LocalDate.now().monthValue)} budgets",
                                style = MaterialTheme.typography.titleMedium,
                                color = c.text
                            )
                            Text(
                                "${(fraction * 100).toInt()}%",
                                fontFamily = AppFont, fontWeight = FontWeight.Bold, fontSize = 15.sp,
                                color = if (over) c.danger else c.text
                            )
                        }
                        ProgressBar(fraction = fraction, color = if (over) c.danger else c.accent)
                        Text(
                            "${state.totalSpent.formatMoney()} of ${state.totalBudget.formatMoney()}",
                            fontFamily = AppFont, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = c.muted
                        )
                    }
                }
            }

            val displayList = if (state.selectedTab == CategoryType.EXPENSE)
                state.expenseCategories else state.incomeCategories

            item {
                if (displayList.isEmpty()) {
                    LedgerCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(0.dp)) {
                        EmptyState(LedgerIcons.Tag, "No categories", "Tap + to create one.")
                    }
                } else {
                    LedgerCard(
                        modifier = Modifier.fillMaxWidth(),
                        radius = 24.dp,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        displayList.forEach { item -> CategoryRow(item, onClick = { vm.openEdit(item.category) }) }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryRow(item: CategoryWithSpent, onClick: () -> Unit) {
    val c = MaterialTheme.ledger
    val cat = item.category
    val budget = cat.budget
    val fraction = if (budget != null && budget > 0) (item.spent / budget).toFloat() else 0f
    val over = fraction > 1f
    val ops = if (item.opsCount == 1) "1 operation" else "${item.opsCount} operations"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CategoryBubble(cat, size = 40.dp, corner = 13.dp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    cat.name,
                    fontFamily = AppFont, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = c.text,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text(
                    if (budget != null && budget > 0) "${(fraction * 100).toInt()}% · $ops" else ops,
                    fontFamily = AppFont, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = c.muted
                )
            }
            if (budget != null && budget > 0) {
                Text(
                    "${item.spent.formatMoney()} / ${budget.formatMoney()}",
                    fontFamily = AppFont, fontWeight = FontWeight.Bold, fontSize = 14.sp,
                    color = if (over) c.danger else c.text
                )
            }
        }
        if (budget != null && budget > 0) {
            ProgressBar(
                fraction = fraction,
                color = if (over) c.danger else parseHexColor(cat.color),
                height = 4.dp
            )
        }
    }
}

@Composable
private fun CategoryDialog(
    editTarget: Category?,
    defaultType: CategoryType,
    onDismiss: () -> Unit,
    onSave: (name: String, type: CategoryType, color: String, budget: Double?, iconCode: String) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val c = MaterialTheme.ledger
    val isEditing = editTarget != null

    var name by remember(editTarget) { mutableStateOf(editTarget?.name ?: "") }
    var type by remember(editTarget) { mutableStateOf(editTarget?.type ?: defaultType) }
    var color by remember(editTarget) { mutableStateOf(editTarget?.color ?: CATEGORY_PALETTE[0]) }
    var budgetText by remember(editTarget) {
        mutableStateOf(editTarget?.budget?.let { "%.0f".format(it) } ?: "")
    }
    var iconCode by remember(editTarget) { mutableStateOf(editTarget?.iconCode ?: AUTO_ICON_CODE) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        LedgerDialog(
            title = "Delete category?",
            onDismiss = { showDeleteConfirm = false },
            confirmText = "Delete",
            onConfirm = { onDelete?.invoke() },
            destructive = true
        ) {
            Text(
                "Operations in this category are kept but lose their category.",
                style = MaterialTheme.typography.bodyMedium,
                color = c.muted
            )
        }
        return
    }

    LedgerDialog(
        title = if (isEditing) "Edit category" else "New category",
        onDismiss = onDismiss,
        confirmText = "Save",
        onConfirm = { onSave(name, type, color, budgetText.toDoubleOrNull(), iconCode) }
    ) {
        // Live preview of how the category will look in lists
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBubble(categoryIcon(iconCode, name), parseHexColor(color), size = 52.dp, corner = 17.dp)
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    name.ifBlank { "Category name" },
                    fontFamily = AppFont, fontWeight = FontWeight.Bold, fontSize = 16.sp,
                    color = if (name.isBlank()) c.faint else c.text,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text(type.label, fontFamily = AppFont, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = c.muted)
            }
        }

        LedgerTextField(value = name, onValueChange = { name = it }, label = "Name", placeholder = "e.g. Groceries")

        FormSection("Type") {
            if (isEditing) {
                Text(
                    "${type.label} · can't be changed after creation",
                    fontFamily = AppFont, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = c.muted
                )
            } else {
                SegmentedControl(
                    options = listOf(CategoryType.EXPENSE, CategoryType.INCOME),
                    selected = type,
                    onSelect = { type = it },
                    label = { it.label },
                    height = 38.dp,
                    containerColor = c.surface2,
                    bordered = false
                )
            }
        }

        FormSection("Color") {
            ColorPicker(selected = color, onSelect = { color = it })
        }

        FormSection("Icon") {
            IconPicker(
                selected = iconCode,
                name = name,
                color = parseHexColor(color),
                onSelect = { iconCode = it }
            )
        }

        if (type == CategoryType.EXPENSE) {
            LedgerTextField(
                value = budgetText,
                onValueChange = { s -> if (s.all { it.isDigit() }) budgetText = s },
                label = "Monthly budget (optional)",
                placeholder = "No budget",
                keyboardType = KeyboardType.Number
            )
        }

        if (isEditing && onDelete != null) {
            Text(
                "Delete category",
                fontFamily = AppFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = c.danger,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { showDeleteConfirm = true }
                    .padding(vertical = 4.dp)
            )
        }
    }
}

/** "Auto" (icon guessed from the name) plus a grid of every assignable icon. */
@Composable
private fun IconPicker(
    selected: String,
    name: String,
    color: Color,
    onSelect: (String) -> Unit
) {
    val c = MaterialTheme.ledger
    val isAuto = CATEGORY_ICON_OPTIONS.none { it.code == selected }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (isAuto) c.accentSoft else c.surface2)
                .then(if (isAuto) Modifier.border(2.dp, c.accent, RoundedCornerShape(12.dp)) else Modifier)
                .clickable { onSelect(AUTO_ICON_CODE) }
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                categoryIcon(AUTO_ICON_CODE, name),
                contentDescription = null,
                tint = if (isAuto) color else c.muted,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("Auto · by name", fontFamily = AppFont, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = c.text)
        }

        CATEGORY_ICON_OPTIONS.chunked(6).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { option ->
                    val on = option.code == selected
                    val shape = RoundedCornerShape(12.dp)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(shape)
                            .background(if (on) color.copy(alpha = 0.18f) else c.surface2)
                            .then(if (on) Modifier.border(2.dp, color, shape) else Modifier)
                            .clickable { onSelect(option.code) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            option.icon,
                            contentDescription = option.code,
                            tint = if (on) color else c.muted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

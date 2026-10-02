package com.ledger.app.ui.screen.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ledger.app.LedgerApplication
import com.ledger.app.MainActivity
import com.ledger.app.data.CsvImporter
import com.ledger.app.data.prefs.ThemeMode
import com.ledger.app.ui.components.*
import com.ledger.app.ui.theme.AccentColor
import com.ledger.app.ui.theme.AppFont
import com.ledger.app.ui.theme.ledger

@Composable
fun SettingsScreen(
    app: LedgerApplication,
    onSetPin: (() -> Unit)? = null,
    onAccountsClick: () -> Unit,
    onCategoriesClick: () -> Unit
) {
    val vm: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory(app))
    val state by vm.state.collectAsState()
    val c = MaterialTheme.ledger
    val context = LocalContext.current
    val activity = context as? MainActivity

    LaunchedEffect(state.navigateToPinSetup) {
        if (state.navigateToPinSetup) {
            onSetPin?.invoke()
            vm.onNavigatedToPinSetup()
        }
    }

    // Trigger the system file-save dialog as soon as the CSV is ready
    LaunchedEffect(state.exportStatus) {
        val es = state.exportStatus
        if (es is ExportStatus.Ready) {
            activity?.openCsvExport(
                csvContent = es.csv,
                onError    = { vm.showExportError(it) }
            ) ?: vm.showExportError("Error: activity is not available")
            vm.onExportHandled()
        }
    }

    var showClearConfirm by remember { mutableStateOf(false) }
    var showUsdDialog    by remember { mutableStateOf(false) }
    var showEurDialog    by remember { mutableStateOf(false) }
    var showFormatDialog by remember { mutableStateOf(false) }

    // ── Dialogs ──────────────────────────────────────────────────────────────

    when (val status = state.importStatus) {
        is ImportStatus.Done -> ImportDoneDialog(
            imported  = status.imported,
            skipped   = status.skipped,
            errors    = status.errors,
            onDismiss = vm::dismissImport
        )
        is ImportStatus.Error -> LedgerDialog(
            title = "Import failed",
            onDismiss = vm::dismissImport,
            dismissText = "OK"
        ) {
            Text(status.message, style = MaterialTheme.typography.bodyMedium, color = c.danger)
        }
        else -> {}
    }

    val exportStatus = state.exportStatus
    if (exportStatus is ExportStatus.Error) {
        LedgerDialog(title = "Export failed", onDismiss = vm::dismissExport, dismissText = "OK") {
            Text(exportStatus.message, style = MaterialTheme.typography.bodyMedium, color = c.danger)
        }
    }

    if (showFormatDialog) CsvFormatDialog(onDismiss = { showFormatDialog = false })

    if (showUsdDialog) RateEditDialog(
        title = "USD / RUB rate", initialValue = state.usdRate,
        onDismiss = { showUsdDialog = false },
        onSave = { vm.setUsdRate(it); showUsdDialog = false }
    )
    if (showEurDialog) RateEditDialog(
        title = "EUR / RUB rate", initialValue = state.eurRate,
        onDismiss = { showEurDialog = false },
        onSave = { vm.setEurRate(it); showEurDialog = false }
    )

    if (showClearConfirm) LedgerDialog(
        title = "Clear all data?",
        onDismiss = { showClearConfirm = false },
        confirmText = "Delete",
        onConfirm = { vm.clearData(); showClearConfirm = false },
        destructive = true
    ) {
        Text(
            "All operations will be deleted and account balances reset. This can't be undone.",
            style = MaterialTheme.typography.bodyMedium,
            color = c.muted
        )
    }

    // ── Screen body ──────────────────────────────────────────────────────────

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        item { ScreenTitle("Settings") }

        item {
            SettingsGroup("Appearance") {
                Column(
                    modifier = Modifier.padding(vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Theme", fontFamily = AppFont, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = c.text)
                    SegmentedControl(
                        options = ThemeMode.entries.toList(),
                        selected = state.themeMode,
                        onSelect = vm::setThemeMode,
                        label = { mode ->
                            when (mode) {
                                ThemeMode.DARK   -> "Dark"
                                ThemeMode.LIGHT  -> "Light"
                                ThemeMode.SYSTEM -> "System"
                            }
                        },
                        icon = { mode ->
                            when (mode) {
                                ThemeMode.DARK   -> LedgerIcons.Moon
                                ThemeMode.LIGHT  -> LedgerIcons.Sun
                                ThemeMode.SYSTEM -> LedgerIcons.Monitor
                            }
                        },
                        height = 40.dp,
                        containerColor = c.surface2,
                        bordered = false
                    )
                }
                Column(
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val selectedAccent = AccentColor.fromName(state.accentColor)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Accent color", fontFamily = AppFont, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = c.text)
                        Text(selectedAccent.label, fontFamily = AppFont, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = c.muted)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        AccentColor.entries.forEach { accent ->
                            AccentSwatch(
                                accent = accent,
                                selected = accent == selectedAccent,
                                onClick = { vm.setAccentColor(accent.name) }
                            )
                        }
                    }
                }
            }
        }

        item {
            SettingsGroup("Manage") {
                SettingsItem(LedgerIcons.Wallet, "Accounts", value = state.accountsCount.toString(), onClick = onAccountsClick)
                SettingsItem(LedgerIcons.Tag, "Categories & budgets", value = state.categoriesCount.toString(), onClick = onCategoriesClick)
            }
        }

        item {
            SettingsGroup("Currency") {
                Column(
                    modifier = Modifier.padding(vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Show net worth in", fontFamily = AppFont, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = c.text)
                    SegmentedControl(
                        options = listOf("RUB", "USD", "EUR"),
                        selected = state.netWorthCurrency,
                        onSelect = vm::setNetWorthCurrency,
                        label = { it },
                        height = 38.dp,
                        containerColor = c.surface2,
                        bordered = false
                    )
                }
                SettingsItem(LedgerIcons.Coins, "USD / RUB", value = "%.2f".format(state.usdRate), onClick = { showUsdDialog = true })
                SettingsItem(LedgerIcons.Coins, "EUR / RUB", value = "%.2f".format(state.eurRate), onClick = { showEurDialog = true })
                SettingsItem(
                    LedgerIcons.Globe,
                    "Look up today's rates",
                    subtitle = "Opens the browser",
                    onClick = {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=USD+RUB+EUR+rate+today"))
                        )
                    }
                )
            }
        }

        item {
            SettingsGroup("Data") {
                val isImporting = state.importStatus is ImportStatus.Importing
                SettingsItem(
                    LedgerIcons.Upload,
                    "Import CSV",
                    subtitle = if (isImporting) "Importing…" else "Bulk-add operations from a file",
                    onClick = {
                        if (!isImporting) {
                            activity?.openCsvPicker(
                                onResult = { uri -> uri?.let { vm.importCsv(it) } },
                                onError  = { vm.showImportError(it) }
                            ) ?: vm.showImportError("Error: activity is not available")
                        }
                    }
                )
                SettingsItem(LedgerIcons.FileText, "CSV format", subtitle = "Columns and an example file", onClick = { showFormatDialog = true })
                val isExporting = state.exportStatus is ExportStatus.Building
                SettingsItem(
                    LedgerIcons.Download,
                    "Export CSV",
                    subtitle = if (isExporting) "Preparing the file…" else "Save all operations to a file",
                    onClick = { if (!isExporting) vm.startExport() }
                )
                SettingsItem(
                    LedgerIcons.Trash,
                    "Clear data",
                    subtitle = "Delete all operations",
                    danger = true,
                    onClick = { showClearConfirm = true }
                )
            }
        }

        item {
            SettingsGroup("Security") {
                SettingsItem(
                    LedgerIcons.Lock,
                    "PIN lock",
                    subtitle = if (state.pinEnabled) "On" else "Off",
                    trailing = { LedgerSwitch(checked = state.pinEnabled, onCheckedChange = { vm.togglePin() }) }
                )
                SettingsItem(
                    LedgerIcons.Fingerprint,
                    "Biometric unlock",
                    subtitle = if (state.pinEnabled) (if (state.biometricEnabled) "On" else "Off") else "Requires a PIN",
                    trailing = {
                        LedgerSwitch(
                            checked = state.biometricEnabled,
                            onCheckedChange = { vm.toggleBiometric() },
                            enabled = state.pinEnabled
                        )
                    }
                )
            }
        }

        item {
            Text(
                "Leager · v1.0.0",
                fontFamily = AppFont,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = c.faint,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

// ── Building blocks ─────────────────────────────────────────────────────────

@Composable
private fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        GroupLabel(title)
        LedgerCard(
            modifier = Modifier.fillMaxWidth(),
            radius = 24.dp,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            content = content
        )
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    value: String? = null,
    danger: Boolean = false,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    val c = MaterialTheme.ledger
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBubble(
            icon,
            color = if (danger) c.danger else c.text,
            size = 38.dp,
            corner = 12.dp,
            background = c.surface2
        )
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontFamily = AppFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = if (danger) c.danger else c.text
            )
            if (subtitle != null) {
                Text(subtitle, fontFamily = AppFont, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = c.muted)
            }
        }
        if (value != null) {
            Text(value, fontFamily = AppFont, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = c.muted)
            Spacer(Modifier.width(8.dp))
        }
        if (trailing != null) {
            trailing()
        } else if (onClick != null) {
            Icon(LedgerIcons.ChevronRight, contentDescription = null, tint = c.faint, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun AccentSwatch(accent: AccentColor, selected: Boolean, onClick: () -> Unit) {
    val c = MaterialTheme.ledger
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .border(2.dp, if (selected) c.text else Color.Transparent, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(accent.color),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(LedgerIcons.Check, contentDescription = accent.label, tint = accent.onColor, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// ── Dialogs ──────────────────────────────────────────────────────────────────

@Composable
private fun ImportDoneDialog(
    imported: Int,
    skipped: Int,
    errors: List<String>,
    onDismiss: () -> Unit
) {
    val c = MaterialTheme.ledger
    LedgerDialog(title = "Import complete", onDismiss = onDismiss, dismissText = "OK") {
        Text(
            "Imported: $imported\nSkipped: $skipped",
            style = MaterialTheme.typography.bodyLarge,
            color = c.text
        )
        if (errors.isNotEmpty()) {
            GroupLabel("Errors")
            Column(
                modifier = Modifier
                    .heightIn(max = 180.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                errors.take(50).forEach { err ->
                    Text("• $err", fontFamily = AppFont, fontSize = 12.sp, color = c.danger)
                }
                if (errors.size > 50) {
                    Text("… and ${errors.size - 50} more", fontFamily = AppFont, fontSize = 12.sp, color = c.faint)
                }
            }
        }
    }
}

@Composable
private fun CsvFormatDialog(onDismiss: () -> Unit) {
    val c = MaterialTheme.ledger
    LedgerDialog(title = "CSV format", onDismiss = onDismiss, dismissText = "Close") {
        Text(
            CsvImporter.FORMAT_DESCRIPTION,
            fontFamily = AppFont,
            fontSize = 13.sp,
            lineHeight = 19.sp,
            color = c.muted
        )
        GroupLabel("Example file")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(c.surface2)
                .padding(12.dp)
        ) {
            Text(
                CsvImporter.TEMPLATE_CSV,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                color = c.text
            )
        }
        Text(
            "Save the file to Downloads (or any folder you can access), then tap “Import CSV” and pick it.",
            fontFamily = AppFont,
            fontSize = 13.sp,
            lineHeight = 19.sp,
            color = c.muted
        )
    }
}

@Composable
private fun RateEditDialog(
    title: String,
    initialValue: Double,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var text by remember { mutableStateOf("%.2f".format(java.util.Locale.US, initialValue)) }
    LedgerDialog(
        title = title,
        onDismiss = onDismiss,
        confirmText = "Save",
        onConfirm = { text.toDoubleOrNull()?.let { onSave(it) } }
    ) {
        LedgerTextField(
            value = text,
            onValueChange = { s ->
                val v = s.replace(',', '.')
                if (v.isEmpty() || v.matches(Regex("\\d*\\.?\\d*"))) text = v
            },
            keyboardType = KeyboardType.Decimal
        )
    }
}

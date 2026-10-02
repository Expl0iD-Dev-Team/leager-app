package com.ledger.app.ui.screen.settings

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ledger.app.LedgerApplication
import com.ledger.app.data.CsvImporter
import com.ledger.app.data.prefs.ThemeMode
import com.ledger.app.domain.model.Transaction
import com.ledger.app.domain.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class ImportStatus {
    object Idle : ImportStatus()
    object Importing : ImportStatus()
    data class Done(val imported: Int, val skipped: Int, val errors: List<String> = emptyList()) : ImportStatus()
    data class Error(val message: String) : ImportStatus()
}

sealed class ExportStatus {
    object Idle : ExportStatus()
    object Building : ExportStatus()
    data class Ready(val csv: String) : ExportStatus()
    data class Error(val message: String) : ExportStatus()
}

data class SettingsState(
    val pinEnabled: Boolean = false,
    val biometricEnabled: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.DARK,
    val accentColor: String = "LIME",
    val accountsCount: Int = 0,
    val categoriesCount: Int = 0,
    val usdRate: Double = 92.0,
    val eurRate: Double = 99.0,
    val netWorthCurrency: String = "RUB",
    val navigateToPinSetup: Boolean = false,
    val importStatus: ImportStatus = ImportStatus.Idle,
    val exportStatus: ExportStatus = ExportStatus.Idle
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as LedgerApplication
    private val security = app.securityManager
    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                combine(security.isPinEnabled, security.isBiometricEnabled) { pin, bio -> pin to bio },
                combine(app.prefsManager.themeMode, app.prefsManager.accentColor) { mode, accent -> mode to accent },
                combine(app.prefsManager.usdRate, app.prefsManager.eurRate, app.prefsManager.netWorthCurrency) { usd, eur, cur -> Triple(usd, eur, cur) },
                combine(app.accountRepo.getActiveAccounts(), app.categoryRepo.getAll()) { accs, cats -> accs.size to cats.size }
            ) { sec, look, money, counts ->
                _state.value.copy(
                    pinEnabled = sec.first, biometricEnabled = sec.second,
                    themeMode = look.first, accentColor = look.second,
                    usdRate = money.first, eurRate = money.second, netWorthCurrency = money.third,
                    accountsCount = counts.first, categoriesCount = counts.second,
                    navigateToPinSetup = false
                )
            }.collect { _state.value = it }
        }
    }

    fun togglePin() {
        viewModelScope.launch {
            if (_state.value.pinEnabled) {
                security.clearPin()
            } else {
                _state.value = _state.value.copy(navigateToPinSetup = true)
            }
        }
    }

    fun onNavigatedToPinSetup() {
        _state.value = _state.value.copy(navigateToPinSetup = false)
    }

    fun toggleBiometric() {
        viewModelScope.launch {
            security.setBiometricEnabled(!_state.value.biometricEnabled)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { app.prefsManager.setThemeMode(mode) }
    }

    fun setAccentColor(name: String) {
        viewModelScope.launch { app.prefsManager.setAccentColor(name) }
    }

    fun setUsdRate(rate: Double) {
        viewModelScope.launch { app.prefsManager.setUsdRate(rate) }
    }

    fun setEurRate(rate: Double) {
        viewModelScope.launch { app.prefsManager.setEurRate(rate) }
    }

    fun setNetWorthCurrency(currency: String) {
        viewModelScope.launch { app.prefsManager.setNetWorthCurrency(currency) }
    }

    // ── Import ───────────────────────────────────────────────────────────────

    fun importCsv(uri: Uri) {
        viewModelScope.launch {
            _state.value = _state.value.copy(importStatus = ImportStatus.Importing)
            try {
                val result = withContext(Dispatchers.IO) {
                    val stream = app.contentResolver.openInputStream(uri)
                        ?: throw Exception("Could not open the file")
                    stream.use { s ->
                        CsvImporter(app.accountRepo, app.categoryRepo, app.transactionRepo).import(s)
                    }
                }
                _state.value = _state.value.copy(
                    importStatus = ImportStatus.Done(result.imported, result.skipped, result.errors)
                )
            } catch (e: Throwable) {
                _state.value = _state.value.copy(
                    importStatus = ImportStatus.Error(e.message ?: "Unknown error")
                )
            }
        }
    }

    fun showImportError(message: String) {
        _state.value = _state.value.copy(importStatus = ImportStatus.Error(message))
    }

    fun dismissImport() {
        _state.value = _state.value.copy(importStatus = ImportStatus.Idle)
    }

    // ── Export ───────────────────────────────────────────────────────────────

    fun startExport() {
        if (_state.value.exportStatus is ExportStatus.Building) return
        _state.value = _state.value.copy(exportStatus = ExportStatus.Building)
        viewModelScope.launch {
            try {
                val csv = withContext(Dispatchers.IO) { buildCsv() }
                _state.value = _state.value.copy(exportStatus = ExportStatus.Ready(csv))
            } catch (e: Throwable) {
                _state.value = _state.value.copy(
                    exportStatus = ExportStatus.Error(e.message ?: "Could not build the file")
                )
            }
        }
    }

    fun onExportHandled() {
        _state.value = _state.value.copy(exportStatus = ExportStatus.Idle)
    }

    fun showExportError(message: String) {
        _state.value = _state.value.copy(exportStatus = ExportStatus.Error(message))
    }

    fun dismissExport() {
        _state.value = _state.value.copy(exportStatus = ExportStatus.Idle)
    }

    private suspend fun buildCsv(): String {
        val txns = app.transactionRepo.getAll().first()
            .sortedWith(compareByDescending<Transaction> { it.date }.thenByDescending { it.time })
        val accountMap = app.accountRepo.getAllAccounts().first().associateBy { it.id }
        val categoryMap = app.categoryRepo.getAll().first().associateBy { it.id }

        val sb = StringBuilder()
        sb.append("uid,date,amount,type,account,to_account,category,note\n")

        txns.forEach { tx ->
            val accName  = accountMap[tx.accountId]?.name ?: ""
            val toAcc    = tx.toAccountId?.let { accountMap[it]?.name } ?: ""
            val catName  = categoryMap[tx.categoryId]?.name ?: ""
            val typeStr  = when (tx.type) {
                TransactionType.INCOME   -> "income"
                TransactionType.EXPENSE  -> "expense"
                TransactionType.TRANSFER -> "transfer"
            }
            val amount = "%.2f".format(Math.abs(tx.amount))
            sb.append("${esc(tx.id)},${tx.date},${amount},${typeStr},${esc(accName)},${esc(toAcc)},${esc(catName)},${esc(tx.note)}\n")
        }
        return sb.toString()
    }

    private fun esc(v: String): String =
        if (v.contains(',') || v.contains('"') || v.contains('\n'))
            "\"${v.replace("\"", "\"\"")}\""
        else v

    // ── Clear data ───────────────────────────────────────────────────────────

    fun clearData() {
        viewModelScope.launch {
            app.transactionRepo.deleteAll()
            app.accountRepo.resetAllBalances()
        }
    }

    class Factory(private val app: LedgerApplication) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
            SettingsViewModel(app) as T
    }
}

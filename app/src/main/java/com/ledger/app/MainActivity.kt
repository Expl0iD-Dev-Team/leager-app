package com.ledger.app

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ledger.app.data.prefs.ThemeMode
import com.ledger.app.ui.components.LedgerBottomNav
import com.ledger.app.ui.components.NavTab
import com.ledger.app.ui.navigation.LedgerNavHost
import com.ledger.app.ui.navigation.Routes
import com.ledger.app.ui.theme.AccentColor
import com.ledger.app.ui.theme.LedgerTheme
import com.ledger.app.ui.theme.ledger
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : FragmentActivity() {

    // Fixed request codes — always within 16 bits, bypasses ActivityResultRegistry entirely
    companion object {
        private const val RC_CSV_PICK   = 1001
        private const val RC_STORAGE    = 1002
        private const val RC_CSV_EXPORT = 1003
    }

    private var onCsvResult        : ((Uri?) -> Unit)? = null
    private var onCsvError         : ((String) -> Unit)? = null
    private var pendingExportCsv   : String? = null
    private var onExportError      : ((String) -> Unit)? = null

    /**
     * Opens the system file picker for CSV import.
     * Requests READ_EXTERNAL_STORAGE on Android 6–12 if not yet granted.
     */
    fun openCsvPicker(onResult: (Uri?) -> Unit, onError: (String) -> Unit) {
        onCsvResult = onResult
        onCsvError  = onError

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), RC_STORAGE)
        } else {
            launchFilePicker()
        }
    }

    fun openCsvExport(csvContent: String, onError: (String) -> Unit) {
        pendingExportCsv = csvContent
        onExportError    = onError
        try {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/csv"
                putExtra(Intent.EXTRA_TITLE,
                    "ledger_export_${java.time.LocalDate.now()}.csv")
            }
            @Suppress("DEPRECATION")
            startActivityForResult(intent, RC_CSV_EXPORT)
        } catch (e: Exception) {
            onError("Could not open the file manager: ${e.localizedMessage ?: e.javaClass.simpleName}")
            pendingExportCsv = null
            onExportError    = null
        }
    }

    private fun launchFilePicker() {
        try {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            @Suppress("DEPRECATION")
            startActivityForResult(intent, RC_CSV_PICK)
        } catch (e: Exception) {
            onCsvError?.invoke(
                "Could not open the file manager: ${e.localizedMessage ?: e.javaClass.simpleName}"
            )
            onCsvResult = null
            onCsvError  = null
        }
    }

    @Deprecated("Required for file picker result")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_CSV_PICK) {
            val uri = if (resultCode == Activity.RESULT_OK) data?.data else null
            onCsvResult?.invoke(uri)
            onCsvResult = null
            onCsvError  = null
        }
        if (requestCode == RC_CSV_EXPORT && resultCode == Activity.RESULT_OK) {
            val uri = data?.data
            if (uri != null) {
                try {
                    contentResolver.openOutputStream(uri)?.use { stream ->
                        stream.write(pendingExportCsv!!.toByteArray(Charsets.UTF_8))
                    }
                } catch (e: Exception) {
                    onExportError?.invoke("Could not write the file: ${e.localizedMessage ?: e.javaClass.simpleName}")
                }
            }
            pendingExportCsv = null
            onExportError    = null
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RC_STORAGE) {
            if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                launchFilePicker()
            } else {
                onCsvError?.invoke("File access denied. Allow it in the app settings.")
                onCsvResult = null
                onCsvError  = null
            }
        }
    }

    // ── Lifecycle ────────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val app = application as LedgerApplication

        val initialThemeMode = runBlocking { app.prefsManager.themeMode.first() }
        val initialAccent = runBlocking { app.prefsManager.accentColor.first() }
        val pinEnabled = runBlocking { app.securityManager.isPinEnabled.first() }

        val startDestination = if (pinEnabled) Routes.PIN else Routes.HOME

        setContent {
            val themeMode by app.prefsManager.themeMode.collectAsState(initial = initialThemeMode)
            val accentName by app.prefsManager.accentColor.collectAsState(initial = initialAccent)
            val dark = when (themeMode) {
                ThemeMode.DARK   -> true
                ThemeMode.LIGHT  -> false
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            // Status/navigation bar icons must follow the app theme, not the system one
            DisposableEffect(dark) {
                val transparent = android.graphics.Color.TRANSPARENT
                val style = if (dark) SystemBarStyle.dark(transparent)
                    else SystemBarStyle.light(transparent, transparent)
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                onDispose {}
            }

            LedgerTheme(darkTheme = dark, accent = AccentColor.fromName(accentName)) {
                val navController = rememberNavController()
                val backEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backEntry?.destination?.route

                // Floating nav only on the four top-level tabs
                val showBottomNav = currentRoute != null && currentRoute in setOf(
                    Routes.HOME, Routes.TRANSACTIONS, Routes.STATS, Routes.SETTINGS
                )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomNav) {
                            LedgerBottomNav(
                                currentRoute = currentRoute ?: Routes.HOME,
                                onTabSelected = { tab ->
                                    when (tab) {
                                        is NavTab.Add -> navController.navigate(Routes.ADD_TRANSACTION_BASE)
                                        else -> navController.navigate(tab.route) {
                                            launchSingleTop = true
                                            restoreState = tab.route != Routes.HOME
                                            popUpTo(Routes.HOME) {
                                                saveState = tab.route != Routes.HOME
                                                inclusive = tab.route == Routes.HOME
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    },
                    containerColor = MaterialTheme.ledger.bg
                ) { paddingValues ->
                    LedgerNavHost(
                        navController = navController,
                        app = app,
                        startDestination = startDestination,
                        modifier = Modifier
                            .padding(paddingValues)
                            .imePadding()
                    )
                }
            }
        }
    }
}

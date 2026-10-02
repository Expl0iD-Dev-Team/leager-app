package com.ledger.app.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.appDataStore by preferencesDataStore(name = "app_prefs")

enum class ThemeMode { DARK, LIGHT, SYSTEM }

class PrefsManager(private val context: Context) {

    private object Keys {
        /** Legacy boolean; read only to migrate users who picked light theme before [THEME_MODE] existed. */
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ACCENT_COLOR = stringPreferencesKey("accent_color")
        val USD_RATE = doublePreferencesKey("usd_rate")
        val EUR_RATE = doublePreferencesKey("eur_rate")
        val NET_WORTH_CURRENCY = stringPreferencesKey("net_worth_currency")
    }

    val themeMode: Flow<ThemeMode> = context.appDataStore.data
        .map { prefs ->
            prefs[Keys.THEME_MODE]?.let { stored -> ThemeMode.entries.firstOrNull { it.name == stored } }
                ?: if (prefs[Keys.DARK_THEME] == false) ThemeMode.LIGHT else ThemeMode.DARK
        }

    /** Name of the selected accent (see ui.theme.AccentColor). */
    val accentColor: Flow<String> = context.appDataStore.data
        .map { prefs -> prefs[Keys.ACCENT_COLOR] ?: "LIME" }

    val usdRate: Flow<Double> = context.appDataStore.data
        .map { prefs -> prefs[Keys.USD_RATE] ?: 92.0 }

    val eurRate: Flow<Double> = context.appDataStore.data
        .map { prefs -> prefs[Keys.EUR_RATE] ?: 99.0 }

    val netWorthCurrency: Flow<String> = context.appDataStore.data
        .map { prefs -> prefs[Keys.NET_WORTH_CURRENCY] ?: "RUB" }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.appDataStore.edit { prefs -> prefs[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setAccentColor(name: String) {
        context.appDataStore.edit { prefs -> prefs[Keys.ACCENT_COLOR] = name }
    }

    suspend fun setUsdRate(rate: Double) {
        context.appDataStore.edit { prefs -> prefs[Keys.USD_RATE] = rate }
    }

    suspend fun setEurRate(rate: Double) {
        context.appDataStore.edit { prefs -> prefs[Keys.EUR_RATE] = rate }
    }

    suspend fun setNetWorthCurrency(currency: String) {
        context.appDataStore.edit { prefs -> prefs[Keys.NET_WORTH_CURRENCY] = currency }
    }
}

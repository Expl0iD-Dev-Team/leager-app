package com.ledger.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class LedgerColors(
    val bg: Color,
    val surface: Color,
    val surface2: Color,
    val border: Color,
    val borderStrong: Color,
    val text: Color,
    val muted: Color,
    val faint: Color,
    /** User-selected accent — fills only. */
    val accent: Color,
    /** Text / icons drawn on top of [accent]. */
    val onAccent: Color,
    /** Translucent accent for selected backgrounds. */
    val accentSoft: Color,
    /** Positive amounts (income). */
    val income: Color,
    /** Errors, destructive actions, over-budget. */
    val danger: Color,
    val isDark: Boolean
)

private fun darkColors(accent: AccentColor) = LedgerColors(
    bg           = LedgerBgDark,
    surface      = LedgerSurfDark,
    surface2     = LedgerSurf2Dark,
    border       = LedgerBorderDark,
    borderStrong = LedgerBorderStrongDark,
    text         = LedgerTextDark,
    muted        = LedgerMutedDark,
    faint        = LedgerFaintDark,
    accent       = accent.color,
    onAccent     = accent.onColor,
    accentSoft   = accent.color.copy(alpha = 0.16f),
    income       = LedgerIncomeDark,
    danger       = LedgerDangerDark,
    isDark       = true
)

private fun lightColors(accent: AccentColor) = LedgerColors(
    bg           = LedgerBgLight,
    surface      = LedgerSurfLight,
    surface2     = LedgerSurf2Light,
    border       = LedgerBorderLight,
    borderStrong = LedgerBorderStrongLight,
    text         = LedgerTextLight,
    muted        = LedgerMutedLight,
    faint        = LedgerFaintLight,
    accent       = accent.color,
    onAccent     = accent.onColor,
    accentSoft   = accent.color.copy(alpha = 0.22f),
    income       = LedgerIncomeLight,
    danger       = LedgerDangerLight,
    isDark       = false
)

val LocalLedgerColors = staticCompositionLocalOf { darkColors(AccentColor.LIME) }

private fun buildColorScheme(c: LedgerColors): ColorScheme {
    val base = if (c.isDark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary            = c.accent,
        onPrimary          = c.onAccent,
        secondary          = c.text,
        onSecondary        = c.bg,
        tertiary           = c.danger,
        background         = c.bg,
        surface            = c.surface,
        surfaceVariant     = c.surface2,
        surfaceContainerHigh = c.surface,
        onBackground       = c.text,
        onSurface          = c.text,
        onSurfaceVariant   = c.muted,
        outline            = c.border,
        outlineVariant     = c.borderStrong,
        error              = c.danger,
        onError            = Color.White
    )
}

@Composable
fun LedgerTheme(
    darkTheme: Boolean,
    accent: AccentColor = AccentColor.LIME,
    content: @Composable () -> Unit
) {
    val ledgerColors = if (darkTheme) darkColors(accent) else lightColors(accent)

    CompositionLocalProvider(LocalLedgerColors provides ledgerColors) {
        MaterialTheme(
            colorScheme = buildColorScheme(ledgerColors),
            typography = LedgerTypography,
            content = content
        )
    }
}

// Convenient accessor
val MaterialTheme.ledger: LedgerColors
    @Composable get() = LocalLedgerColors.current

package com.ledger.app.ui.theme

import androidx.compose.ui.graphics.Color

// ── Dark theme ────────────────────────────────────────────────
val LedgerBgDark           = Color(0xFF0F1013)
val LedgerSurfDark         = Color(0xFF181A1F)
val LedgerSurf2Dark        = Color(0xFF22252B)
val LedgerBorderDark       = Color(0x12FFFFFF)   // rgba(255,255,255,0.07)
val LedgerBorderStrongDark = Color(0x24FFFFFF)   // rgba(255,255,255,0.14)
val LedgerTextDark         = Color(0xFFF5F6F8)
val LedgerMutedDark        = Color(0xFF9BA0AA)
val LedgerFaintDark        = Color(0xFF5F646E)
val LedgerIncomeDark       = Color(0xFF3DDC97)
val LedgerDangerDark       = Color(0xFFFF5C5C)

// ── Light theme ───────────────────────────────────────────────
val LedgerBgLight           = Color(0xFFF3F4F7)
val LedgerSurfLight         = Color(0xFFFFFFFF)
val LedgerSurf2Light        = Color(0xFFEBEDF1)
val LedgerBorderLight       = Color(0x140B0C0F)  // rgba(11,12,15,0.08)
val LedgerBorderStrongLight = Color(0x290B0C0F)  // rgba(11,12,15,0.16)
val LedgerTextLight         = Color(0xFF111317)
val LedgerMutedLight        = Color(0xFF6A707C)
val LedgerFaintLight        = Color(0xFFA3A8B2)
val LedgerIncomeLight       = Color(0xFF12A867)
val LedgerDangerLight       = Color(0xFFE5484D)

/**
 * User-selectable accent (Settings → Accent color).
 * The accent is used only as a fill (buttons, chips, active states, charts);
 * text on top of it uses [onColor]. Never use the accent as a text color —
 * several options are unreadable on the light background.
 */
enum class AccentColor(val label: String, val color: Color, val onColor: Color) {
    LIME("Lime", Color(0xFFC5FF4A), Color(0xFF0B0C0F)),
    VIOLET("Violet", Color(0xFF8B7CFF), Color(0xFFFFFFFF)),
    BLUE("Blue", Color(0xFF4C8DFF), Color(0xFFFFFFFF)),
    CORAL("Coral", Color(0xFFFF7A59), Color(0xFF1A0B05)),
    MINT("Mint", Color(0xFF3DDC97), Color(0xFF04140C)),
    AMBER("Amber", Color(0xFFFFB547), Color(0xFF1A1200));

    companion object {
        fun fromName(name: String?): AccentColor =
            entries.firstOrNull { it.name == name } ?: LIME
    }
}

/** Distinct colors offered when creating categories and accounts. */
val CATEGORY_PALETTE = listOf(
    "#4ADE80", "#FB923C", "#38BDF8", "#F472B6",
    "#A78BFA", "#FACC15", "#E879F9", "#818CF8",
    "#2DD4BF", "#F87171", "#94A3B8", "#60A5FA",
    "#34D399", "#22D3EE", "#FBBF24", "#C5FF4A",
    "#FF7A59", "#8B7CFF", "#4C8DFF", "#9CA3AF"
)

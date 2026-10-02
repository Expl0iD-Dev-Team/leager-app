package com.ledger.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.ledger.app.R

private val fontsProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

// Manrope: close to the Figma concept's Plus Jakarta Sans, but with full Cyrillic
// coverage — category names and notes are often Russian.
private val manrope = GoogleFont("Manrope")

/** Single typeface for the whole app (Concept A · Soft Fintech). */
val AppFont = FontFamily(
    Font(googleFont = manrope, fontProvider = fontsProvider, weight = FontWeight.Normal),
    Font(googleFont = manrope, fontProvider = fontsProvider, weight = FontWeight.Medium),
    Font(googleFont = manrope, fontProvider = fontsProvider, weight = FontWeight.SemiBold),
    Font(googleFont = manrope, fontProvider = fontsProvider, weight = FontWeight.Bold),
    Font(googleFont = manrope, fontProvider = fontsProvider, weight = FontWeight.ExtraBold),
)

val LedgerTypography = Typography(
    displayLarge = TextStyle(fontFamily = AppFont, fontWeight = FontWeight.ExtraBold, fontSize = 40.sp, letterSpacing = (-1.2).sp, lineHeight = 46.sp),
    displayMedium = TextStyle(fontFamily = AppFont, fontWeight = FontWeight.ExtraBold, fontSize = 30.sp, letterSpacing = (-0.6).sp, lineHeight = 36.sp),
    titleLarge = TextStyle(fontFamily = AppFont, fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 24.sp),
    titleMedium = TextStyle(fontFamily = AppFont, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = AppFont, fontWeight = FontWeight.Medium, fontSize = 15.sp, lineHeight = 20.sp),
    bodyMedium = TextStyle(fontFamily = AppFont, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = AppFont, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = AppFont, fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = AppFont, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = AppFont, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp, lineHeight = 14.sp)
)

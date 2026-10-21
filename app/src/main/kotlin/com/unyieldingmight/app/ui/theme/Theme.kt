package com.unyieldingmight.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.unyieldingmight.app.R

// Brand palette carried over from the original app (umBlack / umGrey) plus one accent.
val UmBlack = Color(0xFF0E0E0E)
val UmGrey = Color(0xFF4E4E4E)
val Ember = Color(0xFFFF6B2C)
val EmberDark = Color(0xFFC24A12)
val Success = Color(0xFF2E9E5B)
val Warning = Color(0xFFE0A100)

private val DarkColors = darkColorScheme(
    primary = Ember,
    onPrimary = Color(0xFF1A0A00),
    primaryContainer = Color(0xFF3A1A08),
    onPrimaryContainer = Color(0xFFFFDBCB),
    secondary = Color(0xFFD8D8D8),
    onSecondary = UmBlack,
    secondaryContainer = Color(0xFF2A2A2A),
    onSecondaryContainer = Color(0xFFEDEDED),
    tertiary = Color(0xFF8FD3A8),
    background = UmBlack,
    onBackground = Color(0xFFF2F2F2),
    surface = UmBlack,
    onSurface = Color(0xFFF2F2F2),
    surfaceVariant = Color(0xFF242424),
    onSurfaceVariant = Color(0xFFB4B4B4),
    surfaceContainerLowest = Color(0xFF0A0A0A),
    surfaceContainerLow = Color(0xFF151515),
    surfaceContainer = Color(0xFF1A1A1A),
    surfaceContainerHigh = Color(0xFF222222),
    surfaceContainerHighest = Color(0xFF2C2C2C),
    outline = UmGrey,
    outlineVariant = Color(0xFF333333),
    error = Color(0xFFFF6B6B),
    onError = Color(0xFF2B0000),
)

private val LightColors = lightColorScheme(
    primary = EmberDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDBCB),
    onPrimaryContainer = Color(0xFF3A1200),
    secondary = UmBlack,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE6E6E6),
    onSecondaryContainer = UmBlack,
    tertiary = Success,
    background = Color(0xFFFAFAFA),
    onBackground = UmBlack,
    surface = Color(0xFFFAFAFA),
    onSurface = UmBlack,
    surfaceVariant = Color(0xFFEDEDED),
    onSurfaceVariant = UmGrey,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF5F5F5),
    surfaceContainer = Color(0xFFF0F0F0),
    surfaceContainerHigh = Color(0xFFEAEAEA),
    surfaceContainerHighest = Color(0xFFE2E2E2),
    outline = Color(0xFF9A9A9A),
    outlineVariant = Color(0xFFDADADA),
    error = Color(0xFFC62828),
)

private val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

/** Jura (display) and Manrope (body) are the fonts the original app used; fall back to system fonts offline. */
private val Jura = FontFamily(
    Font(GoogleFont("Jura"), fontProvider, FontWeight.Medium),
    Font(GoogleFont("Jura"), fontProvider, FontWeight.Bold),
)
private val Manrope = FontFamily(
    Font(GoogleFont("Manrope"), fontProvider, FontWeight.Normal),
    Font(GoogleFont("Manrope"), fontProvider, FontWeight.Medium),
    Font(GoogleFont("Manrope"), fontProvider, FontWeight.SemiBold),
    Font(GoogleFont("Manrope"), fontProvider, FontWeight.Bold),
    Font(GoogleFont("Manrope"), fontProvider, FontWeight.ExtraBold),
)

private val base = Typography()

private val UmTypography = Typography(
    displayLarge = base.displayLarge.copy(fontFamily = Jura, fontWeight = FontWeight.Bold),
    displayMedium = base.displayMedium.copy(fontFamily = Jura, fontWeight = FontWeight.Bold),
    displaySmall = base.displaySmall.copy(fontFamily = Jura, fontWeight = FontWeight.Bold),
    headlineLarge = base.headlineLarge.copy(fontFamily = Jura, fontWeight = FontWeight.Bold),
    headlineMedium = base.headlineMedium.copy(fontFamily = Jura, fontWeight = FontWeight.Bold),
    headlineSmall = base.headlineSmall.copy(fontFamily = Jura, fontWeight = FontWeight.Bold),
    titleLarge = base.titleLarge.copy(fontFamily = Manrope, fontWeight = FontWeight.ExtraBold),
    titleMedium = base.titleMedium.copy(fontFamily = Manrope, fontWeight = FontWeight.Bold),
    titleSmall = base.titleSmall.copy(fontFamily = Manrope, fontWeight = FontWeight.Bold),
    bodyLarge = base.bodyLarge.copy(fontFamily = Manrope),
    bodyMedium = base.bodyMedium.copy(fontFamily = Manrope),
    bodySmall = base.bodySmall.copy(fontFamily = Manrope),
    labelLarge = base.labelLarge.copy(fontFamily = Manrope, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp),
    labelMedium = base.labelMedium.copy(fontFamily = Manrope, fontWeight = FontWeight.SemiBold),
    labelSmall = base.labelSmall.copy(fontFamily = Manrope, fontWeight = FontWeight.SemiBold),
)

/** Small all-caps overline used above section titles. */
val Overline = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, letterSpacing = 1.6.sp)

private val UmShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

enum class ThemeMode { SYSTEM, LIGHT, DARK }

@Composable
fun UnyieldingMightTheme(mode: ThemeMode = ThemeMode.SYSTEM, content: @Composable () -> Unit) {
    val dark = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = UmTypography,
        shapes = UmShapes,
        content = content,
    )
}

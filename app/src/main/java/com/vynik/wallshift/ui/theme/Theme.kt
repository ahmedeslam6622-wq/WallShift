package com.vynik.wallshift.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Palette ────────────────────────────────────────────────────────────────
// Deep midnight navy base → creates depth beneath frosted glass
val DeepNavy = Color(0xFF040D21)
val MidNavy = Color(0xFF0A1628)
val SurfaceNavy = Color(0xFF0F1E3A)

// Accent: Electric violet-blue with warm lilac highlights
val AccentPrimary = Color(0xFF7B61FF)      // vivid violet
val AccentSecondary = Color(0xFF4FC3F7)    // ice blue
val AccentTertiary = Color(0xFFE040FB)     // magenta spark

// Glass surfaces
val GlassWhite = Color(0x1AFFFFFF)         // 10% white — frosted card bg
val GlassBorder = Color(0x33FFFFFF)        // 20% white — card border
val GlassStrong = Color(0x26FFFFFF)        // 15% — stronger cards

// Text
val TextPrimary = Color(0xFFF0F4FF)
val TextSecondary = Color(0x99F0F4FF)      // 60% — muted labels
val TextDisabled = Color(0x40F0F4FF)       // 25%

// Status
val SuccessGreen = Color(0xFF4CAF82)
val ErrorRed = Color(0xFFFF5B5B)
val WarnAmber = Color(0xFFFFB74D)

private val DarkColorScheme = darkColorScheme(
    primary = AccentPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF3B2E8A),
    onPrimaryContainer = Color(0xFFD4CCFF),
    secondary = AccentSecondary,
    onSecondary = DeepNavy,
    secondaryContainer = Color(0xFF1A4A5C),
    onSecondaryContainer = Color(0xFFB3E5FC),
    tertiary = AccentTertiary,
    background = DeepNavy,
    onBackground = TextPrimary,
    surface = SurfaceNavy,
    onSurface = TextPrimary,
    surfaceVariant = MidNavy,
    onSurfaceVariant = TextSecondary,
    error = ErrorRed,
    outline = GlassBorder,
)

@Composable
fun WallShiftTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = WallShiftTypography,
        content = content
    )
}

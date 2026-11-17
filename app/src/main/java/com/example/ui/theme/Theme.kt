package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LuxuryDarkColorScheme = darkColorScheme(
    primary = PrimaryGold,
    onPrimary = ObsidianBlack,
    primaryContainer = Color(0xFF2B2310),
    onPrimaryContainer = SecondaryGold,
    secondary = SecondaryGold,
    onSecondary = ObsidianBlack,
    secondaryContainer = ElevatedObsidian,
    onSecondaryContainer = TextPrimary,
    tertiary = DeepBronzeGold,
    onTertiary = TextPrimary,
    background = ObsidianBlack,
    onBackground = TextPrimary,
    surface = CharcoalSurface,
    onSurface = TextPrimary,
    surfaceVariant = CardObsidian,
    onSurfaceVariant = TextSecondary,
    outline = BorderGoldMuted,
    outlineVariant = Color(0xFF25221E),
    error = StatusError,
    onError = TextPrimary
)

val StudioShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun AiPhotoStudioTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LuxuryDarkColorScheme,
        typography = StudioTypography,
        shapes = StudioShapes,
        content = content
    )
}

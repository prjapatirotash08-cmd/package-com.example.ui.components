package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val ObsidianBlack = Color(0xFF080808)
val CharcoalSurface = Color(0xFF121212)
val CardObsidian = Color(0xFF181818)
val ElevatedObsidian = Color(0xFF202020)

val PrimaryGold = Color(0xFFD4AF37)
val SecondaryGold = Color(0xFFF5D76E)
val DeepBronzeGold = Color(0xFF9E7B1E)

val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFA5A5A5)
val BorderGoldMuted = Color(0xFF34302A)

val StatusSuccess = Color(0xFF22C55E)
val StatusError = Color(0xFFEF4444)
val StatusWarning = Color(0xFFF59E0B)

val GoldGradientBrush = Brush.linearGradient(
    colors = listOf(PrimaryGold, SecondaryGold, PrimaryGold)
)

val SubtleGoldSurfaceBrush = Brush.verticalGradient(
    colors = listOf(Color(0xFF221D13), CardObsidian)
)

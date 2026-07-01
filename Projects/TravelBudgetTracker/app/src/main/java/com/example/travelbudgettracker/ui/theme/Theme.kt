package com.example.travelbudgettracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val PremiumDarkColors = darkColorScheme(

    primary = Gold,
    onPrimary = Background,

    secondary = GoldLight,
    onSecondary = Background,

    tertiary = Success,

    background = Background,
    onBackground = WhiteText,

    surface = Surface,
    onSurface = WhiteText,

    surfaceVariant = Surface2,
    onSurfaceVariant = GrayText,

    error = Danger,
    onError = WhiteText,

    outline = CardBorder
)

private val PremiumShapes = Shapes()

@Composable
fun TravelBudgetTrackerTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PremiumDarkColors,
        typography = Typography(),
        shapes = PremiumShapes,
        content = content
    )
}

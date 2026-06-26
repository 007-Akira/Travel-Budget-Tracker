package com.example.travelbudgettracker.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = TravelPrimaryDark,
    secondary = TravelSecondaryDark,
    tertiary = TravelTertiaryDark,
    background = TravelOnSurface,
    surface = Color(0xFF1A2030),
    surfaceVariant = Color(0xFF283247),
    onPrimary = Color(0xFF071331),
    onSecondary = Color(0xFF00201C),
    onTertiary = Color(0xFF301400),
    onBackground = Color(0xFFF6F7FB),
    onSurface = Color(0xFFF6F7FB)
)

private val LightColorScheme = lightColorScheme(
    primary = TravelPrimary,
    secondary = TravelSecondary,
    tertiary = TravelTertiary,
    background = TravelBackground,
    surface = TravelSurface,
    surfaceVariant = TravelSurfaceVariant,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = TravelOnSurface,
    onSurface = TravelOnSurface

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun TravelBudgetTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

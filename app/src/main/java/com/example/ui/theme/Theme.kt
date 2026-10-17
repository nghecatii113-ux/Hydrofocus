package com.example.ui.theme

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
    primary = WaterBluePrimaryDark,
    onPrimary = Color.Black,
    primaryContainer = WaterDeepNavyDark,
    onPrimaryContainer = Color.White,
    secondary = WaterBlueSecondaryDark,
    onSecondary = Color.Black,
    tertiary = GoldReward,
    background = WaterBackgroundDark,
    onBackground = Color(0xFFF0F4F8),
    surface = WaterSurfaceDark,
    onSurface = Color(0xFFF0F4F8),
    surfaceVariant = WaterSurfaceVariantDark,
    onSurfaceVariant = Color(0xFFD0D7DE)
)

private val LightColorScheme = lightColorScheme(
    primary = WaterBluePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE1F5FE),
    onPrimaryContainer = WaterDeepNavy,
    secondary = WaterBlueSecondary,
    onSecondary = Color.White,
    tertiary = GoldRewardDark,
    background = Color(0xFFF6FAFD),
    onBackground = Color(0xFF0F1E2A),
    surface = Color.White,
    onSurface = Color(0xFF0F1E2A),
    surfaceVariant = Color(0xFFE8F1F5),
    onSurfaceVariant = Color(0xFF3F5566)
)

@Composable
fun HydroFocusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our handcrafted water palette for consistent branding
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

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) = HydroFocusTheme(darkTheme, dynamicColor, content)

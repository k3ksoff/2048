package com.example.a2048.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Custom colors for the 2048 game
val GameBackground = Color(0xFFBBADA0)
val TileEmpty = Color(0xFFCDC1B4)
val Tile2 = Color(0xFFEEE4DA)
val Tile4 = Color(0xFFEDE0C8)
val Tile8 = Color(0xFFF2B179)
val Tile16 = Color(0xFFF59563)
val Tile32 = Color(0xFFF67C5F)
val Tile64 = Color(0xFFF65E3B)
val Tile128 = Color(0xFFEDCF72)
val Tile256 = Color(0xFFEDCC61)
val Tile512 = Color(0xFFEDC850)
val Tile1024 = Color(0xFFEDC53F)
val Tile2048 = Color(0xFFEDC22E)
val TileSuper = Color(0xFF3C3A32) // For tiles > 2048

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFEDC22E),       // Primary color from 2048
    secondary = Color(0xFFF67C5F),     // Secondary color
    tertiary = Color(0xFFEDCF72),      // Tertiary color
    background = Color(0xFF121212),    // Dark background
    surface = Color(0xFF1E1E1E)        // Dark surface
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFEDC22E),       // Primary color from 2048
    secondary = Color(0xFFF67C5F),     // Secondary color
    tertiary = Color(0xFFEDCF72),      // Tertiary color
    background = Color(0xFFFAFAFA),    // Light background
    surface = Color(0xFFFFFFFF)        // Light surface
)

@Composable
fun GameTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
} 
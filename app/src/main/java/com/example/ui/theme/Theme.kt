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
    primary = MeadowLight,
    onPrimary = Color(0xFF142407),
    primaryContainer = MeadowDark,
    onPrimaryContainer = Color(0xFFD9E8CA),
    secondary = GoldAccent,
    onSecondary = Color(0xFF422006),
    secondaryContainer = Color(0xFF78350F),
    onSecondaryContainer = GoldContainer,
    background = DarkBackground,
    onBackground = Color(0xFFE8F0E4),
    surface = DarkSurface,
    onSurface = Color(0xFFE8F0E4),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFC4D4BE)
)

private val LightColorScheme = lightColorScheme(
    primary = MeadowPrimary,
    onPrimary = Color.White,
    primaryContainer = MeadowContainer,
    onPrimaryContainer = OnMeadowContainer,
    secondary = GoldAccent,
    onSecondary = Color.White,
    secondaryContainer = GoldContainer,
    onSecondaryContainer = Color(0xFF92400E),
    background = LightBackground,
    onBackground = Color(0xFF131910),
    surface = LightSurface,
    onSurface = Color(0xFF131910),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF42503A)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Pure Meadow Green #598234 theme
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

package com.evolvarc.adskipper.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = Gold80,
    tertiary = Amber80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = Gold40,
    tertiary = Amber40

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
fun AdSkipperTheme(
    darkTheme: Boolean = false,  // Force light theme for consistent branding
    dynamicColor: Boolean = false,  // Disable dynamic color to use custom theme
    content: @Composable () -> Unit
) {
    // Always use light color scheme for consistent premium branding
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
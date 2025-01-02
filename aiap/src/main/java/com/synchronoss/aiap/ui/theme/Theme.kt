package com.synchronoss.aiap.ui.theme

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

    primary = ThemeColors.Dark.primary,
    secondary = ThemeColors.Dark.secondary,
    background = ThemeColors.Dark.background,
    onPrimary = ThemeColors.Dark.textHeading,
    onSecondary = ThemeColors.Dark.textBody,
    onBackground = ThemeColors.Dark.textBodyAlt,
)

private val LightColorScheme = lightColorScheme(

        primary = ThemeColors.Light.primary,
        secondary = ThemeColors.Light.secondary,
        background = ThemeColors.Light.background,
        onPrimary = ThemeColors.Light.textHeading,
        onSecondary = ThemeColors.Light.textBody,
        onBackground = ThemeColors.Light.textBodyAlt,
)

@Composable
fun SampleAiAPTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
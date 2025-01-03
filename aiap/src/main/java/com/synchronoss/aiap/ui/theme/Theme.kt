package com.synchronoss.aiap.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext


//

val DarkThemeColors = ThemeLoader.getDarkThemeColors()
val LightThemeColors = ThemeLoader.getThemeColors()

private val LightColorScheme = lightColorScheme(

        primary = LightThemeColors.primary,
        secondary = LightThemeColors.secondary,
        background = LightThemeColors.background,
        onPrimary = LightThemeColors.textHeading,
        onSecondary = LightThemeColors.textBody,
        onBackground = LightThemeColors.textBodyAlt,
        surface = LightThemeColors.surface,
        onSurface = LightThemeColors.onSurface,
        outline = LightThemeColors.outline,
        outlineVariant = LightThemeColors.outlineVariant,
        tertiary = LightThemeColors.tertiary,
        onTertiary = LightThemeColors.onTertiary

)

private val DarkColorScheme = darkColorScheme(
    primary = DarkThemeColors.primary,
    secondary = DarkThemeColors.secondary,
    background = DarkThemeColors.background,
    onPrimary = DarkThemeColors.textHeading,
    onSecondary = DarkThemeColors.textBody,
    onBackground = DarkThemeColors.textBodyAlt,
    surface = DarkThemeColors.surface,
    onSurface = DarkThemeColors.onSurface,
    outline = DarkThemeColors.outline,
    outlineVariant = DarkThemeColors.outlineVariant,
    tertiary = DarkThemeColors.tertiary,
    onTertiary = DarkThemeColors.onTertiary
)


@Composable
fun SampleAiAPTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
//        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
//            val context = LocalContext.current
//            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
//        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
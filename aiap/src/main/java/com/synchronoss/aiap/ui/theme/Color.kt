package com.synchronoss.aiap.ui.theme

import androidx.compose.ui.graphics.Color


sealed class ThemeColors(
    val primary: Color,
    val secondary: Color,
    val background: Color,
    val textHeading: Color,
    val textBody: Color,
    val textBodyAlt: Color,
    val surface: Color,
    val onSurface: Color,
    val outline : Color,
    val outlineVariant : Color,
    val tertiary : Color,
    val onTertiary : Color
) {
    data object Light : ThemeColors(
        primary = Color(0xFF0096D5),
        secondary = Color(0xFFE7F8FF),
        background = Color.White,
        textHeading = Color(0xFF1F2937),
        textBody = Color(0xFF6B7280),
        textBodyAlt = Color(0xFF1F2937),
        surface = Color(0xFFE4FFF4),
        onSurface = Color(0xFF2A7948),
        outline  = Color(0xFFE5E7EB),
        outlineVariant = Color(0xFF0096D5),
        tertiary = Color.White,
        onTertiary = Color(0xFF6B7280)

    )

    data object Dark : ThemeColors(
        primary = Color(0xFF0096D5),
        secondary = Color(0xFF262627),
        background = Color(0xFF0D0D0D),
        textHeading = Color(0xFFC8D2E0),
        textBody = Color(0xFF8C8C8C),
        textBodyAlt = Color(0xFFFEFEFF),
        surface = Color(0xFF166534),
        onSurface = Color(0xFFE4FFF4),
        outline = Color(0xFF262627),
        outlineVariant = Color(0xFF404040),
        tertiary = Color(0xFF212121),
        onTertiary = Color(0xFFFEFEFF)
    )

}
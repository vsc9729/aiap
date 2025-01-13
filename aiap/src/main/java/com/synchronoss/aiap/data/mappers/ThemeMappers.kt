package com.synchronoss.aiap.data.mappers

import com.synchronoss.aiap.data.remote.theme.ThemeDataDto
import com.synchronoss.aiap.domain.models.theme.ThemeInfo
import com.synchronoss.aiap.domain.models.theme.Theme

fun List<ThemeDataDto>.toThemeInfo(): ThemeInfo {
    // Find Light and Dark themes
    val lightTheme = this.find { it.themeName == "Light" }
    val darkTheme = this.find { it.themeName == "Dark" }

    return ThemeInfo(
        light = lightTheme?.let {
            Theme(
                logoUrl = it.logoUrl,
                primary = it.primaryColor,
                secondary = it.secondaryColor
            )
        } ?: Theme(
            logoUrl = "",
            primary = "#ffffff",
            secondary = "#000000"
        ),
        
        dark = darkTheme?.let {
            Theme(
                logoUrl = it.logoUrl,
                primary = it.primaryColor,
                secondary = it.secondaryColor
            )
        } ?: Theme(
            logoUrl = "",
            primary = "#000000",
            secondary = "#ffffff"
        )
    )
}

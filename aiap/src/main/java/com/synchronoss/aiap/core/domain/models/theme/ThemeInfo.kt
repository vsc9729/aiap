package com.synchronoss.aiap.core.domain.models.theme

data class ThemeInfo(
    val light: Theme,
    val dark: Theme
)

data class Theme(
    val logoUrl: String?,
    val primary: String?,
    val secondary: String?,
)



package com.synchronoss.aiap.data.mappers

import com.synchronoss.aiap.data.remote.theme.ThemeDataDto
import com.synchronoss.aiap.domain.models.theme.ThemeInfo
import org.junit.Assert.*
import org.junit.Test

class ThemeMappersTest {

    @Test
    fun `test themes mapping with valid Light and Dark`() {
        // Prepare sample data
        val themesDto = listOf(
            ThemeDataDto(themeName = "Light", logoUrl = "lightLogo", primaryColor = "#123456", secondaryColor = "#abcdef"),
            ThemeDataDto(themeName = "Dark", logoUrl = "darkLogo", primaryColor = "#654321", secondaryColor = "#fedcba")
        )

        // Use toThemeInfo()
        val themeInfo = themesDto.toThemeInfo()

        // Validate Light theme
        assertEquals("lightLogo", themeInfo.light.logoUrl)
        assertEquals("#123456", themeInfo.light.primary)
        assertEquals("#abcdef", themeInfo.light.secondary)

        // Validate Dark theme
        assertEquals("darkLogo", themeInfo.dark.logoUrl)
        assertEquals("#654321", themeInfo.dark.primary)
        assertEquals("#fedcba", themeInfo.dark.secondary)
    }

    @Test
    fun `test themes mapping with missing Dark theme`() {
        // Prepare sample data
        val themesDto = listOf(
            ThemeDataDto(themeName = "Light", logoUrl = "lightLogo", primaryColor = "#123456", secondaryColor = "#abcdef")
        )

        // Use toThemeInfo()
        val themeInfo = themesDto.toThemeInfo()

        // Validate Light theme
        assertEquals("lightLogo", themeInfo.light.logoUrl)
        assertEquals("#123456", themeInfo.light.primary)
        assertEquals("#abcdef", themeInfo.light.secondary)

        // Validate Dark theme
        assertEquals("", themeInfo.dark.logoUrl)
        assertEquals("#000000", themeInfo.dark.primary)
        assertEquals("#ffffff", themeInfo.dark.secondary)
    }
}
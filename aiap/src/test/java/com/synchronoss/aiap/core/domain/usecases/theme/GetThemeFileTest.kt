package com.synchronoss.aiap.core.domain.usecases.theme

import com.synchronoss.aiap.core.domain.models.theme.Theme
import com.synchronoss.aiap.core.domain.models.theme.ThemeInfo
import com.synchronoss.aiap.core.domain.repository.theme.ThemeManager
import com.synchronoss.aiap.utils.Resource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class GetThemeFileTest {
    private lateinit var getThemeFile: GetThemeFile
    private lateinit var themeManager: ThemeManager

    @Before
    fun setUp() {
        themeManager = mockk()
        getThemeFile = GetThemeFile(themeManager)
    }

    @Test
    fun `invoke returns success with theme info`() = runTest {
        // Given
        val mockThemeInfo = ThemeInfo(
            light = Theme(
                logoUrl = "light_logo_url",
                primary = "#0096D5",
                secondary = "#E7F8FF"
            ),
            dark = Theme(
                logoUrl = "dark_logo_url",
                primary = "#0096D5",
                secondary = "#262627"
            )
        )
        coEvery { themeManager.getTheme() } returns Resource.Success(mockThemeInfo)

        // When
        val result = getThemeFile()

        // Then
        assertEquals(Resource.Success(mockThemeInfo), result)
    }

    @Test
    fun `invoke returns error when theme manager fails`() = runTest {
        // Given
        val errorMessage = "Failed to load theme"
        coEvery { themeManager.getTheme() } returns Resource.Error(errorMessage)

        // When
        val result = getThemeFile()

        // Then
        assertEquals(Resource.Error(errorMessage), result)
    }

    @Test
    fun `invoke handles exception from theme manager`() = runTest {
        // Given
        val exception = Exception("Network error")
        coEvery { themeManager.getTheme() } throws exception

        // When
        val result = getThemeFile()

        // Then
        assert(result is Resource.Error)
        assertEquals("Network error", (result as Resource.Error).message)
    }
} 
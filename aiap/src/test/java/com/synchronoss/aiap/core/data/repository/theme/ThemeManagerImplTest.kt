package com.synchronoss.aiap.core.data.repository.theme

import android.content.Context
import android.content.res.AssetManager
import com.synchronoss.aiap.R
import com.synchronoss.aiap.core.data.remote.theme.ThemeDataDto
import com.synchronoss.aiap.core.domain.models.theme.Theme
import com.synchronoss.aiap.core.domain.models.theme.ThemeInfo
import com.synchronoss.aiap.core.domain.usecases.theme.TransformThemeData
import com.synchronoss.aiap.utils.Resource
import com.synchronoss.aiap.utils.Vendors
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ThemeManagerImplTest {
    private lateinit var themeManager: ThemeManagerImpl
    private lateinit var context: Context
    private lateinit var transformThemeData: TransformThemeData
    private lateinit var assetManager: AssetManager

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        transformThemeData = mockk()
        assetManager = mockk()

        every { context.assets } returns assetManager
        every { context.getString(R.string.theme_error_no_theme_file, any()) } returns "No theme file found for vendor: %s"
        every { context.getString(R.string.theme_error_parse_json) } returns "Failed to parse theme JSON"
        every { context.getString(R.string.theme_error_load_json) } returns "Failed to load theme from JSON"
        every { context.getString(R.string.theme_manager_tag) } returns "ThemeManager"

        themeManager = ThemeManagerImpl(
            context = context,
            currentVendor = Vendors.Capsyl,
            transformThemeData = transformThemeData
        )
    }

    @Test
    fun `getTheme successfully loads and transforms theme data`() = runTest {
        // Given
        val jsonContent = """
            [
                {
                    "themeId": "1",
                    "themeName": "light",
                    "logoUrl": "light_logo.png",
                    "primaryColor": "#FF0000",
                    "secondaryColor": "#00FF00"
                },
                {
                    "themeId": "2",
                    "themeName": "dark",
                    "logoUrl": "dark_logo.png",
                    "primaryColor": "#000000",
                    "secondaryColor": "#FFFFFF"
                }
            ]
        """.trimIndent()

        val expectedThemeInfo = ThemeInfo(
            light = Theme(
                logoUrl = "light_logo.png",
                primary = "#FF0000",
                secondary = "#00FF00"
            ),
            dark = Theme(
                logoUrl = "dark_logo.png",
                primary = "#000000",
                secondary = "#FFFFFF"
            )
        )

        val inputStream = ByteArrayInputStream(jsonContent.toByteArray())
        every { assetManager.open(any()) } returns inputStream
        coEvery { transformThemeData(any()) } returns Resource.Success(expectedThemeInfo)

        // When
        val result = themeManager.getTheme()

        // Then
        assertTrue(result is Resource.Success)
        assertEquals(expectedThemeInfo, (result as Resource.Success).data)
        verify { assetManager.open(any()) }
        coVerify { transformThemeData(any()) }
    }

    @Test
    fun `getTheme handles missing theme file`() = runTest {
        // Given
        val errorMessage = "File not found"
        every { assetManager.open(any()) } throws Exception(errorMessage)

        // When
        val result = themeManager.getTheme()

        // Then
        assertTrue(result is Resource.Error)
        assertEquals(errorMessage, (result as Resource.Error).message)
    }

    @Test
    fun `getTheme handles invalid JSON data`() = runTest {
        // Given
        val invalidJson = "invalid json content"
        val inputStream = ByteArrayInputStream(invalidJson.toByteArray())
        every { assetManager.open(any()) } returns inputStream

        // When
        val result = themeManager.getTheme()

        // Then
        assertTrue(result is Resource.Error)
        // Just verify we get some error message
        assertTrue((result as Resource.Error).message?.isNotEmpty() == true, 
            "Error message should not be empty")
    }

    @Test
    fun `getTheme handles transformation error`() = runTest {
        // Given
        val jsonContent = """
            [
                {
                    "themeId": "1",
                    "themeName": "light",
                    "logoUrl": "light_logo.png",
                    "primaryColor": "#FF0000",
                    "secondaryColor": "#00FF00"
                }
            ]
        """.trimIndent()

        val inputStream = ByteArrayInputStream(jsonContent.toByteArray())
        every { assetManager.open(any()) } returns inputStream
        coEvery { transformThemeData(any()) } returns Resource.Error("Transform error")

        // When
        val result = themeManager.getTheme()

        // Then
        assertTrue(result is Resource.Error)
        assertEquals("Transform error", (result as Resource.Error).message)
    }
}
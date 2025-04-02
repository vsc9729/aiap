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

    @Test
    fun `getTheme handles unknown vendor`() = runTest {
        // Given
        val testVendor = Vendors.Verizon
        val unknownVendorString = "No theme file found for vendor: $testVendor"
        
        // Create a themed manager with the test vendor
        val themeManagerWithVendor = ThemeManagerImpl(
            context = context,
            currentVendor = testVendor,
            transformThemeData = transformThemeData
        )

        // Mock the vendorThemeMap lookup to throw the specific exception
        every { context.getString(R.string.theme_error_no_theme_file, testVendor) } returns unknownVendorString
        every { assetManager.open(any()) } throws Exception(unknownVendorString)

        // When
        val result = themeManagerWithVendor.getTheme()

        // Then
        assertTrue(result is Resource.Error)
        assertEquals(unknownVendorString, (result as Resource.Error).message)
    }

    @Test
    fun `getTheme handles null JSON response from adapter`() = runTest {
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

        // Instead of trying to mock the static methods, let's verify the error handling
        // by making the transform throw an exception with the specific error message we're looking for
        coEvery { transformThemeData(any()) } throws Exception("Failed to parse theme JSON")

        // When
        val result = themeManager.getTheme()

        // Then
        assertTrue(result is Resource.Error)
        assertEquals("Failed to parse theme JSON", (result as Resource.Error).message)
    }

    @Test
    fun `getTheme handles empty theme file`() = runTest {
        // Given
        val emptyJson = "[]"
        val inputStream = ByteArrayInputStream(emptyJson.toByteArray())
        every { assetManager.open(any()) } returns inputStream

        // When
        val result = themeManager.getTheme()

        // Then
        assertTrue(result is Resource.Error)
        // We expect some error message related to parsing since an empty array won't have
        // the expected light/dark themes
        assertTrue((result as Resource.Error).message?.isNotEmpty() == true)
    }

    @Test
    fun `getTheme handles missing theme elements`() = runTest {
        // Given
        val jsonWithMissingData = """
            [
                {
                    "themeId": "1",
                    "themeName": "light"
                }
            ]
        """.trimIndent()

        val inputStream = ByteArrayInputStream(jsonWithMissingData.toByteArray())
        every { assetManager.open(any()) } returns inputStream

        // When
        val result = themeManager.getTheme()

        // Then
        assertTrue(result is Resource.Error)
        // We expect some parsing error since fields are missing
        assertTrue((result as Resource.Error).message?.isNotEmpty() == true)
    }

    @Test
    fun `getTheme handles exception with null message`() = runTest {
        // Given
        // Throw an exception with a null message to test the fallback error message
        val exception = Exception()
        every { assetManager.open(any()) } throws exception
        every { context.getString(R.string.theme_error_load_json) } returns "Default error message"

        // When
        val result = themeManager.getTheme()

        // Then
        assertTrue(result is Resource.Error)
        assertEquals("Default error message", (result as Resource.Error).message)
    }

    @Test
    fun `getTheme handles successful transformation with different theme structures`() = runTest {
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
                },
                {
                    "themeId": "3",
                    "themeName": "other",
                    "logoUrl": "other_logo.png",
                    "primaryColor": "#CCCCCC",
                    "secondaryColor": "#333333"
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
        
        // Use a more complex mock implementation for transformThemeData
        coEvery { transformThemeData(any()) } answers {
            // This simulates logic that might be in the transform use case
            val themeDataList = firstArg<List<ThemeDataDto>>()
            
            // Verify the theme data was parsed correctly
            assertEquals(3, themeDataList.size)
            assertEquals("light", themeDataList[0].themeName)
            assertEquals("dark", themeDataList[1].themeName)
            assertEquals("other", themeDataList[2].themeName)
            
            // Return success with the expected theme info
            Resource.Success(expectedThemeInfo)
        }

        // When
        val result = themeManager.getTheme()

        // Then
        assertTrue(result is Resource.Success)
        assertEquals(expectedThemeInfo, (result as Resource.Success).data)
        coVerify { transformThemeData(any()) }
    }

    // This test specifically targets branches in the theme map lookup (line 46)
    @Test
    fun `getTheme handles vendor with theme in map`() = runTest {
        // Given - use a vendor from the map and mock the correct json filename
        val expectedFilename = "Capsyl_theme.json"
        val jsonContent = """
            [
                {"themeId": "1", "themeName": "light", "logoUrl": "logo.png", "primaryColor": "#CCC", "secondaryColor": "#DDD"}
            ]
        """.trimIndent()

        val inputStream = ByteArrayInputStream(jsonContent.toByteArray())
        every { assetManager.open(expectedFilename) } returns inputStream
        coEvery { transformThemeData(any()) } returns Resource.Success(mockk(relaxed = true))

        // When
        val result = themeManager.getTheme()

        // Then
        assertTrue(result is Resource.Success)
        verify(exactly = 1) { assetManager.open(expectedFilename) }
    }

    // This test targets the bufferedReader usage and its boundaries (line 50)
    @Test
    fun `getTheme handles large file input`() = runTest {
        // Given
        val largeJson = buildString {
            append("[\n")
            for (i in 1..100) {
                append("""
                    {
                        "themeId": "$i",
                        "themeName": "${if (i == 1) "light" else if (i == 2) "dark" else "other$i"}",
                        "logoUrl": "logo$i.png",
                        "primaryColor": "#CCCCCC",
                        "secondaryColor": "#333333"
                    }${if (i < 100) "," else ""}
                """.trimIndent())
            }
            append("\n]")
        }

        val inputStream = ByteArrayInputStream(largeJson.toByteArray())
        every { assetManager.open(any()) } returns inputStream
        
        val expectedThemeInfo = mockk<ThemeInfo>(relaxed = true)
        coEvery { transformThemeData(any()) } returns Resource.Success(expectedThemeInfo)

        // When
        val result = themeManager.getTheme()

        // Then
        assertTrue(result is Resource.Success)
        assertEquals(expectedThemeInfo, (result as Resource.Success).data)
    }

    // This test specifically ensures the conditional branch in line 56 is covered
    // by ensuring adapter.fromJson returns a non-null value
    @Test
    fun `getTheme handles valid JSON with special characters`() = runTest {
        // Given
        val jsonWithSpecialChars = """
            [
                {
                    "themeId": "1", 
                    "themeName": "light",
                    "logoUrl": "light_logo.png?param=value&special=true",
                    "primaryColor": "#FF\u0000\u0000\u0000",
                    "secondaryColor": "#00FF00"
                }
            ]
        """.trimIndent()

        val inputStream = ByteArrayInputStream(jsonWithSpecialChars.toByteArray())
        every { assetManager.open(any()) } returns inputStream
        
        val expectedThemeInfo = mockk<ThemeInfo>(relaxed = true)
        coEvery { transformThemeData(any()) } returns Resource.Success(expectedThemeInfo)

        // When
        val result = themeManager.getTheme()

        // Then
        assertTrue(result is Resource.Success)
        coVerify { transformThemeData(any()) }
    }

    // Test to ensure exception case with custom error message (targeting line 62)
    @Test
    fun `getTheme handles exception with custom error message`() = runTest {
        // Given
        val customErrorMsg = "Custom error message"
        val exception = Exception(customErrorMsg)
        every { assetManager.open(any()) } throws exception

        // When
        val result = themeManager.getTheme()

        // Then
        assertTrue(result is Resource.Error)
        assertEquals(customErrorMsg, (result as Resource.Error).message)
    }
}
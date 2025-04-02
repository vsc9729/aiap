package com.synchronoss.aiap.ui.theme

import android.content.Context
import android.graphics.Color
import android.util.Log
import androidx.compose.ui.graphics.Color as ComposeColor
import com.synchronoss.aiap.R
import com.synchronoss.aiap.core.domain.models.theme.Theme
import com.synchronoss.aiap.core.domain.models.theme.ThemeInfo
import com.synchronoss.aiap.core.domain.usecases.theme.GetThemeFile
import com.synchronoss.aiap.core.domain.usecases.theme.ThemeManagerUseCases
import com.synchronoss.aiap.core.domain.usecases.theme.TransformThemeData
import com.synchronoss.aiap.utils.Resource
import com.synchronoss.aiap.utils.LogUtils
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ThemeLoaderTest {
    private lateinit var themeLoader: ThemeLoader
    private lateinit var themeManagerUseCases: ThemeManagerUseCases
    private lateinit var context: Context
    private lateinit var getThemeFile: GetThemeFile
    private lateinit var transformThemeData: TransformThemeData

    companion object {
        private const val DEFAULT_LIGHT_PRIMARY = 0xFF0096D5
        private const val DEFAULT_LIGHT_SECONDARY = 0xFFE7F8FF
        private const val DEFAULT_DARK_PRIMARY = 0xFF0096D5
        private const val DEFAULT_DARK_SECONDARY = 0xFF262627
        private const val TAG = "AIAP:ThemeLoader"
    }

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        mockkStatic(Color::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0

        // Mock Color.parseColor
        every { Color.parseColor("#0096D5") } returns DEFAULT_LIGHT_PRIMARY.toInt()
        every { Color.parseColor("#E7F8FF") } returns DEFAULT_LIGHT_SECONDARY.toInt()
        every { Color.parseColor("#262627") } returns DEFAULT_DARK_SECONDARY.toInt()

        context = mockk(relaxed = true)
        getThemeFile = mockk()
        transformThemeData = mockk()
        themeManagerUseCases = ThemeManagerUseCases(getThemeFile, transformThemeData)

        // Mock string resources
        every { context.getString(R.string.theme_error_parse_json) } returns "Failed to parse theme JSON"
        every { context.getString(R.string.theme_error_load_json) } returns "Failed to load theme from JSON"
        every { context.getString(R.string.theme_error_load_json, any()) } returns "Failed to load theme from JSON: {error}"
        
        // Mock color resources
        every { context.getColor(R.string.theme_default_light_primary) } returns DEFAULT_LIGHT_PRIMARY.toInt()
        every { context.getColor(R.string.theme_default_light_secondary) } returns DEFAULT_LIGHT_SECONDARY.toInt()
        every { context.getColor(R.string.theme_default_dark_primary) } returns DEFAULT_DARK_PRIMARY.toInt()
        every { context.getColor(R.string.theme_default_dark_secondary) } returns DEFAULT_DARK_SECONDARY.toInt()

        themeLoader = ThemeLoader(themeManagerUseCases, context)
    }



    @Test
    fun `loadTheme handles error response`() = runTest {
        // Given
        val errorMessage = "Failed to load theme"
        coEvery { themeManagerUseCases.getThemeFile() } returns Resource.Error(errorMessage)

        // When
        themeLoader.loadTheme()

        // Then
        val lightTheme = themeLoader.getThemeColors()
        val darkTheme = themeLoader.getDarkThemeColors()

        // Verify default values are used
        assertEquals(ComposeColor(DEFAULT_LIGHT_PRIMARY), lightTheme.themeColors.primary)
        assertEquals(ComposeColor(DEFAULT_LIGHT_SECONDARY), lightTheme.themeColors.secondary)
        assertEquals(ComposeColor(DEFAULT_DARK_PRIMARY), darkTheme.themeColors.primary)
        assertEquals(ComposeColor(DEFAULT_DARK_SECONDARY), darkTheme.themeColors.secondary)
        assertEquals(ComposeColor.White, lightTheme.themeColors.background)
        assertEquals(ComposeColor(0xFF0D0D0D), darkTheme.themeColors.background)
        assertEquals(null, lightTheme.logoUrl)
        assertEquals(null, darkTheme.logoUrl)
        
        verify { Log.e(TAG, "Failed to parse theme JSON") }
    }

    @Test
    fun `loadTheme handles exception`() = runTest {
        // Given
        val exception = Exception("Network error")
        coEvery { themeManagerUseCases.getThemeFile() } throws exception

        // When
        themeLoader.loadTheme()

        // Then
        verify { Log.e(TAG, "Failed to load theme from JSON", exception) }
        
        // Verify default values are used
        val lightTheme = themeLoader.getThemeColors()
        val darkTheme = themeLoader.getDarkThemeColors()
        assertEquals(ComposeColor(DEFAULT_LIGHT_PRIMARY), lightTheme.themeColors.primary)
        assertEquals(ComposeColor(DEFAULT_LIGHT_SECONDARY), lightTheme.themeColors.secondary)
        assertEquals(ComposeColor(DEFAULT_DARK_PRIMARY), darkTheme.themeColors.primary)
        assertEquals(ComposeColor(DEFAULT_DARK_SECONDARY), darkTheme.themeColors.secondary)
        assertEquals(null, lightTheme.logoUrl)
        assertEquals(null, darkTheme.logoUrl)
    }

    @Test
    fun `getThemeColors returns correct default values when theme not loaded`() {
        // When
        val themeWithLogo = themeLoader.getThemeColors()

        // Then
        assertNotNull(themeWithLogo.themeColors)
        assertEquals(ComposeColor(DEFAULT_LIGHT_PRIMARY), themeWithLogo.themeColors.primary)
        assertEquals(ComposeColor(DEFAULT_LIGHT_SECONDARY), themeWithLogo.themeColors.secondary)
        assertEquals(ComposeColor.White, themeWithLogo.themeColors.background)
        assertEquals(null, themeWithLogo.logoUrl)
    }

    @Test
    fun `getDarkThemeColors returns correct default values when theme not loaded`() {
        // When
        val darkThemeWithLogo = themeLoader.getDarkThemeColors()

        // Then
        assertNotNull(darkThemeWithLogo.themeColors)
        assertEquals(ComposeColor(DEFAULT_DARK_PRIMARY), darkThemeWithLogo.themeColors.primary)
        assertEquals(ComposeColor(DEFAULT_DARK_SECONDARY), darkThemeWithLogo.themeColors.secondary)
        assertEquals(ComposeColor(0xFF0D0D0D), darkThemeWithLogo.themeColors.background)
        assertEquals(null, darkThemeWithLogo.logoUrl)
    }

    @Test
    fun `loadTheme handles null theme info`() = runTest {
        // Given
        val emptyTheme = ThemeInfo(
            light = Theme(logoUrl = null, primary = null, secondary = null),
            dark = Theme(logoUrl = null, primary = null, secondary = null)
        )
        coEvery { themeManagerUseCases.getThemeFile() } returns Resource.Success(emptyTheme)
        coEvery { themeManagerUseCases.getThemeFile() } returns Resource.Success(emptyTheme)

        // When
        themeLoader.loadTheme()

        // Then
        val lightTheme = themeLoader.getThemeColors()
        val darkTheme = themeLoader.getDarkThemeColors()

        // Verify default values are used
        assertEquals(ComposeColor(DEFAULT_LIGHT_PRIMARY), lightTheme.themeColors.primary)
        assertEquals(ComposeColor(DEFAULT_LIGHT_SECONDARY), lightTheme.themeColors.secondary)
        assertEquals(ComposeColor(DEFAULT_DARK_PRIMARY), darkTheme.themeColors.primary)
        assertEquals(ComposeColor(DEFAULT_DARK_SECONDARY), darkTheme.themeColors.secondary)
        assertEquals(null, lightTheme.logoUrl)
        assertEquals(null, darkTheme.logoUrl)
    }

    @Test
    fun `getDarkThemeColors handles null secondary color`() = runTest {
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
                secondary = null  // This will trigger the null branch
            )
        )
        coEvery { themeManagerUseCases.getThemeFile() } returns Resource.Success(mockThemeInfo)
        themeLoader.loadTheme()
        
        // When
        val result = themeLoader.getDarkThemeColors()
        
        // Then
        assertEquals(ComposeColor(DEFAULT_DARK_PRIMARY), result.themeColors.primary)
        assertEquals(ComposeColor(DEFAULT_DARK_SECONDARY), result.themeColors.secondary)
    }
    
    @Test
    fun `loadTheme handles data with null theme info fields`() = runTest {
        // Create a mocked Resource.Success with null data
        val mockResource = mockk<Resource.Success<ThemeInfo>>()
        every { mockResource.data } returns null
        
        // Mock the getThemeFile call
        coEvery { themeManagerUseCases.getThemeFile() } returns mockResource
        
        // When
        themeLoader.loadTheme()
        
        // Then
        val lightTheme = themeLoader.getThemeColors()
        val darkTheme = themeLoader.getDarkThemeColors()
        
        // Verify default values are used
        assertEquals(ComposeColor(DEFAULT_LIGHT_PRIMARY), lightTheme.themeColors.primary)
        assertEquals(ComposeColor(DEFAULT_LIGHT_SECONDARY), lightTheme.themeColors.secondary)
        assertEquals(null, lightTheme.logoUrl)
        assertEquals(null, darkTheme.logoUrl)
    }

    @Test
    fun `getLogoUrlDark returns the dark logo URL - direct access`() {
        // Given
        val themeLoader = spyk(ThemeLoader(themeManagerUseCases, context))
        every { themeLoader.logoUrlDark } returns "dark_logo_url"
        
        // When - Direct access via the getter
        val result = themeLoader.logoUrlDark
        
        // Then
        assertEquals("dark_logo_url", result)
    }
    
    @Test
    fun `getLogoUrlLight returns the light logo URL - direct access`() {
        // Given
        val themeLoader = spyk(ThemeLoader(themeManagerUseCases, context))
        every { themeLoader.logoUrlLight } returns "light_logo_url"
        
        // When - Direct access via the getter
        val result = themeLoader.logoUrlLight
        
        // Then
        assertEquals("light_logo_url", result)
    }

    @Test
    fun `setLogoUrlDark sets the dark logo URL`() {
        // Given
        val themeLoader = ThemeLoader(themeManagerUseCases, context)
        
        // When
        themeLoader.logoUrlDark = "new_dark_logo_url"
        
        // Then
        assertEquals("new_dark_logo_url", themeLoader.logoUrlDark)
    }
    
    @Test
    fun `setLogoUrlLight sets the light logo URL`() {
        // Given
        val themeLoader = ThemeLoader(themeManagerUseCases, context)
        
        // When
        themeLoader.logoUrlLight = "new_light_logo_url"
        
        // Then
        assertEquals("new_light_logo_url", themeLoader.logoUrlLight)
    }
}
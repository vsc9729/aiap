package com.synchronoss.aiap.ui.theme

import android.util.Log
import androidx.compose.ui.graphics.Color
import com.synchronoss.aiap.domain.models.theme.Theme
import com.synchronoss.aiap.domain.models.theme.ThemeInfo
import com.synchronoss.aiap.domain.usecases.theme.GetThemeApi
import com.synchronoss.aiap.domain.usecases.theme.ThemeManagerUseCases
import com.synchronoss.aiap.utils.CacheManager
import com.synchronoss.aiap.utils.Resource
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ThemeLoaderTest {
    private lateinit var themeLoader: ThemeLoader
    private lateinit var themeManagerUseCases: ThemeManagerUseCases
    private lateinit var cacheManager: CacheManager
    private lateinit var getThemeApi: GetThemeApi

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0

        getThemeApi = mockk()
        themeManagerUseCases = ThemeManagerUseCases(getThemeApi)
        cacheManager = mockk()

        themeLoader = ThemeLoader(themeManagerUseCases, cacheManager)
    }

    @Test
    fun `loadTheme successfully loads theme from cache`() = runTest {
        // Given
        mockkStatic(android.graphics.Color::class)
        every { android.graphics.Color.parseColor("#0096D5") } returns 0xFF0096D5.toInt()
        every { android.graphics.Color.parseColor("#E7F8FF") } returns 0xFFE7F8FF.toInt()
        every { android.graphics.Color.parseColor("#262627") } returns 0xFF262627.toInt()

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

        coEvery {
            cacheManager.getCachedDataWithTimestamp<ThemeInfo>(
                key = "theme_cache",
                currentTimestamp = any(),
                fetchFromNetwork = any(),
                serialize = any(),
                deserialize = any()
            )
        } returns Resource.Success(mockThemeInfo)

        // When
        themeLoader.loadTheme()

        // Then
        val lightTheme = themeLoader.getThemeColors()
        val darkTheme = themeLoader.getDarkThemeColors()

        assertEquals("light_logo_url", lightTheme.logoUrl)
        assertEquals("dark_logo_url", darkTheme.logoUrl)
        assertEquals(Color(0xFF0096D5), lightTheme.themeColors.primary)
        assertEquals(Color(0xFFE7F8FF), lightTheme.themeColors.secondary)
        assertEquals(Color(0xFF0096D5), darkTheme.themeColors.primary)
        assertEquals(Color(0xFF262627), darkTheme.themeColors.secondary)
    }

    @Test
    fun `loadTheme handles error response`() = runTest {
        // Given
        coEvery {
            cacheManager.getCachedDataWithTimestamp<ThemeInfo>(
                key = "theme_cache",
                currentTimestamp = any(),
                fetchFromNetwork = any(),
                serialize = any(),
                deserialize = any()
            )
        } returns Resource.Error("Failed to load theme")

        // When
        themeLoader.loadTheme()

        // Then
        val lightTheme = themeLoader.getThemeColors()
        val darkTheme = themeLoader.getDarkThemeColors()

        // Verify default values are used
        assertEquals(Color(0xFF0096D5), lightTheme.themeColors.primary)
        assertEquals(Color(0xFFE7F8FF), lightTheme.themeColors.secondary)
        assertEquals(Color(0xFF0096D5), darkTheme.themeColors.primary)
        assertEquals(Color(0xFF262627), darkTheme.themeColors.secondary)
    }

    @Test
    fun `loadTheme handles exception`() = runTest {
        // Given
        coEvery {
            cacheManager.getCachedDataWithTimestamp<ThemeInfo>(
                key = any(),
                currentTimestamp = any(),
                fetchFromNetwork = any(),
                serialize = any(),
                deserialize = any()
            )
        } throws Exception("Network error")

        // When
        themeLoader.loadTheme()

        // Then
        verify { Log.e("ThemeLoader", "Error loading theme", any()) }
    }

    @Test
    fun `getThemeColors returns correct default values when theme not loaded`() {
        // When
        val themeWithLogo = themeLoader.getThemeColors()

        // Then
        assertNotNull(themeWithLogo.themeColors)
        assertEquals(Color(0xFF0096D5), themeWithLogo.themeColors.primary)
        assertEquals(Color(0xFFE7F8FF), themeWithLogo.themeColors.secondary)
        assertEquals(Color.White, themeWithLogo.themeColors.background)
    }

    @Test
    fun `getDarkThemeColors returns correct default values when theme not loaded`() {
        // When
        val darkThemeWithLogo = themeLoader.getDarkThemeColors()

        // Then
        assertNotNull(darkThemeWithLogo.themeColors)
        assertEquals(Color(0xFF0096D5), darkThemeWithLogo.themeColors.primary)
        assertEquals(Color(0xFF262627), darkThemeWithLogo.themeColors.secondary)
        assertEquals(Color(0xFF0D0D0D), darkThemeWithLogo.themeColors.background)
    }

    @Test
    fun `loadTheme successfully fetches from network when cache is empty`() = runTest {
        // Given
        mockkStatic(android.graphics.Color::class)
        every { android.graphics.Color.parseColor("#0096D5") } returns 0xFF0096D5.toInt()
        every { android.graphics.Color.parseColor("#E7F8FF") } returns 0xFFE7F8FF.toInt()
        every { android.graphics.Color.parseColor("#262627") } returns 0xFF262627.toInt()

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

        coEvery { themeManagerUseCases.getThemeApi() } returns Resource.Success(mockThemeInfo)

        coEvery {
            cacheManager.getCachedDataWithTimestamp<ThemeInfo>(
                key = "theme_cache",
                currentTimestamp = any(),
                fetchFromNetwork = any(),
                serialize = any(),
                deserialize = any()
            )
        } coAnswers {
            val fetch = thirdArg<(suspend () -> Resource<ThemeInfo>)>()
            fetch()
        }

        // When
        themeLoader.loadTheme()

        // Then
        val lightTheme = themeLoader.getThemeColors()
        val darkTheme = themeLoader.getDarkThemeColors()

        // Verify logo URLs are set
        assertEquals("light_logo_url", lightTheme.logoUrl)
        assertEquals("dark_logo_url", darkTheme.logoUrl)

        // Verify network fetch was called
        coVerify { themeManagerUseCases.getThemeApi() }

        // Verify colors are set correctly
        assertEquals(Color(0xFF0096D5), lightTheme.themeColors.primary)
        assertEquals(Color(0xFFE7F8FF), lightTheme.themeColors.secondary)
        assertEquals(Color(0xFF0096D5), darkTheme.themeColors.primary)
        assertEquals(Color(0xFF262627), darkTheme.themeColors.secondary)
    }
} 
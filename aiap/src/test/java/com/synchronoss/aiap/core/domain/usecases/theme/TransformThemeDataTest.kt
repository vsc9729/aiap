package com.synchronoss.aiap.core.domain.usecases.theme

import android.content.Context
import com.synchronoss.aiap.R
import com.synchronoss.aiap.core.data.remote.theme.ThemeDataDto
import com.synchronoss.aiap.core.domain.models.theme.Theme
import com.synchronoss.aiap.core.domain.models.theme.ThemeInfo
import com.synchronoss.aiap.utils.Resource
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TransformThemeDataTest {
    private lateinit var context: Context
    private lateinit var transformThemeData: TransformThemeData

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        transformThemeData = TransformThemeData(context)
    }

    @Test
    fun `transform returns success with valid theme data`() {
        // Given
        val themeDataList = listOf(
            ThemeDataDto(
                themeId = "1",
                themeName = "light",
                logoUrl = "light_logo.png",
                primaryColor = "#FF0000",
                secondaryColor = "#00FF00"
            ),
            ThemeDataDto(
                themeId = "2",
                themeName = "dark",
                logoUrl = "dark_logo.png",
                primaryColor = "#000000",
                secondaryColor = "#FFFFFF"
            )
        )
        every { context.getString(R.string.theme_light) } returns "light"
        every { context.getString(R.string.theme_dark) } returns "dark"

        // When
        val result = transformThemeData(themeDataList)

        // Then
        val expected = ThemeInfo(
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
        assertEquals(Resource.Success<ThemeInfo>(expected), result)
    }

    @Test
    fun `transform returns success with missing dark theme`() {
        // Given
        val themeDataList = listOf(
            ThemeDataDto(
                themeId = "1",
                themeName = "light",
                logoUrl = "light_logo.png",
                primaryColor = "#FF0000",
                secondaryColor = "#00FF00"
            )
        )
        every { context.getString(R.string.theme_light) } returns "light"
        every { context.getString(R.string.theme_dark) } returns "dark"
        every { context.getString(R.string.theme_default_dark_primary) } returns "#000000"
        every { context.getString(R.string.theme_default_dark_secondary) } returns "#FFFFFF"

        // When
        val result = transformThemeData(themeDataList)

        // Then
        val expected = ThemeInfo(
            light = Theme(
                logoUrl = "light_logo.png",
                primary = "#FF0000",
                secondary = "#00FF00"
            ),
            dark = Theme(
                logoUrl = "light_logo.png",
                primary = "#000000",
                secondary = "#FFFFFF"
            )
        )
        assertEquals(Resource.Success<ThemeInfo>(expected), result)
    }

    @Test
    fun `transform returns success with empty data list`() {
        // Given
        val themeDataList = emptyList<ThemeDataDto>()
        every { context.getString(R.string.theme_light) } returns "light"
        every { context.getString(R.string.theme_dark) } returns "dark"
        every { context.getString(R.string.theme_default_light_primary) } returns "#FFFFFF"
        every { context.getString(R.string.theme_default_light_secondary) } returns "#000000"
        every { context.getString(R.string.theme_default_dark_primary) } returns "#000000"
        every { context.getString(R.string.theme_default_dark_secondary) } returns "#FFFFFF"

        // When
        val result = transformThemeData(themeDataList)

        // Then
        val expected = ThemeInfo(
            light = Theme(
                logoUrl = "",
                primary = "#FFFFFF",
                secondary = "#000000"
            ),
            dark = Theme(
                logoUrl = "",
                primary = "#000000",
                secondary = "#FFFFFF"
            )
        )
        assertEquals(Resource.Success<ThemeInfo>(expected), result)
    }
}
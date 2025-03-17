package com.synchronoss.aiap.ui.theme

import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.Color
import com.synchronoss.aiap.R
import com.synchronoss.aiap.core.domain.models.theme.ThemeInfo
import com.synchronoss.aiap.core.domain.usecases.theme.ThemeManagerUseCases
import com.synchronoss.aiap.utils.Resource
import com.synchronoss.aiap.utils.toColor
import javax.inject.Inject
import com.synchronoss.aiap.utils.LogUtils

open class ThemeColors(
    val primary: Color,
    val secondary: Color,
    val background: Color = Color.White,
    val textHeading: Color = Color(0xFF1F2937),
    val textBody: Color = Color(0xFF6B7280),
    val textBodyAlt: Color = Color(0xFF1F2937),
    val surface: Color = Color(0xFFE4FFF4),
    val onSurface: Color = Color(0xFF2A7948),
    val outline: Color = Color(0xFFE5E7EB),
    val outlineVariant: Color = Color(0xFF0096D5),
    val tertiary: Color = Color.White,
    val onTertiary: Color = Color(0xFF6B7280)
)

class DarkThemeColors(
    primary: Color,
    secondary: Color
) : ThemeColors(
    primary = primary,
    secondary = secondary,
    background = Color(0xFF0D0D0D),
    textHeading = Color(0xFFC8D2E0),
    textBody = Color(0xFF8C8C8C),
    textBodyAlt = Color(0xFFFEFEFF),
    surface = Color(0xFF166534),
    onSurface = Color(0xFFE4FFF4),
    outline = Color(0xFF262627),
    outlineVariant = Color(0xFF0096D5),
    tertiary = Color(0xFF212121),
    onTertiary = Color(0xFFFEFEFF)
)

class ThemeLoader @Inject constructor(
    private var themeManagerUseCases: ThemeManagerUseCases,
    private val context: Context
) {
    private var themeConfig: ThemeInfo? = null
    private val TAG = "ThemeLoader"
    var logoUrlLight: String? = null
    var logoUrlDark: String? = null

    suspend fun loadTheme() {
        try {
            when (val result = themeManagerUseCases.getThemeFile()) {
                is Resource.Success -> {
                    result.data?.let { theme ->
                        themeConfig = theme
                        logoUrlLight = theme.light.logoUrl
                        logoUrlDark = theme.dark.logoUrl
                        LogUtils.d(TAG, "Theme loaded successfully")
                    }
                }
                is Resource.Error -> {
                    LogUtils.e(TAG, context.getString(R.string.theme_error_parse_json))
                }
            }
        } catch (e: Exception) {
            LogUtils.e(TAG, context.getString(R.string.theme_error_load_json), e)
        }
    }

    fun getThemeColors(): ThemeWithLogo {
        val primary = themeConfig?.light?.primary?.toColor() ?: Color(0xff0096D5)
        val secondary = themeConfig?.light?.secondary?.toColor() ?: Color(0xffE7F8FF)

        return ThemeWithLogo(
            themeColors = ThemeColors(primary = primary, secondary = secondary),
            logoUrl = logoUrlLight
        )
    }

    fun getDarkThemeColors(): ThemeWithLogo {
        val primary = themeConfig?.dark?.primary?.toColor() ?: Color(0xff0096D5)
        val secondary = themeConfig?.dark?.secondary?.toColor() ?: Color(0xff262627)

        return ThemeWithLogo(
            themeColors = DarkThemeColors(primary = primary, secondary = secondary),
            logoUrl = logoUrlDark
        )
    }
}

data class ThemeWithLogo(
    val themeColors: ThemeColors,
    val logoUrl: String?
)
package com.synchronoss.aiap.ui.theme

import android.util.Log
import androidx.compose.ui.graphics.Color
import com.synchronoss.aiap.domain.models.theme.ThemeInfo
import com.synchronoss.aiap.domain.usecases.theme.ThemeManagerUseCases
import com.synchronoss.aiap.utils.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

//@JsonClass(generateAdapter = true)
//data class ThemeConfig(
//    @Json(name = "shared") val shared: SharedConfig,
//    @Json(name = "themes") val themes: ThemesConfig
//)
//
//@JsonClass(generateAdapter = true)
//data class SharedConfig(
//    @Json(name = "logoUrl") val logoUrl: String
//)
//
//@JsonClass(generateAdapter = true)
//data class ThemesConfig(
//    @Json(name = "light") val light: ThemeColorConfig,
//    @Json(name = "dark") val dark: ThemeColorConfig
//)
//
//@JsonClass(generateAdapter = true)
//data class ThemeColorConfig(
//    @Json(name = "colors") val colors: Colors
//)
//
//@JsonClass(generateAdapter = true)
//data class Colors(
//    @Json(name = "primary") val primary: String,
//    @Json(name = "secondary") val secondary: String,
//    @Json(name = "background") val background: String,
//    @Json(name = "text") val text: TextColors,
//    @Json(name = "surface") val surface: SurfaceColors,
//    @Json(name = "outline") val outline: OutlineColors,
//    @Json(name = "tertiary") val tertiary: TertiaryColors
//)
//
//@JsonClass(generateAdapter = true)
//data class TextColors(
//    @Json(name = "heading") val heading: String,
//    @Json(name = "body") val body: String,
//    @Json(name = "bodyAlt") val bodyAlt: String
//)
//
//@JsonClass(generateAdapter = true)
//data class SurfaceColors(
//    @Json(name = "base") val base: String,
//    @Json(name = "onSurface") val onSurface: String
//)
//
//@JsonClass(generateAdapter = true)
//data class OutlineColors(
//    @Json(name = "defaultColor") val defaultColor: String,
//    @Json(name = "variant") val variant: String
//)
//
//@JsonClass(generateAdapter = true)
//data class TertiaryColors(
//    @Json(name = "base") val base: String,
//    @Json(name = "onTertiary") val onTertiary: String
//)
//

open class ThemeColors(
    val primary: Color,
    val secondary: Color,
    val background: Color,
    val textHeading: Color,
    val textBody: Color,
    val textBodyAlt: Color,
    val surface: Color,
    val onSurface: Color,
    val outline: Color,
    val outlineVariant: Color,
    val tertiary: Color,
    val onTertiary: Color
)

object ThemeLoader {
    private var themeConfig: ThemeInfo? = null
    private lateinit var themeManagerUseCases: ThemeManagerUseCases
    private var onThemeLoaded: (() -> Unit)? = null

//    fun setOnThemeLoadedListener(listener: () -> Unit) {
//        onThemeLoaded = listener
//    }

    fun loadTheme(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            try {
                when (val result = themeManagerUseCases.getThemeApi()) {
                    is Resource.Success -> {
                        if(result.data !=null){
                            Log.d("ThemeLoader", "Loaded theme yooo boii: ${result.data}")
                        }
                        themeConfig = result.data
//                        withContext(Dispatchers.Main) {
//                            onThemeLoaded?.invoke()
//                        }
                    }
                    is Resource.Error -> {
                        Log.e("ThemeLoader", "Failed to fetch theme: ${result.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e("ThemeLoader", "Error loading theme", e)
            }
        }
    }

    private fun String.toColor(): Color {
        return Color(android.graphics.Color.parseColor(this))
    }

    fun getThemeColors(): ThemeColors {
        return object : ThemeColors(
            primary = themeConfig?.light?.primary?.toColor() ?: Color(0xFF0096D5),
            secondary = themeConfig?.light?.secondary?.toColor() ?: Color(0xFFE7F8FF),
            background = Color.White,
            textHeading = Color(0xFF1F2937),
            textBody = Color(0xFF6B7280),
            textBodyAlt = Color(0xFF1F2937),
            surface = Color(0xFFE4FFF4),
            onSurface = Color(0xFF2A7948),
            outline = Color(0xFFE5E7EB),
            outlineVariant = Color(0xFF0096D5),
            tertiary = Color.White,
            onTertiary = Color(0xFF6B7280)
        ) {}
    }

    fun getDarkThemeColors(): ThemeColors {
        return object : ThemeColors(
            primary = themeConfig?.dark?.primary?.toColor() ?: Color(0xFF0096D5),
            secondary = themeConfig?.dark?.secondary?.toColor() ?: Color(0xFF262627),
            background = Color(0xFF0D0D0D),
            textHeading = Color(0xFFC8D2E0),
            textBody = Color(0xFF8C8C8C),
            textBodyAlt = Color(0xFFFEFEFF),
            surface = Color(0xFF166534),
            onSurface = Color(0xFFE4FFF4),
            outline = Color(0xFF262627),
            outlineVariant = Color(0xFF404040),
            tertiary = Color(0xFF212121),
            onTertiary = Color(0xFFFEFEFF)
        ) {}
    }
}
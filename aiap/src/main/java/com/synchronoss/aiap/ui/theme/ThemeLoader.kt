package com.synchronoss.aiap.ui.theme

import android.util.Log
import androidx.compose.ui.graphics.Color
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.synchronoss.aiap.domain.models.theme.ThemeInfo
import com.synchronoss.aiap.domain.usecases.theme.ThemeManagerUseCases
import com.synchronoss.aiap.utils.CacheManager
import com.synchronoss.aiap.utils.Resource
import javax.inject.Inject

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

class ThemeLoader @Inject constructor(
    private var themeManagerUseCases: ThemeManagerUseCases,
    private val cacheManager: CacheManager,
) {
    private var themeConfig: ThemeInfo? = null
    var logoUrlLight:String? = null
    var logoUrlDark:String? = null


    private companion object {
        const val THEME_CACHE_KEY = "theme_cache"
    }
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val jsonAdapter = moshi.adapter(ThemeInfo::class.java).lenient()

    suspend fun loadTheme(timestamp: Long?) {
        try {
            val result = cacheManager.getCachedDataWithTimestamp(
                key = THEME_CACHE_KEY,
                currentTimestamp = timestamp,
                fetchFromNetwork = {
                    themeManagerUseCases.getThemeApi()
                },
                serialize = { theme ->
                    jsonAdapter.toJson(theme)
                },
                deserialize = { jsonString ->
                    jsonAdapter.fromJson(jsonString)!!
                }
            )

            when (result) {
                is Resource.Success -> {
                    if (result.data != null) {
                        Log.d("ThemeLoader", "Loaded theme: ${result.data}")
                        themeConfig = result.data;
                        logoUrlLight = themeConfig!!.light.logoUrl
                        logoUrlDark = themeConfig!!.dark.logoUrl
                    }
                }
                is Resource.Error -> {
                    Log.e("ThemeLoader", "Failed to fetch theme: ${result.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("ThemeLoader", "Error loading theme", e)
        }
    }

    private fun String.toColor(): Color {
        return Color(android.graphics.Color.parseColor(this))
    }

    fun getThemeColors(): ThemeWithLogo {
        val themeObj = object : ThemeColors(
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
        ){}
        return ThemeWithLogo(
            themeColors = themeObj,
            logoUrl = logoUrlLight
        )
    }

    fun getDarkThemeColors(): ThemeWithLogo {
        val themeObj =  object : ThemeColors(
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
        return ThemeWithLogo(
            themeColors = themeObj,
            logoUrl = logoUrlDark
        )
    }
}

data class ThemeWithLogo(
    val themeColors: ThemeColors,
    val logoUrl: String?
)
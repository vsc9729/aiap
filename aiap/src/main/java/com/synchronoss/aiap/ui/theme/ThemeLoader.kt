package com.synchronoss.aiap.ui.theme

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.synchronoss.aiap.utils.Vendors
import com.synchronoss.aiap.utils.vendorThemeMap

@JsonClass(generateAdapter = true)
data class ThemeConfig(
    @Json(name = "shared") val shared: SharedConfig,
    @Json(name = "themes") val themes: ThemesConfig
)

@JsonClass(generateAdapter = true)
data class SharedConfig(
    @Json(name = "logoUrl") val logoUrl: String
)

@JsonClass(generateAdapter = true)
data class ThemesConfig(
    @Json(name = "light") val light: ThemeColorConfig,
    @Json(name = "dark") val dark: ThemeColorConfig
)

@JsonClass(generateAdapter = true)
data class ThemeColorConfig(
    @Json(name = "colors") val colors: Colors
)

@JsonClass(generateAdapter = true)
data class Colors(
    @Json(name = "primary") val primary: String,
    @Json(name = "secondary") val secondary: String,
    @Json(name = "background") val background: String,
    @Json(name = "text") val text: TextColors,
    @Json(name = "surface") val surface: SurfaceColors,
    @Json(name = "outline") val outline: OutlineColors,
    @Json(name = "tertiary") val tertiary: TertiaryColors
)

@JsonClass(generateAdapter = true)
data class TextColors(
    @Json(name = "heading") val heading: String,
    @Json(name = "body") val body: String,
    @Json(name = "bodyAlt") val bodyAlt: String
)

@JsonClass(generateAdapter = true)
data class SurfaceColors(
    @Json(name = "base") val base: String,
    @Json(name = "onSurface") val onSurface: String
)

@JsonClass(generateAdapter = true)
data class OutlineColors(
    @Json(name = "defaultColor") val defaultColor: String,
    @Json(name = "variant") val variant: String
)

@JsonClass(generateAdapter = true)
data class TertiaryColors(
    @Json(name = "base") val base: String,
    @Json(name = "onTertiary") val onTertiary: String
)

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
    private var themeConfig: ThemeConfig? = null

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val jsonAdapter = moshi.adapter(ThemeConfig::class.java)

    fun loadTheme(context: Context, vendor: Vendors?) {
        try {
            val inputStream = context.assets.open(vendorThemeMap[vendor] ?: vendorThemeMap[Vendors.Capsyl]!!)
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            themeConfig = jsonAdapter.fromJson(jsonString)
            inputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun String.toColor(): Color {
        return Color(android.graphics.Color.parseColor(this))
    }

    fun getThemeColors(): ThemeColors {
        return object : ThemeColors(
            primary = themeConfig?.themes?.light?.colors?.primary?.toColor() ?: Color(0xFF0096D5),
            secondary = themeConfig?.themes?.light?.colors?.secondary?.toColor() ?: Color(0xFFE7F8FF),
            background = themeConfig?.themes?.light?.colors?.background?.toColor() ?: Color.White,
            textHeading = themeConfig?.themes?.light?.colors?.text?.heading?.toColor() ?: Color(0xFF1F2937),
            textBody = themeConfig?.themes?.light?.colors?.text?.body?.toColor() ?: Color(0xFF6B7280),
            textBodyAlt = themeConfig?.themes?.light?.colors?.text?.bodyAlt?.toColor() ?: Color(0xFF1F2937),
            surface = themeConfig?.themes?.light?.colors?.surface?.base?.toColor() ?: Color(0xFFE4FFF4),
            onSurface = themeConfig?.themes?.light?.colors?.surface?.onSurface?.toColor() ?: Color(0xFF2A7948),
            outline = themeConfig?.themes?.light?.colors?.outline?.defaultColor?.toColor() ?: Color(0xFFE5E7EB),
            outlineVariant = themeConfig?.themes?.light?.colors?.outline?.variant?.toColor() ?: Color(0xFF0096D5),
            tertiary = themeConfig?.themes?.light?.colors?.tertiary?.base?.toColor() ?: Color.White,
            onTertiary = themeConfig?.themes?.light?.colors?.tertiary?.onTertiary?.toColor() ?: Color(0xFF6B7280)
        ) {}
    }

    fun getDarkThemeColors(): ThemeColors {
        return object : ThemeColors(
            primary = themeConfig?.themes?.dark?.colors?.primary?.toColor() ?: Color(0xFF0096D5),
            secondary = themeConfig?.themes?.dark?.colors?.secondary?.toColor() ?: Color(0xFF262627),
            background = themeConfig?.themes?.dark?.colors?.background?.toColor() ?: Color(0xFF0D0D0D),
            textHeading = themeConfig?.themes?.dark?.colors?.text?.heading?.toColor() ?: Color(0xFFC8D2E0),
            textBody = themeConfig?.themes?.dark?.colors?.text?.body?.toColor() ?: Color(0xFF8C8C8C),
            textBodyAlt = themeConfig?.themes?.dark?.colors?.text?.bodyAlt?.toColor() ?: Color(0xFFFEFEFF),
            surface = themeConfig?.themes?.dark?.colors?.surface?.base?.toColor() ?: Color(0xFF166534),
            onSurface = themeConfig?.themes?.dark?.colors?.surface?.onSurface?.toColor() ?: Color(0xFFE4FFF4),
            outline = themeConfig?.themes?.dark?.colors?.outline?.defaultColor?.toColor() ?: Color(0xFF262627),
            outlineVariant = themeConfig?.themes?.dark?.colors?.outline?.variant?.toColor() ?: Color(0xFF404040),
            tertiary = themeConfig?.themes?.dark?.colors?.tertiary?.base?.toColor() ?: Color(0xFF212121),
            onTertiary = themeConfig?.themes?.dark?.colors?.tertiary?.onTertiary?.toColor() ?: Color(0xFFFEFEFF)
        ) {}
    }
}
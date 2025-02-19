package com.synchronoss.aiap.core.data.remote.theme

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data Transfer Object representing theme information from the remote API.
 *
 * @property themeId Unique identifier for the theme
 * @property themeName Name of the theme
 * @property logoUrl Optional URL for the theme's logo
 * @property primaryColor Optional primary color code for the theme
 * @property secondaryColor Optional secondary color code for the theme
 */
@JsonClass(generateAdapter = true)
data class ThemeDataDto(
    @Json(name = "themeId")
    val themeId: String,
    @Json(name = "themeName")
    val themeName: String,
    @Json(name = "logoUrl")
    val logoUrl: String?,
    @Json(name = "primaryColor")
    val primaryColor: String?,
    @Json(name = "secondaryColor")
    val secondaryColor: String?
)


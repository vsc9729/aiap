package com.synchronoss.aiap.data.remote.theme

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ThemeDataDto(
    @Json(name = "themeName")
    val themeName: String,
    @Json(name = "logoUrl")
    val logoUrl: String?,
    @Json(name = "primaryColor")
    val primaryColor: String?,
    @Json(name = "secondaryColor")
    val secondaryColor: String?
)


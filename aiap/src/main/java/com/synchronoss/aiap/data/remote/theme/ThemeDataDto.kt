package com.synchronoss.aiap.data.remote.theme

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ThemeDataDto(
    @Json(name = "light")
    val light: ThemeDto,
    @Json(name = "dark")
    val dark: ThemeDto
)

@JsonClass(generateAdapter = true)
data class ThemeDto(
    @Json(name = "logoUrl")
    val logoUrl: String,
    @Json(name = "primary")
    val primary: String,
    @Json(name = "secondary")
    val secondary: String
)


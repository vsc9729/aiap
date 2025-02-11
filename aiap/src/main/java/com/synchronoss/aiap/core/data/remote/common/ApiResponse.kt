package com.synchronoss.aiap.core.data.remote.common

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    @Json(name = "code")
    val code: Int,
    
    @Json(name = "title")
    val title: String,
    
    @Json(name = "message")
    val message: String,
    
    @Json(name = "data")
    val data: T
) 
package com.synchronoss.aiap.core.data.remote.common

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Generic data class representing the standard API response format.
 *
 * @param T The type of data contained in the response
 * @property code HTTP status code or custom response code
 * @property title Brief title or description of the response
 * @property message Detailed message about the response
 * @property data The actual response data of type T
 */
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
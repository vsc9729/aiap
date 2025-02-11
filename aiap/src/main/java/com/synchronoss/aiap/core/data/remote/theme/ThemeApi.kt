package com.synchronoss.aiap.core.data.remote.theme
import com.synchronoss.aiap.core.data.remote.common.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Header

fun interface ThemeApi {
  @GET("api/theme")
  suspend fun getTheme(
    @Header("x-api-key") apiKey: String
  ): ApiResponse<List<ThemeDataDto>>
}
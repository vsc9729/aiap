package com.synchronoss.aiap.core.data.remote.theme
import com.synchronoss.aiap.core.data.remote.common.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Header

/**
 * Retrofit interface for theme-related API endpoints.
 * Provides method for retrieving theme information.
 */
fun interface ThemeApi {
  /**
   * Retrieves theme configuration.
   * @param apiKey API key for authentication
   * @return Response containing list of theme data
   */
  @GET("api/theme")
  suspend fun getTheme(
    @Header("x-api-key") apiKey: String
  ): ApiResponse<List<ThemeDataDto>>
}
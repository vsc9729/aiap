package com.synchronoss.aiap.data.remote.theme
import retrofit2.http.GET
import retrofit2.http.Header

interface ThemeApi {
  @GET("api/theme")
  suspend fun getTheme(
    @Header("x-api-key") apiKey: String = "IAPAppAndroid"
  ):List<ThemeDataDto>

}
package com.synchronoss.aiap.data.remote.theme
import retrofit2.http.GET

interface ThemeApi {
  @GET("0c5277decab56a59d6d9")
  suspend fun getTheme():ThemeDataDto
}
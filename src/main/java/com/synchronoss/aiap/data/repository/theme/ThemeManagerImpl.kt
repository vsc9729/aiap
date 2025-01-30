package com.synchronoss.aiap.data.repository.theme

import android.util.Log
import com.synchronoss.aiap.data.mappers.toThemeInfo
import com.synchronoss.aiap.data.remote.theme.ThemeApi
import com.synchronoss.aiap.data.remote.theme.ThemeDataDto
import com.synchronoss.aiap.domain.models.theme.ThemeInfo
import com.synchronoss.aiap.domain.repository.theme.ThemeManager
import com.synchronoss.aiap.utils.Resource
import javax.inject.Inject

private const val API_KEY = "IAPAppAndroid"

class ThemeManagerImpl @Inject constructor(
    private val api: ThemeApi,
) : ThemeManager {
    private val mockThemeData = listOf(
        ThemeDataDto(
            themeName = "Light",
            logoUrl = "https://capsyl.com/wp-content/uploads/cropped-Capsyl-Logo-sm-2.png",
            primaryColor = "#0096D5",
            secondaryColor = "#E7F8FF"
        ),
        ThemeDataDto(
            themeName = "Dark",
            logoUrl = "https://i.ibb.co/WsyDq6v/capsyl-dark.png",
            primaryColor = "#0096D5",
            secondaryColor = "#262627"
        )
    )

    override suspend fun getTheme(): Resource<ThemeInfo> {
        return try {
            val themeInfo = mockThemeData.toThemeInfo()
            Log.d("ThemeData", "Mock data: $themeInfo")
            Resource.Success(themeInfo)
        } catch (e: Exception) {
            throw Exception(e.message ?: "Failed to process mock theme data")
        }
    }
}
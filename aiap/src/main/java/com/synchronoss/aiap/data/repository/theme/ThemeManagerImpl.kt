package com.synchronoss.aiap.data.repository.theme

import android.util.Log
import com.synchronoss.aiap.data.mappers.toThemeInfo
import com.synchronoss.aiap.data.remote.common.ApiResponse
import com.synchronoss.aiap.data.remote.theme.ThemeApi
import com.synchronoss.aiap.data.remote.theme.ThemeDataDto
import com.synchronoss.aiap.domain.models.theme.Theme
import com.synchronoss.aiap.domain.models.theme.ThemeInfo
import com.synchronoss.aiap.domain.repository.theme.ThemeManager
import com.synchronoss.aiap.utils.Resource
import javax.inject.Inject

class ThemeManagerImpl @Inject constructor(
    private val api: ThemeApi,
) : ThemeManager {
    override suspend fun getTheme(): Resource<ThemeInfo> {  // Change return type to ThemeInfo
        return try {
            val apiResponse: ApiResponse<List<ThemeDataDto>> = api.getTheme()
            val themeData: List<ThemeDataDto> = apiResponse.data
            val themeInfo =themeData.toThemeInfo()

            Log.d("ThemeData", "data ${themeInfo}")

            Resource.Success(themeInfo)  // Return the domain model
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch theme data")
        }
    }
}
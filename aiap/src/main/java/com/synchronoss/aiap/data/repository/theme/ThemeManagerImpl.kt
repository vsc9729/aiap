package com.synchronoss.aiap.data.repository.theme

import android.util.Log
import com.synchronoss.aiap.data.mappers.ThemeMapper
import com.synchronoss.aiap.data.remote.theme.ThemeApi
import com.synchronoss.aiap.domain.models.theme.ThemeInfo
import com.synchronoss.aiap.domain.repository.theme.ThemeManager
import com.synchronoss.aiap.utils.Resource
import javax.inject.Inject

class ThemeManagerImpl @Inject constructor(
    private val api: ThemeApi,
    private val themeMapper: ThemeMapper  // Inject the mapper
) : ThemeManager {
    override suspend fun getTheme(): Resource<ThemeInfo> {  // Change return type to ThemeInfo
        return try {
            val themeData = api.getTheme()
            val themeInfo = themeMapper.mapToDomain(themeData)

            Log.d("ThemeData", "data ${themeInfo}")

            Resource.Success(themeInfo)  // Return the domain model
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch theme data")
        }
    }
}
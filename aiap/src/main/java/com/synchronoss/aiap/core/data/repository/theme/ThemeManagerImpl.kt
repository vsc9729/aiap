package com.synchronoss.aiap.core.data.repository.theme

import android.content.Context
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.synchronoss.aiap.core.domain.repository.theme.ThemeManager
import com.synchronoss.aiap.core.data.remote.theme.ThemeDataDto
import com.synchronoss.aiap.core.domain.models.theme.ThemeInfo
import com.synchronoss.aiap.core.domain.models.theme.Theme
import com.synchronoss.aiap.utils.Resource
import com.synchronoss.aiap.utils.Vendors
import com.synchronoss.aiap.utils.vendorThemeMap
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import com.synchronoss.aiap.utils.LogUtils

class ThemeManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val currentVendor: Vendors = Vendors.Capsyl
) : ThemeManager {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    override suspend fun getTheme(): Resource<ThemeInfo> {
        return try {
            val jsonFileName = vendorThemeMap[currentVendor] 
                ?: throw Exception("No theme file found for vendor $currentVendor")

            val jsonString = context.assets.open(jsonFileName)
                .bufferedReader()
                .use { it.readText() }

            val listType = Types.newParameterizedType(List::class.java, ThemeDataDto::class.java)
            val adapter = moshi.adapter<List<ThemeDataDto>>(listType)
            
            val themeDataList = adapter.fromJson(jsonString) 
                ?: throw Exception("Failed to parse theme JSON")

            val lightTheme = themeDataList.find { it.themeName == "Light" }
            val darkTheme = themeDataList.find { it.themeName == "Dark" }

            val themeInfo = ThemeInfo(
                light = Theme(
                    logoUrl = lightTheme?.logoUrl ?: "",
                    primary = lightTheme?.primaryColor ?: "#0096D5",
                    secondary = lightTheme?.secondaryColor ?: "#E7F8FF"
                ),
                dark = Theme(
                    logoUrl = darkTheme?.logoUrl ?: lightTheme?.logoUrl ?: "",
                    primary = darkTheme?.primaryColor ?: "#0096D5",
                    secondary = darkTheme?.secondaryColor ?: "#262627"
                )
            )

            LogUtils.d("ThemeManager", "Loaded theme from JSON: $themeInfo")
            Resource.Success(themeInfo)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to load theme from JSON")
        }
    }
}
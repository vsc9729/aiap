package com.synchronoss.aiap.core.data.repository.theme

import android.content.Context
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.synchronoss.aiap.R
import com.synchronoss.aiap.core.domain.repository.theme.ThemeManager
import com.synchronoss.aiap.core.data.remote.theme.ThemeDataDto
import com.synchronoss.aiap.core.domain.models.theme.ThemeInfo
import com.synchronoss.aiap.core.domain.models.theme.Theme
import com.synchronoss.aiap.core.domain.usecases.theme.TransformThemeData
import com.synchronoss.aiap.utils.LogUtils
import com.synchronoss.aiap.utils.Resource
import com.synchronoss.aiap.utils.Vendors
import com.synchronoss.aiap.utils.vendorThemeMap
import javax.inject.Inject

/**
 * Implementation of ThemeManager interface that handles theme-related operations.
 * Manages theme loading from JSON assets and theme configuration.
 *
 * @property context Android application context
 * @property currentVendor Current vendor for theme selection
 * @property transformThemeData Use case for transforming theme data
 */
class ThemeManagerImpl @Inject constructor(
    private val context: Context,
    private val currentVendor: Vendors = Vendors.Capsyl,
    private val transformThemeData: TransformThemeData
) : ThemeManager {

    private val TAG = context.getString(R.string.theme_manager_tag)
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    /**
     * Retrieves theme configuration from JSON assets.
     * Loads and parses theme data for both light and dark themes.
     * @return Resource containing theme information or error
     */
    override suspend fun getTheme(): Resource<ThemeInfo> {
        return try {
            val jsonFileName = vendorThemeMap[currentVendor] 
                ?: throw Exception(context.getString(R.string.theme_error_no_theme_file, currentVendor))

            val jsonString = context.assets.open(jsonFileName)
                .bufferedReader()
                .use { it.readText() }

            val listType = Types.newParameterizedType(List::class.java, ThemeDataDto::class.java)
            val adapter = moshi.adapter<List<ThemeDataDto>>(listType)
            
            val themeDataList = adapter.fromJson(jsonString) 
                ?: throw Exception(context.getString(R.string.theme_error_parse_json))

            // Use the transform use case to convert DTO to domain model
            transformThemeData(themeDataList)
        } catch (e: Exception) {
            Resource.Error(e.message ?: context.getString(R.string.theme_error_load_json))
        }
    }
}
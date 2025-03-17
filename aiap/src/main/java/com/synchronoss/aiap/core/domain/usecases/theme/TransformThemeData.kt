package com.synchronoss.aiap.core.domain.usecases.theme

import android.content.Context
import com.synchronoss.aiap.R
import com.synchronoss.aiap.core.data.remote.theme.ThemeDataDto
import com.synchronoss.aiap.core.domain.models.theme.Theme
import com.synchronoss.aiap.core.domain.models.theme.ThemeInfo
import com.synchronoss.aiap.utils.Resource
import javax.inject.Inject

/**
 * Use case for transforming raw theme data into domain models.
 */
class TransformThemeData @Inject constructor(
    private val context: Context
) {
    private val TAG = context.getString(R.string.theme_manager_tag)

    /**
     * Transform a list of ThemeDataDto into ThemeInfo
     * @param themeDataList List of theme data from repository
     * @return Resource containing transformed ThemeInfo or error
     */
    operator fun invoke(themeDataList: List<ThemeDataDto>): Resource<ThemeInfo> {
        return try {
            val errorMessage = context.getString(R.string.theme_error_load_json)
            val lightTheme = themeDataList.find { it.themeName == context.getString(R.string.theme_light) }
            val darkTheme = themeDataList.find { it.themeName == context.getString(R.string.theme_dark) }

            val themeInfo = ThemeInfo(
                light = Theme(
                    logoUrl = lightTheme?.logoUrl ?: "",
                    primary = lightTheme?.primaryColor ?: context.getString(R.string.theme_default_light_primary),
                    secondary = lightTheme?.secondaryColor ?: context.getString(R.string.theme_default_light_secondary)
                ),
                dark = Theme(
                    logoUrl = darkTheme?.logoUrl ?: lightTheme?.logoUrl ?: "",
                    primary = darkTheme?.primaryColor ?: context.getString(R.string.theme_default_dark_primary),
                    secondary = darkTheme?.secondaryColor ?: context.getString(R.string.theme_default_dark_secondary)
                )
            )

            Resource.Success(themeInfo)
        } catch (e: Exception) {
            Resource.Error(e.message ?: context.getString(R.string.theme_error_load_json))
        }
    }
} 
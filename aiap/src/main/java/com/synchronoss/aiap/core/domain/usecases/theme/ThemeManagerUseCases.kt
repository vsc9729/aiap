package com.synchronoss.aiap.core.domain.usecases.theme



/**
 * Collection of use cases for managing theme operations.
 *
 * @property getThemeApi Use case for retrieving theme information from the API
 * @property transformThemeData Use case for transforming theme data into domain models
 */

data class ThemeManagerUseCases(
    val getThemeFile: GetThemeFile,
    val transformThemeData: TransformThemeData
)

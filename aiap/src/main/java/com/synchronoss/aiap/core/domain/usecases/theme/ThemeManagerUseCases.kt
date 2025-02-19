package com.synchronoss.aiap.core.domain.usecases.theme

/**
 * Collection of use cases for managing theme operations.
 *
 * @property getThemeApi Use case for retrieving theme information from the API
 */
data class ThemeManagerUseCases(
    val getThemeApi: GetThemeApi
)

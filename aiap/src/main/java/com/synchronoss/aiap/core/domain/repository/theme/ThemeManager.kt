package com.synchronoss.aiap.core.domain.repository.theme

import com.synchronoss.aiap.core.domain.models.theme.ThemeInfo
import com.synchronoss.aiap.utils.Resource

/**
 * Interface for managing theme-related operations.
 * Provides functionality to retrieve theme information for the application.
 * This is a functional interface with a single method for theme retrieval.
 */
fun interface ThemeManager {
    /**
     * Retrieves the current theme information.
     * @return Resource containing ThemeInfo object with theme details
     */
    suspend fun getTheme(): Resource<ThemeInfo>
}

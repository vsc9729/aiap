package com.synchronoss.aiap.core.domain.usecases.theme

import com.synchronoss.aiap.core.domain.models.theme.ThemeInfo
import com.synchronoss.aiap.core.domain.repository.theme.ThemeManager
import com.synchronoss.aiap.utils.Resource

class GetThemeFile(
    private val themeManager: ThemeManager
) {
    suspend operator fun invoke(): Resource<ThemeInfo> {
        return try {
            themeManager.getTheme()
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error occurred")
        }
    }
}

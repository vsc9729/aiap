package com.synchronoss.aiap.domain.usecases.theme

import com.synchronoss.aiap.domain.models.theme.ThemeInfo
import com.synchronoss.aiap.domain.repository.theme.ThemeManager
import com.synchronoss.aiap.utils.Resource

class GetThemeApi(
    private val themeManager: ThemeManager
) {
    suspend operator fun invoke(): Resource<ThemeInfo> {
        return themeManager.getTheme()
    }
}

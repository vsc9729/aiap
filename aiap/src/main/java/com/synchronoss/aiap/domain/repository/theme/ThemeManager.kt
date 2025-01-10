package com.synchronoss.aiap.domain.repository.theme

import com.synchronoss.aiap.data.remote.theme.ThemeDataDto
import com.synchronoss.aiap.domain.models.theme.ThemeInfo
import com.synchronoss.aiap.utils.Resource

interface ThemeManager {
    suspend fun getTheme(): Resource<ThemeInfo>
}

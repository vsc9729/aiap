package com.synchronoss.aiap.domain.repository.theme

import com.synchronoss.aiap.domain.models.theme.ThemeInfo
import com.synchronoss.aiap.utils.Resource

// in case of single fun in an interface, it is recommended to make an interface fun interface.
fun interface ThemeManager {
    suspend fun getTheme(): Resource<ThemeInfo>
}

package com.synchronoss.aiap.data.mappers

import com.synchronoss.aiap.data.remote.theme.ThemeDataDto
import com.synchronoss.aiap.data.remote.theme.ThemeDto
import com.synchronoss.aiap.domain.models.theme.Theme
import com.synchronoss.aiap.domain.models.theme.ThemeInfo


class ThemeMapper {
    fun mapToDomain(themeDataDto: ThemeDataDto): ThemeInfo {
        return ThemeInfo(
            light = mapThemeToDomain(themeDataDto.light),
            dark = mapThemeToDomain(themeDataDto.dark)
        )
    }

    private fun mapThemeToDomain(themeDto: ThemeDto): Theme {
        return Theme(
            logoUrl = themeDto.logoUrl,
            primary = themeDto.primary,
            secondary = themeDto.secondary,
        )
    }

}

package com.synchronoss.aiap.utils

import androidx.compose.ui.graphics.Color

fun String.toColor(): Color {
    return Color(android.graphics.Color.parseColor(this))
} 
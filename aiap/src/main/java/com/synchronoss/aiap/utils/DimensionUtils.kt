package com.synchronoss.aiap.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit


@Composable
fun getDimension(id: Int): Dp {
    val context = LocalContext.current
    // Get the dimension in pixels
    val pixelValue = context.resources.getDimensionPixelSize(id)
    // Convert it to dp
    return with(LocalDensity.current) { pixelValue.toDp() }
}

@Composable
fun getDimensionText(id: Int): TextUnit {
    val context = LocalContext.current
    // Get the dimension in pixels
    val pixelValue = context.resources.getDimensionPixelSize(id)
    // Convert it to sp (which is a type of TextUnit)
    return with(LocalDensity.current) { pixelValue.toSp() }
}
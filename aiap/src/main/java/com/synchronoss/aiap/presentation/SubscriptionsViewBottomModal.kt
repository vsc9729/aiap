package com.synchronoss.aiap.presentation

import androidx.activity.ComponentActivity
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.hilt.navigation.compose.hiltViewModel
import com.synchronoss.aiap.R
import com.synchronoss.aiap.utils.getDimension
import com.synchronoss.aiap.presentation.SubscriptionsViewModel
import com.synchronoss.aiap.presentation.ToastComposable

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.internal.wait
import com.synchronoss.aiap.ui.theme.AiAPTheme as AiAPTheme1

@Composable
fun SubscriptionsViewBottomSheet(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    visible: Boolean,
    partnerUserId: String,
    activity: ComponentActivity,
    subscriptionsViewModel: SubscriptionsViewModel = hiltViewModel(),
    isLaunchedViaIntent: Boolean = false
) {
    LaunchedEffect(partnerUserId) {
        subscriptionsViewModel.initialize(id = partnerUserId, intentLaunch = isLaunchedViaIntent)
    }

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val sheetHeight = screenHeight * 0.98f

    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(visible) {
        isVisible = visible
    }

    val animatedAlpha by animateFloatAsState(
        targetValue = if (isVisible) 0.4f else 0f,
        animationSpec = tween(300),
        label = "overlay_animation"
    )

    val animatedOffset by animateFloatAsState(
        targetValue = if (isVisible) 0f else 1f,
        animationSpec = spring(
            dampingRatio = 1f,  // Critical damping - no oscillation
            stiffness = Spring.StiffnessMediumLow,
            visibilityThreshold = 0.001f
        ),
        label = "sheet_animation",
        finishedListener = { if (!isVisible) onDismissRequest() }
    )

    if (visible || animatedOffset < 1f) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = animatedAlpha))
                .clickable(
                    enabled = isVisible,
                    onClick = { isVisible = false }
                )
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(sheetHeight)
                    .offset(y = animatedOffset * sheetHeight)
                    .clip(RoundedCornerShape(
                        topStart = getDimension(R.dimen.bottom_sheet_corner_radius),
                        topEnd = getDimension(R.dimen.bottom_sheet_corner_radius)
                    ))
                    .background(if (isSystemInDarkTheme()) Color(0xFF0D0D0D) else Color.White)
                    .clickable(enabled = false) {}
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(getDimension(R.dimen.bottom_sheet_header_padding)),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = { isVisible = false }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = if (isSystemInDarkTheme()) Color.White else Color(0xFF6B7280),
                                modifier = Modifier.size(getDimension(R.dimen.bottom_sheet_icon_size))
                            )
                        }
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        AiAPTheme1 {
                            SubscriptionsView(activity = activity)

                            ToastComposable(
                                heading = subscriptionsViewModel.toastState.heading,
                                subText = subscriptionsViewModel.toastState.message,
                                onDismiss = { subscriptionsViewModel.hideToast() },
                                isVisible = subscriptionsViewModel.toastState.isVisible,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(
                                        bottom = getDimension(R.dimen.bottom_sheet_toast_padding_bottom),
                                        start = getDimension(R.dimen.bottom_sheet_toast_padding_horizontal),
                                        end = getDimension(R.dimen.bottom_sheet_toast_padding_horizontal)
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}
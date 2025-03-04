package com.synchronoss.aiap.presentation

import androidx.activity.ComponentActivity
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.synchronoss.aiap.R
import com.synchronoss.aiap.utils.getDimension
import com.synchronoss.aiap.presentation.viewmodels.SubscriptionsViewModel
import com.synchronoss.aiap.ui.theme.AiAPTheme
import com.synchronoss.aiap.core.di.DaggerAiapComponent
import com.synchronoss.aiap.presentation.viewmodels.SubscriptionsViewBottomModalWrapper

@Composable
fun SubscriptionsViewBottomSheet(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    visible: Boolean,
    partnerUserId: String,
    activity: ComponentActivity,
    isLaunchedViaIntent: Boolean = false,
    apiKey: String
) {
    val wrapper = remember {
        val wrapper = SubscriptionsViewBottomModalWrapper()
        val application = activity.application
        val aiapComponent = DaggerAiapComponent.factory().create(application)
        aiapComponent.inject(wrapper)
        wrapper
    }
    
    val viewModel = remember {
        wrapper.getViewModel(activity)
    }

    LaunchedEffect(partnerUserId) {
        viewModel.initialize(id = partnerUserId, intentLaunch = isLaunchedViaIntent, apiKey = apiKey)
    }

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val sheetHeight = screenHeight * 0.98f

    if(isLaunchedViaIntent) {
        AiAPTheme(
            darkTheme = isSystemInDarkTheme(),
            activity = activity,
            content = {
                FullScreenContent(
                    viewModel = viewModel,
                    activity = activity,
                    isLaunchedViaIntent = isLaunchedViaIntent
                )
            }
        )
    } else {
        LaunchedEffect(visible) {
            viewModel.dialogState.value = visible
        }

        val animatedAlpha by animateFloatAsState(
            targetValue = if (viewModel.dialogState.value) 0.4f else 0f,
            animationSpec = tween(300),
            label = "overlay_animation"
        )

        val animatedOffset by animateFloatAsState(
            targetValue = if (viewModel.dialogState.value) 0f else 1f,
            animationSpec = spring(
                dampingRatio = 1f,
                stiffness = Spring.StiffnessMediumLow,
                visibilityThreshold = 0.001f
            ),
            label = "sheet_animation",
            finishedListener = { if (!viewModel.dialogState.value) {
                onDismissRequest()
                viewModel.clearState()
            } }
        )

        if (visible || animatedOffset < 1f) {
            AiAPTheme(
                darkTheme = isSystemInDarkTheme(),
                activity = activity,
                content = {
                    ModalContent(
                        viewModel = viewModel,
                        activity = activity,
                        isLaunchedViaIntent = isLaunchedViaIntent,
                        animatedAlpha = animatedAlpha,
                        animatedOffset = animatedOffset,
                        sheetHeight = sheetHeight
                    )
                }
            )
        }
    }
}

@Composable
private fun FullScreenContent(
    viewModel: SubscriptionsViewModel,
    activity: ComponentActivity,
    isLaunchedViaIntent: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                enabled = viewModel.dialogState.value,
                onClick = { viewModel.dialogState.value = false }
            )
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight()
                .background(if (isSystemInDarkTheme()) Color(0xFF0D0D0D) else Color.White)
                .clickable(enabled = false) {}
        ) {
            SubscriptionsView(activity = activity, launchedViaIntent = isLaunchedViaIntent)
            ToastComposable(
                heading = viewModel.toastState.heading,
                subText = viewModel.toastState.message,
                onDismiss = { viewModel.hideToast() },
                isVisible = viewModel.toastState.isVisible,
                formatArgs = viewModel.toastState.formatArgs,
                isSuccess = viewModel.toastState.isSuccess,
                isPending = viewModel.toastState.isPending,
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

@Composable
private fun ModalContent(
    viewModel: SubscriptionsViewModel,
    activity: ComponentActivity,
    isLaunchedViaIntent: Boolean,
    animatedAlpha: Float,
    animatedOffset: Float,
    sheetHeight: androidx.compose.ui.unit.Dp
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = animatedAlpha))
            .clickable(
                enabled = viewModel.dialogState.value,
                onClick = { viewModel.dialogState.value = false }
            )
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(sheetHeight)
                .offset(y = animatedOffset * sheetHeight)
                .clip(
                    RoundedCornerShape(
                        topStart = getDimension(R.dimen.bottom_sheet_corner_radius),
                        topEnd = getDimension(R.dimen.bottom_sheet_corner_radius)
                    )
                )
                .background(if (isSystemInDarkTheme()) Color(0xFF0D0D0D) else Color.White)
                .clickable(enabled = false) {}
        ) {
            SubscriptionsView(activity = activity, launchedViaIntent = isLaunchedViaIntent)
            ToastComposable(
                heading = viewModel.toastState.heading,
                subText = viewModel.toastState.message,
                onDismiss = { viewModel.hideToast() },
                isVisible = viewModel.toastState.isVisible,
                formatArgs = viewModel.toastState.formatArgs,
                isSuccess = viewModel.toastState.isSuccess,
                isPending = viewModel.toastState.isPending,
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
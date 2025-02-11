package com.synchronoss.aiap.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.synchronoss.aiap.R
import com.synchronoss.aiap.utils.getDimension

// hello sonarqube

@Composable
fun SkeletonLoader() {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            Color.LightGray.copy(alpha = 0.6f),
            Color.LightGray.copy(alpha = 0.2f),
            Color.LightGray.copy(alpha = 0.6f),
        ),
        start = Offset(translateAnim - 200, translateAnim - 200),
        end = Offset(translateAnim, translateAnim)
    )

    Box {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = if(isSystemInDarkTheme()) Color.Black else Color.White)
                .padding(horizontal = getDimension(R.dimen.skeleton_padding_horizontal)),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // Logo
                Box(
                    modifier = Modifier
                        .width(getDimension(R.dimen.logo_width))
                        .height(getDimension(R.dimen.logo_height))
                        .clip(RoundedCornerShape(getDimension(R.dimen.skeleton_toast_corner_radius)))
                        .background(shimmerBrush)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(getDimension(R.dimen.spacing_large)))

                // Title text
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(getDimension(R.dimen.current_plan_card_height))
                        .clip(RoundedCornerShape(getDimension(R.dimen.skeleton_toast_corner_radius)))
                        .background(shimmerBrush)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(getDimension(R.dimen.spacing_small)))

                // Tab selector
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(getDimension(R.dimen.tab_height_portrait))
                        .clip(RoundedCornerShape(getDimension(R.dimen.tab_selector_corner_radius)))
                        .background(shimmerBrush)
                )

                Spacer(modifier = Modifier.height(getDimension(R.dimen.spacing_large)))

                // Current subscription card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(getDimension(R.dimen.current_plan_card_height))
                        .clip(RoundedCornerShape(getDimension(R.dimen.card_corner_radius)))
                        .background(shimmerBrush)
                )

                Spacer(modifier = Modifier.height(getDimension(R.dimen.spacing_large)))

                // Other plan cards
                repeat(2) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(getDimension(R.dimen.other_plan_card_height))
                            .clip(RoundedCornerShape(getDimension(R.dimen.card_corner_radius)))
                            .background(shimmerBrush)
                    )
                    Spacer(modifier = Modifier.height(getDimension(R.dimen.spacing_large)))
                }
            }

            // Bottom section
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(getDimension(R.dimen.continue_btn_height))
                        .clip(RoundedCornerShape(getDimension(R.dimen.continue_btn_shape)))
                        .background(shimmerBrush)
                )

                Spacer(modifier = Modifier.height(getDimension(R.dimen.spacing_large)))

                // Apply coupon text shimmer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(getDimension(R.dimen.dialog_width))
                            .height(getDimension(R.dimen.skeleton_toast_height))
                            .clip(RoundedCornerShape(getDimension(R.dimen.skeleton_toast_corner_radius)))
                            .background(shimmerBrush)
                    )
                }
            }
        }
    }
}

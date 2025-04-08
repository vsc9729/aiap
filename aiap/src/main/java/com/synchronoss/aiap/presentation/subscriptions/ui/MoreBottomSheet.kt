package com.synchronoss.aiap.presentation.subscriptions.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.synchronoss.aiap.R
import com.synchronoss.aiap.core.di.DaggerAiapComponent
import com.synchronoss.aiap.presentation.subscriptions.wrapper.MoreBottomSheetWrapper
import com.synchronoss.aiap.utils.LogUtils
import com.synchronoss.aiap.utils.getDimension
import com.synchronoss.aiap.utils.getDimensionText

@Composable
fun MoreBottomSheet(
    onDismiss: () -> Unit,
    onApplyCoupon: () -> Unit,
    onGoToSubscriptions: () -> Unit,
    activity: ComponentActivity
) {
    val TAG = "MoreBottomSheet"
    var showCouponDialog by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    val bottomSheetHeight = 105.dp
    
    val wrapper = remember {
        val wrapper = MoreBottomSheetWrapper()
        val application = activity.application
        val aiapComponent = DaggerAiapComponent.factory().create(application)
        aiapComponent.inject(wrapper)
        wrapper
    }
    
    val subscriptionsViewModel = remember {
        wrapper.getViewModel(activity)
    }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    val animatedAlpha by animateFloatAsState(
        targetValue = if (isVisible) 0.4f else 0f,
        animationSpec = tween(300),
        label = "overlay_animation"
    )

    val animatedOffset by animateFloatAsState(
        targetValue = if (isVisible) 0f else 1f,
        animationSpec = spring(
            dampingRatio = 1f,
            stiffness = Spring.StiffnessMediumLow,
            visibilityThreshold = 0.001f
        ),
        label = "sheet_animation",
        finishedListener = { if (!isVisible) onDismiss() }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = animatedAlpha))
            .clickable { isVisible = false }
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(bottomSheetHeight)
                .offset(y = animatedOffset * bottomSheetHeight)
                .drawBehind {
                    val shadowColor = Color.Black.copy(alpha = 0.068f)
                    val shadowRadius = 35.dp.toPx()
                    val offsetY = -shadowRadius / 2
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, shadowColor),
                            startY = offsetY,
                            endY = offsetY + shadowRadius,
                            tileMode = TileMode.Clamp
                        ),
                        topLeft = Offset(0f, offsetY),
                        size = Size(size.width, shadowRadius)
                    )
                }
                .clip(
                    RoundedCornerShape(
                        topStart = getDimension(R.dimen.more_bottom_sheet_corner_radius),
                        topEnd = getDimension(R.dimen.more_bottom_sheet_corner_radius)
                    )
                )
                .background(color = MaterialTheme.colorScheme.tertiary)
                .clickable(enabled = false) {}
        ) {
            Column {
                BottomSheetItem(
                    text = stringResource(R.string.bottom_sheet_apply_coupon),
                    iconResId = R.drawable.coupon,
                    contentDescription = stringResource(R.string.bottom_sheet_coupon_icon),
                    onClick = {
                        showCouponDialog = true
                    }
                )
                BottomSheetItem(
                    text = stringResource(R.string.bottom_sheet_go_to_subscriptions),
                    iconResId = R.drawable.subscriptions,
                    contentDescription = stringResource(R.string.bottom_sheet_subscriptions_icon),
                    onClick = {
                        isVisible = false
                        val currentProductId = subscriptionsViewModel.currentProductId
                        val packageName = context.packageName
                        val subscriptionUrl = if (currentProductId != null) {
                            "https://play.google.com/store/account/subscriptions?sku=$currentProductId&package=$packageName"
                        } else {
                            "https://play.google.com/store/account/subscriptions"
                        }
                        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(subscriptionUrl))
                        context.startActivity(webIntent)
                        onGoToSubscriptions()
                    }
                )
            }
        }
        
        if (showCouponDialog) {
            DialogBox(
                onDismiss = { 
                    showCouponDialog = false
                    isVisible = false
                },
                onConfirm = { input -> 
                    onApplyCoupon()
                    LogUtils.d(TAG, "Input: $input")
                }
            )
        }
    }
}

@Composable
private fun BottomSheetItem(
    text: String,
    iconResId: Int,
    contentDescription: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(getDimension(R.dimen.more_bottom_sheet_item_height))
            .clickable(onClick = onClick)
            .padding(horizontal = getDimension(R.dimen.more_bottom_sheet_item_padding)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Image(
            painter = painterResource(id = iconResId),
            contentDescription = contentDescription,
            modifier = Modifier.size(getDimension(R.dimen.bottom_sheet_icon_size))
        )
        Spacer(modifier = Modifier.width(getDimension(R.dimen.more_bottom_sheet_item_padding)))
        Text(
            text = text,
            fontSize = getDimensionText(R.dimen.more_bottom_sheet_item_text_size),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.W400,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = getDimensionText(R.dimen.box_plan_renew_size),
                textAlign = TextAlign.Center
            )
        )
    }
} 
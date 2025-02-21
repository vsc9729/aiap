package com.synchronoss.aiap.presentation

import android.app.Activity
import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.synchronoss.aiap.R
import com.synchronoss.aiap.presentation.subscriptions.*
import com.synchronoss.aiap.utils.Constants
import com.synchronoss.aiap.utils.getDimension
import com.synchronoss.aiap.utils.getDimensionText
import com.synchronoss.aiap.utils.LogUtils

@Composable
fun SubscriptionsView(activity: ComponentActivity, modifier: Modifier = Modifier, launchedViaIntent: Boolean) {
    val TAG: String = "SubscriptionsView"
    val subscriptionsViewModel = hiltViewModel<SubscriptionsViewModel>()
    var showDialog by remember { mutableStateOf(false) }
    val logoUrl = subscriptionsViewModel.finalLogoUrl
    val configuration = LocalConfiguration.current
    val logoUrlWidth = getDimension(R.dimen.logo_width)
    val logoUrlHeight = getDimension(R.dimen.logo_height)
    LogUtils.d(TAG, "Logo url is $logoUrl")
    val context = LocalContext.current
    val filteredProductDetails = subscriptionsViewModel.filteredProductDetails

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if(launchedViaIntent) Arrangement.Start else Arrangement.End
        ) {
            IconButton(onClick = { if(launchedViaIntent) (context as? Activity)?.finish() else subscriptionsViewModel.dialogState.value = false }) {
                Icon(
                    imageVector = if(launchedViaIntent) Icons.Default.ArrowBack else Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color(context.getColor(R.color.light_primary_default)),
                    modifier = Modifier.size(getDimension(R.dimen.bottom_sheet_icon_size))
                )
            }
        }
        if(subscriptionsViewModel.noInternetConnectionAndNoCache.value){
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colorScheme.background)
                    .padding(vertical = getDimension(R.dimen.no_data_box)),
                contentAlignment = Alignment.Center
            ){
                Text(text = stringResource(R.string.no_connection_text))
            }
        } else {
            if (!subscriptionsViewModel.isLoading.value)
                Box {
                    Column(modifier = Modifier
                        .fillMaxSize()
                        .background(color = MaterialTheme.colorScheme.background)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = getDimension(R.dimen.spacing_xlarge))
                    ) {
                        if (subscriptionsViewModel.isIosPlatform) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = getDimension(R.dimen.ios_warning_vertical_padding))
                                    .background(
                                        color = Color(context.getColor(R.color.ios_warning_background)),
                                        shape = MaterialTheme.shapes.medium
                                    )
                                    .padding(getDimension(R.dimen.ios_warning_internal_padding))
                            ) {
                                Column {
                                    Row(
                                        horizontalArrangement = Arrangement.Start,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Image(
                                            painter = rememberAsyncImagePainter(R.drawable.important),
                                            contentDescription = "Important Icon",
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(Modifier.width(getDimension(R.dimen.ios_warning_internal_padding)/2))
                                        Text("Important", color = Color(context.getColor(R.color.ios_warning_text_heading)), fontWeight = FontWeight.W600, fontSize = getDimensionText(R.dimen.ios_warning_text_size))
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = getDimension(R.dimen.ios_warning_internal_padding))
                                    ) {
                                        Text(
                                            text = stringResource(R.string.ios_platform_warning),
                                            color = Color(context.getColor(R.color.ios_warning_text)),
                                            fontSize = getDimensionText(R.dimen.ios_warning_text_size),
                                            fontWeight = FontWeight.W400,
                                            textAlign = TextAlign.Left,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(getDimension(R.dimen.ios_warning_spacing)))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(logoUrl),
                                contentDescription = stringResource(R.string.image_url_description),
                                modifier = Modifier
                                    .wrapContentSize()
                                    .width(logoUrlWidth)
                                    .height(logoUrlHeight)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = Constants.STORAGE_TAGLINE,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.W700,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(getDimension(R.dimen.spacing_small)))

                        (subscriptionsViewModel.selectedTab ?: subscriptionsViewModel.productDetails?.let {
                            TabOption.getAvailableTabs(it).first()
                        })?.let {
                            TabSelector(
                                selectedTab = it,
                                onTabSelected = { tab ->
                                    subscriptionsViewModel.selectedTab = tab
                                    subscriptionsViewModel.onTabSelected(tab = tab)
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = modifier.height(getDimension(R.dimen.spacing_small)))
                        ScrollablePlans()
                        Spacer(modifier = modifier.height(getDimension(R.dimen.spacing_small)))
                    }

                    // Bottom action box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .let {
                                if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                                    it.height(
                                        if (subscriptionsViewModel.selectedPlan != -1)
                                            getDimension(R.dimen.box_landscape_selected_height) else getDimension(
                                            R.dimen.box_landscape_not_selected_height
                                        )
                                    )
                                } else {
                                    it.height(
                                        if (subscriptionsViewModel.selectedPlan != -1) getDimension(
                                            R.dimen.box_non_landscape_selected_height
                                        ) else getDimension(R.dimen.box_non_landscape_not_selected_height)
                                    )
                                }
                            }
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
                            .background(color = MaterialTheme.colorScheme.tertiary)
                            .align(Alignment.BottomCenter)
                    ) {
                        when(configuration.orientation) {
                            Configuration.ORIENTATION_PORTRAIT -> {
                                Box(
                                    modifier = Modifier.padding(getDimension(R.dimen.box_portrait_padding))
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.Top,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        if(subscriptionsViewModel.selectedPlan != -1){
                                            val selectedProduct = filteredProductDetails?.get(subscriptionsViewModel.selectedPlan)
                                            val offerDetails = selectedProduct?.subscriptionOfferDetails?.firstOrNull()
                                            val pricingPhases = offerDetails?.pricingPhases?.pricingPhaseList

                                            Text(
                                                text = stringResource(
                                                    R.string.plan_auto_renews,
                                                    selectedProduct?.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice ?: ""
                                                ),
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.W400,
                                                    color = MaterialTheme.colorScheme.onSecondary,
                                                    fontSize = getDimensionText(R.dimen.box_plan_renew_size),
                                                    textAlign = TextAlign.Center
                                                )
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                        }
                                        Button(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(getDimension(R.dimen.continue_btn_height)),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                contentColor = Color.White,
                                                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                                disabledContentColor = Color.White.copy(alpha = 0.4f)
                                            ),
                                            onClick = {
                                                filteredProductDetails?.get(subscriptionsViewModel.selectedPlan)?.let {
                                                    subscriptionsViewModel.purchaseSubscription(
                                                        activity = activity,
                                                        productDetails = it,
                                                        onError = { error ->
                                                            LogUtils.d(TAG, "Error: $error")
                                                        }
                                                    )
                                                }
                                            },
                                            enabled = subscriptionsViewModel.selectedPlan != -1 && !subscriptionsViewModel.isCurrentProductBeingUpdated && !subscriptionsViewModel.isIosPlatform
                                        ) {
                                            Row {
                                                Text(
                                                    text = stringResource(R.string.continue_text),
                                                    fontSize = getDimensionText(R.dimen.continue_btn_font_size),
                                                    fontWeight = FontWeight.W600,
                                                    color = Color.White
                                                )
                                                Spacer(modifier = Modifier.width(getDimension(R.dimen.continue_btn_spacer)))
                                                if (subscriptionsViewModel.isCurrentProductBeingUpdated) {
                                                    CircularProgressIndicator(
                                                        color = Color.White,
                                                        strokeWidth = getDimension(R.dimen.circular_progress_indicator_width),
                                                        modifier = Modifier.size(getDimension(R.dimen.circular_progress_indicator_modifier_size))
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = { showDialog = true },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(getDimension(R.dimen.continue_btn_height)),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color.Transparent,
                                                contentColor = MaterialTheme.colorScheme.primary
                                            )
                                        ) {
                                            Text(
                                                text = stringResource(R.string.more_button_text),
                                                fontSize = getDimensionText(R.dimen.continue_btn_font_size),
                                                color = MaterialTheme.colorScheme.onTertiary,
                                                fontWeight = FontWeight.W600
                                            )
                                        }
                                    }
                                }
                            }
                            Configuration.ORIENTATION_LANDSCAPE -> {
                                Box(
                                    modifier = Modifier.padding(getDimension(R.dimen.box_landscape_padding))
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Spacer(modifier = Modifier.height(getDimension(R.dimen.box_landscape_spacer_height)))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Start
                                        ) {
                                            Button(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(getDimension(R.dimen.landscape_continue_btn_height)),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.primary,
                                                    contentColor = Color.White,
                                                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                                    disabledContentColor = Color.White.copy(alpha = 0.4f)
                                                ),
                                                onClick = {
                                                    filteredProductDetails?.get(subscriptionsViewModel.selectedPlan)?.let {
                                                        subscriptionsViewModel.purchaseSubscription(
                                                            activity = activity,
                                                            productDetails = it,
                                                            onError = { error ->
                                                                LogUtils.d(TAG, "Error: $error")
                                                            }
                                                        )
                                                    }
                                                },
                                                enabled = subscriptionsViewModel.selectedPlan != -1 && !subscriptionsViewModel.isCurrentProductBeingUpdated && !subscriptionsViewModel.isIosPlatform
                                            ) {
                                                Row {
                                                    Text(
                                                        stringResource(R.string.continue_text),
                                                        fontSize = getDimensionText(R.dimen.landscape_continue_btn_font_size),
                                                        fontWeight = FontWeight.W600,
                                                        color = Color.White
                                                    )
                                                    Spacer(modifier = Modifier.width(getDimension(R.dimen.landscape_continue_btn_spacer)))
                                                    if (subscriptionsViewModel.isCurrentProductBeingUpdated) {
                                                        CircularProgressIndicator(
                                                            color = Color.White,
                                                            strokeWidth = getDimension(R.dimen.circular_progress_indicator_width),
                                                            modifier = Modifier.size(getDimension(R.dimen.circular_progress_indicator_modifier_size))
                                                        )
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Button(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(getDimension(R.dimen.landscape_continue_btn_height)),
                                                onClick = { showDialog = true },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color.Transparent,
                                                    contentColor = MaterialTheme.colorScheme.primary
                                                )
                                            ) {
                                                Text(
                                                    text = stringResource(R.string.more_button_text),
                                                    fontSize = getDimensionText(R.dimen.landscape_continue_btn_font_size),
                                                    color = MaterialTheme.colorScheme.onTertiary,
                                                    fontWeight = FontWeight.W600
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            else Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colorScheme.background)
                    .padding(vertical = 16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SkeletonLoader()
            }
        }
    }
    if(showDialog){
        MoreBottomSheet(
            onDismiss = { showDialog = false },
            onApplyCoupon = { LogUtils.d(TAG, "Apply coupon") },
            onGoToSubscriptions = { LogUtils.d(TAG, "Go to subscriptions") }
        )
    }
}

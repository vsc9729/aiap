package com.synchronoss.aiap.presentation

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Paint.Style
import android.net.Uri
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.animation.animateColorAsState

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.synchronoss.aiap.R
import com.synchronoss.aiap.core.domain.models.ProductInfo
import com.synchronoss.aiap.presentation.SubscriptionsViewModel
import com.synchronoss.aiap.presentation.ToastComposable
import com.synchronoss.aiap.utils.Constants
import com.synchronoss.aiap.utils.getDimension
import com.synchronoss.aiap.utils.getDimensionText
import com.synchronoss.aiap.utils.LogUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.android.billingclient.api.ProductDetails

/**
 * Main composable for displaying subscription plans and handling user interactions.
 * Provides a complete subscription interface with plan selection, purchase flow, and coupon handling.
 */
@Composable
fun SubscriptionsView(activity: ComponentActivity, modifier: Modifier = Modifier, launchedViaIntent: Boolean) {
    val TAG: String = "SubscriptionsView"
    val subscriptionsViewModel = hiltViewModel<SubscriptionsViewModel>()
    var showDialog by remember { mutableStateOf(false) }
    val logoUrl = subscriptionsViewModel.finalLogoUrl;
    val configuration = LocalConfiguration.current;
    val logoUrlWidth = getDimension(R.dimen.logo_width)
    val logoUrlHeight = getDimension(R.dimen.logo_height)
    LogUtils.d(TAG, "Logo url is $logoUrl")
    val context = LocalContext.current
    val filteredProductDetails: List<ProductDetails>? = subscriptionsViewModel.filteredProductDetails

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = if(launchedViaIntent) Arrangement.Start else Arrangement.End
        ) {
            IconButton(onClick = { if(launchedViaIntent) (context as? Activity)?.finish()  else subscriptionsViewModel.dialogState.value = false }) {
                Icon(
                    imageVector = if(launchedViaIntent) Icons.Default.ArrowBack else Icons.Default.Close,
                    contentDescription = "Close",
                    tint = if (isSystemInDarkTheme()) Color.White else Color(
                        0xFF6B7280
                    ),
                    modifier = Modifier.size(getDimension(R.dimen.bottom_sheet_icon_size))
                )
            }
        }
        if(subscriptionsViewModel.noInternetConnectionAndNoCache.value){
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = if (isSystemInDarkTheme()) Color(0xFF0D0D0D) else Color.White)
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
//                            Spacer(modifier = Modifier.height(16.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = getDimension(R.dimen.ios_warning_vertical_padding))
                                    .background(
                                        color = Color(context.getColor(R.color.ios_warning_background)),
                                        shape = RoundedCornerShape(getDimension(R.dimen.ios_warning_corner_radius))
                                    )
                                    .border(
                                        width = getDimension(R.dimen.ios_warning_border_width),
                                        color = Color(context.getColor(R.color.ios_warning_border)),
                                        shape = RoundedCornerShape(getDimension(R.dimen.ios_warning_corner_radius))
                                    )
                                    .padding(getDimension(R.dimen.ios_warning_internal_padding))
                            ) {
                                Column{
                                    Row (
                                        horizontalArrangement = Arrangement.Start,
                                        verticalAlignment = Alignment.CenterVertically
                                    ){
                                        Image(
                                            painter = painterResource(id = R.drawable.important),
                                            contentDescription = "Important Icon",
                                            modifier = Modifier.size(12.dp),
                                        )
                                        Spacer(Modifier.width(getDimension(R.dimen.ios_warning_internal_padding)/2))
                                        Text("Important", color = Color(context.getColor(R.color.ios_warning_text_heading)), fontWeight = FontWeight.W600, fontSize = getDimensionText(R.dimen.ios_warning_text_size  ))
                                    }
//                                    Spacer(Modifier.height(getDimension(R.dimen.ios_warning_spacing)/2))
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
                            modifier = Modifier
                                .fillMaxWidth(),
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
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = Constants.STORAGE_TAGLINE,
                                style = LocalTextStyle.current.copy(
                                    fontWeight = FontWeight.W700,
                                    fontSize = getDimensionText(R.dimen.size_text_storage_tagline_medium),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(getDimension(R.dimen.spacing_small)))
                        (subscriptionsViewModel.selectedTab?: subscriptionsViewModel.productDetails?.let {
                            TabOption.getAvailableTabs(
                                it
                            ).first()
                        })?.let {
                            TabSelector(
                                selectedTab = it,
                                onTabSelected = { tab ->
                                    subscriptionsViewModel.selectedTab = tab
                                    subscriptionsViewModel.onTabSelected(tab= tab)
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(modifier = modifier.height(getDimension(R.dimen.spacing_small)))
                        
                        ScrollablePlans()
                        Spacer(modifier = modifier.height(getDimension(R.dimen.spacing_small)))
                    }
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
                            .clip(
                                RoundedCornerShape(
                                    topEnd = getDimension(R.dimen.box_top_corner),
                                    topStart = getDimension(R.dimen.box_top_corner)
                                )
                            )
                            .background(color = MaterialTheme.colorScheme.tertiary)
                            .align(Alignment.BottomCenter)
                    )
                    {
                        when(configuration.orientation) {
                            Configuration.ORIENTATION_PORTRAIT -> {
                                Box(
                                    modifier = Modifier
                                        .padding(top = getDimension(R.dimen.box_portrait_padding), bottom = getDimension(R.dimen.box_portrait_padding), start =getDimension(R.dimen.box_portrait_padding), end =getDimension(R.dimen.box_portrait_padding))
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
                                            
                                            // Display auto-renewal text
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
                                            Spacer(
                                                modifier = Modifier.height(8.dp)
                                            )
                                        }
                                        Button(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(getDimension(R.dimen.continue_btn_height))
                                                .clip(RoundedCornerShape(getDimension(R.dimen.continue_btn_shape))),
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
                                                Spacer(
                                                    modifier = Modifier.width(getDimension(R.dimen.continue_btn_spacer))
                                                )
                                                if (subscriptionsViewModel.isCurrentProductBeingUpdated) {
                                                    CircularProgressIndicator(
                                                        color = Color.White,
                                                        strokeWidth = getDimension(R.dimen.circular_progress_indicator_width),
                                                        modifier = Modifier.size(getDimension(R.dimen.circular_progress_indicator_modifier_size))
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(
                                            modifier = Modifier.height(8.dp)
                                        )
                                        Button(
                                            onClick = {
                                                showDialog = true
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(getDimension(R.dimen.continue_btn_height))
                                                .clip(RoundedCornerShape(getDimension(R.dimen.continue_btn_shape))),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color.Transparent,
                                                contentColor = MaterialTheme.colorScheme.primary
                                            ),
//                                            border = BorderStroke(
//                                                width = getDimension(R.dimen.card_border_width_selected),
//                                                color = Color(0xff9CA3AF)
//                                            )
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
                                    modifier = Modifier
                                        .padding(top = getDimension(R.dimen.box_landscape_padding), bottom = getDimension(R.dimen.box_landscape_padding), start = getDimension(R.dimen.box_landscape_padding), end = getDimension(R.dimen.box_landscape_padding))
                                ) {

                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally

                                    ) {
//                            Text(
//                                text = "Plan auto-renews for ₹1000 every month. You can cancel anytime you want.",
//                                style = MaterialTheme.typography.bodyMedium.copy(
//                                    fontWeight = FontWeight.W400,
//                                    color = MaterialTheme.colorScheme.onSecondary,
//                                    fontSize = 12.sp,
//                                    textAlign = TextAlign.Center
//                                )
                                        Spacer(
                                            modifier = Modifier.height(getDimension(R.dimen.box_landscape_spacer_height))
                                        )
                                        Row(
                                            modifier=Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Start

                                        ) {
                                            Button(
                                                modifier = Modifier
                                                    //.fillMaxWidth()
                                                    .weight(1f)
                                                    .height(getDimension(R.dimen.landscape_continue_btn_height))
                                                    .clip(RoundedCornerShape(getDimension(R.dimen.landscape_continue_btn_shape))),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.primary, // Normal state color
                                                    contentColor = Color.White, // Normal text/icon color
                                                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(
                                                        alpha = 0.4f
                                                    ),
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
                                                    Spacer(
                                                        modifier = Modifier.width(getDimension(R.dimen.landscape_continue_btn_spacer))
                                                    )
                                                    if (subscriptionsViewModel.isCurrentProductBeingUpdated) {
                                                        CircularProgressIndicator(
                                                            color = Color.White,
                                                            strokeWidth = getDimension(R.dimen.circular_progress_indicator_width),
                                                            modifier = Modifier.size(getDimension(R.dimen.circular_progress_indicator_modifier_size))
                                                        )
                                                    }
                                                }
                                            }
                                            Spacer(
                                                modifier = Modifier.width(8.dp)
                                            )
                                            Button(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(getDimension(R.dimen.landscape_continue_btn_height))
                                                    .clip(RoundedCornerShape(getDimension(R.dimen.landscape_continue_btn_shape))),
                                                onClick = {
                                                    showDialog = true
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color.Transparent,
                                                    contentColor = MaterialTheme.colorScheme.primary
                                                ),
//                                                border = BorderStroke(
//                                                    width = getDimension(R.dimen.card_border_width_selected),
//                                                    color = Color(0xff9CA3AF)
//                                                )
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

            else Column (
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colorScheme.background)
                    .padding(vertical = 16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally

            ) {
                //Add skeleton loader here
                SkeletonLoader()
            }
        }
    }
    if(showDialog){
        MoreBottomSheet(
            onDismiss = {showDialog = false},
            onApplyCoupon = { LogUtils.d(TAG, "Apply coupon") },
            onGoToSubscriptions = { LogUtils.d(TAG, "Go to subscriptions") }
        )
    }

}

/**
 * Displays scrollable subscription plans including current and available plans.
 * Handles plan selection and display of pricing information.
 */
@Composable
fun ScrollablePlans() {
    val subscriptionsViewModel = hiltViewModel<SubscriptionsViewModel>()
    val filteredProductDetails = subscriptionsViewModel.filteredProductDetails
    val currentProductDetails = subscriptionsViewModel.currentProductDetails

    if (filteredProductDetails.isNullOrEmpty()) {
        Text(
            text = stringResource(R.string.no_products_available),
            style = LocalTextStyle.current.copy(
                fontWeight = FontWeight.W700,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center
            )
        )
        return
    }

    //Display current plan
    if(currentProductDetails != null ) {
        val currentProductTab = when {
            currentProductDetails.subscriptionOfferDetails?.last()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.billingPeriod?.endsWith("W") == true -> TabOption.WEEKlY
            currentProductDetails.subscriptionOfferDetails?.last()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.billingPeriod?.endsWith("M") == true -> TabOption.MONTHLY
            else -> TabOption.YEARLY
        }
        Spacer(modifier = Modifier.height(16.dp))
        if(subscriptionsViewModel.selectedTab == currentProductTab){
            ActualCurrentPlanCard(productDetails = currentProductDetails)
            Spacer(modifier = Modifier.height(16.dp))
        }
    } else {
        Spacer(modifier = Modifier.height(16.dp))
    }
    
    // Display other plans
    filteredProductDetails.forEachIndexed { productIndex, productDetails ->
        if (productDetails.productId != subscriptionsViewModel.currentProductId) {
            // Get subscription offers for this product
            val offers = productDetails.subscriptionOfferDetails
            if (offers != null) {
                // Determine which offer to show based on the number of offers
                val offerToShow = if (offers.size > 1) {
                    offers[offers.size - 2]  // Get second to last offer if multiple offers exist
                } else {
                    offers.last()  // Get last offer if only one exists
                }

                // Get the final recurring phase of the selected offer
                val finalPhase = offerToShow.pricingPhases.pricingPhaseList.last()
                val selectedTab = subscriptionsViewModel.selectedTab

                // Check if this offer should be shown in current tab based on final billing period
                val shouldShowInCurrentTab = when (selectedTab) {
                    TabOption.WEEKlY -> finalPhase.billingPeriod.endsWith("W")
                    TabOption.MONTHLY -> finalPhase.billingPeriod.endsWith("M")
                    TabOption.YEARLY -> finalPhase.billingPeriod.endsWith("Y")
                    else -> false
                }

                if (shouldShowInCurrentTab) {
                    // Calculate unique index for this offer
                    
                    OtherPlanCard(
                        productDetails = productDetails,
                        offerDetails = offerToShow,
                        productIndex = productIndex
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(135.dp))
}

/**
 * Displays the current active subscription plan card.
 * @param productDetails The currently active subscription product
 */
@Composable
fun ActualCurrentPlanCard(productDetails: ProductDetails) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(getDimension(R.dimen.current_plan_card_height))
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(getDimension(R.dimen.card_corner_radius))
            )
            .border(
                border = BorderStroke(
                    getDimension(R.dimen.card_border_width),
                    color = MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(getDimension(R.dimen.card_corner_radius))
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ){
            Row(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(
                            topStart = getDimension(R.dimen.card_corner_radius),
                            topEnd = getDimension(R.dimen.card_corner_radius)
                        )
                    )
                    .fillMaxWidth()
                    .padding(
                        vertical = getDimension(R.dimen.card_padding_vertical),
                        horizontal = getDimension(R.dimen.card_padding_horizontal)
                    ),
            ) {
                Text(
                    text = stringResource(R.string.current_subscription),
                    fontSize = getDimensionText(R.dimen.text_size_current_plan_header),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.W600,
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Content Column
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
                ) {
                    val offerDetails = productDetails.subscriptionOfferDetails?.firstOrNull()
                    val price = offerDetails?.pricingPhases?.pricingPhaseList?.lastOrNull()?.formattedPrice ?: ""
                    
                    Text(
                        text = price,
                        fontSize = getDimensionText(R.dimen.text_size_plan_price),
                        fontWeight = FontWeight.W700,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = getDimension(R.dimen.card_content_spacing))
                    )

                    Text(
                        text = productDetails.description,
                        fontSize = getDimensionText(R.dimen.text_size_plan_description),
                        fontWeight = FontWeight.W400,
                        color = MaterialTheme.colorScheme.onSecondary,
                        lineHeight = getDimensionText(R.dimen.text_line_height)
                    )
                }
                Image(
                    painter = painterResource(id = R.drawable.checkmark_circle),
                    contentDescription = stringResource(R.string.selected_plan_indicator),
                    modifier = Modifier
                        .size(getDimension(R.dimen.icon_size)),
                    alignment = Alignment.Center
                )
            }
        }
    }
}

/**
 * Displays available subscription plan cards for selection.
 * @param productDetails The subscription product to display
 * @param offerDetails The specific offer details to display
 * @param productIndex Index for tracking selected plan
 */
@Composable
fun OtherPlanCard(
    productDetails: ProductDetails,
    offerDetails: ProductDetails.SubscriptionOfferDetails,
    productIndex: Int
) {
    val TAG  = "OtherPlanCard"
    val subscriptionsViewModel = hiltViewModel<SubscriptionsViewModel>()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(getDimension(R.dimen.other_plan_card_height))
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(getDimension(R.dimen.card_corner_radius))
            )
            .then(
                if (subscriptionsViewModel.selectedPlan == productIndex) {
                    LogUtils.d(TAG, "Current Plan: ${subscriptionsViewModel.selectedPlan}")
                    Modifier
                        .shadow(
                            elevation = 1.dp,
                            shape = RoundedCornerShape(getDimension(R.dimen.card_corner_radius)),
                        )
                        .background(
                            color = MaterialTheme.colorScheme.background,
                            shape = RoundedCornerShape(getDimension(R.dimen.card_corner_radius))
                        )
                        .border(
                            border = BorderStroke(
                                getDimension(R.dimen.card_border_width_selected),
                                color = MaterialTheme.colorScheme.outlineVariant
                            ),
                            shape = RoundedCornerShape(getDimension(R.dimen.card_corner_radius))
                        )
                } else {
                    Modifier.border(
                        border = BorderStroke(
                            getDimension(R.dimen.card_border_width),
                            color = MaterialTheme.colorScheme.outline
                        ),
                        shape = RoundedCornerShape(getDimension(R.dimen.card_corner_radius))
                    )
                }
            )
            .clickable {
                subscriptionsViewModel.selectedPlan = productIndex
                LogUtils.d(TAG, "Current Plan: ${subscriptionsViewModel.selectedPlan}")
            }
            .padding(horizontal = getDimension(R.dimen.card_padding_horizontal))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Content Column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                val pricingPhases = offerDetails.pricingPhases.pricingPhaseList
                val finalPhase = pricingPhases.last()  // Always show the final price
                
                Text(
                    text = finalPhase.formattedPrice,
                    fontSize = getDimensionText(R.dimen.text_size_plan_price),
                    fontWeight = FontWeight.W700,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = getDimension(R.dimen.card_content_spacing))
                )

                Text(
                    text = productDetails.description,
                    fontSize = getDimensionText(R.dimen.text_size_plan_description),
                    fontWeight = FontWeight.W400,
                    color = MaterialTheme.colorScheme.onSecondary,
                    lineHeight = getDimensionText(R.dimen.text_line_height)
                )

                // Show special offer text if there are multiple pricing phases
                if (pricingPhases.size > 1) {
                    val firstPhase = pricingPhases.first()
                    val offerText = when {
                        firstPhase.formattedPrice == "₹0.00" || firstPhase.formattedPrice == "$0.00" -> {
                            val period = firstPhase.billingPeriod.replace("P", "")
                            val (count, type) = period.partition { it.isDigit() }
                            val periodText = when (type) {
                                "D" -> if (count.toInt() > 1) "$count Days" else "$count Day"
                                "W" -> if (count.toInt() > 1) "$count Weeks" else "$count Week"
                                "M" -> if (count.toInt() > 1) "$count Months" else "$count Month"
                                "Y" -> if (count.toInt() > 1) "$count Years" else "$count Year"
                                else -> period
                            }
                            stringResource(R.string.free_trial_offer, periodText)
                        }
                        else -> {
                            val period = firstPhase.billingPeriod.replace("P", "")
                            val (count, type) = period.partition { it.isDigit() }
                            val periodText = when (type) {
                                "D" -> if (count.toInt() > 1) "$count Days" else "$count Day"
                                "W" -> if (count.toInt() > 1) "$count Weeks" else "$count Week"
                                "M" -> if (count.toInt() > 1) "$count Months" else "$count Month"
                                "Y" -> if (count.toInt() > 1) "$count Years" else "$count Year"
                                else -> period
                            }
                            stringResource(R.string.introductory_price_info, firstPhase.formattedPrice, periodText)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = offerText,
                        fontSize = getDimensionText(R.dimen.text_size_plan_intro_price),
                        fontWeight = FontWeight.W400,
                        color = MaterialTheme.colorScheme.primary,
                        lineHeight = getDimensionText(R.dimen.text_line_height)
                    )
                }
            }

            // Checkmark Icon
            if (subscriptionsViewModel.selectedPlan == productIndex) {
                Image(
                    painter = painterResource(id = R.drawable.radio_button_checked),
                    contentDescription = stringResource(R.string.selected_plan_indicator),
                    modifier = Modifier
                        .size(getDimension(R.dimen.icon_size)),
                    alignment = Alignment.Center,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                )
            }
            else{
                Image(
                    painter = painterResource(id = R.drawable.radio_button),
                    contentDescription = stringResource(R.string.unselected_plan_indicator),
                    modifier = Modifier
                        .size(getDimension(R.dimen.icon_size)),
                )
            }
        }
    }
}

enum class TabOption {
    WEEKlY,
    MONTHLY,
    YEARLY;

    companion object {
        fun getAvailableTabs(productDetails: List<ProductDetails>): List<TabOption> {
            val orderedTabs = mutableListOf<TabOption>()

            // Check for weekly plans first
            if (productDetails.any { details ->
                    details.subscriptionOfferDetails?.last()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.billingPeriod?.endsWith("W") == true
                }) {
                orderedTabs.add(WEEKlY)
            }

            // Then check for monthly plans
            if (productDetails.any { details ->
                    details.subscriptionOfferDetails?.last()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.billingPeriod?.endsWith("M") == true
                }) {
                orderedTabs.add(MONTHLY)
            }

            // Finally check for yearly plans
            if (productDetails.any { details ->
                    details.subscriptionOfferDetails?.last()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.billingPeriod?.endsWith("Y") == true
                }) {
                orderedTabs.add(YEARLY)
            }

            return orderedTabs
        }
    }
}

@Composable
fun TabSelector(
    selectedTab: TabOption,
    onTabSelected: (TabOption) -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val subscriptionsViewModel = hiltViewModel<SubscriptionsViewModel>()
    val availableTabs = subscriptionsViewModel.productDetails?.let { TabOption.getAvailableTabs(it) } ?: emptyList()

    if (availableTabs.isNotEmpty()) {
        Surface(
            modifier = modifier
                .let {
                    if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        it.height(getDimension(R.dimen.tab_height_landscape))
                    } else {
                        it.height(getDimension(R.dimen.tab_height_portrait))
                    }
                }
                .clip(RoundedCornerShape(getDimension(R.dimen.tab_selector_corner_radius)))
                .background(color = MaterialTheme.colorScheme.secondary),
            color = MaterialTheme.colorScheme.secondary
        ) {
            Row(
                modifier = Modifier.padding(getDimension(R.dimen.tab_selector_padding)),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                availableTabs.forEach { tab ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(getDimension(R.dimen.tab_item_corner_radius)))
                            .background(
                                if (tab == selectedTab) MaterialTheme.colorScheme.primary
                                else Color.Transparent
                            )
                            .clickable { onTabSelected(tab) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.name.lowercase().replaceFirstChar { it.uppercase() },
                            color = if (tab == selectedTab) Color.White else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.W600
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MoreBottomSheet(
    onDismiss: () -> Unit,
    onApplyCoupon: () -> Unit,
    onGoToSubscriptions: () -> Unit
) {
    val TAG = "MoreBottomSheet"
    var showCouponDialog by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    val screenHeight = configuration.screenHeightDp.dp
    val bottomSheetHeight = screenHeight * (0.147f)
    val subscriptionsViewModel = hiltViewModel<SubscriptionsViewModel>()
    // Start animation immediately when composable is created
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
//                BottomSheetItem(
//                    text = stringResource(R.string.bottom_sheet_restore_purchase),
//                    iconResId = R.drawable.restore,
//                    contentDescription = stringResource(R.string.bottom_sheet_restore_icon),
//                    onClick = {
//                        isVisible = false
//                        onRestorePurchase()
//                    }
//                )
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
            modifier = Modifier.size(getDimension(R.dimen.bottom_sheet_icon_size)),
//            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondary)
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

/**
 * Dialog for entering and applying coupon codes.
 * @param onDismiss Callback when dialog is dismissed
 * @param onConfirm Callback when coupon is confirmed
 */
@Composable
fun DialogBox(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .height(getDimension(R.dimen.dialog_height))
                .width(getDimension(R.dimen.dialog_width))
                .padding(getDimension(R.dimen.dialog_padding))
                .clickable(enabled = false) {},  // Prevent click propagation
            shape = RoundedCornerShape(getDimension(R.dimen.dialog_corner_radius)),
            colors = CardDefaults.cardColors(
                containerColor = Color.White,
            ),
        ) {
            Column(
                modifier = Modifier.padding(getDimension(R.dimen.dialog_content_padding)),
                verticalArrangement = Arrangement.spacedBy(getDimension(R.dimen.dialog_spacing)),
            ) {
                Text(
                    text = stringResource(R.string.dialog_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black,
                    fontWeight = FontWeight.W400
                )

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(
                        text = stringResource(R.string.dialog_coupon_label),
                        color = Color.Gray
                    ) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                    ),
                    textStyle = TextStyle(color = Color.Black)
                )

                Spacer(modifier = Modifier.height(getDimension(R.dimen.dialog_spacer_height)))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        )
                    ) {
                        Text(stringResource(R.string.dialog_cancel))
                    }

                    Spacer(modifier = Modifier.width(getDimension(R.dimen.dialog_spacing)))

                    Button(
                        onClick = {
                            onConfirm(text)
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/redeem?code=$text"))
                            context.startActivity(intent)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        )
                    ) {
                        Text(stringResource(R.string.dialog_ok))
                    }
                }
            }
        }
    }
}
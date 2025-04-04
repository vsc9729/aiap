package com.synchronoss.aiap.presentation.subscriptions.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.synchronoss.aiap.R
import com.synchronoss.aiap.core.di.DaggerAiapComponent
import com.synchronoss.aiap.presentation.subscriptions.wrapper.ScrollablePlansWrapper
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.android.billingclient.api.ProductDetails
import com.synchronoss.aiap.utils.getDimension
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.fillMaxWidth
import com.synchronoss.aiap.utils.LogUtils

@Composable
fun ScrollablePlans(
    activity: ComponentActivity,
    enableDarkTheme: Boolean = isSystemInDarkTheme(),
    shouldScroll: Boolean = true, // Default is true for backward compatibility
    scrollState: ScrollState = rememberScrollState() // Default scroll state if not provided
) {
    val wrapper = remember {
        val wrapper = ScrollablePlansWrapper()
        val application = activity.application
        val aiapComponent = DaggerAiapComponent.factory().create(application)
        aiapComponent.inject(wrapper)
        wrapper
    }
    
    val subscriptionsViewModel = remember {
        wrapper.getViewModel(activity)
    }
    
    val filteredProductDetails = subscriptionsViewModel.filteredProductDetails
    val currentProductDetails = subscriptionsViewModel.currentProductDetails
    val currentProductInfo = subscriptionsViewModel.currentProduct
    val selectedTab = subscriptionsViewModel.selectedTab
    val isProductInIos = subscriptionsViewModel.isIosPlatform
    val unacknowledgedProductDetails = subscriptionsViewModel.unacknowledgedProductDetails
    
    if (filteredProductDetails.isNullOrEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Spacer(modifier = Modifier.height(200.dp))
                Text(
                    text = stringResource(R.string.no_products_available),
                    style = LocalTextStyle.current.copy(
                        fontWeight = FontWeight.W700,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onPrimary,
                        textAlign = TextAlign.Center
                    )
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    } else {
        // Apply scrolling only when shouldScroll is true
        Column(
            modifier = if (shouldScroll) {
                Modifier
                    .verticalScroll(scrollState)
                    // Add a key to force recomposition when content changes
                    .fillMaxWidth()
            } else {
                Modifier.fillMaxWidth()
            }
        ) {
            // Display current product if exists and matches the selected tab's billing period
            if ((currentProductDetails != null || ( currentProductInfo == null && unacknowledgedProductDetails != null)) && !isProductInIos) {
                val highlightedProductDetails: ProductDetails = currentProductDetails ?: run {
                    if (unacknowledgedProductDetails != null) {
                        unacknowledgedProductDetails
                    } else {
                        LogUtils.e("ScrollablePlans", "Both currentProductDetails and unacknowledgedProductDetails are null")
                        return@Column
                    }
                }
                val highlightedProductBillingPeriod = highlightedProductDetails.subscriptionOfferDetails?.last()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.billingPeriod
                val shouldShowCurrentPlan = when (selectedTab) {
                    TabOption.WEEKlY -> highlightedProductBillingPeriod?.endsWith("W") == true
                    TabOption.MONTHLY -> highlightedProductBillingPeriod?.endsWith("M") == true
                    TabOption.YEARLY -> highlightedProductBillingPeriod?.endsWith("Y") == true
                    else -> false
                }
                
                if (shouldShowCurrentPlan) {
                    ActualCurrentPlanCard(
                        productDetails = highlightedProductDetails,
                        enableDarkTheme = enableDarkTheme,
                        isPending = currentProductDetails == null
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Display other product options
            filteredProductDetails.forEachIndexed { index, product ->
                // Skip current product since it's already displayed if it matches the tab
                if ((product.productId != currentProductDetails?.productId) && (product.productId != unacknowledgedProductDetails?.productId)) {
                    val offerDetails = product.subscriptionOfferDetails?.firstOrNull()
                    if (offerDetails != null) {
                        OtherPlanCard(
                            productDetails = product,
                            offerDetails = offerDetails,
                            productIndex = index,
                            activity = activity,
                            enableDarkTheme = enableDarkTheme
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            // Add some empty space at the bottom for better UX
            Spacer(modifier = Modifier.height(getDimension(R.dimen.scrollable_plans_empty_space)))
        }
    }
} 
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

@Composable
fun ScrollablePlans(activity: ComponentActivity) {
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
        Column {
            // Display current product if exists and matches the selected tab's billing period
            if (currentProductDetails != null && currentProductInfo != null) {
                val currentBillingPeriod = currentProductDetails.subscriptionOfferDetails?.last()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.billingPeriod
                val shouldShowCurrentPlan = when (selectedTab) {
                    TabOption.WEEKlY -> currentBillingPeriod?.endsWith("W") == true
                    TabOption.MONTHLY -> currentBillingPeriod?.endsWith("M") == true
                    TabOption.YEARLY -> currentBillingPeriod?.endsWith("Y") == true
                    else -> false
                }
                
                if (shouldShowCurrentPlan) {
                    ActualCurrentPlanCard(productDetails = currentProductDetails, productInfo = currentProductInfo)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Display other product options
            filteredProductDetails.forEachIndexed { index, product ->
                // Skip current product since it's already displayed if it matches the tab
                if (product.productId != currentProductDetails?.productId) {
                    val offerDetails = product.subscriptionOfferDetails?.firstOrNull()
                    if (offerDetails != null) {
                        OtherPlanCard(
                            productDetails = product,
                            offerDetails = offerDetails,
                            productIndex = index,
                            activity = activity,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
} 
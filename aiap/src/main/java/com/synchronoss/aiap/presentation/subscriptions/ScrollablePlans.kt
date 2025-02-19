package com.synchronoss.aiap.presentation.subscriptions

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.synchronoss.aiap.R
import com.synchronoss.aiap.presentation.SubscriptionsViewModel

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

    filteredProductDetails.forEachIndexed { productIndex, productDetails ->
        if (productDetails.productId != subscriptionsViewModel.currentProductId) {
            val offers = productDetails.subscriptionOfferDetails
            if (offers != null) {
                val offerToShow = if (offers.size > 1) {
                    offers[offers.size - 2]
                } else {
                    offers.last()
                }

                val finalPhase = offerToShow.pricingPhases.pricingPhaseList.last()
                val selectedTab: TabOption? = subscriptionsViewModel.selectedTab

                val shouldShowInCurrentTab = when (selectedTab) {
                    TabOption.WEEKlY -> finalPhase.billingPeriod.endsWith("W")
                    TabOption.MONTHLY -> finalPhase.billingPeriod.endsWith("M")
                    TabOption.YEARLY -> finalPhase.billingPeriod.endsWith("Y")
                    else -> false
                }

                if (shouldShowInCurrentTab) {
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
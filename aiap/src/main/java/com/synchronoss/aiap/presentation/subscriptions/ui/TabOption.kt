package com.synchronoss.aiap.presentation.subscriptions.ui

import com.android.billingclient.api.ProductDetails

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
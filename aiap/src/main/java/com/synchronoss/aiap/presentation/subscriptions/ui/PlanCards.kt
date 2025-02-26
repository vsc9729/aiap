package com.synchronoss.aiap.presentation.subscriptions.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.billingclient.api.ProductDetails
import com.synchronoss.aiap.R
import com.synchronoss.aiap.core.di.DaggerAiapComponent
import com.synchronoss.aiap.presentation.subscriptions.wrapper.PlanCardsWrapper
import com.synchronoss.aiap.utils.LogUtils
import com.synchronoss.aiap.utils.getDimension
import com.synchronoss.aiap.utils.getDimensionText

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

@Composable
fun OtherPlanCard(
    productDetails: ProductDetails,
    offerDetails: ProductDetails.SubscriptionOfferDetails,
    productIndex: Int,
    activity: ComponentActivity
) {
    val TAG = "OtherPlanCard"
    
    val wrapper = remember {
        val wrapper = PlanCardsWrapper()
        val application = activity.application
        val aiapComponent = DaggerAiapComponent.factory().create(application)
        aiapComponent.inject(wrapper)
        wrapper
    }
    
    val subscriptionsViewModel = remember {
        wrapper.getViewModel(activity)
    }

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
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                val pricingPhases = offerDetails.pricingPhases.pricingPhaseList
                val finalPhase = pricingPhases.last()
                
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
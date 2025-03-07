package com.synchronoss.aiap.presentation.subscriptions.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.android.billingclient.api.ProductDetails
import com.synchronoss.aiap.R
import com.synchronoss.aiap.core.di.DaggerAiapComponent
import com.synchronoss.aiap.core.domain.models.ProductInfo
import com.synchronoss.aiap.presentation.subscriptions.wrapper.PlanCardsWrapper
import com.synchronoss.aiap.utils.LogUtils
import com.synchronoss.aiap.utils.getDimension
import com.synchronoss.aiap.utils.getDimensionText


fun getTimePeriod(input: String): String{
    val inp = input[1]
    return when {
        input.endsWith("D") -> "${if(inp == '1') "" else "$inp "}day${if (inp == '1')"" else "s"}"
        input.endsWith("W") -> "${if(inp == '1') "" else "$inp "}week${if (inp == '1')"" else "s"}"
        input.endsWith("M") -> "${if(inp == '1') "" else "$inp "}month${if (inp == '1')"" else "s"}"
        else -> "${if(inp == '1') "" else "$inp "}year${if (inp == '1')"" else "s"}"
    }
}

@Composable
fun ActualCurrentPlanCard(
    productDetails: ProductDetails, 
    productInfo: ProductInfo, 
    isPending: Boolean = false,
    enableDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val context = LocalContext.current
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
            .testTag("current_plan_card")

    ) {
        Row (
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()

            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(35.dp)
                        //                    .weight(1f)
                        .background(
                            color = when{
                                isPending -> Color(context.getColor(R.color.pending_subscription_background))
                                else -> if(enableDarkTheme)Color(context.getColor(R.color.current_subscription_text)) else Color(context.getColor(R.color.current_subscription_background))
                            },
                            RoundedCornerShape(
                                topStart = getDimension(R.dimen.card_corner_radius),
                                topEnd = getDimension(R.dimen.card_corner_radius)
                            )
                        )
                        .fillMaxWidth()
                        .padding(horizontal = getDimension(R.dimen.card_padding_horizontal)),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    //                modifier = Modifier
                    //                    .padding(
                    //                        vertical = getDimension(R.dimen.card_padding_vertical),
                    //                        horizontal = getDimension(R.dimen.card_padding_horizontal)
                    //                    ),
                ) {
                    Text(
                        text = stringResource(
                            when{
                                isPending -> R.string.pending_subscription
                                else -> R.string.current_subscription
                            }
                        ),
                        fontSize = getDimensionText(R.dimen.text_size_current_plan_header),
                        color = when{
                            isPending -> Color(context.getColor(R.color.pending_subscription_text))
                            else -> if(enableDarkTheme)Color(context.getColor(R.color.current_subscription_background)) else Color(context.getColor(R.color.current_subscription_text))
                        },
                        fontWeight = FontWeight.W600,
                    )
                }
                Row{
                    Column (
                        verticalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .weight(4f)
                            .height(getDimension(R.dimen.other_plan_card_height))
                            .padding(
                                horizontal = getDimension(R.dimen.card_padding_horizontal),
                                vertical = getDimension(R.dimen.card_padding_vertical)
                            )
                    ){
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                        ) {
                            val pricingPhases =
                                productDetails.subscriptionOfferDetails?.first()?.pricingPhases?.pricingPhaseList!!
                            val finalPhase = pricingPhases.last()

                            Text(
                                text = productDetails.name,
                                fontSize = getDimensionText(R.dimen.text_size_plan_price),
                                fontWeight = FontWeight.W700,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(bottom = getDimension(R.dimen.card_content_spacing))
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            val formattedPrice = productDetails.subscriptionOfferDetails
                                ?.firstOrNull()
                                ?.pricingPhases
                                ?.pricingPhaseList
                                ?.last()
                                ?.formattedPrice ?: ""

                            val billingPeriod = productDetails.subscriptionOfferDetails
                                ?.firstOrNull()
                                ?.pricingPhases
                                ?.pricingPhaseList
                                ?.last()
                                ?.billingPeriod?.let { getTimePeriod(it) } ?: ""

                            // Get raw formatted string from strings.xml
                            val rawString =
                                stringResource(R.string.plan_auto_renews, formattedPrice, billingPeriod)

                            // Build an AnnotatedString for styled text
                            val formattedText = buildAnnotatedString {
                                val firstIndex = rawString.indexOf(formattedPrice)
                                val secondIndex =
                                    rawString.indexOf(billingPeriod, firstIndex + formattedPrice.length)

                                append(rawString)

                                // Make the first %1$s (formattedPrice) bold
                                if (firstIndex >= 0) {
                                    addStyle(
                                        style = SpanStyle(
                                            fontWeight = FontWeight.Bold,
                                            color = if(enableDarkTheme)  Color.White else  Color.Black
                                        ),
                                        start = firstIndex,
                                        end = firstIndex + formattedPrice.length
                                    )
                                }
                            }

                            // Display styled text
                            Text(
                                text = formattedText,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.W400, // Default weight (except for bold part)
                                    color = MaterialTheme.colorScheme.onSecondary,
                                    fontSize = getDimensionText(R.dimen.box_plan_renew_size),
                                    textAlign = TextAlign.Center
                                )
                            )

                        }
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(getDimension(R.dimen.other_plan_card_height))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(getDimension(R.dimen.other_plan_card_height)),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ){
                            if(!isPending) {
                                Image(
                                    painter = rememberAsyncImagePainter(R.drawable.checkmark_circle),
                                    contentDescription = "Checkmark Icon",
                                    modifier = Modifier.size(25.dp)
                                )
                            }
                        }
                    }

                }
            }

        }
    }
}

@Composable
fun OtherPlanCard(
    productDetails: ProductDetails,
    offerDetails: ProductDetails.SubscriptionOfferDetails,
    productIndex: Int,
    activity: ComponentActivity,
    enableDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val TAG = "OtherPlanCard"
    val context = LocalContext.current
    
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

    val productInfo: ProductInfo? = subscriptionsViewModel.products?.find { it.productId == productDetails.productId }
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
            .padding(
                horizontal = getDimension(R.dimen.card_padding_horizontal),
                vertical = getDimension(R.dimen.card_padding_vertical)
            )
            .testTag(if (subscriptionsViewModel.selectedPlan == productIndex) "selected_plan_card" else "unselected_plan_card")
    ) {

        Column(

            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                    val pricingPhases = offerDetails.pricingPhases.pricingPhaseList
                    val finalPhase = pricingPhases.last()

                    Text(
                        text = productDetails.name,
                        fontSize = getDimensionText(R.dimen.text_size_plan_price),
                        fontWeight = FontWeight.W700,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = getDimension(R.dimen.card_content_spacing))
                    )

                    if (pricingPhases.size > 1) {
                        val firstPhase = pricingPhases.first()
                        val lastPhase = pricingPhases.last()

                        // Extract numeric values from price strings (assuming format like "₹100.00" or "$100.00")
                        val firstPrice = firstPhase.priceAmountMicros / 1_000_000.0
                        val lastPrice = lastPhase.priceAmountMicros / 1_000_000.0

                        val priceDifference = lastPrice - firstPrice
                        val currencySymbol = lastPhase.formattedPrice.first()

                        val savingsText = if (priceDifference > 0) {
                            "${currencySymbol}${String.format("%.2f", priceDifference)} OFF"
                        } else {
                            ""
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        if (savingsText.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(5.dp)
                                    )
                                    .clip(RoundedCornerShape(5.dp))
                                    .border(
                                        color = MaterialTheme.colorScheme.onSurface.copy(
                                            alpha = 0.5F
                                        ), width = 1.dp, shape = RoundedCornerShape(5.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ){
                                Row (
                                    verticalAlignment = Alignment.CenterVertically
                                ){
                                    Image(
                                        painter = rememberAsyncImagePainter(R.drawable.discount),
                                        contentDescription = "Discount Icon",
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = savingsText,
                                        fontSize = getDimensionText(R.dimen.text_size_plan_intro_price),
                                        fontWeight = FontWeight.W500,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = getDimensionText(R.dimen.text_line_height)
                                    )
                                }

                            }

                        }
                }

            }
            Row(
                modifier = Modifier.fillMaxWidth(),

            ) {
                val formattedPrice = productDetails.subscriptionOfferDetails
                    ?.firstOrNull()
                    ?.pricingPhases
                    ?.pricingPhaseList
                    ?.last()
                    ?.formattedPrice ?: ""

                val billingPeriod = productDetails.subscriptionOfferDetails
                    ?.firstOrNull()
                    ?.pricingPhases
                    ?.pricingPhaseList
                    ?.last()
                    ?.billingPeriod?.let { getTimePeriod(it) } ?: ""

                // Get raw formatted string from strings.xml
                val rawString = stringResource(R.string.plan_auto_renews, formattedPrice, billingPeriod)

                // Build an AnnotatedString for styled text
                val formattedText = buildAnnotatedString {
                    val firstIndex = rawString.indexOf(formattedPrice)
                    val secondIndex = rawString.indexOf(billingPeriod, firstIndex + formattedPrice.length)

                    append(rawString)

                    // Make the first %1$s (formattedPrice) bold
                    if (firstIndex >= 0) {
                        addStyle(
                            style = SpanStyle(fontWeight = FontWeight.Bold, color = if(enableDarkTheme)  Color.White else  Color.Black),
                            start = firstIndex,
                            end = firstIndex + formattedPrice.length
                        )
                    }
                }

                // Display styled text
                Text(
                    text = formattedText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.W400, // Default weight (except for bold part)
                        color = MaterialTheme.colorScheme.onSecondary,
                        fontSize = getDimensionText(R.dimen.box_plan_renew_size),
                        textAlign = TextAlign.Center
                    )
                )

            }
        }
//
//            if (subscriptionsViewModel.selectedPlan == productIndex) {
//                Image(
//                    painter = painterResource(id = R.drawable.radio_button_checked),
//                    contentDescription = stringResource(R.string.selected_plan_indicator),
//                    modifier = Modifier
//                        .size(getDimension(R.dimen.icon_size)),
//                    alignment = Alignment.Center,
//                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
//                )
//            }
//            else{
//                Image(
//                    painter = painterResource(id = R.drawable.radio_button),
//                    contentDescription = stringResource(R.string.unselected_plan_indicator),
//                    modifier = Modifier
//                        .size(getDimension(R.dimen.icon_size)),
//                )
//            }
        }

}
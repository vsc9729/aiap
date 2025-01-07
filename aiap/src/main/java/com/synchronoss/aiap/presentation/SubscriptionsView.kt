import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton

import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.billingclient.api.ProductDetails
import com.synchronoss.aiap.R
import com.synchronoss.aiap.presentation.SubscriptionsViewModel
import com.synchronoss.aiap.utils.AppColors
import com.synchronoss.aiap.utils.Constants.PURCHASE_REQUIRED
import com.synchronoss.aiap.utils.Constants.STORAGE_TAGLINE
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.internal.OpDescriptor
import kotlinx.coroutines.launch

import kotlinx.coroutines.runBlocking

@Composable
fun SubscriptionsView(activity: ComponentActivity, modifier: Modifier = Modifier) {
    val subscriptionsViewModel = hiltViewModel<SubscriptionsViewModel>()


    if(!subscriptionsViewModel.isConnectionStarted){
        runBlocking {
            subscriptionsViewModel.startConnection()
        }
    }
    val filteredProducts: List<ProductDetails>? = subscriptionsViewModel.filteredProducts

    if(subscriptionsViewModel.filteredProducts != null && subscriptionsViewModel.selectedTab != null)
    Box {
        Column(modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .padding(20.dp)
        ) {


                Text(
                    text = STORAGE_TAGLINE,
                    style = LocalTextStyle.current.copy(
                        fontWeight = FontWeight.W700,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onPrimary,
                        textAlign = TextAlign.Center
                    )
                )


                Spacer(modifier = Modifier.height(8.dp))
                TabSelector(
                    selectedTab = subscriptionsViewModel.selectedTab?:TabOption.YEARLY,
                    onTabSelected = { tab ->
                        subscriptionsViewModel.selectedTab = tab
                        subscriptionsViewModel.onTabSelected(tab= tab)

                    },
                    modifier = Modifier.fillMaxWidth()
                )


            TextButton(
                onClick = { /* Handle restore */ },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 16.dp)
            ) {
                Text(
                    text = "Restore purchase",
                    color = MaterialTheme.colorScheme.onTertiary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W400,
                    style = TextStyle(
                        textDecoration = TextDecoration.Underline,

                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            ScrollablePlans(activity = activity)
            Spacer(modifier = modifier.height(4.dp))
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .shadow(
                    elevation = 1.dp,
                    shape = RoundedCornerShape(topEnd = 16.dp, topStart = 16.dp),
                    spotColor = Color.Black.copy(alpha = 0.1f),
                    ambientColor = Color.Black.copy(alpha = 0.1f)

                )
                .fillMaxWidth()
                .height(170.dp)
                .clip(RoundedCornerShape(topEnd = 16.dp, topStart = 16.dp))
                .background(color = MaterialTheme.colorScheme.tertiary)
                .align(Alignment.BottomCenter)

        )
        {
            Box(modifier = Modifier.padding(top = 16.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)) {

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally

            ) {
                Text(
                    text = "Plan auto-renews for ₹1000 every month. You can cancel anytime you want.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.W400,
                        color = MaterialTheme.colorScheme.onSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                )
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(999.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary, // Normal state color
                        contentColor = Color.White, // Normal text/icon color
                        disabledContainerColor = AppColors.lightGray.copy(alpha = 0.6f),
                        disabledContentColor = Color.White.copy(alpha = 0.6f)
                    ),
//                    shape = RoundedCornerShape(10.dp),
                    onClick = {
                        runBlocking {
                            subscriptionsViewModel.purchaseSubscription(
                                activity = activity,
                                product = filteredProducts?.get(subscriptionsViewModel.selectedPlan) ?: return@runBlocking,
                                onError = { error ->
                                    // Handle error
                                    println("Error: $error")
                                }
                            )
                        }
                    },
                    enabled = subscriptionsViewModel.selectedPlan != -1
                ) {
                    Text(
                        "Continue",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W600,
                        color = Color.White
                    )
                }
                Text(
                    text = "Apply Coupon",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W600,
                    color = MaterialTheme.colorScheme.onTertiary
                )
            }
        }

        }
    }
    else Column (
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        //Loader here
        CircularProgressIndicator(
            modifier = Modifier.size(50.dp),
            color = MaterialTheme.colorScheme.primary
        )

    }
}


@Composable
fun ScrollablePlans(
    activity: ComponentActivity

) {
    val subscriptionsViewModel = hiltViewModel<SubscriptionsViewModel>()

    val filteredProducts: List<ProductDetails>? = subscriptionsViewModel.filteredProducts
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // Enables scrolling
            .padding(bottom = 16.dp)

    ) {
        if (subscriptionsViewModel.filteredProducts.isNullOrEmpty()) {
            Text(
                text = "No products available",
                style = LocalTextStyle.current.copy(
                    fontWeight = FontWeight.W700,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center
                )
            )
        } else {
            val currentProductIndex: Int? =
                subscriptionsViewModel.products?.indexOfFirst { it.productId == subscriptionsViewModel.currentProductId }

            val currentProduct =
                if (currentProductIndex != -1) subscriptionsViewModel.products?.get(currentProductIndex!!) else null

            if (currentProduct == null) DemoCurrentPlanCard()
            if(currentProduct != null && (filteredProducts?: mutableListOf()).contains(currentProduct)){
                ActualCurrentPlanCard(
                    product = currentProduct
                )
            }

            // Add multiple cards for testing

            Spacer(modifier = Modifier.height(16.dp)) // Spacing between cards
            filteredProducts?.forEachIndexed { index, product ->
                if (product.productId == subscriptionsViewModel.currentProductId) return@forEachIndexed
                val subscriptionOfferDetails = product.subscriptionOfferDetails?.last()
                val pricingPhases =
                    subscriptionOfferDetails?.pricingPhases?.pricingPhaseList?.last()
                val price = pricingPhases?.formattedPrice
                val description: String =
                    product.description.ifEmpty { "Get 100 GB of storage for photos, files  & backup." }
                OtherPlanCard(
                    price = price ?: "₹500",
                    description = description,
                    product = product,
                    activity = activity,
                    productIndex = index
                )
                Spacer(modifier = Modifier.height(16.dp)) // Spacing between cards

            }
            Spacer(modifier = Modifier.height(135.dp)) // Spacing at the end of the list

        }
    }
}

@Composable
fun ActualCurrentPlanCard(
    product: ProductDetails
) {
    val subscriptionOfferDetails = product.subscriptionOfferDetails?.last()
    val pricingPhases = subscriptionOfferDetails?.pricingPhases?.pricingPhaseList?.last()
    val price = pricingPhases?.formattedPrice
    val description: String = product.description.ifEmpty { "Get 100 GB of storage for photos, files  & backup." }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                border = BorderStroke(0.5.dp, color = MaterialTheme.colorScheme.outline),
                shape = RoundedCornerShape(12.dp)
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
                        RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                    )
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 16.dp),
            ) {
                Text(
                    text = "Current Subscription",
                    fontSize = 14.sp,
                    color =MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.W600,
//                modifier = Modifier.padding(horizontal = 6.dp)
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
                    Text(
                        text = price?:"₹500",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.W700,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    Text(
                        text = description?: "Get 50 GB of storage for photos, files & backup.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.W400,
                        color = MaterialTheme.colorScheme.onSecondary,
                        lineHeight = 16.sp
                    )
                }
                Image(
                    painter = painterResource(id = R.drawable.checkmark_circle),
                    contentDescription = "Selected Plan Indicator",
                    modifier = Modifier
                        .size(24.dp),
                    alignment = Alignment.Center

                )
            }
        }

    }
}
@Composable
fun DemoCurrentPlanCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                border = BorderStroke(0.5.dp, color = MaterialTheme.colorScheme.outline),
                shape = RoundedCornerShape(12.dp)
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
                    RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                )
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp),
        ) {
            Text(
                text = "Current Subscription",
                fontSize = 14.sp,
                color =MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.W600,
//                modifier = Modifier.padding(horizontal = 6.dp)
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
                Text(
                    text = "₹500",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W700,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Text(
                    text = "Get 50 GB of storage for photos, files & backup.",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.W400,
                    color = MaterialTheme.colorScheme.onSecondary,
                    lineHeight = 16.sp
                )
            }
            Image(
                painter = painterResource(id = R.drawable.checkmark_circle),
                contentDescription = "Selected Plan Indicator",
                modifier = Modifier
                    .size(24.dp),
                alignment = Alignment.Center

            )
        }
    }

    }
    }

@Composable
fun OtherPlanCard(price: String, description: String, activity: ComponentActivity, product: ProductDetails, productIndex: Int) {
    val subscriptionsViewModel = hiltViewModel<SubscriptionsViewModel>()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(95.dp)
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(12.dp)
            )
            .then(
                if (subscriptionsViewModel.selectedPlan == productIndex) {
                    Log.d("Co", "Current Plan: ${subscriptionsViewModel.selectedPlan}")
                    Modifier
                        .shadow(
                            elevation = 1.dp,  // Reduced elevation for softer shadow
                            shape = RoundedCornerShape(12.dp),
//                            spotColor = Color.Black.copy(alpha = 0.1f),  // More transparent shadow
//                            ambientColor = Color.Black.copy(alpha = 0.1f)  // More transparent ambient shadow
                        )
                        .background(
                            color = MaterialTheme.colorScheme.background,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            border = BorderStroke(1.5.dp,color = MaterialTheme.colorScheme.outlineVariant),
                            shape = RoundedCornerShape(12.dp)
                        )
                } else {
                    Modifier.border(
                        border = BorderStroke(0.5.dp, color = MaterialTheme.colorScheme.outline),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            )
            .clickable {
                subscriptionsViewModel.selectedPlan = productIndex
                Log.d("Co", "Current Plan: ${subscriptionsViewModel.selectedPlan}")
            }
            .padding(horizontal = 16.dp)
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
                Text(
                    text = price,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W700,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Text(
                    text = description,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.W400,
                    color = MaterialTheme.colorScheme.onSecondary,
                    lineHeight = 16.sp
                )
            }

            // Checkmark Icon
            if (subscriptionsViewModel.selectedPlan == productIndex) {
                Image(
                    painter = painterResource(id = R.drawable.radio_button_checked),
                    contentDescription = "Selected Plan Indicator",
                    modifier = Modifier
                        .size(24.dp),
                    alignment = Alignment.Center,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)

                )
            }
            else{
                Image(
                    painter = painterResource(id = R.drawable.radio_button),
                    contentDescription = "Unselected Plan Indicator",
                    modifier = Modifier
                        .size(24.dp),
                )
            }
        }
    }
}

enum class TabOption {
    YEARLY,
    MONTHLY,
    WEEKlY,
}

@Composable
fun TabSelector(
    selectedTab: TabOption,
    onTabSelected: (TabOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color = MaterialTheme.colorScheme.secondary),
        color = MaterialTheme.colorScheme.secondary
    ) {
        Row(
            modifier = Modifier.padding(6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TabOption.entries.forEach { tab ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(6.dp))
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
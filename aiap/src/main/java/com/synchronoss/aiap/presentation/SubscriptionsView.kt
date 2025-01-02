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
    var selectedTab by remember { mutableStateOf(TabOption.MONTHLY) }


    runBlocking {
        subscriptionsViewModel.startConnection(
            productIds = listOf(
                "yearly_subscription",
            )
        )
    }
    val products: List<ProductDetails>? = subscriptionsViewModel.products

    Box {
        Column(modifier = Modifier.fillMaxSize()
            .background(color = Color.White)
            .padding(20.dp)
        ) {

                Text(
                    text = STORAGE_TAGLINE,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.W700,
                        color = AppColors.black,
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))
                TabSelector(
                    selectedTab = selectedTab,
                    onTabSelected = { tab ->
                        selectedTab = tab
                        // Handle tab selection

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
                    color = Color.Gray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W400,
                    style = TextStyle(
                        textDecoration = TextDecoration.Underline
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
                    shape =RoundedCornerShape(topEnd = 16.dp, topStart = 16.dp),
                    spotColor = Color.Black.copy(alpha = 0.1f),
                    ambientColor = Color.Black.copy(alpha = 0.1f)

                    )
                .fillMaxWidth()
                .height(170.dp)
                .clip(RoundedCornerShape(topEnd = 16.dp, topStart = 16.dp))
                .background(Color.White)
                .align(Alignment.BottomCenter)
                .border(border = BorderStroke(0.5.dp, Color(0xFFE5E7EB)), shape = RoundedCornerShape(topEnd = 16.dp, topStart = 16.dp))
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
                        color = AppColors.textGray,
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
                        containerColor = AppColors.primaryBlue, // Normal state color
                        contentColor = Color.White, // Normal text/icon color
                        disabledContainerColor = AppColors.lightGray.copy(alpha = 0.6f),
                        disabledContentColor = Color.White.copy(alpha = 0.6f)
                    ),
//                    shape = RoundedCornerShape(10.dp),
                    onClick = {
                        runBlocking {
                            subscriptionsViewModel.purchaseSubscription(
                                activity = activity,
                                product = products?.get(0) ?: return@runBlocking,
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
                    color = AppColors.textGray,
                )
            }
        }

        }
    }
    }


@Composable
fun ScrollablePlans(
    activity: ComponentActivity

) {
    val subscriptionsViewModel = hiltViewModel<SubscriptionsViewModel>()

    val products: List<ProductDetails>? = subscriptionsViewModel.products
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // Enables scrolling
            .padding(bottom = 16.dp)

    ) {
         // Add multiple cards for testing
        CurrentPlanCard()
        Spacer(modifier = Modifier.height(16.dp)) // Spacing between cards
        products?.forEachIndexed { index, product ->
            val subscriptionOfferDetails = product.subscriptionOfferDetails?.last()
            val pricingPhases = subscriptionOfferDetails?.pricingPhases?.pricingPhaseList?.last()
            val price = pricingPhases?.formattedPrice
            val description: String = product.description.ifEmpty { "Get 100 GB of storage for photos, files  & backup." }
            OtherPlanCard(
                price = price ?: "₹500",
                description = description,
                product = product,
                activity = activity,
                productIndex= index
            )
            Spacer(modifier = Modifier.height(16.dp)) // Spacing between cards

        }
        Spacer(modifier = Modifier.height(135.dp)) // Spacing at the end of the list

    }
}

@Composable
fun CurrentPlanCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(
                color = Color.White,
                shape = RoundedCornerShape(12.dp)
            )
            .border(border = BorderStroke(0.5.dp, Color(0xFFE5E7EB)), shape = RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ){
        Row(
            modifier = Modifier
                .background(
                    color = Color(0xFFDFF6DD),
                    RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                )
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp),
        ) {
            Text(
                text = "Current Subscription",
                fontSize = 14.sp,
                color = Color(0xFF2E7D32),
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
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Text(
                    text = "Get 50 GB of storage for photos, files & backup.",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.W400,
                    color = AppColors.lightGray,
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
                color = Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .then(
                if(subscriptionsViewModel.selectedPlan == productIndex) {
                    Log.d("Co", "Current Plan: ${subscriptionsViewModel.selectedPlan}")
                    Modifier
                        .shadow(
                            elevation = 1.dp,  // Reduced elevation for softer shadow
                            shape = RoundedCornerShape(12.dp),
//                            spotColor = Color.Black.copy(alpha = 0.1f),  // More transparent shadow
//                            ambientColor = Color.Black.copy(alpha = 0.1f)  // More transparent ambient shadow
                        )
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(border = BorderStroke(0.5.dp, Color(0xFF0096D5)), shape = RoundedCornerShape(12.dp))
                } else{
                    Modifier.border(border = BorderStroke(0.5.dp, Color(0xFFE5E7EB)), shape = RoundedCornerShape(12.dp))
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
                    color = AppColors.black,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Text(
                    text = description,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.W400,
                    color = AppColors.lightGray,
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
                    alignment = Alignment.Center

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
    MONTHLY,
    YEARLY
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
            .background(Color(0xFFE7F8FF)),
        color = Color(0xFFE7F8FF)
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
                            if (tab == selectedTab) AppColors.primaryBlue
                            else Color.Transparent
                        )
                        .clickable { onTabSelected(tab) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab.name.lowercase().replaceFirstChar { it.uppercase() },
                        color = if (tab == selectedTab) Color.White else AppColors.primaryBlue,
                        fontWeight = FontWeight.W600
                    )
                }
            }
        }
    }
}
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.billingclient.api.ProductDetails
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



    runBlocking {
        subscriptionsViewModel.startConnection(
            productIds = listOf(
                "yearly_subscription",
            )
        )
    }
    val products: List<ProductDetails>? = subscriptionsViewModel.products

    Column(modifier = Modifier.padding(30.dp)) {
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
        ScrollablePlans(activity = activity)
//        products?.forEach { product ->
//            Text(
//                product.name,
//                style = MaterialTheme.typography.bodyLarge,
//                modifier = Modifier
//                    .padding(4.dp)
//                    .clickable {
//
//                        runBlocking {
//                            subscriptionsViewModel.purchaseSubscription(
//                                activity = activity,
//                                product = product,
//                                onError = { error ->
//                                    // Handle error
//                                    println("Error: $error")
//                                }
//                            )
//                        }
//
//
//                        //TODO: Launch Billing Flow Implementation
//                    })
            Spacer(modifier = modifier.height(4.dp))
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

    ) {
         // Add multiple cards for testing
        CurrentPlanCard()
        Spacer(modifier = Modifier.height(16.dp)) // Spacing between cards
        products?.forEach { product ->
            val subscriptionOfferDetails = product.subscriptionOfferDetails?.last()
            val pricingPhases = subscriptionOfferDetails?.pricingPhases?.pricingPhaseList?.last()
            val price = pricingPhases?.formattedPrice
            val description: String = product.description.ifEmpty { "Get 100 GB of storage for photos, files  & backup." }
            OtherPlanCard(

                price = price ?: "₹500",
                description = description,
                product = product,
                activity = activity
            )
            Spacer(modifier = Modifier.height(16.dp)) // Spacing between cards
//            Text(
//                price.toString().split(" ")[0],
//                style = MaterialTheme.typography.bodyLarge,
//                modifier = Modifier
//                    .padding(4.dp)
//                    .clickable {
//
//                        runBlocking {
//                            subscriptionsViewModel.purchaseSubscription(
//                                activity = activity,
//                                product = product,
//                                onError = { error ->
//                                    // Handle error
//                                    println("Error: $error")
//                                }
//                            )
//                        }
//
//
//                        //TODO: Launch Billing Flow Implementation
//                    })
        }
//        repeat(3){
//
//            OtherPlanCard()
//            Spacer(modifier = Modifier.height(16.dp)) // Spacing between cards
//        }
    }
}

@Composable
fun CurrentPlanCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf( Color(0xFFA954D4),Color(0xFF3AD8EC))
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(start = 16.dp, end = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {

            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .fillMaxHeight(),
                horizontalArrangement = Arrangement.SpaceBetween
            ){

                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = "₹500",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.W700,
                        color = Color.White,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
                Column(
                    modifier = Modifier
                        .background(Color(0xFFDFF6DD), RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                        .padding(horizontal = 6.dp, vertical = 0.dp),
                    verticalArrangement = Arrangement.Top
                ) {

                    Box(
                        modifier = Modifier
                            .background(Color(0xFFDFF6DD), RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                            .padding(horizontal = 6.dp, vertical = 0.dp),
                    ) {
                        Text(
                            text = "Current Plan",
                            fontSize = 10.sp,
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Price Text


                // Description
                Text(
                    text = "Get 50 GB of storage for photos, files & backup.",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.W400,
                    color = Color(0xffF9FAFB),
                    lineHeight = 16.sp,
                    modifier = Modifier.weight(1f)
                )

                // Current Plan Indicator

            }
        }

    }
}

@Composable
fun OtherPlanCard(price: String,  description: String,activity: ComponentActivity, product: ProductDetails) {
    val subscriptionsViewModel = hiltViewModel<SubscriptionsViewModel>()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(
                color = Color.White,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(start = 16.dp, end = 16.dp)                    .clickable {

                        runBlocking {
                            subscriptionsViewModel.purchaseSubscription(
                                activity = activity,
                                product = product,
                                onError = { error ->
                                    // Handle error
                                    println("Error: $error")
                                }
                            )
                        }
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {

            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .fillMaxHeight(),
                horizontalArrangement = Arrangement.SpaceBetween
            ){

                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = price,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.W700,
                        color = AppColors.black,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }

            }
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Price Text


                // Description
                Text(
                    text = description,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.W400,
                    color = AppColors.lightGray,
                    lineHeight = 16.sp,
                    modifier = Modifier.weight(1f)
                )

                // Current Plan Indicator

            }
        }

    }
}


@Preview(showBackground = true)
@Composable
fun PreviewSubscriptionsView() {
    SubscriptionsView(activity = ComponentActivity())
}
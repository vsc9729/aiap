import android.content.res.Configuration
import android.graphics.Paint.Style
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.synchronoss.aiap.R
import com.synchronoss.aiap.domain.models.ProductInfo
import com.synchronoss.aiap.presentation.SubscriptionsViewModel
import com.synchronoss.aiap.presentation.ToastComposable
import com.synchronoss.aiap.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Main composable for displaying subscription plans and handling user interactions.
 * Provides a complete subscription interface with plan selection, purchase flow, and coupon handling.
 */
@Composable
fun SubscriptionsView(activity: ComponentActivity, modifier: Modifier = Modifier) {
    val subscriptionsViewModel = hiltViewModel<SubscriptionsViewModel>()
    var showDialog by remember { mutableStateOf(false) }
    val logoUrl = subscriptionsViewModel.finalLogoUrl;
    val configuration = LocalConfiguration.current;

    val logoUrlWidth = 110.dp
    val logoUrlHeight = 55.dp

    Log.d(null, "Logo url is $logoUrl")

    val filteredProducts: List<ProductInfo>? = subscriptionsViewModel.filteredProducts

    if (!subscriptionsViewModel.isLoading.value)
    Box {
        Column(modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
        ) {
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .background(color = MaterialTheme.colorScheme.background)
//                    .padding(vertical = 16.dp),
//                verticalArrangement = Arrangement.Center,
//            ) {
//                ToastComposable(
//                    heading = "Purchase Cancelled",
//                    subText = "User has cancelled the purchase",
//                    onDismiss = {
//                        println("Toast dismissed!")
//                    }
//                )
//            }

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(logoUrl),
                        contentDescription = "Image from URL",
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
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.onPrimary,
                            textAlign = TextAlign.Center
                        )
                    )
                }


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
                    .let {
                    if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        it.padding(4.dp)
                    } else {
                        it.padding(12.dp)
                    }
                }
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

            ScrollablePlans()
            Spacer(modifier = modifier.height(8.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .let {
                    if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        it.height(80.dp)
                    } else {
                        it.height(125.dp)
                    }
                }
                .drawBehind {
                    val shadowColor = Color.Black.copy(alpha = 0.068f)
                    val shadowRadius = 35.dp.toPx()
                    val offsetY = -shadowRadius / 2
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf( Color.Transparent, shadowColor),
                            startY = offsetY,
                            endY = offsetY + shadowRadius,
                            tileMode = TileMode.Clamp
                        ),
                        topLeft = Offset(0f, offsetY),
                        size = Size(size.width, shadowRadius)
                    )
                }
                .clip(RoundedCornerShape(topEnd = 16.dp, topStart = 16.dp))
                .background(color = MaterialTheme.colorScheme.tertiary)

                .align(Alignment.BottomCenter)
        )
        {
            when(configuration.orientation) {
                Configuration.ORIENTATION_PORTRAIT -> {
                    Box(
                        modifier = Modifier
                            .padding(top = 16.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
                    ) {

                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween,
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
//                            )
                            Button(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .clip(RoundedCornerShape(999.dp)),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary, // Normal state color
                                    contentColor = Color.White, // Normal text/icon color
                                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(
                                        alpha = 0.4f
                                    ),
                                    disabledContentColor = Color.White.copy(alpha = 0.4f)
                                ),
                                onClick = {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        subscriptionsViewModel.purchaseSubscription(
                                            activity = activity,
                                            product = filteredProducts?.get(subscriptionsViewModel.selectedPlan)
                                                ?: return@launch,
                                            onError = { error ->
                                                // Handle error
                                                println("Error: $error")
                                            }
                                        )
                                    }
                                },
                                enabled = subscriptionsViewModel.selectedPlan != -1 && !subscriptionsViewModel.isCurrentProductBeingUpdated
                            ) {
                                Row {
                                    Text(
                                        "Continue",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.W600,
                                        color = Color.White
                                    )
                                    Spacer(
                                        modifier = Modifier.width(8.dp)
                                    )
                                    if (subscriptionsViewModel.isCurrentProductBeingUpdated) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            strokeWidth = 2.dp,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                            TextButton(
                                onClick = {
                                    showDialog = true
                                }
                            ) {
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
                Configuration.ORIENTATION_LANDSCAPE -> {
                    Box(
                        modifier = Modifier
                            .padding(top = 16.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
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
//                            )
                            Spacer(
                                modifier = Modifier.height(4.dp)
                            )
                            Row(
                                modifier=Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween

                            ) {
                            Button(
                                modifier = Modifier
                                    //.fillMaxWidth()
                                    .weight(1f)
                                    .height(50.dp)
                                    .clip(RoundedCornerShape(999.dp)),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary, // Normal state color
                                    contentColor = Color.White, // Normal text/icon color
                                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(
                                        alpha = 0.4f
                                    ),
                                    disabledContentColor = Color.White.copy(alpha = 0.4f)
                                ),
                                onClick = {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        subscriptionsViewModel.purchaseSubscription(
                                            activity = activity,
                                            product = filteredProducts?.get(subscriptionsViewModel.selectedPlan)
                                                ?: return@launch,
                                            onError = { error ->
                                                // Handle error
                                                println("Error: $error")
                                            }
                                        )
                                    }
                                },
                                enabled = subscriptionsViewModel.selectedPlan != -1 && !subscriptionsViewModel.isCurrentProductBeingUpdated
                            ) {
                                Row {
                                    Text(
                                        "Continue",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.W600,
                                        color = Color.White
                                    )
                                    Spacer(
                                        modifier = Modifier.width(8.dp)
                                    )
                                    if (subscriptionsViewModel.isCurrentProductBeingUpdated) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            strokeWidth = 2.dp,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                            TextButton(
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    showDialog = true
                                }
                            ) {
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
    if(showDialog){
        DialogBox(
            onDismiss = {showDialog = false},
            onConfirm = { input -> Log.d("Co", "Input: $input")}
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
    val filteredProducts = subscriptionsViewModel.filteredProducts
    val currentProductId = subscriptionsViewModel.currentProductId
    val currentProduct = subscriptionsViewModel.products?.find { it.productId == currentProductId }

//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//
//            .padding(bottom = 16.dp)
//    ) {
        if (filteredProducts.isNullOrEmpty()) {
            Text(
                text = "No products available",
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
        when {
            filteredProducts.contains(currentProduct) -> ActualCurrentPlanCard(product = currentProduct!!)
        }



    // Display other plans
        Spacer(modifier = Modifier.height(16.dp))
        filteredProducts.forEachIndexed { index, product ->
            if (product.productId != currentProductId) {
                OtherPlanCard(product = product, productIndex = index)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(135.dp))
//    }
}

/**
 * Displays the current active subscription plan card.
 * @param product The currently active subscription product
 */
@Composable
fun ActualCurrentPlanCard(
    product: ProductInfo
) {
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
                        text = product.displayPrice,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.W700,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    Text(
                        text = product.description,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.W400,
                        color = MaterialTheme.colorScheme.onSecondary,
                        lineHeight = 16.sp
                    )
                }
                Image(
                    painter = painterResource(id = R.drawable.checkmark_circle),
                    contentDescription = Constants.SELECTED_PLAN_INDICATOR,
                    modifier = Modifier
                        .size(24.dp),
                    alignment = Alignment.Center

                )
            }
        }

    }
}
/**
 * Displays available subscription plan cards for selection.
 * @param product The subscription product to display
 * @param productIndex Index for tracking selected plan
 */
@Composable
fun OtherPlanCard( product: ProductInfo, productIndex: Int) {
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
                            border = BorderStroke(
                                1.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant
                            ),
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
                    text = product.displayPrice,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W700,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Text(
                    text = product.description,
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
                    contentDescription = Constants.SELECTED_PLAN_INDICATOR,
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
    MONTHLY,
    WEEKlY,
    YEARLY,
}

@Composable
fun TabSelector(
    selectedTab: TabOption,
    onTabSelected: (TabOption) -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current;
    Surface(
        modifier = modifier
            .let {
                if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    it.height(40.dp)
                } else {
                    it.height(50.dp)
                }
            }
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

/**
 * Dialog for entering and applying coupon codes.
 * @param onDismiss Callback when dialog is dismissed
 * @param onConfirm Callback when coupon is confirmed
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogBox(
    onDismiss : ()->Unit,
    onConfirm : (String)->Unit
) {
    var text by remember { mutableStateOf("") }
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .height(300.dp)
                .width(400.dp)
                .padding(24.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White,
            ),

        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),

            ) {
                Text(
                    text = "Enter Your Coupon Code",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black,
                    fontWeight = FontWeight.W400
                )

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(
                        text = "Coupon Code",
                        color = Color.Gray
                    ) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                ),
                    textStyle = TextStyle(color = Color.Black)
                )
                Spacer(modifier = Modifier.height(4.dp))
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
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onConfirm(text);
                            onDismiss();
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        )
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

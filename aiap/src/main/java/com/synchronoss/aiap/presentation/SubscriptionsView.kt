package com.synchronoss.aiap.presentation

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Paint.Style
import android.net.Uri
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import com.synchronoss.aiap.core.domain.models.ProductInfo
import com.synchronoss.aiap.presentation.SubscriptionsViewModel
import com.synchronoss.aiap.presentation.ToastComposable
import com.synchronoss.aiap.utils.Constants
import com.synchronoss.aiap.utils.getDimension
import com.synchronoss.aiap.utils.getDimensionText
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
    val logoUrlWidth = getDimension(R.dimen.logo_width)
    val logoUrlHeight = getDimension(R.dimen.logo_height)
    Log.d(null, "Logo url is $logoUrl")
    val filteredProducts: List<ProductInfo>? = subscriptionsViewModel.filteredProducts
    if(subscriptionsViewModel.noInternetConnectionAndNoCache.value){
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = if(isSystemInDarkTheme()) Color(0xFF0D0D0D) else Color.White )
                .padding(vertical = getDimension(R.dimen.no_data_box)),
            contentAlignment = Alignment.Center
        ){
            Text(text = stringResource(R.string.no_connection_text))
        }
    }else{
        if (!subscriptionsViewModel.isLoading.value)
            Box {
                Column(modifier = Modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = getDimension(R.dimen.spacing_xlarge))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(logoUrl),
                            contentDescription = stringResource(R.string.image_url_description),
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
                                fontSize = getDimensionText(R.dimen.size_text_storage_tagline_medium),
                                color = MaterialTheme.colorScheme.onPrimary,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(getDimension(R.dimen.spacing_small)))
                    (subscriptionsViewModel.selectedTab?: subscriptionsViewModel.products?.let {
                        TabOption.getAvailableTabs(
                            it
                        ).first()
                    })?.let {
                        TabSelector(
                            selectedTab = it,
                            onTabSelected = { tab ->
                                subscriptionsViewModel.selectedTab = tab
                                subscriptionsViewModel.onTabSelected(tab= tab)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    TextButton(
                        onClick = { /* Handle restore */ },
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .let {
                                if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                                    it.padding(getDimension(R.dimen.spacing_xsmall))
                                } else {
                                    it.padding(getDimension(R.dimen.spacing_medium))
                                }
                            }
                    ) {
                        Text(
                            text = stringResource(R.string.restore_purchase),
                            color = MaterialTheme.colorScheme.onTertiary,
                            fontSize = getDimensionText(R.dimen.size_text_storage_tagline_small),
                            fontWeight = FontWeight.W400,
                            style = TextStyle(
                                textDecoration = TextDecoration.Underline,
                            )
                        )
                    }
                    ScrollablePlans()
                    Spacer(modifier = modifier.height(getDimension(R.dimen.spacing_small)))
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .let {
                            if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                                it.height(if(subscriptionsViewModel.selectedPlan != -1)
                                    getDimension(R.dimen.box_landscape_selected_height) else getDimension(R.dimen.box_landscape_not_selected_height))
                            } else {
                                it.height(if(subscriptionsViewModel.selectedPlan != -1)getDimension(R.dimen.box_non_landscape_selected_height) else getDimension(R.dimen.box_non_landscape_not_selected_height))
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
                        .clip(RoundedCornerShape(topEnd = getDimension(R.dimen.box_top_corner), topStart =  getDimension(R.dimen.box_top_corner)))
                        .background(color = MaterialTheme.colorScheme.tertiary)
                        .align(Alignment.BottomCenter)
                )
                {
                    when(configuration.orientation) {
                        Configuration.ORIENTATION_PORTRAIT -> {
                            Box(
                                modifier = Modifier
                                    .padding(top = getDimension(R.dimen.box_portrait_padding), bottom = getDimension(R.dimen.box_portrait_padding), start =getDimension(R.dimen.box_portrait_padding), end =getDimension(R.dimen.box_portrait_padding))
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceBetween,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    if(subscriptionsViewModel.selectedPlan != -1){
                                        val selectedProduct = filteredProducts?.get(subscriptionsViewModel.selectedPlan)

                                        Text(
                                            text = stringResource(
                                                R.string.plan_auto_renews,
                                                selectedProduct?.displayPrice ?: ""
                                            ),
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.W400,
                                                color = MaterialTheme.colorScheme.onSecondary,
                                                fontSize = getDimensionText(R.dimen.box_plan_renew_size),
                                                textAlign = TextAlign.Center
                                            )
                                        )
                                    }
                                    Button(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(getDimension(R.dimen.continue_btn_height))
                                            .clip(RoundedCornerShape(getDimension(R.dimen.continue_btn_shape))),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary, // Normal state color
                                            contentColor = Color.White, // Normal text/icon color
                                            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(
                                                alpha = 0.4f
                                            ),
                                            disabledContentColor = Color.White.copy(alpha = 0.4f)
                                        ),
                                        onClick = {
                                            filteredProducts?.get(subscriptionsViewModel.selectedPlan)
                                                ?.let {
                                                    subscriptionsViewModel.purchaseSubscription(
                                                        activity = activity,
                                                        product = it,
                                                        onError = { error ->
                                                            // Handle error
                                                            Log.d("Co", "Error: $error")
                                                        }
                                                    )
                                                }
                                        },
                                        enabled = subscriptionsViewModel.selectedPlan != -1 && !subscriptionsViewModel.isCurrentProductBeingUpdated
                                    ) {
                                        Row {
                                            Text(
                                                text = stringResource(R.string.continue_text),
                                                fontSize = getDimensionText(R.dimen.continue_btn_font_size),
                                                fontWeight = FontWeight.W600,
                                                color = Color.White
                                            )
                                            Spacer(
                                                modifier = Modifier.width(getDimension(R.dimen.continue_btn_spacer))
                                            )
                                            if (subscriptionsViewModel.isCurrentProductBeingUpdated) {
                                                CircularProgressIndicator(
                                                    color = Color.White,
                                                    strokeWidth = getDimension(R.dimen.circular_progress_indicator_width),
                                                    modifier = Modifier.size(getDimension(R.dimen.circular_progress_indicator_modifier_size))
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
                                            text = stringResource(R.string.apply_coupon),
                                            fontSize = getDimensionText(R.dimen.apply_btn_font_size),
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
                                    .padding(top = getDimension(R.dimen.box_landscape_padding), bottom = getDimension(R.dimen.box_landscape_padding), start = getDimension(R.dimen.box_landscape_padding), end = getDimension(R.dimen.box_landscape_padding))
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
                                        modifier = Modifier.height(getDimension(R.dimen.box_landscape_spacer_height))
                                    )
                                    Row(
                                        modifier=Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween

                                    ) {
                                        Button(
                                            modifier = Modifier
                                                //.fillMaxWidth()
                                                .weight(1f)
                                                .height(getDimension(R.dimen.landscape_continue_btn_height))
                                                .clip(RoundedCornerShape(getDimension(R.dimen.landscape_continue_btn_shape))),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary, // Normal state color
                                                contentColor = Color.White, // Normal text/icon color
                                                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(
                                                    alpha = 0.4f
                                                ),
                                                disabledContentColor = Color.White.copy(alpha = 0.4f)
                                            ),
                                            onClick = {
                                                filteredProducts?.get(subscriptionsViewModel.selectedPlan)
                                                    ?.let {
                                                        subscriptionsViewModel.purchaseSubscription(
                                                            activity = activity,
                                                            product = it,
                                                            onError = { error ->
                                                                // Handle error
                                                                Log.d("Co", "Error: $error")
                                                            }
                                                        )
                                                    }

                                            },
                                            enabled = subscriptionsViewModel.selectedPlan != -1 && !subscriptionsViewModel.isCurrentProductBeingUpdated
                                        ) {
                                            Row {
                                                Text(
                                                    stringResource(R.string.continue_text),
                                                    fontSize = getDimensionText(R.dimen.landscape_continue_btn_font_size),
                                                    fontWeight = FontWeight.W600,
                                                    color = Color.White
                                                )
                                                Spacer(
                                                    modifier = Modifier.width(getDimension(R.dimen.landscape_continue_btn_spacer))
                                                )
                                                if (subscriptionsViewModel.isCurrentProductBeingUpdated) {
                                                    CircularProgressIndicator(
                                                        color = Color.White,
                                                        strokeWidth = getDimension(R.dimen.circular_progress_indicator_width),
                                                        modifier = Modifier.size(getDimension(R.dimen.circular_progress_indicator_modifier_size))
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
                                                text = stringResource(R.string.apply_coupon),
                                                fontSize = getDimensionText(R.dimen.apply_btn_font_size),
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

    //Display current plan
    when {
        filteredProducts.contains(currentProduct) -> currentProduct?.let { ActualCurrentPlanCard(product = it) }
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
                // Content Column
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = product.displayPrice,
                        fontSize = getDimensionText(R.dimen.text_size_plan_price),
                        fontWeight = FontWeight.W700,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = getDimension(R.dimen.card_content_spacing))
                    )

                    Text(
                        text = product.description,
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
            .height(getDimension(R.dimen.other_plan_card_height))
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(getDimension(R.dimen.card_corner_radius))
            )
            .then(
                if (subscriptionsViewModel.selectedPlan == productIndex) {
                    Log.d("Co", "Current Plan: ${subscriptionsViewModel.selectedPlan}")
                    Modifier
                        .shadow(
                            elevation = 1.dp,  // Reduced elevation for softer shadow
                            shape = RoundedCornerShape(getDimension(R.dimen.card_corner_radius)),
//                            spotColor = Color.Black.copy(alpha = 0.1f),  // More transparent shadow
//                            ambientColor = Color.Black.copy(alpha = 0.1f)  // More transparent ambient shadow
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
                Log.d("Co", "Current Plan: ${subscriptionsViewModel.selectedPlan}")
            }
            .padding(horizontal = getDimension(R.dimen.card_padding_horizontal))
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
                    fontSize = getDimensionText(R.dimen.text_size_plan_price),
                    fontWeight = FontWeight.W700,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = getDimension(R.dimen.card_content_spacing))
                )

                Text(
                    text = product.description,
                    fontSize = getDimensionText(R.dimen.text_size_plan_description),
                    fontWeight = FontWeight.W400,
                    color = MaterialTheme.colorScheme.onSecondary,
                    lineHeight = getDimensionText(R.dimen.text_line_height)
                )
            }

            // Checkmark Icon
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

enum class TabOption {
    MONTHLY,
    WEEKlY,
    YEARLY;

    companion object {
        fun getAvailableTabs(products: List<ProductInfo>): List<TabOption> {
            val tabsInOrder = mutableListOf<TabOption>()
            val seenPeriods = mutableSetOf<Char>()

            // Iterate through products in order
            for (product in products) {
                val periodCode = product.recurringPeriodCode.last()
                if (!seenPeriods.contains(periodCode)) {
                    when (periodCode) {
                        'M' -> tabsInOrder.add(MONTHLY)
                        'Y' -> tabsInOrder.add(YEARLY)
                        'W' -> tabsInOrder.add(WEEKlY)
                    }
                    seenPeriods.add(periodCode)
                }
            }

            return tabsInOrder
        }
    }
}

@Composable
fun TabSelector(
    selectedTab: TabOption,
    onTabSelected: (TabOption) -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val subscriptionsViewModel = hiltViewModel<SubscriptionsViewModel>()
    val availableTabs = subscriptionsViewModel.products?.let { TabOption.getAvailableTabs(it) } ?: emptyList()

    if (availableTabs.isNotEmpty()) {
        Surface(
            modifier = modifier
                .let {
                    if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        it.height(getDimension(R.dimen.tab_height_landscape))
                    } else {
                        it.height(getDimension(R.dimen.tab_height_portrait))
                    }
                }
                .clip(RoundedCornerShape(getDimension(R.dimen.tab_selector_corner_radius)))
                .background(color = MaterialTheme.colorScheme.secondary),
            color = MaterialTheme.colorScheme.secondary
        ) {
            Row(
                modifier = Modifier.padding(getDimension(R.dimen.tab_selector_padding)),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                availableTabs.forEach { tab ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(getDimension(R.dimen.tab_item_corner_radius)))
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
}

/**
 * Dialog for entering and applying coupon codes.
 * @param onDismiss Callback when dialog is dismissed
 * @param onConfirm Callback when coupon is confirmed
 */
@Composable
fun DialogBox(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .height(getDimension(R.dimen.dialog_height))
                .width(getDimension(R.dimen.dialog_width))
                .padding(getDimension(R.dimen.dialog_padding))
                .clickable(enabled = false) {},  // Prevent click propagation
            shape = RoundedCornerShape(getDimension(R.dimen.dialog_corner_radius)),
            colors = CardDefaults.cardColors(
                containerColor = Color.White,
            ),
        ) {
            Column(
                modifier = Modifier.padding(getDimension(R.dimen.dialog_content_padding)),
                verticalArrangement = Arrangement.spacedBy(getDimension(R.dimen.dialog_spacing)),
            ) {
                Text(
                    text = stringResource(R.string.dialog_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black,
                    fontWeight = FontWeight.W400
                )

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(
                        text = stringResource(R.string.dialog_coupon_label),
                        color = Color.Gray
                    ) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                    ),
                    textStyle = TextStyle(color = Color.Black)
                )

                Spacer(modifier = Modifier.height(getDimension(R.dimen.dialog_spacer_height)))

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
                        Text(stringResource(R.string.dialog_cancel))
                    }

                    Spacer(modifier = Modifier.width(getDimension(R.dimen.dialog_spacing)))

                    Button(
                        onClick = {
                            onConfirm(text)
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/redeem?code=$text"))
                            context.startActivity(intent)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        )
                    ) {
                        Text(stringResource(R.string.dialog_ok))
                    }
                }
            }
        }
    }
}
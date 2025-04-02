package com.synchronoss.aiap.presentation

import android.app.Activity
import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
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
import com.synchronoss.aiap.presentation.subscriptions.ui.MoreBottomSheet
import com.synchronoss.aiap.presentation.subscriptions.ui.ScrollablePlans
import com.synchronoss.aiap.presentation.subscriptions.ui.TabOption
import com.synchronoss.aiap.presentation.subscriptions.ui.TabSelector
import com.synchronoss.aiap.presentation.viewmodels.SubscriptionsViewModel
import com.synchronoss.aiap.presentation.wrappers.SubscriptionsViewWrapper
import com.synchronoss.aiap.utils.Constants
import com.synchronoss.aiap.utils.getDimension
import com.synchronoss.aiap.utils.getDimensionText
import com.synchronoss.aiap.utils.LogUtils

// Tag for logging purposes
private const val TAG = "SubscriptionsView"

/**
 * Composable function that displays the subscriptions view.
 *
 * @param activity The activity context.
 * @param modifier The modifier to be applied to the composable.
 * @param launchedViaIntent Whether the view was launched via an intent.
 * @param enableDarkTheme Whether to use dark theme or not.
 */
@Composable
fun SubscriptionsView(
    activity: ComponentActivity, 
    modifier: Modifier = Modifier, 
    launchedViaIntent: Boolean,
    enableDarkTheme: Boolean = isSystemInDarkTheme()
) {
    // Initialize the ViewModel using the custom remember function
    val viewModel = rememberSubscriptionsViewModel(activity)
    // State for controlling the visibility of the "More" dialog
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    Column {
        // Header with close/back button
        HeaderSection(
            launchedViaIntent = launchedViaIntent,
            onCloseClick = { 
                if(launchedViaIntent) {
                    // If launched via intent, finish the activity when closed
                    (context as? Activity)?.finish() 
                } else {
                    // Otherwise, update dialog state in the ViewModel
                    viewModel.dialogState.value = false
                }
            }
        )
        
        // Show appropriate view based on connection and loading state
        if(viewModel.noInternetConnectionAndNoCache.value) {
            // Show no connection view when there's no internet and no cached data
            NoConnectionView()
        } else {
            if (!viewModel.isLoading.value) {
                // Show main content when data is loaded
                MainContent(
                    viewModel = viewModel,
                    activity = activity,
                    onShowMoreDialog = { showDialog = true },
                    modifier = modifier,
                    enableDarkTheme = enableDarkTheme
                )
            } else {
                // Show loading view while data is being fetched
                LoadingView(enableDarkTheme = enableDarkTheme)
            }
        }
    }
    
    // Show the "More" bottom sheet dialog when requested
    if(showDialog) {
        MoreBottomSheet(
            onDismiss = { showDialog = false },
            onApplyCoupon = { LogUtils.d(TAG, "Apply coupon") },
            onGoToSubscriptions = { LogUtils.d(TAG, "Go to subscriptions") },
            activity = activity
        )
    }
}

/**
 * Creates and remembers an instance of SubscriptionsViewModel.
 * Uses dependency injection to provide the ViewModel with required dependencies.
 *
 * @param activity The parent ComponentActivity
 * @return An instance of SubscriptionsViewModel
 */
@Composable
private fun rememberSubscriptionsViewModel(activity: ComponentActivity): SubscriptionsViewModel {
    // Create and remember the wrapper that will provide the ViewModel
    val wrapper = remember {
        val wrapper = SubscriptionsViewWrapper()
        val application = activity.application
        // Use Dagger to inject dependencies into the wrapper
        val aiapComponent = DaggerAiapComponent.factory().create(application)
        aiapComponent.inject(wrapper)
        wrapper
    }

    // Get and remember the ViewModel from the wrapper
    return remember {
        wrapper.getViewModel(activity)
    }
}

/**
 * Displays the header section with a close or back button.
 *
 * @param launchedViaIntent Determines whether to show a back arrow (if launched via intent) or close icon
 * @param onCloseClick Callback for when the close/back button is clicked
 */
@Composable
private fun HeaderSection(
    launchedViaIntent: Boolean,
    onCloseClick: () -> Unit
) {
    val context = LocalContext.current
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        // Align button to left if launched via intent, otherwise to right
        horizontalArrangement = if(launchedViaIntent) Arrangement.Start else Arrangement.End
    ) {
        IconButton(onClick = onCloseClick) {
            Icon(
                // Show back arrow if launched via intent, otherwise show close icon
                imageVector = if(launchedViaIntent) Icons.Default.ArrowBack else Icons.Default.Close,
                contentDescription = "Close",
                tint = Color(context.getColor(R.color.light_primary_default)),
                modifier = Modifier.size(getDimension(R.dimen.bottom_sheet_icon_size))
            )
        }
    }
}

/**
 * Displays a message when there's no internet connection and no cached data.
 */
@Composable
private fun NoConnectionView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .padding(vertical = getDimension(R.dimen.no_data_box)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = stringResource(R.string.no_connection_text))
    }
}

/**
 * Displays a loading indicator while data is being fetched.
 */
@Composable
private fun LoadingView(enableDarkTheme: Boolean = isSystemInDarkTheme()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display a skeleton loader animation
        SkeletonLoader(enableDarkTheme = enableDarkTheme)
    }
}

/**
 * The main content of the Subscriptions screen, showing all subscription options and controls.
 *
 * @param viewModel The SubscriptionsViewModel providing data and handling events
 * @param activity The parent ComponentActivity
 * @param onShowMoreDialog Callback for when the "More" button is clicked
 * @param modifier Modifier for customizing the layout
 */
@Composable
private fun MainContent(
    viewModel: SubscriptionsViewModel,
    activity: ComponentActivity,
    onShowMoreDialog: () -> Unit,
    modifier: Modifier = Modifier,
    enableDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    val filteredProductDetails = viewModel.filteredProductDetails
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    
    // Create and remember scroll states for both scrollable areas
    val mainContentScrollState = rememberScrollState()
    val plansScrollState = rememberScrollState()
    
    // Store the scroll states in the ViewModel for access from callbacks
    LaunchedEffect(mainContentScrollState, plansScrollState) {
        LogUtils.d(TAG, "Setting scroll states: main=${mainContentScrollState.value}, plans=${plansScrollState.value}")
        viewModel.mainContentScrollState = mainContentScrollState
        viewModel.plansScrollState = plansScrollState
    }
    
    Box {
        // In landscape mode, make the entire content scrollable
        // In portrait mode, only the plan cards will be scrollable (handled in ScrollablePlans)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background)
                .then(
                    if (isLandscape) {
                        Modifier.verticalScroll(mainContentScrollState)
                    } else {
                        Modifier
                    }
                )
        ) {
            // Header content with padding
            Column(
                modifier = Modifier.padding(horizontal = getDimension(R.dimen.spacing_xlarge))
            ) {
                // Show iOS platform warning if user is on iOS platform
                if (viewModel.isIosPlatform) {
                    IosPlatformWarning()
                }
                
                // Display logo and tagline
                LogoAndTagline(logoUrl = viewModel.finalLogoUrl!!)
                
                // Display tab selector for different subscription categories
                TabSelectorSection(viewModel, activity)
                
                Spacer(modifier = Modifier.height(getDimension(R.dimen.spacing_medium)))
            }

            // Show current storage banner if user has base service level but no current product
            if(viewModel.baseServiceLevel != null && viewModel.currentProduct == null) {
                CurrentStorageBanner(baseServiceLevel = viewModel.baseServiceLevel!!, enableDarkTheme = enableDarkTheme)
            }

            // Plans section with horizontal padding
            Column(
                modifier = Modifier.padding(horizontal = getDimension(R.dimen.spacing_xlarge))
            ) {
                Spacer(modifier = Modifier.height(getDimension(R.dimen.spacing_medium)))
                // Display scrollable subscription plans
                ScrollablePlans(
                    activity = activity,
                    enableDarkTheme = enableDarkTheme,
                    shouldScroll = !isLandscape, // Only enable scrolling for plans in portrait mode
                    scrollState = plansScrollState // Pass the scroll state to ScrollablePlans
                )
                
                // Add bottom spacing that adjusts based on orientation and selection state
                Spacer(modifier = calculateBottomSpacing(
                    configuration = configuration,
                    selectedPlan = viewModel.selectedPlan,
                    modifier = modifier,
//                    isPendingPurchase = viewModel.isPurchasePending
                ))
            }
        }

        // Fixed bottom action buttons (Continue and More)
        BottomActionBox(
            configuration = configuration,
            viewModel = viewModel,
            filteredProductDetails = filteredProductDetails,
            activity = activity,
            onShowMoreDialog = onShowMoreDialog,
            modifier = modifier.align(Alignment.BottomCenter)
        )
    }
}

/**
 * Displays a warning for iOS platform users about subscription management.
 */
@Composable
private fun IosPlatformWarning() {
    val context = LocalContext.current
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = getDimension(R.dimen.ios_warning_vertical_padding))
            .background(
                color = Color(context.getColor(R.color.ios_warning_background)),
                shape = MaterialTheme.shapes.medium
            )
            .padding(getDimension(R.dimen.ios_warning_internal_padding))
    ) {
        Column {
            // Warning header with icon
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = rememberAsyncImagePainter(R.drawable.important),
                    contentDescription = "Important Icon",
                    modifier = Modifier.size(12.dp)
                )
                Spacer(Modifier.width(getDimension(R.dimen.ios_warning_internal_padding)/2))
                Text(
                    "Important", 
                    color = Color(context.getColor(R.color.ios_warning_text_heading)), 
                    fontWeight = FontWeight.W600, 
                    fontSize = getDimensionText(R.dimen.ios_warning_text_size)
                )
            }
            // Warning message
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = getDimension(R.dimen.ios_warning_internal_padding))
            ) {
                Text(
                    text = stringResource(R.string.ios_platform_warning),
                    color = Color(context.getColor(R.color.ios_warning_text)),
                    fontSize = getDimensionText(R.dimen.ios_warning_text_size),
                    fontWeight = FontWeight.W400,
                    textAlign = TextAlign.Left,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(getDimension(R.dimen.ios_warning_spacing)))
}

/**
 * Displays the app logo and tagline.
 *
 * @param logoUrl URL of the logo image to display
 */
@Composable
private fun LogoAndTagline(logoUrl: String) {
    val context = LocalContext.current
    val logoUrlWidth = getDimension(R.dimen.logo_width)
    val logoUrlHeight = getDimension(R.dimen.logo_height)
    LogUtils.d(TAG, "Logo url is $logoUrl")
    
    // Logo image
    Row(
        modifier = Modifier.fillMaxWidth(),
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
    
    // Tagline text
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = Constants.STORAGE_TAGLINE,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.W700,
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center
            )
        )
    }
    Spacer(modifier = Modifier.height(getDimension(R.dimen.spacing_small)))
}

/**
 * Displays the tab selector for different subscription categories.
 *
 * @param viewModel The SubscriptionsViewModel providing data and handling events
 * @param activity The parent ComponentActivity
 */
@Composable
private fun TabSelectorSection(viewModel: SubscriptionsViewModel, activity: ComponentActivity) {
    // Get the selected tab or default to the first available tab
    (viewModel.selectedTab ?: viewModel.productDetails?.let {
        TabOption.getAvailableTabs(it).first()
    })?.let { selectedTab ->
        TabSelector(
            selectedTab = selectedTab,
            onTabSelected = { tab ->
                // Update selected tab in ViewModel and trigger filtering
                viewModel.selectedTab = tab
                viewModel.onTabSelected(tab = tab)
            },
            modifier = Modifier.fillMaxWidth(),
            activity = activity
        )
    }
}

/**
 * Displays a banner showing the user's current storage plan.
 *
 * @param baseServiceLevel String representing the user's base service level
 */
@Composable
private fun CurrentStorageBanner(baseServiceLevel: String, enableDarkTheme : Boolean = isSystemInDarkTheme()) {
    val context = LocalContext.current
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(getDimension(R.dimen.current_storage_banner_height))
            .background(color = Color(context.getColor(if(enableDarkTheme) R.color.current_storage_banner_background_dark else R.color.current_storage_banner_background_light)))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Extract storage size from the base service level
            val formattedStorage: String = extractSize(baseServiceLevel)
            val rawString = stringResource(
                R.string.current_storage_banner_text,
                formattedStorage
            )

            // Build an AnnotatedString to make the storage size bold
            val formattedText = buildAnnotatedString {
                val firstIndex = rawString.indexOf(formattedStorage)
                append(rawString)

                // Make the storage size bold
                if (firstIndex >= 0) {
                    addStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = if(enableDarkTheme) Color(0xfffefeff) else Color.Black
                        ),
                        start = firstIndex,
                        end = firstIndex + formattedStorage.length
                    )
                }
            }

            // Display styled text
            Text(
                text = formattedText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.W400,
                    color = if(enableDarkTheme) Color(0xfffefeff) else  MaterialTheme.colorScheme.onSecondary,
                    fontSize = getDimensionText(R.dimen.box_plan_renew_size),
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}

/**
 * Calculates the appropriate bottom spacing based on device orientation and plan selection state.
 *
 * @param configuration The current device Configuration
 * @param selectedPlan The index of the selected plan (-1 if none selected)
 * @param modifier The base Modifier to extend
 * @return A Modifier with the appropriate height
 */
@Composable
private fun calculateBottomSpacing(
    configuration: Configuration,
    selectedPlan: Int,
    modifier: Modifier,

): Modifier {
    // When a purchase is pending, we don't need bottom spacing for the action box
    
    return modifier.let {
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Landscape mode spacing
            it.height(
                if (selectedPlan != -1)
                    getDimension(R.dimen.box_landscape_selected_height) 
                else 
                    getDimension(R.dimen.box_landscape_not_selected_height)
            )
        } else {
            // Portrait mode spacing
            it.height(
                if (selectedPlan != -1) 
                    getDimension(R.dimen.box_non_landscape_selected_height)
                else 
                    getDimension(R.dimen.box_non_landscape_not_selected_height)
            )
        }
    }
}

/**
 * Displays the bottom action box with Continue and More buttons.
 *
 * @param configuration The current device Configuration
 * @param viewModel The SubscriptionsViewModel providing data and handling events
 * @param filteredProductDetails List of filtered product details
 * @param activity The parent ComponentActivity
 * @param modifier The base Modifier to extend
 * @param onShowMoreDialog Callback for when the "More" button is clicked
 */
@Composable
private fun BottomActionBox(
    configuration: Configuration,
    viewModel: SubscriptionsViewModel,
    filteredProductDetails: List<ProductDetails>?,
    activity: ComponentActivity,
    modifier: Modifier,
    onShowMoreDialog: () -> Unit
) {
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .let {
                // Set height based on orientation
                if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    it.height(getDimension(R.dimen.box_landscape_not_selected_height))
                } else {
                    it.height(getDimension(R.dimen.box_non_landscape_not_selected_height))
                }
            }
            .drawBehind {
                // Create a shadow effect at the top of the box
                val shadowColor = Color.Black.copy(alpha = 0.068f)
                val shadowRadius = 35.dp.toPx()
                val offsetY = -shadowRadius / 2
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, shadowColor),
                        startY = offsetY,
                        endY = offsetY + shadowRadius,
                        tileMode = TileMode.Clamp
                    ),
                    topLeft = Offset(0f, offsetY),
                    size = Size(size.width, shadowRadius)
                )
            }
            .background(color = MaterialTheme.colorScheme.tertiary)
    ) {
        // Display buttons based on orientation
        when(configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                PortraitActionButtons(
                    viewModel = viewModel,
                    filteredProductDetails = filteredProductDetails,
                    activity = activity,
                    onShowMoreDialog = onShowMoreDialog
                )
            }
            Configuration.ORIENTATION_LANDSCAPE -> {
                LandscapeActionButtons(
                    viewModel = viewModel,
                    filteredProductDetails = filteredProductDetails,
                    activity = activity,
                    onShowMoreDialog = onShowMoreDialog
                )
            }
            else -> {
                // For square or undefined orientation, use portrait layout as default
                PortraitActionButtons(
                    viewModel = viewModel,
                    filteredProductDetails = filteredProductDetails,
                    activity = activity,
                    onShowMoreDialog = onShowMoreDialog
                )
            }
        }
    }
}

/**
 * Displays action buttons in portrait orientation (stacked vertically).
 *
 * @param viewModel The SubscriptionsViewModel providing data and handling events
 * @param filteredProductDetails List of filtered product details
 * @param activity The parent ComponentActivity
 * @param onShowMoreDialog Callback for when the "More" button is clicked
 */
@Composable
private fun PortraitActionButtons(
    viewModel: SubscriptionsViewModel,
    filteredProductDetails: List<ProductDetails>?,
    activity: ComponentActivity,
    onShowMoreDialog: () -> Unit
) {
    Box(
        modifier = Modifier.padding(getDimension(R.dimen.box_portrait_padding))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Continue button
            ContinueButton(
                viewModel = viewModel,
                filteredProductDetails = filteredProductDetails,
                activity = activity,
                isLandscape = false
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // More button
            MoreButton(
                viewModel = viewModel,
                onShowMoreDialog = onShowMoreDialog,
                isLandscape = false
            )
        }
    }
}

/**
 * Displays action buttons in landscape orientation (side by side).
 *
 * @param viewModel The SubscriptionsViewModel providing data and handling events
 * @param filteredProductDetails List of filtered product details
 * @param activity The parent ComponentActivity
 * @param onShowMoreDialog Callback for when the "More" button is clicked
 */
@Composable
private fun LandscapeActionButtons(
    viewModel: SubscriptionsViewModel,
    filteredProductDetails: List<ProductDetails>?,
    activity: ComponentActivity,
    onShowMoreDialog: () -> Unit
) {
    Box(
        modifier = Modifier.padding(getDimension(R.dimen.box_landscape_padding))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(getDimension(R.dimen.box_landscape_spacer_height)))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                // Continue button (takes 50% of width)
                ContinueButton(
                    viewModel = viewModel,
                    filteredProductDetails = filteredProductDetails,
                    activity = activity,
                    isLandscape = true,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // More button (takes 50% of width)
                MoreButton(
                    viewModel = viewModel,
                    onShowMoreDialog = onShowMoreDialog,
                    isLandscape = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Displays the Continue button for purchasing the selected subscription.
 *
 * @param viewModel The SubscriptionsViewModel providing data and handling events
 * @param filteredProductDetails List of filtered product details
 * @param activity The parent ComponentActivity
 * @param isLandscape Whether the device is in landscape orientation
 * @param modifier Modifier for customizing the layout
 */
@Composable
private fun ContinueButton(
    viewModel: SubscriptionsViewModel,
    filteredProductDetails: List<ProductDetails>?,
    activity: ComponentActivity,
    isLandscape: Boolean,
    modifier: Modifier = Modifier
) {
    // Adjust dimensions based on orientation
    val buttonHeight = if (isLandscape) {
        getDimension(R.dimen.landscape_continue_btn_height)
    } else {
        getDimension(R.dimen.continue_btn_height)
    }
    
    val fontSize = if (isLandscape) {
        getDimensionText(R.dimen.landscape_continue_btn_font_size)
    } else {
        getDimensionText(R.dimen.continue_btn_font_size)
    }
    
    val spacerWidth = if (isLandscape) {
        getDimension(R.dimen.landscape_continue_btn_spacer)
    } else {
        getDimension(R.dimen.continue_btn_spacer)
    }
    
    Button(
        modifier = modifier
            .fillMaxWidth()
            .height(buttonHeight)
            .testTag("continue_button_${if (viewModel.selectedPlan != -1 &&
                !viewModel.isCurrentProductBeingUpdated &&
                !viewModel.isIosPlatform && !viewModel.isPurchasePending ) "enabled" else "disabled"}"),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
            disabledContentColor = Color.White.copy(alpha = 0.4f)
        ),
        onClick = {
            // Initiate purchase of the selected subscription
            filteredProductDetails?.get(viewModel.selectedPlan)?.let {
                viewModel.purchaseSubscription(
                    activity = activity,
                    productDetails = it,
                    onError = { error ->
                        LogUtils.d(TAG, "Error: $error")
                    }
                )
            }
        },
        // Button is enabled only if a plan is selected, not currently updating, and not on iOS
        enabled = viewModel.selectedPlan != -1 && 
                !viewModel.isCurrentProductBeingUpdated && 
                !viewModel.isIosPlatform && !viewModel.isPurchasePending
    ) {
        Row {
            Text(
                text = stringResource(R.string.continue_text),
                fontSize = fontSize,
                fontWeight = FontWeight.W600,
                color = Color.White
            )
            Spacer(modifier = Modifier.width(spacerWidth))
            // Show loading indicator if a purchase is in progress
            if (viewModel.isCurrentProductBeingUpdated) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = getDimension(R.dimen.circular_progress_indicator_width),
                    modifier = Modifier.size(getDimension(R.dimen.circular_progress_indicator_modifier_size))
                )
            }
        }
    }
}

/**
 * Displays the More button for showing additional options.
 *
 * @param viewModel The SubscriptionsViewModel providing data and handling events
 * @param onShowMoreDialog Callback for when the "More" button is clicked
 * @param isLandscape Whether the device is in landscape orientation
 * @param modifier Modifier for customizing the layout
 */
@Composable
private fun MoreButton(
    viewModel: SubscriptionsViewModel,
    onShowMoreDialog: () -> Unit,
    isLandscape: Boolean,
    modifier: Modifier = Modifier
) {
    // Adjust dimensions based on orientation
    val buttonHeight = if (isLandscape) {
        getDimension(R.dimen.landscape_continue_btn_height)
    } else {
        getDimension(R.dimen.continue_btn_height)
    }
    
    val fontSize = if (isLandscape) {
        getDimensionText(R.dimen.landscape_continue_btn_font_size)
    } else {
        getDimensionText(R.dimen.continue_btn_font_size)
    }
    
    // Button is enabled only if not currently updating and not on iOS
    val isEnabled = !viewModel.isCurrentProductBeingUpdated && !viewModel.isIosPlatform && !viewModel.isPurchasePending
    
    Button(
        onClick = onShowMoreDialog,
        modifier = modifier
            .fillMaxWidth()
            .height(buttonHeight),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = MaterialTheme.colorScheme.primary,
        ),
        enabled = isEnabled
    ) {
        Text(
            text = stringResource(R.string.more_button_text),
            fontSize = fontSize,
            color = if(isEnabled) 
                MaterialTheme.colorScheme.onTertiary 
            else 
                MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.5f),
            fontWeight = FontWeight.W600
        )
    }
}

/**
 * Extracts the storage size from a service level string.
 * For example, extracts "5G" from "Basic 5G Plan" and returns "5GB".
 *
 * @param input The service level string
 * @return The extracted storage size with "B" appended
 */
fun extractSize(input: String): String {
    val regex = "\\d+G".toRegex()  // Matches a number followed by 'G'
    return "${regex.find(input)?.value}B" // Returns the matched value or null if not found
}

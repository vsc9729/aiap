package com.synchronoss.aiap.presentation.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.synchronoss.aiap.core.domain.handlers.PurchaseUpdateHandler
import com.synchronoss.aiap.core.domain.handlers.SubscriptionCancelledHandler
import com.synchronoss.aiap.core.domain.usecases.activity.LibraryActivityManagerUseCases
import com.synchronoss.aiap.core.domain.usecases.analytics.SegmentAnalyticsUseCases
import com.synchronoss.aiap.core.domain.usecases.billing.BillingManagerUseCases
import com.synchronoss.aiap.core.domain.usecases.product.ProductManagerUseCases
import com.synchronoss.aiap.ui.theme.ThemeLoader
import javax.inject.Inject

class SubscriptionsViewModelFactory @Inject constructor(
    private val billingManagerUseCases: BillingManagerUseCases,
    private val productManagerUseCases: ProductManagerUseCases,
    private val themeLoader: ThemeLoader,
    private val libraryActivityManagerUseCases: LibraryActivityManagerUseCases,
    private val purchaseUpdateHandler: PurchaseUpdateHandler,
    private val subscriptionCancelledHandler: SubscriptionCancelledHandler,
    private val segmentAnalyticsUseCases: SegmentAnalyticsUseCases,
    private val context: Context
) : ViewModelProvider.Factory {
    
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SubscriptionsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SubscriptionsViewModel(
                billingManagerUseCases,
                productManagerUseCases,
                themeLoader,
                libraryActivityManagerUseCases,
                purchaseUpdateHandler,
                subscriptionCancelledHandler,
                segmentAnalyticsUseCases,
                context
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 
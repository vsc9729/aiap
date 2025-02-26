package com.synchronoss.aiap.presentation.subscriptions.wrapper

import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import com.synchronoss.aiap.presentation.viewmodels.SubscriptionsViewModel
import com.synchronoss.aiap.presentation.viewmodels.SubscriptionsViewModelFactory
import javax.inject.Inject

/**
 * Wrapper class for ScrollablePlans that can be injected by Dagger
 */
class ScrollablePlansWrapper @Inject constructor() {

    @Inject
    lateinit var subscriptionsViewModelFactory: SubscriptionsViewModelFactory

    fun getViewModel(activity: ComponentActivity): SubscriptionsViewModel {
        return ViewModelProvider(activity, subscriptionsViewModelFactory)[SubscriptionsViewModel::class.java]
    }
} 
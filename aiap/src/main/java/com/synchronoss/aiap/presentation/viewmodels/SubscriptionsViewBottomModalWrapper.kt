package com.synchronoss.aiap.presentation.viewmodels

import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import javax.inject.Inject

/**
 * Wrapper class for SubscriptionsViewBottomModal that can be injected by Dagger
 */
class SubscriptionsViewBottomModalWrapper @Inject constructor() {

    @Inject
    lateinit var subscriptionsViewModelFactory: SubscriptionsViewModelFactory

    fun getViewModel(activity: ComponentActivity): SubscriptionsViewModel {
        return ViewModelProvider(activity, subscriptionsViewModelFactory)[SubscriptionsViewModel::class.java]
    }
} 
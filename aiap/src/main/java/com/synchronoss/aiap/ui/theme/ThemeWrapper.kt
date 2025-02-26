package com.synchronoss.aiap.ui.theme

import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import com.synchronoss.aiap.presentation.viewmodels.SubscriptionsViewModel
import com.synchronoss.aiap.presentation.viewmodels.SubscriptionsViewModelFactory
import javax.inject.Inject

/**
 * Wrapper class for Theme components that can be injected by Dagger
 */
class ThemeWrapper @Inject constructor() {

    @Inject
    lateinit var subscriptionsViewModelFactory: SubscriptionsViewModelFactory

    fun getViewModel(activity: ComponentActivity): SubscriptionsViewModel {
        return ViewModelProvider(activity, subscriptionsViewModelFactory)[SubscriptionsViewModel::class.java]
    }
} 
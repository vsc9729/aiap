package com.synchronoss.aiap.core.di

import android.app.Application
import com.synchronoss.aiap.presentation.viewmodels.SubscriptionsViewBottomModalWrapper
import com.synchronoss.aiap.presentation.viewmodels.SubscriptionsViewModelFactory
import com.synchronoss.aiap.presentation.wrappers.SubscriptionsViewWrapper
import com.synchronoss.aiap.presentation.subscriptions.wrapper.MoreBottomSheetWrapper
import com.synchronoss.aiap.presentation.subscriptions.wrapper.PlanCardsWrapper
import com.synchronoss.aiap.presentation.subscriptions.wrapper.ScrollablePlansWrapper
import com.synchronoss.aiap.presentation.subscriptions.wrapper.TabSelectorWrapper
import com.synchronoss.aiap.ui.theme.ThemeWrapper
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        BillingModule::class,
        ProductModule::class,
        ThemeModule::class,
        CacheModule::class,
        AnalyticsModule::class,
        LibraryActivityModule::class,
        ViewModelModule::class,
        NetworkModule::class
    ]
)
interface AiapComponent {
    
    fun inject(wrapper: SubscriptionsViewBottomModalWrapper)
    
    fun inject(wrapper: SubscriptionsViewWrapper)
    
    fun inject(wrapper: TabSelectorWrapper)
    
    fun inject(wrapper: MoreBottomSheetWrapper)
    
    fun inject(wrapper: PlanCardsWrapper)
    
    fun inject(wrapper: ScrollablePlansWrapper)
    
    fun inject(wrapper: ThemeWrapper)
    
    fun subscriptionsViewModelFactory(): SubscriptionsViewModelFactory
    
    @Component.Factory
    interface Factory {
        fun create(@BindsInstance application: Application): AiapComponent
    }
} 
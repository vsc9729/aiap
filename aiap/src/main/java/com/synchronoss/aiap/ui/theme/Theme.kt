package com.synchronoss.aiap.ui.theme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.synchronoss.aiap.presentation.SubscriptionsViewModel



@Composable
fun SampleAiAPTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val subscriptionViewModel = hiltViewModel<SubscriptionsViewModel>()
    val colorScheme = when {
        darkTheme -> subscriptionViewModel.darkThemeColorScheme!!
        else -> subscriptionViewModel.lightThemeColorScheme!!
    }
    if(darkTheme){
        if(subscriptionViewModel.darkThemeLogoUrl == null){
            subscriptionViewModel.finalLogoUrl = subscriptionViewModel.lightThemeLogoUrl
        }
        else{
            subscriptionViewModel.finalLogoUrl = subscriptionViewModel.darkThemeLogoUrl
        }
    }
    else{
        subscriptionViewModel.finalLogoUrl = subscriptionViewModel.lightThemeLogoUrl
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
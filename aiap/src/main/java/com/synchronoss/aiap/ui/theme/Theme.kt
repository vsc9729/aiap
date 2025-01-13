package com.synchronoss.aiap.ui.theme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.synchronoss.aiap.presentation.SubscriptionsViewModel



@Composable
fun SampleAiAPTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val subscriptionViewModel = hiltViewModel<SubscriptionsViewModel>()
    val colorScheme = when {
        darkTheme -> subscriptionViewModel.darkThemeColorScheme!!
        else -> subscriptionViewModel.lightThemeColorScheme!!
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
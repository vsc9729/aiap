package com.synchronoss.aiap.ui.theme
import androidx.activity.ComponentActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.synchronoss.aiap.core.di.DaggerAiapComponent

@Composable
fun AiAPTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
    activity: ComponentActivity? = null
) {
    val subscriptionViewModel = if (activity != null) {
        val wrapper = remember {
            val wrapper = ThemeWrapper()
            val application = activity.application
            val aiapComponent = DaggerAiapComponent.factory().create(application)
            aiapComponent.inject(wrapper)
            wrapper
        }
        
        remember {
            wrapper.getViewModel(activity)
        }
    } else {
        null
    }
    
    val colorScheme = when {
        darkTheme -> subscriptionViewModel?.darkThemeColorScheme ?: darkColorScheme(background = Color.Black)
        else -> subscriptionViewModel?.lightThemeColorScheme ?: lightColorScheme(background = Color.White)
    }
    
    if (subscriptionViewModel != null) {
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
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
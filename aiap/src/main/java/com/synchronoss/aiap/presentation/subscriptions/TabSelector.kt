package com.synchronoss.aiap.presentation.subscriptions

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.synchronoss.aiap.R
import com.synchronoss.aiap.presentation.SubscriptionsViewModel
import com.synchronoss.aiap.utils.getDimension

@Composable
fun TabSelector(
    selectedTab: TabOption,
    onTabSelected: (TabOption) -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val subscriptionsViewModel = hiltViewModel<SubscriptionsViewModel>()
    val availableTabs = subscriptionsViewModel.productDetails?.let { TabOption.getAvailableTabs(it) } ?: emptyList()

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
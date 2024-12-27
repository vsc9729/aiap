import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.synchronoss.aiap.presentation.SubscriptionsViewModel
import com.synchronoss.aiap.utils.AppColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionsViewBottomSheet(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    activity: ComponentActivity,
    subscriptionsViewModel: SubscriptionsViewModel = hiltViewModel()
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val sheetHeight = screenHeight * 0.85f

    // Automatically open the sheet when dialog state is true
    LaunchedEffect(subscriptionsViewModel.dialogState.value) {
        if (subscriptionsViewModel.dialogState.value) {
            coroutineScope.launch {
                sheetState.show()
            }
        }
    }

    if (subscriptionsViewModel.dialogState.value) {
        ModalBottomSheet(
            onDismissRequest = {
                coroutineScope.launch {
                    sheetState.hide()
                    subscriptionsViewModel.dialogState.value = false
                    onDismissRequest()
                }
            },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            modifier = modifier.height(sheetHeight)
        ) {
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                    )
            ) {
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            sheetState.hide()
                            subscriptionsViewModel.dialogState.value = false
                            onDismissRequest()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = AppColors.lightGray
                    )
                }
            }
            SubscriptionsView(activity = activity)
        }
    }
}

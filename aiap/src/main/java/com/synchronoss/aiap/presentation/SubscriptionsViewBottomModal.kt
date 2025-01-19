import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.synchronoss.aiap.presentation.SubscriptionsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import com.synchronoss.aiap.ui.theme.SampleAiAPTheme as SampleAiAPTheme1

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionsViewBottomSheet(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    visible : Boolean,
    partnerUserId: String,
    activity: ComponentActivity,
    subscriptionsViewModel: SubscriptionsViewModel = hiltViewModel(),

) {
    subscriptionsViewModel.initialize(id = partnerUserId)
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )
    val coroutineScope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val sheetHeight = screenHeight * 0.92f

    // Automatically open the sheet when dialog state is true
//    LaunchedEffect(subscriptionsViewModel.dialogState.value) {
//        if (subscriptionsViewModel.dialogState.value) {
//            coroutineScope.launch {
//                sheetState.show()
//            }
//        }
//    }

  //  if (subscriptionsViewModel.dialogState.value) {
    if(visible) {
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
                modifier = modifier.height(sheetHeight),
                dragHandle = null,


                ) {
                SampleAiAPTheme1 {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.background,
                            ),
                        horizontalArrangement = Arrangement.End,


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
                                tint = MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    SubscriptionsView(activity = activity)
                }


            }
        }
    }

    //}


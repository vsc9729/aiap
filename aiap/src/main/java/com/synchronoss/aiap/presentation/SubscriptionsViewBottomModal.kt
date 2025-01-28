import androidx.activity.ComponentActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.synchronoss.aiap.presentation.SubscriptionsViewModel
import com.synchronoss.aiap.presentation.ToastComposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.internal.wait
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
    val isVisible: MutableState<Boolean> = remember {mutableStateOf(false)}
    val coroutineScope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val sheetHeight = screenHeight * 0.98f


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
                                color = if( isSystemInDarkTheme()) Color(0xFF0D0D0D)    else  Color.White,
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
                                tint = if( isSystemInDarkTheme()) Color.White   else  Color(0xFF6B7280),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Box{

                        SubscriptionsView(activity = activity)

                        ToastComposable(
                            heading = subscriptionsViewModel.toastState.heading,
                            subText = subscriptionsViewModel.toastState.message,
                            onDismiss = { subscriptionsViewModel.hideToast() },
                            isVisible = subscriptionsViewModel.toastState.isVisible,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 30.dp, start = 16.dp, end = 16.dp)
                        )
                    }

                }
            }
        }
    }

    //}


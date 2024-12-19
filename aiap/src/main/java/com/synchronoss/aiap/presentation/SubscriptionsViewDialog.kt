import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.synchronoss.aiap.presentation.SubscriptionsViewModel

@Composable
fun SubscriptionsViewDialog(modifier: Modifier = Modifier, onDismissRequest: () -> Unit, activity: ComponentActivity) {
    val subscriptionsViewModel: SubscriptionsViewModel = hiltViewModel<SubscriptionsViewModel>()
    if (subscriptionsViewModel.dialogState.value)
        Dialog(
            properties = DialogProperties(usePlatformDefaultWidth = false),
            onDismissRequest = {
                subscriptionsViewModel.dialogState.value = false
                onDismissRequest()
            }) {
            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = modifier.fillMaxWidth()
                )
                {
                    IconButton(
                        onClick = {
                            subscriptionsViewModel.dialogState.value = false
                            onDismissRequest()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Black
                        )
                    }
                }
                SubscriptionsView(activity = activity)
            }
        }

}


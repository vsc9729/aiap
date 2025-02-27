package com.synchronoss.aiap.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.synchronoss.aiap.R

@Composable
fun ToastComposable(
     heading: String,
    subText: String,
    isSuccess: Boolean,
    formatArgs: Any?,
    headingResId: Int? = null,
    messageResId: Int? = null,
    onDismiss: () -> Unit,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    val finalHeading = headingResId?.let { stringResource(it) } ?: heading
    val finalMessage = messageResId?.let { if(formatArgs != null) stringResource(it, formatArgs) else  stringResource(it) } ?: subText
    val context = LocalContext.current
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier

    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(
                    color = Color(context.getColor(if(isSuccess) R.color.success_green_background else R.color.error_red)),
                    shape = RoundedCornerShape(8.dp)
                )

        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxSize()
            ) {
                // Red block on the left
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(6.dp)
                        .clip(RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
                        .background(
                            color = if(isSuccess) Color(context.getColor(R.color.success_green_border)) else Color.Red,
                        )
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Text content
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = finalHeading,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                    )
                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Text(
                        text = finalMessage,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 14.sp,
                            color = Color(0xFF525252)
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                    ,
                    contentAlignment = Alignment.TopStart
                ) {
                     IconButton(
                                onClick = {onDismiss()}
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    modifier = Modifier.size(24.dp),
                                    tint = Color.Black
                                )
                            }
                }
            }
        }
    }
}

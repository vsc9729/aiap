package com.synchronoss.aiap.presentation.subscriptions

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.synchronoss.aiap.R
import com.synchronoss.aiap.utils.getDimension

@Composable
fun DialogBox(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .height(getDimension(R.dimen.dialog_height))
                .width(getDimension(R.dimen.dialog_width))
                .padding(getDimension(R.dimen.dialog_padding))
                .clickable(enabled = false) {},  // Prevent click propagation
            shape = RoundedCornerShape(getDimension(R.dimen.dialog_corner_radius)),
            colors = CardDefaults.cardColors(
                containerColor = Color.White,
            ),
        ) {
            Column(
                modifier = Modifier.padding(getDimension(R.dimen.dialog_content_padding)),
                verticalArrangement = Arrangement.spacedBy(getDimension(R.dimen.dialog_spacing)),
            ) {
                Text(
                    text = stringResource(R.string.dialog_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black,
                    fontWeight = FontWeight.W400
                )

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(
                        text = stringResource(R.string.dialog_coupon_label),
                        color = Color.Gray
                    ) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                    ),
                    textStyle = TextStyle(color = Color.Black)
                )

                Spacer(modifier = Modifier.height(getDimension(R.dimen.dialog_spacer_height)))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        )
                    ) {
                        Text(stringResource(R.string.dialog_cancel))
                    }

                    Spacer(modifier = Modifier.width(getDimension(R.dimen.dialog_spacing)))

                    Button(
                        onClick = {
                            onConfirm(text)
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/redeem?code=$text"))
                            context.startActivity(intent)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        )
                    ) {
                        Text(stringResource(R.string.dialog_ok))
                    }
                }
            }
        }
    }
} 
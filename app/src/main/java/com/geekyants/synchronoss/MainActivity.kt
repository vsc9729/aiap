package com.geekyants.synchronoss


import SubscriptionsViewBottomSheet
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button

import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel


import com.geekyants.synchronoss.ui.theme.Poppins
import com.geekyants.synchronoss.ui.theme.Roboto
import com.geekyants.synchronoss.ui.theme.SynchronossTheme
import com.synchronoss.aiap.presentation.SubscriptionsViewModel
import com.synchronoss.aiap.ui.theme.ThemeLoader
import com.synchronoss.aiap.utils.Vendors
import dagger.hilt.android.AndroidEntryPoint


//Implementors need to implement the following annotation
// in their app and create an application class with the
// annotation @HiltAndroidApp because the library uses
// Dagger Hilt for dependency injection
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        makeStatusBarTransparent()
        setContent {
            SynchronossTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            androidx.compose.foundation.layout.WindowInsets.systemBars.asPaddingValues()
                        ), containerColor = Color(0xFFE3EBFC)
                ) { innerPadding ->
                    val subscriptionsViewModel: SubscriptionsViewModel =
                        hiltViewModel<SubscriptionsViewModel>()
                    SubscriptionScreen(
                        modifier = Modifier.padding(innerPadding),
                        onClickSubscribe = {
                            subscriptionsViewModel.dialogState.value = true
                        }
                    )
                    SubscriptionsViewBottomSheet(
                        onDismissRequest = {
                            //TODO: If the implementor wants to do something on dismissal
                        },
                        activity = this,
                        vendor = Vendors.Capsyl

                    )


                }


            }

        }

    }


    private fun makeStatusBarTransparent() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = androidx.compose.ui.graphics.Color.Transparent.toArgb()
    }
}


@Composable
fun SubscriptionScreen(
    modifier: Modifier = Modifier,
    onClickSubscribe: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                horizontal = 10.dp
            ),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Go Premium",
//            modifier = modifier,
                fontFamily = Roboto,
                fontWeight = FontWeight.Bold,
                color = Color(0xff0D368C),
                fontSize = 40.sp,
                modifier = Modifier.padding(0.dp)
            )
            Spacer(
                modifier = Modifier.height(10.dp)
            )
            Text(
                text = "Unlock all the power of this mobile tool and enjoy digital experience like never before!",
//            modifier = modifier,
                fontFamily = Poppins,
                color = Color(0xff0D368C),
                fontSize = 17.sp,
                modifier = Modifier.padding(0.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Image(
                painter = painterResource(id = R.drawable.top),
                contentDescription = "Sample Image",

                modifier = Modifier.size(300.dp)
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally

        ) {
            Box(
                modifier = Modifier

                    .fillMaxWidth()
                    .clip(shape = RoundedCornerShape(20.dp))
                    .background(Color(0x1A092765))
                    .border(
                        2.dp,
                        Color(0xff0D368C),
                        RoundedCornerShape(20.dp),

                        )
                    .padding(vertical = 10.dp, horizontal = 20.dp)

            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Annual",
                            fontFamily = Roboto,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xff0D368C),
                            fontSize = 25.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                        Box(
                            modifier = Modifier

                                .width(70.dp)
                                .height(30.dp)
                                .clip(shape = RoundedCornerShape(Int.MAX_VALUE.dp))
                                .background(Color(0xff26CB63))

                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxSize(),
                            ) {
                                Text(
                                    text = "Best Value",
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xffffffff),
                                    fontSize = 9.sp,

                                    )
                            }

                        }
                    }
                    Text(
                        text = "First 30 days free - Then \$999/year",
                        fontFamily = Poppins,
                        color = Color(0xff0D368C),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }

            }
            Spacer(
                modifier = Modifier.height(10.dp)
            )
            Box(
                modifier = Modifier

                    .fillMaxWidth()
                    .clip(shape = RoundedCornerShape(20.dp))
                    .background(Color(0x1A092765))
                    .border(
                        2.dp,
                        Color(0xff0D368C),
                        RoundedCornerShape(20.dp),

                        )
                    .padding(vertical = 10.dp, horizontal = 20.dp)

            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Monthly",
                            fontFamily = Roboto,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xff0D368C),
                            fontSize = 25.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Text(
                        text = "First 7 days free - Then \$4.99/month",
                        fontFamily = Poppins,
                        color = Color(0xff0D368C),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }

            }

        }
        Spacer(
            modifier = Modifier.height(10.dp)
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { onClickSubscribe() },
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    Color(0xff0D368C),
                    contentColor = Color(0xFFFFFFFF)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp)
                    .clip(shape = RoundedCornerShape(20.dp))
//                    .padding(16.dp)
            ) {
                Text(
                    text = "Subscribe Now",
                    fontFamily = Roboto,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFFFFF),
                    fontSize = 18.sp
                )
            }
            Text(
                "By placing this order, you agree to the Terms of Service and Privacy Policy. Subscription automatically renews unless auto-renew is turned off at least 24-hours before the end of the current period.",
                fontFamily = Poppins,
                color = Color(0xff0D368C),
                fontSize = 12.sp,
                modifier = Modifier.padding(10.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SynchronossTheme {
        SubscriptionScreen()
    }
}
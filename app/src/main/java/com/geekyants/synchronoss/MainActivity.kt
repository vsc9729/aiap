package com.geekyants.synchronoss

import SubscriptionsViewBottomSheet
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.core.view.WindowCompat
import com.geekyants.synchronoss.ui.theme.Poppins
import com.geekyants.synchronoss.ui.theme.Roboto
import com.geekyants.synchronoss.ui.theme.SynchronossTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var launchedFromIntent: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intentLcid: String? = intent.getStringExtra("lcid")
        // Set flag if launched with LCID
        launchedFromIntent = intentLcid != null

        enableEdgeToEdge()
        setContent {
            SynchronossTheme {

                SetupSystemBars()
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            androidx.compose.foundation.layout.WindowInsets.systemBars.asPaddingValues()
                        ), containerColor = Color(0xFFE3EBFC)
                ) { innerPadding ->
                    // Initialize showBottomSheet based on intent LCID
                    var showBottomSheet by rememberSaveable { mutableStateOf(intentLcid != null) }
                    var lcid by rememberSaveable { mutableStateOf(intentLcid ?: "") }

                    SubscriptionScreen(
                        modifier = Modifier.padding(innerPadding),
                        lcid = lcid,
                        onLcidChange = { lcid = it },
                        onClickSubscribe = {
                            showBottomSheet = true
                        }
                    )

                    // Rest of the code remains the same
                    if(showBottomSheet) {
                        Log.d(null, "Showing bottom sheet")
                        //Composable responsible for displaying the paywall
                        SubscriptionsViewBottomSheet(
                            onDismissRequest = {
                                if (launchedFromIntent) {
                                    finish()
                                }else{
                                    showBottomSheet = false
                                }
                                //If the implementor wants to do something on dismissal
                            },
                            visible = showBottomSheet,
                            activity = this,
                            partnerUserId = lcid.ifEmpty { "6215c9b8-29ef-45d1-a6dd-d9e5f3dbfd5c" }, //Pass you own use id for testing
                            isLaunchedViaIntent = launchedFromIntent
                            )
                    }
                }

            }

        }
    }
}

@Composable
private fun SetupSystemBars() {
    val view = LocalView.current
    DisposableEffect(view) {
        val window = (view.context as ComponentActivity).window
        WindowCompat.setDecorFitsSystemWindows(window, false)
        onDispose {}
    }
}

@Composable
fun SubscriptionScreen(
    modifier: Modifier = Modifier,
    lcid: String = "",
    onLcidChange: (String) -> Unit = {},
    onClickSubscribe: () -> Unit = {}
) {
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = 0.75*10.dp
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
                fontSize = 0.75*40.sp,
                modifier = Modifier.padding(0.75*0.dp)
            )
            Spacer(
                modifier = Modifier.height(0.75*10.dp)
            )
            Text(
                text = "Unlock all the power of this mobile tool and enjoy digital experience like never before!",
//            modifier = modifier,
                fontFamily = Poppins,
                color = Color(0xff0D368C),
                fontSize = 0.75*17.sp,
                modifier = Modifier.padding(0.75*0.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Image(
                painter = painterResource(id = R.drawable.top),
                contentDescription = "Sample Image",

                modifier = Modifier.size(0.75*300.dp)
            )
        }
        Column {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally

            ) {
                Box(
                    modifier = Modifier

                        .fillMaxWidth()
                        .clip(shape = RoundedCornerShape(0.75*20.dp))
                        .background(Color(0x1A092765))
                        .border(
                            0.75*2.dp,
                            Color(0xff0D368C),
                            RoundedCornerShape(0.75*20.dp),

                            )
                        .padding(vertical = 0.75*10.dp, horizontal = 0.75*20.dp)

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
                                fontSize = 0.75*25.sp,
                                modifier = Modifier.padding(0.75*10.dp)
                            )
                            Box(
                                modifier = Modifier

                                    .width(0.75*70.dp)
                                    .height(0.75*30.dp)
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
                                        fontSize =0.75* 9.sp,

                                        )
                                }

                            }
                        }
                        Text(
                            text = "First 30 days free - Then \$999/year",
                            fontFamily = Poppins,
                            color = Color(0xff0D368C),
                            fontSize = 0.75*14.sp,
                            modifier = Modifier.padding(0.75*10.dp)
                        )
                    }

                }
                Spacer(
                    modifier = Modifier.height(0.75*10.dp)
                )
                Box(
                    modifier = Modifier

                        .fillMaxWidth()
                        .clip(shape = RoundedCornerShape(0.75*20.dp))
                        .background(Color(0x1A092765))
                        .border(
                            0.75*2.dp,
                            Color(0xff0D368C),
                            RoundedCornerShape(0.75*20.dp),

                            )
                        .padding(vertical = 0.75*10.dp, horizontal = 0.75*20.dp)

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
                                fontSize = 0.75*25.sp,
                                modifier = Modifier.padding(0.75*10.dp)
                            )
                        }
                        Text(
                            text = "First 7 days free - Then \$4.99/month",
                            fontFamily = Poppins,
                            color = Color(0xff0D368C),
                            fontSize = 0.75*14.sp,
                            modifier = Modifier.padding(0.75*10.dp)
                        )
                    }

                }

            }
            Spacer(
                modifier = Modifier.height(0.75*10.dp)
            )
            OutlinedTextField(
                value = lcid,
                onValueChange = onLcidChange,
                label = { Text("Enter LCID") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.75 * 10.dp),
                shape = RoundedCornerShape(0.75 * 20.dp),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = Poppins
                )
            )
            
            Spacer(
                modifier = Modifier.height(0.75*10.dp)
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = { onClickSubscribe() },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(0.75*20.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        Color(0xff0D368C),
                        contentColor = Color(0xFFFFFFFF)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.75*65.dp)
                        .clip(shape = RoundedCornerShape(0.75*20.dp))
//                    .padding(0.75*16.dp)
                ) {
                    Text(
                        text = "Subscribe Now",
                        fontFamily = Roboto,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFFFFF),
                        fontSize = 0.75*18.sp
                    )
                }
                Text(
                    "By placing this order, you agree to the Terms of Service and Privacy Policy. Subscription automatically renews unless auto-renew is turned off at least 24-hours before the end of the current period.",
                    fontFamily = Poppins,
                    color = Color(0xff0D368C),
                    fontSize = 0.75*12.sp,
                    modifier = Modifier.padding(0.75*10.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
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
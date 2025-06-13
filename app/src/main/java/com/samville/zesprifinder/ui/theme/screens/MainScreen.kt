package com.samville.zesprifinder.ui.theme.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samville.zesprifinder.ui.theme.viewmodel.MainScreenState
import kotlinx.coroutines.delay
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.samville.zesprifinder.R
import com.samville.zesprifinder.ui.theme.HayGrassGradient

@Preview
@PreviewScreenSizes
@Composable
fun MainScreen(
    state: MainScreenState = MainScreenState.Initial,
    onStartClick: () -> Unit = {},
    showDialog: Boolean = false,
    onDialogDismiss: () -> Unit = {}
) {
    var showLogo by remember { mutableStateOf(false) }
    var showButton by remember { mutableStateOf(false) }
    var showScanningUI by remember { mutableStateOf(false) }
    //var showMapPage by remember { mutableStateOf(false) }
    var distanceText by remember { mutableStateOf("Jarak ke Kiwi: -") }
    //var showNoBeaconMessage by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Simulasi state buat preview
    LaunchedEffect(state) {
        showLogo = true
        delay(1000)
        showButton = true
        when (state) {
            MainScreenState.Initial -> {
                showScanningUI = false
                distanceText = "Jarak ke Kiwi: -"
            }
            MainScreenState.Scanning -> {
                showScanningUI = true
                distanceText = "Jarak ke Kiwi: 8.00 meter\nRSSI: -65 dBm"
            }
            MainScreenState.Dialog -> {
                showScanningUI = true
                distanceText = "Jarak ke Kiwi: 1.00 meter\nRSSI: -50 dBm"
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(HayGrassGradient)
                .padding(paddingValues),
                //.padding(0.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (showLogo) {
                LogoAnimation {
                    Image(
                        painter = painterResource(id = R.drawable.zespri_logo),
                        contentDescription = "Zespri Logo",
                        modifier = Modifier
                            .size(600.dp)
                            .padding(top = 0.dp)
                            .offset(x = 0.dp, y = 60.dp)
                    )
                }
            }
            if (showButton) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(durationMillis = 2000))
                ) {
                    Button(
                        onClick = {
                            onStartClick()
                            showScanningUI = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.2f)
                            .height(100.dp)
                            .offset(x = 0.dp, y = -100.dp)
                            //.padding(bottom = 150.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.button_findzespri),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .width(65.dp)
                            )
                            Text(
                                text = "Find Zespri",
                                fontSize = 32.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
//            if (showScanningUI) {
//                Spacer(modifier = Modifier.height(16.dp))
//                Text(
//                    text = distanceText,
//                    color = MaterialTheme.colorScheme.primary,
//                    fontSize = 16.sp,
//                    textAlign = TextAlign.Center,
//                    modifier = Modifier.fillMaxWidth()
//                )
//            }
        }
    }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDialogDismiss,
            confirmButton = {
                TextButton(onClick = onDialogDismiss) {
                    Text("OK")
                }
            },
            title = { Text("Booth Ditemukan!") },
            text = { Text("Selamat datang di booth Zespri!") }
        )
    }
}

@Composable
fun LogoAnimation(content: @Composable () -> Unit) {
    var animationStarted by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 2f,
        animationSpec = tween(durationMillis = 3000),
        label = "Logo Scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "Logo Alpha"
    )
    val translationY by animateFloatAsState(
        targetValue = if (animationStarted) -200f else 0f,
        animationSpec = tween(durationMillis = 2000),
        label = "Logo Translation"
    )
    LaunchedEffect(Unit) {
        animationStarted = true
    }
    Box(
        modifier = Modifier
            .size(600.dp)
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                alpha = alpha,
                translationY = translationY
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Preview(
    showBackground = true,
    widthDp = 2800,
    heightDp = 1840,
    name = "Tablet Preview - No Dialog"
)
@Composable
fun PreviewMainScreen() {
    MainScreen(
        onStartClick = {},
        state = MainScreenState.Initial,
        showDialog = false,
        onDialogDismiss = {}
    )
}

@Preview(
    showBackground = true,
    widthDp = 2800,
    heightDp = 1840,
    name = "Tablet Preview - With Dialog"
)
@Composable
fun PreviewMainScreenWithDialog() {
    MainScreen(
        onStartClick = {},
        state = MainScreenState.Dialog,
        showDialog = true,
        onDialogDismiss = {}
    )
}

package com.samville.zesprifinder

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.ParcelUuid
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.samville.zesprifinder.ui.theme.ZespriFinderTheme
import com.samville.zesprifinder.ui.theme.ZespriGreen
import com.samville.zesprifinder.ui.theme.HayYellow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.samville.zesprifinder.ui.theme.HayGrassGradient
import kotlinx.coroutines.delay
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanCallback
import no.nordicsemi.android.support.v18.scanner.ScanFilter
import no.nordicsemi.android.support.v18.scanner.ScanResult
import no.nordicsemi.android.support.v18.scanner.ScanSettings
import java.util.*
import kotlin.math.abs
import kotlin.math.pow

@Suppress("DEPRECATION")
class MainActivity : ComponentActivity() {

    private var lastDistance: Double? = null
    private var isDialogShown = false
    private val rssiHistory = LinkedList<Int>()
    private val maxRssiHistory = 3
    private var lastScanTime: Long = 0
    private var isScanning = false

    private val targetBeaconNamespaceId = "0x35e06fb12bc955aae08a"
    private val permissionRequestBluetoothScan = 3
    private lateinit var scanner: BluetoothLeScannerCompat
    private var scanCallback: ScanCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scanner = BluetoothLeScannerCompat.getScanner()
        setContent {
            ZespriFinderTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        state = MainScreenState.Initial,
                        onStartClick = {
                            if (checkPermissions()) startScanning() else requestPermissions()
                        },
                        showZespriDialog = { /* Triggered by state in MainScreen */ }
                    )
                }
            }
        }
    }

    enum class MainScreenState {
        Initial,
        Scanning,
        Dialog
    }

    @Preview
    @PreviewLightDark
    @PreviewScreenSizes
    @Composable
    fun MainScreen(
        state: MainScreenState = MainScreenState.Initial,
        onStartClick: () -> Unit = {},
        showZespriDialog: () -> Unit = {}
    ) {
        var showLogo by remember { mutableStateOf(false) }
        var showButton by remember { mutableStateOf(false) }
        var showScanningUI by remember { mutableStateOf(false) }
        var showMapPage by remember { mutableStateOf(false) }
        var distanceText by remember { mutableStateOf("Jarak ke Kiwi: -") }
        var lastUiUpdateTime by remember { mutableLongStateOf(0L) }
        var showDialog by remember { mutableStateOf(false) }
        var showNoBeaconMessage by remember { mutableStateOf(false) }
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
                    showDialog = true
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
                    .padding(paddingValues)
                    .padding(0.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (showLogo) {
                    LogoAnimation {
                        Image(
                            painter = painterResource(id = R.drawable.zespri_logo), // Ganti jadi nama file yang bener
                            contentDescription = "Zespri Logo",
                            modifier = Modifier
                                .size(900.dp)
                                .padding(top = 20.dp)
                                .offset( x = 0.dp, y = 60.dp)
                        )
                    }
                }

                //Spacer(modifier = Modifier.height(0.dp))

                if (showButton) {
                    AnimatedVisibility(
                        visible = showButton,
                        enter = fadeIn(animationSpec = tween(durationMillis = 1500))
                    ) {
                        Button(
                            onClick = {
                                onStartClick()
                                showScanningUI = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .fillMaxWidth(0.3f)
                                .height(160.dp)
                                .offset(x = 0.dp, y = -60.dp)
                                .padding(bottom = 50.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.button_findzespri),
                                    contentDescription = null,
                                    modifier = Modifier
                                            .fillMaxSize()
                                            .width(30.dp)
                                )
                                Text(
                                    text = "Find Zespri",
                                    fontSize = 40.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                if (showScanningUI) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = distanceText,
                        color = ZespriGreen,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Skip scanning logic buat preview
                    LaunchedEffect(Unit) {
                        val settings = ScanSettings.Builder()
                            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                            .setReportDelay(0L)
                            .build()
                        val filters = listOf(
                            ScanFilter.Builder()
                                .setServiceUuid(ParcelUuid.fromString("0000FEAA-0000-1000-8000-00805F9B34FB"))
                                .build()
                        )

                        scanCallback = object : ScanCallback() {
                            override fun onScanResult(callbackType: Int, result: ScanResult) {
                                val startTime = System.currentTimeMillis()
                                val currentTime = startTime
                                Log.d("BeaconSearch", "Beacons detected, Scan interval: ${if (lastScanTime > 0) currentTime - lastScanTime else 0} ms")
                                lastScanTime = currentTime

                                processScanResult(result) { namespaceId, instanceId, rssi, txPower ->
                                    Log.d("BeaconSearch", "Raw RSSI: $rssi dBm")
                                    if (rssiHistory.isNotEmpty()) {
                                        val avgRssi = rssiHistory.average()
                                        if (abs(rssi - avgRssi) > 10) {
                                            Log.d("BeaconSearch", "Skipping outlier RSSI: $rssi, avg: $avgRssi")
                                            return@processScanResult
                                        }
                                    }
                                    rssiHistory.add(rssi)
                                    if (rssiHistory.size > maxRssiHistory) {
                                        rssiHistory.removeFirst()
                                    }
                                    val smoothedRssi = rssiHistory.average().toInt()
                                    val distance = 0.01 * 10.0.pow((smoothedRssi + 24) / -15.5)
                                    lastDistance = distance.coerceAtLeast(0.0)
                                    Log.d(
                                        "BeaconSearch",
                                        "Beacon: Namespace: $namespaceId, Instance: $instanceId, RSSI: $rssi, Smoothed RSSI: $smoothedRssi, TxPower: $txPower, Jarak: $distance meter"
                                    )
                                    if (currentTime - lastUiUpdateTime >= 500) {
                                        distanceText = "Jarak ke Kiwi: %.2f meter\nRSSI: %d dBm".format(Locale.US, distance, smoothedRssi)
                                        lastUiUpdateTime = currentTime
                                        if (distance <= 1.1 && !isDialogShown) {
                                            showDialog = true
                                            isDialogShown = true
                                        }
                                    }
                                    Log.d("BeaconSearch", "Scan processing time: ${System.currentTimeMillis() - startTime} ms")
                                }
                            }

                            override fun onBatchScanResults(results: MutableList<ScanResult>) {
                                results.forEach { result ->
                                    processScanResult(result) { namespaceId, instanceId, rssi, txPower ->
                                        val startTime = System.currentTimeMillis()
                                        val currentTime = startTime
                                        Log.d("BeaconSearch", "Beacons detected, Scan interval: ${if (lastScanTime > 0) currentTime - lastScanTime else 0} ms")
                                        lastScanTime = currentTime

                                        Log.d("BeaconSearch", "Raw RSSI: $rssi dBm")
                                        if (rssiHistory.isNotEmpty()) {
                                            val avgRssi = rssiHistory.average()
                                            if (abs(rssi - avgRssi) > 10) {
                                                Log.d("BeaconSearch", "Skipping outlier RSSI: $rssi, avg: $avgRssi")
                                                return@processScanResult
                                            }
                                        }
                                        rssiHistory.add(rssi)
                                        if (rssiHistory.size > maxRssiHistory) {
                                            rssiHistory.removeFirst()
                                        }
                                        val smoothedRssi = rssiHistory.average().toInt()
                                        val distance = 0.01 * 10.0.pow((smoothedRssi + 24) / -15.5)
                                        lastDistance = distance.coerceAtLeast(0.0)
                                        Log.d(
                                            "BeaconSearch",
                                            "Beacon: Namespace: $namespaceId, Instance: $instanceId, RSSI: $rssi, Smoothed RSSI: $smoothedRssi, TxPower: $txPower, Jarak: $distance meter"
                                        )
                                        if (currentTime - lastUiUpdateTime >= 500) {
                                            distanceText = "Jarak ke Kiwi: %.2f meter\nRSSI: %d dBm".format(Locale.US, distance, smoothedRssi)
                                            lastUiUpdateTime = currentTime
                                            if (distance <= 1.1 && !isDialogShown) {
                                                showDialog = true
                                                isDialogShown = true
                                            }
                                        }
                                        Log.d("BeaconSearch", "Scan processing time: ${System.currentTimeMillis() - startTime} ms")
                                    }
                                }
                            }

                            override fun onScanFailed(errorCode: Int) {
                                Log.e("BeaconSearch", "Scan failed with error: $errorCode")
                                val currentTime = System.currentTimeMillis()
                                if (currentTime - lastUiUpdateTime >= 500) {
                                    distanceText = "${if (lastDistance != null) "Jarak ke Kiwi: %.2f meter".format(Locale.US, lastDistance) else "Beacon Kiwi tidak ditemukan."}\nRSSI: N/A"
                                    lastUiUpdateTime = currentTime
                                    if (currentTime - lastScanTime > 5000) {
                                        showNoBeaconMessage = true
                                    }
                                }
                            }
                        }

                        scanCallback?.let {
                            scanner.startScan(filters, settings, it)
                        }
                    }
                }

                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = { Text("Selamat Datang!") },
                        text = { Text("Anda sudah sampai di booth Zespri!") },
                        confirmButton = {
                            TextButton(onClick = { showDialog = false }) {
                                Text("OK")
                            }
                        }
                    )
                }

                LaunchedEffect(showNoBeaconMessage) {
                    if (showNoBeaconMessage) {
                        snackbarHostState.showSnackbar(
                            message = "No Zespri beacons detected. Ensure a beacon is nearby and Bluetooth is active.",
                            actionLabel = "OK",
                            duration = SnackbarDuration.Long
                        )
                        showNoBeaconMessage = false
                    }
                }
            }
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
                .size(500.dp)
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

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            permissionRequestBluetoothScan -> {
                var allGranted = true
                for (grantResult in grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        allGranted = false
                        break
                    }
                }
                if (allGranted) {
                    Log.d("BeaconSearch", "All permissions granted, starting scan")
                    startScanning()
                } else {
                    Log.e("BeaconSearch", "Permissions denied")
                }
            }
        }
    }

    private fun checkPermissions(): Boolean {
        val fineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        Log.d("BeaconSearch", "Fine Location Permission: $fineLocationPermission")
        var allPermissionsGranted = fineLocationPermission == PackageManager.PERMISSION_GRANTED

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val bluetoothScanPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
            val bluetoothConnectPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
            Log.d("BeaconSearch", "Bluetooth Scan: $bluetoothScanPermission, Connect: $bluetoothConnectPermission")
            allPermissionsGranted = allPermissionsGranted &&
                    bluetoothScanPermission == PackageManager.PERMISSION_GRANTED &&
                    bluetoothConnectPermission == PackageManager.PERMISSION_GRANTED
        }
        return allPermissionsGranted
    }

    private fun requestPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }
        if (permissionsToRequest.isNotEmpty()) {
            Log.d("BeaconSearch", "Requesting permissions: $permissionsToRequest")
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                permissionRequestBluetoothScan
            )
        } else {
            Log.d("BeaconSearch", "No permissions to request")
        }
    }

    private fun startScanning() {
        isDialogShown = false
        rssiHistory.clear()
        lastScanTime = 0
        isScanning = true
        Log.d("BeaconSearch", "Starting scan")
    }

    private fun processScanResult(
        result: ScanResult,
        onBeaconFound: (String, String, Int, Int?) -> Unit
    ) {
        val scanRecord = result.scanRecord ?: return
        val bytes = scanRecord.serviceData?.get(ParcelUuid.fromString("0000FEAA-0000-1000-8000-00805F9B34FB")) ?: run {
            Log.d("BeaconSearch", "No service data found for Eddystone UID")
            return
        }
        if (bytes.size >= 16) {
            if (bytes[0].toInt() == 0x00) { // Check if it's UID frame
                val namespaceBytes = bytes.copyOfRange(2, 12)
                val instanceBytes = bytes.copyOfRange(12, 18)
                val namespaceId = "0x${namespaceBytes.joinToString("") { "%02x".format(it) }}"
                val instanceId = "0x${instanceBytes.joinToString("") { "%02x".format(it) }}"
                val txPower = bytes[1].toInt()
                val rssi = result.rssi

                if (namespaceId == targetBeaconNamespaceId) {
                    onBeaconFound(namespaceId, instanceId, rssi, txPower)
                } else {
                    Log.d("BeaconSearch", "Found Eddystone UID but namespace does not match: $namespaceId")
                }
            }
        }
    }

    private fun stopScanning() {
        scanCallback?.let {
            scanner.stopScan(it)
            Log.d("BeaconSearch", "Stopped scanning with callback")
        } ?: Log.d("BeaconSearch", "No scan callback to stop")
        scanCallback = null
    }

    override fun onDestroy() {
        super.onDestroy()
        isScanning = false
        stopScanning()
    }
}
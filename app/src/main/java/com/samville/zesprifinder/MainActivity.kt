package com.samville.zesprifinder

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.samville.zesprifinder.ui.theme.screens.MainScreen
import com.samville.zesprifinder.ui.theme.viewmodel.MainScreenState
import com.samville.zesprifinder.ui.theme.screens.ZespriExperienceScreen
import com.samville.zesprifinder.ui.theme.ZespriFinderTheme
import com.samville.zesprifinder.ui.theme.screens.MainViewModel

class MainActivity : ComponentActivity() {

    private val permissionRequestBluetoothScan = 3
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ZespriFinderTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "main") {
                    composable("main") {
                        MainScreen(
                            state = MainScreenState.Initial,
                            onStartClick = {
                                if (checkPermissions()) {
                                    viewModel.initManager(applicationContext)
                                    viewModel.startScan()
                                    navController.navigate("zespri")
                                } else {
                                    requestPermissions()
                                }
                            }
                        )
                    }

                    composable("zespri") {
                        ZespriExperienceScreen(viewModel)
                    }
                }
            }
        }
    }

    private fun checkPermissions(): Boolean {
        val fineLocationPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        )
        var allGranted = fineLocationPermission == PackageManager.PERMISSION_GRANTED

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val scanPerm = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
            val connectPerm = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
            allGranted = allGranted &&
                    scanPerm == PackageManager.PERMISSION_GRANTED &&
                    connectPerm == PackageManager.PERMISSION_GRANTED
        }
        return allGranted
    }

    private fun requestPermissions() {
        val perms = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            perms.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                perms.add(Manifest.permission.BLUETOOTH_SCAN)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                perms.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }
        if (perms.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                perms.toTypedArray(),
                permissionRequestBluetoothScan
            )
        }
    }
}

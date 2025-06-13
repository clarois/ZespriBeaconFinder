package com.samville.zesprifinder.ui.theme.screens

import android.content.Context
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.samville.zesprifinder.ble.BeaconScannerManager
import com.samville.zesprifinder.model.StoreZone

class MainViewModel : ViewModel() {

    private val _distance = mutableStateOf(0.0)
    val distance: State<Double> get() = _distance

    private val _rssi = mutableStateOf(0)
    val rssi: State<Int> get() = _rssi

    private val _currentZone = mutableStateOf<StoreZone?>(null)
    val currentZone: State<StoreZone?> get() = _currentZone

    private var beaconScannerManager: BeaconScannerManager? = null

    fun initManager(context: Context) {
        if (beaconScannerManager == null) {
            beaconScannerManager = BeaconScannerManager(context) { distance, rssi, zone ->
                _distance.value = distance
                _rssi.value = rssi
                _currentZone.value = zone
            }
        }
    }

    fun startScan() {
        beaconScannerManager?.startScan()
    }

    fun stopScan() {
        beaconScannerManager?.stopScan()
    }
}

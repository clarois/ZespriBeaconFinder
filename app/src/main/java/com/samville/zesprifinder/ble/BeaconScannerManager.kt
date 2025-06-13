package com.samville.zesprifinder.ble

import android.content.Context
import android.os.ParcelUuid
import com.samville.zesprifinder.model.StoreZone
import no.nordicsemi.android.support.v18.scanner.*
import java.util.*
import kotlin.math.abs
import kotlin.math.pow

class BeaconScannerManager(
    private val context: Context,
    private val onBeaconFound: (Double, Int, StoreZone?) -> Unit
) {
    private val scanner = BluetoothLeScannerCompat.getScanner()
    private val rssiHistory = LinkedList<Int>()
    private val maxRssiHistory = 3
    private var scanCallback: ScanCallback? = null

    fun startScan() {
        val filters = listOf(
            ScanFilter.Builder()
                .setServiceUuid(ParcelUuid.fromString("0000FEAA-0000-1000-8000-00805F9B34FB"))
                .build()
        )
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                process(result)
            }
        }

        scanner.startScan(filters, settings, scanCallback!!)
    }

    fun stopScan() {
        scanCallback?.let { scanner.stopScan(it) }
        scanCallback = null
    }

    private fun process(result: ScanResult) {
        val scanRecord = result.scanRecord ?: return
        val bytes = scanRecord.serviceData?.get(
            ParcelUuid.fromString("0000FEAA-0000-1000-8000-00805F9B34FB")
        ) ?: return

        if (bytes.size >= 16 && bytes[0].toInt() == 0x00) { // Eddystone UID frame
            val namespaceBytes = bytes.slice(2..11)
            val instanceBytes = bytes.slice(12..17)
            val namespaceId = "0x" + namespaceBytes.joinToString("") { "%02x".format(it) }
            val instanceId = "0x" + instanceBytes.joinToString("") { "%02x".format(it) }

            if (namespaceId == "0x35e06fb12bc955aae08a") {
                val rssi = result.rssi
                if (rssiHistory.isNotEmpty()) {
                    val avg = rssiHistory.average()
                    if (abs(rssi - avg) > 10) return
                }

                rssiHistory.add(rssi)
                if (rssiHistory.size > maxRssiHistory) rssiHistory.removeFirst()

                val smoothedRssi = rssiHistory.average().toInt()
                val distance = 0.01 * 10.0.pow((smoothedRssi + 24) / -15.5)

                // 🧠 Map instanceId ke zona
                val zone = when (instanceId) {
                    "0xaaaa01" -> StoreZone.ZESPRI_CART
                    "0xaaaa02" -> StoreZone.DAIRY
                    "0xaaaa03" -> StoreZone.HOUSEHOLD
                    "0xaaaa05" -> StoreZone.ZESPRI_BOOTH
                    else -> null
                }

                if (zone != null && distance <= 1.1) {
                    onBeaconFound(distance.coerceAtLeast(0.0), smoothedRssi, zone)
                }
            }
        }
    }
}

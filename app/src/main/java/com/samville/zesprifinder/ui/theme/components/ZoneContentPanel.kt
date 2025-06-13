package com.samville.zesprifinder.ui.theme.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.samville.zesprifinder.model.StoreZone

@Composable
fun ZoneContentPanel(zone: StoreZone?) {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Zone: ${zone?.name ?: "Unknown"}", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(12.dp))
        when (zone) {
            StoreZone.ZESPRI_CART -> Text("Ambil keranjang dan mulai perjalananmu 🍃")
            StoreZone.DAIRY -> Text("Produk susu terbaik ada di sini 🥛")
            StoreZone.HOUSEHOLD -> Text("Promo peralatan rumah tangga 🎉")
            StoreZone.ZESPRI_BOOTH -> Text("Booth Zespri ditemukan! Kuis spesial menantimu 💚")
            else -> Text("Jelajahi toko dan temukan kejutan menarik ✨")
        }
    }
}

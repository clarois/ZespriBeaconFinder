package com.samville.zesprifinder.model


data class BeaconZone(
    val namespace: String,
    val instance: String,
    val zone: StoreZone
)

object SamvilleLayout {
    val beaconZones = listOf(
        BeaconZone("0x35e06fb12bc955aae08a", "0xaaaa01", StoreZone.ZESPRI_CART),
//        BeaconZone("0x35e06fb12bc955aae08a", "0xaaaa02", StoreZone.DAIRY),
//        BeaconZone("0x35e06fb12bc955aae08a", "0xaaaa03", StoreZone.SNACKS),
//        BeaconZone("0x35e06fb12bc955aae08a", "0xaaaa04", StoreZone.HOUSEHOLD),
//        BeaconZone("0x35e06fb12bc955aae08a", "0xaaaa05", StoreZone.ZESPRI_BOOTH)
    )
}

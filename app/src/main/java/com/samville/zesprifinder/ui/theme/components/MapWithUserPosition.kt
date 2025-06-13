package com.samville.zesprifinder.ui.theme.components


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.*
import com.samville.zesprifinder.R
import com.samville.zesprifinder.model.StoreZone

@Composable
fun MapWithUserPosition(zone: StoreZone?) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.map),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        zone?.let {
            val (x, y) = when (zone) {
                StoreZone.ZESPRI_CART -> 0.85f to 0.85f
                StoreZone.DAIRY -> 0.5f to 0.1f
                StoreZone.HOUSEHOLD -> 0.6f to 0.5f
                StoreZone.ZESPRI_BOOTH -> 0.1f to 0.1f
                else -> 0.5f to 0.5f
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = y * 400.dp, start = x * 400.dp)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color.Red)
            )
        }
    }
}

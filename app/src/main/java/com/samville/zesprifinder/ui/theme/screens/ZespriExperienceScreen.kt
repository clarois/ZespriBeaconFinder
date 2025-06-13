package com.samville.zesprifinder.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.samville.zesprifinder.model.StoreZone
import com.samville.zesprifinder.ui.theme.components.MapWithUserPosition
import com.samville.zesprifinder.ui.theme.components.ZoneContentPanel
import com.samville.zesprifinder.ui.theme.HayGrassGradient
import com.samville.zesprifinder.ui.theme.screens.MainViewModel

@Composable
fun ZespriExperienceScreen(viewModel: MainViewModel) {
    val currentZone by viewModel.currentZone

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(HayGrassGradient),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .aspectRatio(.8f)
                .padding(20.dp)
                .background(Color.White, shape = RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            MapWithUserPosition(currentZone)
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .aspectRatio(0.8f)
                .padding(20.dp)
                .background(Color.White, shape = RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.TopCenter
        ) {
            ZoneContentPanel(currentZone)
        }
    }
}

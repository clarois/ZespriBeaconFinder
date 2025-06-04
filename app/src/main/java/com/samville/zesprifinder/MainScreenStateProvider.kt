package com.samville.zesprifinder

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.samville.zesprifinder.MainActivity.MainScreenState

class MainScreenStateProvider : PreviewParameterProvider<MainScreenState> {
    override val values = sequenceOf(
        MainScreenState.Initial,
        MainScreenState.Scanning,
        MainScreenState.Dialog
    )
}
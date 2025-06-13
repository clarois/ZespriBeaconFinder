package com.samville.zesprifinder.ui.theme.viewmodel

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.samville.zesprifinder.ui.theme.viewmodel.MainScreenState

class MainScreenStateProvider : PreviewParameterProvider<MainScreenState> {
    override val values = sequenceOf(
        MainScreenState.Initial,
        MainScreenState.Scanning,
        MainScreenState.Dialog
    )
}

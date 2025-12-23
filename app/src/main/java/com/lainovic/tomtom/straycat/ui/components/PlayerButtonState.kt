package com.lainovic.tomtom.straycat.ui.components

internal sealed class PlayerButtonState {
    data object Start : PlayerButtonState()
    data object Stop : PlayerButtonState()
    data object Retry : PlayerButtonState()
    data object Pause : PlayerButtonState()
    data object Resume : PlayerButtonState()
}
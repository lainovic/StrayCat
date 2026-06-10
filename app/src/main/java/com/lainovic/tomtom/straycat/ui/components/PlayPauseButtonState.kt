package com.lainovic.tomtom.straycat.ui.components

sealed class PlayPauseButtonState {
    data object Play : PlayPauseButtonState()
    data object Pause : PlayPauseButtonState()
}

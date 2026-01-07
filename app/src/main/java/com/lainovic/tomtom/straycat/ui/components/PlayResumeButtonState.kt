package com.lainovic.tomtom.straycat.ui.components

sealed class PlayResumeButtonState {
    data object Play : PlayResumeButtonState()
    data object Pause : PlayResumeButtonState()
}
package com.lainovic.tomtom.straycat

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow

fun tickerFlow(periodMs: Long) = flow {
    var tick = 0L
    while (true) {
        emit(tick++)
        delay(periodMs)
    }
}
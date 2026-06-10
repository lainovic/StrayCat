package com.lainovic.tomtom.straycat.domain.location

import android.location.Location
import androidx.compose.runtime.Immutable
import kotlin.time.Duration

@Immutable
data class TrackPoint(
    val location: Location,
    val elapsedTravelTime: Duration?,
    val speed: Double?,
)

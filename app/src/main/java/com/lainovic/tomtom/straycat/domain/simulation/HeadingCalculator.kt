package com.lainovic.tomtom.straycat.domain.simulation

import android.location.Location
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

object HeadingCalculator {
    var prevLocation: Location? = null

    fun calculateHeadingFromPrevious(
        location: Location
    ): Float? {
        val from = prevLocation ?: return null.also {
            prevLocation = location
        }
        return calculateHeading(from, location)
            .also {
                prevLocation = location
            }
    }

    fun calculateHeading(
        from: Location,
        to: Location
    ): Float {
        val lat1 = Math.toRadians(from.latitude)
        val lon1 = Math.toRadians(from.longitude)
        val lat2 = Math.toRadians(to.latitude)
        val lon2 = Math.toRadians(to.longitude)

        val dLon = lon2 - lon1
        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) -
                sin(lat1) * cos(lat2) * cos(dLon)

        val heading = (Math.toDegrees(atan2(y, x)) + 360) % 360

        return heading.toFloat()
    }
}

fun Location.withBearing(): Location {
    val bearing =
        HeadingCalculator.calculateHeadingFromPrevious(this)
            ?: this.bearing
    return Location(this).apply {
        this.bearing = bearing
    }
}
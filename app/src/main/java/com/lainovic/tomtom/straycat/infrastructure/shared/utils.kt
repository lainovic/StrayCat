package com.lainovic.tomtom.straycat.infrastructure.shared

import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.location.LocationManager
import android.os.Build

fun Context.getLocationManager(): LocationManager =
    requireNotNull(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSystemService(LocationManager::class.java)
        } else {
            getSystemService(LOCATION_SERVICE) as LocationManager
        }
    )
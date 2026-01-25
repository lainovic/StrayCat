// infrastructure/location/MockLocationProvider.kt
package com.lainovic.tomtom.straycat.infrastructure.location

import android.annotation.SuppressLint
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.location.provider.ProviderProperties
import android.os.Build
import com.lainovic.tomtom.straycat.infrastructure.logging.AndroidLogger

class MockLocationProvider(private val locationManager: LocationManager) {
    fun setup() {
        try {
            AndroidLogger.d(TAG, "Setting up mock location provider")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                locationManager.addTestProvider(
                    LocationManager.GPS_PROVIDER,
                    ProviderProperties.Builder()
                        .setHasNetworkRequirement(false)
                        .setHasSatelliteRequirement(false)
                        .setHasCellRequirement(false)
                        .setHasMonetaryCost(false)
                        .setHasAltitudeSupport(true)
                        .setHasSpeedSupport(true)
                        .setHasBearingSupport(true)
                        .setPowerUsage(ProviderProperties.POWER_USAGE_LOW)
                        .setAccuracy(ProviderProperties.ACCURACY_FINE)
                        .build()
                )
            } else {
                @Suppress("DEPRECATION")
                @SuppressLint("WrongConstant")
                locationManager.addTestProvider(
                    LocationManager.GPS_PROVIDER,
                    false, false, false, false,
                    true, true, true,
                    Criteria.POWER_LOW,
                    Criteria.ACCURACY_FINE
                )
            }

            locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true)
            AndroidLogger.d(TAG, "Mock location provider enabled")
        } catch (e: SecurityException) {
            AndroidLogger.e(TAG, "Failed to enable mock location provider - missing permissions?", e)
            throw e
        } catch (e: IllegalArgumentException) {
            AndroidLogger.e(TAG, "Test provider already exists or invalid parameters", e)
            throw e
        }
    }

    fun setLocation(location: Location) {
        try {
            locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, location)
        } catch (e: SecurityException) {
            AndroidLogger.e(TAG, "Failed to set mock location", e)
            throw e
        }
    }

    fun cleanup() {
        try {
            AndroidLogger.d(TAG, "Cleaning up mock location provider")
            locationManager.removeTestProvider(LocationManager.GPS_PROVIDER)
            AndroidLogger.d(TAG, "Mock location provider removed")
        } catch (e: IllegalArgumentException) {
            AndroidLogger.w(TAG, "Test provider was not registered or already removed", e)
        } catch (e: Exception) {
            AndroidLogger.e(TAG, "Failed to remove mock location provider", e)
        }
    }

    companion object {
        private val TAG = MockLocationProvider::class.simpleName!!
    }
}

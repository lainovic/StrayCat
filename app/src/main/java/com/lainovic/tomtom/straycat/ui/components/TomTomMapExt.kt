package com.lainovic.tomtom.straycat.ui.components

import android.location.Location
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.fragment.app.FragmentManager
import com.lainovic.tomtom.straycat.R
import com.lainovic.tomtom.straycat.shared.toGeoPoint
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.map.display.MapOptions
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.common.WidthByZoom
import com.tomtom.sdk.map.display.image.ImageFactory
import com.tomtom.sdk.map.display.marker.Marker
import com.tomtom.sdk.map.display.marker.MarkerOptions
import com.tomtom.sdk.map.display.polyline.Polyline
import com.tomtom.sdk.map.display.polyline.PolylineOptions
import com.tomtom.sdk.map.display.ui.MapFragment
import kotlin.time.Duration.Companion.milliseconds

internal fun TomTomMap.updateMarker(
    existingMarker: Marker?,
    location: Location,
    resourceId: Int = R.drawable.cat_paw,
): Marker {
    existingMarker?.remove()

    return addMarker(
        MarkerOptions(
            coordinate = GeoPoint(
                latitude = location.latitude,
                longitude = location.longitude
            ),
            pinImage = ImageFactory.fromResource(resourceId)
        )
    )
}

internal fun TomTomMap.updatePolyline(
    existingPolyline: Polyline?,
    locations: List<Location>,
): Polyline {
    existingPolyline?.remove()
    return addPolyline(
        PolylineOptions(
            coordinates = locations.map(Location::toGeoPoint),
            lineColor = Color.Red.toArgb(),
            outlineColor = Color.Black.toArgb(),
        )
    )
}

internal fun TomTomMap.animateToLocation(
    location: Location,
    zoom: Double? = null,
    duration: Long = 1000,
) {
    animateCamera(
        CameraOptions(
            position = GeoPoint(
                latitude = location.latitude,
                longitude = location.longitude
            ),
            zoom = zoom,
        ),
        animationDuration = duration.milliseconds,
    )
}

internal fun TomTomMap.animateToBounds(
    locations: List<Location>,
    duration: Long = 1000,
) {
    if (locations.isEmpty()) return

    if (locations.size == 1) {
        animateToLocation(locations.first())
        return
    }

    val latitudes = locations.map { it.latitude }
    val longitudes = locations.map { it.longitude }

    val minLat = latitudes.min()
    val maxLat = latitudes.max()
    val minLon = longitudes.min()
    val maxLon = longitudes.max()

    val centerLat = (minLat + maxLat) / 2
    val centerLon = (minLon + maxLon) / 2

    val latDiff = maxLat - minLat
    val lonDiff = maxLon - minLon
    val maxDiff = maxOf(latDiff, lonDiff)

    val zoom = when {
        maxDiff > 10 -> 4.0
        maxDiff > 5 -> 6.0
        maxDiff > 1 -> 8.0
        maxDiff > 0.5 -> 10.0
        maxDiff > 0.1 -> 12.0
        else -> 14.0
    }

    animateCamera(
        CameraOptions(
            position = GeoPoint(latitude = centerLat, longitude = centerLon),
            zoom = zoom,
        ),
        animationDuration = duration.milliseconds,
    )
}

internal fun createTomTomMapFragment(
    fragmentManager: FragmentManager,
    mapOptions: MapOptions,
    onFragmentReady: (MapFragment) -> Unit
) {
    val fragment = fragmentManager.findFragmentById(R.id.map_container) as MapFragment?
    val newFragment = fragment ?: MapFragment.newInstance(mapOptions)
    fragmentManager.beginTransaction()
        .add(newFragment, "map_fragment")
        .commitNow()
    onFragmentReady(newFragment)
}
package com.lainovic.tomtom.straycat.ui.components

import android.content.Context
import android.location.Location
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.fragment.app.FragmentManager
import com.lainovic.tomtom.straycat.R
import com.lainovic.tomtom.straycat.domain.service.CustomLocationProvider
import com.lainovic.tomtom.straycat.shared.toGeoPoint
import com.lainovic.tomtom.straycat.shared.toLocation
import com.tomtom.sdk.map.display.location.LocationMarkerOptions
import com.tomtom.quantity.Distance
import com.tomtom.sdk.location.DefaultLocationProviderFactory
import com.tomtom.sdk.location.LocationProviderConfig
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.map.display.MapOptions
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.image.ImageFactory
import com.tomtom.sdk.map.display.marker.Marker
import com.tomtom.sdk.map.display.marker.MarkerOptions
import com.tomtom.sdk.map.display.polyline.Polyline
import com.tomtom.sdk.map.display.polyline.PolylineOptions
import com.tomtom.sdk.map.display.ui.MapFragment
import kotlin.collections.remove
import kotlin.time.Duration.Companion.milliseconds


internal fun TomTomMap.addMarker(
    location: Location,
    resourceId: Int = R.drawable.blue_paw,
) = addMarker(
    MarkerOptions(
        coordinate = GeoPoint(
            latitude = location.latitude,
            longitude = location.longitude
        ),
        pinImage = ImageFactory.fromResource(resourceId)
    )
)

internal fun TomTomMap.addPolyline(
    locations: List<Location>,
) = addPolyline(
    PolylineOptions(
        coordinates = locations.map(Location::toGeoPoint),
        lineColor = Color(0xFF0066FF).toArgb(),
        outlineColor = Color(0xFF00D9FF).toArgb(),
    )
)

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

    animateCamera(
        CameraOptions(
            position = GeoPoint(latitude = centerLat, longitude = centerLon),
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

internal fun TomTomMap.initialize(
    context: Context,
    locationProvider: LocationProvider,
    onMapLongPress: (Location) -> Unit,
) {
    val cameraOptions = CameraOptions(
        position = GeoPoint(latitude = 44.7866, longitude = 20.4489),
        zoom = 2.0,
    )

    animateCamera(cameraOptions, animationDuration = 1000.milliseconds)
    addMapLongClickListener { point: GeoPoint ->
        onMapLongPress(point.toLocation())
        true
    }

    setLocationProvider(locationProvider)
    locationProvider.enable()

    enableLocationMarker(
        LocationMarkerOptions(type = LocationMarkerOptions.Type.Chevron)
    )
}
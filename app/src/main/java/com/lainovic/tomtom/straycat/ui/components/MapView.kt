package com.lainovic.tomtom.straycat.ui.components

import android.location.Location
import android.view.View
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.lainovic.tomtom.straycat.BuildConfig
import com.lainovic.tomtom.straycat.R
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.map.display.MapOptions
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.map.display.annotation.AlphaInitialCameraOptionsApi
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.common.WidthByZoom
import com.tomtom.sdk.map.display.image.ImageFactory
import com.tomtom.sdk.map.display.marker.Marker
import com.tomtom.sdk.map.display.marker.MarkerOptions
import com.tomtom.sdk.map.display.polyline.Polyline
import com.tomtom.sdk.map.display.polyline.PolylineOptions
import com.tomtom.sdk.map.display.ui.MapFragment
import kotlin.time.Duration.Companion.milliseconds

@OptIn(
    AlphaInitialCameraOptionsApi::class,
)
@Composable
fun MapView(
    modifier: Modifier = Modifier,
    origin: Location? = null,
    destination: Location? = null,
    points: List<Location> = emptyList(),
    onMapLongPress: (Location) -> Unit = { _ -> },
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val fragmentManager = (context as FragmentActivity).supportFragmentManager

    var tomtomMap by remember { mutableStateOf<TomTomMap?>(null) }
    var originMarker by remember { mutableStateOf<Marker?>(null) }
    var destinationMarker by remember { mutableStateOf<Marker?>(null) }
    var routePolyline by remember { mutableStateOf<Polyline?>(null) }

    var mapFragment by remember { mutableStateOf<MapFragment?>(null) }
    var isMapFragmentReady by remember { mutableStateOf(false) }

    val mapOptions = remember {
        MapOptions(mapKey = BuildConfig.TOMTOM_API_KEY)
    }

    LaunchedEffect(Unit) {
        createTomTomMapFragment(fragmentManager, mapOptions) { fragment ->
            mapFragment = fragment
            isMapFragmentReady = true
        }
    }

    LaunchedEffect(tomtomMap, origin) {
        val map = tomtomMap ?: return@LaunchedEffect
        val origin = origin ?: return@LaunchedEffect

        originMarker = map.updateMarker(
            originMarker,
            origin,
            R.drawable.start_pin
        )

        destination?.let { dest ->
            map.animateToBounds(listOf(origin, dest))
        } ?: run {
            map.animateToLocation(origin)
        }
    }

    LaunchedEffect(tomtomMap, destination) {
        val map = tomtomMap ?: return@LaunchedEffect
        val destination = destination ?: return@LaunchedEffect

        destinationMarker = map.updateMarker(
            destinationMarker,
            destination,
            R.drawable.flag_pin
        )

        origin?.let { orig ->
            map.animateToBounds(listOf(orig, destination))
        } ?: run {
            map.animateToLocation(destination)
        }
    }

    LaunchedEffect(tomtomMap, points) {
        val map = tomtomMap ?: return@LaunchedEffect
        if (points.isEmpty()) {
            return@LaunchedEffect
        }

        val geoPoints = points.map { location ->
            GeoPoint(
                latitude = location.latitude,
                longitude = location.longitude
            )
        }

        routePolyline?.remove()
        routePolyline = map.addPolyline(
            PolylineOptions(
                coordinates = geoPoints,
                lineColor = 0xFF0000FF.toInt(),
                lineWidths = listOf(WidthByZoom(zoom = 10.0, width = 8.0)),
            )
        )
    }

    LaunchedEffect(mapFragment) {
        mapFragment?.getMapAsync {
            tomtomMap = it
        }
    }

    if (isMapFragmentReady) {
        AndroidView(
            modifier = modifier.fillMaxSize(),
            factory = { ctx ->
                mapFragment?.requireView() ?: View(ctx)
            }
        )
    }

    LaunchedEffect(tomtomMap) {
        tomtomMap?.let { map ->
            val cameraOptions = CameraOptions(
                position = GeoPoint(latitude = 44.7866, longitude = 20.4489),
                zoom = 2.0,
            )
            map.animateCamera(cameraOptions, animationDuration = 1000.milliseconds)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { source, event ->
            // Handle lifecycle events if needed
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

private fun createTomTomMapFragment(
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

private fun TomTomMap.updateMarker(
    existingMarker: Marker?,
    location: Location,
    resourceId: Int,
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

private fun TomTomMap.animateToLocation(
    location: Location,
    zoom: Double = 15.0,
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

private fun TomTomMap.animateToBounds(
    locations: List<Location>,
    padding: Int = 100,
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
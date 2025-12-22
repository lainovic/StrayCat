package com.lainovic.tomtom.straycat.ui.components

import android.location.Location
import android.util.Log
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.lainovic.tomtom.straycat.BuildConfig
import com.lainovic.tomtom.straycat.shared.toLocation
import com.tomtom.quantity.Distance
import com.tomtom.sdk.location.DefaultLocationProviderFactory
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.LocationProviderConfig
import com.tomtom.sdk.map.display.location.LocationMarkerOptions
import com.tomtom.sdk.map.display.MapOptions
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.map.display.annotation.AlphaInitialCameraOptionsApi
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.marker.Marker
import com.tomtom.sdk.map.display.polyline.Polyline
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

    val mapOptions = remember {
        MapOptions(mapKey = BuildConfig.TOMTOM_API_KEY)
    }

    val locationProviderConfig = LocationProviderConfig(
        minTimeInterval = 250L.milliseconds,
        minDistance = Distance.meters(5.0)
    )

    val defaultLocationProvider = DefaultLocationProviderFactory.create(
        context = context,
        config = locationProviderConfig
    )

    mapFragment?.let { fragment ->
        AndroidView(
            modifier = modifier.fillMaxSize(),
            factory = {
                fragment.requireView()
            }
        )
    }

    LaunchedEffect(Unit) {
        createTomTomMapFragment(fragmentManager, mapOptions) { fragment ->
            mapFragment = fragment
        }
    }

    LaunchedEffect(tomtomMap, origin) {
        val map = tomtomMap ?: return@LaunchedEffect
        val origin = origin ?: run {
            originMarker?.remove()
            originMarker = null
            return@LaunchedEffect
        }

        originMarker = map.updateMarker(
            originMarker,
            origin,
        )

        destination?.let { destination ->
            map.animateToBounds(listOf(origin, destination))
        } ?: run {
            map.animateToLocation(origin)
        }
    }

    LaunchedEffect(tomtomMap, destination) {
        val map = tomtomMap ?: return@LaunchedEffect
        val destination = destination ?: run {
            destinationMarker?.remove()
            destinationMarker = null
            return@LaunchedEffect
        }

        destinationMarker = map.updateMarker(
            destinationMarker,
            destination,
        )

        origin?.let { origin ->
            map.animateToBounds(listOf(origin, destination))
        } ?: run {
            map.animateToLocation(destination)
        }
    }

    LaunchedEffect(tomtomMap, points) {
        val map = tomtomMap ?: return@LaunchedEffect
        if (points.isEmpty()) {
            routePolyline?.remove()
            routePolyline = null
            return@LaunchedEffect
        }

        routePolyline = map.updatePolyline(
            routePolyline,
            points,
        )
    }

    LaunchedEffect(mapFragment) {
        mapFragment?.getMapAsync {
            tomtomMap = it
        }
    }

    LaunchedEffect(tomtomMap) {
        tomtomMap?.let { map ->
            val cameraOptions = CameraOptions(
                position = GeoPoint(latitude = 44.7866, longitude = 20.4489),
                zoom = 2.0,
            )
            map.animateCamera(cameraOptions, animationDuration = 1000.milliseconds)
            map.addMapLongClickListener { point ->
                onMapLongPress(point.toLocation())
                true
            }

            map.setLocationProvider(defaultLocationProvider)
            defaultLocationProvider.enable()
            map.enableLocationMarker(
                LocationMarkerOptions(type = LocationMarkerOptions.Type.Chevron)
            )
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            Log.d("MapView", "Lifecycle event: $event")
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    Log.d("MapView", "ON_RESUME")
                }

                Lifecycle.Event.ON_PAUSE -> {
                    Log.d("MapView", "ON_PAUSE")
                }

                Lifecycle.Event.ON_DESTROY -> {
                    Log.d("MapView", "ON_DESTROY")
                }

                else -> {
                    Log.d("MapView", "Other event: $event")
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            fragmentManager.beginTransaction()
                .remove(mapFragment!!)
                .commitNow()
        }
    }
}



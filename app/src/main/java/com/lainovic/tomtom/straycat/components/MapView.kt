package com.lainovic.tomtom.straycat.components

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
import com.tomtom.sdk.annotations.InternalTomTomSdkApi
import com.tomtom.sdk.map.display.MapOptions
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.map.display.annotation.AlphaInitialCameraOptionsApi
import com.tomtom.sdk.map.display.marker.domain.Marker
import com.tomtom.sdk.map.display.polyline.Polyline
import com.tomtom.sdk.map.display.ui.MapFragment
import com.tomtom.sdk.map.display.ui.MapView as TomTomMapView

@OptIn(
    AlphaInitialCameraOptionsApi::class,
    InternalTomTomSdkApi::class,
)
@Composable
fun MapView(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val fragmentManager = (context as FragmentActivity).supportFragmentManager
    val lifecycleOwner = LocalLifecycleOwner.current

    var tomtomMap by remember { mutableStateOf<TomTomMap?>(null) }
    var mapView by remember { mutableStateOf<TomTomMapView?>(null) }
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

    if (isMapFragmentReady) {
        AndroidView(
            modifier = modifier.fillMaxSize(),
            factory = { ctx ->
                mapFragment?.requireView() ?: View(ctx)
            }
        )
    }

    LaunchedEffect(mapView) {
        mapView?.getMapAsync { tomtomMap = it }
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
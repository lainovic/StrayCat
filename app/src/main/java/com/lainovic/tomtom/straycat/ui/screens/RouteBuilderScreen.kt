package com.lainovic.tomtom.straycat.ui.screens

import android.location.Location
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lainovic.tomtom.straycat.BuildConfig
import com.lainovic.tomtom.straycat.ui.components.LocationSearchBox
import com.lainovic.tomtom.straycat.ui.components.MapView
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.Place
import com.tomtom.sdk.routing.RoutePlanner
import com.tomtom.sdk.routing.RoutePlanningCallback
import com.tomtom.sdk.routing.RoutePlanningResponse
import com.tomtom.sdk.routing.RoutingFailure
import com.tomtom.sdk.routing.online.OnlineRoutePlanner
import com.tomtom.sdk.routing.options.Itinerary
import com.tomtom.sdk.routing.options.ItineraryPoint
import com.tomtom.sdk.routing.options.RoutePlanningOptions
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Composable
fun RouteBuilderScreen(
    modifier: Modifier = Modifier,
    onNavigateToSimulation: () -> Unit,
    backgroundScope: CoroutineScope = CoroutineScope(
        Dispatchers.IO +
                SupervisorJob() +
                CoroutineName("RouteBuilderScreenBackground")
    )
) {
    val context = LocalContext.current
    var origin by remember { mutableStateOf<Location?>(null) }
    var destination by remember { mutableStateOf<Location?>(null) }
    var points by remember { mutableStateOf<List<Location>>(emptyList()) }
    val routePlanner = remember {
        OnlineRoutePlanner.create(
            context = context,
            apiKey = BuildConfig.TOMTOM_API_KEY,
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

    }
    Column {
        LocationSearchBox(placeholderText = "Origin") { location, address ->
            origin = location
        }

        LocationSearchBox(placeholderText = "Destination") { location, address ->
            destination = location
        }

        MapView(
            origin = origin,
            destination = destination,
            points = points,
//            onMapLongPress = onMapLongPress,
        )

        Text(
            text = "Route Builder",
            style = MaterialTheme.typography.headlineMedium
        )

        Button(onClick = onNavigateToSimulation) {
            Text(
                text = "Start Simulation"
            )
        }

        LaunchedEffect(origin, destination) {
            val origin = origin ?: return@LaunchedEffect
            val destination = destination ?: return@LaunchedEffect
            backgroundScope.planRoute(
                origin,
                destination,
                routePlanner,
            ) {
                points = it
            }
        }
    }
}

private fun CoroutineScope.planRoute(
    origin: Location,
    destination: Location,
    routePlanner: RoutePlanner,
    onRouteCalculated: (List<Location>) -> Unit
) {
    launch {
        val polyline = routePlanner.planRoute(origin, destination)
        onRouteCalculated(polyline)
    }
}

private suspend fun RoutePlanner.planRoute(
    origin: Location,
    destination: Location,
): List<Location> = suspendCancellableCoroutine { cont ->
    val job = planRoute(
        routePlanningOptions = RoutePlanningOptions(
            itinerary = Itinerary(
                origin = ItineraryPoint(
                    Place(
                        coordinate = GeoPoint(
                            latitude = origin.latitude,
                            longitude = origin.longitude,
                        )
                    ),
                ),
                destination = ItineraryPoint(
                    Place(
                        coordinate = GeoPoint(
                            latitude = destination.latitude,
                            longitude = destination.longitude,
                        )
                    ),
                )
            )
        ),
        callback = object : RoutePlanningCallback {
            override fun onFailure(failure: RoutingFailure) {
                cont.resumeWithException(
                    Exception("Route planning failed: ${failure.message}")
                )
            }

            override fun onSuccess(result: RoutePlanningResponse) {
                cont.resume(result.routes.first().routePoints.map {
                    Location("gsp").apply {
                        latitude = it.coordinate.latitude
                        longitude = it.coordinate.longitude
                    }
                })
            }
        },
    )

    cont.invokeOnCancellation { job.cancel() }
}
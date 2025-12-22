package com.lainovic.tomtom.straycat.infrastructure.shared

import android.location.Location
import com.lainovic.tomtom.straycat.shared.toGeoPoint
import com.lainovic.tomtom.straycat.shared.toLocation
import com.tomtom.sdk.location.Place
import com.tomtom.sdk.routing.RoutePlanner
import com.tomtom.sdk.routing.RoutePlanningCallback
import com.tomtom.sdk.routing.RoutePlanningResponse
import com.tomtom.sdk.routing.RoutingFailure
import com.tomtom.sdk.routing.options.Itinerary
import com.tomtom.sdk.routing.options.ItineraryPoint
import com.tomtom.sdk.routing.options.RoutePlanningOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun RoutePlanner.planRoute(
    origin: Location,
    destination: Location,
): List<Location> = suspendCancellableCoroutine { cont ->
    val job = planRoute(
        routePlanningOptions = RoutePlanningOptions(
            itinerary = Itinerary(
                origin = ItineraryPoint(
                    Place(
                        coordinate = origin.toGeoPoint()
                    ),
                ),
                destination = ItineraryPoint(
                    Place(
                        coordinate = destination.toGeoPoint()
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
                cont.resume(
                    result.routes.first().routePoints.map {
                        it.toLocation()
                    })
            }
        },
    )

    cont.invokeOnCancellation { job.cancel() }
}
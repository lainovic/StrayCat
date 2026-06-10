package com.lainovic.tomtom.straycat.infrastructure.shared

import android.location.Location
import com.lainovic.tomtom.straycat.domain.location.TrackPoint
import com.lainovic.tomtom.straycat.shared.calculateSpeedBetweenPoints
import com.lainovic.tomtom.straycat.shared.toGeoPoint
import com.lainovic.tomtom.straycat.shared.toTrackPoint
import com.tomtom.sdk.location.Place
import com.tomtom.sdk.routing.RoutePlanner
import com.tomtom.sdk.routing.RoutePlanningCallback
import com.tomtom.sdk.routing.RoutePlanningResponse
import com.tomtom.sdk.routing.RoutingFailure
import com.tomtom.sdk.routing.options.Itinerary
import com.tomtom.sdk.routing.options.ItineraryPoint
import com.tomtom.sdk.routing.options.RoutePlanningOptions
import com.tomtom.sdk.routing.route.Route
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun RoutePlanner.planRoute(
    origin: Location,
    destination: Location,
): List<TrackPoint> = suspendCancellableCoroutine { cont ->
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
                val route = result.routes.first()
                cont.resume(route.toTrackPoints())
            }
        },
    )

    cont.invokeOnCancellation { job.cancel() }
}

private fun Route.toTrackPoints(): List<TrackPoint> {
    val speeds = calculateSegmentSpeeds()
    val baseOffset = routePoints.first().travelTime

    return routePoints.mapIndexed { index, point ->
        point.toTrackPoint(
            elapsedTravelTime = point.travelTime - baseOffset,
            speed = speeds[index],
        )
    }
}

private fun Route.calculateSegmentSpeeds(): List<Double?> {
    val segmentSpeeds = routePoints.zipWithNext { prev, curr ->
        calculateSpeedBetweenPoints(
            startOffset = prev.routeOffset,
            endOffset = curr.routeOffset,
        )
    }
    return listOf(null) + segmentSpeeds
}
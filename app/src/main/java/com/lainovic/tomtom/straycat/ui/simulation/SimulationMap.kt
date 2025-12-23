package com.lainovic.tomtom.straycat.ui.simulation

import android.location.Location
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.lainovic.tomtom.straycat.ui.components.LocationSearchBox
import com.lainovic.tomtom.straycat.ui.components.MapView
import com.lainovic.tomtom.straycat.ui.generateCatSound
import com.tomtom.sdk.location.LocationProvider

@Composable
internal fun SimulationMap(
    origin: Location?,
    destination: Location?,
    locations: List<Location>,
    locationProvider: LocationProvider,
    onOriginSelected: (Location, String) -> Unit,
    onDestinationSelected: (Location, String) -> Unit,
    onMapLongPress: (Location) -> Unit
) {
    Column {
        Text(
            modifier = Modifier.padding(8.dp),
            text = "${generateCatSound()} \uD83D\uDC08",
            style = MaterialTheme.typography.headlineMedium
        )
        LocationSearchBox(placeholderText = "Origin", onLocationSelected = onOriginSelected)
        LocationSearchBox(placeholderText = "Destination", onLocationSelected = onDestinationSelected)
        MapView(
            origin = origin,
            destination = destination,
            points = locations,
            locationProvider = locationProvider,
            onMapLongPress = onMapLongPress,
            modifier = Modifier
                .padding(8.dp)
                .clip(RoundedCornerShape(12.dp))
        )
    }
}
package com.lainovic.tomtom.straycat.ui.route_builder

import android.location.Location
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.lainovic.tomtom.straycat.ui.components.LocationSearchBox
import com.lainovic.tomtom.straycat.ui.components.MapView

@Composable
internal fun RouteBuilderContent(
    modifier: Modifier,
    origin: Location?,
    destination: Location?,
    points: List<Location>,
    onOriginSelected: (Location, String) -> Unit,
    onDestinationSelected: (Location, String) -> Unit,
    onMapLongPress: (Location) -> Unit,
    onNavigateToSimulation: () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column {
            Text(
                modifier = Modifier
                    .padding(8.dp),
                text = "Stray Cat \uD83D\uDC08",
                style = MaterialTheme.typography.headlineMedium
            )
            LocationSearchBox(placeholderText = "Origin", onLocationSelected = onOriginSelected)
            LocationSearchBox(placeholderText = "Destination", onLocationSelected = onDestinationSelected)
            MapView(
                origin = origin,
                destination = destination,
                points = points,
                onMapLongPress = onMapLongPress,
                modifier = Modifier
                    .padding(8.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        }

        Button(
            onClick = onNavigateToSimulation,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 42.dp, end = 16.dp)
        ) {
            Text("Player")
        }
    }
}
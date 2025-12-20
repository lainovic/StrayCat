package com.lainovic.tomtom.straycat.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.lainovic.tomtom.straycat.components.MapView

@Composable
fun RouteBuilderScreen(
    modifier: Modifier = Modifier,
    onNavigateToSimulation: () -> Unit,
) {
    var origin by remember { mutableStateOf<LatLng?>(null) }
    var destination by remember { mutableStateOf<LatLng?>(null) }
    var routePolyline by remember { mutableStateOf<List<LatLng>>(emptyList()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
//        LocationSearchBox(
//            label = "Origin",
//            onLocationSelected = { /* TODO */ }
//        )
//
//        LocationSearchBox(
//            label = "Destination",
//            onLocationSelected = { /* TODO */ }
//        )

        MapView(
//            origin = origin,
//            destination = destination,
//            routePolyline = null,
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
    }
}
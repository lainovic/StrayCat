package com.lainovic.tomtom.straycat.ui.simulation

import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lainovic.tomtom.straycat.ui.components.SearchField
import com.lainovic.tomtom.straycat.ui.components.TomTomMap
import com.lainovic.tomtom.straycat.ui.showToast
import com.tomtom.sdk.location.LocationProvider

@Composable
internal fun SimulationInput(
    origin: Location?,
    destination: Location?,
    locations: List<Location>,
    locationProvider: LocationProvider,
    onOriginSelected: (Location, String) -> Unit,
    onDestinationSelected: (Location, String) -> Unit,
    onMapLongPress: (Location) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isSearchOpen by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        TomTomMap(
            origin = origin,
            destination = destination,
            locations = locations,
            locationProvider = locationProvider,
            onMapLongPress = onMapLongPress,
            modifier = Modifier
                .padding(8.dp)
                .clip(RoundedCornerShape(12.dp))
        )


        if (isSearchOpen) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp,
                tonalElevation = 2.dp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(start = 72.dp, top = 16.dp, end = 16.dp)
                    .fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    SearchField(placeholderText = "Origin", onLocationSelected = onOriginSelected)
                    Spacer(modifier = Modifier.height(8.dp))
                    SearchField(placeholderText = "Destination", onLocationSelected = onDestinationSelected)
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            IconButton(
                onClick = { isSearchOpen = !isSearchOpen },
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Icon(
                    imageVector =
                        if (isSearchOpen)
                            Icons.Outlined.Cancel
                        else
                            Icons.Filled.Search,
                    contentDescription = "Open Search Input",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(32.dp)
                )
            }
            IconButton(
                onClick = { context.showToast("Hello!") },
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Open Search Input",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(32.dp)
                )
            }

        }
    }
}
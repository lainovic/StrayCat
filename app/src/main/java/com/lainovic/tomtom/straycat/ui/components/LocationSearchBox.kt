package com.lainovic.tomtom.straycat.ui.components

import android.location.Location
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.tomtom.sdk.common.android.greenFloat

@Composable
fun LocationSearchBox(
    modifier: Modifier = Modifier,
    placeholderText: String = "Search Location",
    onLocationSelected: (Location, String) -> Unit = { _, _ -> },
) {
    val context = LocalContext.current
    val placesClient = remember { Places.createClient(context) }

    var searchQuery by remember { mutableStateOf("") }
    var predictions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { query ->
                searchQuery = query
                placesClient.findAutocompletePredictions(searchQuery) {
                    predictions = it
                    Log.d("LocationSearchBox", "Predictions: $it")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(text = placeholderText) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear Search")
                    }
                }
            }
        )

        LazyColumn {
            items(predictions) { prediction ->
                Text(
                    text = prediction.getFullText(null).toString(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val placeFields = listOf(
                                Place.Field.LOCATION,
                                Place.Field.DISPLAY_NAME,
                            )
                            val request = FetchPlaceRequest
                                .builder(prediction.placeId, placeFields)
                                .build()

                            placesClient.fetchPlace(request)
                                .addOnSuccessListener { res ->
                                    val place = res.place
                                    val location = place.location
                                    if (location != null) {
                                        val location = Location("").apply {
                                            latitude = location.latitude
                                            longitude = location.longitude
                                        }
                                        onLocationSelected(location, place.displayName ?: "")
                                        predictions = emptyList()
                                        searchQuery = place.displayName ?: ""
                                    }
                                }
                        }
                )
            }
        }
    }
}

private fun PlacesClient.findAutocompletePredictions(
    query: String,
    onPredictionsFound: (List<AutocompletePrediction>) -> Unit
) {
    val request = FindAutocompletePredictionsRequest.builder()
        .setQuery(query)
        .build()

    findAutocompletePredictions(request)
        .addOnSuccessListener {
            onPredictionsFound(it.autocompletePredictions)
        }
}


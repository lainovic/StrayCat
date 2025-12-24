package com.lainovic.tomtom.straycat.ui.components

import android.location.Location
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@Composable
fun LocationSearchBox(
    modifier: Modifier = Modifier,
    placeholderText: String = "Search Location",
    onLocationSelected: (Location, String) -> Unit = { _, _ -> },
) {
    val context = LocalContext.current
    val placesClient = remember { Places.createClient(context) }

    val searchQuery = remember { MutableStateFlow("") }
    var searchQueryText by remember { mutableStateOf("") }
    var predictions
            by remember {
                mutableStateOf<List<AutocompletePrediction>>(emptyList())
            }

    Column(
        modifier = modifier
            .padding(8.dp)
    ) {
        OutlinedTextField(
            value = searchQueryText,
            onValueChange = { query ->
                searchQueryText = query
                searchQuery.value = query
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(text = placeholderText) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            trailingIcon = {
                if (searchQueryText.isNotEmpty()) {
                    IconButton(onClick = {
                        searchQueryText = ""
                        searchQuery.value = ""
                    }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Clear Search"
                        )
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
                                    val name = place.displayName ?: ""
                                    if (location != null) {
                                        val location = Location("").apply {
                                            latitude = location.latitude
                                            longitude = location.longitude
                                        }
                                        onLocationSelected(location, name)
                                        predictions = emptyList()
                                        searchQueryText = name
                                        searchQuery.value = name
                                    }
                                }
                        }
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        searchQuery
            .debounce(300.milliseconds)
            .filter { it.length >= 3 }
            .distinctUntilChanged()
            .flatMapLatest { query ->
                flow {
                    emit(placesClient.searchPredictions(query))
                }
                    .catch { emit(emptyList()) }
            }
            .collect { results ->
                predictions = results
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

private suspend fun PlacesClient.searchPredictions(
    query: String
): List<AutocompletePrediction> {
    return suspendCancellableCoroutine { continuation ->
        findAutocompletePredictions(query) { predictions ->
            continuation.resume(predictions) {}
        }
    }
}

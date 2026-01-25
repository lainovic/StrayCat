package com.lainovic.tomtom.straycat.ui.simulation

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SearchViewModel(
    private val placesClient: PlacesClient
) : ViewModel() {

    private val _originQuery = MutableStateFlow("")
    val originQuery: StateFlow<String> = _originQuery

    private val _destinationQuery = MutableStateFlow("")
    val destinationQuery: StateFlow<String> = _destinationQuery

    private val _originPredictions = MutableStateFlow<List<AutocompletePrediction>>(emptyList())
    val originPredictions: StateFlow<List<AutocompletePrediction>> = _originPredictions

    private val _destinationPredictions = MutableStateFlow<List<AutocompletePrediction>>(emptyList())
    val destinationPredictions: StateFlow<List<AutocompletePrediction>> = _destinationPredictions

    private val isSelectingOrigin = MutableStateFlow(false)
    private val isSelectingDestination = MutableStateFlow(false)

    init {
        setupSearchFlow(_originQuery, _originPredictions, isSelectingOrigin)
        setupSearchFlow(_destinationQuery, _destinationPredictions, isSelectingDestination)
    }

    private fun setupSearchFlow(
        queryFlow: MutableStateFlow<String>,
        predictionsFlow: MutableStateFlow<List<AutocompletePrediction>>,
        isSelectingFlow: MutableStateFlow<Boolean>
    ) {
        viewModelScope.launch {
            queryFlow
                .debounce(300.milliseconds)
                .filter { it.length >= 3 }
                .filter { !isSelectingFlow.value }
                .distinctUntilChanged()
                .flatMapLatest { query ->
                    flow {
                        emit(searchPredictions(query))
                    }.catch { emit(emptyList()) }
                }
                .collect { results ->
                    if (!isSelectingFlow.value) {
                        predictionsFlow.value = results
                    }
                }
        }
    }

    fun onOriginQueryChanged(query: String) {
        _originQuery.value = query
    }

    fun onDestinationQueryChanged(query: String) {
        _destinationQuery.value = query
    }

    fun selectOrigin(prediction: AutocompletePrediction, onLocationSelected: (Location, String) -> Unit) {
        isSelectingOrigin.value = true
        _originPredictions.value = emptyList()
        fetchPlace(prediction) { location, name ->
            _originQuery.value = name
            onLocationSelected(location, name)
            isSelectingOrigin.value = false
        }
    }

    fun selectDestination(prediction: AutocompletePrediction, onLocationSelected: (Location, String) -> Unit) {
        isSelectingDestination.value = true
        _destinationPredictions.value = emptyList()
        fetchPlace(prediction) { location, name ->
            _destinationQuery.value = name
            onLocationSelected(location, name)
            isSelectingDestination.value = false
        }
    }

    private fun fetchPlace(
        prediction: AutocompletePrediction,
        onResult: (Location, String) -> Unit
    ) {
        val placeFields = listOf(Place.Field.LOCATION, Place.Field.DISPLAY_NAME)
        val request = FetchPlaceRequest.builder(prediction.placeId, placeFields).build()

        placesClient.fetchPlace(request).addOnSuccessListener { res ->
            val place = res.place
            val location = place.location
            val name = place.displayName ?: ""
            if (location != null) {
                val androidLocation = Location("").apply {
                    latitude = location.latitude
                    longitude = location.longitude
                }
                onResult(androidLocation, name)
            }
        }
    }

    private suspend fun searchPredictions(query: String): List<AutocompletePrediction> {
        return suspendCancellableCoroutine { continuation ->
            val request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .build()

            placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener {
                    continuation.resumeWith(Result.success(it.autocompletePredictions))
                }
                .addOnFailureListener {
                    continuation.resumeWith(Result.success(emptyList()))
                }
        }
    }

    class Factory(private val placesClient: PlacesClient) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SearchViewModel(placesClient) as T
        }
    }
}

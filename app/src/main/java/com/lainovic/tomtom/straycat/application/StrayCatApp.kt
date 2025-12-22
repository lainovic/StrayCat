package com.lainovic.tomtom.straycat.application

import android.content.Context
import android.location.Location
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.libraries.places.api.Places
import com.lainovic.tomtom.straycat.BuildConfig
import com.lainovic.tomtom.straycat.ui.route_builder.RouteBuilderScreen
import com.lainovic.tomtom.straycat.ui.route_player.RoutePlayerScreen

@Composable
fun StrayCatApp(context: Context) {
    val navController = rememberNavController()
    val locations = remember { mutableListOf<Location>() }

    if (!Places.isInitialized()) {
        Places.initializeWithNewPlacesApiEnabled(
            context,
            BuildConfig.GOOGLE_PLACES_API_KEY,
        )
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "route_builder",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("route_builder") {
                RouteBuilderScreen(
                    context = context,
                    onNavigateToPlayer = {
                        navController.navigate("route_player")
                    },
                    onLocationsUpdated = { updatedLocations ->
                        locations.clear()
                        locations.addAll(updatedLocations)
                    }
                )
            }

            composable("route_player") {
                RoutePlayerScreen(
                    context = context,
                    locations = locations,
                    onNavigateToBuilder = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
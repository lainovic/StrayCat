package com.lainovic.tomtom.straycat.application

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lainovic.tomtom.straycat.ui.screens.RouteBuilderScreen
import com.lainovic.tomtom.straycat.ui.screens.SimulationScreen

@Composable
fun StrayCatApp() {
    val navController = rememberNavController()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "route_builder",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("route_builder") {
                RouteBuilderScreen(
                    onNavigateToSimulation = {
                        navController.navigate("simulation")
                    }
                )
            }

            composable("simulation") {
                SimulationScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
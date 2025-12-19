package com.lainovic.tomtom.straycat

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lainovic.tomtom.straycat.screens.RouteBuilderScreen
import com.lainovic.tomtom.straycat.screens.SimulationScreen
import com.lainovic.tomtom.straycat.ui.theme.StrayCatTheme

class MainActivity : ComponentActivity() {
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { handlePermissions(it) }

    private val viewModel: SimulationViewModel by viewModels {
        val service = LocationServiceFacade(
            application,
            MockLocationService::class.java
        )
        SimulationViewModelFactory(service)
    }

    private fun handlePermissions(permissions: Map<String, Boolean>) {
        val allGranted = permissions.all { it.value }
        if (!allGranted) {
            Toast.makeText(
                this,
                "Location permissions are required for Stray Cat to function properly.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            StrayCatTheme {
                StrayCatApp()
            }
        }

        requestLocationPermissions()
    }

    private fun requestLocationPermissions() {
        locationPermissionLauncher.launch(
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
            )
        )
    }

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
//            val state by viewModel.state.collectAsState()
//            val startStopText by viewModel.startStopButtonText.collectAsState()
//            val pauseResumeText by viewModel.pauseResumeButtonText.collectAsState()
//
//
//            MainScreen(
//                modifier = Modifier.padding(innerPadding),
//                state = state,
//                startStopButtonText = startStopText,
//                pauseResumeButtonText = pauseResumeText,
//                onStartStopClick = { viewModel.startStop() },
//                onPauseResumeClick = { viewModel.pauseResume() },
//            )
        }
    }

    @Composable
    fun MainScreen(
        modifier: Modifier = Modifier,
        state: LocationServiceState = LocationServiceState.Idle,
        startStopButtonText: String = "Start",
        pauseResumeButtonText: String = "Pause/Resume",
        onStartStopClick: () -> Unit = {},
        onPauseResumeClick: () -> Unit = {}
    ) {
        val context = LocalContext.current

        // Show toast when error occurs
        LaunchedEffect(state) {
            if (state is LocationServiceState.Error) {
                Toast.makeText(
                    context,
                    "Error: ${state.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.BottomEnd,
        ) {
            Button(onClick = onStartStopClick) {
                Text(startStopButtonText)
            }
        }

        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.BottomStart,
        ) {
            Button(onClick = onPauseResumeClick) {
                Text(pauseResumeButtonText)
            }
        }
    }


    @Preview(showBackground = true)
    @Composable
    fun MainScreenPreview() {
        StrayCatTheme {
            MainScreen()
        }
    }
}
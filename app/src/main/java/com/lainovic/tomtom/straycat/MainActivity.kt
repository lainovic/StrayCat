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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lainovic.tomtom.straycat.ui.theme.StrayCatTheme

class MainActivity : ComponentActivity() {
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { handlePermissions(it) }

    private val viewModel: SimulationViewModel by viewModels {
        SimulationViewModelFactory(application)
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
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(modifier = Modifier.padding(innerPadding))
                }
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
    fun MainScreen(modifier: Modifier = Modifier) {
        val simulationState by viewModel.state.collectAsState()

        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.BottomEnd,
        ) {
            Button(
                onClick = { onStartStopClick() }
            ) {
                Text(onStartStopClickText(simulationState))
            }
        }

        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.BottomStart,
        ) {
            Button(
                onClick = { onPauseResumeClick() }
            ) {
                Text(onPauseResumeClickText(simulationState))
            }
        }
    }

    private fun onStartStopClick() {
        viewModel.startStopSimulation()
    }

    private fun onStartStopClickText(state: SimulationState): String {
        return when (state) {
            SimulationState.Idle, SimulationState.Stopped -> "Start"
            SimulationState.Running, SimulationState.Paused -> "Stop"
        }
    }

    private fun onPauseResumeClick() {
        viewModel.pauseResumeSimulation()
    }

    private fun onPauseResumeClickText(state: SimulationState): String {
        return when (state) {
            SimulationState.Running -> "Pause"
            SimulationState.Paused -> "Resume"
            else -> "Pause/Resume"
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
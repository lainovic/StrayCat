package com.lainovic.tomtom.straycat

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startForegroundService
import androidx.lifecycle.lifecycleScope
import com.lainovic.tomtom.straycat.ui.theme.StrayCatTheme
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MainActivity : ComponentActivity() {

    val controller = SimulationController()

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { handlePermissions(it) }

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

        observeSimulationState()

        setContent {
            StrayCatTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }

        requestLocationPermissions()
    }

    private fun observeSimulationState() {
        controller.state
            .onEach {
                when (it) {
                    is SimulationState.Idle -> Toast.makeText(
                        this,
                        "Stray Cat simulation idle.",
                        Toast.LENGTH_SHORT
                    ).show()
                    is SimulationState.Started -> startSimulation()
                    is SimulationState.Paused -> pauseSimulation()
                    is SimulationState.Resumed -> resumeSimulation()
                    is SimulationState.Stopped -> stopSimulation()
                }
            }
            .launchIn(lifecycleScope)
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
        var isSimulating by remember { mutableStateOf(false) }

        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.BottomEnd,
        ) {
            Button(
                onClick = {
                    if (isSimulating) {
                        controller.stop()
                        isSimulating = false
                    } else {
                        controller.start()
                        isSimulating = true
                    }
                }
            ) {
                Text(if (isSimulating) "Stop Simulation" else "Start Simulation")
            }

        }
    }

    fun startSimulation() {
        val intent = Intent(this, LocationSimulationService::class.java)
        intent.action = LocationSimulationService.ACTION_START
        startForegroundService(this, intent)
    }

    fun stopSimulation() {
        val intent = Intent(this, LocationSimulationService::class.java)
        intent.action = LocationSimulationService.ACTION_STOP
        startForegroundService(this, intent)
    }

    fun resumeSimulation() {
        val intent = Intent(this, LocationSimulationService::class.java)
        intent.action = LocationSimulationService.ACTION_RESUME
        startForegroundService(this, intent)
    }

    fun pauseSimulation() {
        val intent = Intent(this, LocationSimulationService::class.java)
        intent.action = LocationSimulationService.ACTION_PAUSE
        startForegroundService(this, intent)
    }

    @Preview(showBackground = true)
    @Composable
    fun MainScreenPreview() {
        StrayCatTheme {
            MainScreen()
        }
    }
}
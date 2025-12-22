package com.lainovic.tomtom.straycat

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import com.lainovic.tomtom.straycat.application.StrayCatApp
import com.lainovic.tomtom.straycat.ui.theme.StrayCatTheme

class MainActivity : FragmentActivity() {
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { handlePermissions(it) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            StrayCatTheme {
                StrayCatApp(applicationContext)
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


//    @Composable
//    fun MainScreen(
//        modifier: Modifier = Modifier,
//        state: LocationServiceState = LocationServiceState.Idle,
//        startStopButtonText: String = "Start",
//        pauseResumeButtonText: String = "Pause/Resume",
//        onStartStopClick: () -> Unit = {},
//        onPauseResumeClick: () -> Unit = {}
//    ) {
//        val context = LocalContext.current
//
//        // Show toast when error occurs
//        LaunchedEffect(state) {
//            if (state is LocationServiceState.Error) {
//                Toast.makeText(
//                    context,
//                    "Error: ${state.message}",
//                    Toast.LENGTH_LONG
//                ).show()
//            }
//        }
//
//        Box(
//            modifier = modifier
//                .fillMaxSize()
//                .padding(24.dp),
//            contentAlignment = Alignment.BottomEnd,
//        ) {
//            Button(onClick = onStartStopClick) {
//                Text(startStopButtonText)
//            }
//        }
//
//        Box(
//            modifier = modifier
//                .fillMaxSize()
//                .padding(24.dp),
//            contentAlignment = Alignment.BottomStart,
//        ) {
//            Button(onClick = onPauseResumeClick) {
//                Text(pauseResumeButtonText)
//            }
//        }
//    }
}
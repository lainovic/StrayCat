package com.lainovic.tomtom.straycat

import android.app.AlertDialog
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

        if (BuildConfig.TOMTOM_API_KEY.isBlank() || BuildConfig.GOOGLE_PLACES_API_KEY.isBlank()) {
            showMissingApiKeysError()
            return
        }

        requestLocationPermissions()

        setContent {
            StrayCatTheme {
                StrayCatApp()
            }
        }
    }

    private fun showMissingApiKeysError() {
        val missing = buildList {
            if (BuildConfig.TOMTOM_API_KEY.isBlank()) add("tomtomApiKey")
            if (BuildConfig.GOOGLE_PLACES_API_KEY.isBlank()) add("googlePlacesApiKey")
        }
        AlertDialog.Builder(this)
            .setTitle("Missing API Keys")
            .setMessage(
                "The following API keys are not configured: ${missing.joinToString(", ")}.\n\n" +
                "Add them to local.properties in the project root and rebuild."
            )
            .setPositiveButton("OK") { _, _ -> finish() }
            .setCancelable(false)
            .show()
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
}
package com.lainovic.tomtom.straycat.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding

@Composable
fun RouteBuilderScreen(
    modifier: Modifier = Modifier,
    onNavigateToSimulation: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Route Builder",
            style = MaterialTheme.typography.headlineMedium
        )

        Button(onClick = onNavigateToSimulation) {
            Text(
                text = "Start Simulation"
            )
        }
    }
}
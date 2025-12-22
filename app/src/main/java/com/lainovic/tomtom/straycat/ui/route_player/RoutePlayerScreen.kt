package com.lainovic.tomtom.straycat.ui.route_player

import android.content.Context
import android.location.Location
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lainovic.tomtom.straycat.domain.service.LocationPlayerService
import com.lainovic.tomtom.straycat.domain.service.LocationPlayerServiceFacade
import com.lainovic.tomtom.straycat.ui.components.MapView

@Composable
fun RoutePlayerScreen(
    context: Context,
    locations: List<Location>,
    onNavigateToBuilder: () -> Unit,
    modifier: Modifier = Modifier
) {
    val service = LocationPlayerServiceFacade(
        context = context,
        serviceClass = LocationPlayerService::class.java,
    )

    val viewModel: RoutePlayerViewModel = viewModel(
        factory = RoutePlayerViewModel.Factory(service)
    )

    val startStopText by viewModel.startStopButtonText.collectAsState()
    val pauseResumeText by viewModel.pauseResumeButtonText.collectAsState()

    Box(
        modifier = modifier
    ) {
        MapView(
            points = locations,
            modifier = Modifier
                .padding(8.dp)
                .clip(RoundedCornerShape(12.dp))
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp),
        ) {
            Button(
                onClick = { viewModel.startStop(locations) },
                modifier = Modifier
            ) {
                Text(startStopText)
            }
            Button(
                onClick = viewModel::pauseResume,
                modifier = Modifier
            ) {
                Text(pauseResumeText)
            }
        }

        Button(
            onClick = onNavigateToBuilder,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 42.dp, end = 16.dp)
        ) {
            Text("Builder")
        }
    }
}
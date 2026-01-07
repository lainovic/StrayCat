package com.lainovic.tomtom.straycat.ui.components

import android.location.Location
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lainovic.tomtom.straycat.ui.theme.AppColors
import com.lainovic.tomtom.straycat.ui.theme.AppSizes

@Composable
fun SearchPlacesPanel(
    isOpen: Boolean,
    onOriginSelected: (Location, String) -> Unit,
    onDestinationSelected: (Location, String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (isOpen) {
        Surface(
            tonalElevation = AppSizes.TonalElevation,
            shadowElevation = AppSizes.ShadowElevation,
            color = AppColors.Surface,
            shape = RoundedCornerShape(AppSizes.SnackbarCornerRadius),
            modifier = modifier
                .fillMaxWidth(0.9f)
                .padding(top = AppSizes.ButtonPadding)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(AppSizes.ButtonSpacing),
                modifier = Modifier.padding(AppSizes.ButtonPadding)
            ) {
                PlaceSearchField(placeholderText = "Search Origin") { location, address ->
                    onOriginSelected(location, address)
                }
                PlaceSearchField(placeholderText = "Search Destination") { location, address ->
                    onDestinationSelected(location, address)
                }
            }
        }
    }
}


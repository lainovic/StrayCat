package com.lainovic.tomtom.straycat.ui.simulation

import android.location.Location
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.lainovic.tomtom.straycat.ui.theme.AppColors
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchPanel(
    originQuery: StateFlow<String>,
    originPredictions: StateFlow<List<AutocompletePrediction>>,
    destinationQuery: StateFlow<String>,
    destinationPredictions: StateFlow<List<AutocompletePrediction>>,
    onOriginQueryChanged: (String) -> Unit,
    onDestinationQueryChanged: (String) -> Unit,
    onOriginPredictionSelected: (AutocompletePrediction) -> Unit,
    onDestinationPredictionSelected: (AutocompletePrediction) -> Unit,
    onDismiss: () -> Unit,
) {
    val originQueryValue by originQuery.collectAsState()
    val originPredictionsValue by originPredictions.collectAsState()
    val destinationQueryValue by destinationQuery.collectAsState()
    val destinationPredictionsValue by destinationPredictions.collectAsState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp)
        ) {
            Text(
                text = "Plan Route",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            SearchField(
                label = "Origin",
                query = originQueryValue,
                onQueryChange = onOriginQueryChanged,
                predictions = originPredictionsValue,
                onPredictionSelected = onOriginPredictionSelected,
            )

            Spacer(modifier = Modifier.height(16.dp))

            SearchField(
                label = "Destination",
                query = destinationQueryValue,
                onQueryChange = onDestinationQueryChanged,
                predictions = destinationPredictionsValue,
                onPredictionSelected = onDestinationPredictionSelected,
            )
        }
    }
}

@Composable
private fun SearchField(
    label: String,
    query: String,
    onQueryChange: (String) -> Unit,
    predictions: List<AutocompletePrediction>,
    onPredictionSelected: (AutocompletePrediction) -> Unit
) {
    Column {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Clear",
                        )
                    }
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
            ),
            singleLine = true,
        )

        if (predictions.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp),
                color = MaterialTheme.colorScheme.surface,
            ) {
                LazyColumn {
                    items(predictions) { prediction ->
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = prediction.getPrimaryText(null).toString(),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            supportingContent = {
                                Text(
                                    text = prediction.getSecondaryText(null).toString(),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            leadingContent = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline
                                )
                            },
                            modifier = Modifier.clickable { onPredictionSelected(prediction) }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }
        }
    }
}

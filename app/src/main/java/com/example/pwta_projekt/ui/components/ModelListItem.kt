package com.example.pwta_projekt.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pwta_projekt.domain.models.ModelInfo
import com.example.pwta_projekt.domain.models.ModelType

@Composable
fun ModelListItem(
    modelInfo: ModelInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = modelInfo.filename,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = modelInfo.formattedFileSize,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = MaterialTheme.shapes.small,
                color = when (modelInfo.type) {
                    ModelType.STL -> MaterialTheme.colorScheme.primaryContainer
                    ModelType.DXF -> MaterialTheme.colorScheme.secondaryContainer
                }
            ) {
                Text(
                    text = modelInfo.type.displayName,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = when (modelInfo.type) {
                        ModelType.STL -> MaterialTheme.colorScheme.onPrimaryContainer
                        ModelType.DXF -> MaterialTheme.colorScheme.onSecondaryContainer
                    }
                )
            }
        }
    }
}

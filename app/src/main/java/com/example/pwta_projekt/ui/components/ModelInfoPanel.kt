package com.example.pwta_projekt.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pwta_projekt.domain.models.Model2D
import com.example.pwta_projekt.domain.models.Model3D
import com.example.pwta_projekt.domain.models.ModelInfo

@Composable
fun ModelInfoPanel(
    modelInfo: ModelInfo,
    model3D: Model3D? = null,
    model2D: Model2D? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Informacje o modelu",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            InfoRow(label = "Nazwa", value = modelInfo.filename)
            InfoRow(label = "Format", value = modelInfo.type.displayName)
            InfoRow(label = "Rozmiar", value = modelInfo.formattedFileSize)

            model3D?.let {
                Spacer(modifier = Modifier.height(8.dp))
                InfoRow(label = "Trójkąty", value = it.triangleCount.toString())
                InfoRow(label = "Wierzchołki", value = it.vertexCount.toString())
                InfoRow(
                    label = "Wymiary",
                    value = String.format(
                        "%.2f × %.2f × %.2f",
                        it.bounds.sizeX,
                        it.bounds.sizeY,
                        it.bounds.sizeZ
                    )
                )
            }

            model2D?.let {
                Spacer(modifier = Modifier.height(8.dp))
                InfoRow(label = "Encje", value = it.entityCount.toString())
                InfoRow(
                    label = "Wymiary",
                    value = String.format(
                        "%.2f × %.2f",
                        it.bounds.width,
                        it.bounds.height
                    )
                )
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

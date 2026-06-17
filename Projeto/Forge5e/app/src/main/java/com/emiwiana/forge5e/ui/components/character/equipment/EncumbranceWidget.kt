package com.emiwiana.forge5e.ui.components.character.equipment

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun EncumbranceWidget(
    totalWeight: Double,
    carryCapacity: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (totalWeight > carryCapacity) 
                MaterialTheme.colorScheme.errorContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Encumbrance", style = MaterialTheme.typography.titleMedium)
            LinearProgressIndicator(
                progress = { (totalWeight / carryCapacity).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                String.format(Locale.getDefault(), "%.1f / %.0f lbs", totalWeight, carryCapacity),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

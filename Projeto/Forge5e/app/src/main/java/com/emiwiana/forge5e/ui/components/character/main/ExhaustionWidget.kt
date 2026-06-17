package com.emiwiana.forge5e.ui.components.character.main

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emiwiana.forge5e.model.db.CharacterEntity
import com.emiwiana.forge5e.viewModel.CharacterDetailViewModel

@Composable
fun ExhaustionWidget(character: CharacterEntity, viewModel: CharacterDetailViewModel, modifier: Modifier = Modifier) {
    var showInfo by remember { mutableStateOf(false) }
    val exhaustionEffects = listOf(
        "0: No effects",
        "1: Disadvantage on ability checks",
        "2: Speed halved",
        "3: Disadvantage on attack rolls and saving throws",
        "4: Hit point maximum halved",
        "5: Speed reduced to 0",
        "6: Death"
    )

    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Exhaustion", style = MaterialTheme.typography.labelSmall)
                IconButton(onClick = { showInfo = true }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Info, contentDescription = "Info", modifier = Modifier.size(16.dp))
                }
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.updateExhaustion(character.exhaustionLevel - 1) }) {
                    Text("-")
                }
                Text("${character.exhaustionLevel}", style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = { viewModel.updateExhaustion(character.exhaustionLevel + 1) }) {
                    Text("+")
                }
            }
        }
    }

    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            title = { Text("Exhaustion Effects") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    exhaustionEffects.forEachIndexed { index, effect ->
                        val isCurrentOrBelow = index <= character.exhaustionLevel && index > 0
                        Text(
                            text = effect,
                            style = if (index == character.exhaustionLevel) 
                                MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary) 
                            else if (isCurrentOrBelow) 
                                MaterialTheme.typography.bodyMedium 
                            else 
                                MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfo = false }) { Text("OK") }
            }
        )
    }
}

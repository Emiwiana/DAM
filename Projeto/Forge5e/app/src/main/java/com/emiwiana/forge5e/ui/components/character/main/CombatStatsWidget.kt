package com.emiwiana.forge5e.ui.components.character.main

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emiwiana.forge5e.model.db.CharacterEntity
import com.emiwiana.forge5e.viewModel.CharacterDetailViewModel

@Composable
fun CombatStatsWidget(character: CharacterEntity, viewModel: CharacterDetailViewModel) {
    val initiative by viewModel.initiative.collectAsState()
    val speed by viewModel.speed.collectAsState()
    val ac by viewModel.armorClass.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem("Initiative", "${if (initiative >= 0) "+" else ""}$initiative")
            StatItem("Speed", "$speed ft")
            StatItem("Armor Class", "$ac")

            IconButton(onClick = { showEditDialog = true }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Bonuses", modifier = Modifier.size(20.dp))
            }
        }
    }

    if (showEditDialog) {
        var initiativeBonus by remember { mutableStateOf(character.initiativeBonus.toString()) }
        var speedBonus by remember { mutableStateOf(character.speedBonus.toString()) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Combat Stat Bonuses") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Add modifiers from features or items", style = MaterialTheme.typography.labelMedium)
                    TextField(
                        value = initiativeBonus,
                        onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() || c == '-' }) initiativeBonus = it },
                        label = { Text("Initiative Bonus") }
                    )
                    TextField(
                        value = speedBonus,
                        onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() || c == '-' }) speedBonus = it },
                        label = { Text("Speed Bonus") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateInitiativeBonus(initiativeBonus.toIntOrNull() ?: 0)
                    viewModel.updateSpeedBonus(speedBonus.toIntOrNull() ?: 0)
                    showEditDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
        Text(value, style = MaterialTheme.typography.titleLarge)
    }
}

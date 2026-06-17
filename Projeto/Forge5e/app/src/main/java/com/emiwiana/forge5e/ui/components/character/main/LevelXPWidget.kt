package com.emiwiana.forge5e.ui.components.character.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emiwiana.forge5e.model.db.CharacterEntity
import com.emiwiana.forge5e.viewModel.CharacterDetailViewModel

@Composable
fun LevelXPWidget(character: CharacterEntity, viewModel: CharacterDetailViewModel) {
    var showXpDialog by remember { mutableStateOf(false) }
    var xpInput by remember { mutableStateOf(character.experience.toString()) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Level ${character.level} ${character.characterClass}", style = MaterialTheme.typography.titleMedium)
                    Text("${character.experience} XP", style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = { showXpDialog = true }) {
                    Text("Edit XP")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { viewModel.levelUp() }, enabled = character.level < 20) {
                    Text("Level Up")
                }
            }
        }
    }

    if (showXpDialog) {
        AlertDialog(
            onDismissRequest = { showXpDialog = false },
            title = { Text("Update Experience") },
            text = {
                TextField(
                    value = xpInput,
                    onValueChange = { if (it.all { c -> c.isDigit() }) xpInput = it },
                    label = { Text("Experience Points") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    xpInput.toIntOrNull()?.let { viewModel.updateExperience(it) }
                    showXpDialog = false
                }) { Text("Save") }
            }
        )
    }
}

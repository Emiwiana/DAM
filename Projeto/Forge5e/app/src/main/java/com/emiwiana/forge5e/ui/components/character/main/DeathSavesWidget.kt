package com.emiwiana.forge5e.ui.components.character.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emiwiana.forge5e.model.db.CharacterEntity
import com.emiwiana.forge5e.viewModel.CharacterDetailViewModel

@Composable
fun DeathSavesWidget(character: CharacterEntity, viewModel: CharacterDetailViewModel, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Death Saves", style = MaterialTheme.typography.labelSmall)
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("S", style = MaterialTheme.typography.labelSmall)
                repeat(3) { i ->
                    Checkbox(
                        checked = i < character.deathSaveSuccesses,
                        onCheckedChange = { 
                            val newSuccesses = if (i < character.deathSaveSuccesses) i else i + 1
                            viewModel.updateDeathSaves(newSuccesses, character.deathSaveFailures)
                        },
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("F", style = MaterialTheme.typography.labelSmall)
                repeat(3) { i ->
                    Checkbox(
                        checked = i < character.deathSaveFailures,
                        onCheckedChange = { 
                            val newFailures = if (i < character.deathSaveFailures) i else i + 1
                            viewModel.updateDeathSaves(character.deathSaveSuccesses, newFailures)
                        },
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

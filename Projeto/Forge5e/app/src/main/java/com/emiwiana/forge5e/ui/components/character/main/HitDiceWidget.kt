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
fun HitDiceWidget(character: CharacterEntity, viewModel: CharacterDetailViewModel, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Hit Dice", style = MaterialTheme.typography.labelSmall)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.updateHitDice(character.currentHitDice - 1) }) {
                    Text("-")
                }
                Text("${character.currentHitDice} / ${character.level}", style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = { viewModel.updateHitDice(character.currentHitDice + 1) }) {
                    Text("+")
                }
            }
        }
    }
}

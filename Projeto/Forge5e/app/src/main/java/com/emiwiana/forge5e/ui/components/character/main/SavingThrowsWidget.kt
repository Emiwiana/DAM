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
fun SavingThrowsWidget(
    character: CharacterEntity,
    viewModel: CharacterDetailViewModel,
    proficiencyBonus: Int
) {
    Text("Saving Throws", style = MaterialTheme.typography.titleLarge)
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(8.dp)) {
            val abilities = listOf(
                "STR" to character.strength,
                "DEX" to character.dexterity,
                "CON" to character.constitution,
                "INT" to character.intelligence,
                "WIS" to character.wisdom,
                "CHA" to character.charisma
            )
            abilities.chunked(3).forEach { chunk ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    chunk.forEach { (name, score) ->
                        val isProficient = viewModel.isSavingThrowProficient(name)
                        val mod = viewModel.getModifier(score)
                        val total = mod + (if (isProficient) proficiencyBonus else 0)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = isProficient, onClick = null, enabled = false)
                            Text(
                                "$name: ${if (total >= 0) "+" else ""}$total",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

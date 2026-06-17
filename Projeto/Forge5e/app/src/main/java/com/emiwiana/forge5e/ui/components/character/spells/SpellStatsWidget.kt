package com.emiwiana.forge5e.ui.components.character.spells

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emiwiana.forge5e.model.db.CharacterEntity
import com.emiwiana.forge5e.viewModel.CharacterDetailViewModel

@Composable
fun SpellStatsWidget(character: CharacterEntity, viewModel: CharacterDetailViewModel) {
    val proficiencyBonus = viewModel.getProficiencyBonus(character.level)
    val spellcastingAbility = character.spellcastingAbility
    val spellMod = viewModel.getModifierForAbility(character, spellcastingAbility)
    val spellAttack = spellMod + proficiencyBonus
    val spellDC = 8 + proficiencyBonus + spellMod

    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Spellcasting", style = MaterialTheme.typography.titleMedium)
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Ability:", style = MaterialTheme.typography.labelSmall)
                        Spacer(modifier = Modifier.width(8.dp))
                        var expanded by remember { mutableStateOf(false) }
                        Box {
                            TextButton(onClick = { expanded = true }) {
                                Text(spellcastingAbility)
                            }
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                listOf("STR", "DEX", "CON", "INT", "WIS", "CHA").forEach { ability ->
                                    DropdownMenuItem(
                                        text = { Text(ability) },
                                        onClick = {
                                            viewModel.updateSpellcastingAbility(ability)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem("Attack Bonus", "+$spellAttack")
                    StatItem("Save DC", "$spellDC")
                    StatItem("Modifier", (if (spellMod >= 0) "+$spellMod" else "$spellMod"))
                }
            }
        }

        SpellSlotsTracker(character, viewModel)
    }
}

@Composable
fun SpellSlotsTracker(character: CharacterEntity, viewModel: CharacterDetailViewModel) {
    // Basic slot calculation or placeholder
    // For now, let's use a simple mapping or just check if the class is a caster
    val isCaster = listOf("wizard", "cleric", "druid", "bard", "sorcerer", "paladin", "ranger", "warlock")
        .contains(character.classIndex.lowercase())

    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Spell Slots", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            
            if (!isCaster) {
                Text("No Spell Slots available", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
            } else {
                // Display slots for 1st-9th level based on level/class
                // Simplified: show up to level 9 slots if they exist
                val maxSlots = getMaxSlots(character.classIndex, character.level)
                
                if (maxSlots.isEmpty()) {
                    Text("No Spell Slots available at this level", style = MaterialTheme.typography.bodyMedium)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        maxSlots.forEach { (level, max) ->
                            if (max > 0) {
                                val used = when(level) {
                                    1 -> character.usedSlots1
                                    2 -> character.usedSlots2
                                    3 -> character.usedSlots3
                                    4 -> character.usedSlots4
                                    5 -> character.usedSlots5
                                    6 -> character.usedSlots6
                                    7 -> character.usedSlots7
                                    8 -> character.usedSlots8
                                    9 -> character.usedSlots9
                                    else -> 0
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Level $level", modifier = Modifier.width(60.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = { viewModel.updateUsedSlots(level, (used - 1).coerceAtLeast(0)) }) {
                                            Text("-", style = MaterialTheme.typography.bodyLarge)
                                        }
                                        Text("$used / $max")
                                        IconButton(onClick = { viewModel.updateUsedSlots(level, (used + 1).coerceAtMost(max)) }) {
                                            Text("+", style = MaterialTheme.typography.bodyLarge)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Simplified spell slot table
fun getMaxSlots(classIndex: String, level: Int): Map<Int, Int> {
    val fullCasters = listOf("wizard", "cleric", "druid", "bard", "sorcerer")
    val halfCasters = listOf("paladin", "ranger")
    
    return when {
        fullCasters.contains(classIndex.lowercase()) -> getFullCasterSlots(level)
        halfCasters.contains(classIndex.lowercase()) -> getHalfCasterSlots(level)
        classIndex.lowercase() == "warlock" -> getWarlockSlots(level)
        else -> emptyMap()
    }
}

fun getFullCasterSlots(level: Int): Map<Int, Int> {
    val table = listOf(
        mapOf(1 to 2),
        mapOf(1 to 3),
        mapOf(1 to 4, 2 to 2),
        mapOf(1 to 4, 2 to 3),
        mapOf(1 to 4, 2 to 3, 3 to 2),
        mapOf(1 to 4, 2 to 3, 3 to 3),
        mapOf(1 to 4, 2 to 3, 3 to 3, 4 to 1),
        mapOf(1 to 4, 2 to 3, 3 to 3, 4 to 2),
        mapOf(1 to 4, 2 to 3, 3 to 3, 4 to 3, 5 to 1),
        mapOf(1 to 4, 2 to 3, 3 to 3, 4 to 3, 5 to 2),
        mapOf(1 to 4, 2 to 3, 3 to 3, 4 to 3, 5 to 2, 6 to 1),
        mapOf(1 to 4, 2 to 3, 3 to 3, 4 to 3, 5 to 2, 6 to 1),
        mapOf(1 to 4, 2 to 3, 3 to 3, 4 to 3, 5 to 2, 6 to 1, 7 to 1),
        mapOf(1 to 4, 2 to 3, 3 to 3, 4 to 3, 5 to 2, 6 to 1, 7 to 1),
        mapOf(1 to 4, 2 to 3, 3 to 3, 4 to 3, 5 to 2, 6 to 1, 7 to 1, 8 to 1),
        mapOf(1 to 4, 2 to 3, 3 to 3, 4 to 3, 5 to 2, 6 to 1, 7 to 1, 8 to 1),
        mapOf(1 to 4, 2 to 3, 3 to 3, 4 to 3, 5 to 2, 6 to 1, 7 to 1, 8 to 1, 9 to 1),
        mapOf(1 to 4, 2 to 3, 3 to 3, 4 to 3, 5 to 3, 6 to 1, 7 to 1, 8 to 1, 9 to 1),
        mapOf(1 to 4, 2 to 3, 3 to 3, 4 to 3, 5 to 3, 6 to 2, 7 to 1, 8 to 1, 9 to 1),
        mapOf(1 to 4, 2 to 3, 3 to 3, 4 to 3, 5 to 3, 6 to 2, 7 to 2, 8 to 1, 9 to 1)
    )
    return table.getOrNull(level - 1) ?: emptyMap()
}

fun getHalfCasterSlots(level: Int): Map<Int, Int> {
    if (level < 2) return emptyMap()
    val table = listOf(
        emptyMap(), // Level 1
        mapOf(1 to 2),
        mapOf(1 to 3),
        mapOf(1 to 3),
        mapOf(1 to 4, 2 to 2),
        mapOf(1 to 4, 2 to 2),
        mapOf(1 to 4, 2 to 3),
        mapOf(1 to 4, 2 to 3),
        mapOf(1 to 4, 2 to 3, 3 to 2),
        mapOf(1 to 4, 2 to 3, 3 to 2),
        mapOf(1 to 4, 2 to 3, 3 to 3),
        mapOf(1 to 4, 2 to 3, 3 to 3),
        mapOf(1 to 4, 2 to 3, 3 to 3, 4 to 1),
        mapOf(1 to 4, 2 to 3, 3 to 3, 4 to 1),
        mapOf(1 to 4, 2 to 3, 3 to 3, 4 to 2),
        mapOf(1 to 4, 2 to 3, 3 to 3, 4 to 2),
        mapOf(1 to 4, 2 to 3, 3 to 3, 4 to 3, 5 to 1),
        mapOf(1 to 4, 2 to 3, 3 to 3, 4 to 3, 5 to 1),
        mapOf(1 to 4, 2 to 3, 3 to 3, 4 to 3, 5 to 2),
        mapOf(1 to 4, 2 to 3, 3 to 3, 4 to 3, 5 to 2)
    )
    return table.getOrNull(level - 1) ?: emptyMap()
}

fun getWarlockSlots(level: Int): Map<Int, Int> {
    val slotLevel = when {
        level >= 9 -> 5
        level >= 7 -> 4
        level >= 5 -> 3
        level >= 3 -> 2
        else -> 1
    }
    val count = when {
        level >= 17 -> 4
        level >= 11 -> 3
        level >= 2 -> 2
        else -> 1
    }
    return mapOf(slotLevel to count)
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Text(value, style = MaterialTheme.typography.titleLarge)
    }
}

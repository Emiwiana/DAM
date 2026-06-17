package com.emiwiana.forge5e.ui.components.character.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emiwiana.forge5e.model.db.CharacterEntity
import com.emiwiana.forge5e.viewModel.CharacterDetailViewModel

@Composable
fun SkillsWidget(
    character: CharacterEntity,
    viewModel: CharacterDetailViewModel,
    proficiencyBonus: Int
) {
    Text("Skills", style = MaterialTheme.typography.titleLarge)
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(8.dp)) {
            val skills = listOf(
                "Acrobatics" to ("DEX" to character.dexterity),
                "Animal Handling" to ("WIS" to character.wisdom),
                "Arcana" to ("INT" to character.intelligence),
                "Athletics" to ("STR" to character.strength),
                "Deception" to ("CHA" to character.charisma),
                "History" to ("INT" to character.intelligence),
                "Insight" to ("WIS" to character.wisdom),
                "Intimidation" to ("CHA" to character.charisma),
                "Investigation" to ("INT" to character.intelligence),
                "Medicine" to ("WIS" to character.wisdom),
                "Nature" to ("INT" to character.intelligence),
                "Perception" to ("WIS" to character.wisdom),
                "Performance" to ("CHA" to character.charisma),
                "Persuasion" to ("CHA" to character.charisma),
                "Religion" to ("INT" to character.intelligence),
                "Sleight of Hand" to ("DEX" to character.dexterity),
                "Stealth" to ("DEX" to character.dexterity),
                "Survival" to ("WIS" to character.wisdom)
            )
            
            skills.forEach { (skill, abilityInfo) ->
                val (ability, score) = abilityInfo
                val isProficient = viewModel.isSkillProficient(skill)
                val mod = viewModel.getModifier(score)
                val total = mod + (if (isProficient) proficiencyBonus else 0)
                
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = isProficient, onClick = null, enabled = false, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(skill, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    Text("($ability) ${if (total >= 0) "+" else ""}$total", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

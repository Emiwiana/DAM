package com.emiwiana.forge5e.ui.components.character.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    var editingSkill by remember { mutableStateOf<String?>(null) }

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
                val isExpert = viewModel.isSkillExpert(skill)
                val mod = viewModel.getModifier(score)
                
                val bonus = if (isExpert) proficiencyBonus * 2 else if (isProficient) proficiencyBonus else 0
                val total = mod + bonus
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .clickable { editingSkill = skill },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Visual indicator for proficiency/expertise
                    Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                        if (isExpert) {
                            // Double circle or special icon for expertise
                            Surface(shape = androidx.compose.foundation.shape.CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp)) {}
                            Surface(shape = androidx.compose.foundation.shape.CircleShape, border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary), color = androidx.compose.ui.graphics.Color.Transparent, modifier = Modifier.size(20.dp)) {}
                        } else if (isProficient) {
                            Surface(shape = androidx.compose.foundation.shape.CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp)) {}
                        } else {
                            Surface(shape = androidx.compose.foundation.shape.CircleShape, border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline), color = androidx.compose.ui.graphics.Color.Transparent, modifier = Modifier.size(16.dp)) {}
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(skill, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    Text("($ability) ${if (total >= 0) "+" else ""}$total", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (editingSkill != null) {
        SkillEditDialog(
            skillName = editingSkill!!,
            isProficient = viewModel.isSkillProficient(editingSkill!!),
            isExpert = viewModel.isSkillExpert(editingSkill!!),
            onSave = { prof, expert ->
                viewModel.updateSkillProficiency(editingSkill!!, prof, expert)
                editingSkill = null
            },
            onDismiss = { editingSkill = null }
        )
    }
}

@Composable
fun SkillEditDialog(
    skillName: String,
    isProficient: Boolean,
    isExpert: Boolean,
    onSave: (Boolean, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var prof by remember { mutableStateOf(isProficient) }
    var expert by remember { mutableStateOf(isExpert) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit $skillName") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = prof, onCheckedChange = { 
                        prof = it
                        if (!it) expert = false // Can't have expertise without proficiency
                    })
                    Text("Proficient")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = expert, onCheckedChange = { 
                        expert = it
                        if (it) prof = true // Expertise implies proficiency
                    })
                    Text("Expertise")
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(prof, expert) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

package com.emiwiana.forge5e.ui.components.character.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emiwiana.forge5e.model.db.CharacterEntity
import com.emiwiana.forge5e.viewModel.CharacterDetailViewModel

@Composable
fun ProficiencyTrackerWidget(character: CharacterEntity, viewModel: CharacterDetailViewModel) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Proficiencies & Languages", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            
            ProficiencySection("Armor", character.armorProficiencies)
            ProficiencySection("Weapons", character.weaponProficiencies)
            ProficiencySection("Tools", character.toolProficiencies)
            ProficiencySection("Languages", character.languages)
        }
    }
}

@Composable
fun ProficiencySection(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        Surface(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            shape = MaterialTheme.shapes.extraSmall,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = ButtonDefaults.outlinedButtonBorder
        ) {
            Text(
                text = value.ifBlank { "None" },
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

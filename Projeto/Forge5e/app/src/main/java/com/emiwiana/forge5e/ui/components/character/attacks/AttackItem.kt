package com.emiwiana.forge5e.ui.components.character.attacks

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emiwiana.forge5e.viewModel.AttackInfo
import com.emiwiana.forge5e.viewModel.CharacterDetailViewModel

@Composable
fun AttackItem(attack: AttackInfo, viewModel: CharacterDetailViewModel) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(attack.name, style = MaterialTheme.typography.titleMedium)
                    val toHitStr = if (attack.toHit >= 0) "+${attack.toHit}" else "${attack.toHit}"
                    val damageBonusStr = if (attack.damageBonus >= 0) "+${attack.damageBonus}" else "${attack.damageBonus}"
                    
                    Text(
                        text = "$toHitStr to hit | ${attack.damage ?: "No Damage"} $damageBonusStr ${attack.damageType ?: ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(attack.range ?: "", style = MaterialTheme.typography.labelMedium)
                    Text(attack.source, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }

            // Finesse selection
            if (attack.properties.contains("Finesse") && attack.item != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Ability:", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.width(8.dp))
                    val selected = attack.item.selectedAbility ?: "Auto"
                    
                    FilterChip(
                        selected = selected == "STR",
                        onClick = { viewModel.updateEquipmentAbility(attack.item, "STR") },
                        label = { Text("STR") }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    FilterChip(
                        selected = selected == "DEX",
                        onClick = { viewModel.updateEquipmentAbility(attack.item, "DEX") },
                        label = { Text("DEX") }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    FilterChip(
                        selected = selected == "Auto",
                        onClick = { viewModel.updateEquipmentAbility(attack.item, null) },
                        label = { Text("Auto") }
                    )
                }
            }

            // Versatile selection
            if (attack.properties.contains("Versatile") && attack.item != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Hands:", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = !attack.item.useTwoHanded,
                        onClick = { viewModel.updateEquipmentVersatile(attack.item, false) },
                        label = { Text("1-Hand") }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    FilterChip(
                        selected = attack.item.useTwoHanded,
                        onClick = { viewModel.updateEquipmentVersatile(attack.item, true) },
                        label = { Text("2-Hand") }
                    )
                }
            }
            
            // Spell ability selection if it's a spell attack in the attacks tab
            if (attack.source == "Spell" && attack.spell != null) {
                 Spacer(modifier = Modifier.height(8.dp))
                 SpellAbilitySelector(attack.spell, viewModel)
            }
        }
    }
}

@Composable
fun SpellAbilitySelector(spell: com.emiwiana.forge5e.model.db.CharacterSpellEntity, viewModel: CharacterDetailViewModel) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Ability:", style = MaterialTheme.typography.labelSmall)
        Spacer(modifier = Modifier.width(8.dp))
        val selected = spell.customAbility ?: "Default"
        val abilities = listOf("INT", "WIS", "CHA", "Default")
        
        abilities.forEach { ability ->
            FilterChip(
                selected = selected == ability,
                onClick = { viewModel.updateSpellAbility(spell, if (ability == "Default") null else ability) },
                label = { Text(ability) }
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
    }
}

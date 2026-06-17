package com.emiwiana.forge5e.ui.components.character.spells

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.emiwiana.forge5e.model.db.CharacterSpellEntity
import com.emiwiana.forge5e.model.domain.BrowserItem
import com.emiwiana.forge5e.model.repository.toDomainModel
import com.emiwiana.forge5e.ui.components.browser.FeatureCard
import com.emiwiana.forge5e.viewModel.CharacterDetailViewModel
import kotlinx.coroutines.launch

@Composable
fun SpellItem(
    spell: CharacterSpellEntity,
    viewModel: CharacterDetailViewModel,
    showPreparedCheckbox: Boolean = true,
    onRemove: () -> Unit
) {
    var showUsesSettings by remember { mutableStateOf(false) }
    var showInfo by remember { mutableStateOf(false) }
    var spellDetail by remember { mutableStateOf<BrowserItem?>(null) }
    val scope = rememberCoroutineScope()

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showPreparedCheckbox) {
                    Checkbox(
                        checked = spell.isPrepared,
                        onCheckedChange = { viewModel.toggleSpellPrepared(spell) }
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(spell.name.ifBlank { spell.spellIndex }, style = MaterialTheme.typography.titleMedium)
                    if (spell.maxUses > 0) {
                        Text("${spell.currentUses} / ${spell.maxUses} uses (${spell.resetType})", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
                
                if (spell.maxUses > 0) {
                    Button(
                        onClick = { viewModel.useSpellUse(spell) },
                        enabled = spell.currentUses > 0,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Use")
                    }
                }

                IconButton(onClick = {
                    scope.launch {
                        viewModel.fetchSpellDetails(spell.spellIndex).onSuccess {
                            spellDetail = it.toDomainModel()
                            showInfo = true
                        }
                    }
                }) {
                    Icon(Icons.Default.Info, contentDescription = "Info")
                }
                
                IconButton(onClick = { showUsesSettings = true }) {
                    Icon(Icons.Default.Settings, contentDescription = "Resource Settings")
                }
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Ability Override:", style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.width(8.dp))
                val selected = spell.customAbility ?: "None"
                val abilities = listOf("STR", "DEX", "CON", "INT", "WIS", "CHA", "None")
                
                var expanded by remember { mutableStateOf(false) }
                Box {
                    AssistChip(
                        onClick = { expanded = true },
                        label = { Text(selected) }
                    )
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        abilities.forEach { ability ->
                            DropdownMenuItem(
                                text = { Text(ability) },
                                onClick = {
                                    viewModel.updateSpellAbility(spell, if (ability == "None") null else ability)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showInfo && spellDetail != null) {
        Dialog(onDismissRequest = { showInfo = false }) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    FeatureCard(item = spellDetail!!)
                    Button(
                        onClick = { showInfo = false },
                        modifier = Modifier.align(Alignment.End).padding(top = 8.dp)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }

    if (showUsesSettings) {
        var maxUses by remember { mutableStateOf(spell.maxUses.toString()) }
        var resetType by remember { mutableStateOf(spell.resetType) }

        AlertDialog(
            onDismissRequest = { showUsesSettings = false },
            title = { Text("Limited Uses Settings") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Set max uses (0 to disable limited uses for this spell)")
                    TextField(
                        value = maxUses,
                        onValueChange = { if (it.all { c -> c.isDigit() }) maxUses = it },
                        label = { Text("Max Uses") }
                    )
                    Text("Reset on:", style = MaterialTheme.typography.labelMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = resetType == "Short Rest", onClick = { resetType = "Short Rest" })
                        Text("Short Rest")
                        Spacer(modifier = Modifier.width(16.dp))
                        RadioButton(selected = resetType == "Long Rest", onClick = { resetType = "Long Rest" })
                        Text("Long Rest")
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val max = maxUses.toIntOrNull() ?: 0
                    viewModel.updateSpellUses(spell, max, max, resetType)
                    showUsesSettings = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showUsesSettings = false }) { Text("Cancel") }
            }
        )
    }
}

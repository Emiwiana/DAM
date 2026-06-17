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
fun HealthWidget(character: CharacterEntity, viewModel: CharacterDetailViewModel) {
    var showTempHpDialog by remember { mutableStateOf(false) }
    var tempHpInput by remember { mutableStateOf("") }
    var showDamageDialog by remember { mutableStateOf(false) }
    var damageInput by remember { mutableStateOf("") }
    var showHealDialog by remember { mutableStateOf(false) }
    var healInput by remember { mutableStateOf("") }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Health", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${character.currentHp} / ${character.maxHp} HP",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    if (character.temporaryHp > 0) {
                        Text(
                            "+${character.temporaryHp} Temp HP",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = { showDamageDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    Text("DMG")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { showHealDialog = true }) {
                    Text("HEAL")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { showTempHpDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Add Temporary HP")
            }
        }
    }

    if (showTempHpDialog) {
        AlertDialog(
            onDismissRequest = { showTempHpDialog = false },
            title = { Text("Add Temporary HP") },
            text = {
                TextField(value = tempHpInput, onValueChange = { if (it.all { c -> c.isDigit() }) tempHpInput = it }, label = { Text("Amount") })
            },
            confirmButton = {
                Button(onClick = {
                    tempHpInput.toIntOrNull()?.let { viewModel.addTempHp(it) }
                    showTempHpDialog = false
                    tempHpInput = ""
                }) { Text("Apply") }
            }
        )
    }

    if (showDamageDialog) {
        AlertDialog(
            onDismissRequest = { showDamageDialog = false },
            title = { Text("Take Damage") },
            text = {
                TextField(value = damageInput, onValueChange = { if (it.all { c -> c.isDigit() }) damageInput = it }, label = { Text("Amount") })
            },
            confirmButton = {
                Button(onClick = {
                    damageInput.toIntOrNull()?.let { viewModel.takeDamage(it) }
                    showDamageDialog = false
                    damageInput = ""
                }) { Text("Damage") }
            }
        )
    }

    if (showHealDialog) {
        AlertDialog(
            onDismissRequest = { showHealDialog = false },
            title = { Text("Heal") },
            text = {
                TextField(value = healInput, onValueChange = { if (it.all { c -> c.isDigit() }) healInput = it }, label = { Text("Amount") })
            },
            confirmButton = {
                Button(onClick = {
                    healInput.toIntOrNull()?.let { viewModel.heal(it) }
                    showHealDialog = false
                    healInput = ""
                }) { Text("Heal") }
            }
        )
    }
}

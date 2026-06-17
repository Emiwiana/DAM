package com.emiwiana.forge5e.ui.components.character.equipment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.emiwiana.forge5e.model.db.CharacterEntity
import com.emiwiana.forge5e.viewModel.CharacterDetailViewModel

@Composable
fun MoneyTrackerWidget(character: CharacterEntity, viewModel: CharacterDetailViewModel) {
    Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Money", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MoneyField("CP", character.cp) { viewModel.updateMoney(it, character.sp, character.ep, character.gp, character.pp) }
                MoneyField("SP", character.sp) { viewModel.updateMoney(character.cp, it, character.ep, character.gp, character.pp) }
                MoneyField("EP", character.ep) { viewModel.updateMoney(character.cp, character.sp, it, character.gp, character.pp) }
                MoneyField("GP", character.gp) { viewModel.updateMoney(character.cp, character.sp, character.ep, it, character.pp) }
                MoneyField("PP", character.pp) { viewModel.updateMoney(character.cp, character.sp, character.ep, character.gp, it) }
            }
        }
    }
}

@Composable
fun RowScope.MoneyField(label: String, value: Int, onValueChange: (Int) -> Unit) {
    var textValue by remember(value) { mutableStateOf(value.toString()) }

    OutlinedTextField(
        value = textValue,
        onValueChange = {
            textValue = it
            it.toIntOrNull()?.let { newValue -> onValueChange(newValue) }
        },
        label = { Text(label) },
        modifier = Modifier.weight(1f),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true
    )
}

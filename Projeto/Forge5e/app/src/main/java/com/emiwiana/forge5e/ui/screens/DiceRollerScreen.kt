package com.emiwiana.forge5e.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.emiwiana.forge5e.viewModel.DiceRollerViewModel
import com.emiwiana.forge5e.viewModel.RollUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiceRollerScreen(
    viewModel: DiceRollerViewModel,
    onNavigateBack: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    val selectedDice by viewModel.selectedDice.collectAsState()
    val numberOfDice by viewModel.numberOfDice.collectAsState()
    val modifierValue by viewModel.modifier.collectAsState()
    val rollState by viewModel.rollState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Dice Roller",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Dice Type Selection
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            OutlinedTextField(
                value = "d$selectedDice",
                onValueChange = {},
                readOnly = true,
                label = { Text("Select Dice Type") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                viewModel.diceOptions.forEach { side ->
                    DropdownMenuItem(
                        text = { Text("d$side") },
                        onClick = {
                            viewModel.onDiceTypeSelected(side)
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Number of Dice Input
        OutlinedTextField(
            value = numberOfDice,
            onValueChange = { viewModel.onNumberOfDiceChanged(it) },
            label = { Text("Number of Dice (max 1000)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(0.8f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Modifier Input
        OutlinedTextField(
            value = modifierValue,
            onValueChange = { viewModel.onModifierChanged(it) },
            label = { Text("Modifier (e.g. 5 or -2)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(0.8f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Roll Button
        Button(
            onClick = { viewModel.rollDice() },
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(56.dp)
        ) {
            Text("Roll Dice", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Results Display
        when (val state = rollState) {
            is RollUiState.Success -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Total: ${state.total}",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    
                    if (state.modifier != 0) {
                        val sign = if (state.modifier > 0) "+" else "-"
                        Text(
                            text = "(${state.diceSum} $sign ${if (state.modifier > 0) state.modifier else -state.modifier})",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val rollsToDisplay = state.rolls.take(30)
                    Text(
                        text = "Rolls: ${rollsToDisplay.joinToString(", ")}${if (state.rolls.size > 30) "..." else ""}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (state.rolls.size > 30) {
                        Text(
                            text = "(Showing first 30 of ${state.rolls.size} rolls)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            RollUiState.Idle -> {
                Text(
                    text = "Ready to roll!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        TextButton(onClick = onNavigateBack) {
            Text("Back to Menu")
        }
    }
}

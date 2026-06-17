package com.emiwiana.forge5e.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emiwiana.forge5e.model.api.dto.APIReference
import com.emiwiana.forge5e.model.api.dto.character.characterClass.EquipmentOption
import com.emiwiana.forge5e.viewModel.CharacterBuilderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterBuilderScreen(
    viewModel: CharacterBuilderViewModel,
    onNavigateBack: () -> Unit,
    onFinish: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Character Builder - Step ${uiState.step}/${uiState.maxSteps}") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 2.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (uiState.step > 1) {
                        TextButton(onClick = { viewModel.previousStep() }) {
                            Text("Back")
                        }
                    } else {
                        Spacer(Modifier.width(8.dp))
                    }

                    Button(
                        onClick = {
                            if (uiState.step == uiState.maxSteps) {
                                viewModel.finishCreation()
                                onFinish()
                            } else {
                                viewModel.nextStep()
                            }
                        },
                        enabled = isStepValid(uiState.step, uiState)
                    ) {
                        Text(if (uiState.step == uiState.maxSteps) "Finish" else "Next")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            when (uiState.step) {
                1 -> VariantRulesStep(viewModel)
                2 -> AbilityScoresStep(viewModel)
                3 -> RaceBackgroundStep(viewModel)
                4 -> ClassStep(viewModel)
                5 -> FeatStep(viewModel)
            }
        }
    }
}

private fun isStepValid(step: Int, state: com.emiwiana.forge5e.viewModel.CharacterBuilderState): Boolean {
    return when (step) {
        1 -> state.name.isNotBlank()
        3 -> state.selectedRace != null && state.selectedBackground != null
        4 -> state.selectedClass != null
        5 -> state.selectedFeat != null
        else -> true
    }
}

@Composable
fun VariantRulesStep(viewModel: CharacterBuilderViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Basic Information", style = MaterialTheme.typography.titleLarge)
        
        OutlinedTextField(
            value = uiState.name,
            onValueChange = { viewModel.updateName(it) },
            label = { Text("Character Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        HorizontalDivider()

        Text("Variant Rules & Preferences", style = MaterialTheme.typography.titleLarge)

        ListItem(
            headlineContent = { Text("Level-Up Progression") },
            supportingContent = { Text(if (uiState.useMilestones) "Milestones (Story-based)" else "Experience Points (XP)") },
            trailingContent = {
                Switch(checked = uiState.useMilestones, onCheckedChange = { viewModel.updateUseMilestones(it) })
            }
        )

        ListItem(
            headlineContent = { Text("Use Encumbrance") },
            supportingContent = { Text("Calculate weight of items and speed penalties") },
            trailingContent = {
                Switch(checked = uiState.useEncumbrance, onCheckedChange = { viewModel.updateUseEncumbrance(it) })
            }
        )

        Column {
            Text("HP Calculation Mode", style = MaterialTheme.typography.titleMedium)
            val modes = listOf("Average", "Max", "Roll", "RollAboveAverage")
            val modeLabels = mapOf(
                "Average" to "Fixed Average",
                "Max" to "Always Maximum",
                "Roll" to "Standard Roll",
                "RollAboveAverage" to "Roll (Take average if lower)"
            )
            
            modes.forEach { mode ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { viewModel.updateHpCalculationMode(mode) }) {
                    RadioButton(selected = uiState.hpCalculationMode == mode, onClick = { viewModel.updateHpCalculationMode(mode) })
                    Text(modeLabels[mode] ?: mode, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }

        ListItem(
            headlineContent = { Text("Use Feats") },
            supportingContent = { Text("Allow selecting feats instead of Ability Score Improvements") },
            trailingContent = {
                Switch(checked = uiState.useFeats, onCheckedChange = { viewModel.updateUseFeats(it) })
            }
        )

        if (uiState.useFeats) {
            ListItem(
                headlineContent = { Text("Starting Feat") },
                supportingContent = { Text("Begin with one free feat at level 1") },
                trailingContent = {
                    Switch(checked = uiState.hasStartingFeat, onCheckedChange = { viewModel.updateHasStartingFeat(it) })
                },
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}

@Composable
fun AbilityScoresStep(viewModel: CharacterBuilderViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Ability Scores", style = MaterialTheme.typography.titleLarge)
        Text("Manually enter your base ability scores (before racial bonuses).", style = MaterialTheme.typography.bodyMedium)

        AbilityScoreSlider("STR", uiState.baseStr) { viewModel.updateAbilityScore("STR", it) }
        AbilityScoreSlider("DEX", uiState.baseDex) { viewModel.updateAbilityScore("DEX", it) }
        AbilityScoreSlider("CON", uiState.baseCon) { viewModel.updateAbilityScore("CON", it) }
        AbilityScoreSlider("INT", uiState.baseInt) { viewModel.updateAbilityScore("INT", it) }
        AbilityScoreSlider("WIS", uiState.baseWis) { viewModel.updateAbilityScore("WIS", it) }
        AbilityScoreSlider("CHA", uiState.baseCha) { viewModel.updateAbilityScore("CHA", it) }
    }
}

@Composable
private fun AbilityScoreSlider(label: String, value: Int, onUpdate: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(label, modifier = Modifier.width(40.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Slider(
            value = value.toFloat(),
            onValueChange = { onUpdate(it.toInt()) },
            valueRange = 3f..18f,
            steps = 14,
            modifier = Modifier.weight(1f)
        )
        Text(value.toString(), style = MaterialTheme.typography.titleLarge, modifier = Modifier.width(30.dp))
    }
}

@Composable
fun RaceBackgroundStep(viewModel: CharacterBuilderViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Race & Background", style = MaterialTheme.typography.titleLarge)

        SrdDropdown(
            label = "Race",
            selected = uiState.selectedRace?.name,
            options = uiState.availableRaces,
            onSelect = { viewModel.selectRace(it.index) }
        )

        if (uiState.availableSubraces.isNotEmpty()) {
            SrdDropdown(
                label = "Subrace",
                selected = uiState.selectedSubrace?.name,
                options = uiState.availableSubraces,
                onSelect = { viewModel.selectSubrace(it.index) }
            )
        }

        SrdDropdown(
            label = "Background",
            selected = uiState.selectedBackground?.name,
            options = uiState.availableBackgrounds,
            onSelect = { viewModel.selectBackground(it.index) }
        )

        uiState.selectedRace?.let { race ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Racial Traits", style = MaterialTheme.typography.titleSmall)
                    race.traits.forEach { trait ->
                        Text("• ${trait.name}", style = MaterialTheme.typography.bodySmall)
                    }
                    if (race.abilityBonuses.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text("Ability Bonuses:", style = MaterialTheme.typography.labelSmall)
                        race.abilityBonuses.forEach { bonus ->
                            Text("+${bonus.bonus} ${bonus.abilityScore.name}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClassStep(viewModel: CharacterBuilderViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Class & Subclass", style = MaterialTheme.typography.titleLarge)

        SrdDropdown(
            label = "Class",
            selected = uiState.selectedClass?.name,
            options = uiState.availableClasses,
            onSelect = { viewModel.selectClass(it.index) }
        )

        if (uiState.availableSubclasses.isNotEmpty()) {
            SrdDropdown(
                label = "Subclass",
                selected = uiState.selectedSubclass?.name,
                options = uiState.availableSubclasses,
                onSelect = { viewModel.selectSubclass(it) }
            )
        }

        uiState.selectedClass?.let { clazz ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Class Details", style = MaterialTheme.typography.titleSmall)
                    Text("Hit Die: d${clazz.hitDie}")
                    Text("Saving Throws: ${clazz.savingThrows.joinToString { it.name }}")
                }
            }
            
            uiState.startingEquipment?.let { startingEquip ->
                Text("Starting Equipment Choices", style = MaterialTheme.typography.titleMedium)
                
                startingEquip.startingEquipmentOptions?.forEachIndexed { index, choice ->
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(choice.desc ?: "Choose ${choice.choose}:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        
                        choice.from.options?.forEach { option ->
                            val optionLabel = getOptionLabel(option)
                            val optionIndex = getOptionIndex(option)
                            
                            if (optionIndex != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().clickable { viewModel.selectEquipment(index, optionIndex) }
                                ) {
                                    RadioButton(
                                        selected = uiState.equipmentSelections[index] == optionIndex,
                                        onClick = { viewModel.selectEquipment(index, optionIndex) }
                                    )
                                    Text(optionLabel, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }
        }
    }
}

private fun getOptionLabel(option: EquipmentOption): String {
    return option.item?.name 
        ?: option.of?.name 
        ?: option.items?.joinToString(", ") { it.item?.name ?: it.of?.name ?: "Unknown" }
        ?: "Choice Option"
}

private fun getOptionIndex(option: EquipmentOption): String? {
    return option.item?.index ?: option.of?.index ?: option.items?.firstOrNull()?.let { it.item?.index ?: it.of?.index }
}

@Composable
fun FeatStep(viewModel: CharacterBuilderViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Starting Feat", style = MaterialTheme.typography.titleLarge)
        Text("Select a feat to begin your adventure with.", style = MaterialTheme.typography.bodyMedium)

        SrdDropdown(
            label = "Feat",
            selected = uiState.selectedFeat?.name,
            options = uiState.availableFeats,
            onSelect = { viewModel.selectFeat(it.index) }
        )

        uiState.selectedFeat?.let { feat ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(feat.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    feat.desc.forEach { line ->
                        Text(line, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SrdDropdown(
    label: String,
    selected: String?,
    options: List<APIReference>,
    onSelect: (APIReference) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selected ?: "Select $label",
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }
        )
        // Hidden clickable layer to trigger dropdown
        Box(modifier = Modifier.matchParentSize().clickable { expanded = true })

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.name) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

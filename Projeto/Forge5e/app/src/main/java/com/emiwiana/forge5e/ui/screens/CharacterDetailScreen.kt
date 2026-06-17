package com.emiwiana.forge5e.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.emiwiana.forge5e.model.api.dto.APIReference
import com.emiwiana.forge5e.model.db.*
import com.emiwiana.forge5e.model.domain.EquipmentItem
import com.emiwiana.forge5e.model.repository.toDomainModel
import com.emiwiana.forge5e.ui.components.character.main.*
import com.emiwiana.forge5e.ui.components.character.attacks.AttackItem
import com.emiwiana.forge5e.ui.components.character.equipment.EncumbranceWidget
import com.emiwiana.forge5e.ui.components.character.equipment.MoneyTrackerWidget
import com.emiwiana.forge5e.ui.components.character.spells.SpellItem
import com.emiwiana.forge5e.ui.components.character.spells.SpellStatsWidget
import com.emiwiana.forge5e.viewModel.CharacterDetailViewModel
import com.emiwiana.forge5e.viewModel.FeatureInfo
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterDetailScreen(
    viewModel: CharacterDetailViewModel,
    onNavigateBack: () -> Unit
) {
    val character by viewModel.character.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Main", "Attacks", "Spells", "Equipment", "Features")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(character?.name ?: "Character Sheet") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            ScrollableTabRow(selectedTabIndex = selectedTabIndex, edgePadding = 16.dp) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            character?.let { char ->
                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTabIndex) {
                        0 -> MainTab(char, viewModel)
                        1 -> AttacksTab(viewModel)
                        2 -> SpellsTab(char, viewModel)
                        3 -> EquipmentTab(char, viewModel)
                        4 -> FeaturesTab(viewModel)
                    }
                }
            } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun MainTab(character: CharacterEntity, viewModel: CharacterDetailViewModel) {
    var showShortRestDialog by remember { mutableStateOf(false) }
    var showLongRestConfirm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { LevelXPWidget(character, viewModel) }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { showShortRestDialog = true }, modifier = Modifier.weight(1f)) { Text("Short Rest") }
                Button(onClick = { showLongRestConfirm = true }, modifier = Modifier.weight(1f)) { Text("Long Rest") }
            }
        }

        item { HealthWidget(character, viewModel) }
        item { CombatStatsWidget(character, viewModel) }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HitDiceWidget(character, viewModel, modifier = Modifier.weight(1f))
                DeathSavesWidget(character, viewModel, modifier = Modifier.weight(1f))
                ExhaustionWidget(character, viewModel, modifier = Modifier.weight(1f))
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProficiencyBonusCard(viewModel.getProficiencyBonus(character.level))
                PassivePerceptionWidget(viewModel)
            }
        }

        item {
            Text("Ability Scores", style = MaterialTheme.typography.titleLarge)
            AbilityScoresGrid(character)
        }

        item { SavingThrowsWidget(character, viewModel, viewModel.getProficiencyBonus(character.level)) }
        item { SkillsWidget(character, viewModel, viewModel.getProficiencyBonus(character.level)) }
        item { ProficiencyTrackerWidget(character, viewModel) }
    }

    if (showShortRestDialog) {
        ShortRestDialog(character, viewModel, onDismiss = { showShortRestDialog = false })
    }

    if (showLongRestConfirm) {
        LongRestConfirmDialog(viewModel, onDismiss = { showLongRestConfirm = false })
    }
}

@Composable
private fun ProficiencyBonusCard(bonus: Int) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Proficiency", style = MaterialTheme.typography.labelSmall)
            Text("+$bonus", style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun AbilityScoresGrid(character: CharacterEntity) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            AbilityScoreItem("STR", character.strength)
            AbilityScoreItem("DEX", character.dexterity)
            AbilityScoreItem("CON", character.constitution)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            AbilityScoreItem("INT", character.intelligence)
            AbilityScoreItem("WIS", character.wisdom)
            AbilityScoreItem("CHA", character.charisma)
        }
    }
}

@Composable
fun AttacksTab(viewModel: CharacterDetailViewModel) {
    val attacks by viewModel.attacks.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (attacks.isEmpty()) {
            item { Text("No attacks available. Equip weapons or add spells.") }
        }
        items(attacks) { attack -> AttackItem(attack, viewModel) }
    }
}

@Composable
fun SpellsTab(character: CharacterEntity, viewModel: CharacterDetailViewModel) {
    val spells by viewModel.spells.collectAsState()
    val availableSpells by viewModel.availableSpells.collectAsState()
    var showSettings by remember { mutableStateOf(false) }
    var showAddSpellDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Spellcasting Stats", style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = { showSettings = true }) { Icon(Icons.Default.Settings, contentDescription = "Settings") }
                }
            }

            item { SpellStatsWidget(character, viewModel) }

            if (character.preparesSpells) {
                val (prepared, known) = spells.partition { it.isPrepared }
                
                item { SpellSectionHeader("Prepared Spells", prepared.size, character.maxPreparedSpells) }
                items(prepared) { spell -> SpellItem(spell, viewModel, onRemove = { viewModel.removeSpell(spell) }) }

                item { SpellSectionHeader("Known Spells", spells.size, character.maxKnownSpells, Modifier.padding(top = 16.dp)) }
                items(known) { spell -> SpellItem(spell, viewModel, onRemove = { viewModel.removeSpell(spell) }) }
            } else {
                item { SpellSectionHeader("Spells", spells.size, character.maxKnownSpells) }
                items(spells) { spell ->
                    SpellItem(spell, viewModel, showPreparedCheckbox = false, onRemove = { viewModel.removeSpell(spell) })
                }
            }
        }

        if (spells.size < character.maxKnownSpells) {
            Button(
                onClick = { showAddSpellDialog = true },
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Add Spell")
            }
        }
    }

    if (showSettings) {
        SpellcastingSettingsDialog(character, viewModel, onDismiss = { showSettings = false })
    }

    if (showAddSpellDialog) {
        SrdSpellDialog(
            availableSpells = availableSpells.filter { avail -> spells.none { it.spellIndex == avail.index } },
            onAdd = { viewModel.addSpellFromSrd(it) },
            onDismiss = { showAddSpellDialog = false }
        )
    }
}

@Composable
private fun SpellSectionHeader(title: String, current: Int, max: Int, modifier: Modifier = Modifier) {
    Text("$title ($current / $max)", style = MaterialTheme.typography.titleMedium, modifier = modifier)
}

@Composable
fun EquipmentTab(character: CharacterEntity, viewModel: CharacterDetailViewModel) {
    val inventory by viewModel.inventory.collectAsState()
    val availableEq by viewModel.availableEquipment.collectAsState()
    var showAddEqDialog by remember { mutableStateOf(false) }

    val totalWeight = inventory.sumOf { it.weight * it.quantity }
    val carryCapacity = character.strength * 15.0

    Column(modifier = Modifier.fillMaxSize()) {
        EncumbranceWidget(character = character, totalWeight = totalWeight, carryCapacity = carryCapacity)
        MoneyTrackerWidget(character, viewModel)

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(inventory) { item -> EquipmentItemRow(item, viewModel) }
        }

        Button(onClick = { showAddEqDialog = true }, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(8.dp))
            Text("Add from SRD")
        }
    }

    if (showAddEqDialog) {
        SrdEquipmentDialog(availableEq, onAdd = { viewModel.addEquipmentFromSrd(it) }, onDismiss = { showAddEqDialog = false })
    }
}

@Composable
fun EquipmentItemRow(item: CharacterEquipmentEntity, viewModel: CharacterDetailViewModel) {
    var showInfo by remember { mutableStateOf(false) }
    var itemDetail by remember { mutableStateOf<EquipmentItem?>(null) }
    val scope = rememberCoroutineScope()

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name.ifBlank { item.equipmentIndex })
                Text("${item.weight} lbs | Qty: ${item.quantity}", style = MaterialTheme.typography.bodySmall)
            }

            IconButton(onClick = {
                scope.launch {
                    viewModel.fetchEquipmentDetails(item.equipmentIndex).onSuccess {
                        itemDetail = it.toDomainModel()
                        showInfo = true
                    }
                }
            }) { Icon(Icons.Default.Info, "Info") }

            Text("Equipped", style = MaterialTheme.typography.labelSmall)
            Checkbox(checked = item.isEquipped, onCheckedChange = { viewModel.toggleEquip(item) })
            IconButton(onClick = { viewModel.removeEquipment(item) }) { Icon(Icons.Default.Delete, "Delete") }
        }
    }

    if (showInfo && itemDetail != null) {
        InfoDialog(itemDetail!!, onDismiss = { showInfo = false })
    }
}

@Composable
fun SrdSpellDialog(
    availableSpells: List<APIReference>,
    onAdd: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Select Spell", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(availableSpells) { spell ->
                        ListItem(
                            headlineContent = { Text(spell.name) },
                            modifier = Modifier.clickable {
                                onAdd(spell.index)
                                onDismiss()
                            }
                        )
                        HorizontalDivider()
                    }
                }
                Spacer(Modifier.height(16.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
fun FeaturesTab(viewModel: CharacterDetailViewModel) {
    val features by viewModel.features.collectAsState()
    var selectedFeature by remember { mutableStateOf<FeatureInfo?>(null) }
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(features) { feature ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        scope.launch {
                            viewModel.fetchFeatureDetails(feature.featureIndex, feature.featureType)
                                .onSuccess { selectedFeature = it }
                        }
                    }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(feature.name, style = MaterialTheme.typography.titleMedium)
                    Text("${feature.source} | ${feature.featureType}", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }

    selectedFeature?.let { info ->
        AlertDialog(
            onDismissRequest = { selectedFeature = null },
            title = { Text(info.name) },
            text = { Text(info.description) },
            confirmButton = { TextButton(onClick = { selectedFeature = null }) { Text("Close") } }
        )
    }
}

@Composable
fun SrdEquipmentDialog(
    availableEq: List<APIReference>,
    onAdd: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Add Equipment", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(availableEq) { eq ->
                        ListItem(
                            headlineContent = { Text(eq.name) },
                            modifier = Modifier.clickable {
                                onAdd(eq.index)
                                onDismiss()
                            }
                        )
                        HorizontalDivider()
                    }
                }
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("Cancel") }
            }
        }
    }
}

@Composable
fun InfoDialog(item: EquipmentItem, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(item.name) },
        text = {
            Column {
                Text("Category: ${item.category}")
                if (item.cost.isNotBlank()) Text("Cost: ${item.cost}")
                Text("Weight: ${item.weight} lbs")
                if (item.description.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(item.description)
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
fun ShortRestDialog(character: CharacterEntity, viewModel: CharacterDetailViewModel, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Short Rest") },
        text = { Text("Use hit dice to heal or reset short-rest features?") },
        confirmButton = {
            Button(onClick = {
                viewModel.shortRest()
                onDismiss()
            }) { Text("Rest") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun LongRestConfirmDialog(viewModel: CharacterDetailViewModel, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Long Rest") },
        text = { Text("Perform a long rest? This will restore all HP, half hit dice, and reset all features/slots.") },
        confirmButton = {
            Button(onClick = {
                viewModel.longRest()
                onDismiss()
            }) { Text("Confirm") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun SpellcastingSettingsDialog(character: CharacterEntity, viewModel: CharacterDetailViewModel, onDismiss: () -> Unit) {
    var prepares by remember { mutableStateOf(character.preparesSpells) }
    var maxPrepared by remember { mutableStateOf(character.maxPreparedSpells.toString()) }
    var maxKnown by remember { mutableStateOf(character.maxKnownSpells.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Spellcasting Settings") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = prepares, onCheckedChange = { prepares = it })
                    Text("Prepares Spells")
                }
                OutlinedTextField(value = maxPrepared, onValueChange = { maxPrepared = it }, label = { Text("Max Prepared Spells") })
                OutlinedTextField(value = maxKnown, onValueChange = { maxKnown = it }, label = { Text("Max Known Spells") })
            }
        },
        confirmButton = {
            Button(onClick = {
                viewModel.updateSpellcastingLimits(prepares, maxPrepared.toIntOrNull() ?: 0, maxKnown.toIntOrNull() ?: 0)
                onDismiss()
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

package com.emiwiana.forge5e.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emiwiana.forge5e.model.db.CharacterEntity
import com.emiwiana.forge5e.ui.components.character.main.AbilityScoreItem
import com.emiwiana.forge5e.ui.components.character.attacks.AttackItem
import com.emiwiana.forge5e.ui.components.character.main.DeathSavesWidget
import com.emiwiana.forge5e.ui.components.character.equipment.EncumbranceWidget
import com.emiwiana.forge5e.ui.components.character.main.ExhaustionWidget
import com.emiwiana.forge5e.ui.components.character.main.HealthWidget
import com.emiwiana.forge5e.ui.components.character.main.LevelXPWidget
import com.emiwiana.forge5e.ui.components.character.main.SavingThrowsWidget
import com.emiwiana.forge5e.ui.components.character.main.SkillsWidget
import com.emiwiana.forge5e.ui.components.character.spells.SpellItem
import com.emiwiana.forge5e.viewModel.CharacterDetailViewModel

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
                when (selectedTabIndex) {
                    0 -> MainTab(char, viewModel)
                    1 -> AttacksTab(viewModel)
                    2 -> SpellsTab(viewModel)
                    3 -> EquipmentTab(char, viewModel)
                    4 -> FeaturesTab(viewModel)
                }
            } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun MainTab(character: CharacterEntity, viewModel: CharacterDetailViewModel) {
    val proficiencyBonus = viewModel.getProficiencyBonus(character.level)
    val ac by viewModel.armorClass.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            LevelXPWidget(character, viewModel)
        }

        item {
            HealthWidget(character, viewModel)
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DeathSavesWidget(character, viewModel, modifier = Modifier.weight(1f))
                ExhaustionWidget(character, viewModel, modifier = Modifier.weight(1f))
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
            ) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Proficiency", style = MaterialTheme.typography.labelSmall)
                        Text("+$proficiencyBonus", style = MaterialTheme.typography.titleLarge)
                    }
                }
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Armor Class", style = MaterialTheme.typography.labelSmall)
                        Text("$ac", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }

        item {
            Text("Ability Scores", style = MaterialTheme.typography.titleLarge)
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

        item {
            SavingThrowsWidget(character, viewModel, proficiencyBonus)
        }

        item {
            SkillsWidget(character, viewModel, proficiencyBonus)
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
        items(attacks) { attack ->
            AttackItem(attack)
        }
    }
}

@Composable
fun SpellsTab(viewModel: CharacterDetailViewModel) {
    val spells by viewModel.spells.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(spells) { spell ->
            SpellItem(spell = spell, onRemove = { viewModel.removeSpell(spell) })
        }
    }
}

@Composable
fun EquipmentTab(character: CharacterEntity, viewModel: CharacterDetailViewModel) {
    val inventory by viewModel.inventory.collectAsState()
    val availableEq by viewModel.availableEquipment.collectAsState()
    var showAddEqDialog by remember { mutableStateOf(false) }

    val totalWeight = inventory.sumOf { it.weight * it.quantity }
    val carryCapacity = character.strength * 15.0

    Column(modifier = Modifier.fillMaxSize()) {
        EncumbranceWidget(totalWeight = totalWeight, carryCapacity = carryCapacity)

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(inventory) { item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.name.ifBlank { item.equipmentIndex })
                            Text("${item.weight} lbs | Qty: ${item.quantity}", style = MaterialTheme.typography.bodySmall)
                        }
                        Text("Equipped", style = MaterialTheme.typography.labelSmall)
                        Checkbox(checked = item.isEquipped, onCheckedChange = { viewModel.toggleEquip(item) })
                        IconButton(onClick = { viewModel.removeEquipment(item) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            }
        }

        Button(
            onClick = { showAddEqDialog = true },
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add from SRD")
        }
    }

    if (showAddEqDialog) {
        AlertDialog(
            onDismissRequest = { showAddEqDialog = false },
            title = { Text("Add Equipment from SRD") },
            text = {
                Box(modifier = Modifier.height(300.dp)) {
                    LazyColumn {
                        items(availableEq) { eq ->
                            TextButton(onClick = {
                                viewModel.addEquipmentFromSrd(eq.index)
                                showAddEqDialog = false
                            }, modifier = Modifier.fillMaxWidth()) {
                                Text(eq.name)
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showAddEqDialog = false }) { Text("Close") } }
        )
    }
}

@Composable
fun FeaturesTab(viewModel: CharacterDetailViewModel) {
    val features by viewModel.features.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (features.isEmpty()) {
            item { Text("No features found for this character's race and class.") }
        }
        items(features) { feature ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(feature.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        Surface(
                            shape = MaterialTheme.shapes.extraSmall,
                            color = if (feature.source == "Race") MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                feature.source,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(feature.description, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

package com.emiwiana.forge5e.ui.screens

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
import com.emiwiana.forge5e.model.db.CharacterEntity
import com.emiwiana.forge5e.model.db.CharacterEquipmentEntity
import com.emiwiana.forge5e.model.db.CharacterSpellEntity
import com.emiwiana.forge5e.model.domain.EquipmentItem
import com.emiwiana.forge5e.model.repository.toDomainModel
import com.emiwiana.forge5e.ui.components.browser.FeatureCard
import com.emiwiana.forge5e.ui.components.character.main.AbilityScoreItem
import com.emiwiana.forge5e.ui.components.character.attacks.AttackItem
import com.emiwiana.forge5e.ui.components.character.main.DeathSavesWidget
import com.emiwiana.forge5e.ui.components.character.equipment.EncumbranceWidget
import com.emiwiana.forge5e.ui.components.character.equipment.MoneyTrackerWidget
import com.emiwiana.forge5e.ui.components.character.main.ExhaustionWidget
import com.emiwiana.forge5e.ui.components.character.main.HealthWidget
import com.emiwiana.forge5e.ui.components.character.main.HitDiceWidget
import com.emiwiana.forge5e.ui.components.character.main.LevelXPWidget
import com.emiwiana.forge5e.ui.components.character.main.SavingThrowsWidget
import com.emiwiana.forge5e.ui.components.character.main.SkillsWidget
import com.emiwiana.forge5e.ui.components.character.main.ProficiencyTrackerWidget
import com.emiwiana.forge5e.ui.components.character.main.TrackersWidget
import com.emiwiana.forge5e.ui.components.character.main.CombatStatsWidget
import com.emiwiana.forge5e.ui.components.character.main.PassivePerceptionWidget
import com.emiwiana.forge5e.ui.components.character.spells.SpellItem
import com.emiwiana.forge5e.ui.components.character.spells.SpellStatsWidget
import com.emiwiana.forge5e.viewModel.CharacterDetailViewModel
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
                when (selectedTabIndex) {
                    0 -> MainTab(char, viewModel)
                    1 -> AttacksTab(viewModel)
                    2 -> SpellsTab(char, viewModel)
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
    var showShortRestDialog by remember { mutableStateOf(false) }
    var showLongRestConfirm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            LevelXPWidget(character, viewModel)
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { showShortRestDialog = true }, modifier = Modifier.weight(1f)) {
                    Text("Short Rest")
                }
                Button(onClick = { showLongRestConfirm = true }, modifier = Modifier.weight(1f)) {
                    Text("Long Rest")
                }
            }
        }

        item {
            HealthWidget(character, viewModel)
        }

        item {
            CombatStatsWidget(character, viewModel)
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HitDiceWidget(character, viewModel, modifier = Modifier.weight(1f))
                DeathSavesWidget(character, viewModel, modifier = Modifier.weight(1f))
                ExhaustionWidget(character, viewModel, modifier = Modifier.weight(1f))
            }
        }

        item {
            TrackersWidget(viewModel)
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
                PassivePerceptionWidget(viewModel)
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

        item {
            ProficiencyTrackerWidget(character, viewModel)
        }
    }

    if (showShortRestDialog) {
        AlertDialog(
            onDismissRequest = {
                viewModel.shortRest()
                showShortRestDialog = false
            },
            title = { Text("Short Rest") },
            text = {
                Column {
                    Text("Expend hit dice to heal. You have ${character.currentHitDice} hit dice remaining.")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.spendHitDie() },
                        enabled = character.currentHitDice > 0 && character.currentHp < character.maxHp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Spend Hit Die (1d4 + CON)")
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.shortRest()
                    showShortRestDialog = false
                }) { Text("Finish Rest") }
            }
        )
    }

    if (showLongRestConfirm) {
        AlertDialog(
            onDismissRequest = { showLongRestConfirm = false },
            title = { Text("Confirm Long Rest") },
            text = { Text("A long rest will restore all HP, some hit dice, spell slots, and resources. Reset death saves?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.longRest()
                    showLongRestConfirm = false
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { showLongRestConfirm = false }) { Text("Cancel") }
            }
        )
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
            AttackItem(attack, viewModel)
        }
    }
}

@Composable
fun SpellsTab(character: CharacterEntity, viewModel: CharacterDetailViewModel) {
    val spells by viewModel.spells.collectAsState()
    var showSettings by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Spellcasting Stats", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = { showSettings = true }) {
                    Icon(Icons.Default.Settings, contentDescription = "Spellcasting Settings")
                }
            }
        }

        item {
            SpellStatsWidget(character, viewModel)
        }

        if (character.preparesSpells) {
            val prepared = spells.filter { it.isPrepared }
            val known = spells.filter { !it.isPrepared }

            item {
                Text("Prepared Spells (${prepared.size} / ${character.maxPreparedSpells})", style = MaterialTheme.typography.titleMedium)
            }
            items(prepared) { spell ->
                SpellItem(spell = spell, viewModel = viewModel, onRemove = { viewModel.removeSpell(spell) })
            }

            item {
                Text("Known Spells (${spells.size} / ${character.maxKnownSpells})", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
            }
            items(known) { spell ->
                SpellItem(spell = spell, viewModel = viewModel, onRemove = { viewModel.removeSpell(spell) })
            }
        } else {
            item {
                Text("Spells (${spells.size} / ${character.maxKnownSpells})", style = MaterialTheme.typography.titleMedium)
            }
            items(spells) { spell ->
                SpellItem(
                    spell = spell,
                    viewModel = viewModel,
                    showPreparedCheckbox = false,
                    onRemove = { viewModel.removeSpell(spell) }
                )
            }
        }
    }

    if (showSettings) {
        var preparesSpells by remember { mutableStateOf(character.preparesSpells) }
        var maxPrepared by remember { mutableStateOf(character.maxPreparedSpells.toString()) }
        var maxKnown by remember { mutableStateOf(character.maxKnownSpells.toString()) }

        AlertDialog(
            onDismissRequest = { showSettings = false },
            title = { Text("Spellcasting Configuration") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = preparesSpells, onCheckedChange = { preparesSpells = it })
                        Text("Prepares Spells (e.g. Wizard, Cleric)")
                    }
                    TextField(
                        value = maxPrepared,
                        onValueChange = { if (it.all { c -> c.isDigit() }) maxPrepared = it },
                        label = { Text("Max Prepared Spells") },
                        enabled = preparesSpells
                    )
                    TextField(
                        value = maxKnown,
                        onValueChange = { if (it.all { c -> c.isDigit() }) maxKnown = it },
                        label = { Text("Max Known Spells") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateSpellcastingLimits(
                        preparesSpells,
                        maxPrepared.toIntOrNull() ?: 0,
                        maxKnown.toIntOrNull() ?: 0
                    )
                    showSettings = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showSettings = false }) { Text("Cancel") }
            }
        )
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

        MoneyTrackerWidget(character, viewModel)

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(inventory) { item ->
                EquipmentItemRow(item, viewModel)
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
            }) {
                Icon(Icons.Default.Info, contentDescription = "Info")
            }

            Text("Equipped", style = MaterialTheme.typography.labelSmall)
            Checkbox(checked = item.isEquipped, onCheckedChange = { viewModel.toggleEquip(item) })
            IconButton(onClick = { viewModel.removeEquipment(item) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }

    if (showInfo && itemDetail != null) {
        Dialog(onDismissRequest = { showInfo = false }) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    FeatureCard(item = itemDetail!!)
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

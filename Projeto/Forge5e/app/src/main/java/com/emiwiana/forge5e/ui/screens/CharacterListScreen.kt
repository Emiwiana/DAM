package com.emiwiana.forge5e.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emiwiana.forge5e.ui.components.character.main.CharacterItem
import com.emiwiana.forge5e.viewModel.CharacterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterListScreen(
    viewModel: CharacterViewModel,
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToBuilder: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val characters by viewModel.allCharacters.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var newCharacterName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Characters") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToBuilder) {
                        Icon(Icons.Default.Build, contentDescription = "Character Builder")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Character")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(characters) { character ->
                CharacterItem(
                    character = character,
                    onDelete = { viewModel.deleteCharacter(character) },
                    onClick = { onNavigateToDetail(character.id) }
                )
            }
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Quick Character Creation") },
                text = {
                    TextField(
                        value = newCharacterName,
                        onValueChange = { newCharacterName = it },
                        label = { Text("Character Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newCharacterName.isNotBlank()) {
                                viewModel.addCharacter(newCharacterName)
                                newCharacterName = ""
                                showAddDialog = false
                            }
                        }
                    ) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

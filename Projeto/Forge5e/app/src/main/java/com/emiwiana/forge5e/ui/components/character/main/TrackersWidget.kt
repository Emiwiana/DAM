package com.emiwiana.forge5e.ui.components.character.main

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emiwiana.forge5e.model.db.CharacterTrackerEntity
import com.emiwiana.forge5e.viewModel.CharacterDetailViewModel

@Composable
fun TrackersWidget(viewModel: CharacterDetailViewModel) {
    val trackers by viewModel.trackers.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        trackers.forEach { tracker ->
            TrackerItem(tracker, viewModel)
        }

        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Custom Tracker")
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var max by remember { mutableStateOf("1") }
        var resetType by remember { mutableStateOf("Long Rest") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Custom Tracker") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(value = name, onValueChange = { name = it }, label = { Text("Tracker Name") })
                    TextField(
                        value = max,
                        onValueChange = { if (it.all { c -> c.isDigit() }) max = it },
                        label = { Text("Max Uses") }
                    )
                    Text("Resets on:", style = MaterialTheme.typography.labelMedium)
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
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            viewModel.addTracker(name, max.toIntOrNull() ?: 1, resetType)
                            showAddDialog = false
                        }
                    }
                ) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun TrackerItem(tracker: CharacterTrackerEntity, viewModel: CharacterDetailViewModel) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(tracker.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                IconButton(onClick = { viewModel.removeTracker(tracker) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Tracker")
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${tracker.current} / ${tracker.max}", style = MaterialTheme.typography.headlineSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { viewModel.updateTracker(tracker, tracker.current - 1) },
                        enabled = tracker.current > 0
                    ) {
                        Text("Use")
                    }
                    OutlinedButton(
                        onClick = { viewModel.updateTracker(tracker, tracker.current + 1) },
                        enabled = tracker.current < tracker.max
                    ) {
                        Text("Refill")
                    }
                }
            }
            Text("Resets on ${tracker.resetType}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
        }
    }
}

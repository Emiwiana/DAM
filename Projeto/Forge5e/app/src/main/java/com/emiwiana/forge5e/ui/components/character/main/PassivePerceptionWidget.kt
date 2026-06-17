package com.emiwiana.forge5e.ui.components.character.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emiwiana.forge5e.viewModel.CharacterDetailViewModel

@Composable
fun PassivePerceptionWidget(viewModel: CharacterDetailViewModel) {
    val passivePerception by viewModel.passivePerception.collectAsState()
    
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Passive Perception", style = MaterialTheme.typography.labelSmall)
            Text("$passivePerception", style = MaterialTheme.typography.titleLarge)
        }
    }
}

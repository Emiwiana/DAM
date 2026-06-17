package com.emiwiana.forge5e.ui.components.character.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AbilityScoreItem(label: String, score: Int) {
    val modifier = (score - 10) / 2
    val modSign = if (modifier >= 0) "+" else ""
    Card(modifier = Modifier.width(90.dp)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Text("$score", style = MaterialTheme.typography.titleLarge)
            Text("$modSign$modifier", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}

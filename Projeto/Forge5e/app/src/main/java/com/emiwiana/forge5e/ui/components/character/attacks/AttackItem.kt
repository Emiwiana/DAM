package com.emiwiana.forge5e.ui.components.character.attacks

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.emiwiana.forge5e.viewModel.AttackInfo

@Composable
fun AttackItem(attack: AttackInfo) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(attack.name, style = MaterialTheme.typography.titleMedium)
                Text("${attack.damage ?: "No Damage"} ${attack.damageType ?: ""}", color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(attack.range ?: "", style = MaterialTheme.typography.labelMedium)
                Text(attack.source, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

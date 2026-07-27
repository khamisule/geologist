package tz.geologist.features.rockid

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tz.geologist.data.remote.GeoApiClient

/**
 * M4 · Mineral Identification (determinative key).
 * Ingiza sifa ulizoona (hardness, streak, luster, color, magnetic) → mfumo unatoa
 * madini yanayolingana zaidi. (Utambuzi wa PICHA unahitaji model — angalia ml/DATASET_PLAN.md.)
 */
@Composable
fun MineralIdScreen(api: GeoApiClient = remember { GeoApiClient() }) {
    val scope = rememberCoroutineScope()

    var hardness by remember { mutableStateOf(6f) }
    var streak by remember { mutableStateOf("white") }
    var luster by remember { mutableStateOf("vitreous") }
    var color by remember { mutableStateOf("") }
    var magnetic by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf<List<Pair<String, Double>>>(emptyList()) }
    var busy by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState())) {
        Text("Utambuzi wa Madini", style = MaterialTheme.typography.titleLarge)
        Text("Ingiza sifa ulizoona shambani (determinative key)", style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(Modifier.height(16.dp))
        Text("Hardness (Mohs): %.1f".format(hardness))
        Slider(value = hardness, onValueChange = { hardness = it }, valueRange = 1f..10f, steps = 17)

        Spacer(Modifier.height(8.dp))
        Picker("Streak", listOf("white", "black", "red brown", "green", "yellow brown", "gray"), streak) { streak = it }
        Picker("Luster", listOf("vitreous", "metallic", "dull", "pearly", "resinous", "adamantine"), luster) { luster = it }

        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = color, onValueChange = { color = it },
            label = { Text("Color (mfano: blue, brass yellow)") }, modifier = Modifier.fillMaxWidth())

        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Switch(checked = magnetic, onCheckedChange = { magnetic = it })
            Spacer(Modifier.width(8.dp)); Text("Magnetic")
        }

        Spacer(Modifier.height(16.dp))
        Button(
            enabled = !busy,
            onClick = {
                busy = true
                scope.launch {
                    val r = withContext(Dispatchers.IO) {
                        runCatching {
                            api.identifyMineral(hardness.toDouble(), streak, luster,
                                color.ifBlank { null }, magnetic)
                        }.getOrDefault(emptyList())
                    }
                    results = r; busy = false
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (busy) "Inatambua..." else "Tambua madini") }

        Spacer(Modifier.height(16.dp))
        results.forEachIndexed { i, (name, score) ->
            ListItem(
                headlineContent = { Text(name, fontWeight = if (i == 0) FontWeight.Bold else FontWeight.Normal) },
                trailingContent = { Text("score $score") }
            )
            HorizontalDivider()
        }
        if (results.isEmpty() && !busy) {
            Text("(Bonyeza \"Tambua\" — inahitaji backend/LAN)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Picker(label: String, options: List<String>, selected: String, onSelect: (String) -> Unit) {
    var open by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = open, onExpandedChange = { open = it }) {
        OutlinedTextField(
            value = selected, onValueChange = {}, readOnly = true, label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = open) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            options.forEach { o ->
                DropdownMenuItem(text = { Text(o) }, onClick = { onSelect(o); open = false })
            }
        }
    }
}

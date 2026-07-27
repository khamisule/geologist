package tz.geologist.features.assistant

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
 * M7 · Geology Assistant (reference lookup).
 * Tafuta madini kwa jina/rangi/commodity → ona sifa kamili (hardness, streak, luster,
 * colors, notes za Tanzania). Hii ni reference halisi kutoka DB.
 * (Chat-AI ya on-device/LLM — inakuja; angalia ROADMAP.)
 */
@Composable
fun AssistantScreen(api: GeoApiClient = remember { GeoApiClient() }) {
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<GeoApiClient.MineralInfo>>(emptyList()) }
    var loaded by remember { mutableStateOf(false) }

    fun search() {
        scope.launch {
            results = withContext(Dispatchers.IO) {
                runCatching { api.searchMinerals(query) }.getOrDefault(emptyList())
            }
            loaded = true
        }
    }
    LaunchedEffect(Unit) { search() }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Msaidizi wa Jiolojia", style = MaterialTheme.typography.titleLarge)
        Text("Tafuta madini kwa jina, rangi, au commodity", style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            OutlinedTextField(query, { query = it }, label = { Text("mfano: gold, blue, Li, tanzanite") },
                modifier = Modifier.weight(1f), singleLine = true)
            Spacer(Modifier.width(8.dp))
            Button(onClick = { search() }) { Text("Tafuta") }
        }

        Spacer(Modifier.height(12.dp))
        if (loaded && results.isEmpty()) {
            Text("(Hakuna matokeo — au backend haipatikani)",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(results) { m ->
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Text(m.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text(m.formula, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(6.dp))
                        Prop("Hardness", m.hardness)
                        Prop("Streak", m.streak)
                        Prop("Luster", m.luster)
                        Prop("Colors", m.colors)
                        Prop("Commodity", m.commodity)
                        if (m.tzNotes.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(m.tzNotes, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Prop(label: String, value: String) {
    if (value.isBlank()) return
    Row(Modifier.padding(vertical = 1.dp)) {
        Text("$label: ", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
        Text(value, style = MaterialTheme.typography.bodySmall)
    }
}

package tz.geologist.features.projects

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tz.geologist.data.remote.GeoApiClient

/**
 * M8 · Projects (mobile view). Onyesha projects kutoka backend, unda project mpya
 * (field_lead/admin). Missions/tasks za kina zinaweza kuongezwa hapa baadaye.
 */
@Composable
fun ProjectsScreen(api: GeoApiClient = remember { GeoApiClient() }) {
    val scope = rememberCoroutineScope()
    var projects by remember { mutableStateOf<List<GeoApiClient.Project>>(emptyList()) }
    var newName by remember { mutableStateOf("") }
    var msg by remember { mutableStateOf<String?>(null) }

    fun load() {
        scope.launch {
            projects = withContext(Dispatchers.IO) {
                runCatching { api.getProjects() }.getOrDefault(emptyList())
            }
        }
    }
    LaunchedEffect(Unit) { load() }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Miradi", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            OutlinedTextField(value = newName, onValueChange = { newName = it },
                label = { Text("Jina la mradi mpya") }, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            Button(
                enabled = newName.isNotBlank(),
                onClick = {
                    val n = newName
                    scope.launch {
                        val ok = withContext(Dispatchers.IO) {
                            runCatching { api.createProject(n, null) }.getOrDefault(false)
                        }
                        msg = if (ok) "Imeundwa ✓" else "Imeshindwa (ruhusa? login?)"
                        if (ok) { newName = ""; load() }
                    }
                }
            ) { Text("Unda") }
        }
        msg?.let { Spacer(Modifier.height(8.dp)); Text(it, color = MaterialTheme.colorScheme.primary) }

        Spacer(Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(projects) { p ->
                ListItem(
                    headlineContent = { Text(p.name) },
                    supportingContent = { Text("${p.region ?: "—"} · ${p.stage}") }
                )
                HorizontalDivider()
            }
        }
        if (projects.isEmpty()) {
            Text("(Hakuna projects bado — unda moja hapo juu)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

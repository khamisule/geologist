package tz.geologist.features.coredrill

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tz.geologist.core.lastKnownLocation
import tz.geologist.data.remote.GeoApiClient

/**
 * M5 · Core & Drilling Logging.
 * Rekodi intervals za core (depth from/to, lithology, RQD, recovery). Kila interval
 * huhifadhiwa na inaweza kutumwa kwa backend (field-report). Strip-log view rahisi.
 */
data class CoreInterval(val from: Double, val to: Double, val lithology: String,
                        val rqd: Int?, val recovery: Int?)

@Composable
fun CoreLogScreen(api: GeoApiClient = remember { GeoApiClient() }) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var holeId by remember { mutableStateOf("") }
    var from by remember { mutableStateOf("") }
    var to by remember { mutableStateOf("") }
    var lithology by remember { mutableStateOf("") }
    var rqd by remember { mutableStateOf("") }
    var recovery by remember { mutableStateOf("") }
    var intervals by remember { mutableStateOf<List<CoreInterval>>(emptyList()) }
    var msg by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Kumbukumbu ya Core / Drilling", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(holeId, { holeId = it }, label = { Text("Hole ID (mfano DDH-001)") },
            modifier = Modifier.fillMaxWidth())

        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(from, { from = it }, label = { Text("Kutoka (m)") }, modifier = Modifier.weight(1f))
            OutlinedTextField(to, { to = it }, label = { Text("Hadi (m)") }, modifier = Modifier.weight(1f))
        }
        OutlinedTextField(lithology, { lithology = it }, label = { Text("Lithology") }, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(rqd, { rqd = it }, label = { Text("RQD %") }, modifier = Modifier.weight(1f))
            OutlinedTextField(recovery, { recovery = it }, label = { Text("Recovery %") }, modifier = Modifier.weight(1f))
        }

        Spacer(Modifier.height(8.dp))
        Button(
            enabled = from.toDoubleOrNull() != null && to.toDoubleOrNull() != null && lithology.isNotBlank(),
            onClick = {
                intervals = intervals + CoreInterval(
                    from.toDouble(), to.toDouble(), lithology, rqd.toIntOrNull(), recovery.toIntOrNull())
                from = to; to = ""; lithology = ""; rqd = ""; recovery = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Ongeza interval") }

        if (intervals.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            Button(
                onClick = {
                    val loc = lastKnownLocation(ctx)
                    scope.launch {
                        var ok = 0
                        withContext(Dispatchers.IO) {
                            intervals.forEach { iv ->
                                val litho = "core $holeId [${iv.from}-${iv.to}m]: ${iv.lithology}" +
                                    (iv.rqd?.let { " RQD $it%" } ?: "") + (iv.recovery?.let { " rec $it%" } ?: "")
                                val sent = runCatching {
                                    api.postFieldReport(null, loc?.first ?: 0.0, loc?.second ?: 0.0, litho, "core log")
                                }.getOrDefault(false)
                                if (sent) ok++
                            }
                        }
                        msg = "Zimetumwa $ok/${intervals.size} intervals"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Tuma intervals zote kwa backend") }
        }
        msg?.let { Spacer(Modifier.height(6.dp)); Text(it, color = MaterialTheme.colorScheme.primary) }

        Spacer(Modifier.height(12.dp))
        Text("Strip log (${intervals.size} intervals)", style = MaterialTheme.typography.labelLarge)
        LazyColumn(Modifier.heightIn(max = 300.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(intervals.sortedBy { it.from }) { iv ->
                ListItem(
                    headlineContent = { Text("${iv.from}–${iv.to} m · ${iv.lithology}",
                        fontFamily = FontFamily.Monospace) },
                    supportingContent = { Text("RQD ${iv.rqd ?: "–"}% · rec ${iv.recovery ?: "–"}%") }
                )
                HorizontalDivider()
            }
        }
    }
}

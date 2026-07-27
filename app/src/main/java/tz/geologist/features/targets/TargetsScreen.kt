package tz.geologist.features.targets

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * M6 · Targets — inaonyesha prospectivity candidates HALISI za satellite.
 * Geologist anachagua eneo, anaona points bora, na anaweza kufungua
 * navigation (geo: intent) kwenda ku-validate shambani.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TargetsScreen(vm: TargetsViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    val ctx = LocalContext.current

    Column(Modifier.fillMaxSize()) {

        // Kichwa + caveat
        Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
            Column(Modifier.padding(16.dp, 12.dp)) {
                Text("Prospectivity Targets", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Kutoka Sentinel-2 (alteration signatures). Uwezekano, SI uthibitisho — thibitisha shambani.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Chagua eneo
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TargetRegion.entries.forEach { r ->
                FilterChip(
                    selected = state.region == r,
                    onClick = { vm.select(r) },
                    label = { Text(r.label, maxLines = 1) }
                )
            }
        }

        when {
            state.loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            state.error != null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("Hitilafu: ${state.error}", color = MaterialTheme.colorScheme.error)
            }
            else -> {
                Text(
                    "Targets ${state.candidates.size} · juu (≥85%): ${state.highPriorityCount} · chanzo: ${state.sourceLabel}",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(16.dp, 4.dp)
                )
                LazyColumn(
                    Modifier.fillMaxSize().padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(state.candidates) { c ->
                        TargetRow(c) {
                            val uri = Uri.parse("geo:${c.lat},${c.lon}?q=${c.lat},${c.lon}(Target #${c.rank} · ${c.scorePct}%)")
                            runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TargetRow(c: TargetCandidate, onNavigate: () -> Unit) {
    val color = when (c.priority) {
        TargetCandidate.Priority.VERY_HIGH -> Color(0xFFB3261E)
        TargetCandidate.Priority.HIGH -> Color(0xFFE4761B)
        TargetCandidate.Priority.MODERATE -> Color(0xFF3D6B39)
    }
    ElevatedCard(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {

            // Score badge
            Surface(color = color, shape = RoundedCornerShape(10.dp)) {
                Column(
                    Modifier.width(56.dp).padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("${c.scorePct}%", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("score", color = Color.White, style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(Modifier.weight(1f)) {
                Text("Target #${c.rank}", fontWeight = FontWeight.SemiBold)
                Text(
                    "lat ${"%.5f".format(c.lat)},  lon ${"%.5f".format(c.lon)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            FilledTonalButton(onClick = onNavigate) { Text("Nenda") }
        }
    }
}

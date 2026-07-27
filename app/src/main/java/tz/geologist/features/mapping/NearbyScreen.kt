package tz.geologist.features.mapping

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tz.geologist.core.lastKnownLocation
import tz.geologist.data.remote.GeoApiClient

/**
 * M1 (lite) · Targets karibu nami.
 * Inatumia GPS ya simu + endpoint /api/targets/near kuonyesha targets zilizo karibu,
 * zikipangwa kwa umbali. (Ramani kamili ya offline — MapLibre — inakuja.)
 */
@Composable
fun NearbyScreen(api: GeoApiClient = remember { GeoApiClient() }) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var lat by remember { mutableStateOf<Double?>(null) }
    var lon by remember { mutableStateOf<Double?>(null) }
    var targets by remember { mutableStateOf<List<GeoApiClient.NearTarget>>(emptyList()) }
    var msg by remember { mutableStateOf<String?>(null) }

    fun refresh() {
        val loc = lastKnownLocation(ctx)
        if (loc == null) { msg = "GPS haipatikani (ruhusu location)"; return }
        lat = loc.first; lon = loc.second; msg = null
        scope.launch {
            targets = withContext(Dispatchers.IO) {
                runCatching { api.getTargetsNear(loc.first, loc.second, radiusKm = 15.0) }.getOrDefault(emptyList())
            }
            if (targets.isEmpty()) msg = "Hakuna targets ndani ya 15km (au backend haipatikani)"
        }
    }

    LaunchedEffect(Unit) { refresh() }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Targets karibu nami", style = MaterialTheme.typography.titleLarge)
        Text(
            if (lat != null) "GPS: %.5f, %.5f".format(lat, lon) else "…",
            style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Button(onClick = { refresh() }) { Text("Sasisha GPS + targets") }
        Spacer(Modifier.height(12.dp))

        msg?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }

        // Offline spatial mini-map (Canvas — hakuna tiles, inafanya kazi bila mtandao)
        if (lat != null && targets.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            MiniMap(lat!!, lon!!, targets)
            Spacer(Modifier.height(8.dp))
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(targets) { t ->
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Target #${t.rank}  ·  ${(t.score * 100).toInt()}%")
                            Text("%.5f, %.5f".format(t.lat, t.lon),
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("%.2f km".format(t.distanceKm), style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}

/**
 * Ramani ndogo ya offline: wewe katikati (buluu), targets kuzunguka kwa nafasi halisi
 * (lat/lon scaled). Rangi kwa score. Hakuna internet/tiles inayohitajika.
 */
@Composable
private fun MiniMap(uLat: Double, uLon: Double, targets: List<GeoApiClient.NearTarget>) {
    val maxDx = (targets.maxOf { kotlin.math.abs(it.lon - uLon) }).coerceAtLeast(1e-4)
    val maxDy = (targets.maxOf { kotlin.math.abs(it.lat - uLat) }).coerceAtLeast(1e-4)
    val span = maxOf(maxDx, maxDy) * 1.2
    Surface(
        Modifier.fillMaxWidth().height(220.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium
    ) {
        Canvas(Modifier.fillMaxSize().padding(12.dp)) {
            val cx = size.width / 2f; val cy = size.height / 2f
            val r = minOf(cx, cy)
            // pete za umbali
            listOf(0.33f, 0.66f, 1f).forEach {
                drawCircle(Color(0x22000000), radius = r * it, center = Offset(cx, cy), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f))
            }
            targets.forEach { t ->
                val px = cx + ((t.lon - uLon) / span * r).toFloat()
                val py = cy - ((t.lat - uLat) / span * r).toFloat()   // kaskazini juu
                val col = when {
                    t.score >= 0.9 -> Color(0xFFB3261E)
                    t.score >= 0.85 -> Color(0xFFE4761B)
                    else -> Color(0xFF3d6b39)
                }
                drawCircle(col, radius = 6f, center = Offset(px, py))
            }
            // wewe (katikati)
            drawCircle(Color(0xFF1560D8), radius = 9f, center = Offset(cx, cy))
        }
    }
}

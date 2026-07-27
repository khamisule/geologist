package tz.geologist.features.sampling

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tz.geologist.data.remote.GeoApiClient

/**
 * M3 · Sample & Outcrop Logging.
 * Rekodi sample yenye GPS (auto), aina, lithology, na note → inasync na backend.
 * Offline-first: ikishindwa kutuma, ihifadhiwe local (TODO: outbox) na isync baadaye.
 */
@Composable
fun SamplingScreen(api: GeoApiClient = remember { GeoApiClient() }) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var type by remember { mutableStateOf("rock") }
    var lithology by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var status by remember { mutableStateOf<String?>(null) }

    val loc = remember { lastKnownLocation(ctx) }
    var lat by remember { mutableStateOf(loc?.first) }
    var lon by remember { mutableStateOf(loc?.second) }

    Column(
        Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState())
    ) {
        Text("Kurekodi Sampuli", style = MaterialTheme.typography.titleLarge)
        Text(
            if (lat != null) "GPS: %.5f, %.5f".format(lat, lon)
            else "GPS haipatikani (ruhusu location / subiri fix)",
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(16.dp))
        Text("Aina ya sample", style = MaterialTheme.typography.labelMedium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("soil", "rock", "stream", "chip", "core").forEach { t ->
                FilterChip(selected = type == t, onClick = { type = t }, label = { Text(t) })
            }
        }

        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = lithology, onValueChange = { lithology = it },
            label = { Text("Lithology (mfano: quartz vein, granite)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = note, onValueChange = { note = it },
            label = { Text("Note / mineralization / alteration") },
            modifier = Modifier.fillMaxWidth(), minLines = 3
        )

        Spacer(Modifier.height(20.dp))
        Button(
            enabled = lat != null,
            onClick = {
                val la = lat ?: return@Button; val lo = lon ?: return@Button
                val litho = "$type: $lithology"
                scope.launch {
                    val ok = withContext(Dispatchers.IO) {
                        runCatching { api.postFieldReport(null, la, lo, litho, note) }.getOrDefault(false)
                    }
                    status = if (ok) "Sample imerekodiwa ✓" else "Imeshindwa (login? mtandao?) — hifadhi na jaribu tena"
                    if (ok) { lithology = ""; note = "" }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Rekodi sample") }

        Button(
            onClick = { lastKnownLocation(ctx)?.let { lat = it.first; lon = it.second } },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) { Text("Sasisha GPS") }

        status?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@SuppressLint("MissingPermission")
private fun lastKnownLocation(ctx: Context): Pair<Double, Double>? {
    val ok = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED
    if (!ok) return null
    val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val provs = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
    for (p in provs) {
        val l = runCatching { lm.getLastKnownLocation(p) }.getOrNull()
        if (l != null) return l.latitude to l.longitude
    }
    return null
}

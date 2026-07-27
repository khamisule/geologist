package tz.geologist.features.safety

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tz.geologist.core.Config
import tz.geologist.core.lastKnownLocation
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * M10 · Safety & Sync.
 * - Shiriki lokesheni yako na base (SOS).
 * - Piga simu ya dharura (inafungua dialer — hakuna kupiga otomatiki).
 * - Angalia hali ya backend/LAN (sync).
 */
@Composable
fun SafetyScreen() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val loc = remember { lastKnownLocation(ctx) }
    var backendUp by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) {
        backendUp = withContext(Dispatchers.IO) {
            runCatching {
                val http = OkHttpClient()
                http.newCall(Request.Builder().url("${Config.BASE_URL}/api/health").build())
                    .execute().use { it.isSuccessful }
            }.getOrDefault(false)
        }
    }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("Usalama & Sync", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            if (loc != null) "Lokesheni yako: %.5f, %.5f".format(loc.first, loc.second)
            else "GPS haipatikani (ruhusu location)",
            fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(24.dp))
        Button(
            enabled = loc != null,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB3261E)),
            modifier = Modifier.fillMaxWidth().height(56.dp),
            onClick = {
                loc?.let { (la, lo) ->
                    val text = "SOS — geologist anahitaji msaada. Lokesheni: $la, $lo\ngeo:$la,$lo"
                    val send = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text)
                    }
                    runCatching { ctx.startActivity(Intent.createChooser(send, "Shiriki SOS")) }
                }
            }
        ) { Text("🆘  SHIRIKI SOS (lokesheni)") }

        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            modifier = Modifier.fillMaxWidth().height(52.dp),
            onClick = {
                // Inafungua dialer tu — hakuna kupiga otomatiki (usalama)
                runCatching { ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:112"))) }
            }
        ) { Text("📞  Simu ya dharura (112)") }

        Spacer(Modifier.height(28.dp))
        Text("Hali ya sync (backend / LAN)", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(6.dp))
        val (label, col) = when (backendUp) {
            true -> "Imeunganishwa ✓ (${Config.BASE_URL})" to Color(0xFF3d6b39)
            false -> "Haijaunganishwa — offline (data itahifadhiwa)" to Color(0xFFB3261E)
            null -> "Inaangalia…" to MaterialTheme.colorScheme.onSurfaceVariant
        }
        Text(label, color = col)
    }
}

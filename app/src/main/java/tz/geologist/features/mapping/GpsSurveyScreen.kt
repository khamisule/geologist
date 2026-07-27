package tz.geologist.features.mapping

import android.annotation.SuppressLint
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import tz.geologist.core.GeoArea
import tz.geologist.core.captureLocation

/**
 * Survey ya Kutembea Mpaka (GPS) — M3+.
 *
 * Tembea kwenye eneo; kwenye KILA kona bonyeza "📍 Rekodi kona hapa" → GPS
 * inarekodi coordinate yenyewe (lat/lon + usahihi ± mita). Ukimaliza, app
 * inahesabu ENEO (ha) + mzunguko na kuonyesha kwenye ramani ya satelaiti.
 *
 * ONYO wa uwazi: GPS ya simu ni ± mita 3–10 (si survey-grade). Kwa mipaka RASMI
 * ya leseni, tumia DGPS/RTK + surveyor aliyeidhinishwa.
 */
data class SurveyPoint(val lat: Double, val lon: Double, val accM: Float?)

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun GpsSurveyScreen() {
    val ctx = LocalContext.current
    val points = remember { mutableStateListOf<SurveyPoint>() }
    var status by remember { mutableStateOf("Bonyeza \"Rekodi kona\" kwenye kila kona ya eneo.") }
    var capturing by remember { mutableStateOf(false) }
    var showMap by remember { mutableStateOf(false) }

    val geoPts = points.map { GeoArea.Pt(it.lat, it.lon) }
    val res = if (geoPts.size >= 3) GeoArea.areaPerimeter(geoPts) else null

    if (showMap && res != null) {
        SurveyMapView(GeoArea.toPolyParam(res.points)) { showMap = false }
        return
    }

    fun capture() {
        capturing = true
        status = "Ninasoma GPS… (simama tuli sekunde chache)"
        captureLocation(ctx,
            onResult = { lat, lon, acc ->
                points.add(SurveyPoint(lat, lon, acc))
                capturing = false
                status = "Kona ${points.size} imerekodiwa" + (acc?.let { " (±%.0f m)".format(it) } ?: "")
            },
            onError = { msg -> capturing = false; status = "Hitilafu: $msg" }
        )
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("🚶 Survey ya Kutembea Mpaka (GPS)", style = MaterialTheme.typography.titleLarge)
        Text(status, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))

        Spacer(Modifier.height(12.dp))
        Button(onClick = { capture() }, enabled = !capturing, modifier = Modifier.fillMaxWidth()) {
            Text(if (capturing) "Inasoma GPS…" else "📍 Rekodi kona hapa")
        }
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { if (points.isNotEmpty()) points.removeAt(points.size - 1) },
                enabled = points.isNotEmpty(), modifier = Modifier.weight(1f)) { Text("↩ Futa ya mwisho") }
            OutlinedButton(onClick = { points.clear(); status = "Imeanzishwa upya." },
                enabled = points.isNotEmpty(), modifier = Modifier.weight(1f)) { Text("Anza upya") }
        }

        Spacer(Modifier.height(12.dp))

        // Matokeo (yanapatikana ukishakuwa na kona 3+)
        if (res != null) {
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("%.4f hekta".format(GeoArea.hectares(res.areaM2)),
                        fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
                    Text("%,.0f m² · %.3f acres · mzunguko %.3f km".format(
                        res.areaM2, GeoArea.acres(res.areaM2), GeoArea.km(res.perimM)),
                        style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { showMap = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("🛰️  Onyesha eneo kwenye satelaiti")
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        } else {
            Text("Kona zilizorekodiwa: ${points.size} (zinahitajika 3+ kupima eneo).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
        }

        // Orodha ya kona
        LazyColumn(Modifier.weight(1f)) {
            itemsIndexed(points) { i, p ->
                ListItem(
                    headlineContent = { Text("Kona ${i + 1}") },
                    supportingContent = {
                        Text("%.6f, %.6f".format(p.lat, p.lon) + (p.accM?.let { "  ±%.0f m".format(it) } ?: ""),
                            fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    }
                )
                Divider()
            }
        }

        Text(
            "⚠️ GPS ya simu ni ± mita 3–10 (si survey-grade). Kwa mipaka RASMI ya leseni, " +
                "tumia DGPS/RTK + surveyor aliyeidhinishwa. Simama tuli kila kona kupata usahihi bora.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun SurveyMapView(polyParam: String, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Surface(color = MaterialTheme.colorScheme.primary, tonalElevation = 2.dp) {
            Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack) { Text("‹ Rudi", color = MaterialTheme.colorScheme.onPrimary) }
                Text("🛰️ Eneo la survey", color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleMedium)
            }
        }
        AndroidView(modifier = Modifier.fillMaxSize(), factory = { c ->
            WebView(c).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                settings.allowFileAccess = true
                settings.domStorageEnabled = true
                settings.setSupportZoom(true)
                settings.builtInZoomControls = true
                settings.displayZoomControls = false
                loadUrl("file:///android_asset/map.html?poly=" + Uri.encode(polyParam))
            }
        })
    }
}

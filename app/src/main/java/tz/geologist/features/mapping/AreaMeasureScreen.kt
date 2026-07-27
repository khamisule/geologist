package tz.geologist.features.mapping

import android.annotation.SuppressLint
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import tz.geologist.core.GeoArea

/**
 * "Pima Eneo" — ingiza point za coordinate (DMS au decimal), app inachora poligoni
 * kwenye ramani ya satelaiti na kupima ENEO (ha / m² / acres) + mzunguko.
 *
 * Inafanya kazi OFFLINE (hesabu ni ndani ya app). Ramani ya satelaiti inahitaji
 * internet mara ya kwanza kisha ina-cache.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AreaMeasureScreen() {
    val sample = """
        1
        05° 11' 56.09" S
        036° 28' 05.13" E
        2
        05° 11' 56.32" S
        036° 28' 19.50" E
        3
        05° 12' 05.71" S
        036° 28' 19.73" E
        4
        05° 12' 00.86" S
        036° 28' 12.24" E
        5
        05° 11' 57.40" S
        036° 28' 05.29" E
    """.trimIndent()

    var text by remember { mutableStateOf(sample) }
    var result by remember { mutableStateOf<GeoArea.Result?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var showMap by remember { mutableStateOf(false) }

    fun compute() {
        val pts = GeoArea.parseCoordinates(text)
        if (pts.size < 3) {
            error = "Point pungufu: nimepata ${pts.size}. Zinahitajika angalau 3."
            result = null; return
        }
        error = null
        result = GeoArea.areaPerimeter(pts)
    }

    val res = result
    if (showMap && res != null) {
        AreaMapView(GeoArea.toPolyParam(res.points)) { showMap = false }
        return
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("📐 Pima Eneo", style = MaterialTheme.typography.titleLarge)
        Text(
            "Ingiza point za pembe za eneo (DMS mfano 05° 11' 56\" S, au decimal -5.1989, 36.4681). " +
                "Kila point: lat kisha lon.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Point za coordinate") },
            modifier = Modifier.fillMaxWidth().heightIn(min = 160.dp),
            textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            keyboardOptions = KeyboardOptions.Default
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { compute() }) { Text("Pima eneo") }
            OutlinedButton(onClick = { text = ""; result = null; error = null }) { Text("Futa") }
        }

        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }

        if (res != null) {
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Eneo lililopimwa", style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("%.4f hekta".format(GeoArea.hectares(res.areaM2)),
                        fontSize = 26.sp, style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary)
                    Text("%,.0f m²  ·  %.3f acres".format(res.areaM2, GeoArea.acres(res.areaM2)),
                        style = MaterialTheme.typography.bodyMedium)
                    Divider(Modifier.padding(vertical = 6.dp))
                    Text("Mzunguko (perimeter): %.4f km  (%,.0f m)".format(
                        GeoArea.km(res.perimM), res.perimM),
                        style = MaterialTheme.typography.bodyMedium)
                    Text("Point: ${res.points.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { showMap = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("🛰️  Onyesha kwenye ramani ya satelaiti")
                    }
                }
            }
            Text(
                "Makadirio ya ellipsoid (WGS84) — sahihi kwa maeneo ya kawaida. " +
                    "Kwa mipaka rasmi ya leseni, thibitisha na survey rasmi.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun AreaMapView(polyParam: String, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Surface(color = MaterialTheme.colorScheme.primary, tonalElevation = 2.dp) {
            Row(Modifier.fillMaxWidth().padding(10.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                TextButton(onClick = onBack) {
                    Text("‹ Rudi", color = MaterialTheme.colorScheme.onPrimary)
                }
                Text("🛰️ Eneo kwenye satelaiti", color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleMedium)
            }
        }
        AndroidView(modifier = Modifier.fillMaxSize(), factory = { ctx ->
            WebView(ctx).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                settings.allowFileAccess = true
                settings.domStorageEnabled = true
                settings.setSupportZoom(true)
                settings.builtInZoomControls = true
                settings.displayZoomControls = false
                val url = "file:///android_asset/map.html?poly=" + Uri.encode(polyParam)
                loadUrl(url)
            }
        })
    }
}

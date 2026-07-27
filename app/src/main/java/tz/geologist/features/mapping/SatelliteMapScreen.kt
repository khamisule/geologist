package tz.geologist.features.mapping

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Ramani ya Satelaiti HALISI (M1 kamili).
 *
 * WebView inapakia `assets/map.html` (Leaflet + Esri World Imagery = satelaiti halisi)
 * ikisoma targets zilizofungwa ndani ya app (`assets/targets_seed/{region}.geojson`) —
 * markers zenye rangi kwa score + ramani ya joto (prospectivity ya Sentinel-2).
 *
 * - Inafanya kazi bila backend (data imo ndani ya app).
 * - Tiles za satelaiti zinahitaji internet mara ya kwanza, kisha WebView ina-cache.
 * - focusLat/focusLon: ukibonyeza target, ramani inaruka pale.
 */
/** Daraja la data: map.html inaita hii kupata geojson iliyofungwa (offline, ya uhakika). */
private class MapDataBridge(private val ctx: Context) {
    @JavascriptInterface
    fun getTargets(region: String): String {
        val safe = region.filter { it.isLetterOrDigit() }   // zuia path traversal
        return runCatching {
            ctx.assets.open("targets_seed/$safe.geojson").bufferedReader().use { it.readText() }
        }.getOrDefault("{\"type\":\"FeatureCollection\",\"features\":[]}")
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun SatelliteMapScreen(
    initialRegion: String = "dodoma",
    focusLat: Double? = null,
    focusLon: Double? = null,
) {
    var region by remember { mutableStateOf(initialRegion) }
    var webView by remember { mutableStateOf<WebView?>(null) }

    fun urlFor(r: String): String {
        val base = "file:///android_asset/map.html?region=$r"
        return if (focusLat != null && focusLon != null) "$base&lat=$focusLat&lon=$focusLon" else base
    }

    Column(Modifier.fillMaxSize()) {
        // Upau: kichwa + chagua eneo
        Surface(color = MaterialTheme.colorScheme.primary, tonalElevation = 2.dp) {
            Column(Modifier.fillMaxWidth().padding(12.dp)) {
                Text(
                    "🛰️ Ramani ya Satelaiti",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("manyara", "tanga", "dodoma").forEach { r ->
                        FilterChip(
                            selected = region == r,
                            onClick = {
                                region = r
                                webView?.loadUrl(urlFor(r))
                            },
                            label = { Text(r.replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }
            }
        }

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    webViewClient = WebViewClient()
                    addJavascriptInterface(MapDataBridge(ctx), "Android")
                    settings.javaScriptEnabled = true
                    settings.allowFileAccess = true
                    // Inaruhusu map.html (file://) ku-fetch geojson (file://) — offline
                    @Suppress("DEPRECATION")
                    settings.allowFileAccessFromFileURLs = true
                    @Suppress("DEPRECATION")
                    settings.allowUniversalAccessFromFileURLs = true
                    settings.domStorageEnabled = true
                    settings.setSupportZoom(true)
                    settings.builtInZoomControls = true
                    settings.displayZoomControls = false
                    // Cache ili tiles zilizopakuliwa zifanye kazi offline
                    @Suppress("DEPRECATION")
                    settings.cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
                    loadUrl(urlFor(initialRegion))
                    webView = this
                }
            }
        )
    }
}

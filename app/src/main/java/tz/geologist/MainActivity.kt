package tz.geologist

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.GeolocationPermissions
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.io.ByteArrayInputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Geologist — app kamili ndani ya WebView (assets/Geologist.html):
 * Ona Madini, Pima & Chunguza, GPS Survey, Historia. GPS + hifadhi zinafanya kazi.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var web: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            1
        )
        web = WebView(this)
        // WebViewClient yenye CACHE YA TILES: kila tile ya satelaiti/DEM ikishapakuliwa
        // huhifadhiwa diski (filesDir/tilecache) na kutumika OFFLINE bila kupakua tena.
        // Pia huongeza Access-Control-Allow-Origin ili uchambuzi wa nyufa (canvas) ufanye kazi.
        web.webViewClient = object : WebViewClient() {
            private val tileDir: File by lazy { File(filesDir, "tilecache").apply { mkdirs() } }
            private fun isTile(host: String): Boolean =
                host.contains("arcgisonline.com") || host.contains("mt1.google.com") ||
                host.contains("elevation-tiles-prod") || host.contains("amazonaws.com")

            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                val host = request.url.host ?: return null
                if (request.method != "GET" || !isTile(host)) return null
                val url = request.url.toString()
                val mime = if (url.contains("World_Imagery") || url.contains("lyrs=s")) "image/jpeg" else "image/png"
                val hdrs = hashMapOf(
                    "Access-Control-Allow-Origin" to "*",
                    "Cache-Control" to "max-age=31536000"
                )
                return try {
                    val name = Integer.toHexString(url.hashCode()) + "_" +
                        url.substringAfterLast('/').substringBefore('?').replace(Regex("[^A-Za-z0-9._-]"), "_")
                    val f = File(tileDir, name)
                    if (f.exists() && f.length() > 0) {
                        // OFFLINE: tumia nakala ya diski
                        return WebResourceResponse(mime, null, 200, "OK", hdrs, f.inputStream())
                    }
                    val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                        connectTimeout = 15000; readTimeout = 20000
                        instanceFollowRedirects = true
                        setRequestProperty("User-Agent", "Geologist/1.0")
                    }
                    conn.connect()
                    if (conn.responseCode in 200..299) {
                        val bytes = conn.inputStream.use { it.readBytes() }
                        try { f.writeBytes(bytes) } catch (_: Exception) {}   // hifadhi kwa offline
                        WebResourceResponse(mime, null, 200, "OK", hdrs, ByteArrayInputStream(bytes))
                    } else null
                } catch (e: Exception) {
                    null   // offline & haijahifadhiwa -> WebView itashughulikia (itashindwa kimya)
                }
            }
        }
        web.webChromeClient = object : WebChromeClient() {
            override fun onGeolocationPermissionsShowPrompt(
                origin: String?, callback: GeolocationPermissions.Callback?
            ) { callback?.invoke(origin, true, false) }
        }
        web.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            @Suppress("DEPRECATION") setGeolocationEnabled(true)
            allowFileAccess = true
            @Suppress("DEPRECATION") allowFileAccessFromFileURLs = true
            @Suppress("DEPRECATION") allowUniversalAccessFromFileURLs = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            cacheMode = WebSettings.LOAD_DEFAULT
            mediaPlaybackRequiresUserGesture = false
        }
        web.loadUrl("file:///android_asset/Geologist.html")
        setContentView(web)
    }

    // Kitufe cha SAUTI (+) huweka point ya GPS ukiwa shambani (rahisi kuliko kugusa skrini).
    override fun onKeyDown(keyCode: Int, event: android.view.KeyEvent?): Boolean {
        if (keyCode == android.view.KeyEvent.KEYCODE_VOLUME_UP && ::web.isInitialized) {
            web.evaluateJavascript("window.__volCapture && window.__volCapture();", null)
            return true  // tumia kitufe kwa kuweka point
        }
        return super.onKeyDown(keyCode, event)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (::web.isInitialized && web.canGoBack()) web.goBack() else super.onBackPressed()
    }
}

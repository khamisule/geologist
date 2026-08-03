package tz.geologist

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.webkit.GeolocationPermissions
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
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

    // Kupiga picha ya mwamba: <input type=file> kwenye WebView inahitaji onShowFileChooser + camera intent
    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private var cameraImageUri: Uri? = null
    private val fileChooserLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val cb = filePathCallback
        filePathCallback = null
        if (cb == null) return@registerForActivityResult
        var results: Array<Uri>? = null
        if (result.resultCode == RESULT_OK) {
            val dataUri = result.data?.data
            results = when {
                dataUri != null -> arrayOf(dataUri)                 // imechaguliwa kwenye gallery
                cameraImageUri != null -> arrayOf(cameraImageUri!!) // imepigwa kwa kamera
                else -> null
            }
        }
        cb.onReceiveValue(results)   // rudisha kwenye WebView (au null ikighairiwa)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CAMERA
            ),
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

            // Ruhusu maombi ya kamera/media kutoka kwa web content
            override fun onPermissionRequest(request: PermissionRequest?) {
                request?.grant(request.resources)
            }

            // <input type="file"> ya kupiga picha ya mwamba: fungua KAMERA + gallery
            override fun onShowFileChooser(
                webView: WebView?,
                callback: ValueCallback<Array<Uri>>?,
                params: FileChooserParams?
            ): Boolean {
                filePathCallback?.onReceiveValue(null)
                filePathCallback = callback
                // Hatua A: Intent ya KAMERA -- picha huhifadhiwa kwa FileProvider
                val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                cameraImageUri = null
                if (captureIntent.resolveActivity(packageManager) != null) {
                    try {
                        val dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                        if (dir != null && !dir.exists()) dir.mkdirs()
                        val photo = File.createTempFile("rock_", ".jpg", dir)
                        cameraImageUri = FileProvider.getUriForFile(
                            this@MainActivity, "$packageName.fileprovider", photo
                        )
                        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)
                        captureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    } catch (e: Exception) { cameraImageUri = null }
                }
                // Hatua B: Intent ya GALLERY
                val pickIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "image/*"; addCategory(Intent.CATEGORY_OPENABLE)
                }
                // Hatua C: Chooser -- kamera + gallery
                val chooser = Intent(Intent.ACTION_CHOOSER).apply {
                    putExtra(Intent.EXTRA_INTENT, pickIntent)
                    putExtra(Intent.EXTRA_TITLE, "Piga picha ya mwamba au chagua")
                    if (cameraImageUri != null) {
                        putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(captureIntent))
                    }
                }
                return try {
                    fileChooserLauncher.launch(chooser); true
                } catch (e: Exception) {
                    filePathCallback = null
                    Toast.makeText(this@MainActivity, "Imeshindwa kufungua kamera: ${e.message}", Toast.LENGTH_LONG).show()
                    false
                }
            }
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
        // Bridge ya kuhifadhi PDF (WebView haiwezi kupakua blob/doc.save())
        web.addJavascriptInterface(PdfBridge(), "AndroidBridge")
        web.loadUrl("file:///android_asset/Geologist.html")
        setContentView(web)
    }

    /** JS huita AndroidBridge.savePdf(jina, base64) -> huhifadhi PDF kwenye Downloads. */
    inner class PdfBridge {
        @JavascriptInterface
        fun savePdf(filename: String, b64: String) {
            try {
                val bytes = Base64.decode(b64, Base64.DEFAULT)
                val safe = filename.replace(Regex("[^A-Za-z0-9._-]"), "_").ifEmpty { "ripoti.pdf" }
                val name = if (safe.endsWith(".pdf")) safe else "$safe.pdf"
                if (Build.VERSION.SDK_INT >= 29) {
                    val cv = ContentValues().apply {
                        put(MediaStore.Downloads.DISPLAY_NAME, name)
                        put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                        put(MediaStore.Downloads.IS_PENDING, 1)
                    }
                    val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv)
                    if (uri != null) {
                        contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
                        cv.clear(); cv.put(MediaStore.Downloads.IS_PENDING, 0)
                        contentResolver.update(uri, cv, null, null)
                    }
                } else {
                    // Devices za zamani: app-specific external (bila ruhusa)
                    val dir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: filesDir
                    if (!dir.exists()) dir.mkdirs()
                    File(dir, name).writeBytes(bytes)
                }
                runOnUiThread { Toast.makeText(this@MainActivity, "✓ Ripoti imehifadhiwa: Downloads/$name", Toast.LENGTH_LONG).show() }
            } catch (e: Exception) {
                runOnUiThread { Toast.makeText(this@MainActivity, "Imeshindwa kuhifadhi PDF: ${e.message}", Toast.LENGTH_LONG).show() }
            }
        }
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

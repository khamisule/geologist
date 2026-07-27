package tz.geologist.features.app

import android.annotation.SuppressLint
import android.webkit.GeolocationPermissions
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/**
 * App KAMILI ya Geologist ndani ya WebView — inapakia `assets/Geologist.html`
 * yenye vipengele VYOTE: Ona Madini, Pima & Chunguza, GPS Survey, na Historia
 * (📋). Hii inahakikisha APK ina kila kitu kilichothibitishwa kwenye web app.
 *
 * - JavaScript + DOM storage (kumbukumbu zinadumu ndani ya app).
 * - Geolocation IMEWASHWA + ruhusa (GPS ya kweli kupitia OS ya Android).
 * - Ufikiaji wa assets (map tiles hu-cache; targets_seed offline).
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AppWebScreen() {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            WebView(ctx).apply {
                webViewClient = WebViewClient()
                // Ruhusu GPS ndani ya WebView (inatumia location ya OS — GPS halisi)
                webChromeClient = object : WebChromeClient() {
                    override fun onGeolocationPermissionsShowPrompt(
                        origin: String?, callback: GeolocationPermissions.Callback?
                    ) { callback?.invoke(origin, true, false) }
                }
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true            // localStorage -> kumbukumbu zadumu
                    databaseEnabled = true
                    setGeolocationEnabled(true)         // GPS
                    allowFileAccess = true
                    @Suppress("DEPRECATION")
                    allowFileAccessFromFileURLs = true
                    @Suppress("DEPRECATION")
                    allowUniversalAccessFromFileURLs = true
                    setSupportZoom(true)
                    builtInZoomControls = true
                    displayZoomControls = false
                    cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
                    mediaPlaybackRequiresUserGesture = false
                }
                loadUrl("file:///android_asset/Geologist.html")
            }
        }
    )
}

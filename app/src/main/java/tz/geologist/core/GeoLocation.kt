package tz.geologist.core

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

/**
 * Rekodi GPS fix MPYA yenye usahihi wa juu (kwa survey ya kutembea mpaka).
 * onResult(lat, lon, accuracyMeters?) — accuracy ni ± mita (usahihi wa GPS).
 */
@SuppressLint("MissingPermission")
fun captureLocation(
    ctx: Context,
    onResult: (lat: Double, lon: Double, accuracyM: Float?) -> Unit,
    onError: (String) -> Unit
) {
    val granted = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED
    if (!granted) { onError("Ruhusa ya location haijatolewa"); return }
    val client = LocationServices.getFusedLocationProviderClient(ctx)
    val cts = CancellationTokenSource()
    client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
        .addOnSuccessListener { loc ->
            if (loc != null) onResult(loc.latitude, loc.longitude, if (loc.hasAccuracy()) loc.accuracy else null)
            else onError("GPS haikupatikana — jaribu tena ukiwa nje/eneo wazi")
        }
        .addOnFailureListener { onError(it.message ?: "GPS imeshindwa") }
}

/** Msaidizi wa pamoja wa kupata last-known location (GPS/network). */
@SuppressLint("MissingPermission")
fun lastKnownLocation(ctx: Context): Pair<Double, Double>? {
    val granted = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED
    if (!granted) return null
    val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    for (p in listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)) {
        val l = runCatching { lm.getLastKnownLocation(p) }.getOrNull()
        if (l != null) return l.latitude to l.longitude
    }
    return null
}

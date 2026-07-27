package tz.geologist.features.compass

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tz.geologist.data.remote.GeoApiClient
import tz.geologist.sensors.CompassClinometer

/**
 * M2 · Digital Compass-Clinometer.
 * Weka simu bapa juu ya uso wa mwamba → strike/dip husomeka live kutoka sensor.
 * Bonyeza "Rekodi" kuhifadhi kwenye backend (structural analysis).
 *
 * NOTE: magnetic declination — weka kwa eneo lako (Tanzania ~ -1 hadi +2 deg).
 */
@Composable
fun CompassScreen(api: GeoApiClient = remember { GeoApiClient() }) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var strike by remember { mutableStateOf(0.0) }
    var dip by remember { mutableStateOf(0.0) }
    var dipDir by remember { mutableStateOf(0.0) }
    var feature by remember { mutableStateOf("bedding") }
    var status by remember { mutableStateOf<String?>(null) }

    // Sikiliza ROTATION_VECTOR na kokotoa plane orientation
    DisposableEffect(Unit) {
        val sm = ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val listener = object : SensorEventListener {
            val R = FloatArray(9)
            override fun onSensorChanged(e: SensorEvent) {
                SensorManager.getRotationMatrixFromVector(R, e.values)
                val p = CompassClinometer.planeFromRotationMatrix(R, declinationDeg = 0.0)
                strike = p.strike; dip = p.dip; dipDir = p.dipDirection
            }
            override fun onAccuracyChanged(s: Sensor?, a: Int) {}
        }
        sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        onDispose { sm.unregisterListener(listener) }
    }

    Column(
        Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Dira · Clinometer", style = MaterialTheme.typography.titleLarge)
        Text("Weka simu bapa juu ya mwamba", style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(Modifier.height(24.dp))

        // Live readout
        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Reading("Mwelekeo (strike)", strike)
                Reading("Mwinamo (dip)", dip)
                Reading("Uelekeo (dip dir)", dipDir)
            }
        }

        Spacer(Modifier.height(20.dp))

        // Aina ya feature
        Text("Feature", style = MaterialTheme.typography.labelMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("bedding", "foliation", "joint", "fault").forEach { f ->
                FilterChip(selected = feature == f, onClick = { feature = f }, label = { Text(f) })
            }
        }

        Spacer(Modifier.height(20.dp))
        Button(
            onClick = {
                val s = strike; val d = dip; val dd = dipDir
                scope.launch {
                    val ok = withContext(Dispatchers.IO) {
                        runCatching { api.postStructural(dd, d, s, feature, null, null) }.getOrDefault(false)
                    }
                    status = if (ok) "Imerekodiwa: %03.0f/%02.0f".format(s, d) else "Imeshindwa (login? mtandao?)"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Rekodi measurement") }

        status?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun Reading(label: String, value: Double) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("%.0f°".format(value), fontSize = 28.sp, fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace)
    }
}

package tz.geologist.features.reports

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import tz.geologist.core.Config

/**
 * M9 · Reports & Export.
 * Inafungua ripoti za PDF za backend (portfolio kwa region) kwenye browser/PDF viewer.
 * Mission report hufunguliwa kutoka Missions (mission_id). Data-export (CSV/GeoJSON)
 * hutoka pipeline ya satellite.
 */
@Composable
fun ReportsScreen() {
    val ctx = LocalContext.current

    fun open(url: String) {
        runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
    }

    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Ripoti", style = MaterialTheme.typography.titleLarge)
        Text("Ripoti za PDF kutoka backend (zinahitaji LAN/mtandao)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(Modifier.height(8.dp))
        Text("Portfolio report (PDF)", style = MaterialTheme.typography.labelLarge)
        listOf("manyara", "tanga", "dodoma").forEach { r ->
            Button(
                onClick = { open("${Config.BASE_URL}/api/reports/portfolio?region=$r") },
                modifier = Modifier.fillMaxWidth()
            ) { Text("📄  Portfolio — ${r.replaceFirstChar { it.uppercase() }}") }
        }

        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = { open("${Config.BASE_URL}/docs") },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Fungua API docs (backend)") }

        Spacer(Modifier.height(12.dp))
        Text("Data export (CSV / GeoJSON) hutoka pipeline ya satellite (mineral-intelligence). "
            + "Mission report ya PDF hufunguliwa kutoka mission husika.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

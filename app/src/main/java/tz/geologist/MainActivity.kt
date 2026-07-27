package tz.geologist

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import dagger.hilt.android.AndroidEntryPoint
import tz.geologist.core.ui.AiGeologistTheme
import tz.geologist.features.targets.TargetsScreen
import tz.geologist.features.compass.CompassScreen
import tz.geologist.features.sampling.SamplingScreen
import tz.geologist.features.rockid.MineralIdScreen
import tz.geologist.features.mapping.NearbyScreen
import tz.geologist.features.mapping.SatelliteMapScreen
import tz.geologist.features.mapping.AreaMeasureScreen
import tz.geologist.features.mapping.GpsSurveyScreen
import tz.geologist.features.app.AppWebScreen
import tz.geologist.features.projects.ProjectsScreen
import tz.geologist.features.safety.SafetyScreen
import tz.geologist.features.coredrill.CoreLogScreen
import tz.geologist.features.assistant.AssistantScreen
import tz.geologist.features.reports.ReportsScreen
import androidx.navigation.NavController

/**
 * Single-activity app. Navigation huelekeza kwa modules M1..M10.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Omba ruhusa za GPS + camera wakati app inafunguka (zinahitajika na M1/M3/M4/M10)
    private val permLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* matokeo — screens hukagua tena wenyewe */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permLauncher.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA,
        ))
        setContent {
            AiGeologistTheme {
                val nav = rememberNavController()
                Scaffold(bottomBar = { BottomNav(nav) }) { pad ->
                    NavHost(nav, startDestination = "app", modifier = Modifier.padding(pad)) {
                        composable("app")      { AppWebScreen() }
                        composable("compass")  { CompassScreen() }
                        composable("sampling") { SamplingScreen() }
                        composable("rockid")   { MineralIdScreen() }
                        composable("targets")  { TargetsScreen() }
                        composable("nearby")   { NearbyScreen() }
                        composable("satmap")   { SatelliteMapScreen() }
                        composable("area")     { AreaMeasureScreen() }
                        composable("survey")   { GpsSurveyScreen() }
                        composable("projects") { ProjectsScreen() }
                        composable("safety")   { SafetyScreen() }
                        composable("coredrill"){ CoreLogScreen() }
                        composable("assistant"){ AssistantScreen() }
                        composable("reports")  { ReportsScreen() }
                        composable("more")     { MoreScreen(nav) }
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomNav(nav: androidx.navigation.NavController) {
    val items = listOf(
        "app" to "Geologist", "targets" to "Shabaha", "rockid" to "Madini",
        "compass" to "Dira", "more" to "Zaidi"
    )
    NavigationBar {
        val current = nav.currentBackStackEntryAsState().value?.destination?.route
        items.forEach { (route, label) ->
            NavigationBarItem(
                selected = current == route,
                onClick = { nav.navigate(route) { launchSingleTop = true } },
                icon = {}, label = { Text(label) }
            )
        }
    }
}

@Composable
private fun MoreScreen(nav: NavController) {
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Zaidi", style = MaterialTheme.typography.titleLarge)
        val items = listOf(
            "satmap" to "🛰️  Ramani ya satelaiti halisi",
            "survey" to "🚶  Survey ya GPS (tembea mpaka → eneo)",
            "area" to "📐  Pima eneo (coordinates → hekta)",
            "sampling" to "🧪  Kusanya sampuli (M3)",
            "nearby" to "🧭  Shabaha karibu nami (M1)",
            "projects" to "📁  Miradi (M8)",
            "coredrill" to "🪨  Kumbukumbu ya core/drilling (M5)",
            "assistant" to "📖  Msaidizi wa jiolojia (M7)",
            "reports" to "📄  Ripoti / PDF (M9)",
            "safety" to "🆘  Usalama & Sync (M10)"
        )
        items.forEach { (route, label) ->
            Button(onClick = { nav.navigate(route) }, modifier = Modifier.fillMaxWidth()) { Text(label) }
        }
        Spacer(Modifier.height(8.dp))
        Text("Ramani ya satelaiti inasoma targets zilizofungwa ndani ya app (offline); " +
            "tiles za satelaiti zina-cache baada ya mtandao wa kwanza.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

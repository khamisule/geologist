package tz.geologist

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point. Hilt DI root.
 * Sync engine (WorkManager) na local DB huanzishwa hapa.
 */
@HiltAndroidApp
class AiGeologistApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // TODO: schedule background sync (SyncWorker) via WorkManager
        // TODO: initialize MapLibre + offline tile cache
        // TODO: warm-up on-device TFLite classifier
    }
}

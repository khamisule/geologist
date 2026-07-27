package tz.geologist.data.remote.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * M10 · Background sync engine (offline-first).
 *
 * Outbox pattern: mabadiliko ya offline (samples, measurements, reports)
 * yamehifadhiwa local yakiwa na syncState=QUEUED. Worker hii huyapeleka
 * kwa backend ya blueprint yanapopatikana mtandao, kisha hu-pull updates
 * (target scores mpya, tasks) kutoka event bus.
 *
 * Conflict: last-write-wins kwa field data; kwa records nyeti (targets,
 * approvals) server-wins + flag CONFLICT kwa review (kama Mergin/QField).
 * Idempotency: eventId huzuia double-processing wakati wa retries.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    // TODO: inject SampleRepository, EventApi, ConflictResolver
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            // 1. push: tuma outbox (QUEUED) → POST /api/ingestion/field-report, /lab-result
            // 2. pull: pokea events (target scores, tasks) → update local
            // 3. resolve conflicts + set syncState = SYNCED / CONFLICT
            Result.success()
        } catch (e: Exception) {
            Result.retry()   // exponential backoff (WorkManager default)
        }
    }
}

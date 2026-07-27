package tz.geologist.domain

import java.time.Instant

/** Domain models zinazolingana na event/entity contracts za blueprint. */

data class GeoPoint(
    val lat: Double,
    val lng: Double,
    val elevation: Double? = null,
    val accuracyM: Double? = null
)

enum class SampleType { SOIL, ROCK, STREAM, CHIP, CORE }
enum class QaQcFlag { DUPLICATE, BLANK, STANDARD }
enum class SyncState { LOCAL, QUEUED, SYNCED, CONFLICT }

data class PhotoRef(val id: String, val localPath: String, val remoteUrl: String? = null)

data class Sample(
    val id: String,                 // UUID (offline-generated)
    val projectId: String,
    val type: SampleType,
    val location: GeoPoint,
    val lithology: String? = null,
    val alteration: String? = null,
    val mineralization: String? = null,
    val photos: List<PhotoRef> = emptyList(),
    val qaqc: QaQcFlag? = null,
    val barcode: String? = null,    // sample bag QR/barcode
    val collectedAt: Instant,
    val syncState: SyncState = SyncState.LOCAL,
    val traceId: String
)

enum class MeasureKind { PLANAR, LINEAR }

data class StructuralMeasurement(
    val id: String,
    val sampleId: String? = null,
    val kind: MeasureKind,
    val strike: Double,             // 0..360
    val dip: Double,                // 0..90
    val dipDirection: Double,
    val trend: Double? = null,      // kwa linear
    val plunge: Double? = null,
    val location: GeoPoint,
    val takenAt: Instant,
    val traceId: String
)

data class Target(
    val id: String,
    val projectId: String,
    val score: Double,              // prospectivity 0..1
    val confidence: Double,
    val rationale: String,
    val evidenceRefs: List<String> = emptyList(),
    val validatedInField: Boolean = false
)

/** Event mirror ya blueprint (event bus contract). */
data class DomainEvent(
    val eventId: String,
    val eventType: String,          // e.g. NEW_FIELD_REPORT, NEW_SAMPLE_RESULT
    val occurredAt: Instant,
    val projectId: String,
    val entityType: String,
    val entityId: String,
    val payloadJson: String,
    val traceId: String
)

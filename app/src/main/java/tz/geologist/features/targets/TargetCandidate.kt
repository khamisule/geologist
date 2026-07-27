package tz.geologist.features.targets

/**
 * Target moja kutoka pipeline ya satellite (top_candidates.geojson).
 * Inatokana na Sentinel-2 alteration prospectivity — SI uthibitisho wa madini.
 */
data class TargetCandidate(
    val rank: Int,
    val score: Double,      // 0..1 prospectivity
    val lat: Double,
    val lon: Double,
    val aoi: String
) {
    /** Score kama asilimia kwa UI. */
    val scorePct: Int get() = (score * 100).toInt()

    /** Kiwango cha kipaumbele kwa rangi/lebo. */
    val priority: Priority
        get() = when {
            score >= 0.90 -> Priority.VERY_HIGH
            score >= 0.85 -> Priority.HIGH
            else -> Priority.MODERATE
        }

    enum class Priority { VERY_HIGH, HIGH, MODERATE }
}

/** Maeneo yenye seed data ndani ya assets/targets_seed/. */
enum class TargetRegion(val asset: String, val label: String) {
    MANYARA("targets_seed/manyara.geojson", "Manyara (Mererani)"),
    TANGA("targets_seed/tanga.geojson", "Tanga (Handeni)"),
    DODOMA("targets_seed/dodoma.geojson", "Dodoma (craton)")
}

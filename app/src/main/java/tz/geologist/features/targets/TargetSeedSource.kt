package tz.geologist.features.targets

import android.content.Context
import org.json.JSONObject

/**
 * Inasoma targets halisi kutoka GeoJSON iliyowekwa kwenye assets
 * (imetengenezwa na pipeline ya Sentinel-2: satellite_prospectivity.py).
 *
 * Hutumia org.json (imejengwa ndani ya Android — hakuna dependency ya ziada).
 * Baadaye source hii itabadilishwa na sync kutoka backend ya blueprint
 * (endpoint /api/targets) — interface itabaki ile ile.
 */
class TargetSeedSource(private val context: Context) {

    fun load(region: TargetRegion): List<TargetCandidate> {
        val text = context.assets.open(region.asset)
            .bufferedReader().use { it.readText() }
        val fc = JSONObject(text)
        val feats = fc.getJSONArray("features")
        val out = ArrayList<TargetCandidate>(feats.length())
        for (i in 0 until feats.length()) {
            val f = feats.getJSONObject(i)
            val coords = f.getJSONObject("geometry").getJSONArray("coordinates")
            val lon = coords.getDouble(0)   // GeoJSON = [lon, lat]
            val lat = coords.getDouble(1)
            val p = f.getJSONObject("properties")
            out.add(
                TargetCandidate(
                    rank = p.optInt("rank", i + 1),
                    score = p.optDouble("score", 0.0),
                    lat = lat,
                    lon = lon,
                    aoi = p.optString("aoi", region.label)
                )
            )
        }
        return out.sortedByDescending { it.score }
    }
}

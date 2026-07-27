package tz.geologist.data.remote

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import tz.geologist.core.Config
import tz.geologist.features.targets.TargetCandidate
import tz.geologist.features.targets.TargetRegion
import java.util.concurrent.TimeUnit

/**
 * Mteja wa API halisi ya backend (FastAPI ya blueprint).
 * Inatumia OkHttp + org.json (sawa na parser ya seed) — hakuna codegen.
 * Kazi hizi ni blocking; ziitwe ndani ya Dispatchers.IO.
 */
class GeoApiClient(private val baseUrl: String = Config.BASE_URL) {

    private val http = OkHttpClient.Builder()
        .connectTimeout(Config.API_TIMEOUT_SEC, TimeUnit.SECONDS)
        .readTimeout(Config.API_TIMEOUT_SEC, TimeUnit.SECONDS)
        .build()

    private val json = "application/json".toMediaType()

    /** Token ya session (baada ya login). Huambatanishwa kwa maombi yanayohitaji auth. */
    @Volatile var token: String? = null
        private set

    data class Session(val username: String, val role: String)

    /** POST /api/auth/login — hurudisha session na huhifadhi token ndani. */
    fun login(username: String, password: String): Session {
        val body = JSONObject().put("username", username).put("password", password)
            .toString().toRequestBody(json)
        http.newCall(Request.Builder().url("$baseUrl/api/auth/login").post(body).build()).execute().use { resp ->
            if (!resp.isSuccessful) error("Login imeshindwa (HTTP ${resp.code})")
            val o = JSONObject(resp.body!!.string())
            token = o.getString("token")
            return Session(o.getString("username"), o.getString("role"))
        }
    }

    fun logout() { token = null }

    private fun Request.Builder.auth(): Request.Builder =
        apply { token?.let { header("Authorization", "Bearer $it") } }

    /** GET /api/targets?region=&min_score=&limit= (read — hakuna auth inayohitajika) */
    fun getTargets(region: TargetRegion, minScore: Double = 0.0, limit: Int = 500): List<TargetCandidate> {
        val url = "$baseUrl/api/targets?region=${region.name.lowercase()}&min_score=$minScore&limit=$limit"
        http.newCall(Request.Builder().url(url).build()).execute().use { resp ->
            if (!resp.isSuccessful) error("HTTP ${resp.code}")
            val arr = JSONArray(resp.body!!.string())
            return (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                TargetCandidate(
                    rank = o.optInt("rank", i + 1),
                    score = o.optDouble("score", 0.0),
                    lat = o.getDouble("lat"),
                    lon = o.getDouble("lon"),
                    aoi = o.optString("aoi", region.label),
                )
            }
        }
    }

    /** POST /api/targets/{id}/validate — human-in-the-loop ya geologist. */
    fun validateTarget(targetId: String, validated: Boolean, note: String?, geologist: String?): Boolean {
        val body = JSONObject()
            .put("validated", validated).put("note", note).put("geologist", geologist)
            .toString().toRequestBody(json)
        val req = Request.Builder().url("$baseUrl/api/targets/$targetId/validate").post(body).auth().build()
        http.newCall(req).execute().use { return it.isSuccessful }
    }

    /** POST /api/ingestion/field-report — sync ya sample/field data. */
    fun postFieldReport(targetId: String?, lat: Double, lon: Double, lithology: String?, note: String?): Boolean {
        val body = JSONObject()
            .put("target_id", targetId).put("lat", lat).put("lon", lon)
            .put("lithology", lithology).put("note", note)
            .toString().toRequestBody(json)
        val req = Request.Builder().url("$baseUrl/api/ingestion/field-report").post(body).auth().build()
        http.newCall(req).execute().use { return it.isSuccessful }
    }

    /** POST /api/ingestion/structural — strike/dip kutoka compass (M2). */
    fun postStructural(dipDirection: Double, dip: Double, strike: Double,
                       feature: String?, lat: Double?, lon: Double?, targetId: String? = null): Boolean {
        val body = JSONObject()
            .put("target_id", targetId).put("kind", "PLANAR")
            .put("dip_direction", dipDirection).put("dip", dip).put("strike", strike)
            .put("feature", feature).put("lat", lat).put("lon", lon)
            .toString().toRequestBody(json)
        val req = Request.Builder().url("$baseUrl/api/ingestion/structural").post(body).auth().build()
        http.newCall(req).execute().use { return it.isSuccessful }
    }

    data class NearTarget(val rank: Int, val score: Double, val lat: Double, val lon: Double, val distanceKm: Double)

    /** GET /api/targets/near — targets karibu na nukta (M1). */
    fun getTargetsNear(lat: Double, lon: Double, radiusKm: Double = 10.0, limit: Int = 30): List<NearTarget> {
        val url = "$baseUrl/api/targets/near?lat=$lat&lon=$lon&radius_km=$radiusKm&limit=$limit"
        http.newCall(Request.Builder().url(url).build()).execute().use { resp ->
            if (!resp.isSuccessful) error("HTTP ${resp.code}")
            val arr = JSONArray(resp.body!!.string())
            return (0 until arr.length()).map {
                val o = arr.getJSONObject(it)
                NearTarget(o.optInt("rank"), o.optDouble("score"), o.getDouble("lat"),
                    o.getDouble("lon"), o.optDouble("distance_km", 0.0))
            }
        }
    }

    data class Project(val id: String, val name: String, val region: String?, val stage: String)

    /** GET /api/projects (M8). */
    fun getProjects(): List<Project> {
        http.newCall(Request.Builder().url("$baseUrl/api/projects").build()).execute().use { resp ->
            if (!resp.isSuccessful) error("HTTP ${resp.code}")
            val arr = JSONArray(resp.body!!.string())
            return (0 until arr.length()).map {
                val o = arr.getJSONObject(it)
                Project(o.getString("id"), o.getString("name"),
                    o.optString("region", null), o.optString("stage", "draft"))
            }
        }
    }

    /** POST /api/projects (field_lead/admin). */
    fun createProject(name: String, region: String?): Boolean {
        val body = JSONObject().put("name", name).put("region", region).toString().toRequestBody(json)
        val req = Request.Builder().url("$baseUrl/api/projects").post(body).auth().build()
        http.newCall(req).execute().use { return it.isSuccessful }
    }

    data class MineralInfo(
        val id: String, val name: String, val formula: String, val commodity: String,
        val hardness: String, val streak: String, val luster: String, val colors: String,
        val tzNotes: String
    )

    /** GET /api/minerals?q= — tafuta madini kwenye reference DB (M7). */
    fun searchMinerals(q: String? = null): List<MineralInfo> {
        val url = "$baseUrl/api/minerals" + (q?.takeIf { it.isNotBlank() }?.let { "?q=$it" } ?: "")
        http.newCall(Request.Builder().url(url).build()).execute().use { resp ->
            if (!resp.isSuccessful) error("HTTP ${resp.code}")
            val arr = JSONArray(resp.body!!.string())
            return (0 until arr.length()).map {
                val o = arr.getJSONObject(it)
                val m = o.getJSONArray("mohs")
                val hard = if (m.getDouble(0) == m.getDouble(1)) "${m.getDouble(0)}"
                           else "${m.getDouble(0)}–${m.getDouble(1)}"
                val cols = o.getJSONArray("colors").let { c -> (0 until c.length()).joinToString(", ") { i -> c.getString(i) } }
                val lus = o.getJSONArray("luster").let { l -> (0 until l.length()).joinToString(", ") { i -> l.getString(i) } }
                MineralInfo(
                    o.getString("id"), o.getString("name"), o.getString("formula"),
                    o.optString("commodity", ""), hard, o.optString("streak", ""),
                    lus, cols, o.optString("tz_notes", "")
                )
            }
        }
    }

    /** POST /api/minerals/identify — determinative key kwa sifa (M4 fallback). */
    fun identifyMineral(hardness: Double?, streak: String?, luster: String?,
                        color: String?, magnetic: Boolean?): List<Pair<String, Double>> {
        val body = JSONObject()
            .put("hardness", hardness).put("streak", streak).put("luster", luster)
            .put("color", color).put("magnetic", magnetic)
            .toString().toRequestBody(json)
        val req = Request.Builder().url("$baseUrl/api/minerals/identify").post(body).build()
        http.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) error("HTTP ${resp.code}")
            val arr = JSONObject(resp.body!!.string()).getJSONArray("candidates")
            return (0 until arr.length()).map {
                val o = arr.getJSONObject(it)
                o.getString("name") to o.getDouble("score")
            }
        }
    }
}

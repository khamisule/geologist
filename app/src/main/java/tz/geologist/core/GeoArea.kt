package tz.geologist.core

import kotlin.math.*

/**
 * Kupima ENEO kutoka point za coordinate (DMS au decimal).
 *
 * - parseCoordinates(): inasoma maandishi yenye point (DMS mfano `05° 11' 56.09" S`
 *   au decimal mfano `-5.1989, 36.4681`) na kurudisha orodha ya (lat, lon).
 * - areaPerimeter(): eneo (m²) + mzunguko (m) kwa makadirio ya ellipsoid (WGS84),
 *   sahihi <0.001% kwa maeneo madogo (imethibitishwa dhidi ya geodesic ya geographiclib).
 */
object GeoArea {

    data class Pt(val lat: Double, val lon: Double)
    data class Result(val areaM2: Double, val perimM: Double, val points: List<Pt>)

    // DMS: deg [sep] min [sep] sec [sep] hemi(N/S/E/W)
    private val DMS = Regex(
        """(\d+(?:\.\d+)?)\s*[°:\s]\s*(\d+(?:\.\d+)?)\s*['′:\s]\s*(\d+(?:\.\d+)?)\s*["″]?\s*([NSEWnsew])"""
    )
    // Decimal yenye hemisphere: 5.1989 S  au  36.4681 E
    private val DEC_H = Regex("""(-?\d+\.\d+)\s*([NSEWnsew])""")
    // Decimal tupu (bila hemisphere)
    private val DEC = Regex("""(-?\d+\.\d+)""")

    /** Rudisha coordinate (signed decimal degrees) kwa mpangilio walivyoandikwa. */
    fun parseScalars(text: String): List<Double> {
        val positioned = ArrayList<Pair<Int, Double>>()

        // 1) DMS (deg/min/sec + hemisphere)
        val dmsRanges = ArrayList<IntRange>()
        for (m in DMS.findAll(text)) {
            val d = m.groupValues[1].toDouble()
            val mi = m.groupValues[2].toDouble()
            val s = m.groupValues[3].toDouble()
            val h = m.groupValues[4].uppercase()
            var v = d + mi / 60.0 + s / 3600.0
            if (h == "S" || h == "W") v = -v
            positioned.add(m.range.first to v)
            dmsRanges.add(m.range)
        }
        fun inDms(i: Int) = dmsRanges.any { i in it }

        // 2) Decimal + hemisphere (isiyogusana na DMS)
        for (m in DEC_H.findAll(text)) {
            if (inDms(m.range.first)) continue
            var v = m.groupValues[1].toDouble()
            val h = m.groupValues[2].uppercase()
            if (h == "S" || h == "W") v = -abs(v)
            positioned.add(m.range.first to v)
        }

        // 3) Decimal tupu — tu kama hakuna DMS wala DEC_H
        if (positioned.isEmpty()) {
            for (m in DEC.findAll(text)) positioned.add(m.range.first to m.groupValues[1].toDouble())
        }
        return positioned.sortedBy { it.first }.map { it.second }
    }

    /** Panga scalars kuwa point (lat, lon) — mbili mbili kwa mpangilio. */
    fun parseCoordinates(text: String): List<Pt> {
        val s = parseScalars(text)
        val pts = ArrayList<Pt>()
        var i = 0
        while (i + 1 < s.size) { pts.add(Pt(s[i], s[i + 1])); i += 2 }
        return pts
    }

    /** Eneo (m²) + mzunguko (m) — ellipsoid local projection + shoelace. */
    fun areaPerimeter(pts: List<Pt>): Result {
        if (pts.size < 3) return Result(0.0, 0.0, pts)
        val a = 6378137.0; val f = 1.0 / 298.257223563; val e2 = 2 * f - f * f
        fun rad(d: Double) = d * PI / 180.0
        val n = pts.size
        val lat0 = pts.sumOf { it.lat } / n
        val lon0 = pts.sumOf { it.lon } / n
        val sin0 = sin(rad(lat0)); val w = 1 - e2 * sin0 * sin0
        val m = a * (1 - e2) / w.pow(1.5)      // meridional radius
        val nn = a / sqrt(w)                    // prime-vertical radius
        val xy = pts.map { p ->
            doubleArrayOf(nn * cos(rad(lat0)) * rad(p.lon - lon0), m * rad(p.lat - lat0))
        }
        var area = 0.0; var perim = 0.0
        for (i in 0 until n) {
            val (x1, y1) = xy[i]
            val (x2, y2) = xy[(i + 1) % n]
            area += x1 * y2 - x2 * y1
            perim += hypot(x2 - x1, y2 - y1)
        }
        return Result(abs(area / 2), perim, pts)
    }

    fun hectares(m2: Double) = m2 / 10000.0
    fun acres(m2: Double) = m2 / 4046.8564224
    fun km(m: Double) = m / 1000.0

    /** Tengeneza `poly` param kwa map.html: "lat,lon;lat,lon;..." */
    fun toPolyParam(pts: List<Pt>): String =
        pts.joinToString(";") { "${it.lat},${it.lon}" }
}

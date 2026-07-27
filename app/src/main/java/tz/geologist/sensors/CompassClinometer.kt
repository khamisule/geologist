package tz.geologist.sensors

import android.hardware.SensorManager
import kotlin.math.abs

/**
 * M2 · Digital compass-clinometer.
 * Inabadilisha rotation matrix ya simu kuwa strike, dip na dip-direction
 * ya plane (kama FieldMove Clino / Rocklogger). Weka simu bapa juu ya
 * uso wa mwamba ili kupima plane orientation.
 *
 * NOTE: rekebisha kwa magnetic declination ya eneo (Tanzania ~ -3 hadi +1 deg
 * kutegemea eneo) kabla ya kuripoti true north.
 */
object CompassClinometer {

    data class PlaneReading(
        val strike: Double,        // 0..360 (right-hand rule)
        val dip: Double,           // 0..90
        val dipDirection: Double   // 0..360
    )

    /**
     * @param rotationMatrix R[9] kutoka SensorManager.getRotationMatrix()
     * @param declinationDeg magnetic declination ya eneo (deg)
     */
    fun planeFromRotationMatrix(rotationMatrix: FloatArray, declinationDeg: Double = 0.0): PlaneReading {
        // Vector ya kawaida (normal) ya uso wa simu = safu ya tatu ya R
        val nx = rotationMatrix[2].toDouble()
        val ny = rotationMatrix[5].toDouble()
        val nz = rotationMatrix[8].toDouble()

        // Dip = pembe kati ya normal na wima
        val dip = Math.toDegrees(Math.acos(abs(nz))).let { 90.0 - abs(90.0 - it) }

        // Dip direction = azimuth ya makadirio ya normal kwenye ndege ya usawa
        var dipDir = Math.toDegrees(Math.atan2(nx, ny)) + declinationDeg
        dipDir = (dipDir + 360.0) % 360.0

        // Strike = dip direction - 90 (right-hand rule)
        val strike = (dipDir - 90.0 + 360.0) % 360.0

        return PlaneReading(strike = strike, dip = dip, dipDirection = dipDir)
    }

    /** Msaidizi: pata rotation matrix kutoka accelerometer + magnetometer. */
    fun rotationMatrix(accel: FloatArray, magnet: FloatArray): FloatArray? {
        val r = FloatArray(9)
        val i = FloatArray(9)
        return if (SensorManager.getRotationMatrix(r, i, accel, magnet)) r else null
    }
}

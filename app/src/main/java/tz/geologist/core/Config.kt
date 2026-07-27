package tz.geologist.core

/**
 * Config ya app. Badilisha BASE_URL kwa anwani ya backend yako.
 *
 * - Emulator (Android Studio) -> host = 10.0.2.2  (inaelekeza localhost ya PC)
 * - Simu halisi kwenye WiFi ile ile -> tumia IP ya PC, mfano http://192.168.1.20:8000
 */
object Config {
    const val BASE_URL = "http://10.0.2.2:8000"   // backend ya blueprint (FastAPI)
    const val API_TIMEOUT_SEC = 8L
}

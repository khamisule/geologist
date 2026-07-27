# ⛏️ Geologist — Android

Mfumo wa **kutafuta madini Tanzania** kwa satelaiti (Sentinel-2) na zana za shambani. Kiswahili kwanza.
Sehemu ya Android ya mfumo mzima wa Geologist (Android + backend + desktop).

> Skrini kuu ni **app kamili ya Geologist** (WebView ya `assets/Geologist.html`) yenye vipengele vyote,
> pamoja na nyongeza za asili (dira/sensor, kamera ya utambuzi madini, sampuli).

## Vipengele
- **🗺️ Ona Madini** — ramani ya satelaiti halisi (Esri) Tanzania nzima: dhahabu, tanzanite, almasi,
  nickel, n.k. + maeneo ya uwezekano kutoka uchambuzi wa Sentinel-2 (Dodoma/Manyara/Tanga).
- **📐 Pima & Chunguza** — pima eneo kwa kubonyeza ramani au kuandika coordinates (DMS/decimal) →
  eneo (hekta/acres) + mzunguko + madini yanayowezekana ya kikanda.
- **🚶 GPS Survey** — tembea mpaka, bonyeza kila kona, GPS inajiandika (usahihi ±m, best-of-N) → eneo halisi.
- **📋 Historia** — kumbukumbu za maeneo (backend/database au offline ndani ya kifaa) + sidebar ya historia.
- **Nyongeza (native):** dira (strike/dip), utambuzi wa madini kwa kamera, kukusanya sampuli, ripoti, usalama.

## Kujenga APK

**Njia ya wingu (rahisi — bila Android Studio):**
1. Tengeneza repo tupu GitHub, nakili URL.
2. Endesha **`push_to_github.bat`** (bandika URL, ingia GitHub mara moja).
3. Tab ya **Actions** → "Build Geologist APK" → subiri → pakua **`geologist-debug-apk`** (`app-debug.apk`).

Mwongozo: [`CLOUD_BUILD.md`](CLOUD_BUILD.md) · Workflow: [`.github/workflows/build-apk.yml`](.github/workflows/build-apk.yml)

**Android Studio:** [`BUILD_APK.md`](BUILD_APK.md) → Build → Build APK.

## Tech stack
Kotlin · Jetpack Compose (Material 3) · WebView (app kamili) · Hilt · Room · Retrofit ·
play-services-location (GPS) · CameraX · TensorFlow Lite (utambuzi wa madini).

## Modules (features/)
| Module | Maelezo |
|---|---|
| app | **Geologist kamili** (WebView): Ona Madini, Pima, GPS Survey, Historia |
| targets | Prospectivity targets (Sentinel-2) + field validation |
| compass | Strike/dip kwa sensor |
| sampling | Sampuli + GPS + lithology |
| rockid | Utambuzi wa madini (determinative key + kamera) |
| coredrill · projects · reports · assistant · safety · mapping | Nyongeza za shambani |

## Uthibitisho & vyanzo
Mbinu imethibitishwa dhidi ya mifumo ya kitaalamu (EIS Toolkit ya EU, Geoscience Australia).
Hesabu ya eneo inalingana na geodesic ya **Karney (GeographicLib)** kwa **≤0.0001%**.
GPS inafuata best-practice ya Geolocation API. Ona `VALIDATION.md` na `REFERENCES.md` (mzizi wa mradi).

## Onyo (uwazi)
Mfumo unaonyesha **viashiria** vya madini (alteration + faults + muktadha wa kikanda) — **si uthibitisho**.
Uthibitisho wa mwisho: sampuli + maabara shambani. GPS ya simu ni ±mita chache; kwa mipaka RASMI ya
leseni tumia DGPS/RTK + surveyor aliyeidhinishwa.

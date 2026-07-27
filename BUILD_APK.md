# Kujenga APK ya Geologist (Android)

App hii inajengwa kwa **Android Studio** (uliyonayo tayari). Hatua ni rahisi.

## Hatua za ku-build APK

1. **Fungua Android Studio** → *Open* → chagua folda:
   `C:\Users\TechHub\PROJECTs\PROJECTs\GEOLOGIST\ai-geologist-android`

2. Subiri **Gradle Sync** ikamilike (mara ya kwanza inapakua dependencies — inahitaji internet, dakika chache). Android Studio itatengeneza `gradlew` na kupakua Gradle yenyewe.

3. Menyu: **Build → Build Bundle(s) / APK(s) → Build APK(s)**.

4. Ikimaliza, bonyeza **"locate"** kwenye ujumbe unaojitokeza. APK iko:
   `app\build\outputs\apk\debug\app-debug.apk`

## Kusakinisha kwenye simu

- **Njia ya USB:** unganisha simu (Developer options + USB debugging ON) → Android Studio: **Run ▶** (au `Run 'app'`) → app itasakinishwa na kufunguka.
- **Njia ya faili:** tuma `app-debug.apk` kwenye simu (WhatsApp/email/USB) → fungua → ruhusu "Install from unknown sources" → Sakinisha.

## Ruhusa app inazoomba (za lazima kwa vipengele vipya)

- **Location (GPS)** — kwa "Survey ya GPS" (kutembea mpaka) na "Shabaha karibu nami".
- **Camera** — kwa kupiga picha za madini (M3/M4).
- Ruhusu zote zinapoombwa mara ya kwanza.

## Vipengele vipya vya ramani (ndani ya app)

- **Ramani** (bottom bar) — satelaiti halisi ya Esri + targets + heatmap.
- **Zaidi → 🚶 Survey ya GPS** — tembea mpaka, bonyeza kila kona, GPS inajiandika → eneo (ha).
- **Zaidi → 📐 Pima eneo** — ingiza coordinates (DMS/decimal) → eneo + ramani.
- **Zaidi → 🛰️ Ramani ya satelaiti halisi**.

> **Muhimu:** Ramani ya satelaiti inahitaji internet mara ya kwanza (tiles) kisha ina-cache.
> Uchambuzi wa madini kamili (prospectivity kutoka DB) unahitaji backend (PC) kwenye WiFi ile ile —
> weka anwani ya PC kwenye `core/Config.kt` (`BASE_URL`). Bila backend, ramani + pima eneo +
> uchambuzi wa kikanda bado vinafanya kazi (data ndani ya app).

## Release APK (hiari, kwa kusambaza)

Kwa toleo la kusambaza (signed): **Build → Generate Signed Bundle / APK** → fuata wizard
kutengeneza keystore. Kwa majaribio, `app-debug.apk` inatosha.

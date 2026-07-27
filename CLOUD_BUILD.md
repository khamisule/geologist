# Kujenga APK kwenye Wingu (GitHub Actions) — bila Android Studio

App ya Geologist ni **native Kotlin** (si React Native/Expo), kwa hiyo **EAS Build haifai**.
Njia sahihi ya wingu ni **GitHub Actions** — server za GitHub zinajenga APK bila kompyuta yako.
Workflow tayari imewekwa: `.github/workflows/build-apk.yml`.

## Unachohitaji
- **Akaunti ya GitHub** (bure — github.com). *Siwezi kukutengenezea akaunti; lazima uifungue mwenyewe.*
- Internet.

## Hatua

1. **Fungua akaunti ya GitHub** (kama huna): github.com → Sign up.

2. **Tengeneza repo mpya**: bofya **+** (juu kulia) → **New repository** → jina mfano `geologist-android` → **Create**.

3. **Pakia code**: kwenye ukurasa wa repo mpya, bofya **"uploading an existing file"** →
   buruta **vitu vyote vilivyomo ndani ya folda `ai-geologist-android`** (app/, build.gradle.kts,
   settings.gradle.kts, .github/, n.k.) → **Commit changes**.
   > Muhimu: pakia *yaliyomo ndani* ya `ai-geologist-android`, si folda yenyewe — ili
   > `.github/workflows/build-apk.yml` na `build.gradle.kts` viwe kwenye mzizi wa repo.

4. **Jenga APK**: bofya tab ya **Actions** → chagua **"Build Geologist APK"** →
   **Run workflow** (au inajianzisha baada ya kupakia). Subiri dakika ~5–10 (rangi ya kijani = imefanikiwa).

5. **Pakua APK**: bofya build iliyokamilika → chini kwenye **Artifacts** → pakua
   **`geologist-debug-apk`** (ni zip). Fungua zip → utapata **`app-debug.apk`**.

6. **Sakinisha simuni**: tuma `app-debug.apk` kwenye simu (WhatsApp/USB) → fungua →
   ruhusu "Install from unknown sources" → Sakinisha.

## Vidokezo
- Build ya kwanza inaweza kuchukua muda (inasakinisha SDK + dependencies).
- Ikishindwa: bofya build → soma "log" nyekundu; nipe ujumbe wa hitilafu, nitakusaidia.
- Hii inajenga **debug APK** (kwa majaribio). Kwa toleo la Play Store, tutahitaji signed release
  (keystore) — tunaweza kuiongeza baadaye.

## Njia nyingine za wingu (zote zinahitaji repo ya GitHub/GitLab)
- **Codemagic** au **Bitrise**: una UI ya kuunganisha repo, zinajenga Android Gradle projects.
  GitHub Actions ndiyo rahisi na bure zaidi kwa hili.

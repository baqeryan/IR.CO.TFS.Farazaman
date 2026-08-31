# FaraZaman agent guide

FaraZaman is a Persian/RTL Android field-operations app for municipal driver and supervisor workflows: missions, GPS track recording, maps, road/address lookup, violations, photos, and track upload.

## Start here

- Read this file and the relevant parts of `docs/PROJECT_CONTEXT.md`, `docs/CODEMAP.md`, and `docs/RELEASE_NOTES.md` before exploring source.
- Use `docs/CODEMAP.md` to locate a feature first. Inspect only files relevant to the task.
- Do not recursively scan the repository unless the map is insufficient. Do not re-discover documented architecture.
- Do not refactor, format, or modify unrelated files. Prefer minimal targeted changes.
- Run the smallest relevant test/build first. Update these documents only when architecture, important locations, or confirmed behavior changes.

## Project facts

- Single Android application module: `:app`; package/namespace `Ir.co.tfs.farazaman`.
- Kotlin and legacy Java; minSdk 23, target/compile SDK 34, Java/Kotlin 17.
- UI is hybrid: XML Activities/Fragments with ViewBinding/DataBinding plus Compose for splash and role selection.
- Architecture is partially layered, not uniform: Hilt ViewModels/repositories exist, but several legacy Activities/Fragments directly combine UI, APIs, `ContentResolver`, and `SharedPreferences`.
- DI: Hilt via `MyApplication`, `di/NetworkModule.kt`, and `di/AppModule.kt`.
- Network: Retrofit 2, OkHttp 4, Gson, AppAuth. APIs live in `data/api/` and `service/remote/`.
- Authentication/tokens: `auth/`, `data/repository/AuthRepositoryImpl.kt`, and token/auth helpers in `util/`.
- Persistence: legacy SQLite through `data/db/TrackContentProvider.java` and `DatabaseHelper.java`; mission/history and user metadata also use default `SharedPreferences`. Room stubs are present but disabled.
- UI/navigation: `activity/`, `presentation/`, `supervisor/`, `app/src/main/res/layout/`, and `app/src/main/AndroidManifest.xml`. Navigation is Intent/FragmentTransaction based; no active Navigation graph was found.
- Business logic: `domain/`, ViewModels under `presentation/viewmodel/`, repositories under `data/repository/`, plus legacy controllers/helpers under `supervisor/` and `activity/`.
- Build types: `debug` (`versionNameSuffix "-dev"`) and minified/resource-shrunk `release`; no product flavors. Release packages ARM (`armeabi-v7a`, `arm64-v8a`) only; Debug retains x86 emulator support.
- Naming follows Android conventions (`*Activity`, `*Fragment`, `*ViewModel`, `*Service`, `*Repository`, `*Manager`). Package prefix uses the existing uppercase `Ir` and must not be normalized casually.

## Development rules

- Preserve Persian locale and RTL behavior; app-wide setup is in `MyApplication.kt` and themes/resources have night variants.
- The selected municipality base URL is runtime state in default preferences under `BASE_URL`; do not replace it with the central URL in feature code.
- Never log token values, authorization headers, passwords, or client secrets.
- Keep network/database work off the main thread. Close `Cursor`, response bodies, services, listeners, and location updates with lifecycle ownership.
- Release uses R8. Preserve Retrofit/Gson/AppAuth rules in `app/proguard-rules.pro`; test auth and the first authenticated API call in Release.
- `LEGACY_CLIENT_SECRET` comes from `local.properties` (`farazaman.legacyClientSecret`) or `FARAZAMAN_LEGACY_CLIENT_SECRET`; never commit it.

## Commands

On Windows replace `./gradlew` with `.\gradlew.bat`.

- Debug APK: `./gradlew :app:assembleDebug`
- Signed Release APK: `./gradlew :app:assembleRelease`
- Unit tests: `./gradlew :app:testDebugUnitTest`
- Coverage: `./gradlew :app:jacocoTestReport`
- Instrumented tests (device/emulator): `./gradlew :app:connectedDebugAndroidTest`
- Lint: `./gradlew :app:lintDebug` or `./gradlew :app:lintRelease`

Release signing is configured by `signing/release-signing.properties` and the permanent keystore beside it. Do not replace, regenerate, or rename that keystore; losing it breaks APK upgrade compatibility. On low-memory machines use `./gradlew :app:assembleRelease "-Dorg.gradle.jvmargs=-Xmx2g -XX:MaxMetaspaceSize=768m" --no-daemon --no-parallel --max-workers=1`.

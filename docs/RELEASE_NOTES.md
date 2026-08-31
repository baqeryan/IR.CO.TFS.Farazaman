# Debug/Release technical notes

This file records build-type differences and confirmed Release-specific constraints. It is not a product changelog.

## Build configuration

| Concern | Debug | Release |
|---|---|---|
| Version name | Date-based plus `-dev` | Date-based |
| Minification | Off | `minifyEnabled true` |
| Resource shrinking | Off | `shrinkResources true` |
| Compose tooling | Included by `debugImplementation` | Not included |
| Compose previews | `app/src/debug/java/` | Excluded |
| Signing | Standard Android debug signing | Permanent FaraZaman Release keystore |

There are no product flavors. Both build types use the same application ID, manifest, runtime municipality registry, selected `BASE_URL`, API interfaces, and authentication architecture.

## BuildConfig and secrets

- `BuildConfig.LEGACY_CLIENT_SECRET` exists in both variants.
- Its value comes from `local.properties` key `farazaman.legacyClientSecret`, otherwise environment variable `FARAZAMAN_LEGACY_CLIENT_SECRET`, otherwise an empty string.
- Password-grant login calls `check(BuildConfig.LEGACY_CLIENT_SECRET.isNotBlank())`; a machine can therefore build successfully but fail legacy login at runtime when the secret is absent.
- OIDC mode does not use this legacy Basic-auth secret.
- Never commit or print the secret.

## API and environment behavior

- Debug and Release do not have different compiled base URLs.
- Central registry default: `https://app.tfs.co.ir/` from `AppConstants.kt`.
- The selected municipality is saved at runtime in default SharedPreferences as `BASE_URL`; OIDC client ID, scope, and login mode are saved with it.
- Retrofit is provided from `di/NetworkModule.kt`; legacy upload URLs must go through `util/SelectedServerUrl.kt` or equivalent selected-base logic.
- When reproducing a server issue, clear app data or verify the stored city and `BASE_URL`; an upgraded Debug installation may retain different session/city state.
- In-app update discovery is identical in Debug and Release and uses the fixed HTTPS endpoint in `data/update/AppUpdateConfig.kt`, not the selected municipality `BASE_URL`. Its dedicated OkHttp client sends no bearer token. The APK URL must be HTTPS on `ftp.tfs.co.ir`.
- Release update smoke tests must use an APK signed with the same permanent Release certificate. Debug and Release certificates intentionally differ, so one cannot update the other in place. Android O+ also requires the user to grant this app unknown-source install access.

## Authentication behavior

- Web/OIDC flow: `LoginActivity` -> `OidcLoginBottomSheet` -> `OidcAuthManager` -> AppAuth token exchange -> encrypted `TokenManager` storage.
- Password flow: `LoginUseCase` -> `AuthRepositoryImpl`, trying `token` then `api/token` on the selected server.
- After either flow, login is not fully verified until `UserService.getUserInfo()` and `RolesService.getUserRoles()` succeed and their results are persisted.
- `AuthInterceptor` adds bearer auth and performs synchronized refresh/retry. Its HTTP logging level is `NONE`, and `Authorization` is configured for redaction.
- Token strings are encrypted with Android Keystore AES/GCM through `SecureTokenStorage`; token expiry remains a preference value.

## R8/ProGuard contract

`app/proguard-rules.pro` currently preserves:

- generic signatures, inner/enclosing metadata, exceptions, and runtime annotations used by Retrofit;
- API DTO fields and constructors used by Gson reflection;
- Retrofit service interfaces under `service.remote` and `data.api`;
- coroutine `Continuation` generic metadata used by suspend Retrofit methods;
- `retrofit2.Response`, whose parameterized type is read from suspend method signatures;
- authentication repository/domain classes, secure token classes, and AppAuth.

Do not remove these rules based only on a successful Debug build. Any new reflection-based serializer, DTO package, or Retrofit interface outside the existing packages must be assessed for R8 rules.

MapLibre adds native renderer libraries to both build types. Release smoke tests must open both
`DisplayTrackMap` and `SubmitViolationActivity`, confirm PMTiles range loading from Baran, and
exercise route/location/fence overlays after R8 and resource shrinking.
Release intentionally packages only `armeabi-v7a` and `arm64-v8a`; x86/x86_64 MapLibre binaries
remain available in Debug for emulators but are excluded from the directly distributed phone APK.
The runtime style is `https://baran.tfs.co.ir/tiles/style.json`; the last valid response is cached,
and `current.json` plus the internal style is the network/error fallback in both build types.

## Confirmed previously investigated Release issue

A minified login could successfully obtain/store the OIDC token but fail on the immediately following Retrofit user-info call because R8 removed/changed generic response metadata. Evidence remains in the explanatory R8 comments/rules in `app/proguard-rules.pro` and the current non-suspend `retrofit2.Call<T>` signatures in `UserService.kt` and `RolesService.kt`.

The applied safeguards are:

- Retrofit/Gson/signature keep rules in `app/proguard-rules.pro`;
- `UserService.getUserInfo()` and `RolesService.getUserRoles()` return `Call<T>`;
- callers execute these calls on `Dispatchers.IO` in `LoginActivity.kt` and `SupervisorProfileActivity.kt`.

Future auth fixes must confirm all four stages separately: authorization redirect/password response, secure token read-back, user info, and roles. A successful redirect alone is not a successful application login.

The Release-only employer-list failure was reproduced on an emulator on 2026-08-31. R8 reduced
the suspend return type of `FormDataApiService.getFormData()` to a raw `retrofit2.Response`, so
Retrofit threw `Response must include generic type` before OkHttp created a request. Keeping
`retrofit2.Response` in `app/proguard-rules.pro` fixed it. The minified Release build then sent
`POST /api/BillOriginViEvent/FormData`, received HTTP 200, and populated the employer/contract
selection fields.

An ANR after selecting the Babol OIDC city was also reproduced on 2026-08-31. The city callback
opened a loading dialog and WebView bottom sheet before the city bottom sheet had finished
dismissing, while the WebView session path also called synchronous `CookieManager.flush()` on the
main thread. `CitySelectionBottomSheet` now delivers its selection from `onDismiss`,
`LoginActivity` starts the next window on the following UI-loop turn, and
`OidcLoginBottomSheet` uses asynchronous cookie removal without `flush()`. A minified Release
smoke test opened the OIDC WebView without SurfaceSync timeout or input-dispatch ANR.

The Release work-area confirmation failure was caused by deriving
`TrackContentProvider.AUTHORITY` from `OSMTracker.class.getPackage()`. R8 renamed that class to an
obfuscated package, producing `content://a.provider/...`, while the manifest still registered
`Ir.co.tfs.farazaman.provider`. `OSMTracker.PACKAGE_NAME`, provider URIs, tracking intent actions,
and legacy resource lookup now use the stable `BuildConfig.APPLICATION_ID`. A minified Release
smoke test successfully created the remote mission, fetched its index and organ fence, inserted
track ID 1 through the correct provider authority, and navigated to `NewSupervisorActivity`.

## Manifest and network security

- Only `app/src/main/AndroidManifest.xml` was found; no Debug/Release manifest override exists.
- OIDC redirect receiver accepts `ir.co.tfs.farazaman://signin-oidc`; a legacy `osmtracker` scheme is also declared.
- The manifest does not declare `android:networkSecurityConfig`, and no network security XML was found.
- `android:allowBackup=false`; RTL is enabled; location foreground service is declared.
- `values-v27/`, `values-night/`, and other resource alternatives affect both build types according to device API/night mode, not by Debug/Release.

## Signing

- Permanent keystore: `signing/farazaman-release.jks`; alias: `farazaman-release`.
- Credentials and keystore path: `signing/release-signing.properties`. They are intentionally versioned with the private repository at the repository owner's explicit request.
- `app/build.gradle` can alternatively read `signing.*` Gradle properties or `FARAZAMAN_SIGNING_*` environment variables.
- Certificate SHA-256: `48:4C:C5:50:23:86:DD:A3:EF:6E:CF:BB:80:D1:AE:F8:08:A2:BD:10:99:1C:B0:9B:73:5A:B2:9E:63:CF:08:77`; validity ends 2053-12-30.
- `assembleRelease` writes the signed artifact to `app/build/outputs/apk/release/app-release.apk`. Never regenerate this identity casually; back up the entire `signing/` directory outside Git as well.

## Release-only investigation checklist

1. Reproduce on a clean installation and record the selected municipality/login mode without logging secrets.
2. Confirm the installed APK is actually Release/minified and signed as intended.
3. Confirm `BuildConfig.LEGACY_CLIENT_SECRET` is configured only when testing password mode.
4. Verify stored `BASE_URL`, OIDC client ID/scope, and that the failing request targets the selected server.
5. Distinguish token acquisition from user-info/roles and from the first feature API call.
6. Compare HTTP status and exception class, not token/header content.
7. For Retrofit/Gson failures seen only in Release, inspect `app/proguard-rules.pro`, service return generic types, DTO field retention, and `mapping.txt`.
8. For an employer/contract list failure, begin at `NewMissionBottomSheetFragment.fetchOrgans/fetchContracts`, then `FormDataApiService`, `FormDataResponse`, `NetworkModule`, and `AuthInterceptor`.
9. Run `:app:testDebugUnitTest`, `:app:lintRelease`, and `:app:assembleRelease`; then perform the relevant runtime smoke path because unit tests do not exercise R8.

## Known limitations

- No automated Release UI/auth smoke test exists.
- No instrumented test class was found; current automated tests are local unit tests.
- Network/error handling is distributed, so failures may be caught and converted to UI messages after partial login/navigation.
- Some legacy screens construct requests/Retrofit clients directly. Confirm they use the selected base URL and shared authenticated OkHttp client before assuming DI behavior applies.

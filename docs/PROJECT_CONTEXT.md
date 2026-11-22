# Project context

## Purpose and domain

FaraZaman is an Android client for municipal field work. A user selects a municipality/server, authenticates, receives roles, and enters a driver, municipal-contract-supervisor, or Contractor entry flow. Core concepts are municipality/instance, user role, work area/zone, mission, track, track point, waypoint, employer (`organ`), contract, daily cleaning item, violation, road/address, and GPX/ZIP upload.

## Module and architecture

- `:app` is the only Gradle module (`settings.gradle`).
- `app/src/main/java/Ir/co/tfs/farazaman/` contains Kotlin plus inherited Java from OSMTracker.
- Architecture is transitional:
  - `domain/`: authentication contracts/use case, validation, tracking policy, road-width classification.
  - `data/`: DTOs, repositories, Retrofit APIs, and local database implementation.
  - `presentation/`: Compose UI, login UI, base Activity, and Hilt ViewModels.
  - `activity/`: most screens and significant legacy orchestration/business logic.
  - `supervisor/`: supervisor navigation, mission/track control, daily-plan helpers, and local violation history.
  - `service/`: GPS foreground service and Retrofit interfaces.
- This is not strict Clean Architecture: some screens create Retrofit clients, query `ContentResolver`, write preferences, and navigate directly. Preserve behavior and extract seams incrementally only when the task requires it.

## Dependency injection

- `MyApplication.kt` is `@HiltAndroidApp`.
- `di/NetworkModule.kt` provides default `SharedPreferences`, Gson, secure token/user/role managers, `AuthInterceptor`, OkHttp, Retrofit, API interfaces, and `FormDataRepository`.
- Retrofit reads `BASE_URL` from default preferences when constructed. `NetworkModule.RetrofitProvider` can recreate a client when the URL changes.
- `di/AppModule.kt` binds `AuthRepositoryImpl` to `AuthRepository` and provides `LoginUseCase`. Room providers are commented out.
- Activities/Fragments using injection are marked `@AndroidEntryPoint`; ViewModels use `@HiltViewModel`.

## Networking and API flow

- Stack: Retrofit 2.9, OkHttp 4.12, Gson converter, coroutines.
- Default central URL: `AppConstants.BASE_URL` = `https://app.tfs.co.ir/`.
- `presentation/login/LoginActivity.kt` downloads municipality instances from the central registry, saves the selected city's `BASE_URL`, client ID, scope, and login mode, then authenticates against the selected configuration.
- Normal authenticated flow: UI/ViewModel or repository -> Retrofit interface -> shared OkHttp -> `AuthInterceptor` -> selected municipality server.
- `AuthInterceptor.kt` attaches the bearer token, refreshes expired tokens under `TokenManager`'s mutex, retries once on 401, and broadcasts auth failure through `AuthStateManager`.
- General timeouts are centralized in `util/NetworkTimeouts.kt`. Address lookup has a separate 2-second timeout and 10-second cooldown in `data/repository/AddressLookupCoordinator.kt`.
- Startup update discovery uses a separate unauthenticated OkHttp client from `di/AppUpdateModule.kt`; it never receives the municipality bearer token. `data/update/AppUpdateService.kt` checks the fixed HTTPS metadata endpoint and only accepts HTTPS APK URLs on `ftp.tfs.co.ir`.
- `util/SelectedServerUrl.kt` builds selected-server URLs for legacy upload paths. Driver, supervisor, `OpenStreetMapUpload`, and `GpxUploader` currently use the selected base URL.
- No `networkSecurityConfig` is declared in the manifest; API URLs found in active configuration are HTTPS.

## Authentication and token lifecycle

- Three city-configured modes exist in `presentation/login/LoginModeHelper.kt`:
  - web/OIDC (`MODE_WEB`, default), implemented with AppAuth in `auth/OidcAuthManager.kt` and dispatched through a browser Custom Tab (not an embedded WebView), so browser-backed OTP and passkeys remain available;
  - password grant (`MODE_PASSWORD`), implemented by `data/repository/AuthRepositoryImpl.kt`;
  - local authorization code (`MODE_LOCAL_REMOTE`), which uses the selected municipality's HTTPS `BaseAddress` for `/Authorize` and `/api/token`, with its configured `ClientId` and AppAuth PKCE.
- OIDC issuer is `https://identity.tfs.co.ir`; redirect URI is `ir.co.tfs.farazaman://signin-oidc`. The manifest also retains an `osmtracker` redirect scheme.
- Password grant tries selected-server endpoints `token` and `api/token`. Its legacy client secret is injected into `BuildConfig.LEGACY_CLIENT_SECRET` from local/environment configuration.
- `util/TokenManager.kt` owns token expiry, authorization headers, and refresh serialization. A valid session requires an access token; refresh tokens are optional in some successful responses.
- `util/SecureTokenStorage.kt` stores token strings encrypted in default preferences using AES/GCM from `util/AndroidKeyStoreCipher.kt`; it migrates old plaintext preference values on read. Expiry time stays in preferences.
- After login, `LoginActivity.kt` fetches `/api/AccountUser/Info` and `/api/AccountUser/Roles`; `UserManager.kt` and `RolesManager.kt` persist profile/role metadata in default preferences. A successful Roles response with no role data is authoritative: stale or fallback roles are not used, and role selection renders a dedicated no-access screen with a full logout action.
- `data/model/UserRoles.kt` preserves the legacy `roles` dictionary and also models `actorRoles`. Contractor authorization normally comes from the confirmed `Contractor` key in `actorRoles` (display value `پیمانکار`); `superAdministrator` is the deliberate exception and receives all app-supported roles. Super-admin recognition checks normalized keys/values from the actual Roles response; `defaultRoleName` cannot grant access when that response has no data.
- `presentation/base/BaseActivity.kt` listens to `AuthStateManager` broadcasts and redirects after invalid authentication. Explicit logout uses `util/LogoutCacheCleaner.kt` to clear encrypted/legacy auth state, profile/role and mission caches, selected-server/OIDC state, WebView data, and cache directories while preserving the track database and persistent user files.

## Persistence

- Active track storage is legacy SQLite, exposed through `data/db/TrackContentProvider.java` with authority `Ir.co.tfs.farazaman.provider`.
- `data/db/DatabaseHelper.java` creates/migrates `track`, `trackpoint`, and `waypoint`; database version 18 adds track role separation (`driver`/`supervisor`).
- `data/db/DataHelper.java` is the main helper for active tracks, start/stop operations, directories, and dates.
- Model/adapter classes for this database are under `data/db/model/` and `data/db/`.
- `data/db/room/AppDatabase.kt`, `dao/UserDao.kt`, and `model/UserEntity.kt` are inactive scaffolding; Room is not configured as the production database.
- Mission payloads are currently stored as JSON in default preferences using keys such as `mission_data_<trackId>`. Supervisor violation history is JSON in preferences via `supervisor/SupervisorViolationHistoryStore.kt`.
- An unuploaded supervisor mission keeps its GPS track across calendar days. While the supervisor host is visible, a changed local day causes the existing daily-plan Create/Index requests to run again with today's `visitdate`; the refreshed plan replaces `mission_data_<trackId>` on the same track, and stale-date violation entry/submission is blocked until this succeeds.
- Supervisor user actions are stored as a bounded, per-user JSON feed by `supervisor/actions/UserActionStore.kt`. This is a local integration seam because no unified server activity-feed contract is currently known.
- User info and role maps are also preference-backed. `android:allowBackup="false"` is set.

## UI and navigation

- Production screen content is Compose-based; no XML file remains under `app/src/main/res/layout/`.
  Activities/Fragments still own navigation, permissions, lifecycle and selected legacy service effects.
- Compose is enabled and used by `SplashActivity`/`presentation/compose/SplashScreen.kt` and `RoleSelectionActivity`/`RoleSelectionScreen.kt`, wrapped in `FaraZamanTheme`. Role selection mirrors the supervisor work-area visual shell with its exact green/white header colors, curved header, rounded-top content panel, profile/back controls, and notification bell/unread badge. A `superAdministrator` can select every app-supported role (supervisor, driver, and contractor).
- Compose screens expose immutable UI state as input and a single typed event callback as output (`RoleSelectionEvent`, `AppUpdateEvent`); Activities own navigation and side effects. New Compose screens should follow the same contract to remain directly previewable.
- Compose feature pages use `FzPageTemplate` for the shared green curved header and rounded body.
  `FzHeaderSize` selects compact/standard/expanded header intent. The template contains no bottom navigation;
  only supervisor Home/Today/History use `SupervisorScaffold` for that navigation.
- The settings destination is a State/Event Compose screen backed by the existing default
  `SharedPreferences`; the former `PreferenceActivity` and `res/xml/preferences.xml` are removed.
- Most Compose previews live in `app/src/debug/java/.../presentation/compose/`. `RoleSelectionScreen.kt` also keeps one inline preview so Android Studio can render the screen directly beside its implementation.
- Navigation uses explicit Intents and manual FragmentTransactions; no active `res/navigation` graph was found despite the Safe Args plugin.
- Launcher: `SplashActivity` asks `SplashViewModel` to check for an update, then routes to `LoginActivity` or `RoleSelectionActivity`. API failure is fail-open; a successfully reported mandatory update remains enforced for the current process. Role selection routes to `DriverActivity`, supervisor work-area selection/existing mission, or the Contractor shell according to stored access.
- Onboarding now runs after explicit role selection (or implicit single-role resolution), with separate
  supervisor, driver, and contractor content. Completion is stored per role, and the tutorial forwards to
  the exact destination Intent that would otherwise have opened directly.
- Update APKs are streamed into `cacheDir/app_updates` as `.part`, parsed and checked for package name/version/signing certificate, then installed through `PackageInstaller`. Android O+ unknown-source access is requested through system settings; no broad storage permission is used.
- Supervisor host `NewSupervisorActivity` keeps home/today/history Fragments and switches them manually.
- The supervisor bell opens the single `ActionsNotificationsActivity` destination. Its Actions tab is backed by a paged generic action model with data-driven operations. The Notifications tab pages the authenticated selected-server `api/AppNotification/Index` feed, opens details, marks unread items through `Visit/{id}`, and supports confirmed soft deletion. The server feed is the source of truth; Firebase only triggers display and refresh.
- `MyApplication.kt` applies Persian through AppCompat application locales, enforces global RTL decor direction, follows live system night-mode changes, and checks `DeveloperOptionsGuard` whenever an Activity resumes. Do not restore the old full-`Configuration` locale override because it also pinned the initial `uiMode` until process restart.
- Night resources exist in `values-night/` and `drawable-night/`; styles also have API-specific variants including `values-v27/`.
- Compose migration control: `UiMigrationFlagManager` owns protected Map/Camera/GPS/Track flags;
  flags default to Compose while Activity containers retain side effects and rollback paths.
  Track list/detail/waypoint screens live under `presentation/compose/track/`; deterministic system
  screenshots use `FakeComposeMapHost`, and screenshot tests run with `recordRoborazziDebug`/
  `verifyRoborazziDebug`.
- XML buttons use the shared `FaraZaman.Button.*` styles and state-aware palettes in `res/color/button_*.xml`; their semantic colors resolve through `values/colors.xml` and `values-night/colors.xml`. `FaraZamanTheme.kt` mirrors the same primary, surface, and error pairs for Compose.

## Tracking, maps, and background work

- `service/gps/GPSLogger.java` is a sticky foreground location Service declared with `foregroundServiceType="location"`. It listens to GPS and network providers, records points through the ContentProvider, and reacts to start/stop intents from `OSMTracker.java`. If Android recreates it after process death with a null intent, it resumes the active database track; if no active track exists it stops instead of leaving an idle service. Driver and supervisor starts use `startForegroundService`, and `util/BackgroundLocationPermissionHelper.kt` provides version-aware optional prompts for "allow all the time" access and removal of battery optimization.
- `activity/ViolationImageCaptureCoordinator.kt` is the shared camera-only `TakePicture`/MediaStore capture seam used by both Supervisor violation forms and reserved for the Contractor form. Failed/cancelled pending captures are removed.
- Violation submission reads the latest persisted `TrackPoint` metadata and maps its real coordinates,
  timestamp, accuracy, elevation, speed, heading, and pressure through `data/model/ViolationGeoLocationMapper.kt`.
  Invalid coordinates block submission; unavailable sensor fields are omitted instead of fabricated.
- When the active mission contains a usable `organFenceWkt` polygon, both standalone and embedded
  violation forms verify the latest GPS point with `OrganFenceContainment` before creating the API
  request. Points outside the polygon (including inside polygon holes) show a Persian error and are
  not sent; missions without a usable stored fence retain the existing submission behavior.
- `domain/location/LocationFixPolicy.kt` and `util/LocationAcquisitionCoordinator.kt` implement bounded, lifecycle-cancellable fresh-location acquisition followed by the best valid GPS/network last-known fallback. This helper is not connected to a Contractor submission until its API contract is known.

## Contractor correction status

- Confirmed entry: `activity/ContractorViolationsActivity.kt`; Contractor-only users route directly, while multi-role users select `پیمانکار` in the existing Compose role screen.
- Read-only map destination: `activity/ContractorViolationLocationActivity.kt`; it accepts only supplied valid coordinates and uses `BaranMapStyle`/`MapLibreOverlayController`.
- Reusable RTL layouts exist for the list, card, correction form, image preview, and read-only map.
- Actionable-list and correction endpoints, DTOs, multipart names, identifier types, and ContractId/OrganId placement are unknown. No production service/repository/request has been created for them; see `specs/002-contractor-violation-correction/contracts/contractor-violation-contract.md`.
- Contractor role selection now routes to a dedicated work-area shell and a separate operations shell using Contractor terminology (`شروع عملیات`, `ثبت اقدام`, `اصلاحیه`). The action form captures description and camera evidence locally. Operation/action/correction network actions remain explicitly unavailable and cannot report false success until the contracts in `specs/003-contractor-operations/` are supplied.
- `DriverActivity.kt` and `supervisor/SupervisorTrackController.kt` create/activate missions and start/stop GPS tracking.
- `DisplayTrackMap.kt` and `SubmitViolationActivity.kt` render native vector maps with MapLibre.
  When those maps are loaded, nearby rendered road features provide the street name first; the
  selected-municipality road API remains the fallback while tiles are unavailable or unnamed.
  `map/BaranMapStyle.kt` loads and caches `https://baran.tfs.co.ir/tiles/style.json`; if unavailable,
  it resolves `current.json` and generates the bundled light/dark fallback style. Map labels use the
  self-hosted `IRANYekan Regular` glyphs under `/map-assets/glyphs/{fontstack}/{range}.pbf`. `MapLibreOverlayController.kt` owns
  route, live-location, waypoint, and work-area GeoJSON layers. `DisplayTrackMapViewModel` still
  handles road information, GIS layers, and mission details.
- Track export/upload uses GPX generation, ZIP helpers, OkHttp/Retrofit, and selected-server `/api/GisGeolocation/upload` paths.
- No WorkManager worker or scheduled background job was found. `MediaButtonReceiver.java` is the only manifest receiver besides runtime auth/GPS receivers.

## Configuration and environments

- Firebase Cloud Messaging is configured through `app/google-services.json` and the Google Services Gradle plugin. Firebase BoM 33.5.1 is pinned for Kotlin 1.9.22 compatibility. After authentication, the app posts a JSON device record (FCM token, Installation ID when available, Firebase app/project/sender IDs, package, and platform) to the selected municipality at `api/AssetPersonNotify/FirebaseDeviceToken`. Foreground/data notifications are displayed immediately and trigger an unread-count refresh; background notification payloads are ingested when their system notification is opened. The authoritative list and read/delete state come from `api/AppNotification`.
- The unread notification count is fetched on login/foreground and after FCM receipt, retained as lightweight preference state, broadcast inside the package, and rendered as a badge on supervisor bell buttons. The app does not poll continuously.
- Mobile realtime uses the ASP.NET Core SignalR Java client against the selected municipality at `hubs/mobile?clientType=supervisor`. The neutral hub route is shared by current and future mobile actor types; `clientType` identifies the connected role. `MobileSupervisorHubClient` is application-scoped and currently connects only for an authenticated supervisor while the app is foreground, stops in background/logout/auth failure, and retries with bounded increasing delays. FCM remains active in both foreground and background. SignalR events are exposed as package-scoped broadcasts with JSON payloads.

- There are no product flavors and no compile-time dev/staging/prod URL split.
- Municipality/environment selection is runtime data in default preferences (`BASE_URL`, OIDC client ID/scope, login mode), populated from the central instance registry.
- BuildConfig contains `LEGACY_CLIENT_SECRET`; source is `local.properties` or `FARAZAMAN_LEGACY_CLIENT_SECRET`.
- App version code is 124; version name is generated from the build date. Debug appends `-dev`.
- Language resources are extensive, but runtime locale is explicitly Persian. `supportsRtl=true`.

## Debug and Release

- Debug is not minified, includes Compose tooling plus debug-source previews, and retains x86/x86_64 native libraries for emulators.
- Release has `minifyEnabled true`, `shrinkResources true`, packages only `armeabi-v7a` and `arm64-v8a`, and uses optimized default ProGuard plus `app/proguard-rules.pro`.
- Release is signed with the permanent `signing/farazaman-release.jks`; credentials and alias are read from `signing/release-signing.properties`. Gradle properties or `FARAZAMAN_SIGNING_*` environment variables remain supported as overrides/fallbacks.
- R8 rules preserve DTO fields, Retrofit annotations/generic signatures/interfaces, coroutine continuation metadata, authentication classes, secure token classes, and AppAuth. See `docs/RELEASE_NOTES.md` before diagnosing a Release-only API/auth issue.

## Major libraries

AndroidX/AppCompat/Lifecycle, Material, Compose Material 3/BOM, Hilt/Dagger, Retrofit/OkHttp/Gson, Kotlin coroutines, AppAuth, MapLibre Native/PMTiles, osmapi-traces, Coil/SVG, Media3 Transformer, PersianDate, Persian date picker, Speed Dial, JUnit 4, Robolectric, Mockito, PowerMock, Espresso, and JaCoCo.

## Fz component system

- Reusable Compose UI lives in `core:ui` and exposes project-owned `Fz*` APIs. MudBlazor is only a coverage reference; it is not a runtime dependency.
- Components use caller-owned state/callbacks, `FzComponentSize` (`Small`, `Medium`, `Large`), semantic intent colors and theme spacing/shapes. They must remain Persian-RTL, light/dark and accessibility safe.
- Browser-only concepts are mapped to native Android outcomes; for example responsive visibility uses `FzResponsive`, virtualized data uses lazy collections, and modal focus/back behavior remains internal to dialogs and overlays.
- The interactive catalog is Debug-only under `presentation/design/catalog`; production screens adopt components incrementally without creating new navigation, persistence or network sources of truth.

## Confirmed design constraints

- Selected-city routing is intentional and must be honored by every API/upload path.
- Driver and supervisor tracks share one database but are separated by the track `role` column.
- Tokens are encrypted at rest; non-token user/mission metadata remains in default preferences.
- XML and Compose coexist; migration is incremental rather than all-at-once.
- Release minification is required, so reflection-based network serialization must remain R8-safe.

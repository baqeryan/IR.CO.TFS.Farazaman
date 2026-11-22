# Migration Inventory

این فایل source of truth وضعیت مهاجرت UI است. رفتار فعلی مرجع Behavioral Parity و ظاهر XML baseline مقایسه است؛ مقصد Compose از Farazaman Material 3 Design System پیروی می‌کند.

## User-facing destinations

| Destination | Current source / layout | Owner and role | Risk | Target / bridge | Status | Rollback and removal condition |
|---|---|---|---|---|---|---|
| Splash / update | `SplashActivity`, `SplashScreen.kt`, `AppUpdateUi.kt` | System / all | Medium | Existing Compose → Fz theme/components | ParityReview | Version rollback; keep update installer behavior until golden and install flows pass |
| Login / city / OIDC | `LoginActivity`, `activity_login.xml`, login bottom sheets | Auth / all | High | Deferred XML→Compose | Inventoried | Code/version rollback; remove XML only after auth and Release smoke parity |
| Role selection / no access | `RoleSelectionActivity`, `RoleSelectionScreen.kt` | Auth / all roles | Low | Existing Compose → Fz consumer | Baseline | Code/version rollback after role/superAdministrator/no-access parity |
| Contractor work area | `ContractorWorkAreaSelectionActivity`, `activity_contractor_work_area_selection.xml` | Contractor | Medium | Compose Screen in existing Activity container | Baseline | Code/version rollback; remove binding after selection/redirect parity |
| Contractor operations | `ContractorOperationsActivity`, `activity_contractor_operations.xml` | Contractor | Medium | Compose Screen in Activity | Baseline | Code/version rollback after context/action routing parity |
| Contractor action | `ContractorActionActivity`, `activity_contractor_action.xml` | Contractor | High | Compose form; camera remains container-owned | Baseline | Code/version rollback; remove XML after validation/camera recreation parity |
| Contractor corrections | `ContractorViolationsActivity`, `activity_contractor_violations.xml`, item layouts | Contractor | Medium | Compose list/status mapping | Baseline | Code/version rollback; backend remains intentionally unconfigured |
| Contractor violation location | `ContractorViolationLocationActivity`, `activity_contractor_violation_location.xml` | Contractor | System-critical map | Compose host around MapLibre | Baseline | Protected Map flag; remove XML after rollout and lifecycle parity |
| Notifications/actions | `ActionsNotificationsActivity`, `FirebaseNotificationsFragment`, `UserActionsFragment`, related XML/items | Supervisor | Medium | Compose support screens in Hilt Activity | Inventoried | Code/version rollback; preserve Index/Visit/Delete/unread/FCM/SignalR and GPX export |
| Supervisor profile | `SupervisorProfileActivity`, `activity_supervisor_profile.xml` | Supervisor/Contractor | Low | Compose Screen in Activity | Inventoried | Code/version rollback after display/logout parity |
| Work-area selection | `SupervisorWorkAreaSelectionActivity`, `activity_supervisor_work_area_selection.xml` | Supervisor | High | Compose Screen; existing mission setup remains container-owned | Inventoried | Code/version rollback after API/persistence/navigation parity |
| Supervisor shell | `NewSupervisorActivity`; Compose Home/Today/History fragments | Supervisor | High | Responsive Compose shell with existing controller/repositories | In Progress | All destination XML removed; remove the temporary Fragment host after Activity-level state ownership cutover |
| Violation forms | submit/commitment Activities and fragments plus violation XML | Supervisor | System-critical camera | Compose form with external camera effect | Inventoried | Protected Camera flag; remove XML after permission/recreation/submit parity |
| Driver | `DriverActivity`, `activity_driver.xml` | Driver | System-critical GPS | Compose controls with external GPS service | Inventoried | Protected GPS flag; preserve `GPSLogger` and remove XML only after rollout |
| Track manager/logger/detail/waypoints/display/export/upload | Compose list/detail/waypoint Activities plus remaining logger/display/export/upload bridges | Driver/Supervisor | System-critical track/storage | Compose Screens over existing ContentProvider/export/upload | In Progress | TrackManager is a compatibility router and its XML/adapters are removed; waypoint XML/adapters are removed; preserve DB/provider/file contracts until logger/export/upload rollout |
| Track / violation maps | `DisplayTrackMap`, `SubmitViolationActivity`, MapLibre views/layouts | Driver/Supervisor | System-critical map | lifecycle-aware Compose map host | Inventoried | Protected Map flag; preserve Baran style and overlay controller |
| Intro/preferences/about/legacy presets | Manifest Activities and legacy XML | Support/system | Low–Medium | Later batch after operational flows | Inventoried | Code/version rollback; no removal before independent parity record |

## Coexistence policy

- Activity/Fragment containers continue owning Hilt, navigation, permissions, system launchers and lifecycle effects while their content migrates.
- Existing authenticated networking, runtime `BASE_URL`, repositories, ContentProvider, GPS service, MapLibre controllers and Firebase/SignalR behavior remain the source of truth.
- Only Map, Camera, GPS and Track receive a production runtime route flag. All flags default to Compose and are available solely through protected diagnostics.
- Every destination moves through `Not Started → In Progress → ParityReview → Migrated`. `Migrated` requires its independent checklist, screenshot evidence and rollback record.
- A legacy layout/binding/bridge is removed only after behavioral parity, accessibility, screenshot verification and the destination-specific rollout condition pass.

## Evidence fields

Per-destination review records owner, exact source paths, State/Event/Effect, side effects, dependencies, screenshot scenarios, intentional visual deltas, bridge removal condition, coexistence window and rollback steps. Foundation evidence is recorded by T030; Contractor acceptance is recorded in `reviews/contractor-architecture-gate.md`.

## Migration impact and approval

The migration changes presentation only. Backend payloads, selected municipality routing, auth/token storage, mission and track persistence, uploads and notification contracts remain compatible. The owner explicitly approved implementation by invoking `$speckit-implement` after reviewing the specification, plan, tasks and analysis on 2026-09-08.

Foundation evidence (2026-09-08): compileDebugKotlin, Contractor ViewModel tests, recordRoborazziDebug and verifyRoborazziDebug passed. Versioned goldens: app/src/test/snapshots/roborazzi/.

## Implementation gate evidence (2026-09-12)

- `:app:assembleDebug` passed with the low-memory single-worker Gradle configuration.
- `:app:testDebugUnitTest --tests "*SystemFlowsScreenshotTest"` passed; deterministic Driver,
  Track, Fake Map and Violation form captures were generated/verified by Roborazzi.
- `:app:compileDebugAndroidTestKotlin` passed, including the 20-cycle migration flag smoke test.
- `:app:lintDebug` passed after hardening the GPS stop receiver registration and explicitly
  isolating the Material badge API opt-in.
- `:app:lintRelease` passed with the same R8-compatible source and resource configuration.
- Release/ARM and connected-device smoke remain pending T073/T078 and are intentionally not marked
  as accepted by this evidence.
- `:app:assembleRelease` passed with R8/resource shrinking and the configured `armeabi-v7a`/
  `arm64-v8a` release ABI packaging. A physical/emulated ARM runtime smoke is still required.
- `:app:compileDebugAndroidTestKotlin` passed for `ManifestNavigationAuditTest`; migrated track
  containers are internal (`exported=false`) and the legacy entry remains enabled for rollback.
- `:app:recordRoborazziDebug :app:verifyRoborazziDebug` passed on 2026-09-12; Compose system-flow
  goldens are reproducible and verification completed successfully.
- Full `:app:testDebugUnitTest` passed on 2026-09-12.

## Incremental migration evidence (2026-09-25)

- Track list and waypoint destinations now render from Compose state; their legacy adapters and
  layouts were removed after reference audit. `TrackManager` remains only as an exported
  compatibility router for existing intents.
- `:app:compileDebugKotlin` and focused `*Track*` plus `*ComposeScreenArchitectureTest` unit tests
  passed after the initial list/waypoint removal.
- The authenticated track-upload form was migrated from `osm_upload.xml`/Java views to a pure
  `TrackUploadScreen` with an Activity-owned export/ZIP/network effect. The selected municipality
  URL, bearer token, ContentProvider fields and upload endpoint remain unchanged. The upload XML
  was removed and `:app:compileDebugKotlin` passed.
- GPX export is now launched directly by the Compose track list/detail containers with the legacy
  storage-permission compatibility retained. The legacy `TrackDetail`/`TrackDetailEditor`, their
  layouts, row variant and menu were removed.
- TrackLogger now owns one Compose composition and no screen layout XML. Its GPS indicator and
  user-defined operational button surface remain isolated lifecycle-owned AndroidView bridges so
  recording, sensors, camera/audio notes and custom layouts retain behavioral parity. The obsolete
  `DisplayTrack` canvas fallback was removed; all track display actions use the MapLibre destination.
- The new-mission bottom sheet now renders a pure State/Event Compose content for date,
  organ/contract selection, loading and submission. The authenticated API and mission persistence
  effects remain container-owned, and `fragment_new_mission_bottom_sheet.xml` was removed.
- `SubmitViolationActivity` now owns a single Compose map screen with Compose-owned MapLibre
  lifecycle, semantic map controls and State-driven road information. Location, route overlays and
  navigation to the violation form remain Activity effects; `activity_submit_violation.xml` was removed.
- `DisplayTrackMap` now uses the same lifecycle-aware Compose MapLibre host and Compose controls;
  its map XML and obsolete installer bridge were removed. GPS status is retained as a narrowly
  scoped AndroidView lifecycle boundary without an XML layout while its location listener remains imperative.
- The unused commitments Fragment/mission-row adapter and their layouts were removed after a
  reference audit. Dropdown rows now use the platform adapter resource. Five production layouts
  remain, all belonging to the active general/new/commitments violation-form family.
- `:app:compileDebugJavaWithJavac` passed after this migration slice on 2026-09-26.

## Final convergence evidence (2026-09-26)

- All production XML files under `app/src/main/res/layout/` were removed; the architecture test
  now blocks new `R.layout`, ViewBinding and DataBinding screen dependencies.
- Supervisor Home/Today/History and Actions/Notifications now render directly in single Compose
  shells; their `FragmentContainerView` bridges and six content Fragments were removed.
- Roborazzi baselines for all declared scenarios were recorded and `:app:verifyRoborazziDebug`
  passed.
- `:app:testDebugUnitTest` (133 tests), `:app:lintDebug`, `:app:assembleDebug`,
  `:app:lintRelease` and the signed/minified `:app:assembleRelease` passed.
- ARM runtime smoke and connected instrumentation remain device-dependent and were not claimed on
  this workstation because no running ARM device/emulator was available.

## Final legacy-surface cleanup evidence (2026-09-29)

- The last preference XML and `PreferenceActivity` were replaced by a pure State/Event Compose
  settings screen while preserving the existing default-`SharedPreferences` keys and logout flow.
- TrackLogger GPS-disabled and picture-source dialogs are Compose-owned. The GPX exporter no longer
  creates platform `ProgressDialog`/`AlertDialog` instances; presentation remains caller-owned.
- No XML file remains under `app/src/main/res/layout/`, and no `PreferenceScreen` XML remains under
  `app/src/main/res/xml/`. Remaining `BottomSheetDialogFragment`/Dialog hosts contain Compose content
  and retain only lifecycle/window ownership.
- `:app:testDebugUnitTest`, `:app:verifyRoborazziDebug`, `:app:lintDebug`, `:app:assembleDebug`,
  `:app:lintRelease`, signed/minified `:app:assembleRelease`, and
  `:app:compileDebugAndroidTestKotlin` passed on 2026-09-29.
- The installed `pixel_7_-_api_35` AVD targets x86_64 and terminated before registering with ADB.
  No ARM system image or connected physical ARM device is available, so connected/ARM smoke remains
  an explicit environmental blocker for T073/T078/T090.

## Phase 9 verification follow-up (2026-09-29)

- The shared feature-preview annotation now expands every `FzLightPreview` into the required
  Light+RTL, Dark+RTL, large-font and small-phone matrix.
- Missing independent previews were added for Settings, About, Supervisor Profile, Supervisor Work
  Area, User Actions, Waypoints, City Selection, OIDC Login and Zone Selection.
- `:app:compileDebugKotlin`, full `:app:testDebugUnitTest` and
  `:app:verifyRoborazziDebug` passed after the preview expansion.
- The complete local gate command passed after the preview expansion on 2026-09-29:
  `:app:testDebugUnitTest :app:verifyRoborazziDebug :app:lintDebug :app:assembleDebug
  :app:lintRelease :app:assembleRelease :app:compileDebugAndroidTestKotlin`. The signed/minified
  Release APK therefore passes R8 and resource shrinking on the final source state.
- The x86_64 AVD was retried after stopping stale emulator/QEMU processes, restarting ADB, disabling
  snapshots/audio/window rendering and selecting software graphics. With explicit console/gRPC
  ports Emulator 35.6.11 reached `Boot completed`, but could not register with the SDK ADB daemon:
  `Unable to connect to adb daemon on port: 5037`; `adb devices -l` remained empty even after an
  ADB server restart. The AVD image is x86_64, so it could not satisfy the real ARM smoke gate even
  if registration succeeded. Connected UI/navigation/lifecycle tests and real ARM smoke cannot be
  truthfully accepted until a working device is attached; T073/T078/T090 remain open for that reason.
- Android Studio Device Manager automation was also unavailable because the local computer-control
  runtime rejected `C:\Users\Bagherian\package.json` as an invalid package configuration. No file
  outside the repository was modified as part of this attempt.
- A clean official Platform Tools 37.0.1 distribution was then downloaded to the ignored build
  directory and selected explicitly with emulator `-adb-path`. The AVD again reached
  `Boot completed`, but both ADB 36.0.0 and 37.0.1 reported `no emulators found`; no emulator
  transport/listening ADB port was registered. A second attempt with explicit `-ports` and
  external-ADB mode produced the same result. The only locally installed system images are
  `android-36/google_apis_playstore/x86_64` and an incomplete
  `android-37.1/google_apis_playstore_ps16k/x86_64` installer directory; there is no ARM image.

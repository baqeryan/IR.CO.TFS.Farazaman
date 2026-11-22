# Feature-to-code map

Start here instead of recursively searching source. Fully qualified paths are repository-relative. For compact entries, paths beginning with `activity/`, `auth/`, `data/`, `di/`, `domain/`, `gpx/`, `presentation/`, `service/`, `supervisor/`, or `util/` are relative to `app/src/main/java/Ir/co/tfs/farazaman/`; bare layout names are under `app/src/main/res/layout/`.

## Application entry and lifecycle

- Firebase notifications: `app/google-services.json`; Google Services plugin and Messaging dependency in Gradle; `notification/FarazamanFirebaseMessagingService.kt`, `FirebaseDeviceTokenRegistrar.kt`, `FirebaseNotificationInboxStore.kt`, and `NotificationPermissionHelper.kt`. Server unread synchronization/badges are in `NotificationUnreadCountStore.kt` and `NotificationBadgeBinder.kt`. Firebase BoM 33.5.1 is pinned for Kotlin 1.9.22.
- Foreground supervisor realtime: `realtime/MobileSupervisorHubClient.kt` and `MobileSupervisorModels.kt`; application lifecycle ownership is in `MyApplication.kt`, and explicit logout stops the connection through `util/AuthHelper.kt`.

- Application: `app/src/main/java/Ir/co/tfs/farazaman/MyApplication.kt` — Hilt, Persian locale, RTL, system dark mode, developer-options enforcement.
- Manifest: `app/src/main/AndroidManifest.xml` — launcher, Activities, GPS service, providers, permissions, OIDC redirects.
- Launcher UI: `app/src/main/java/Ir/co/tfs/farazaman/activity/SplashActivity.kt`.
- Role-aware onboarding: `activity/Intro.kt` owns per-role completion and destination routing;
  `presentation/compose/IntroScreen.kt` renders the shared five-step tutorial UI. Role selection and
  single-role login paths enter it through `RoleTutorialLauncher` before opening the role destination.
- Compose screen/theme: `app/src/main/java/Ir/co/tfs/farazaman/presentation/compose/SplashScreen.kt`, `FaraZamanTheme.kt`.
- Shared Compose page chrome: `presentation/design/components/FzPageTemplate.kt` owns the green curved
  header, semantic header heights, and rounded content surface. It deliberately has no bottom navigation;
  the bottom navigation remains exclusive to supervisor Home/Today/History through `SupervisorScaffold.kt`.
- Shared day/night button system: `app/src/main/res/values/styles.xml` (`FaraZaman.Button.*`), `app/src/main/res/color/button_*.xml`, and the semantic palettes in `values/colors.xml` / `values-night/colors.xml`; Compose mirrors these pairs in `presentation/compose/FaraZamanTheme.kt`.
- Startup/update state and session routing: `presentation/viewmodel/SplashViewModel.kt`; `SplashActivity.kt` observes it, presents update UI, then routes to login or role selection.
- In-app update UI: `presentation/compose/AppUpdateUi.kt`.
- Update repository/policy: `data/repository/AppUpdateRepository.kt`, `domain/update/AppUpdatePolicy.kt`.
- Update API/download/parser: `data/update/AppUpdateService.kt`, `AppUpdateJsonParser.kt`, `AppUpdateConfig.kt`; uses a dedicated unauthenticated OkHttp client from `di/AppUpdateModule.kt`.
- APK validation/install: `data/update/ApkFileValidator.kt`, `update/AppUpdateInstaller.kt`, `AppUpdateInstallReceiver.kt`; permission/receiver are declared in `app/src/main/AndroidManifest.xml`.
- Debug previews: `app/src/debug/java/Ir/co/tfs/farazaman/presentation/compose/SplashScreenPreviews.kt`.

## Municipality selection and authentication

- Entry/UI: `app/src/main/java/Ir/co/tfs/farazaman/presentation/login/LoginActivity.kt`.
- City chooser: `presentation/login/LoginScreen.kt` uses the shared searchable `FzSelect` bottom sheet; city data and selection side effects remain owned by `LoginActivity.kt`.
- OIDC WebView: `presentation/login/OidcLoginBottomSheet.kt`; layout `bottom_sheet_oidc_login.xml`.
- Login ViewModel/state: `presentation/login/LoginViewModel.kt`, `LoginState.kt` (password flow support; Activity also orchestrates auth directly).
- Use case/contract: `domain/usecase/LoginUseCase.kt`, `domain/repository/AuthRepository.kt`.
- Repository: `data/repository/AuthRepositoryImpl.kt` — password/refresh endpoints and OIDC refresh dispatch.
- OIDC: `auth/OidcAuthManager.kt`; mode/config keys: `presentation/login/LoginModeHelper.kt`.
  `auth/LocalRemoteEndpoints.kt` builds the `LocalRemote` authorization/token endpoints from the selected HTTPS
  municipality `BaseAddress`; the authorization request uses that municipality's configured `ClientId`.
- Token handling: `util/TokenManager.kt`, `SecureTokenStorage.kt`, `AndroidKeyStoreCipher.kt`, `AuthInterceptor.kt`, `AuthStateManager.kt`, `AuthHelper.kt`.
- Logout/cache clearing: `util/LogoutCacheCleaner.kt`; called by `AuthHelper.kt` and the Compose-backed `activity/Preferences.kt` settings destination.
- Profile/roles: `service/remote/UserService.kt`, `RolesService.kt`; models `data/model/UserInfo.kt`, `UserRoles.kt`; stores `util/UserManager.kt`, `RolesManager.kt`.
- Models: `data/model/req/LoginRequest.kt`, `data/model/res/LoginResponse.kt`, `ProvinceInstance.kt`, `domain/model/LoginResult.kt`, `User.kt`.
- DI: `di/AppModule.kt`, `di/NetworkModule.kt`.
- Redirect configuration: `app/src/main/AndroidManifest.xml`.

## Role selection and profile

- Entry/UI: `activity/RoleSelectionActivity.kt` + `presentation/compose/RoleSelectionScreen.kt`; its Compose header mirrors the work-area shell and observes the shared unread-notification count for the bell badge.
- State/domain choice: `RoleSelectionUiState`, `RoleOption` in `RoleSelectionScreen.kt`; stored role interpretation in `util/RolesManager.kt`.
- Role response: `data/model/UserRoles.kt` models both legacy `roles` and confirmed `actorRoles`; Contractor is the `Contractor` actor-role key with label `پیمانکار`.
- Supervisor profile: `activity/SupervisorProfileActivity.kt`, layout `activity_supervisor_profile.xml`.
- Routing helper: `supervisor/SupervisorMissionHelper.kt`.
- Debug previews: `app/src/debug/java/Ir/co/tfs/farazaman/presentation/compose/RoleSelectionScreenPreviews.kt`.

## Contractor violation correction (partial; API contract pending)

- Entry/routing: `presentation/login/LoginActivity.kt` (Contractor-only) and `activity/RoleSelectionActivity.kt` (multi-role).
- List shell: `activity/ContractorViolationsActivity.kt`; layout `res/layout/activity_contractor_violations.xml`.
- Card/form UI shells: `res/layout/item_contractor_violation.xml`, `activity_contractor_correction.xml`, `bottom_sheet_violation_image_preview.xml`.
- Read-only map: `activity/ContractorViolationLocationActivity.kt`; layout `activity_contractor_violation_location.xml`; shared map code `map/BaranMapStyle.kt`, `map/MapLibreOverlayController.kt`.
- Camera: `activity/ViolationImageCaptureCoordinator.kt`; current shared consumers are `SubmitViolationFormActivity.kt` and `CommitmentsViolationFormFragment.kt`.
- Location: `domain/location/LocationFixPolicy.kt`, `util/LocationAcquisitionCoordinator.kt`, `util/GpsAccuracyHelper.kt`.
- Draft validation independent of transport: `domain/validation/ContractorCorrectionValidator.kt`.
- Tests: `util/RolesManagerContractorTest.kt`, `domain/location/LocationFixPolicyTest.kt`, `util/LocationAcquisitionCoordinatorTest.kt`, `domain/validation/ContractorCorrectionValidatorTest.kt` under `app/src/test/java/Ir/co/tfs/farazaman/`.
- API/repository/ViewModels/DTOs: `Unknown / not confirmed`. Do not add methods to `ViolationApiService`, multipart field names, or request models until both contracts in `specs/002-contractor-violation-correction/contracts/contractor-violation-contract.md` are supplied.

## Contractor operations (UI foundation; service contracts pending)

- Role routing: `activity/RoleSelectionActivity.kt` opens `ContractorWorkAreaSelectionActivity.kt` before Contractor operations.
- Operations shell: `activity/ContractorOperationsActivity.kt`; action form: `ContractorActionActivity.kt`; layouts are `activity_contractor_work_area_selection.xml`, `activity_contractor_operations.xml`, and `activity_contractor_action.xml`.
- Safe integration seam and local selected-context holder: `contractor/ContractorGateway.kt`, `ContractorWorkContextStore.kt`; draft validation: `ContractorDraftValidator.kt`.
- Corrections reuse `ContractorViolationsActivity.kt` and its existing correction/map layouts. No endpoint or DTO is implemented until the catalog in `specs/003-contractor-operations/contracts/contractor-gateway.md` is supplied.

## Driver mission

- Entry/UI/controller: `activity/DriverActivity.kt`; layout `app/src/main/res/layout/activity_driver.xml`.
- Work-area API: `service/remote/VehicleZoneWorkService.kt` (`GET api/Application/VehicleZoneWork`).
- Work-area model/UI: `data/model/VehicleZoneWorkResponse.kt`, `activity/ZoneSelectionBottomSheet.kt`, `ZoneWorkAdapter.kt`.
- Mission/track storage: `data/db/DataHelper.java`, `TrackContentProvider.java`.
- GPS: `service/gps/GPSLogger.java`; intent constants in `OSMTracker.java`.
- Map transition: `activity/DisplayTrackMap.kt`.
- Upload: driver upload logic in `DriverActivity.kt`, endpoint `/api/GisGeolocation/upload`.
- Local configuration: default SharedPreferences holds driver mode, selected zone, and mission/track metadata.

## Supervisor work area and shell

- Work-area entry: `activity/SupervisorWorkAreaSelectionActivity.kt`; layout `activity_supervisor_work_area_selection.xml`.
- Work-area API/model: `VehicleZoneWorkService.kt`, `VehicleZoneWorkResponse.kt`.
- Organ activity fence: `data/api/FormDataApiService.kt` (`POST api/GisLayersDataFenceOrgan/Details`), typed request/response in `data/model/OrganFenceResponse.kt`; fetched by `supervisor/SupervisorMissionSetup.kt` after confirmation.
- Fence persistence/map: WKT is stored as `organFenceWkt` (and `organFenceName`) inside `mission_data_<trackId>`; `activity/DisplayTrackMap.kt` draws it as a low-opacity polygon below the GPS route.
- Main host: `activity/NewSupervisorActivity.kt`; layout `activity_new_supervisor.xml`.
- Tabs: `activity/SupervisorHomeFragment.kt`, `SupervisorTodayFragment.kt`, and `SupervisorHistoryFragment.kt` host pure Compose screens in `presentation/compose/`. The Activity still owns the temporary Fragment host until the final supervisor shell cutover.
- Bottom navigation: `supervisor/SupervisorBottomNavHelper.kt`, `SupervisorNavTab` in the same helper file.
- Mission routing/setup: `supervisor/SupervisorMissionHelper.kt`, `SupervisorMissionSetup.kt`, `SupervisorMissionHost.kt`.
- Tracking/controller: `supervisor/SupervisorTrackController.kt`.
- Profile: `supervisor/SupervisorProfileHelper.kt`, `activity/SupervisorProfileActivity.kt`.

## New supervisor mission and employer/contract lists

- Entry/UI: `activity/NewMissionBottomSheetFragment.kt`; layout `fragment_new_mission_bottom_sheet.xml`.
- Employer list: `NewMissionBottomSheetFragment.fetchOrgans()` maps `FormDataResponse.response.organs` to `MissionSelectionOption`.
- Contract list: `NewMissionBottomSheetFragment.fetchContracts()` maps `FormDataResponse.response.contracts`; selection depends on employer ID.
- Selection UI: `activity/MissionItemSelectionBottomSheet.kt`, `MissionSelectionAdapter.kt`, `MissionSelectionOption.kt`.
- Alternate ViewModel implementation: `presentation/viewmodel/NewMissionViewModel.kt`.
- Repository/API: `data/repository/FormDataRepository.kt`, `data/api/FormDataApiService.kt`.
- Work-area cascade: `GET api/Bill/Lookup/ContractType` returns only contract-type/tenant pairs allowed for the signed-in user. The selected tenant is persisted after confirmation, `GET api/Bill/Lookup/Organs?tenantId={tenantId}` and `GET api/Bill/Lookup/Contracts?tenantId={tenantId}&organId={id}` use it explicitly, and subsequent `/api/Bill...` requests receive the same value through `X-Tenant-Id`. Separate cascade routes remain `POST api/Bill/Lookup/ViolationGroups`, `Violations`, `ItemGroups`, `OriginItems`, and `ViolationDetails`; mission creation continues through `POST api/BillOriginCleaningItemDailyItem/Create`, `Index`, and `Details/{encryption}`.
- Models: `data/model/FormDataResponse.kt`, `MissionIndexResponse.kt`.
- DI/base URL/auth: `di/NetworkModule.kt`, `util/AuthInterceptor.kt`.
- Local result: mission JSON under `mission_data_<trackId>` in default preferences; associated track in ContentProvider.
- Day rollover: `SupervisorMissionDayPolicy.kt` detects a stale `visitDate`; `SupervisorMissionSetup.refreshMissionForDateIfNeeded()` repeats the existing Create/Index daily-plan flow for the new date while retaining the same unuploaded GPS track. `NewSupervisorActivity` checks on resume and periodically while visible, and violation entry/submission paths reject stale mission dates.
- First files to inspect when this list fails: `NewMissionBottomSheetFragment.kt`, `FormDataApiService.kt`, `FormDataResponse.kt`, `NetworkModule.kt`, `AuthInterceptor.kt`.

## Supervisor actions and notifications

- Bell destination: `activity/ActionsNotificationsActivity.kt`; layout `activity_actions_notifications.xml`; declared once in `app/src/main/AndroidManifest.xml` and opened by `NewSupervisorActivity`.
- Tabs: `activity/UserActionsFragment.kt` and `FirebaseNotificationsFragment.kt`; notification rows are rendered by `FirebaseNotificationAdapter.kt`. Server contract/model/repository/state are `service/remote/AppNotificationApi.kt`, `data/model/AppNotificationModels.kt`, `data/repository/AppNotificationRepository.kt`, and `presentation/viewmodel/NotificationsViewModel.kt`.
- Feed UI: `activity/UserActionAdapter.kt`, `item_user_action.xml`, `item_user_action_operation.xml`, and `fragment_user_actions.xml`.
- State/pagination: `presentation/viewmodel/ActionsViewModel.kt`; generic local models, persistence, availability policy, and repository are under `supervisor/actions/`.
- Event producers: `SupervisorMissionSetup.kt`, `NewMissionBottomSheetFragment.kt`, `SupervisorTrackController.kt`, and `SupervisorViolationHistoryStore.kt`.
- GPX retention/download: `supervisor/actions/SupervisorActionGpxFiles.kt` preserves a successful mission GPX under app-internal files; `ActionsNotificationsActivity` uses the system document destination picker and streams the file to the user-selected location, so no GPX viewer app is required.
- No unified server activity endpoint/DTO is known. The current feed is bounded, per-user local history and imports only trustworthy locally available mission/violation records.
- Policy tests: `app/src/test/java/Ir/co/tfs/farazaman/supervisor/actions/UserActionFeedPolicyTest.kt`.
- Mission day-rollover policy tests: `app/src/test/java/Ir/co/tfs/farazaman/supervisor/SupervisorMissionDayPolicyTest.kt`.

## Today visit and daily plan

- Container: `activity/SupervisorTodayFragment.kt`.
- Shared supervisor bottom navigation: `supervisor/SupervisorBottomNavHelper.kt` and `res/layout/include_supervisor_bottom_nav.xml`, hosted by `res/layout/activity_new_supervisor.xml`.
- Tabs/content: `activity/TodayVisitTabFragment.kt`; layout `fragment_today_visit_tab.xml`.
- List UI: `activity/DailyPlanItemsAdapter.kt`, `supervisor/DailyPlanItem.kt`.
- Actions: `supervisor/TodayVisitMissionActions.kt`, `DailyPlanItemViolationHelper.kt`.
- Mission source: `mission_data_<trackId>` JSON in default preferences.
- Map/violation navigation: `TodayVisitMissionActions.kt`.

## Violation registration

- Map-based entry: `activity/SubmitViolationActivity.kt`; layout `activity_submit_violation.xml`; ViewModel `presentation/viewmodel/SubmitViolationViewModel.kt`.
- General form: `activity/SubmitViolationFormActivity.kt`; newer subclass `NewSubmitViolationFormActivity.kt`; commitment subclass `CommitmentsSubmitViolationFormActivity.kt`.
- Embedded supervisor form: `activity/CommitmentsViolationFormFragment.kt`; layout `fragment_commitments_violation_form.xml`.
- Main form ViewModel: `presentation/viewmodel/FormDataViewModel.kt`.
- Validation: `domain/validation/ViolationFormValidator.kt`.
- Form repository/API: `data/repository/FormDataRepository.kt`, `data/api/FormDataApiService.kt` (`POST api/Bill/Lookup/ViolationEvent`).
- Submit API: `service/remote/ViolationApiService.kt` (`multipart POST api/BillOriginViEvent/Create`).
- Models/location payload: `data/model/FormDataResponse.kt`, `ViolationModel.kt`,
  `ViolationGeoLocationMapper.kt`; the mapper converts the latest persisted `TrackPoint` into the
  violation GeoJSON/WKT/track payload without fabricated sensor values.
- Fence submission guard: `supervisor/OrganFenceContainment.kt`; `SubmitViolationFormActivity` and
  the embedded form in `NewSupervisorActivity` pass the mission `organFenceWkt` into
  `FormDataViewModel.submitViolation()`, which rejects an outside GPS point before any server call.
- Images: `activity/SelectedImagesAdapter.kt`; `FileProvider` configuration in manifest and `res/xml/filepaths.xml`.
- Local waypoint save: `FormDataViewModel.saveViolationAsWaypoint()` -> `TrackContentProvider`.
- Supervisor local history: `supervisor/SupervisorViolationHistoryStore.kt`.

## Address, road name, and road width

- Road API: both `service/remote/RoadService.kt` and the raw `service/remote/ApiService.kt` use `api/GisLayer/DataLayerRoadByPoint`.
- Coordinator: `data/repository/AddressLookupCoordinator.kt` — serialized calls, 2s timeout, 10s cooldown.
- Model: `data/model/RoadData.kt`.
- Width mapping: `domain/road/RoadWidthCategory.kt`.
- Location-change policy: `util/AddressLocationHelper.kt`, tests in `app/src/test/java/.../util/AddressLocationHelperTest.kt`.
- Consumers: `presentation/viewmodel/DisplayTrackMapViewModel.kt`, `SubmitViolationViewModel.kt`, `activity/DisplayTrackMap.kt`, `SupervisorTodayFragment.kt`, `CommitmentsViolationFormFragment.kt`, `SubmitViolationFormActivity.kt`.

## Track recording and local database

- Contract/provider: `data/db/TrackContentProvider.java` — URIs/schema for `track`, `trackpoint`, `waypoint`.
- SQLite creation/migrations: `data/db/DatabaseHelper.java`.
- Operations: `data/db/DataHelper.java`.
- Models: `data/db/model/Track.java`, `TrackPoint.java`, `WayPoint.java`, `Point.java`.
- Foreground service: `service/gps/GPSLogger.java`; connections `GPSLoggerServiceConnection.java`, `GPSLoggerServiceConnectionDisplay.java`.
- Intent/preferences constants: `OSMTracker.java`.
- Track policy: `domain/tracking/TrackingPointPolicy.kt`.
- Driver orchestration: `activity/DriverActivity.kt`; supervisor orchestration: `supervisor/SupervisorTrackController.kt`.
- Manifest: service and ContentProvider declarations in `app/src/main/AndroidManifest.xml`.

## Track map and GIS layers

- UI: `activity/DisplayTrackMap.kt` with pure Compose contract in `presentation/compose/TrackMapScreen.kt` and the shared `map/ComposeMapHost.kt` bridge.
- ViewModel: `presentation/viewmodel/DisplayTrackMapViewModel.kt`.
- Map library: MapLibre Native Android (`org.maplibre.gl:android-sdk`); dependency in `app/build.gradle`.
- Baran basemap: `map/BaranMapStyle.kt` loads/caches `https://baran.tfs.co.ir/tiles/style.json`;
  it falls back to `current.json` plus an internally generated style. Labels use Baran-hosted
  `IRANYekan Regular` glyph PBF files. Future tile authentication belongs here.
- Dynamic overlays/camera: `map/MapLibreOverlayController.kt` renders track lines, current
  location, waypoints, and work-area polygons as GeoJSON sources/layers.
- Secondary map: `activity/SubmitViolationActivity.kt`; layout `activity_submit_violation.xml`.
- SVG/images outside the basemap: Coil.
- Road APIs: `RoadService.kt`, `ApiService.kt`.
- GIS layer endpoint/model: `ApiService.getGisLayerIndex()`, `data/model/GisLayerIndexResponse.kt`.
- Mission details: `FormDataApiService.getMissionDetails()`.
- Local path: `TrackContentProvider.trackPointsUri(trackId)` and waypoint URI helpers.

## Track management, export, and upload

- Screens: `activity/TrackManager.kt` is now only a compatibility router; list/detail/waypoint/upload destinations are `TrackManagerComposeActivity.kt`, `TrackDetailComposeActivity.kt`, `WaypointListComposeActivity.kt`, and `OpenStreetMapUpload.kt`. `TrackLogger.kt` owns the GPS/service effects around a single Compose screen; its custom button layout and GPS indicator are isolated AndroidView bridges. The obsolete non-map `DisplayTrack.java` fallback was removed and all display routing uses `DisplayTrackMap.kt`.
- ViewModel: `presentation/viewmodel/TrackManagerViewModel.kt`.
- The legacy track-list and waypoint adapters/layouts have been removed; Compose lazy lists render those destinations.
- GPX: `util/GpxWriter.java`, `util/GpxZipper.java`, and export tasks under `gpx/`; database export task `data/db/ExportDatabaseTask.java`.
- Upload service/API: `service/remote/UploadService.kt`, `util/GpxUploader.java`, `util/SelectedServerUrl.kt`.
- Endpoint: `api/GisGeolocation/upload` on selected municipality server.
- Compose migration: `presentation/compose/track/TrackScreens.kt`,
  `activity/TrackManagerComposeActivity.kt`, `TrackDetailComposeActivity.kt`, and
  `WaypointListComposeActivity.kt`; route selection is controlled by `UiMigrationFlow.Track`.
- Deterministic map surface for previews/goldens: `map/ComposeMapHost.kt` (`FakeComposeMapHost`);
  native MapLibre lifecycle remains container-owned through the Map migration flag.
- Legacy OSM API library: `osm/` and `osmapi-traces` dependency.

## Networking

- Retrofit/OkHttp DI: `di/NetworkModule.kt`.
- Base URL: `AppConstants.kt`, runtime preference `BASE_URL`, helper `util/SelectedServerUrl.kt`.
- Interceptor/refresh: `util/AuthInterceptor.kt`, `AuthStateManager.kt`, `TokenManager.kt`.
- Timeouts: `util/NetworkTimeouts.kt`; address timeout/cooldown: `AddressLookupCoordinator.kt`.
- API services: `data/api/FormDataApiService.kt`, and all interfaces under `service/remote/`.
- Error handling: distributed across repositories, ViewModels, and legacy screens; there is no single global API error mapper.

## Dependency injection

- Application root: `MyApplication.kt`.
- Network/services/managers: `di/NetworkModule.kt`.
- Auth repository/use case: `di/AppModule.kt`.
- Room providers in `AppModule.kt` are commented out; do not assume Room is active.

## Release build

- Gradle: `app/build.gradle`, root `build.gradle`, `gradle.properties`.
- R8: `app/proguard-rules.pro` and `proguard-android-optimize.txt` selected by `app/build.gradle`.
- Manifest: `app/src/main/AndroidManifest.xml`; no Release-specific manifest was found.
- Network security: no `network_security_config.xml` and no manifest `networkSecurityConfig` attribute were found.
- Signing: `app/build.gradle` loads `signing/release-signing.properties` and signs with `signing/farazaman-release.jks`; `signing.*` Gradle properties and `FARAZAMAN_SIGNING_*` environment variables are supported fallbacks.
- Secret configuration: `farazaman.legacyClientSecret` or `FARAZAMAN_LEGACY_CLIENT_SECRET` -> `BuildConfig.LEGACY_CLIENT_SECRET`.
- Debug-only source: Compose previews under `app/src/debug/java/`.
- Release investigation checklist: `docs/RELEASE_NOTES.md`.

## Tests

- Login validation: `app/src/test/java/Ir/co/tfs/farazaman/domain/validation/LoginInputValidatorTest.kt`.
- Violation validation: `.../ViolationFormValidatorTest.kt`.
- Tracking: `.../domain/tracking/TrackingPointPolicyTest.kt`.
- Road width: `.../domain/road/RoadWidthCategoryTest.kt`.
- Address lookup/location: `.../data/repository/AddressLookupCoordinatorTest.kt`, `.../util/AddressLocationHelperTest.kt`.
- Token expiry: `.../auth/TokenExpiryPolicyTest.kt`.
- Developer guard: `.../security/DeveloperOptionsGuardTest.kt`.
- No instrumented test class was found under `app/src/androidTest`; only resources/assets are present.

## Fz component system and catalog

- Production components: `core/ui/src/main/java/Ir/co/tfs/farazaman/presentation/design/components/`.
- Shared sizes, semantic intents, theme tokens and shapes: `core/ui/src/main/java/Ir/co/tfs/farazaman/presentation/design/theme/`.
- Caller-owned selection, field, collection and overlay state: `core/ui/src/main/java/Ir/co/tfs/farazaman/presentation/design/state/`.
- Debug-only interactive catalog: `app/src/debug/java/Ir/co/tfs/farazaman/presentation/design/catalog/`.
- Contract, semantics and Roborazzi tests: `app/src/test/java/Ir/co/tfs/farazaman/presentation/design/` and `core/ui/src/test/java/Ir/co/tfs/farazaman/presentation/design/`.
- Coverage and native-mobile mappings: `specs/006-fz-component-system/contracts/component-catalog.md`.
- Use public `Fz*` components instead of screen-local reusable styling. Keep state and side effects in the caller, use `FzComponentSize` and semantic variants, and preserve Persian RTL, dark mode and a 48dp minimum touch target.

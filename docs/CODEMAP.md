# Feature-to-code map

Start here instead of recursively searching source. Fully qualified paths are repository-relative. For compact entries, paths beginning with `activity/`, `auth/`, `data/`, `di/`, `domain/`, `gpx/`, `presentation/`, `service/`, `supervisor/`, or `util/` are relative to `app/src/main/java/Ir/co/tfs/farazaman/`; bare layout names are under `app/src/main/res/layout/`.

## Application entry and lifecycle

- Application: `app/src/main/java/Ir/co/tfs/farazaman/MyApplication.kt` — Hilt, Persian locale, RTL, system dark mode, developer-options enforcement.
- Manifest: `app/src/main/AndroidManifest.xml` — launcher, Activities, GPS service, providers, permissions, OIDC redirects.
- Launcher UI: `app/src/main/java/Ir/co/tfs/farazaman/activity/SplashActivity.kt`.
- Compose screen/theme: `app/src/main/java/Ir/co/tfs/farazaman/presentation/compose/SplashScreen.kt`, `FaraZamanTheme.kt`.
- Startup/update state and session routing: `presentation/viewmodel/SplashViewModel.kt`; `SplashActivity.kt` observes it, presents update UI, then routes to login or role selection.
- In-app update UI: `presentation/compose/AppUpdateUi.kt`.
- Update repository/policy: `data/repository/AppUpdateRepository.kt`, `domain/update/AppUpdatePolicy.kt`.
- Update API/download/parser: `data/update/AppUpdateService.kt`, `AppUpdateJsonParser.kt`, `AppUpdateConfig.kt`; uses a dedicated unauthenticated OkHttp client from `di/AppUpdateModule.kt`.
- APK validation/install: `data/update/ApkFileValidator.kt`, `update/AppUpdateInstaller.kt`, `AppUpdateInstallReceiver.kt`; permission/receiver are declared in `app/src/main/AndroidManifest.xml`.
- Debug previews: `app/src/debug/java/Ir/co/tfs/farazaman/presentation/compose/SplashScreenPreviews.kt`.

## Municipality selection and authentication

- Entry/UI: `app/src/main/java/Ir/co/tfs/farazaman/presentation/login/LoginActivity.kt`.
- City chooser: `presentation/login/CitySelectionBottomSheet.kt`, `CitySelectionAdapter.kt`; layouts `bottom_sheet_city_selection.xml`, `item_city_selection.xml`.
- OIDC WebView: `presentation/login/OidcLoginBottomSheet.kt`; layout `bottom_sheet_oidc_login.xml`.
- Login ViewModel/state: `presentation/login/LoginViewModel.kt`, `LoginState.kt` (password flow support; Activity also orchestrates auth directly).
- Use case/contract: `domain/usecase/LoginUseCase.kt`, `domain/repository/AuthRepository.kt`.
- Repository: `data/repository/AuthRepositoryImpl.kt` — password/refresh endpoints and OIDC refresh dispatch.
- OIDC: `auth/OidcAuthManager.kt`; mode/config keys: `presentation/login/LoginModeHelper.kt`.
- Token handling: `util/TokenManager.kt`, `SecureTokenStorage.kt`, `AndroidKeyStoreCipher.kt`, `AuthInterceptor.kt`, `AuthStateManager.kt`, `AuthHelper.kt`.
- Logout/cache clearing: `util/LogoutCacheCleaner.kt`; called by `AuthHelper.kt` and legacy `activity/Preferences.java`.
- Profile/roles: `service/remote/UserService.kt`, `RolesService.kt`; models `data/model/UserInfo.kt`, `UserRoles.kt`; stores `util/UserManager.kt`, `RolesManager.kt`.
- Models: `data/model/req/LoginRequest.kt`, `data/model/res/LoginResponse.kt`, `ProvinceInstance.kt`, `domain/model/LoginResult.kt`, `User.kt`.
- DI: `di/AppModule.kt`, `di/NetworkModule.kt`.
- Redirect configuration: `app/src/main/AndroidManifest.xml`.

## Role selection and profile

- Entry/UI: `activity/RoleSelectionActivity.kt` + `presentation/compose/RoleSelectionScreen.kt`.
- State/domain choice: `RoleSelectionUiState`, `RoleOption` in `RoleSelectionScreen.kt`; stored role interpretation in `util/RolesManager.kt`.
- Supervisor profile: `activity/SupervisorProfileActivity.kt`, layout `activity_supervisor_profile.xml`.
- Routing helper: `supervisor/SupervisorMissionHelper.kt`.
- Debug previews: `app/src/debug/java/Ir/co/tfs/farazaman/presentation/compose/RoleSelectionScreenPreviews.kt`.

## Driver mission

- Entry/UI/controller: `activity/DriverActivity.kt`; layout `app/src/main/res/layout/activity_driver.xml`.
- Work-area API: `service/remote/VehicleZoneWorkService.kt` (`GET /api/ApplicationUser/VehicleZoneWork`).
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
- Tabs: `activity/SupervisorHomeFragment.kt`, `SupervisorTodayFragment.kt`, `SupervisorHistoryFragment.kt` with matching `fragment_supervisor_*.xml` layouts.
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
- Endpoints: `POST api/BillOriginViEvent/FormData`, `POST api/BillOriginCleaningItemDailyItem/Create`, `POST .../Index`, `POST .../Details/{encryption}`.
- Models: `data/model/FormDataResponse.kt`, `MissionIndexResponse.kt`.
- DI/base URL/auth: `di/NetworkModule.kt`, `util/AuthInterceptor.kt`.
- Local result: mission JSON under `mission_data_<trackId>` in default preferences; associated track in ContentProvider.
- First files to inspect when this list fails: `NewMissionBottomSheetFragment.kt`, `FormDataApiService.kt`, `FormDataResponse.kt`, `NetworkModule.kt`, `AuthInterceptor.kt`.

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
- Form repository/API: `data/repository/FormDataRepository.kt`, `data/api/FormDataApiService.kt` (`POST api/BillOriginViEvent/FormData`).
- Submit API: `service/remote/ViolationApiService.kt` (`multipart POST api/BillOriginViEvent/Create`).
- Models: `data/model/FormDataResponse.kt`, `ViolationModel.kt`.
- Images: `activity/SelectedImagesAdapter.kt`; `FileProvider` configuration in manifest and `res/xml/filepaths.xml`.
- Local waypoint save: `FormDataViewModel.saveViolationAsWaypoint()` -> `TrackContentProvider`.
- Supervisor local history: `supervisor/SupervisorViolationHistoryStore.kt`.

## Address, road name, and road width

- API: `service/remote/RoadService.kt` (`api/GisLayer/DataLayerRoadByPoint`) and legacy/raw `service/remote/ApiService.kt` (`api/GisLayers/DataLayerRoadByPoint`). Note the path spelling difference.
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

- UI: `activity/DisplayTrackMap.kt`; layout `app/src/main/res/layout/displaytrackmap.xml`.
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

- Screens: `activity/TrackManager.kt`, `TrackLogger.kt`, `DisplayTrack.java`, `TrackDetail.java`, `WaypointList.java`, `OpenStreetMapUpload.java`.
- ViewModel: `presentation/viewmodel/TrackManagerViewModel.kt`.
- Adapters: `activity/TrackListRVAdapter.kt`, `data/db/TracklistAdapter.java`, `WaypointListAdapter.java`.
- GPX: `util/GpxWriter.java`, `util/GpxZipper.java`, and export tasks under `gpx/`; database export task `data/db/ExportDatabaseTask.java`.
- Upload service/API: `service/remote/UploadService.kt`, `util/GpxUploader.java`, `util/SelectedServerUrl.kt`.
- Endpoint: `api/GisGeolocation/upload` on selected municipality server.
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

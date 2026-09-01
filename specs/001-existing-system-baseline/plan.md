# Implementation Plan: Existing-System Baseline

**Branch**: `001-existing-system-baseline` | **Date**: 2026-09-01 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/001-existing-system-baseline/spec.md`

**Note**: This is a documentation and validation plan for an existing production system. It does
not authorize application implementation, refactoring, migration, or architectural replacement.

## Summary

Capture the current FaraZaman Android architecture as an evidence-backed baseline. The plan preserves
the single-module, hybrid XML/Compose, partial ViewModel/repository/Hilt, legacy SQLite/preferences,
runtime municipality routing, dual authentication, explicit navigation, and foreground tracking
patterns actually present in the repository. It records server-populated packaged UI and legacy
downloadable tracking layouts without claiming a general SDUI engine, and inventories distributed
native actions and API endpoints without inventing registries. Technical debt is documented
separately from required compatibility architecture.

## Technical Context

**Language/Version**: Kotlin 1.9.22 and legacy Java; Java/Kotlin target 17

**Primary Dependencies**: AndroidX/AppCompat/Lifecycle, XML ViewBinding/DataBinding, Jetpack Compose
Material 3, Hilt 2.51, coroutines, Retrofit 2.9, OkHttp 4.12, Gson, AppAuth, MapLibre Native,
Coil/SVG, Media3, osmapi-traces

**Storage**: SQLite through `TrackContentProvider`/`DatabaseHelper`; default SharedPreferences;
encrypted token values backed by Android Keystore; app cache and persistent/exported files; inactive
Room scaffolding is not a production store

**Testing**: JUnit 4, Robolectric, Mockito, PowerMock, Espresso dependencies, JaCoCo; local unit tests
exist, while no instrumented test class or automated Release UI/auth smoke suite is confirmed

**Target Platform**: Android API 23–34 baseline; phone-oriented Release packages ARM ABIs; Debug
retains x86/x86_64 emulator support

**Project Type**: Existing production Android mobile application, single `:app` Gradle module

**Performance Goals**: Preserve responsive main-thread behavior; record GPS points throughout active
missions; retain current address lookup timeout/cooldown and map-style fallback behavior. No global
quantitative performance SLA is confirmed.

**Constraints**: Persian/RTL; runtime-selected municipality `BASE_URL`; backend compatibility;
OIDC/password compatibility; encrypted tokens; foreground location lifecycle; minified and
resource-shrunk Release; permanent signing identity; no secret logging; no architectural replacement
within this baseline plan

**Scale/Scope**: One application module; driver and supervisor role families; hybrid modern and
legacy screens across `activity`, `presentation`, and `supervisor`; multiple municipality backends;
repository documentation artifacts only

## Constitution Check

*GATE: Passed before Phase 0 and re-checked after Phase 1.*

| Principle | Gate Result | Evidence in Plan |
|---|---|---|
| Preserve proven functionality | PASS | No application source change or replacement is planned. |
| Maintain backend compatibility | PASS | Runtime municipality routing and existing contracts are recorded as required boundaries. |
| Analyze before architecture change | PASS | Actual modules, packages, UI, state, data, DI, navigation, storage, auth, remote UI, actions, endpoints, and builds were inspected. |
| Kotlin and Compose conventions | PASS | Current use is documented; no forced Java/XML rewrite is proposed. |
| Reuse production mechanisms | PASS | Auth, networking, preferences, and SQLite remain authoritative. |
| Deliver incrementally | PASS | Output is documentation-only and independently reviewable. |
| Separate refactoring from features | PASS | Technical debt is recorded without being included as implementation work. |
| Document migration impact | PASS | No architectural migration is proposed. |
| Existing behavior is specification | PASS | The active repository is treated as the baseline. |
| Keep build and tests green | PASS | No source changes are made; quickstart includes non-mutating validation and standard build gates. |

### Post-Design Re-check

Phase 1 adds only architecture documentation, entity descriptions, current-interface contracts, and
a validation guide. It does not change application source, backend contracts, persistence, signing,
or runtime behavior. All gates remain PASS and no complexity exception is required.

## Current Architecture

### Gradle Modules and Build Configuration

- `settings.gradle` includes only `:app`; the root build declares Android Gradle Plugin 8.4.2,
  Kotlin 1.9.22, Hilt 2.51, JaCoCo 0.8.12, and Safe Args 2.9.1.
- `app` uses namespace/application ID `Ir.co.tfs.farazaman`, minSdk 23, target/compile SDK 34,
  Java/Kotlin 17, multidex, ViewBinding, DataBinding, Compose, and BuildConfig.
- Debug is unminified, adds `-dev`, includes Compose tooling/previews, and supports emulator ABIs.
  Release is R8-minified, resource-shrunk, ARM-only, permanently signed, and emits
  `farazaman.apk` plus `farazaman.json` update metadata.
- There are no product flavors. Municipality/environment selection is runtime state, not a build
  variant. The legacy password client secret comes from local/environment configuration.

### Kotlin/Java Package Boundaries

- `activity`: primary Activities/Fragments, adapters, bottom sheets, and substantial legacy
  orchestration.
- `presentation`: base Activity, login UI, Compose screens/theme, and Hilt ViewModels.
- `supervisor`: supervisor navigation, mission setup/control, daily-plan actions, and local history.
- `domain`: selected auth contracts/use case, validation, tracking/road/update policies.
- `data`: API DTOs/models, repositories, update code, and SQLite/Room persistence areas.
- `service`: GPS foreground service and Retrofit service interfaces.
- `auth`, `di`, `map`, `gpx`, `update`, `receiver`, `security`, `util`: cross-cutting ownership.
- `layout`, `listener`, `osm`, `overlay`, `view`: inherited or legacy tracking/custom-layout areas.

### UI Architecture

- Most UI is Activity/Fragment plus XML with ViewBinding/DataBinding; some legacy Java screens use
  direct view lookup and screen-owned orchestration.
- Compose is active for splash/update and role selection, hosted by Activities and themed through
  `FaraZamanTheme`; previews are Debug-only.
- Supervisor UI uses a host Activity, manually switched home/today/history Fragments, bottom sheets,
  adapters, and helper/controller objects.
- Map screens use MapLibre with a dedicated overlay controller, while their Activities/ViewModels
  still coordinate mission, road, GIS, and location concerns.

### ViewModel and State Management

- State management is intentionally mixed. `LoginViewModel` and `FormDataViewModel` expose LiveData
  and sealed result states. Splash, new mission, map, violation map, and track manager ViewModels
  expose StateFlow backed by MutableStateFlow.
- Compose screens receive explicit UI state and callbacks from their Activity/ViewModel boundary.
- Many legacy screens retain state directly in Activities/Fragments, preferences, Cursors, adapters,
  or helper objects. There is no single application-wide state container.
- ViewModels launch coroutine work through lifecycle-owned scopes; legacy callbacks and direct calls
  coexist where already implemented.

### Repositories and Data Flow

- `AuthRepositoryImpl` implements the domain auth contract and coordinates OIDC/password login and
  refresh with token storage.
- `FormDataRepository` wraps form, mission, fence, upload, and violation-related calls used by
  ViewModels and screens.
- `AppUpdateRepository` owns update checking, session mandatory-update state, download, validation,
  and cleanup around dedicated update services.
- `AddressLookupCoordinator` applies address request timeout/cooldown behavior.
- `LoginRepository` is a narrower/legacy login data path and is not the sole auth owner.
- Several Activities/Fragments and Java helpers bypass repositories and use services,
  ContentResolver, preferences, or direct clients. This is current architecture, not a plan defect.

### API Clients and Endpoint Ownership

- Shared authenticated Retrofit/OkHttp/Gson is provided from `NetworkModule`, reads runtime
  `BASE_URL`, attaches bearer credentials, refreshes under synchronization, retries once after 401,
  and redacts/disables authorization logging.
- `FormDataApiService`, `ApiService`, `RoadService`, `ViolationApiService`, `UserService`,
  `RolesService`, `VehicleZoneWorkService`, `UploadService`, and `AuthService` distribute endpoint
  declarations by feature/history rather than through one endpoint registry.
- `RetrofitProvider` and selected-server URL helpers support runtime base changes and legacy upload
  call sites. Some services are recreated against current preferences.
- Update checks use a separate unauthenticated client and fixed endpoint. Map style/assets also use a
  distinct external boundary.
- The current endpoint inventory is documented in
  [contracts/endpoint-inventory.md](./contracts/endpoint-inventory.md). It is documentation, not a
  new runtime registry.

### DTO and Model Mapping

- Gson maps JSON primarily into `data/model` DTO-shaped classes. Request/response types also live in
  `data/model/req` and `data/model/res`.
- Mapping is selective rather than uniform: auth converts token responses into the existing login
  result shape; mission screens map server organs/contracts into UI selection options; road and GIS
  data are adapted into ViewModel UI models; other flows expose response models directly to UI.
- Java database models (`Track`, `TrackPoint`, `WayPoint`, `Point`) represent ContentProvider rows.
- R8 rules protecting fields, annotations, generic signatures, Retrofit interfaces, coroutine
  continuation metadata, and response types are part of the mapping contract in Release.

### Hilt Dependency Injection

- `MyApplication` is the Hilt application root. `AppModule`, `NetworkModule`, and `AppUpdateModule`
  install application-scoped providers.
- Hilt supplies preferences, Gson, token/user/role/auth managers, authenticated clients/services,
  repositories, login use case, update components, and Hilt ViewModels.
- Activities/Fragments use `@AndroidEntryPoint` where migrated. DI is partial; legacy screens and
  helpers continue to instantiate or retrieve dependencies directly.
- Room providers in `AppModule` are commented out and do not define current persistence.

### Navigation

- Launcher routing is Splash → Login or Role Selection. Role selection routes to Driver or
  supervisor work-area/existing-mission paths.
- Navigation uses explicit Intents, AppAuth redirect intents, manual Fragment transactions, bottom
  sheets, and supervisor helper methods.
- The Safe Args plugin is configured, but no active navigation graph is confirmed. Existing explicit
  navigation remains the required baseline.

### Local Storage

- `TrackContentProvider` over `DatabaseHelper` SQLite v18 owns tracks, track points, and waypoints;
  `DataHelper` provides common operations. Driver/supervisor separation uses the track role column.
- Default SharedPreferences stores runtime server/auth configuration, expiry and non-token user/role
  metadata, mission JSON, selections, and other legacy settings. Token strings are encrypted with
  Android Keystore-backed AES/GCM and old plaintext values migrate on read.
- Supervisor violation history is JSON preference data. GPX/ZIP, photos, map style cache, update
  candidates, and other files use app/persistent file boundaries.
- Room files are inactive scaffolding and MUST NOT be described as the current database.

### Authentication

- Municipality configuration selects default OIDC web authentication or legacy password grant.
- OIDC uses AppAuth and the application redirect; password grant tries selected-server token paths
  and requires the externally configured legacy client secret.
- A token is not sufficient for completed login: user-info and role calls must also succeed and be
  persisted before routing.
- `TokenManager`, `SecureTokenStorage`, `OidcAuthManager`, `AuthInterceptor`, and `AuthStateManager`
  divide expiry, encryption, refresh, request attachment, failure broadcast, and redirect ownership.
- Logout clears session/config/cache state but preserves track database and persistent user files.

### SDUI Engine

- No class, package, schema interpreter, component factory, or runtime explicitly implements a
  general SDUI engine.
- Current server-driven behavior supplies municipality choices/configuration, roles, work areas,
  employers, contracts, daily items, form options, GIS layers, mission details, roads, and fences to
  packaged Android screens.
- A separate inherited custom-layout facility downloads XML-like tracking button layouts from a
  configured repository and renders actions through `UserDefinedLayout` and listener classes. It is
  a legacy, limited remote-layout mechanism, not evidence of a general application SDUI platform.
- These two mechanisms are documented in [contracts/sdui-boundary.md](./contracts/sdui-boundary.md).

### Native Action Registry

- No centralized native-action registry exists. Native operations are dispatched through direct
  click listeners, Intent construction, Activity/Fragment callbacks, supervisor helpers/controllers,
  Compose callbacks, and legacy listener classes.
- The legacy custom layout maps button types to listeners such as tag, text note, still image, voice
  record, page, sensor, and pressure actions. Supervisor action helpers route map, violation,
  tracking, profile, navigation, and mission actions.
- The current action inventory is descriptive in
  [contracts/native-action-inventory.md](./contracts/native-action-inventory.md); it does not create
  a runtime registry.

### Endpoint Registry

- No centralized endpoint registry exists. Retrofit annotations, fixed update/map configuration,
  selected-server URL helpers, and limited legacy URL construction collectively define endpoints.
- The authoritative runtime distinction is central registry vs selected municipality vs identity
  issuer vs update host vs map host. Consolidation is not required by this baseline.

## Technical Debt (Not Required Architecture)

The following are observations, not implementation tasks or authorization to change behavior:

- State and orchestration are split across ViewModels, screens, helpers, callbacks, Cursors, and
  preferences, increasing feature-specific reasoning cost.
- Repository and Hilt adoption are partial; some screens build or call networking/storage directly.
- Endpoint declarations and error mapping are distributed, with duplicate/near-duplicate road and
  upload paths and multiple Retrofit creation paths.
- Some UI-facing state uses `Any?`, raw `Cursor`, or transport models rather than consistently typed
  UI/domain models.
- Legacy custom-layout networking and listener actions coexist with modern authenticated business
  networking and are governed differently.
- Safe Args is configured without a confirmed active navigation graph; inactive Room scaffolding is
  present without production providers.
- Debug logging around base URL/client creation is verbose, though secrets and authorization values
  are not intentionally logged.
- Automated Release UI/auth/first-feature smoke coverage and instrumented test classes are absent.
- Error presentation and retry behavior are feature-owned rather than governed by a single contract.

None of these observations overrides required compatibility architecture: selected-server routing,
dual auth modes, current backend payloads, SQLite track data, preferences, foreground GPS, explicit
navigation, hybrid UI, R8 rules, and signing identity remain baseline requirements.

## Project Structure

### Documentation (this feature)

```text
specs/001-existing-system-baseline/
|-- spec.md
|-- plan.md
|-- research.md
|-- data-model.md
|-- quickstart.md
|-- checklists/
|   `-- requirements.md
`-- contracts/
    |-- current-interface-contracts.md
    |-- endpoint-inventory.md
    |-- native-action-inventory.md
    `-- sdui-boundary.md
```

### Source Code (repository root)

```text
settings.gradle
build.gradle
app/
|-- build.gradle
|-- proguard-rules.pro
`-- src/
    |-- main/
    |   |-- AndroidManifest.xml
    |   |-- java/Ir/co/tfs/farazaman/
    |   |   |-- activity/
    |   |   |-- auth/
    |   |   |-- data/{api,db,model,repository,update}/
    |   |   |-- di/
    |   |   |-- domain/{model,repository,road,tracking,update,usecase,validation}/
    |   |   |-- layout/
    |   |   |-- listener/
    |   |   |-- map/
    |   |   |-- presentation/{base,compose,login,viewmodel}/
    |   |   |-- service/{gps,remote}/
    |   |   |-- supervisor/
    |   |   `-- update/, util/, receiver/, security/, gpx/, osm/, overlay/, view/
    |   `-- res/{layout,values,values-night,drawable,drawable-night,xml,...}/
    |-- debug/java/.../presentation/compose/
    `-- test/java/Ir/co/tfs/farazaman/
```

**Structure Decision**: Preserve and document the existing single-module package topology. This plan
does not introduce modules, layers, registries, navigation frameworks, stores, or migrations.

## Complexity Tracking

No constitution violations or new complexity are introduced. Existing hybrid complexity is captured
under Technical Debt and remains outside the scope of this documentation plan.

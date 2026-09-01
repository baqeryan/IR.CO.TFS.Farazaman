# Feature Specification: Existing-System Baseline

**Feature Branch**: `not-created`

**Created**: 2026-09-01

**Status**: Draft

**Input**: User description: "Document the current Farazaman Android application as an existing-system baseline. Do not implement or refactor anything."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Understand the Production Product (Priority: P1)

As a product owner or maintainer, I need a reliable description of the application purpose and
working user journeys so that observed production behavior remains the baseline for later work.

**Why this priority**: Product behavior cannot be safely evolved until the current user-facing
contract is recorded.

**Independent Test**: Compare the documented startup, authentication, driver, supervisor,
violation, tracking, and update journeys with the mapped repository entry points and confirm that
each journey has an evidence-backed status.

**Acceptance Scenarios**:

1. **Given** the repository at the baseline date, **When** a reviewer reads this specification,
   **Then** the application's purpose, actors, primary journeys, screens, and navigation are stated.
2. **Given** behavior that is inactive, future-facing, or not confirmed, **When** it is documented,
   **Then** it is labeled DEPRECATED, PLANNED, or UNKNOWN rather than CURRENT.

---

### User Story 2 - Understand System Boundaries (Priority: P2)

As an engineer or reviewer, I need the current integration and architecture boundaries recorded so
that later specifications do not accidentally bypass established authentication, API routing,
persistence, dependency injection, background work, or device integrations.

**Why this priority**: These boundaries carry backend compatibility, data integrity, security, and
Release-build risk.

**Independent Test**: Trace one authenticated feature call, one track-recording operation, one
media attachment, and one update check from user entry to their external or persistent boundary.

**Acceptance Scenarios**:

1. **Given** an authenticated municipality feature, **When** its baseline is reviewed, **Then** the
   runtime-selected municipality, token handling, request path, and error boundary are identifiable.
2. **Given** a tracking mission, **When** its baseline is reviewed, **Then** foreground location,
   local track storage, map display, export, and upload boundaries are identifiable.

---

### User Story 3 - Bound Future Discovery (Priority: P3)

As a future specification author, I need known unknowns and inactive scaffolding separated from
working behavior so that they are investigated rather than silently treated as requirements.

**Why this priority**: The codebase contains legacy surfaces and inactive scaffolding whose presence
alone does not establish production use or future intent.

**Independent Test**: Review all UNKNOWN, DEPRECATED, and PLANNED entries and verify that none is
presented as a confirmed production workflow.

**Acceptance Scenarios**:

1. **Given** code that is present but inactive, **When** it appears in the baseline, **Then** its
   status and evidence limitation are explicit.
2. **Given** no repository evidence of a future commitment, **When** the baseline is reviewed,
   **Then** the specification does not invent a planned implementation.

### Edge Cases

- A backend or municipality may expose different login modes, form options, roles, work areas, or
  update metadata; the baseline records the client behavior without claiming all server datasets.
- A stored session may be valid while profile or role retrieval fails; token acquisition alone is
  not a completed application login.
- A selected municipality may change at runtime; feature and upload traffic must not be assumed to
  use the central registry address.
- Network loss may leave local tracks and mission data available while address, map, form-data,
  upload, or update operations fail or use their documented fallback.
- Debug success does not establish minified Release behavior for reflection-based API parsing,
  authentication, maps, or provider authorities.
- Manifest declaration establishes requested capability, but not that every permission is exercised
  by every current journey or granted on every supported Android version.
- Legacy Activities may be manifest-reachable without being part of the primary driver or supervisor
  journey; reachability and current business use are distinct.

## Requirements *(mandatory)*

### Status Vocabulary

- **CURRENT**: Confirmed by repository documentation and active configuration or code as part of the
  present application baseline.
- **DEPRECATED**: Retained legacy behavior or compatibility surface that is not the preferred current
  path; removal is not authorized by this specification.
- **PLANNED**: Explicit future intent found in repository evidence. No PLANNED product capability was
  confirmed for this baseline.
- **UNKNOWN**: Presence, production use, ownership, or expected behavior cannot be confirmed from the
  inspected repository evidence.

### Functional Requirements

- **FR-001**: The baseline MUST treat confirmed existing behavior as the product specification until
  explicitly superseded.
- **FR-002**: The baseline MUST document all requested areas: purpose, flows, modules, screens,
  navigation, authentication, APIs, persistence, dependency injection, configuration, build
  variants, background services, permissions, device/file integrations, errors, updates,
  server-driven UI, and architectural boundaries.
- **FR-003**: Every capability or limitation MUST be associated with CURRENT, DEPRECATED, PLANNED,
  or UNKNOWN status.
- **FR-004**: The baseline MUST describe observed behavior only and MUST NOT prescribe
  implementation, migration, refactoring, or replacement work.
- **FR-005**: Security-sensitive values MUST be described by role and storage boundary without
  exposing token values, credentials, client secrets, or signing passwords.
- **FR-006**: Conflicting repository evidence MUST be recorded as UNKNOWN or date-qualified rather
  than silently reconciled.

### Existing-System Baseline

#### Application Purpose and Actors

- **CURRENT**: FaraZaman is a Persian, RTL Android field-operations client for municipal drivers and
  municipal-contract supervisors. It supports municipality selection, authentication, role
  selection, work-area and mission workflows, location-track recording, maps, road/address lookup,
  daily-plan work, violations with evidence, and track export/upload.
- **CURRENT**: Primary actors are an authenticated driver and an authenticated supervisor. A user may
  receive one or both roles from the selected municipality server.
- **UNKNOWN**: Complete organizational role taxonomy, authorization policy on the backend, and exact
  municipality-by-municipality feature availability are not defined in the repository.

#### Current User Flows

- **CURRENT — Startup**: Splash checks update metadata, enforces a successfully reported mandatory
  update for the process, and otherwise routes a valid session to role selection or an unauthenticated
  user to login. Update-check failure is fail-open.
- **CURRENT — Municipality and login**: Login retrieves municipality instances from a central
  registry; the user selects a city; the app stores that city's base URL, client identity, scope, and
  login mode; it performs the configured OIDC web flow or password flow; then it retrieves user info
  and roles before treating login as complete.
- **CURRENT — Role selection**: A user chooses an available driver or supervisor role. Driver routes
  to the driver workflow. Supervisor routes to work-area selection or an existing supervisor mission
  when local state supports resumption.
- **CURRENT — Driver**: The driver selects/receives a work area, creates or resumes a mission/track,
  records location points through the foreground tracking service, views the route on a map, manages
  waypoints/tracks, and can export or upload track data to the selected municipality.
- **CURRENT — Supervisor**: The supervisor selects a work area, chooses employer and contract data,
  creates a mission, optionally receives an employer fence, enters the supervisor shell, and uses
  home, today, and history areas to control tracking, review daily work, open maps, and register
  violations.
- **CURRENT — Violation**: Map, daily-plan, or supervisor entry points open violation forms. The app
  retrieves server form data, validates required values, accepts image evidence, submits violation
  data, and maintains local supervisor violation history for relevant flows.
- **CURRENT — Track management**: Legacy track manager, logger, detail, waypoint, display, preference,
  and upload surfaces remain packaged and operate over the shared track store.
- **UNKNOWN**: Production usage frequency and business ownership of each legacy track-management
  surface are not established.

#### Modules and Architectural Areas

- **CURRENT**: The build contains one Android application module, `:app`, with namespace and package
  `Ir.co.tfs.farazaman`.
- **CURRENT**: Source is organized into partially layered areas: `domain` for selected contracts and
  policies; `data` for models, repositories, API definitions, and persistence; `presentation` for
  Compose/login/base UI and ViewModels; `activity` for most screens and legacy orchestration;
  `supervisor` for supervisor workflow helpers; `service` for GPS and remote interfaces; plus `auth`,
  `di`, `map`, `gpx`, `update`, `receiver`, and `util` concerns.
- **CURRENT**: Kotlin and inherited Java coexist. The architecture is transitional and is not strict
  Clean Architecture.
- **DEPRECATED**: OSMTracker-derived Java screens, custom layouts, upload, preference, and helper
  surfaces are legacy compatibility areas, though several remain packaged and functional.
- **PLANNED**: None confirmed.

#### Screens and Navigation

- **CURRENT — Entry/auth**: `SplashActivity`, `LoginActivity`, OIDC login bottom sheet, city-selection
  bottom sheet, `RoleSelectionActivity`, and developer-options warning.
- **CURRENT — Driver/track**: `DriverActivity`, `TrackManager`, `TrackLogger`, `TrackDetail`,
  `WaypointList`, `DisplayTrack`, `DisplayTrackMap`, preferences, and upload screens.
- **CURRENT — Supervisor**: work-area selection, `NewSupervisorActivity`, home/today/history
  fragments, new-mission bottom sheet, mission item selectors, profile, today-visit content, and
  daily-plan lists/actions.
- **CURRENT — Violation**: map entry, general/new/commitment form Activities, embedded commitment
  fragment, image preview/detail bottom sheets, and related selectors.
- **CURRENT — Ancillary legacy**: intro, about, button presets, and available custom layouts are
  declared or reachable legacy UI surfaces.
- **CURRENT**: Navigation uses explicit Intents, bottom sheets, manual Fragment transactions, and
  manual supervisor tab switching. No active navigation graph was confirmed.
- **UNKNOWN**: The Safe Args build plugin is present, but no current runtime navigation graph use was
  found.

#### Authentication Flow

- **CURRENT**: Authentication mode is municipality-configured. Web/OIDC is the default mode and uses
  the central identity issuer with an application redirect URI. Password mode uses legacy token
  endpoints on the selected municipality server.
- **CURRENT**: A successful token response is followed by user-info and role requests. Only after
  those succeed is the user routed into the authenticated application.
- **CURRENT**: Access and refresh token strings are encrypted at rest. Token expiry, user profile,
  role maps, and selected-server configuration use application preferences.
- **CURRENT**: Authenticated requests receive bearer credentials through the shared request path.
  Expired credentials are refreshed under serialized ownership; a 401 is retried once; unrecoverable
  authentication failure is broadcast to active base screens for redirect.
- **CURRENT**: Explicit logout clears auth, user/role, mission, selected-server/OIDC, WebView, and
  cache state while preserving the track database and persistent user files.
- **DEPRECATED**: Password grant and its externally supplied legacy client secret are retained for
  municipality compatibility. The `osmtracker` redirect scheme is also retained alongside the
  current application redirect.

#### API Communication and Error Handling

- **CURRENT**: A central registry provides municipality instances. Authenticated business traffic
  normally targets the runtime-selected municipality through shared JSON HTTP services and a bearer
  authentication boundary.
- **CURRENT**: Business endpoints cover account info/roles, work areas, form data, daily items,
  missions/details, employer fences, road/address/GIS data, violations, and track upload.
- **CURRENT**: Some legacy screens construct requests directly. Selected-server URL behavior remains
  the required baseline for those paths.
- **CURRENT**: General network timeouts are centralized; address lookup has a short timeout and a
  cooldown. Feature errors are handled across repositories, ViewModels, Activities, Fragments,
  dialogs, messages, and toasts rather than through one global error model.
- **CURRENT**: Auth failure has a shared broadcast/redirect path. Update discovery, download,
  validation, and install errors are distinguished from one another. Map style loading retains a
  cached or bundled fallback.
- **UNKNOWN**: There is no confirmed exhaustive error taxonomy, retry policy, offline queue, or
  cross-feature error presentation contract.

#### Local Persistence

- **CURRENT**: Track, track-point, and waypoint data use a legacy SQLite database exposed through a
  non-exported content provider. Driver and supervisor tracks share the database and are separated
  by a role field.
- **CURRENT**: Mission payloads, selected municipality and auth configuration, profile/roles,
  violation history, and other workflow metadata use preferences. Token strings are encrypted before
  preference storage.
- **CURRENT**: GPX/ZIP exports, images, downloaded update candidates, map style cache, and other
  user/cache files use application or shared file boundaries appropriate to their flow.
- **DEPRECATED**: Plaintext token preference values are legacy data read only for migration into
  encrypted storage.
- **CURRENT**: Room-related database, DAO, and entity files exist as inactive scaffolding and are not
  the production persistence path.
- **UNKNOWN**: No approved Room migration scope or schedule is documented.

#### Dependency Injection and Architectural Ownership

- **CURRENT**: The application root enables dependency injection. Shared modules provide preferences,
  serialization, token/user/role managers, authenticated networking, business services,
  repositories, and authentication use cases. Injected screens and ViewModels use lifecycle-aware
  component ownership.
- **CURRENT**: Dependency injection is partial. Several legacy screens still own UI, API,
  ContentResolver, preference, and navigation logic directly.
- **UNKNOWN**: No repository evidence defines a deadline or approved scope for completing dependency
  injection across legacy surfaces.

#### Configuration and Build Variants

- **CURRENT**: Minimum Android version is API 23; compile and target API are 34; Java/Kotlin target is
  17. Persian is forced at runtime, RTL is enabled, system night mode is followed, and Persian and
  English resources are packaged.
- **CURRENT**: Municipality/environment selection is runtime configuration. There are no product
  flavors or compiled development/staging/production URL variants.
- **CURRENT**: Debug is non-minified, appends `-dev`, includes Compose tooling/previews, and retains
  emulator native architectures. Release is minified and resource-shrunk, packages ARM phone
  architectures, uses the permanent signing identity, emits a fixed APK filename and matching update
  metadata, and depends on preservation rules for reflected/generic API data.
- **CURRENT**: At the baseline date, the build script declares version code 112 and a date-derived
  version name. This value is build-time state and may change after this baseline.
- **UNKNOWN**: Exact configuration and data exposed by each deployed municipality are external to
  this repository.

#### Background Services and Permissions

- **CURRENT**: A non-exported foreground location service listens to GPS and network providers and
  records points while a driver or supervisor mission is active. No scheduled job framework or
  periodic worker was found.
- **CURRENT**: A media-button receiver and an update-install result receiver are declared. Runtime
  auth and GPS listeners/receivers also participate while their owners are active.
- **CURRENT**: Declared permissions cover camera; fine, coarse, and background location; foreground
  location service; internet and network/Wi-Fi state; notifications; audio recording; package
  installation requests; modern image access; and version-limited legacy external storage.
- **CURRENT**: Camera hardware is optional, so installation does not require a camera device.
- **UNKNOWN**: Current business use of audio recording and Samsung badge-writing permission was not
  confirmed from the mapped primary workflows.

#### Camera, Location, and File Integrations

- **CURRENT**: Violation workflows can capture or select multiple images, preview/remove them, and
  include them in submission. A non-exported file provider grants controlled URI access.
- **CURRENT**: Location supports foreground route recording, current-location and route display,
  waypoint storage, work-area/employer-fence overlays, and address/road lookup.
- **CURRENT**: Track data can be exported as GPX, compressed, and uploaded. Update APKs download to an
  application cache directory before package validation and installation.
- **CURRENT**: Modern media permissions coexist with version-limited legacy storage permissions and
  legacy external-storage compatibility for supported older devices.
- **UNKNOWN**: Repository evidence does not establish a single retention policy for captured
  violation media, exported tracks, and other persistent user files.

#### Update Mechanism

- **CURRENT**: Startup uses a dedicated unauthenticated client and fixed HTTPS metadata endpoint,
  separate from municipality API authentication and routing.
- **CURRENT**: If metadata reports a newer version, the user receives update information. A mandatory
  result remains enforced for the process; check failure does not block startup.
- **CURRENT**: Downloaded APKs are streamed to a partial cache file, parsed, and checked for archive
  validity, application package, higher version, allowed HTTPS host, and compatible signing
  certificate before installation through the platform installer.
- **CURRENT**: Android versions requiring unknown-source authorization route the user through system
  settings. Broad storage permission is not required for update installation.

#### Existing Server-Driven UI Functionality

- **CURRENT**: Municipality registry data drives available city choices and the selected city's
  authentication configuration.
- **CURRENT**: Account role responses drive role options and subsequent driver/supervisor routing.
- **CURRENT**: Work-area, employer, contract, daily-item, form-data, GIS-layer, mission-detail, road,
  and fence responses populate choices, forms, map overlays, and workflow context. The client renders
  these values within packaged screens; this is server-driven content and workflow data, not a
  general remote screen-rendering system.
- **DEPRECATED**: The inherited custom-layout facility can retrieve a remote list of layout files,
  download a selected layout, and drive configurable tracking buttons. It is a legacy server/repository
  supplied UI mechanism retained in the packaged application.
- **UNKNOWN**: Active production use, supported remote repository, governance, and municipality
  dependence of the custom-layout facility were not confirmed.

#### Known Architectural Boundaries

- **CURRENT**: The runtime-selected municipality is authoritative for business APIs and uploads; the
  central registry and update service are distinct fixed external boundaries.
- **CURRENT**: Authentication/token ownership, shared authenticated networking, SQLite track storage,
  preference-backed mission/user state, foreground GPS ownership, and permanent Release signing are
  established compatibility boundaries.
- **CURRENT**: XML/ViewBinding/DataBinding and Compose coexist. Activities and manual fragments remain
  the navigation boundary. Layered repositories/ViewModels coexist with legacy screen-owned logic.
- **CURRENT**: Release minification and resource shrinking are part of production behavior; generic
  API metadata, serialized fields, auth classes, stable provider authorities, and map native assets
  are Release compatibility boundaries.
- **CURRENT**: Persian locale, RTL layout, Android lifecycle ownership, and off-main-thread network,
  database, and file work are behavioral constraints.
- **UNKNOWN**: No uniform application-wide repository, error mapper, navigation graph, offline sync
  engine, or scheduled background-work boundary was confirmed.

### Key Entities *(include if feature involves data)*

- **Municipality Instance**: Selected server identity, base address, login mode, client identity, and
  scope used to configure the session.
- **User and Role Set**: Authenticated profile and server-returned driver/supervisor permissions.
- **Work Area / Zone**: Municipality-defined operational area selected before a mission.
- **Mission**: Driver or supervisor work session associated with role, area, server data, and a local
  track.
- **Track / Track Point / Waypoint**: Locally persisted route container, recorded locations, and
  annotated points; tracks carry driver or supervisor role identity.
- **Employer and Contract**: Server-provided supervisor mission selections.
- **Daily Cleaning Item**: Server-derived scheduled work item used by supervisor today/visit flows.
- **Violation**: Form values, location/context, selected classifications, notes, and image evidence
  submitted during field supervision.
- **Update Metadata and Candidate**: Remote version policy and the locally downloaded package pending
  validation and installation.
- **Custom Layout**: Legacy remotely listed/downloaded tracking-button layout and its local selection.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: All 18 requested baseline topics are present and explicitly status-classified.
- **SC-002**: A reviewer can trace 100% of the six primary journeys documented here—startup/login,
  role selection, driver mission, supervisor mission, violation submission, and update—from entry to
  completion or error boundary.
- **SC-003**: Every inactive, legacy, future-facing, or unverified item mentioned in the baseline is
  labeled DEPRECATED, PLANNED, or UNKNOWN; none is silently represented as CURRENT.
- **SC-004**: The specification contains zero proposed implementation, refactoring, migration, or
  replacement changes.
- **SC-005**: A repository reviewer can verify each CURRENT architectural boundary against project
  context, feature map, active build configuration, manifest declarations, or the relevant mapped
  source without requiring undocumented assumptions.
- **SC-006**: The baseline exposes no secret, token, password, authorization-header, signing-password,
  or private-key value.

## Assumptions

- This is a documentation baseline, not a request to add a user-facing feature; its user stories
  describe how stakeholders validate and use the baseline.
- Repository state and project documentation on 2026-09-01 are the evidence boundary. Deployed
  server configuration and live production telemetry were not inspected.
- CURRENT means confirmed in repository evidence, not proof that every municipality or user invokes
  the capability regularly.
- Manifest-declared legacy Activities are considered packaged surfaces; primary-flow membership is
  claimed only where navigation or project documentation confirms it.
- Current implementation names are included solely as evidence and boundary identifiers. They do not
  prescribe future implementation.
- `docs/PROJECT_CONTEXT.md`, `docs/CODEMAP.md`, `docs/RELEASE_NOTES.md`, the application manifest,
  build configuration, constitution, and targeted mapped source are the principal evidence sources.

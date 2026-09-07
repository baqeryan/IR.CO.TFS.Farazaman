# Phase 0 Research: Contractor Violation Correction

## Existing role source and routing

**Decision**: Extend `UserRoles.kt`, `RolesManager.kt`, `LoginActivity.kt`, `RoleSelectionActivity.kt`, and `RoleSelectionScreen.kt`. Preserve `roles` and model the confirmed `actorRoles` dictionary; authorize Contractor from the exact `Contractor` actor-role key (display `پیمانکار`).

**Rationale**: These files already determine role availability and explicit-Intent navigation. Reusing them preserves login, token, role preference, single-role routing, multi-role selection, and Driver/Supervisor destinations.

**Alternatives considered**: A Contractor-only login or separate role repository was rejected because it would duplicate the existing authenticated role source and selection flow. Inferring Contractor access from ContractId/OrganId was rejected because the server is authoritative.

**Confirmed contract finding**: Android previously modeled only `roles`. The backend response contains both `roles` and `actorRoles`; `Contractor` is returned as an `actorRoles` key and `پیمانکار` is its display value. Existing Driver/Supervisor role behavior remains on `roles`.

## Backend endpoint and DTO integration

**Decision**: Add Contractor operations to `ViolationApiService.kt`, typed DTOs to `data/model`, and an injected `ContractorViolationRepository`. Treat `Response.isSuccessful` as correction success, including 200 and 204, without requiring a response body.

**Rationale**: `NetworkModule.kt` already provides the authenticated Retrofit/OkHttp stack with token refresh and runtime municipality `BASE_URL`; `ViolationApiService.kt` already owns violation submission. A repository isolates wire mapping and response/error policy from Activities.

**Alternatives considered**: A new Retrofit instance/client was rejected because it would bypass selected-server and auth behavior. Reusing `FormDataViewModel.submitViolation()` unchanged was rejected because it requires a non-null success body and includes Supervisor-specific payload and coordinate fallback behavior.

**Repository finding**: The actionable-list endpoint, correction endpoint, exact request/multipart names, response envelope, and identifier types are not present in Android. Implementation must transcribe the existing backend contract or captured requests; the plan does not invent these values.

## Operational UI technology

**Decision**: Use XML/ViewBinding Activities and RecyclerView for the Contractor list/form/map, while extending the existing Compose role-selection screen.

**Rationale**: Role selection is already Compose. Comparable Supervisor operational screens and violation forms are XML, Activity/Fragment, RecyclerView, and ViewBinding. This is the smallest change that matches current UI conventions.

**Alternatives considered**: Building the whole Contractor workflow in Compose was rejected because it would introduce a parallel operational-screen pattern and new navigation/state integration solely for this feature.

## List/card and transient submitted state

**Decision**: Model list UI as Loading, Content, Empty, and Error in `ContractorViolationsViewModel`; adapt the two-action card pattern in `DailyPlanItemsAdapter.kt`/`item_daily_plan_task.xml`. Keep successful IDs in an in-memory set owned by the current ViewModel.

**Rationale**: The existing card already expresses title/subtitle/status and two actions. In-memory tags meet the specification: no automatic full refresh after success, but explicit refresh or re-entry restores API authority.

**Alternatives considered**: Persisting the correction tag in SQLite/preferences was rejected because it would conflict with the server-authoritative status. Mutating the DTO in the Adapter was rejected because it mixes transient UI state with wire data. Automatic refresh after success was rejected by the required behavior.

## Camera capture and evidence management

**Decision**: Extract a small reusable Activity Result coordinator around the existing `TakePicture` + `MediaStore` pattern; reuse `SelectedImagesAdapter` and existing image layouts. Complete preview and replace through shared callbacks and a focused preview dialog/bottom sheet. Register no gallery/file picker.

**Rationale**: Supervisor forms already use camera-only capture and thumbnail removal. Centralizing launcher/pending-URI/replace-index behavior prevents another camera implementation. The current adapter exposes click/remove callbacks, but existing call sites do not implement complete preview/replace behavior.

**Alternatives considered**: Copying the camera code into the Contractor Activity was rejected by the no-duplicate constraint. A new camera library was rejected because current Android APIs are sufficient. Gallery/file selection was rejected by requirement.

## Location permission, services, fresh fix, and fallback

**Decision**: Add a lifecycle-cancellable `LocationAcquisitionCoordinator` on the existing `LocationManager` approach. At submit: check/request fine or coarse permission, check at least one provider, try one fresh fix with a bounded timeout, then select the best positive-accuracy last-known GPS/network fix exposed by `GpsAccuracyHelper`.

**Rationale**: `SubmitViolationActivity.kt` already uses `LocationManager`, GPS/network providers, Activity Result permissions, and lifecycle cleanup. `TrackLogger.checkGPSProvider()` already demonstrates the system settings flow. `GpsAccuracyHelper` already embodies the current last-known provider/accuracy interpretation but needs to return the selected `Location`.

**Alternatives considered**: Continuous updates in the form were rejected as unnecessary and lifecycle-heavy. Fused Location Provider was rejected because it would add a second location implementation/dependency. Hardcoded, zero, manually entered, or test coordinates were rejected. A new maximum-age rule was not selected because no existing confirmed validity threshold exists in the project or feature contract.

## Read-only location display

**Decision**: Add a dedicated read-only Activity that uses `BaranMapStyle` and `MapLibreOverlayController` to center/draw the violation point.

**Rationale**: This reuses the established MapLibre stack while avoiding Supervisor-specific behavior inside `SubmitViolationActivity` and `DisplayTrackMap`.

**Alternatives considered**: Launching `SubmitViolationActivity` was rejected because it is coupled to current track, live location, and violation registration. A WebView or second map library was rejected.

## Form state, duplicate submission, and errors

**Decision**: Keep description, image URI list, validation messages, and submission state in a Contractor ViewModel, with URI strings optionally backed by `SavedStateHandle`. Use an `isSubmitting`/active-job guard, disable submit while active, and preserve state after any failure. Render API errors through current Toast/Snackbar/error parsing conventions.

**Rationale**: `FormDataViewModel` already demonstrates submission state and an active-job guard. A dedicated Contractor ViewModel avoids importing Supervisor-specific models and preserves fields during failure/configuration changes.

**Alternatives considered**: Putting request, permission, file, and state logic entirely in the Activity was rejected as inconsistent with current ViewModel/repository patterns. Durable offline drafts were rejected as out of scope.

## DI, build, and R8

**Decision**: Constructor-inject the repository/service using Hilt and the existing `NetworkModule` service provider. Rely on current Gson/Retrofit keep rules, adding a narrow rule only if Release validation proves it necessary.

**Rationale**: The application already uses Hilt, and `NetworkModule` provides authenticated `ViolationApiService`. Existing `proguard-rules.pro` keeps data model fields and Retrofit service signatures.

**Alternatives considered**: Service locators/manual Retrofit creation were rejected. Disabling minification was rejected because Release is intentionally minified/resource-shrunk.

## Testing strategy

**Decision**: Unit-test role interpretation/routing policy, typed repository response policy (including 200/204), correction validation, duplicate-submit reducer behavior, local-tag/reset behavior, and fresh/fallback selection. Then run Debug build/lint and signed Release/R8 smoke against an authenticated Contractor account.

**Rationale**: Most risk is deterministic policy and Release serialization. Pure tests provide fast coverage; permission/camera/settings/install-style Android interactions require device/emulator validation.

**Alternatives considered**: Manual-only testing was rejected due regression risk. Full architecture/UI test expansion was rejected as unrelated scope.

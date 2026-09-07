# Implementation Plan: Contractor Violation Correction

**Branch**: `002-contractor-violation-correction` | **Date**: 2026-09-01 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/002-contractor-violation-correction/spec.md`

## Summary

Extend the existing authenticated role flow with a Contractor role. Contractor users receive the server-authorized actionable-violation list, inspect a violation on the existing MapLibre stack, and submit a description, camera-only evidence, and a fresh-or-last-known device location through the existing authenticated Retrofit client. The operational UI follows the current XML/ViewBinding, RecyclerView, ViewModel, StateFlow, repository, Persian/RTL, loading, and error conventions. A successful correction closes the form and marks only the matching card in the current in-memory list; explicit refresh or screen re-entry reloads authoritative server data.

The Android repository does not contain the Contractor role token, actionable-list endpoint, correction endpoint, or their exact wire DTOs. Implementation must obtain those values from the existing backend contract or a captured successful response before adding Retrofit annotations. This plan deliberately does not invent server paths, field names, or client-side authorization rules.

## Technical Context

**Language/Version**: Kotlin and legacy Java; Java/Kotlin 17  
**Primary Dependencies**: AndroidX Activity/Fragment/Lifecycle, Material Components and Material 3, Hilt, Retrofit 2.9, OkHttp 4.12, Gson, Kotlin coroutines 1.7.3, MapLibre Native Android 11.8  
**Storage**: No new durable storage. Current-list submitted tags live in ViewModel state; description and camera URI strings may use `SavedStateHandle` for configuration recovery. Existing SQLite/ContentProvider and preferences remain unchanged.  
**Testing**: JUnit 4, Mockito, AndroidX Test Core; Gradle unit tests plus targeted device/emulator validation  
**Target Platform**: Android, minSdk 23, target/compile SDK 34  
**Project Type**: Single-module Android application (`:app`)  
**Performance Goals**: One active list request and one active submission at a time; camera and location resources lifecycle-owned; no network or file I/O on the main thread; no full-list reload after correction success  
**Constraints**: Persian/RTL; runtime municipality `BASE_URL`; existing token/refresh client; camera only; location required; API authoritative; no Driver changes, backend redesign, notification/offline work, or unrelated refactor  
**Scale/Scope**: One role-selection extension, three Contractor Activities (list, correction, read-only map), two ViewModels, one repository, typed DTOs, small reusable camera/location helpers, layouts/resources, and targeted tests

## Constitution Check

### Pre-design gate

| Principle | Result | Evidence / plan response |
|---|---|---|
| Preserve proven functionality | PASS | Contractor is additive. Driver routing is untouched; Supervisor edits are limited to extracting truly shared camera behavior without changing its observable behavior. |
| Maintain backend compatibility | PASS | Existing contract is authoritative. Exact role value, endpoints, and DTO shapes must be copied from the backend contract; no guessed fields or local ContractId/OrganId filtering. |
| Analyze before architecture changes | PASS | Existing auth, roles, UI, camera, location, API, map, loading, and error paths are inventoried below and in `research.md`. No new architecture or dependency is introduced. |
| Kotlin/Compose conventions | PASS | New operational UI uses the project's existing XML pattern; the modified Compose role screen keeps unidirectional state, theme tokens, previews, Persian strings, and RTL behavior. |
| Reuse production mechanisms | PASS | Uses `NetworkModule`, `ViolationApiService`, authenticated Retrofit/OkHttp, runtime `BASE_URL`, `RolesManager`, `SelectedImagesAdapter`, MapLibre helpers, and existing error/loading conventions. |
| Deliver incrementally | PASS | Delivery phases separate contract integration, shared behavior-preserving extraction, role/list, form/location, and validation. |
| Separate refactoring from features | PASS | Shared camera/location extraction is isolated and justified by the explicit no-duplicate-implementation requirement. No unrelated cleanup. |
| Document migration impact | PASS | No data, module, networking-stack, or navigation-framework migration is proposed. Rollback is removal of additive Contractor routes/resources and restoration of direct camera calls if needed. |
| Existing behavior as specification | PASS | Current explicit-Intent navigation, Activity Result camera behavior, permission behavior, server-selected URL, and release R8 behavior are preserved. |
| Keep build/tests green | PASS | Targeted unit, Debug, lint, Release/R8, and authenticated Release smoke validation are defined. |

No constitution exception or architecture migration is required.

### Post-design gate

PASS. `research.md`, `data-model.md`, `contracts/contractor-violation-contract.md`, and `quickstart.md` keep the feature additive, preserve the existing network/authentication source of truth, introduce no persistence migration or dependency, and explicitly block implementation from guessing the missing backend wire contract.

## Current Implementation Inventory

### Authentication, roles, selection, and navigation

- `app/src/main/java/Ir/co/tfs/farazaman/presentation/login/LoginActivity.kt`: retrieves roles after login through `RolesService`, saves them through `RolesManager`, and routes single-role users or opens role selection.
- `app/src/main/java/Ir/co/tfs/farazaman/service/remote/RolesService.kt`: `GET api/AccountUser/Roles`.
- `app/src/main/java/Ir/co/tfs/farazaman/data/model/UserRoles.kt`: `UserRolesResponse` preserves `roles`, adds confirmed `actorRoles`, and extends `UserAccess` with Contractor access.
- `app/src/main/java/Ir/co/tfs/farazaman/util/RolesManager.kt`: preference-backed role interpretation for existing application roles plus the exact `Contractor` key in `actorRoles`.
- `app/src/main/java/Ir/co/tfs/farazaman/activity/RoleSelectionActivity.kt`: explicit-Intent role navigation and current Driver/Supervisor selection.
- `app/src/main/java/Ir/co/tfs/farazaman/presentation/compose/RoleSelectionScreen.kt`: `RoleOption`, `RoleSelectionUiState`, and themed Compose cards.
- `app/src/debug/java/Ir/co/tfs/farazaman/presentation/compose/RoleSelectionScreenPreviews.kt`: role screen previews.
- `app/src/main/java/Ir/co/tfs/farazaman/presentation/base/BaseActivity.kt`: authentication-state handling used by authenticated Activities.
- `app/src/main/java/Ir/co/tfs/farazaman/activity/SplashActivity.kt`: startup/session route into the existing selection flow.

### Violation/list UI and submission

- `app/src/main/java/Ir/co/tfs/farazaman/activity/DailyPlanItemsAdapter.kt` and `app/src/main/res/layout/item_daily_plan_task.xml`: closest existing card pattern with two equal actions and status presentation.
- `app/src/main/java/Ir/co/tfs/farazaman/activity/TodayVisitTabFragment.kt` and `app/src/main/res/layout/fragment_today_visit_tab.xml`: RecyclerView plus empty-state convention.
- `app/src/main/java/Ir/co/tfs/farazaman/activity/SupervisorHistoryFragment.kt`: list/status/empty presentation.
- `app/src/main/java/Ir/co/tfs/farazaman/activity/SubmitViolationFormActivity.kt`, `NewSubmitViolationFormActivity.kt`, and `CommitmentsViolationFormFragment.kt`: current Supervisor camera-only evidence collection and form behavior.
- `app/src/main/java/Ir/co/tfs/farazaman/activity/SelectedImagesAdapter.kt`, `app/src/main/res/layout/item_selected_image.xml`, and `app/src/main/res/layout/bottom_sheet_violation_all_images.xml`: image thumbnails, remove action, and all-images sheet. Existing image-click callbacks do not provide a complete preview/replace workflow, so Contractor preview/replace must be completed through shared callbacks rather than claimed as already implemented.
- `app/src/main/java/Ir/co/tfs/farazaman/presentation/viewmodel/FormDataViewModel.kt`: current submit-state and duplicate-job guard pattern. Its body-required success condition and hardcoded zero-coordinate fallback are not suitable for Contractor.
- `app/src/main/java/Ir/co/tfs/farazaman/service/remote/ViolationApiService.kt`: existing authenticated multipart violation API interface.
- `app/src/main/java/Ir/co/tfs/farazaman/data/model/ViolationModel.kt`: existing Supervisor request/success/error DTOs.

### Camera, location, map, loading, and errors

- Camera: `ActivityResultContracts.TakePicture` and `MediaStore` URIs in the existing violation forms; `CAMERA` and `FileProvider` are already declared in `app/src/main/AndroidManifest.xml`.
- Location: `app/src/main/java/Ir/co/tfs/farazaman/activity/SubmitViolationActivity.kt` owns continuous `LocationManager` GPS/network updates and releases them in lifecycle callbacks.
- Location-service prompt: `TrackLogger.checkGPSProvider()` opens `Settings.ACTION_LOCATION_SOURCE_SETTINGS`.
- Last known data: `app/src/main/java/Ir/co/tfs/farazaman/util/GpsAccuracyHelper.kt` examines GPS/network last-known fixes and positive accuracy, but currently returns accuracy rather than a reusable `Location`.
- No reusable one-shot fresh-location implementation was found. A shared coordinator is therefore an extension of the current `LocationManager` approach, not a duplicate screen implementation.
- Map: `app/src/main/java/Ir/co/tfs/farazaman/map/BaranMapStyle.kt` and `MapLibreOverlayController.kt`; `DisplayTrackMap.kt` is the primary host. Contractor location viewing reuses this stack without repurposing the Supervisor submission Activity.
- Loading/errors: `app/src/main/java/Ir/co/tfs/farazaman/util/LoadingDialog.kt`, `ErrorHandler.kt`, Snackbar/Toast conventions, and ViewModel state classes such as `FormDataViewModel.SubmissionState`.

## Implementation Design

### 1. Confirm and encode the existing backend contract

The role contract is confirmed: `GET api/AccountUser/Roles` returns both `roles` and `actorRoles`, and Contractor is the `Contractor` key (`پیمانکار` display value) in `actorRoles`. Before service code, obtain the actionable-list and correction API definitions/samples. Record relative endpoint paths, HTTP verbs, query/path/body/multipart names, identifier types, response nesting, error envelope, image part names, and whether coordinates are scalar or nested. Then:

- add typed wire models in `app/src/main/java/Ir/co/tfs/farazaman/data/model/ContractorViolationModels.kt`;
- extend `app/src/main/java/Ir/co/tfs/farazaman/service/remote/ViolationApiService.kt` rather than adding Retrofit/OkHttp;
- add `app/src/main/java/Ir/co/tfs/farazaman/data/repository/ContractorViolationRepository.kt`, injected with the existing service;
- classify any `Response.isSuccessful` correction response, including HTTP 200 and 204, as success without requiring a body;
- send only the identifiers required by the existing contract and never reproduce server ContractId/OrganId authorization or filtering.

### 2. Extend role interpretation and routing

- Add `hasContractorAccess` and contractor-aware role-count/only-role helpers to `UserAccess` in `UserRoles.kt` while preserving existing Driver/Supervisor helper behavior.
- Read Contractor authorization from the exact `Contractor` key in `actorRoles`; preserve existing key/value interpretation for the legacy `roles` field and do not authorize from the Persian display value. The explicit exception is `superAdministrator`, which can select every app-supported role.
- Update `LoginActivity.decideNextActivity()` so Contractor-only users go directly to the Contractor list, while multi-role users retain the selection flow. Do not alter Driver or Supervisor destinations.
- Add `CONTRACTOR` to `SelectedRole`/`RoleOption`, a `showContractor` field to `RoleSelectionUiState`, and a themed Persian/RTL Contractor card in `RoleSelectionActivity.kt` and `RoleSelectionScreen.kt`.
- Update the existing debug previews and localized strings. Add Contractor Activities to `AndroidManifest.xml`.

### 3. Add the actionable-violations list

Add:

- `activity/ContractorViolationsActivity.kt`, extending `BaseActivity` and using ViewBinding;
- `presentation/viewmodel/ContractorViolationsViewModel.kt`, using `StateFlow<ContractorViolationsUiState>`;
- `activity/ContractorViolationAdapter.kt`;
- `res/layout/activity_contractor_violations.xml` and `res/layout/item_contractor_violation.xml`.

The card follows `item_daily_plan_task.xml`: chapter, violation title, optional local “اصلاح ثبت شد” status, and equal “نمایش روی نقشه” / “ثبت اصلاح” actions. The Activity renders Loading, Content, Empty, and Error with retry/manual refresh. The ViewModel accepts only the repository result, performs no local contract/organ filtering, discards stale request results, and keeps `submittedViolationIds` in memory. Initial entry loads; explicit refresh clears local tags and reloads; successful correction marks one item without fetching; a newly created screen ViewModel reloads API truth.

### 4. Add read-only violation-location navigation

Add `activity/ContractorViolationLocationActivity.kt` and a small MapLibre layout. Pass the selected item identity, title, and contract-provided coordinates through explicit Intent extras. Load `BaranMapStyle`, draw/center the point with `MapLibreOverlayController`, and keep this Activity read-only. Invalid/missing coordinates produce the standard unavailable message while leaving the list usable.

### 5. Reuse and complete camera image management

Introduce a small Activity Result based coordinator such as `activity/ViolationImageCaptureCoordinator.kt` for permission request, pending `MediaStore` URI, `TakePicture`, append, and replace-index completion. It must expose callbacks and own no business/API logic.

Use `SelectedImagesAdapter`, `item_selected_image.xml`, and the existing all-images bottom sheet for thumbnails/removal. Add a focused preview dialog/bottom sheet and use the same coordinator with a replace index. Gallery/file contracts are not registered. If existing Supervisor forms are migrated to the coordinator to satisfy shared reuse, perform that as a separate behavior-preserving change and retain their current validation and output.

### 6. Add correction form, validation, and state

Add:

- `activity/ContractorCorrectionActivity.kt`, extending `BaseActivity`;
- `presentation/viewmodel/ContractorCorrectionViewModel.kt`;
- `domain/validation/ContractorCorrectionValidator.kt`;
- `res/layout/activity_contractor_correction.xml` and any focused image-preview layout.

The immutable violation identifier is supplied by navigation. The form owns description and camera URI state, provides add/preview/remove/replace, and requires a nonblank description and at least one surviving camera image. `SavedStateHandle` may retain description and URI strings across configuration changes, but no durable/offline draft is created. Submission state disables the button and guards against a second active job. Failure keeps fields/images and emits the existing error presentation. Success emits one result containing the violation identifier; the Activity shows success, calls `setResult(RESULT_OK, ...)`, and finishes.

### 7. Centralize fresh-location then fallback policy

Add `util/LocationAcquisitionCoordinator.kt` over `LocationManager` and a pure selection policy suitable for unit testing. Extend `GpsAccuracyHelper.kt` with a reusable best-last-known `Location` result while retaining its current GPS/network and positive-accuracy interpretation.

At submit time the correction Activity must:

1. recheck fine/coarse permission and request through Activity Result APIs if absent;
2. recheck that at least one usable location provider is enabled and offer `Settings.ACTION_LOCATION_SOURCE_SETTINGS` if disabled;
3. request one fresh location with lifecycle cancellation and a bounded timeout;
4. only if fresh acquisition fails, choose the best valid existing last-known GPS/network location;
5. block submission when both are unavailable.

No manual, hardcoded, Los Angeles, zero, or fabricated coordinate fallback is allowed. The current code has no confirmed maximum-age rule for last-known fixes, so this feature preserves the existing provider/positive-accuracy validity rule rather than inventing one.

### 8. Integrate errors, resources, and release behavior

- Use StateFlow UI states for list/form loading and errors; use current inline/Toast/Snackbar patterns and `ApiErrorResponse` parsing where compatible.
- Add Persian/default strings and RTL-safe layouts using project typography/theme colors.
- Reuse `NetworkModule`'s runtime-`BASE_URL` authenticated Retrofit. Add DI bindings only if repository construction cannot use `@Inject` directly.
- Keep multipart file conversion and network/file work off Main; close response/file resources and clean removed/cancelled temporary image rows when safe.
- Confirm R8 retains the new Gson DTO fields and Retrofit signatures through existing `app/proguard-rules.pro`; add narrow rules only if Release validation proves necessary.

## Delivery Phases

1. **Contract fixture and pure models**: capture/confirm backend contract; add typed DTOs, repository response policy, validation and reducer tests. No UI routing yet.
2. **Shared behavior-preserving utilities**: extract/reuse camera capture and location acquisition with tests; verify existing Supervisor capture flow is unchanged if touched.
3. **Role and list slice**: Contractor role interpretation/routing, list API/repository/ViewModel, cards, empty/error/refresh, and read-only map.
4. **Correction slice**: form, camera management, location gate/acquisition, multipart submission, 200/204 success, result-to-local-tag flow.
5. **Compatibility validation**: Debug tests/build/lint, role regression smoke, Release/R8 build and authenticated Contractor API smoke.

## Project Structure

### Documentation (this feature)

```text
specs/002-contractor-violation-correction/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── contractor-violation-contract.md
└── tasks.md                         # Created later by $speckit-tasks
```

### Source Code (planned changes)

```text
app/src/main/java/Ir/co/tfs/farazaman/
├── activity/
│   ├── ContractorViolationsActivity.kt                # new
│   ├── ContractorViolationAdapter.kt                  # new
│   ├── ContractorCorrectionActivity.kt                # new
│   ├── ContractorViolationLocationActivity.kt         # new
│   ├── ViolationImageCaptureCoordinator.kt            # new shared camera coordinator
│   ├── RoleSelectionActivity.kt                       # extend
│   ├── SubmitViolationFormActivity.kt                 # only if shared extraction requires it
│   └── CommitmentsViolationFormFragment.kt            # only if shared extraction requires it
├── data/
│   ├── model/
│   │   ├── UserRoles.kt                               # extend
│   │   └── ContractorViolationModels.kt               # new, exact backend DTOs
│   └── repository/ContractorViolationRepository.kt    # new
├── domain/validation/ContractorCorrectionValidator.kt # new
├── presentation/
│   ├── compose/RoleSelectionScreen.kt                 # extend
│   ├── login/LoginActivity.kt                         # extend routing
│   └── viewmodel/
│       ├── ContractorViolationsViewModel.kt           # new
│       └── ContractorCorrectionViewModel.kt           # new
├── service/remote/ViolationApiService.kt              # extend existing interface
└── util/
    ├── RolesManager.kt                                # extend
    ├── GpsAccuracyHelper.kt                           # expose last-known Location
    └── LocationAcquisitionCoordinator.kt              # new shared one-shot/fallback coordinator

app/src/main/res/
├── layout/
│   ├── activity_contractor_violations.xml             # new
│   ├── item_contractor_violation.xml                  # new
│   ├── activity_contractor_correction.xml             # new
│   └── activity_contractor_violation_location.xml     # new
└── values*/strings.xml                                # Contractor labels/messages

app/src/debug/java/Ir/co/tfs/farazaman/presentation/compose/
└── RoleSelectionScreenPreviews.kt                     # extend

app/src/test/java/Ir/co/tfs/farazaman/
├── util/RolesManagerContractorTest.kt                 # new
├── domain/validation/ContractorCorrectionValidatorTest.kt
├── domain/location/LocationFixPolicyTest.kt
├── data/repository/ContractorViolationRepositoryTest.kt
└── presentation/viewmodel/ContractorViolationsViewModelTest.kt

app/src/main/AndroidManifest.xml                        # declare Contractor Activities
app/proguard-rules.pro                                 # change only if Release proves a narrow keep rule is needed
```

**Structure Decision**: Keep the single `:app` module and its partially layered conventions. New business/network state lives in typed models, an injected repository, and ViewModels; Activities render state and own Android permission/camera/settings contracts. This avoids a parallel architecture while preventing all logic from accumulating in an Activity.

## Migration and Rollback Impact

- **Data migration**: None. No Room, SQLite, ContentProvider, or durable preference schema changes.
- **API compatibility**: Additive use of an existing backend contract through the current authenticated selected-server client. Android filtering remains absent.
- **Navigation compatibility**: Additive explicit Intent destinations. Existing Driver/Supervisor destinations and mission flows remain unchanged.
- **Coexistence**: Contractor is invisible to users whose returned roles do not include the confirmed Contractor token.
- **Operational risks**: wrong role token or wire DTO, Release-only Gson/R8 behavior, lifecycle loss of temporary image URIs, and stale location. Mitigations are contract fixtures, typed DTO tests, Release smoke tests, SavedStateHandle URI retention, and explicit fresh-first policy.
- **Rollback**: Remove Contractor role exposure/routes and additive Activities/service methods. No persisted Contractor state or backend migration requires reversal.

## Complexity Tracking

No constitution violations require justification.

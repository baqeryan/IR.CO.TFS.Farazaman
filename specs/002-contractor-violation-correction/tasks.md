---

description: "Dependency-ordered implementation tasks for Contractor Violation Correction"
---

# Tasks: Contractor Violation Correction

**Input**: Design documents from `specs/002-contractor-violation-correction/`

**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/contractor-violation-contract.md`, `quickstart.md`

**Tests**: Targeted tests are included because the feature specification defines measurable routing, filtering, submission, evidence, location, retry, and success/failure outcomes, and the project constitution requires tests for changed logic and contracts.

**Organization**: Tasks are grouped by user story. All Android changes remain in the existing `:app` module and reuse the current authenticated Retrofit, Hilt, XML/ViewBinding, Compose role selection, MapLibre, camera, location, and error mechanisms.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel after its declared prerequisites because it changes different files.
- **[Story]**: Maps the task to a user story in `spec.md`.
- Every task names the exact file or directory it changes or validates.

## Phase 1: Setup and Authoritative Contract Capture

**Purpose**: Remove the only external integration ambiguity without guessing backend behavior.

- [ ] T001 [BLOCKED_BY_API_CONTRACT] Obtain authoritative actionable-list and correction API definitions, then replace all unknown contract details with exact relative paths, verbs, field names, identifier types, response envelopes, coordinate order, image part names, and error shape in `specs/002-contractor-violation-correction/contracts/contractor-violation-contract.md`; the role contract (`actorRoles.Contractor` / `پیمانکار`) is confirmed.
- [ ] T002 [BLOCKED_BY_API_CONTRACT] Create sanitized representative actionable-list content/empty/error, correction 200/204/error, and malformed-response fixtures from the confirmed contract under `app/src/test/resources/contractor/`

**Checkpoint**: Stop implementation if T001 cannot be completed; do not invent role identifiers, endpoint paths, DTO fields, or client-side ContractId/OrganId authorization.

---

## Phase 2: Foundational Shared Infrastructure

**Purpose**: Add the typed contract boundary and reusable camera/location primitives required by all stories.

**⚠️ CRITICAL**: T001–T002 must be complete before this phase; this phase blocks all user-story work.

- [ ] T003 [BLOCKED_BY_API_CONTRACT] Define confirmed wire DTOs, `ContractorViolationItem`, opaque submission context, correction draft/request/result types, list UI states, and correction UI states in `app/src/main/java/Ir/co/tfs/farazaman/data/model/ContractorViolationModels.kt`
- [ ] T004 [BLOCKED_BY_API_CONTRACT] Add the confirmed actionable-list and correction methods to the existing authenticated interface, preserving Retrofit generic signatures and allowing bodyless success responses, in `app/src/main/java/Ir/co/tfs/farazaman/service/remote/ViolationApiService.kt`
- [ ] T005 [BLOCKED_BY_API_CONTRACT] Implement `ContractorViolationRepository` with DTO-to-domain mapping, no client-side contract/organ filtering, multipart image creation off Main, successful handling for every `Response.isSuccessful` result including 200/204, and existing API error parsing in `app/src/main/java/Ir/co/tfs/farazaman/data/repository/ContractorViolationRepository.kt`
- [X] T006 [P] Implement the Activity Result based camera-only capture coordinator with pending `MediaStore` URI, append/replace index handling, cancellation cleanup, and no gallery/file contract in `app/src/main/java/Ir/co/tfs/farazaman/activity/ViolationImageCaptureCoordinator.kt`
- [X] T007 [P] Implement the pure fresh-first and best-positive-accuracy last-known selection policy in `app/src/main/java/Ir/co/tfs/farazaman/domain/location/LocationFixPolicy.kt`
- [X] T008 [P] Extend the existing GPS/network lookup to return the selected reusable `Location` without changing current accuracy behavior in `app/src/main/java/Ir/co/tfs/farazaman/util/GpsAccuracyHelper.kt`
- [X] T009 Implement lifecycle-cancellable one-shot fresh acquisition, bounded timeout, provider checks, and fallback through `LocationFixPolicy`/`GpsAccuracyHelper` in `app/src/main/java/Ir/co/tfs/farazaman/util/LocationAcquisitionCoordinator.kt`
- [X] T010 [P] Add shared default and Persian Contractor role, list, correction, evidence, permission, location, loading, empty, error, retry, map, and “اصلاح ثبت شد” strings in `app/src/main/res/values/strings.xml` and `app/src/main/res/values-fa-rIR/strings.xml`

**Checkpoint**: Shared contract, repository, camera, and location primitives are available through existing project mechanisms; no second network, camera, location, or persistence stack exists.

---

## Phase 3: User Story 1 — Enter the Contractor Workflow (Priority: P1) 🎯 MVP

**Goal**: Recognize Contractor access, preserve existing Driver/Supervisor routing, and show the authoritative actionable-violation list with loading/content/empty/error states.

**Independent Test**: Sign in with Contractor-only and multi-role fixtures, choose Contractor where applicable, and verify the list identifiers/chapter/title exactly match the API response; verify empty/error behavior and unchanged Driver/Supervisor destinations.

### Tests for User Story 1

- [X] T011 [P] [US1] Write Contractor-only, multi-role, no-Contractor, exact `Contractor` actor-role key, and Driver/Supervisor regression tests in `app/src/test/java/Ir/co/tfs/farazaman/util/RolesManagerContractorTest.kt`
- [ ] T012 [P] [US1] [BLOCKED_BY_API_CONTRACT] Write list mapping, authoritative no-filtering, empty response, malformed fixture, and API failure tests in `app/src/test/java/Ir/co/tfs/farazaman/data/repository/ContractorViolationRepositoryListTest.kt`
- [ ] T013 [P] [US1] [BLOCKED_BY_API_CONTRACT] Write initial Loading→Content/Empty/Error and retry state tests with controlled repository results in `app/src/test/java/Ir/co/tfs/farazaman/presentation/viewmodel/ContractorViolationsViewModelTest.kt`

### Implementation for User Story 1

- [X] T014 [P] [US1] Add `hasContractorAccess` plus contractor-aware selectable-role count and only-role helpers without altering existing helper semantics in `app/src/main/java/Ir/co/tfs/farazaman/data/model/UserRoles.kt`
- [X] T015 [US1] Recognize only the exact confirmed `Contractor` key from `actorRoles` and return the extended `UserAccess` in `app/src/main/java/Ir/co/tfs/farazaman/util/RolesManager.kt`
- [ ] T016 [US1] [BLOCKED_BY_API_CONTRACT] Implement lifecycle-safe initial load, retry, and Loading/Content/Empty/Error `StateFlow` rendering state without local authorization filtering in `app/src/main/java/Ir/co/tfs/farazaman/presentation/viewmodel/ContractorViolationsViewModel.kt`
- [X] T017 [P] [US1] Create the RTL list host with toolbar, loading, RecyclerView, empty, error, and retry containers in `app/src/main/res/layout/activity_contractor_violations.xml`
- [X] T018 [P] [US1] Create the RTL violation card with chapter, title, local-status slot, and equal map/correction action controls modeled on `item_daily_plan_task.xml` in `app/src/main/res/layout/item_contractor_violation.xml`
- [ ] T019 [US1] [BLOCKED_BY_API_CONTRACT] Bind `ContractorViolationItem` chapter/title/status and expose map/correction callbacks without business filtering in `app/src/main/java/Ir/co/tfs/farazaman/activity/ContractorViolationAdapter.kt`
- [ ] T020 [US1] [BLOCKED_BY_API_CONTRACT] Connect the existing `BaseActivity`/Hilt/ViewBinding shell to lifecycle-aware list StateFlow collection and existing Toast/Snackbar error presentation in `app/src/main/java/Ir/co/tfs/farazaman/activity/ContractorViolationsActivity.kt`
- [X] T021 [P] [US1] Add `CONTRACTOR`, `showContractor`, selection semantics, Persian/RTL themed Contractor card, and accessibility labels in `app/src/main/java/Ir/co/tfs/farazaman/presentation/compose/RoleSelectionScreen.kt`
- [X] T022 [US1] Add Contractor-only, all-roles, disabled-selection, and dark RTL preview coverage in `app/src/debug/java/Ir/co/tfs/farazaman/presentation/compose/RoleSelectionScreenPreviews.kt`
- [X] T023 [US1] Extend role visibility/selection and explicit Intent routing to `ContractorViolationsActivity` while preserving current Supervisor/Driver handling in `app/src/main/java/Ir/co/tfs/farazaman/activity/RoleSelectionActivity.kt`
- [X] T024 [US1] Route Contractor-only users directly and multi-role users to existing role selection without changing Supervisor/Driver destinations in `app/src/main/java/Ir/co/tfs/farazaman/presentation/login/LoginActivity.kt`
- [X] T025 [US1] Declare `ContractorViolationsActivity` with the existing authenticated Activity/theme conventions in `app/src/main/AndroidManifest.xml`
- [ ] T026 [US1] [BLOCKED_BY_API_CONTRACT] Run the complete US1 unit-test group after list DTO/repository/ViewModel tests exist; `RolesManagerContractorTest` already passes.

**Checkpoint**: Contractor role entry and authoritative list states work independently; existing roles still route as before. The action buttons are rendered, while their complete flows are delivered by later stories.

---

## Phase 4: User Story 2 — Register a Violation Correction (Priority: P1)

**Goal**: Submit a description, at least one camera image, and an automatically acquired valid location exactly once; handle 200/204 identically and return a local success result without full-list refresh.

**Independent Test**: Open a known violation, enter a valid description, capture an image, use an available location, submit, and verify one request, success for both 200 and 204, form closure, return to the list, and only the matching card tagged locally without a list request.

### Tests for User Story 2

- [ ] T027 [P] [US2] [BLOCKED_BY_API_CONTRACT] Write request mapping, image part, non-2xx/network failure, and identical 200-with-body/200-empty/204 success tests in `app/src/test/java/Ir/co/tfs/farazaman/data/repository/ContractorViolationRepositorySubmitTest.kt`
- [ ] T028 [P] [US2] [BLOCKED_BY_API_CONTRACT] Complete immutable violation-ID and readable-payload tests after its type/transport contract is known; API-independent description/image-presence tests already pass in `app/src/test/java/Ir/co/tfs/farazaman/domain/validation/ContractorCorrectionValidatorTest.kt`.
- [ ] T029 [P] [US2] [BLOCKED_BY_API_CONTRACT] Write single-active-job, repeated-submit, Loading/Error/Success, failure draft retention, and success-result ID tests in `app/src/test/java/Ir/co/tfs/farazaman/presentation/viewmodel/ContractorCorrectionViewModelTest.kt`

### Implementation for User Story 2

- [X] T030 [P] [US2] Implement correction draft validation without coordinate entry or duplicate-count rules in `app/src/main/java/Ir/co/tfs/farazaman/domain/validation/ContractorCorrectionValidator.kt`
- [ ] T031 [US2] [BLOCKED_BY_API_CONTRACT] Implement SavedStateHandle-backed draft state, one active submission job, visible loading, repository submission, error retention, and body-independent success state in `app/src/main/java/Ir/co/tfs/farazaman/presentation/viewmodel/ContractorCorrectionViewModel.kt`
- [X] T032 [P] [US2] Create the Persian/RTL correction form with immutable violation context, description input, camera evidence area, validation messages, and disabled/loading submit control in `app/src/main/res/layout/activity_contractor_correction.xml`
- [ ] T033 [US2] [BLOCKED_BY_API_CONTRACT] Implement `BaseActivity`/Hilt/ViewBinding form rendering, normal camera capture, automatic-location request, duplicate-tap prevention, existing error display, success message, `RESULT_OK` violation ID, and finish behavior in `app/src/main/java/Ir/co/tfs/farazaman/activity/ContractorCorrectionActivity.kt`
- [ ] T034 [US2] [BLOCKED_BY_API_CONTRACT] Launch the correction form with immutable contract context and consume `RESULT_OK` to mark only the matching in-memory item without calling the list API in `app/src/main/java/Ir/co/tfs/farazaman/activity/ContractorViolationsActivity.kt`
- [ ] T035 [US2] [BLOCKED_BY_API_CONTRACT] Declare `ContractorCorrectionActivity` after its contract-backed Activity exists, using the existing authenticated Activity/theme conventions in `app/src/main/AndroidManifest.xml`
- [ ] T036 [US2] [BLOCKED_BY_API_CONTRACT] Run the complete US2 unit-test group after submit DTO/repository/ViewModel tests can be implemented.

**Checkpoint**: The normal correction path is business-complete for an available camera/location, accepts 200/204, preserves failed drafts, and never performs two simultaneous submissions.

---

## Phase 5: User Story 3 — Capture and Manage Correction Evidence (Priority: P2)

**Goal**: Offer Camera only and support add, preview, remove, replace, cancellation, and active-session preservation for evidence.

**Independent Test**: Capture multiple images, preview/remove/replace/add them, cancel a capture, rotate/recreate the form, and verify only final readable camera URIs are submitted and an empty evidence set blocks submit.

### Tests for User Story 3

- [ ] T037 [P] [US3] [BLOCKED_BY_API_CONTRACT] Write add/remove/replace/cancel, final-image validation, SavedStateHandle restoration, and unchanged-description evidence reducer tests after the contract-backed correction draft/ViewModel type exists.

### Implementation for User Story 3

- [X] T038 [P] [US3] Create the themed RTL image preview with Replace, Remove, and Close actions in `app/src/main/res/layout/bottom_sheet_violation_image_preview.xml`
- [ ] T039 [US3] [BLOCKED_BY_API_CONTRACT] Wire `SelectedImagesAdapter`, the existing all-images layout, preview sheet, remove/replace/add callbacks, readable-URI checks, SavedStateHandle restoration, and safe obsolete-media cleanup into the contract-backed `ContractorCorrectionActivity`.
- [X] T040 [P] [US3] Migrate the existing Supervisor camera launcher to `ViolationImageCaptureCoordinator` without changing its validation, selected-image output, or navigation behavior in `app/src/main/java/Ir/co/tfs/farazaman/activity/SubmitViolationFormActivity.kt`
- [X] T041 [P] [US3] Migrate the embedded Supervisor camera launcher to `ViolationImageCaptureCoordinator` without changing its validation, selected-image output, or navigation behavior in `app/src/main/java/Ir/co/tfs/farazaman/activity/CommitmentsViolationFormFragment.kt`
- [ ] T042 [US3] [BLOCKED_BY_API_CONTRACT] Run the evidence reducer tests after the contract-backed correction ViewModel exists, then manually regression-check both migrated Supervisor capture entry points on a device/emulator.

**Checkpoint**: Contractor evidence management is complete and camera-only; Supervisor capture behavior remains unchanged while sharing the coordinator.

---

## Phase 6: User Story 4 — Use a Valid Automatic Location (Priority: P2)

**Goal**: Recheck permission/services at submit, try a fresh fix first, fall back only to an existing valid last-known fix, and block submission otherwise.

**Independent Test**: Exercise fresh success, fresh timeout with valid fallback, missing permission, disabled services, no fix, lifecycle cancellation, and permission/service revocation while the form is open; verify only the first two cases submit.

### Tests for User Story 4

- [X] T043 [P] [US4] Write fresh-over-fallback, best-positive-accuracy GPS/network fallback, invalid coordinate, unavailable, and no-fabricated-default tests in `app/src/test/java/Ir/co/tfs/farazaman/domain/location/LocationFixPolicyTest.kt`
- [X] T044 [P] [US4] Write provider-enabled, timeout, cancellation, last-known fallback, and listener-removal tests with mocked Android location services in `app/src/test/java/Ir/co/tfs/farazaman/util/LocationAcquisitionCoordinatorTest.kt`
- [ ] T045 [P] [US4] [BLOCKED_BY_API_CONTRACT] Write missing-permission, disabled-services, unavailable-location, and unchanged-draft submission-gate tests after the contract-backed correction ViewModel exists.

### Implementation for User Story 4

- [ ] T046 [US4] [BLOCKED_BY_API_CONTRACT] Recheck fine/coarse permission and enabled providers on every submit, request permission through Activity Result APIs, offer `Settings.ACTION_LOCATION_SOURCE_SETTINGS`, invoke fresh-first acquisition, and keep the form on every blocked/error outcome in the contract-backed `ContractorCorrectionActivity`.
- [ ] T047 [US4] [BLOCKED_BY_API_CONTRACT] Ensure only a real `Fresh` or `LastKnown` result can enter repository submission, cancel acquisition with the form lifecycle, and never create hardcoded/zero/manual coordinates in the contract-backed `ContractorCorrectionViewModel`.
- [ ] T048 [US4] [BLOCKED_BY_API_CONTRACT] Run the complete US4 group after the submission-gate test exists; `LocationFixPolicyTest` and `LocationAcquisitionCoordinatorTest` already pass.

**Checkpoint**: Coordinates are always automatic and real; unavailable permission/services/fixes block the request with clear guidance and preserve the draft.

---

## Phase 7: User Story 5 — Inspect and Refresh the Work Queue (Priority: P3)

**Goal**: Show valid violation locations on the existing MapLibre stack and reconcile local submitted tags with authoritative data only on manual refresh or screen re-entry.

**Independent Test**: Open a valid map location, handle an invalid location without a crash, submit a correction, observe the local tag without a list request, manually refresh, and verify tags/list are replaced by the latest server response while stale overlapping requests cannot win.

### Tests for User Story 5

- [ ] T049 [P] [US5] [BLOCKED_BY_API_CONTRACT] Extend list ViewModel tests for local submitted tags, no post-success fetch, tag clearing on refresh, new-entry reload, overlapping refresh cancellation/generation ordering, and repeated non-simultaneous correction allowance after the authoritative item identifier is known.

### Implementation for User Story 5

- [X] T050 [P] [US5] Create a read-only RTL MapLibre host with title, unavailable state, and no Supervisor submission controls in `app/src/main/res/layout/activity_contractor_violation_location.xml`
- [X] T051 [US5] Implement a read-only `BaseActivity` that loads `BaranMapStyle`, draws/centers a supplied valid violation point through `MapLibreOverlayController`, and reports invalid/missing coordinates safely without assuming an API DTO in `app/src/main/java/Ir/co/tfs/farazaman/activity/ContractorViolationLocationActivity.kt`
- [X] T052 [US5] Declare `ContractorViolationLocationActivity` with existing map/authenticated Activity conventions in `app/src/main/AndroidManifest.xml`
- [ ] T053 [US5] [BLOCKED_BY_API_CONTRACT] Wire map navigation, unavailable-location handling, explicit refresh/retry UI, Activity-result local tagging, and no automatic post-success refresh after authoritative list coordinates and identifiers are known.
- [ ] T054 [US5] [BLOCKED_BY_API_CONTRACT] Implement latest-request-wins refresh, transient submitted-ID clearing only on explicit refresh/new ViewModel entry, and API-authoritative state replacement after the list contract is known.
- [ ] T055 [US5] [BLOCKED_BY_API_CONTRACT] Run the extended list tests and manually validate map/tag/refresh/re-entry scenarios after the list/correction API flow exists.

**Checkpoint**: Map inspection and authoritative refresh semantics are complete; transient feedback never becomes durable workflow state.

---

## Phase 8: Polish and Cross-Cutting Validation

**Purpose**: Validate security, RTL/theme consistency, Release/R8 compatibility, and regressions after all desired stories are complete.

- [ ] T056 [P] [BLOCKED_BY_API_CONTRACT] Audit Contractor Retrofit calls, multipart handling, and repository logs after the contract-backed repository exists; current shell/helpers do not log sensitive payloads.
- [X] T057 [P] Verify all new XML and Compose surfaces use project theme tokens, Persian/RTL alignment, accessibility descriptions, scroll-safe bottom spacing, and night resources, applying fixes in `app/src/main/res/layout/activity_contractor_violations.xml`, `app/src/main/res/layout/item_contractor_violation.xml`, `app/src/main/res/layout/activity_contractor_correction.xml`, and `app/src/main/java/Ir/co/tfs/farazaman/presentation/compose/RoleSelectionScreen.kt`
- [ ] T058 [BLOCKED_BY_API_CONTRACT] Confirm existing Retrofit/Gson keep rules preserve the future Contractor DTO fields, suspend/`Response` generic signatures, and service methods under minification after those contract-backed types exist.
- [X] T059 Update the feature-to-code map and stable project context with confirmed Contractor entry points, explicit unknown API/repository status, camera/location helpers, and confirmed Release build behavior in `docs/CODEMAP.md`, `docs/PROJECT_CONTEXT.md`, and `docs/RELEASE_NOTES.md`
- [ ] T060 [BLOCKED_BY_EXISTING_LINT] `:app:testDebugUnitTest` and `:app:assembleDebug` pass. `:app:lintDebug` reaches one unrelated pre-existing error at `service/gps/GPSLogger.java:94` (`UnspecifiedRegisterReceiverFlag`); do not modify it as part of this feature.
- [ ] T061 [BLOCKED_BY_API_CONTRACT] Signed `:app:assembleRelease` and R8/resource shrinking pass. Run the remaining authenticated list, map navigation, camera form, and 200/204 runtime smoke path after the Contractor API contract is implemented.
- [ ] T062 [BLOCKED_BY_API_CONTRACT] Execute the complete device/emulator acceptance and regression guide after the actionable-list and correction flows can be implemented.

---

## Dependencies and Execution Order

### Phase dependencies

- **Phase 1 — Contract capture**: No code work may start until T001 confirms the existing backend contract; T002 depends on T001.
- **Phase 2 — Foundation**: Depends on T001–T002 and blocks every user story. T006, T007, T008, and T010 can proceed in parallel; T009 depends on T007–T008; T005 depends on T003–T004.
- **US1 (Phase 3)**: Depends on Phase 2 and establishes Contractor routing/list entry used by later stories.
- **US2 (Phase 4)**: Depends on US1 for list-to-form/result integration and on Phase 2 for repository/camera/location primitives.
- **US3 (Phase 5)**: Depends on the US2 form shell and shared camera coordinator; it can otherwise proceed independently of US4.
- **US4 (Phase 6)**: Depends on the US2 form shell and shared location coordinator; it can proceed in parallel with US3.
- **US5 (Phase 7)**: Map work depends only on US1/Foundation; full local-tag/refresh validation also depends on US2 success-result handling.
- **Polish (Phase 8)**: Depends on all stories selected for release.

### User story completion order

```text
Contract confirmation
    -> Shared foundation
        -> US1 Role + authoritative list (MVP)
            -> US2 Normal correction submission
                -> US3 Complete evidence management
                -> US4 Complete location edge handling
            -> US5 Map + refresh/local-tag reconciliation
                -> Release/R8 validation
```

### User story independence

- **US1**: Testable after Foundation with role fixtures and list responses; no correction success is required.
- **US2**: Testable after US1/Foundation on the normal camera/location path; its output is an Activity result and local tag, not a server-list refresh.
- **US3**: Testable with the correction form and camera independently of API success by inspecting final URI state and validation.
- **US4**: Testable with controlled LocationManager states independently of image-management variations, using one valid evidence URI.
- **US5**: Map behavior is testable with coordinates from a fixture; refresh/tag semantics are testable with repository generations and the US2 result contract.

---

## Parallel Execution Examples

### User Story 1

```text
After Phase 2:
- T011 RolesManagerContractorTest.kt
- T012 ContractorViolationRepositoryListTest.kt
- T013 ContractorViolationsViewModelTest.kt

After models are stable:
- T017 activity_contractor_violations.xml
- T018 item_contractor_violation.xml
- T021 RoleSelectionScreen.kt
```

### User Story 2

```text
- T027 ContractorViolationRepositorySubmitTest.kt
- T028 ContractorCorrectionValidatorTest.kt
- T029 ContractorCorrectionViewModelTest.kt

After test contracts are written:
- T030 ContractorCorrectionValidator.kt
- T032 activity_contractor_correction.xml
```

### User Story 3

```text
- T037 ContractorCorrectionEvidenceTest.kt
- T038 bottom_sheet_violation_image_preview.xml

After ViolationImageCaptureCoordinator is stable:
- T040 SubmitViolationFormActivity.kt behavior-preserving migration
- T041 CommitmentsViolationFormFragment.kt behavior-preserving migration
```

### User Story 4

```text
- T043 LocationFixPolicyTest.kt
- T044 LocationAcquisitionCoordinatorTest.kt
- T045 ContractorCorrectionLocationFlowTest.kt
```

### User Story 5

```text
- T049 ContractorViolationsViewModelTest.kt extension
- T050 activity_contractor_violation_location.xml
```

---

## Implementation Strategy

### MVP first

1. Complete T001–T010 (contract and foundation).
2. Complete T011–T026 (US1).
3. Stop and validate Contractor-only/multi-role routing plus authoritative list states.
4. This is the smallest demonstrable MVP slice; a business-complete correction release additionally requires US2 and the US3/US4 evidence/location gates.

### Incremental delivery

1. **Role/list slice**: US1 provides visible, server-authoritative Contractor work.
2. **Normal submit slice**: US2 provides a single successful correction path and local result contract.
3. **Evidence/location hardening**: US3 and US4 complete mandatory field-operation safeguards and may be implemented in parallel.
4. **Inspection/reconciliation**: US5 adds read-only map and explicit authoritative refresh behavior.
5. **Release gate**: Phase 8 validates Debug, lint, signed Release/R8, and existing-role regressions.

### Commit boundaries

- Keep T040–T041 behavior-preserving Supervisor camera extraction in a separate commit from Contractor UI behavior.
- Keep backend-contract DTO/service edits separate from role/UI edits.
- Do not include Driver changes, unrelated formatting, architecture migration, persistent local correction status, offline queues, notifications, or backend rules.

## Notes

- `[P]` means different files and no dependency on another incomplete task in the same group.
- Tests named before implementation should be written first and observed failing for the intended reason.
- Use the runtime-selected `BASE_URL`, `AuthInterceptor`, and existing `ViolationApiService`; never instantiate a second Retrofit/OkHttp client.
- API output remains authoritative; local submitted IDs are presentation-only and discarded on refresh/re-entry.
- No broad storage permission, gallery picker, manual coordinate entry, fabricated coordinate, or client-side duplicate-correction business rule is permitted.

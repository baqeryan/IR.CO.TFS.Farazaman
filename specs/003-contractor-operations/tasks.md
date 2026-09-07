# Tasks: Contractor Operations

**Input**: Design documents from `/specs/003-contractor-operations/`

## Phase 1: Setup

- [X] T001 Add Contractor gateway and work-context boundary in app/src/main/java/Ir/co/tfs/farazaman/contractor/ContractorGateway.kt and ContractorWorkContextStore.kt
- [X] T002 Add Contractor operation terminology and not-connected messages in app/src/main/res/values/strings.xml

## Phase 2: Foundational

- [X] T003 Add Contractor Activities to app/src/main/AndroidManifest.xml
- [X] T004 Route Contractor role through work-area selection in app/src/main/java/Ir/co/tfs/farazaman/activity/RoleSelectionActivity.kt

## Phase 3: User Story 1 - Work-area entry (P1)

**Independent Test**: Selecting Contractor opens an RTL work-area screen before any Contractor operational surface.

- [X] T005 [US1] Build Contractor work-area selection shell in app/src/main/java/Ir/co/tfs/farazaman/activity/ContractorWorkAreaSelectionActivity.kt and app/src/main/res/layout/activity_contractor_work_area_selection.xml

## Phase 4: User Story 2 - Contractor operation shell (P1)

**Independent Test**: Contractor home shows the selected context and operation wording without Supervisor violation actions.

- [X] T006 [US2] Build Contractor operations home and navigation in app/src/main/java/Ir/co/tfs/farazaman/activity/ContractorOperationsActivity.kt and app/src/main/res/layout/activity_contractor_operations.xml

## Phase 5: User Story 3 - Register action (P1)

**Independent Test**: Action form captures description and camera images, supports remove/preview behavior, and cannot claim success while the gateway is unavailable.

- [X] T007 [US3] Implement reusable Contractor action draft validation in app/src/main/java/Ir/co/tfs/farazaman/contractor/ContractorDraftValidator.kt
- [X] T008 [US3] Build camera-backed action form in app/src/main/java/Ir/co/tfs/farazaman/activity/ContractorActionActivity.kt and app/src/main/res/layout/activity_contractor_action.xml

## Phase 6: User Story 4 - Correction queue (P2)

**Independent Test**: Corrections opens the existing authoritative empty/not-connected queue with no fabricated items.

- [X] T009 [US4] Relabel and open the existing correction queue from app/src/main/java/Ir/co/tfs/farazaman/activity/ContractorViolationsActivity.kt and app/src/main/res/layout/activity_contractor_violations.xml

## Phase 7: User Story 5 - Correction draft (P2)

**Independent Test**: Existing correction form remains tied to a supplied violation reference and does not submit without a future service contract.

- [X] T010 [US5] Preserve the correction form/map placeholders and document gateway handoff in app/src/main/java/Ir/co/tfs/farazaman/contractor/ContractorGateway.kt

## Phase 8: Polish and validation

- [X] T011 Add validation unit tests in app/src/test/java/Ir/co/tfs/farazaman/contractor/ContractorDraftValidatorTest.kt
- [X] T012 Update docs/CODEMAP.md and docs/PROJECT_CONTEXT.md and run focused unit tests plus Debug compilation

## Dependencies

T001-T002 precede T003-T004. T005 precedes T006. T006 precedes T008-T009. T007 precedes T008. T011-T012 follow implementation.

## Implementation Strategy

Deliver the safe UI foundation now. Backend calls remain explicitly unavailable until their catalog is supplied; later tasks will replace gateway methods without redesigning screens or routing.

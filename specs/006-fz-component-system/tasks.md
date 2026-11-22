# Tasks: FaraZaman Component System

**Input**: Design documents from `/specs/006-fz-component-system/`

**Tests**: Contract, semantics, screenshot and design-drift tests are required by FR-011.

## Phase 1: Setup

- [X] T001 Record the normalized reference inventory and completion matrix in `specs/006-fz-component-system/contracts/component-catalog.md`
- [X] T002 Create Debug catalog navigation shell in `app/src/debug/java/Ir/co/tfs/farazaman/presentation/design/catalog/FzCatalogScreen.kt`
- [X] T003 [P] Add reusable catalog section/example models in `app/src/debug/java/Ir/co/tfs/farazaman/presentation/design/catalog/FzCatalogModels.kt`
- [X] T004 [P] Extend design-drift coverage for new component paths in `app/src/test/java/Ir/co/tfs/farazaman/presentation/design/ComposeDesignDriftTest.kt`

## Phase 2: Foundational

- [X] T005 Add shared size, intent, orientation and selection enums in `core/ui/src/main/java/Ir/co/tfs/farazaman/presentation/design/theme/FzComponentContracts.kt`
- [X] T006 Add resolved size metrics to `core/ui/src/main/java/Ir/co/tfs/farazaman/presentation/design/theme/ComponentSizes.kt`
- [X] T007 [P] Add caller-owned generic selection reducer in `core/ui/src/main/java/Ir/co/tfs/farazaman/presentation/design/state/FzSelectionState.kt`
- [X] T008 [P] Add collection, field and overlay state contracts in `core/ui/src/main/java/Ir/co/tfs/farazaman/presentation/design/state/FzComponentStates.kt`
- [X] T009 [P] Add semantic component color resolution in `core/ui/src/main/java/Ir/co/tfs/farazaman/presentation/design/theme/FzComponentColors.kt`
- [X] T010 Add foundation unit tests in `core/ui/src/test/java/Ir/co/tfs/farazaman/presentation/design/state/FzSelectionStateTest.kt`
- [X] T011 Create accessibility and minimum-touch-target helpers in `core/ui/src/main/java/Ir/co/tfs/farazaman/presentation/design/components/FzAccessibility.kt`

## Phase 3: User Story 1 - Foundations and actions (P1)

**Independent Test**: Catalog renders all action/selection sizes and state transitions in Persian RTL light/dark.

- [X] T012 [P] [US1] Add button size/intent contract tests in `core/ui/src/test/java/Ir/co/tfs/farazaman/presentation/design/components/FzButtonContractTest.kt`
- [X] T013 [P] [US1] Add chip selection reducer tests in `core/ui/src/test/java/Ir/co/tfs/farazaman/presentation/design/components/FzChipSetTest.kt`
- [X] T014 [US1] Upgrade buttons and icon buttons with shared sizes/intents while preserving call sites in `core/ui/src/main/java/Ir/co/tfs/farazaman/presentation/design/components/FzButton.kt` and `FzIconButton.kt`
- [X] T015 [P] [US1] Implement floating action and button group in `core/ui/src/main/java/Ir/co/tfs/farazaman/presentation/design/components/FzActionComponents.kt`
- [X] T016 [US1] Implement interactive chip and chip set while retaining status chip in `core/ui/src/main/java/Ir/co/tfs/farazaman/presentation/design/components/FzChip.kt`
- [X] T017 [P] [US1] Implement checkbox, radio and switch families in `core/ui/src/main/java/Ir/co/tfs/farazaman/presentation/design/components/FzSelectionControls.kt`
- [X] T018 [P] [US1] Implement toggle groups, sliders and rating in `core/ui/src/main/java/Ir/co/tfs/farazaman/presentation/design/components/FzValueControls.kt`
- [X] T019 [US1] Add action/selection examples in `app/src/debug/java/Ir/co/tfs/farazaman/presentation/design/catalog/FzActionCatalog.kt`
- [X] T020 [US1] Add action/selection semantics screenshots in `app/src/test/java/Ir/co/tfs/farazaman/presentation/design/FzActionComponentsTest.kt`

## Phase 4: User Story 2 - Layout, navigation and display (P2)

**Independent Test**: Layout/navigation/display catalog reflows at compact/expanded widths without clipping.

- [X] T021 [P] [US2] Implement container, responsive, grid, stack and spacer primitives in `core/ui/src/main/java/Ir/co/tfs/farazaman/presentation/design/components/FzLayout.kt`
- [X] T022 [P] [US2] Implement surface, divider, avatar, badge, icon and image in `core/ui/src/main/java/Ir/co/tfs/farazaman/presentation/design/components/FzDisplayPrimitives.kt`
- [X] T023 [P] [US2] Implement list and list-item contracts in `core/ui/src/main/java/Ir/co/tfs/farazaman/presentation/design/components/FzList.kt`
- [X] T024 [P] [US2] Implement collapse and expansion panels in `core/ui/src/main/java/Ir/co/tfs/farazaman/presentation/design/components/FzExpansion.kt`
- [X] T025 [P] [US2] Implement carousel, split panel and swipe area in `core/ui/src/main/java/Ir/co/tfs/farazaman/presentation/design/components/FzAdaptiveContent.kt`
- [X] T026 [P] [US2] Implement app bars, toolbar and scroll-to-top in `core/ui/src/main/java/Ir/co/tfs/farazaman/presentation/design/components/FzBars.kt`
- [X] T027 [P] [US2] Implement bottom navigation, rail and drawer contracts in `core/ui/src/main/java/Ir/co/tfs/farazaman/presentation/design/components/FzNavigation.kt`
- [X] T028 [P] [US2] Implement menu, navigation menu, links and breadcrumbs in `core/ui/src/main/java/Ir/co/tfs/farazaman/presentation/design/components/FzMenus.kt`
- [X] T029 [P] [US2] Implement tabs, pagination and section navigation in `core/ui/src/main/java/Ir/co/tfs/farazaman/presentation/design/components/FzTabsPagination.kt`
- [X] T030 [P] [US2] Implement timeline, tree, stepper and highlighted text in `core/ui/src/main/java/Ir/co/tfs/farazaman/presentation/design/components/FzStructuredContent.kt`
- [X] T031 [US2] Add layout/navigation examples in `app/src/debug/java/Ir/co/tfs/farazaman/presentation/design/catalog/FzLayoutCatalog.kt`
- [X] T032 [US2] Add responsive RTL semantics/screenshots in `app/src/test/java/Ir/co/tfs/farazaman/presentation/design/FzLayoutComponentsTest.kt`

## Phase 5: User Story 3 - Forms and pickers (P3)

**Independent Test**: A complete Persian form validates and restores all supported input/selection values.

- [X] T033 [P] [US3] Upgrade text field and add field/form contracts in `core/ui/src/main/java/Ir/co/tfs/farazaman/presentation/design/components/FzFields.kt`
- [X] T034 [P] [US3] Implement numeric field and input masks in `core/ui/src/main/java/Ir/co/tfs/farazaman/presentation/design/components/FzNumericMask.kt`
- [X] T035 [P] [US3] Implement autocomplete and select in `core/ui/src/main/java/Ir/co/tfs/farazaman/presentation/design/components/FzChoiceFields.kt`
- [X] T036 [P] [US3] Implement file picker and drop-zone contracts in `core/ui/src/main/java/Ir/co/tfs/farazaman/presentation/design/components/FzFileInput.kt`
- [X] T037 [P] [US3] Implement color picker in `core/ui/src/main/java/Ir/co/tfs/farazaman/presentation/design/components/FzColorPicker.kt`
- [X] T038 [P] [US3] Implement Persian date/date-range picker in `core/ui/src/main/java/Ir/co/tfs/farazaman/presentation/design/components/FzDatePicker.kt`
- [X] T039 [P] [US3] Implement time picker in `core/ui/src/main/java/Ir/co/tfs/farazaman/presentation/design/components/FzTimePicker.kt`
- [X] T040 [US3] Add form/picker examples in `app/src/debug/java/Ir/co/tfs/farazaman/presentation/design/catalog/FzFormCatalog.kt`
- [X] T041 [US3] Add validation, restoration and accessibility tests in `app/src/test/java/Ir/co/tfs/farazaman/presentation/design/FzFormComponentsTest.kt`

## Phase 6: User Story 4 - Feedback and overlays (P4)

**Independent Test**: Each feedback/overlay can open, announce state, handle action/back and dismiss exactly once.

- [X] T042 [P] [US4] Implement alert and message box in `core/ui/src/main/java/Ir/co/tfs/farazaman/presentation/design/components/FzAlertMessage.kt`
- [X] T043 [P] [US4] Implement dialog and back confirmation in `core/ui/src/main/java/Ir/co/tfs/farazaman/presentation/design/components/FzDialog.kt`
- [X] T044 [P] [US4] Implement snackbar host and message contract in `core/ui/src/main/java/Ir/co/tfs/farazaman/presentation/design/components/FzSnackbar.kt`
- [X] T045 [P] [US4] Implement overlay, popover and tooltip in `core/ui/src/main/java/Ir/co/tfs/farazaman/presentation/design/components/FzOverlay.kt`
- [X] T046 [P] [US4] Implement skeleton and progress indicators in `core/ui/src/main/java/Ir/co/tfs/farazaman/presentation/design/components/FzProgress.kt`
- [X] T047 [US4] Add feedback examples and reduced-motion cases in `app/src/debug/java/Ir/co/tfs/farazaman/presentation/design/catalog/FzFeedbackCatalog.kt`
- [X] T048 [US4] Add overlay focus/back/dismiss tests in `app/src/test/java/Ir/co/tfs/farazaman/presentation/design/FzFeedbackComponentsTest.kt`

## Phase 7: User Story 5 - Data and advanced components (P5)

**Independent Test**: Representative Persian datasets retain sort/filter/page/selection state and scroll smoothly.

- [X] T049 [P] [US5] Implement simple and structured table contracts in `core/ui/src/main/java/Ir/co/tfs/farazaman/presentation/design/components/FzTable.kt`
- [X] T050 [US5] Implement data grid state and presentation in `core/ui/src/main/java/Ir/co/tfs/farazaman/presentation/design/components/FzDataGrid.kt`
- [X] T051 [P] [US5] Implement virtualized list contract in `core/ui/src/main/java/Ir/co/tfs/farazaman/presentation/design/components/FzVirtualizedList.kt`
- [X] T052 [P] [US5] Implement line/bar/pie/sparkline chart contract in `core/ui/src/main/java/Ir/co/tfs/farazaman/presentation/design/components/FzChart.kt`
- [X] T053 [US5] Add data/advanced catalog examples in `app/src/debug/java/Ir/co/tfs/farazaman/presentation/design/catalog/FzDataCatalog.kt`
- [X] T054 [US5] Add collection state/performance/semantics tests in `app/src/test/java/Ir/co/tfs/farazaman/presentation/design/FzDataComponentsTest.kt`

## Phase 8: Polish and rollout

- [X] T055 Wire all catalog sections and completeness assertions in `app/src/debug/java/Ir/co/tfs/farazaman/presentation/design/catalog/FzCatalogScreen.kt`
- [X] T056 [P] Add light/dark RTL Roborazzi matrices in `app/src/test/java/Ir/co/tfs/farazaman/presentation/design/FzCatalogScreenshotTest.kt`
- [X] T057 [P] Update component locations and usage policy in `docs/CODEMAP.md` and `docs/PROJECT_CONTEXT.md`
- [X] T058 Run the commands and scenarios in `specs/006-fz-component-system/quickstart.md`
- [X] T059 Validate Release lint/build and document results in `docs/RELEASE_NOTES.md`
- [X] T060 Mark every catalog entry implemented or platform-mapped in `specs/006-fz-component-system/contracts/component-catalog.md`

## Dependencies and execution order

- Setup precedes Foundational; Foundational blocks every user story.
- US1 establishes APIs reused by US2-US5.
- US2 and US3 may proceed after US1; US4 depends on US1 layout/action primitives; US5 depends on US1-US2.
- Catalog and test tasks follow implementation within each story.

## Implementation strategy

- MVP: T001-T020 delivers complete foundations, sized buttons and real chip/chip-set behavior.
- Each later story is independently cataloged and validated before the next family is adopted by production screens.
- Existing screens migrate only after the relevant family checkpoint passes.

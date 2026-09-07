# Tasks: Material 3 Design System و مهاجرت تدریجی Compose

**Input**: اسناد `specs/004-compose-ui-migration/`  
**Rule**: هیچ production Screen پیش از تکمیل Foundation و هیچ Support/Supervisor Screen پیش از Contractor Architecture Gate مهاجرت نمی‌شود.

## Phase 1: Inventory / Baseline

- [ ] T001 تمام مقصدهای user-facing در CODEMAP و Manifest را با Activity/Fragment/layout، owner، role و risk در `specs/004-compose-ui-migration/migration-inventory.md` ثبت کن
- [ ] T002 [P] تمام Composeهای موجود شامل `RoleSelectionScreen`, `AppUpdateUi`, `SplashScreen` و `FaraZamanTheme` را از نظر State/Event، Material imports، hard-coded styling و Preview در `specs/004-compose-ui-migration/baselines/existing-compose-audit.md` ممیزی کن
- [ ] T003 [P] baseline رفتاری و Visual Intent صفحات Contractor را در `specs/004-compose-ui-migration/baselines/contractor.md` ثبت کن
- [ ] T004 [P] baseline Support/Supervisor/System flows را در `specs/004-compose-ui-migration/baselines/remaining-screens.md` ثبت کن
- [ ] T005 Material View/Compose bridge، removal condition، coexistence و rollback هر مقصد را در `specs/004-compose-ui-migration/migration-inventory.md` تکمیل کن

## Phase 2: User Story 1 — Material 3 + Farazaman Design System Foundation (P1)

**Goal**: ایجاد source واحد Theme/tokens و حداقل Fz components لازم برای Contractor/shell.  
**Independent Test**: کاتالوگ Foundation در matrix کامل رندر و تغییر palette/shape بدون تغییر Feature منعکس شود.

### Audit and tests first

- [ ] T006 [P] [US1] نسخه‌های واقعی Kotlin 1.9.22، AGP 8.4.2، Compiler 1.5.10، BOM 2024.02.02 و Material dependencies را در `specs/004-compose-ui-migration/baselines/material3-dependency-audit.md` ثبت و عدم نیاز upgrade را تأیید کن
- [ ] T007 [P] [US1] تست dependency direction و ممنوعیت business imports در design package را در `app/src/test/java/Ir/co/tfs/farazaman/presentation/design/DesignSystemArchitectureTest.kt` بنویس
- [ ] T008 [P] [US1] تست semantic variant fallback و عدم نشت raw styling را در `app/src/test/java/Ir/co/tfs/farazaman/presentation/design/DesignTokenContractTest.kt` بنویس

### Theme and tokens

- [ ] T009 [US1] `FaraZamanTheme` موجود را بدون source موازی از `app/src/main/java/Ir/co/tfs/farazaman/presentation/compose/FaraZamanTheme.kt` به `app/src/main/java/Ir/co/tfs/farazaman/presentation/design/theme/FaraZamanTheme.kt` evolve کن و call-site compatibility را حفظ کن
- [ ] T010 [P] [US1] Material light/dark ColorScheme و semantic success/warning/info colors را در `app/src/main/java/Ir/co/tfs/farazaman/presentation/design/theme/Color.kt` تعریف کن
- [ ] T011 [P] [US1] Persian semantic typography را در `app/src/main/java/Ir/co/tfs/farazaman/presentation/design/theme/Typography.kt` تعریف کن
- [ ] T012 [P] [US1] semantic shapes را در `app/src/main/java/Ir/co/tfs/farazaman/presentation/design/theme/Shapes.kt` تعریف کن
- [ ] T013 [P] [US1] scale معنایی spacing را در `app/src/main/java/Ir/co/tfs/farazaman/presentation/design/theme/Spacing.kt` تعریف کن
- [ ] T014 [P] [US1] elevation roles را در `app/src/main/java/Ir/co/tfs/farazaman/presentation/design/theme/Elevation.kt` تعریف کن
- [ ] T015 [P] [US1] motion tokens و reduced-motion policy را در `app/src/main/java/Ir/co/tfs/farazaman/presentation/design/theme/Motion.kt` تعریف کن
- [ ] T016 [P] [US1] touch/icon/button/card sizes را در `app/src/main/java/Ir/co/tfs/farazaman/presentation/design/theme/ComponentSizes.kt` تعریف کن
- [ ] T017 [US1] ThemeMode/ThemeConfiguration قابل جایگزینی و Dynamic Color پیش‌فرض خاموش را در `app/src/main/java/Ir/co/tfs/farazaman/presentation/design/theme/ThemeMode.kt` متصل کن

### Design System Components — created intentionally before production Screens

- [ ] T018 [P] [US1] `FzButton` با Primary/Secondary/Danger/Ghost و loading/disabled semantics را در `app/src/main/java/Ir/co/tfs/farazaman/presentation/design/components/FzButton.kt` بساز
- [ ] T019 [P] [US1] `FzCard` با variantهای اثبات‌شده Contractor/shell را در `app/src/main/java/Ir/co/tfs/farazaman/presentation/design/components/FzCard.kt` بساز
- [ ] T020 [P] [US1] `FzTextField` با normal/error/disabled و error announcement را در `app/src/main/java/Ir/co/tfs/farazaman/presentation/design/components/FzTextField.kt` بساز
- [ ] T021 [P] [US1] `FzStatusChip` با Default/Info/Success/Warning/Error را در `app/src/main/java/Ir/co/tfs/farazaman/presentation/design/components/FzStatusChip.kt` بساز
- [ ] T022 [US1] `FzScaffold` را با content insets، RTL و shell slots لازم در `app/src/main/java/Ir/co/tfs/farazaman/presentation/design/components/FzScaffold.kt` بساز
- [ ] T023 [P] [US1] `FzLoading`, `FzEmptyState`, `FzErrorState` را در `app/src/main/java/Ir/co/tfs/farazaman/presentation/design/components/FzFeedbackStates.kt` بساز
- [ ] T024 [US1] نیاز فوری `FzBadge/FzDialog/FzTopAppBar` را از audit ارزیابی و فقط componentهای اثبات‌شده را در `app/src/main/java/Ir/co/tfs/farazaman/presentation/design/components/` اضافه یا تصمیم عدم ایجاد را در `specs/004-compose-ui-migration/design-system.md` ثبت کن

`ViolationCard`, `NotificationCard`, `ContractorHeader` و سایر Feature Shared Components در این Phase ساخته نمی‌شوند؛ آن‌ها فقط پس از اثبات تکرار داخل package Feature ایجاد می‌شوند.

### Preview and foundation gates

- [ ] T025 [US1] MultiPreview مشترک Persian RTL/light/dark/large-font/small-normal-phone را در `app/src/debug/java/Ir/co/tfs/farazaman/presentation/design/preview/FarazamanPreviews.kt` ایجاد کن
- [ ] T026 [P] [US1] catalog رنگ، typography، shapes و spacing را در `app/src/debug/java/Ir/co/tfs/farazaman/presentation/design/preview/DesignTokenPreviews.kt` بساز
- [ ] T027 [P] [US1] catalog تمام component variants/states را در `app/src/debug/java/Ir/co/tfs/farazaman/presentation/design/preview/DesignSystemPreviews.kt` بساز
- [ ] T028 [US1] instrumentation matrix برای RTL/dark/font scale/touch target/small phone و shell tablet را در `app/src/androidTest/java/Ir/co/tfs/farazaman/presentation/design/DesignSystemMatrixTest.kt` اضافه کن
- [ ] T029 [US1] آزمون تعویض palette و base Button/Card shape بدون Feature source change را در `app/src/androidTest/java/Ir/co/tfs/farazaman/presentation/design/ThemeReplaceabilityTest.kt` اضافه کن
- [ ] T030 [US1] checklist Foundation را در `specs/004-compose-ui-migration/checklists/design-system-checklist.md` تکمیل و نتایج build/test را در `specs/004-compose-ui-migration/migration-inventory.md` ثبت کن

## Phase 3: Shared State/Event Architecture Foundation

**CRITICAL**: Phase 2 و این Phase پیش‌نیاز Contractor هستند.

- [ ] T031 قرارداد State/Event/Effect و pure Screen را در `app/src/main/java/Ir/co/tfs/farazaman/presentation/compose/UiContract.kt` پیاده کن
- [ ] T032 [P] stateهای مشترک loading/empty/error و semantics keys را در `app/src/main/java/Ir/co/tfs/farazaman/presentation/compose/CommonUiState.kt` تعریف کن
- [ ] T033 [P] fixture/providerهای Screen state را در `app/src/debug/java/Ir/co/tfs/farazaman/presentation/design/preview/PreviewFixtures.kt` اضافه کن
- [ ] T034 lifecycle-aware route/effect pattern را در `app/src/main/java/Ir/co/tfs/farazaman/presentation/compose/RouteEffectHandler.kt` تعریف و contract آن را مستند کن
- [ ] T035 معماری Screen را در `app/src/test/java/Ir/co/tfs/farazaman/presentation/compose/ComposeScreenArchitectureTest.kt` برای network/storage/Intent/system API، Material 2 و Experimental imports enforce کن
- [ ] T036 static scan کم‌نویز hard-coded Color/fontSize/reusable shape/direct styled Material را با allowlist مستند در `app/src/test/java/Ir/co/tfs/farazaman/presentation/compose/ComposeDesignDriftTest.kt` اضافه کن
- [ ] T037 Composeهای موجود را بدون rewrite غیرضروری با Theme واحد و contract جدید سازگار و regression tests را در `app/src/test/java/Ir/co/tfs/farazaman/presentation/compose/ExistingComposeContractTest.kt` اضافه کن

## Phase 4: User Story 2 — Contractor MVP (P1)

**Goal**: اولین مصرف production از State/Event و Design System.  
**Independent Test**: جریان Contractor با Behavioral Parity، Visual Intent ثبت‌شده، Preview/accessibility matrix و بدون styling موازی قبول شود.

- [ ] T038 [P] [US2] reducer tests انتخاب محدوده را در `app/src/test/java/Ir/co/tfs/farazaman/presentation/viewmodel/ContractorWorkAreaViewModelTest.kt` بنویس
- [ ] T039 [P] [US2] reducer tests عملیات/اقدام را در `app/src/test/java/Ir/co/tfs/farazaman/presentation/viewmodel/ContractorOperationsViewModelTest.kt` بنویس
- [ ] T040 [P] [US2] reducer tests اصلاحیه/مکان را در `app/src/test/java/Ir/co/tfs/farazaman/presentation/viewmodel/ContractorCorrectionsViewModelTest.kt` بنویس
- [ ] T041 [US2] State/Event/Route/Screen انتخاب محدوده را در `app/src/main/java/Ir/co/tfs/farazaman/presentation/compose/ContractorWorkAreaScreen.kt` پیاده و `app/src/main/java/Ir/co/tfs/farazaman/activity/ContractorWorkAreaSelectionActivity.kt` را container کن
- [ ] T042 [US2] عملیات پیمانکار را در `app/src/main/java/Ir/co/tfs/farazaman/presentation/compose/ContractorOperationsScreen.kt` پیاده و `app/src/main/java/Ir/co/tfs/farazaman/activity/ContractorOperationsActivity.kt` را container کن
- [ ] T043 [US2] فرم اقدام را با FzTextField/Button/Card و camera effect بیرونی در `app/src/main/java/Ir/co/tfs/farazaman/presentation/compose/ContractorActionScreen.kt` و `app/src/main/java/Ir/co/tfs/farazaman/activity/ContractorActionActivity.kt` پیاده کن
- [ ] T044 [US2] feature status mapping و فهرست اصلاحیه را در `app/src/main/java/Ir/co/tfs/farazaman/presentation/compose/ContractorCorrectionsScreen.kt` پیاده کن؛ `ContractorHeader` یا card مشترک را فقط در صورت اثبات تکرار به‌عنوان Feature Shared Component در همین package استخراج و container موجود را متصل کن
- [ ] T045 [US2] read-only location را با lifecycle map bridge در `app/src/main/java/Ir/co/tfs/farazaman/presentation/compose/ContractorViolationLocationScreen.kt` و Activity موجود پیاده کن
- [ ] T046 [P] [US2] Feature Component و Screen preview matrix پیمانکار را در `app/src/debug/java/Ir/co/tfs/farazaman/presentation/compose/ContractorScreenPreviews.kt` تکمیل کن
- [ ] T047 [US2] journey/accessibility tests پیمانکار را در `app/src/androidTest/java/Ir/co/tfs/farazaman/contractor/ContractorFlowTest.kt` اضافه و Debug gates را اجرا کن
- [ ] T048 [US2] Contractor Architecture Review را با بررسی تفکیک Design System Components از Feature Shared Components در `specs/004-compose-ui-migration/reviews/contractor-architecture-gate.md` ثبت و فقط در صورت قبولی، Support/Supervisor را unblock کن

## Phase 5: User Story 3 — Support/List Screens (P2)

**Goal**: pagination و business→semantic mapping در فهرست‌ها.  
**Independent Test**: notification/profile در موفق/خالی/error/refresh/page-next Behavioral Parity داشته باشند.

- [ ] T049 [P] [US3] notification/profile reducer tests را در `app/src/test/java/Ir/co/tfs/farazaman/presentation/viewmodel/SupportViewModelTest.kt` بنویس
- [ ] T050 [US3] business→semantic mapping اعلان و Screen را در `app/src/main/java/Ir/co/tfs/farazaman/presentation/compose/NotificationsScreen.kt` پیاده کن و `NotificationCard` را فقط پس از اثبات تکرار به‌عنوان Feature Shared Component در package همان Feature استخراج کن
- [ ] T051 [US3] `FirebaseNotificationsFragment.kt` را container کن و Index/Visit/Delete/unread/FCM/SignalR behavior را حفظ کن در `app/src/main/java/Ir/co/tfs/farazaman/activity/FirebaseNotificationsFragment.kt`
- [ ] T052 [US3] Profile Screen را با Fz policy در `app/src/main/java/Ir/co/tfs/farazaman/presentation/compose/SupervisorProfileScreen.kt` پیاده و Activity را container کن
- [ ] T053 [P] [US3] component/screen previews را در `app/src/debug/java/Ir/co/tfs/farazaman/presentation/compose/SupportScreenPreviews.kt` اضافه کن
- [ ] T054 [US3] UI tests را در `app/src/androidTest/java/Ir/co/tfs/farazaman/support/SupportScreensTest.kt` اضافه و Debug gates را اجرا کن

## Phase 6: User Story 4 — Supervisor Shell (P2)

**Goal**: responsive Fz shell با glass menu و scroll insets.  
**Independent Test**: work area/home/today/history در RTL، theme live و device matrix بدون content overlap.

- [ ] T055 [P] [US4] supervisor reducer tests را در `app/src/test/java/Ir/co/tfs/farazaman/presentation/viewmodel/SupervisorShellViewModelTest.kt` بنویس
- [ ] T056 [US4] Work Area Screen را در `app/src/main/java/Ir/co/tfs/farazaman/presentation/compose/SupervisorWorkAreaScreen.kt` با Design System پیاده کن
- [ ] T057 [US4] feature-specific glass navigation روی FzScaffold را در `app/src/main/java/Ir/co/tfs/farazaman/presentation/compose/SupervisorScaffold.kt` پیاده و `NewSupervisorActivity.kt` را container کن
- [ ] T058 [US4] Home و status mapping محدوده را در `app/src/main/java/Ir/co/tfs/farazaman/presentation/compose/SupervisorHomeScreen.kt` پیاده کن
- [ ] T059 [US4] Today/History را در `app/src/main/java/Ir/co/tfs/farazaman/presentation/compose/SupervisorTodayScreen.kt` و `SupervisorHistoryScreen.kt` پیاده کن
- [ ] T060 [P] [US4] preview matrix small/normal/large/tablet را در `app/src/debug/java/Ir/co/tfs/farazaman/presentation/compose/SupervisorScreenPreviews.kt` اضافه کن
- [ ] T061 [US4] scroll/glass-menu/theme/RTL UI tests را در `app/src/androidTest/java/Ir/co/tfs/farazaman/supervisor/SupervisorShellTest.kt` اضافه و Debug gates را اجرا کن

## Phase 7: User Story 5 — Forms / Map / Camera / GPS (P3)

**Goal**: system-critical flows با lifecycle ownership.  
**Independent Test**: permission/cancel/recreation/network/lifecycle و 20-cycle cleanup قبول شوند.

- [ ] T062 [US5] system flow baseline و restoration policy را در `specs/004-compose-ui-migration/baselines/system-flows.md` تکمیل کن
- [ ] T063 [US5] violation forms را با State/Event و Fz form components در `app/src/main/java/Ir/co/tfs/farazaman/presentation/compose/ViolationFormScreen.kt` منتقل و camera coordinator را بیرون Screen حفظ کن
- [ ] T064 [US5] MapLibre host lifecycle-aware را در `app/src/main/java/Ir/co/tfs/farazaman/map/ComposeMapHost.kt` ساخته و `DisplayTrackMap.kt` و `SubmitViolationActivity.kt` را تدریجی متصل کن
- [ ] T065 [US5] Driver controls را در `app/src/main/java/Ir/co/tfs/farazaman/presentation/compose/DriverScreen.kt` با GPS events بیرونی مهاجرت بده
- [ ] T066 [US5] track/detail/waypoint/export/upload Screenها را زیر `app/src/main/java/Ir/co/tfs/farazaman/presentation/compose/track/` منتقل و file/ContentProvider work را بیرون UI نگه دار
- [ ] T067 [P] [US5] permission/camera/location/map cleanup و submit dedup tests را زیر `app/src/test/java/Ir/co/tfs/farazaman/presentation/` اضافه کن
- [ ] T068 [US5] 20-cycle instrumentation test را در `app/src/androidTest/java/Ir/co/tfs/farazaman/system/SystemFlowsLifecycleTest.kt` اضافه کن
- [ ] T069 [US5] Debug/Release gates و ARM smoke را اجرا و نتایج را در `specs/004-compose-ui-migration/migration-inventory.md` ثبت کن

## Phase 8: User Story 6 — Legacy Cleanup (P3)

**Goal**: حذف فقط legacy پذیرفته‌شده.

- [ ] T070 [US6] DoD هر مقصد را با `specs/004-compose-ui-migration/checklists/screen-parity-template.md` تکمیل و status inventory را به ParityReview/Migrated تغییر بده
- [ ] T071 [US6] با reference audit فقط layout/binding/Material bridge بلااستفاده را از `app/src/main/res/layout/` و source متناظر حذف کن
- [ ] T072 [US6] Manifest/navigation/back-stack را در `app/src/main/AndroidManifest.xml` و containerهای `app/src/main/java/Ir/co/tfs/farazaman/activity/` اعتبارسنجی کن
- [ ] T073 [P] [US6] architecture locations را در `docs/PROJECT_CONTEXT.md` و `docs/CODEMAP.md` به‌روز کن
- [ ] T074 [US6] تمام unit/UI/lintDebug/assembleDebug/lintRelease/assembleRelease gates را اجرا و نتیجه نهایی را در `specs/004-compose-ui-migration/migration-inventory.md` ثبت کن

## Dependencies & Execution Order

- Phase 1 → Phase 2 → Phase 3 → Contractor MVP.
- T009 پس از token definitions T010–T017 نهایی می‌شود؛ componentها پس از Theme API تثبیت می‌شوند.
- T048 gate blocking برای Phase 5 و 6 است.
- Phase 7 پس از تثبیت route/effect و component policy انجام می‌شود.
- cleanup هر مقصد فقط پس از DoD همان مقصد؛ cleanup نهایی بعد از Phase 7.

## Parallel Opportunities

- auditهای T002–T004، token files T010–T016، component files T018–T023 و Preview/testهای `[P]` روی فایل‌های جدا قابل اجرا هستند.
- Feature Screenهای یک دسته پس از تثبیت shared scaffold می‌توانند موازی باشند.
- Theme file و token APIها هم‌زمان توسط چند تغییر ویرایش نشوند.

## Implementation Strategy

1. **Foundation release**: T001–T037؛ بدون migration گسترده production.
2. **MVP release**: T038–T048؛ Contractor و architecture gate.
3. **Incremental releases**: Support سپس Supervisor.
4. **Sensitive releases**: forms/map/camera/GPS/track به برش‌های کوچک.
5. **Cleanup**: فقط مقصدهای دارای DoD کامل.

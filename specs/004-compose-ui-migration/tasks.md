# Tasks: Material 3 Design System و مهاجرت تدریجی Compose

**Input**: اسناد `specs/004-compose-ui-migration/`  
**Rule**: هیچ production Screen پیش از تکمیل Foundation و هیچ Support/Supervisor Screen پیش از Contractor Architecture Gate مهاجرت نمی‌شود.

## Phase 1: Inventory / Baseline

- [X] T001 تمام مقصدهای user-facing در CODEMAP و Manifest را با Activity/Fragment/layout، owner، role و risk در `specs/004-compose-ui-migration/migration-inventory.md` ثبت کن
- [X] T002 [P] تمام Composeهای موجود شامل `RoleSelectionScreen`, `AppUpdateUi`, `SplashScreen` و `FaraZamanTheme` را از نظر State/Event، Material imports، hard-coded styling و Preview در `specs/004-compose-ui-migration/baselines/existing-compose-audit.md` ممیزی کن
- [X] T003 [P] baseline رفتاری و Visual Intent صفحات Contractor را در `specs/004-compose-ui-migration/baselines/contractor.md` ثبت کن
- [X] T004 [P] baseline Support/Supervisor/System flows را در `specs/004-compose-ui-migration/baselines/remaining-screens.md` ثبت کن
- [X] T005 Material View/Compose bridge، removal condition، coexistence و rollback هر مقصد را در `specs/004-compose-ui-migration/migration-inventory.md` تکمیل کن

## Phase 2: User Story 1 — Material 3 + Farazaman Design System Foundation (P1)

**Goal**: ایجاد source واحد Theme/tokens و حداقل Fz components لازم برای Contractor/shell.  
**Independent Test**: کاتالوگ Foundation در matrix کامل رندر و تغییر palette/shape بدون تغییر Feature منعکس شود.

### Audit and tests first

- [X] T006 [US1] compatibility spike ابزار Roborazzi/Robolectric Native Graphics را روی Kotlin 1.9.22، AGP 8.4.2، Compiler 1.5.10 و BOM 2024.02.02 اجرا، نسخه سازگار را pin و plugin/test dependencies و taskهای record/verify را در `build.gradle` و `app/build.gradle` تنظیم و نتیجه را در `specs/004-compose-ui-migration/baselines/material3-dependency-audit.md` ثبت کن
- [X] T007 [P] [US1] تست dependency direction و ممنوعیت business imports در design package را در `app/src/test/java/Ir/co/tfs/farazaman/presentation/design/DesignSystemArchitectureTest.kt` بنویس
- [X] T008 [P] [US1] تست semantic variant fallback، عدم نشت raw styling، touch target حداقل 48dp و contrast حداقل 4.5:1 متن عادی/3:1 متن بزرگ را در `app/src/test/java/Ir/co/tfs/farazaman/presentation/design/DesignTokenContractTest.kt` بنویس

### Theme and tokens

- [X] T009 [US1] `FaraZamanTheme` موجود را بدون source موازی از `app/src/main/java/Ir/co/tfs/farazaman/presentation/compose/FaraZamanTheme.kt` به `app/src/main/java/Ir/co/tfs/farazaman/presentation/design/theme/FaraZamanTheme.kt` evolve کن و call-site compatibility را حفظ کن
- [X] T010 [P] [US1] Material light/dark ColorScheme و semantic success/warning/info colors را در `app/src/main/java/Ir/co/tfs/farazaman/presentation/design/theme/Color.kt` تعریف کن
- [X] T011 [P] [US1] Persian semantic typography را در `app/src/main/java/Ir/co/tfs/farazaman/presentation/design/theme/Typography.kt` تعریف کن
- [X] T012 [P] [US1] semantic shapes را در `app/src/main/java/Ir/co/tfs/farazaman/presentation/design/theme/Shapes.kt` تعریف کن
- [X] T013 [P] [US1] scale معنایی spacing را در `app/src/main/java/Ir/co/tfs/farazaman/presentation/design/theme/Spacing.kt` تعریف کن
- [X] T014 [P] [US1] elevation roles را در `app/src/main/java/Ir/co/tfs/farazaman/presentation/design/theme/Elevation.kt` تعریف کن
- [X] T015 [P] [US1] motion tokens و reduced-motion policy را در `app/src/main/java/Ir/co/tfs/farazaman/presentation/design/theme/Motion.kt` تعریف کن
- [X] T016 [P] [US1] touch/icon/button/card sizes را در `app/src/main/java/Ir/co/tfs/farazaman/presentation/design/theme/ComponentSizes.kt` تعریف کن
- [X] T017 [US1] ThemeMode/ThemeConfiguration قابل جایگزینی و Dynamic Color پیش‌فرض خاموش را در `app/src/main/java/Ir/co/tfs/farazaman/presentation/design/theme/ThemeMode.kt` متصل کن

### Design System Components — created intentionally before production Screens

- [X] T018 [P] [US1] `FzButton` با Primary/Secondary/Danger/Ghost و loading/disabled semantics را در `app/src/main/java/Ir/co/tfs/farazaman/presentation/design/components/FzButton.kt` بساز
- [X] T019 [P] [US1] `FzCard` با variantهای اثبات‌شده Contractor/shell را در `app/src/main/java/Ir/co/tfs/farazaman/presentation/design/components/FzCard.kt` بساز
- [X] T020 [P] [US1] `FzTextField` با normal/error/disabled و error announcement را در `app/src/main/java/Ir/co/tfs/farazaman/presentation/design/components/FzTextField.kt` بساز
- [X] T021 [P] [US1] `FzStatusChip` با Default/Info/Success/Warning/Error را در `app/src/main/java/Ir/co/tfs/farazaman/presentation/design/components/FzStatusChip.kt` بساز
- [X] T022 [US1] `FzScaffold` را با content insets، RTL و shell slots لازم در `app/src/main/java/Ir/co/tfs/farazaman/presentation/design/components/FzScaffold.kt` بساز
- [X] T023 [P] [US1] `FzLoading`, `FzEmptyState`, `FzErrorState` را در `app/src/main/java/Ir/co/tfs/farazaman/presentation/design/components/FzFeedbackStates.kt` بساز
- [X] T024 [US1] نیاز فوری `FzBadge/FzDialog/FzTopAppBar` را از audit ارزیابی و فقط componentهای اثبات‌شده را در `app/src/main/java/Ir/co/tfs/farazaman/presentation/design/components/` اضافه یا تصمیم عدم ایجاد را در `specs/004-compose-ui-migration/design-system.md` ثبت کن

`ViolationCard`, `NotificationCard`, `ContractorHeader` و سایر Feature Shared Components در این Phase ساخته نمی‌شوند؛ آن‌ها فقط پس از اثبات تکرار داخل package Feature ایجاد می‌شوند.

### Preview and foundation gates

- [X] T025 [US1] MultiPreview مشترک Persian RTL/light/dark/large-font/small-normal-phone را در `app/src/debug/java/Ir/co/tfs/farazaman/presentation/design/preview/FarazamanPreviews.kt` ایجاد کن
- [X] T026 [P] [US1] catalog رنگ، typography، shapes و spacing را در `app/src/debug/java/Ir/co/tfs/farazaman/presentation/design/preview/DesignTokenPreviews.kt` بساز
- [X] T027 [P] [US1] catalog تمام component variants/states را در `app/src/debug/java/Ir/co/tfs/farazaman/presentation/design/preview/DesignSystemPreviews.kt` بساز
- [X] T028 [US1] Roborazzi golden matrix برای RTL/light/dark/font-scale/small-normal phone و shell large/tablet را با viewport قطعی در `app/src/test/java/Ir/co/tfs/farazaman/presentation/design/DesignSystemScreenshotTest.kt` اضافه کن
- [X] T029 [US1] آزمون تعویض palette و base Button/Card shape بدون Feature source change را همراه golden verification در `app/src/test/java/Ir/co/tfs/farazaman/presentation/design/ThemeReplaceabilityTest.kt` اضافه کن
- [X] T030 [US1] `recordRoborazziDebug` و `verifyRoborazziDebug` را اجرا، goldenهای versioned را ثبت، checklist اجرایی Foundation را در `specs/004-compose-ui-migration/implementation-checklists/design-system-checklist.md` تکمیل و نتایج را در `specs/004-compose-ui-migration/migration-inventory.md` ثبت کن

## Phase 3: Shared State/Event Architecture Foundation

**CRITICAL**: Phase 2 و این Phase پیش‌نیاز Contractor هستند.

- [X] T031 قرارداد State/Event/Effect و pure Screen را در `app/src/main/java/Ir/co/tfs/farazaman/presentation/compose/UiContract.kt` پیاده کن
- [X] T032 [P] stateهای مشترک loading/empty/error و semantics keys را در `app/src/main/java/Ir/co/tfs/farazaman/presentation/compose/CommonUiState.kt` تعریف کن
- [X] T033 [P] fixture/providerهای Screen state را در `app/src/debug/java/Ir/co/tfs/farazaman/presentation/design/preview/PreviewFixtures.kt` اضافه کن
- [X] T034 lifecycle-aware route/effect pattern را در `app/src/main/java/Ir/co/tfs/farazaman/presentation/compose/RouteEffectHandler.kt` تعریف و contract آن را مستند کن
- [X] T035 معماری Screen را در `app/src/test/java/Ir/co/tfs/farazaman/presentation/compose/ComposeScreenArchitectureTest.kt` برای network/storage/Intent/system API، Material 2 و Experimental imports enforce کن
- [X] T036 static scan کم‌نویز hard-coded Color/fontSize/reusable shape/direct styled Material را با allowlist مستند در `app/src/test/java/Ir/co/tfs/farazaman/presentation/compose/ComposeDesignDriftTest.kt` اضافه کن
- [X] T037 Composeهای موجود را بدون rewrite غیرضروری با Theme واحد و contract جدید سازگار، regression tests را در `app/src/test/java/Ir/co/tfs/farazaman/presentation/compose/ExistingComposeContractTest.kt` و Roborazzi goldens آن‌ها را در `app/src/test/java/Ir/co/tfs/farazaman/presentation/compose/ExistingComposeScreenshotTest.kt` اضافه کن

## Phase 4: User Story 2 — Contractor MVP (P1)

**Goal**: اولین مصرف production از State/Event و Design System.  
**Independent Test**: جریان Contractor با Behavioral Parity، Visual Intent ثبت‌شده، Preview/accessibility matrix و بدون styling موازی قبول شود.

- [X] T038 [P] [US2] reducer tests انتخاب محدوده را در `app/src/test/java/Ir/co/tfs/farazaman/presentation/viewmodel/ContractorWorkAreaViewModelTest.kt` بنویس
- [X] T039 [P] [US2] reducer tests عملیات/اقدام را در `app/src/test/java/Ir/co/tfs/farazaman/presentation/viewmodel/ContractorOperationsViewModelTest.kt` بنویس
- [X] T040 [P] [US2] reducer tests اصلاحیه/مکان را در `app/src/test/java/Ir/co/tfs/farazaman/presentation/viewmodel/ContractorCorrectionsViewModelTest.kt` بنویس
- [X] T041 [US2] State/Event/Route/Screen انتخاب محدوده را در `app/src/main/java/Ir/co/tfs/farazaman/presentation/compose/ContractorWorkAreaScreen.kt` پیاده و `app/src/main/java/Ir/co/tfs/farazaman/activity/ContractorWorkAreaSelectionActivity.kt` را container کن
- [X] T042 [US2] عملیات پیمانکار را در `app/src/main/java/Ir/co/tfs/farazaman/presentation/compose/ContractorOperationsScreen.kt` پیاده و `app/src/main/java/Ir/co/tfs/farazaman/activity/ContractorOperationsActivity.kt` را container کن
- [X] T043 [US2] فرم اقدام را با FzTextField/Button/Card و camera effect بیرونی در `app/src/main/java/Ir/co/tfs/farazaman/presentation/compose/ContractorActionScreen.kt` و `app/src/main/java/Ir/co/tfs/farazaman/activity/ContractorActionActivity.kt` پیاده کن
- [X] T044 [US2] feature status mapping و فهرست اصلاحیه را در `app/src/main/java/Ir/co/tfs/farazaman/presentation/compose/ContractorCorrectionsScreen.kt` پیاده کن؛ `ContractorHeader` یا card مشترک را فقط در صورت اثبات تکرار به‌عنوان Feature Shared Component در همین package استخراج و container موجود را متصل کن
- [X] T045 [US2] read-only location را با lifecycle map bridge در `app/src/main/java/Ir/co/tfs/farazaman/presentation/compose/ContractorViolationLocationScreen.kt` و Activity موجود پیاده کن
- [X] T046 [P] [US2] Feature Component و Screen preview matrix پیمانکار را در `app/src/debug/java/Ir/co/tfs/farazaman/presentation/compose/ContractorScreenPreviews.kt` تکمیل کن
- [X] T047 [US2] journey/accessibility tests را در `app/src/androidTest/java/Ir/co/tfs/farazaman/contractor/ContractorFlowTest.kt` و Roborazzi goldens تمام stateها را در `app/src/test/java/Ir/co/tfs/farazaman/contractor/ContractorScreenshotTest.kt` اضافه و Debug/screenshot gates را اجرا کن
- [X] T048 [US2] Contractor Architecture Review را با بررسی تفکیک Design System Components از Feature Shared Components در `specs/004-compose-ui-migration/reviews/contractor-architecture-gate.md` ثبت و فقط در صورت قبولی، Support/Supervisor را unblock کن

## Phase 5: User Story 3 — Support/List Screens (P2)

**Goal**: pagination و business→semantic mapping در فهرست‌ها.  
**Independent Test**: notification/profile در موفق/خالی/error/refresh/page-next Behavioral Parity داشته باشند.

- [X] T049 [P] [US3] notification/profile reducer tests را در `app/src/test/java/Ir/co/tfs/farazaman/presentation/viewmodel/SupportViewModelTest.kt` بنویس
- [X] T050 [US3] business→semantic mapping اعلان و Screen را در `app/src/main/java/Ir/co/tfs/farazaman/presentation/compose/NotificationsScreen.kt` پیاده کن و `NotificationCard` را فقط پس از اثبات تکرار به‌عنوان Feature Shared Component در package همان Feature استخراج کن
- [X] T051 [US3] `FirebaseNotificationsFragment.kt` را container کن و Index/Visit/Delete/unread/FCM/SignalR behavior را حفظ کن در `app/src/main/java/Ir/co/tfs/farazaman/activity/FirebaseNotificationsFragment.kt`
- [X] T052 [US3] Profile Screen را با Fz policy در `app/src/main/java/Ir/co/tfs/farazaman/presentation/compose/SupervisorProfileScreen.kt` پیاده و Activity را container کن
- [X] T053 [P] [US3] component/screen previews را در `app/src/debug/java/Ir/co/tfs/farazaman/presentation/compose/SupportScreenPreviews.kt` اضافه کن
- [X] T054 [US3] UI tests را در `app/src/androidTest/java/Ir/co/tfs/farazaman/support/SupportScreensTest.kt` و Roborazzi goldens را در `app/src/test/java/Ir/co/tfs/farazaman/support/SupportScreensScreenshotTest.kt` اضافه و Debug/screenshot gates را اجرا کن

## Phase 6: User Story 4 — Supervisor Shell (P2)

**Goal**: responsive Fz shell با glass menu و scroll insets.  
**Independent Test**: work area/home/today/history در RTL، theme live و device matrix بدون content overlap.

- [X] T055 [P] [US4] supervisor reducer tests را در `app/src/test/java/Ir/co/tfs/farazaman/presentation/viewmodel/SupervisorShellViewModelTest.kt` بنویس
- [X] T056 [US4] Work Area Screen را در `app/src/main/java/Ir/co/tfs/farazaman/presentation/compose/SupervisorWorkAreaScreen.kt` با Design System پیاده کن
- [X] T057 [US4] feature-specific glass navigation روی FzScaffold را در `app/src/main/java/Ir/co/tfs/farazaman/presentation/compose/SupervisorScaffold.kt` پیاده و `NewSupervisorActivity.kt` را container کن
- [X] T058 [US4] Home و status mapping محدوده را در `app/src/main/java/Ir/co/tfs/farazaman/presentation/compose/SupervisorHomeScreen.kt` پیاده کن
- [X] T059 [US4] Today/History را در `app/src/main/java/Ir/co/tfs/farazaman/presentation/compose/SupervisorTodayScreen.kt` و `SupervisorHistoryScreen.kt` پیاده کن
- [X] T060 [P] [US4] preview matrix small/normal/large/tablet را در `app/src/debug/java/Ir/co/tfs/farazaman/presentation/compose/SupervisorScreenPreviews.kt` اضافه کن
- [X] T061 [US4] scroll/glass-menu/theme/RTL UI tests را در `app/src/androidTest/java/Ir/co/tfs/farazaman/supervisor/SupervisorShellTest.kt` و responsive Roborazzi goldens را در `app/src/test/java/Ir/co/tfs/farazaman/supervisor/SupervisorShellScreenshotTest.kt` اضافه و Debug/screenshot gates را اجرا کن

## Phase 7: User Story 5 — Forms / Map / Camera / GPS (P3)

**Goal**: system-critical flows با lifecycle ownership.  
**Independent Test**: permission/cancel/recreation/network/lifecycle و 20-cycle cleanup قبول شوند.

- [X] T062 [US5] system flow baseline و restoration policy را در `specs/004-compose-ui-migration/baselines/system-flows.md` تکمیل کن
- [X] T063 [US5] مدل و storage محلی flagهای Map/Camera/GPS/Track را با پیش‌فرض Compose، owner و removal metadata در `app/src/main/java/Ir/co/tfs/farazaman/util/UiMigrationFlagManager.kt` پیاده کن
- [X] T064 [US5] diagnostics/support مخفی را در `app/src/main/java/Ir/co/tfs/farazaman/activity/UiMigrationDiagnosticsActivity.kt` پیاده و در `app/src/main/AndroidManifest.xml` با `exported=false` ثبت کن؛ دسترسی production را فقط برای نقش تأییدشده `superAdministrator` مجاز کن و control را از UI عادی حذف نگه دار
- [X] T065 [US5] تست پیش‌فرض Compose، persistence، `exported=false`، مجوز superAdministrator و رد دسترسی کاربر عادی را در `app/src/test/java/Ir/co/tfs/farazaman/util/UiMigrationFlagManagerTest.kt` و `app/src/androidTest/java/Ir/co/tfs/farazaman/system/UiMigrationDiagnosticsAccessTest.kt` اضافه کن
- [X] T066 [US5] violation forms را با State/Event و Fz form components در `app/src/main/java/Ir/co/tfs/farazaman/presentation/compose/ViolationFormScreen.kt` منتقل، camera coordinator را بیرون Screen حفظ و route را به Camera migration flag متصل کن
- [X] T067 [US5] MapLibre host lifecycle-aware را در `app/src/main/java/Ir/co/tfs/farazaman/map/ComposeMapHost.kt` ساخته، renderer fake قطعی screenshot را فراهم و `DisplayTrackMap.kt` و `SubmitViolationActivity.kt` را از طریق Map migration flag متصل کن
- [X] T068 [US5] Driver controls را در `app/src/main/java/Ir/co/tfs/farazaman/presentation/compose/DriverScreen.kt` با GPS events بیرونی مهاجرت و route را به GPS migration flag متصل کن
- [X] T069 [US5] track/detail/waypoint/export/upload Screenها را زیر `app/src/main/java/Ir/co/tfs/farazaman/presentation/compose/track/` منتقل، file/ContentProvider work را بیرون UI نگه دار و route را به Track migration flag متصل کن
- [X] T070 [P] [US5] permission/camera/location/map cleanup و submit dedup tests را زیر `app/src/test/java/Ir/co/tfs/farazaman/presentation/` اضافه کن
- [X] T071 [US5] Roborazzi goldens همه system Screenها را با camera/location/map/track fakeهای قطعی در `app/src/test/java/Ir/co/tfs/farazaman/system/SystemFlowsScreenshotTest.kt` اضافه کن
- [X] T072 [US5] 20-cycle instrumentation test را در `app/src/androidTest/java/Ir/co/tfs/farazaman/system/SystemFlowsLifecycleTest.kt` اضافه و هر دو مسیر Compose/Legacy flag را smoke کن
- [ ] T073 [US5] Debug/Release/screenshot gates و ARM smoke واقعی را اجرا و نتایج را در `specs/004-compose-ui-migration/migration-inventory.md` ثبت کن

## Phase 8: User Story 6 — Legacy Cleanup (P3)

**Goal**: حذف فقط legacy پذیرفته‌شده.

- [X] T074 [US6] DoD هر مقصد شامل accessibility و golden verification را با یک نسخه مستقل از `specs/004-compose-ui-migration/implementation-checklists/screen-parity-template.md` تکمیل و status inventory را به ParityReview/Migrated تغییر بده
- [X] T075 [US6] با reference audit فقط layout/binding/Material bridge بلااستفاده را از `app/src/main/res/layout/` و source متناظر حذف و flag هر system Screen را فقط پس از پایان rollout آن حذف کن
- [X] T076 [US6] Manifest/navigation/back-stack و diagnostics exposure را در `app/src/main/AndroidManifest.xml` و containerهای `app/src/main/java/Ir/co/tfs/farazaman/activity/` اعتبارسنجی کن
- [X] T077 [P] [US6] architecture locations و screenshot/flag commands را در `docs/PROJECT_CONTEXT.md` و `docs/CODEMAP.md` به‌روز کن
- [ ] T078 [US6] تمام unit/UI/Roborazzi/lintDebug/assembleDebug/lintRelease/assembleRelease gates را اجرا و نتیجه نهایی را در `specs/004-compose-ui-migration/migration-inventory.md` ثبت کن

## Dependencies & Execution Order

- Phase 1 → Phase 2 → Phase 3 → Contractor MVP.
- T009 پس از token definitions T010–T017 نهایی می‌شود؛ componentها پس از Theme API تثبیت می‌شوند.
- T048 gate blocking برای Phase 5 و 6 است.
- Phase 7 پس از تثبیت route/effect و component policy انجام می‌شود.
- T063–T065 پیش‌نیاز اتصال هر system-critical route در T066–T069 هستند.
- cleanup هر مقصد فقط پس از DoD همان مقصد؛ flag system-critical فقط پس از پایان rollout حذف می‌شود.

## Parallel Opportunities

- auditهای T002–T004، token files T010–T016، component files T018–T023 و Preview/testهای `[P]` روی فایل‌های جدا قابل اجرا هستند.
- Feature Screenهای یک دسته پس از تثبیت shared scaffold می‌توانند موازی باشند.
- Theme file و token APIها هم‌زمان توسط چند تغییر ویرایش نشوند.

## Implementation Strategy

1. **Foundation release**: T001–T037؛ شامل Roborazzi و بدون migration گسترده production.
2. **MVP release**: T038–T048؛ Contractor، goldens و architecture gate.
3. **Incremental releases**: Support سپس Supervisor.
4. **Sensitive releases**: forms/map/camera/GPS/track به برش‌های کوچک.
5. **Cleanup**: T074–T078 فقط برای مقصدهای دارای DoD و rollout کامل.

## Phase 9: Convergence

- [X] T091 [US5] قرارداد جای‌گذاری اکشن‌ها، دکمه بازگشت RTL، دکمه آیکونی، نوار اقدام پایین و کنترل‌های نقشه را در `:core:ui` پیاده‌سازی کن
- [X] T092 [US5] صفحه‌های Role Selection، Track Map و Violation Map را به اکشن‌های معنایی مشترک مهاجرت کن
- [X] T093 [US5] تست معماری برای جلوگیری از بازگشت دکمه‌های خام Material و کنترل‌های ناهمگون نقشه اضافه کن
- [X] T094 [US4] اکشن‌های اعلان و پروفایل Home/Today/History/Role Selection را به آیکون مشترک با badge تبدیل کن
- [X] T095 [US4] History را روی `FzPageTemplate` مشترک منتقل و چیدمان آن را با Home/Today همسان کن
- [X] T096 [US4] مصرف‌شدن gesture دکمه توقف بازدید توسط Button داخلی را اصلاح و قرارداد آن را تست کن

- [X] T079 [P] Login، انتخاب شهر و OIDC bottom sheet را به pure State/Event Compose Screen و Route تبدیل کن و XMLهای متناظر را پس از parity حذف کن per FR-001, FR-009, SC-001
- [X] T080 [P] Intro، About، developer warning و loading/dialogهای user-facing را به Compose و `FzPageTemplate` منتقل و layoutهای بدون مصرف را حذف کن per FR-001, FR-006, FR-013
- [X] T081 Supervisor host و سه مقصد Home/Today/History را از Fragment/XML bridge به Compose shell واقعی تبدیل کن و bottom navigation را فقط در همین سه مقصد حفظ کن per US4/AC1, FR-001
- [X] T082 [P] Actions/Notifications parent، tab chrome و action feed را به Compose تبدیل کن؛ محتوای هر tab نباید header تکراری بسازد per US3/AC1, FR-006
- [X] T083 Supervisor و Contractor work-area/profile/operations مقصدها را به Routeهای Compose بدون ViewBinding یا layout overlay تبدیل و page chrome مشترک را اعمال کن per FR-006, US2/AC1, US4/AC1
- [X] T084 DriverActivity را از XML overlay به Container تک‌Composition تبدیل کن و تمام refreshها را فقط از طریق State انجام بده per FR-001, FR-009, US5/AC1
- [X] T085 [P] فرم‌های عمومی/تعهدات ثبت تخلف و image/detail bottom sheetها را به Compose تبدیل و camera/location/submit behavior را حفظ کن per FR-001, FR-009, US5/AC1
- [X] T086 [P] DisplayTrackMap و SubmitViolation map را با Compose host کامل، lifecycle ownership و کنترل‌های Compose جایگزین کن per FR-001, FR-009, US5/AC1
- [X] T087 TrackManager/TrackDetail/Waypoint/TrackLogger/DisplayTrack/Upload را به Routeهای تک‌Composition تبدیل کن و setContent تکراری یا legacy layout را حذف کن per FR-001, FR-009, US5/AC1
- [X] T088 [P] همه BottomSheet، dialog، adapter row و includeهای user-facing باقیمانده را پس از parent screen migration به Compose component تبدیل کن per FR-001, FR-006
- [X] T089 reference audit نهایی انجام بده، تمام layout/bindingهای واقعاً بدون مصرف را حذف کن و صفر بودن مصرف production `setContentView`/ViewBinding برای Screenها را enforce کن per SC-001, T075
- [ ] T090 برای تمام مقصدهای Phase 9 Preview، reducer/UI test، Roborazzi، RTL/dark/font-scale و navigation smoke اضافه و Debug/Release/ARM gates را اجرا کن per FR-014, SC-002, SC-004, T073, T078 (missing)

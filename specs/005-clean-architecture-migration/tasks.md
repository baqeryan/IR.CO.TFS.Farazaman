---

description: "Dependency-ordered tasks for the incremental multi-module Clean Architecture migration"
---

# Tasks: مهاجرت تدریجی Clean Architecture چندماژوله

**Input**: Design documents from `/specs/005-clean-architecture-migration/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/, quickstart.md

**Tests**: الزامی‌اند؛ spec برای هر برش characterization، architecture، contract/unit و smoke test درخواست کرده است.

**Organization**: تسک‌ها بر اساس vertical sliceهای قابل انتشار مرتب شده‌اند. هر Story باید با fakeهای core مستقل تست شود، حتی اگر rollout تولیدی به‌ترتیب اولویت انجام شود.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: روی فایل‌های جدا و بدون dependency حل‌نشده قابل انجام موازی است.
- **[Story]**: نگاشت به User Story متناظر در spec.
- همه تسک‌ها مسیر فایل دقیق دارند.

## Phase 1: Setup — Baseline و تصویب مهاجرت

**Purpose**: ثبت وضعیت واقعی و ایجاد guardrail پیش از تغییر Gradle graph

- [X] T001 ثبت approval، owner، rollout و rollback معماری در `specs/005-clean-architecture-migration/reviews/architecture-approval.md`
- [X] T002 [P] استخراج baseline ماژول/package/dependencyهای فعلی در `specs/005-clean-architecture-migration/baselines/dependency-inventory.md`
- [X] T003 [P] ثبت Android contractهای ثابت شامل application ID، provider authority، intent actions، redirect URI و service declarations در `specs/005-clean-architecture-migration/baselines/platform-contracts.md`
- [X] T004 [P] ثبت baseline زمان build/startup و اندازه APK Debug/Release در `specs/005-clean-architecture-migration/baselines/build-performance.md`
- [X] T005 [P] ساخت matrix مالکیت lifecycle برای GPS، FCM، SignalR، AppAuth، camera و MapLibre در `specs/005-clean-architecture-migration/baselines/lifecycle-ownership.md`
- [X] T006 افزودن feature directory فعال به `.specify/feature.json` بدون تغییر branch یا فایل‌های feature 004

---

## Phase 2: Foundational — مرزهای مشترک و enforcement

**Purpose**: حداقل زیرساختی که همه vertical sliceها برای جداسازی و تست نیاز دارند

**⚠️ CRITICAL**: implementation هیچ Story قبل از تکمیل این فاز شروع نشود.

- [X] T007 تعریف includeهای اولیه `:core:common`، `:core:model`، `:core:ui` و `:core:testing` در `settings.gradle`
- [X] T008 ایجاد plugin/dependency configuration حداقلی Kotlin/JVM برای `:core:common` در `core/common/build.gradle`
- [X] T009 [P] ایجاد Android library configuration برای `:core:model` در `core/model/build.gradle`
- [X] T010 [P] ایجاد Android/Compose library configuration برای `:core:ui` در `core/ui/build.gradle`
- [X] T011 [P] ایجاد test-fixtures library configuration برای `:core:testing` در `core/testing/build.gradle`
- [X] T012 انتقال/تعریف result، failure و dispatcher abstraction مشترک در `core/common/src/main/java/Ir/co/tfs/farazaman/core/common/Outcome.kt`
- [X] T013 [P] تعریف clock و coroutine dispatcher contracts در `core/common/src/main/java/Ir/co/tfs/farazaman/core/common/RuntimeProviders.kt`
- [X] T014 [P] تعریف مدل‌های مشترک فقط-دامنه‌ای session، role و selected municipality در `core/model/src/main/java/Ir/co/tfs/farazaman/core/model/SessionModels.kt`
- [X] T015 انتقال design-system عمومی و `FaraZamanTheme` بدون business import به `core/ui/src/main/java/Ir/co/tfs/farazaman/presentation/design/theme/FaraZamanTheme.kt`
- [X] T016 تعریف قرارداد عمومی immutable state/event/effect در `core/ui/src/main/java/Ir/co/tfs/farazaman/core/ui/UiContract.kt`
- [X] T017 [P] ساخت fake clock، dispatchers و result fixtures در `core/testing/src/main/java/Ir/co/tfs/farazaman/core/testing/CoreTestFixtures.kt`
- [X] T018 ایجاد architecture test برای ممنوعیت Android/Retrofit/Compose در domain source sets در `app/src/test/java/Ir/co/tfs/farazaman/architecture/DomainPurityTest.kt`
- [X] T019 [P] ایجاد architecture test برای ممنوعیت feature-to-feature و app imports در `app/src/test/java/Ir/co/tfs/farazaman/architecture/ModuleDependencyTest.kt`
- [X] T020 [P] ایجاد architecture test برای ممنوعیت دسترسی Screen/ViewModel به Retrofit، SharedPreferences و ContentResolver در `app/src/test/java/Ir/co/tfs/farazaman/architecture/PresentationBoundaryTest.kt`
- [X] T021 ثبت baseline violationهای موقت با owner و removal story در `specs/005-clean-architecture-migration/baselines/architecture-allowlist.txt`
- [X] T022 اتصال ماژول‌های core به composition root و حفظ source فعلی app در `app/build.gradle`

**Checkpoint**: core contracts compile می‌شوند، app بدون تغییر رفتار build می‌شود و violation جدید architecture fail می‌شود.

---

## Phase 3: User Story 1 — Auth و Role به‌عنوان MVP (Priority: P1) 🎯

**Goal**: ورود OIDC/password، session bootstrap، role routing و logout پشت domain contracts و data adapters مستقل قرار گیرند.

**Independent Test**: `:feature:auth` و `:feature:roles` با fake gateway بدون app اجرا شوند؛ سپس نصب تمیز Debug و Release هر دو login mode، user info/roles، first API و logout retention را پاس کند.

### Tests for User Story 1

- [ ] T023 [P] [US1] نوشتن characterization test برای selected-city/OIDC/password/session bootstrap در `app/src/test/java/Ir/co/tfs/farazaman/auth/LegacyAuthCharacterizationTest.kt`
- [ ] T024 [P] [US1] نوشتن contract test برای جدایی selected-server و central unauthenticated clients در `core/network/src/test/java/Ir/co/tfs/farazaman/core/network/NetworkTrustBoundaryTest.kt`
- [ ] T025 [P] [US1] نوشتن reducer/use-case tests ورود، empty roles، super-admin و logout در `feature/auth/src/test/java/Ir/co/tfs/farazaman/feature/auth/AuthFlowTest.kt`
- [ ] T026 [P] [US1] نوشتن role selection/routing tests در `feature/roles/src/test/java/Ir/co/tfs/farazaman/feature/roles/RoleRoutingTest.kt`

### Implementation for User Story 1

- [ ] T027 [US1] افزودن `:core:network` و `:core:security` و `:feature:auth` و `:feature:roles` به `settings.gradle`
- [ ] T028 [P] [US1] ایجاد Retrofit/OkHttp/Gson Android library module در `core/network/build.gradle`
- [ ] T029 [P] [US1] ایجاد secure-session Android library module در `core/security/build.gradle`
- [ ] T030 [P] [US1] ایجاد Compose/Hilt feature module ورود در `feature/auth/build.gradle`
- [ ] T031 [P] [US1] ایجاد Compose/Hilt feature module نقش‌ها در `feature/roles/build.gradle`
- [ ] T032 [US1] انتقال selected-server Retrofit provider، interceptor و central-client separation به `core/network/src/main/java/Ir/co/tfs/farazaman/core/network/NetworkGraph.kt`
- [ ] T033 [P] [US1] تعریف `SessionRepository`، `Authenticate` و `BootstrapSession` در `feature/auth/src/main/java/Ir/co/tfs/farazaman/feature/auth/domain/AuthContracts.kt`
- [ ] T034 [P] [US1] ساخت adapterهای TokenManager، SecureTokenStorage، AppAuth و logout cleaner در `core/security/src/main/java/Ir/co/tfs/farazaman/core/security/SessionSecurityAdapters.kt`
- [ ] T035 [US1] پیاده‌سازی repository و mapperهای auth روی APIهای فعلی در `feature/auth/src/main/java/Ir/co/tfs/farazaman/feature/auth/data/AuthRepositoryImpl.kt`
- [ ] T036 [US1] انتقال Login/City/OIDC UI به state-event-effect و ViewModel جدید در `feature/auth/src/main/java/Ir/co/tfs/farazaman/feature/auth/presentation/AuthRoute.kt`
- [ ] T037 [P] [US1] تعریف role repository/use cases بدون dependency به auth feature در `feature/roles/src/main/java/Ir/co/tfs/farazaman/feature/roles/domain/RoleContracts.kt`
- [ ] T038 [US1] انتقال role selection/profile state و routing effects به `feature/roles/src/main/java/Ir/co/tfs/farazaman/feature/roles/presentation/RoleSelectionRoute.kt`
- [ ] T039 [US1] wire کردن auth/roles entry points و compatibility adapters در `app/src/main/java/Ir/co/tfs/farazaman/navigation/AuthComposition.kt`
- [ ] T040 [US1] به‌روزرسانی R8 rules برای DTO/generic/Hilt entryهای منتقل‌شده در `app/proguard-rules.pro`
- [ ] T041 [US1] ثبت Debug/Release parity، first authenticated API و rollback نتیجه US1 در `specs/005-clean-architecture-migration/reviews/us1-auth-parity.md`

**Checkpoint**: MVP قابل انتشار است؛ presentation ورود/نقش به transport/storage مستقیم دسترسی ندارد.

---

## Phase 4: User Story 2 — Supervisor Mission vertical slice (Priority: P2)

**Goal**: work area، mission setup، daily plan، rollover، track commands و history از Activity/helperها به domain/data/presentation feature منتقل شوند.

**Independent Test**: feature با fake mission/track repositories مأموریت کامل را اجرا کند و device smoke همان track و mission metadata فعلی را حفظ کند.

### Tests for User Story 2

- [ ] T042 [P] [US2] نوشتن characterization tests create/index/fence و local track coupling در `app/src/test/java/Ir/co/tfs/farazaman/supervisor/SupervisorMissionCharacterizationTest.kt`
- [ ] T043 [P] [US2] نوشتن rollover، stale-date blocking و mission state-machine tests در `feature/supervisor/src/test/java/Ir/co/tfs/farazaman/feature/supervisor/SupervisorMissionUseCaseTest.kt`
- [ ] T044 [P] [US2] نوشتن lifecycle contract test برای start/stop GPS command ownership در `feature/supervisor/src/test/java/Ir/co/tfs/farazaman/feature/supervisor/SupervisorTrackingContractTest.kt`

### Implementation for User Story 2

- [ ] T045 [US2] افزودن `:core:database` و `:feature:supervisor` به `settings.gradle`
- [ ] T046 [P] [US2] ایجاد ContentProvider-backed Android library module در `core/database/build.gradle`
- [ ] T047 [P] [US2] ایجاد Compose/Hilt supervisor feature module در `feature/supervisor/build.gradle`
- [ ] T048 [P] [US2] تعریف مدل‌های domain مأموریت، daily plan، work area و track command در `feature/supervisor/src/main/java/Ir/co/tfs/farazaman/feature/supervisor/domain/SupervisorModels.kt`
- [ ] T049 [US2] تعریف repositoryها و use caseهای create/restore/rollover/end در `feature/supervisor/src/main/java/Ir/co/tfs/farazaman/feature/supervisor/domain/SupervisorUseCases.kt`
- [ ] T050 [US2] ساخت adapter برای `TrackContentProvider`، `DataHelper` و mission preferences بدون schema change در `core/database/src/main/java/Ir/co/tfs/farazaman/core/database/LegacyTrackStore.kt`
- [ ] T051 [US2] انتقال remote mission/daily-plan/fence implementation و DTO mappers به `feature/supervisor/src/main/java/Ir/co/tfs/farazaman/feature/supervisor/data/SupervisorRepositoryImpl.kt`
- [ ] T052 [US2] انتقال shell/work-area/home/today/history به route + ViewModel state/effect در `feature/supervisor/src/main/java/Ir/co/tfs/farazaman/feature/supervisor/presentation/SupervisorRoute.kt`
- [ ] T053 [US2] تبدیل `NewSupervisorActivity` به platform/navigation adapter بدون business rules در `app/src/main/java/Ir/co/tfs/farazaman/activity/NewSupervisorActivity.kt`
- [ ] T054 [US2] wire کردن Hilt bindings و GPS command adapter سرپرست در `app/src/main/java/Ir/co/tfs/farazaman/di/SupervisorCompositionModule.kt`
- [ ] T055 [US2] ثبت database/rollover/lifecycle/device parity و rollback نتیجه US2 در `specs/005-clean-architecture-migration/reviews/us2-supervisor-parity.md`

**Checkpoint**: supervisor slice مستقل است و Activity فقط lifecycle/navigation effect اجرا می‌کند.

---

## Phase 5: User Story 3 — Driver و Track management (Priority: P3)

**Goal**: driver mission، track/waypoint list-detail، export و upload از UI جدا و روی core database/network contracts اجرا شوند.

**Independent Test**: featureها با fake store و فایل موقت track driver را ثبت/نمایش/export/upload کنند؛ role separation و retention روی دیتابیس واقعی تأیید شود.

### Tests for User Story 3

- [ ] T056 [P] [US3] نوشتن driver mission characterization tests در `app/src/test/java/Ir/co/tfs/farazaman/driver/DriverMissionCharacterizationTest.kt`
- [ ] T057 [P] [US3] نوشتن contract tests برای Cursor↔domain mapping و role filtering در `core/database/src/test/java/Ir/co/tfs/farazaman/core/database/LegacyTrackStoreTest.kt`
- [ ] T058 [P] [US3] نوشتن export/upload use-case tests شامل failure/retry/selected URL در `feature/tracking/src/test/java/Ir/co/tfs/farazaman/feature/tracking/TrackTransferUseCaseTest.kt`

### Implementation for User Story 3

- [ ] T059 [US3] افزودن `:feature:driver` و `:feature:tracking` به `settings.gradle`
- [ ] T060 [P] [US3] ایجاد driver feature module در `feature/driver/build.gradle`
- [ ] T061 [P] [US3] ایجاد tracking feature module در `feature/tracking/build.gradle`
- [ ] T062 [P] [US3] تعریف track/point/waypoint/export domain models و repositories در `feature/tracking/src/main/java/Ir/co/tfs/farazaman/feature/tracking/domain/TrackingContracts.kt`
- [ ] T063 [P] [US3] تعریف driver mission commands و state policy در `feature/driver/src/main/java/Ir/co/tfs/farazaman/feature/driver/domain/DriverMissionUseCases.kt`
- [ ] T064 [US3] انتقال track query/detail/edit و waypoint adapters به `feature/tracking/src/main/java/Ir/co/tfs/farazaman/feature/tracking/data/TrackRepositoryImpl.kt`
- [ ] T065 [US3] انتقال GPX/ZIP/file و selected-server upload orchestration به `feature/tracking/src/main/java/Ir/co/tfs/farazaman/feature/tracking/data/TrackTransferRepositoryImpl.kt`
- [ ] T066 [US3] انتقال Driver state/effect و GPS commands به `feature/driver/src/main/java/Ir/co/tfs/farazaman/feature/driver/presentation/DriverRoute.kt`
- [ ] T067 [P] [US3] انتقال track list/detail/waypoint screens و ViewModels به `feature/tracking/src/main/java/Ir/co/tfs/farazaman/feature/tracking/presentation/TrackRoutes.kt`
- [ ] T068 [US3] تبدیل Driver/Track Activities و GPS service calls به app adapters در `app/src/main/java/Ir/co/tfs/farazaman/navigation/TrackingComposition.kt`
- [ ] T069 [US3] ثبت role separation، export/upload، process recreation و rollback نتیجه US3 در `specs/005-clean-architecture-migration/reviews/us3-tracking-parity.md`

**Checkpoint**: UI هیچ Cursor/file/Retrofit access مستقیم ندارد و داده‌های driver/supervisor جدا می‌مانند.

---

## Phase 6: User Story 4 — Map، Location، Camera و Violations (Priority: P4)

**Goal**: system integrations پشت adapterهای lifecycle-aware قرار گیرند و violation/map logic به use case و state/effect منتقل شود.

**Independent Test**: feature violations با fake map/location/camera و سپس device smoke واقعی، validation و metadata و fallback style را تأیید کند.

### Tests for User Story 4

- [ ] T070 [P] [US4] نوشتن violation submission characterization tests در `app/src/test/java/Ir/co/tfs/farazaman/violation/ViolationSubmissionCharacterizationTest.kt`
- [ ] T071 [P] [US4] نوشتن coordinate/metadata/mission-date use-case tests در `feature/violations/src/test/java/Ir/co/tfs/farazaman/feature/violations/SubmitViolationUseCaseTest.kt`
- [ ] T072 [P] [US4] نوشتن lifecycle tests برای map/location/camera adapters در `app/src/androidTest/java/Ir/co/tfs/farazaman/system/SystemAdapterLifecycleTest.kt`

### Implementation for User Story 4

- [ ] T073 [US4] افزودن `:core:location`، `:core:map` و `:feature:violations` به `settings.gradle`
- [ ] T074 [P] [US4] ایجاد location Android library module در `core/location/build.gradle`
- [ ] T075 [P] [US4] ایجاد MapLibre Android library module در `core/map/build.gradle`
- [ ] T076 [P] [US4] ایجاد violations Compose/Hilt module در `feature/violations/build.gradle`
- [ ] T077 [P] [US4] انتقال location policy/acquisition contracts و implementation به `core/location/src/main/java/Ir/co/tfs/farazaman/core/location/LocationGateway.kt`
- [ ] T078 [P] [US4] انتقال Baran style/cache/overlay implementation به `core/map/src/main/java/Ir/co/tfs/farazaman/core/map/MapLibreGateway.kt`
- [ ] T079 [US4] تعریف violation models/repository/validation/submit use cases در `feature/violations/src/main/java/Ir/co/tfs/farazaman/feature/violations/domain/ViolationUseCases.kt`
- [ ] T080 [US4] انتقال DTO/multipart/geo mapping repository به `feature/violations/src/main/java/Ir/co/tfs/farazaman/feature/violations/data/ViolationRepositoryImpl.kt`
- [ ] T081 [US4] انتقال form/map/sheet UI و reducer/effects به `feature/violations/src/main/java/Ir/co/tfs/farazaman/feature/violations/presentation/ViolationRoute.kt`
- [ ] T082 [US4] wire کردن Activity Result camera، permission و MapView lifecycle adapters در `app/src/main/java/Ir/co/tfs/farazaman/navigation/ViolationComposition.kt`
- [ ] T083 [US4] ثبت map fallback، camera cancel، invalid location، leak و rollback نتیجه US4 در `specs/005-clean-architecture-migration/reviews/us4-system-parity.md`

**Checkpoint**: Composable/ViewModel به MapLibre، ActivityResult یا Android location API مستقیم وابسته نیست.

---

## Phase 7: User Story 5 — Notifications، Contractor، Settings و cleanup (Priority: P5)

**Goal**: featureهای باقی‌مانده جدا شوند، app به composition root محدود و legacy فقط با مدرک parity حذف شود.

**Independent Test**: notification/realtime lifecycle، contractor contract guard و settings/profile مستقل تست شوند؛ dependency report violation جدید نداشته باشد.

### Tests for User Story 5

- [ ] T084 [P] [US5] نوشتن FCM/SignalR/unread lifecycle characterization tests در `app/src/test/java/Ir/co/tfs/farazaman/notification/NotificationLifecycleCharacterizationTest.kt`
- [ ] T085 [P] [US5] نوشتن contractor unknown-contract/no-false-success tests در `feature/contractor/src/test/java/Ir/co/tfs/farazaman/feature/contractor/ContractorGatewayContractTest.kt`
- [ ] T086 [P] [US5] نوشتن settings/profile state tests در `feature/settings/src/test/java/Ir/co/tfs/farazaman/feature/settings/SettingsStateTest.kt`

### Implementation for User Story 5

- [ ] T087 [US5] افزودن `:feature:notifications`، `:feature:contractor` و `:feature:settings` به `settings.gradle`
- [ ] T088 [P] [US5] ایجاد notifications feature module در `feature/notifications/build.gradle`
- [ ] T089 [P] [US5] ایجاد contractor feature module در `feature/contractor/build.gradle`
- [ ] T090 [P] [US5] ایجاد settings feature module در `feature/settings/build.gradle`
- [ ] T091 [US5] انتقال notification domain/repository/inbox state به `feature/notifications/src/main/java/Ir/co/tfs/farazaman/feature/notifications/NotificationsFeature.kt`
- [ ] T092 [US5] تبدیل FCM service و SignalR application hooks به adapters قرارداد notification در `app/src/main/java/Ir/co/tfs/farazaman/notification/NotificationPlatformAdapters.kt`
- [ ] T093 [P] [US5] انتقال contractor context، validation و فقط gatewayهای دارای قرارداد به `feature/contractor/src/main/java/Ir/co/tfs/farazaman/feature/contractor/ContractorFeature.kt`
- [ ] T094 [P] [US5] انتقال preferences/about/profile/support state و UI به `feature/settings/src/main/java/Ir/co/tfs/farazaman/feature/settings/SettingsFeature.kt`
- [ ] T095 [US5] متمرکزکردن app navigation و feature route contracts در `app/src/main/java/Ir/co/tfs/farazaman/navigation/AppCompositionRoot.kt`
- [ ] T096 [US5] حذف binding/dependencyهای superseded از composition root پس از پاس parity در `app/src/main/java/Ir/co/tfs/farazaman/di/AppModule.kt`
- [ ] T097 [US5] حذف legacy ownerهای تأییدشده و ثبت نگاشت هر حذف در `specs/005-clean-architecture-migration/reviews/legacy-removal-ledger.md`
- [ ] T098 [US5] ثبت FCM/SignalR/contractor/settings و rollback نتیجه US5 در `specs/005-clean-architecture-migration/reviews/us5-final-parity.md`

**Checkpoint**: app module business rule جدید ندارد؛ feature-to-feature dependency و موفقیت حدسی پیمانکار وجود ندارد.

---

## Phase 8: Polish & Cross-Cutting Validation

**Purpose**: تثبیت Release، مستندات و اندازه‌گیری نهایی بدون refactor خارج scope

- [ ] T099 [P] به‌روزرسانی معماری تأییدشده و feature/module locations در `docs/PROJECT_CONTEXT.md`
- [ ] T100 [P] به‌روزرسانی feature-to-code mapping ماژول‌های جدید در `docs/CODEMAP.md`
- [ ] T101 [P] ثبت R8، build-type و smoke constraints جدید در `docs/RELEASE_NOTES.md`
- [ ] T102 اجرای matrix کامل quickstart و ثبت فرمان/نتیجه/محیط در `specs/005-clean-architecture-migration/reviews/final-validation.md`
- [ ] T103 مقایسه build/startup/APK baseline و ثبت regression/تصمیم در `specs/005-clean-architecture-migration/reviews/performance-comparison.md`
- [ ] T104 audit نهایی secret logging، selected-server routing و central-client isolation در `specs/005-clean-architecture-migration/reviews/security-audit.md`
- [ ] T105 بستن allowlist معماری یا ثبت exception دارای owner/expiry در `specs/005-clean-architecture-migration/baselines/architecture-allowlist.txt`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1** بدون dependency فنی است؛ T001 approval gate پیش از implementation الزامی است.
- **Phase 2** به Phase 1 وابسته و blocker همه Storyهاست.
- **US1** بعد از Foundation اولین increment تولیدی است.
- **US2** و **US3** پس از Foundation با fake session مستقل‌اند، ولی rollout تولیدی آن‌ها پس از US1 انجام شود.
- **US4** برای integration واقعی به contractهای tracking/mission از US2/US3 وابسته است؛ unit work آن زودتر قابل شروع است.
- **US5** برای cleanup نهایی به parity storyهای قبلی وابسته است؛ contractor/settings unit work مستقل است.
- **Phase 8** پس از Storyهای انتخاب‌شده اجرا می‌شود؛ final cleanup به همه آن‌ها نیاز دارد.

### User Story Dependencies

```text
Foundation ──> US1 Auth/Roles ───────────────┐
           ├─> US2 Supervisor ──┐            │
           └─> US3 Driver/Track ├─> US4 ────┼─> US5 cleanup ─> Final
                                └────────────┘
```

- **US1**: مستقل با fake network/security؛ MVP پیشنهادی.
- **US2**: مستقل با fake session/track gateway؛ production wiring از core contracts US1 استفاده می‌کند.
- **US3**: مستقل با fake session/store؛ database adapter foundation را reuse می‌کند.
- **US4**: use caseها مستقل‌اند؛ device integration به mission/track contracts نیاز دارد.
- **US5**: notification/contractor/settings مستقل‌اند؛ legacy removal فقط پس از همه parity gateها.

### Within Each User Story

1. characterization/contract test نوشته و شکست مورد انتظار ثبت شود.
2. module و domain contracts ایجاد شوند.
3. data/platform adapters پیاده شوند.
4. presentation state/event/effect منتقل شود.
5. app composition و compatibility wiring تغییر کند.
6. Debug/Release/device gate اجرا و parity/rollback ثبت شود.

### Parallel Opportunities

- T002–T005، T009–T011، T013–T014 و T017–T020 روی فایل‌های جدا موازی‌اند.
- در هر Story تست‌های `[P]` و build files `[P]` موازی‌اند.
- domain model/contract و platform moduleهایی که dependency مستقیم ندارند با `[P]` مشخص شده‌اند.
- پس از Foundation، unit-level کار US2 و US3 می‌تواند موازی باشد؛ mergeهای `settings.gradle` و app composition باید سریالی شوند.

## Parallel Examples

### User Story 1

```text
T023 LegacyAuthCharacterizationTest.kt
T024 NetworkTrustBoundaryTest.kt
T025 AuthFlowTest.kt
T026 RoleRoutingTest.kt
```

### User Story 2 و 3 پس از Foundation

```text
Track A: T042-T055 (Supervisor)
Track B: T056-T069 (Driver/Tracking)
Integration: settings.gradle و app composition changes به‌ترتیب merge شوند.
```

### User Story 4

```text
T074 core/location build
T075 core/map build
T076 feature/violations build
T077 location gateway
T078 map gateway
```

## Implementation Strategy

### MVP First

1. Phase 1 و approval را کامل کنید.
2. Phase 2 را کامل و architecture baseline را freeze کنید.
3. فقط US1 را پیاده کنید.
4. Debug + minified Release login/roles/first API/logout را مستقل validate کنید.
5. قبل از US2 تصمیم ادامه/اصلاح module boundaries را ثبت کنید.

### Incremental Delivery

1. Foundation → بدون تغییر رفتار کاربر.
2. US1 → Auth/Roles MVP قابل انتشار.
3. US2 → Supervisor slice قابل انتشار.
4. US3 → Driver/Track slice قابل انتشار.
5. US4 → System integrations قابل انتشار.
6. US5 → remaining features و cleanup کنترل‌شده.

### Guardrails

- نمونه GitHub را guideline بدانید؛ library/tooling را بدون ADR و نیاز واقعی کپی نکنید.
- migration و replacement را جدا نگه دارید: ابتدا adapter روی implementation فعلی، سپس feature مستقل برای تعویض implementation.
- فایل‌های dirty و feature 004 را preserve کنید؛ هر task فقط owner/path مشخص خود را تغییر دهد.
- هیچ legacy file پیش از parity evidence و rollback review حذف نشود.

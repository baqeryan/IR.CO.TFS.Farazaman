# Implementation Plan: Material 3 Design System و مهاجرت تدریجی Compose

**Branch**: `004-compose-ui-migration` | **Date**: 2026-09-07 | **Spec**: [spec.md](spec.md)

## Summary

`FaraZamanTheme` موجود به Design System رسمی single-module بر پایه Material 3 evolve می‌شود. مسیر dependency عبارت است از Material 3 → Design Tokens → Fz Components → Feature Components → Pure State/Event Screens → Route/Container/ViewModel. پس از foundation و enforcement، Contractor اولین مصرف‌کننده production و architecture gate آن مانع شروع سایر دسته‌هاست. XML تا Behavioral/Visual Intent parity حفظ می‌شود.

## Technical Context

**Language/Version**: Kotlin 1.9.22 و Java 17 legacy  
**Build Toolchain**: AGP 8.4.2، Compose Compiler 1.5.10  
**UI Dependencies**: Compose BOM 2024.02.02، `androidx.compose.material3:material3` از BOM، Material Components Views 1.11.0 برای XML legacy  
**Primary Dependencies**: Lifecycle، Hilt، Coroutines، AppCompat/ViewBinding، MapLibre  
**Storage**: SQLite/ContentProvider و SharedPreferences موجود؛ بدون data migration  
**Testing**: JUnit4/Robolectric، Compose UI tests، Roborazzi screenshot regression، preview catalog، static architecture checks و device smoke tests  
**Target Platform**: Android minSdk 23، target/compileSdk 34، Persian RTL  
**Project Type**: single-module Android app  
**Performance Goals**: افت کمتر از 10٪ در render/scroll و صفر resource leak در 20 lifecycle cycle  
**Constraints**: runtime BASE_URL، auth/storage فعلی، R8 Release، live dark mode، no blind redesign  
**Scale/Scope**: همه مقصدهای user-facing در چند release مستقل

**Dependency decision**: نسخه‌های فعلی Material 3 foundation موردنیاز را دارند. upgrade در این plan انجام نمی‌شود؛ هر نیاز آینده به API جدید باید به task/تغییر جدا با compatibility matrix تبدیل شود.

**Screenshot decision**: ابزار رسمی Compose Preview Screenshot Testing با AGP فعلی `8.4.2` سازگار نیست و حداقل AGP `8.5.0` می‌خواهد. برای جلوگیری از upgrade بی‌دلیل toolchain، Roborazzi روی Robolectric Native Graphics انتخاب می‌شود. یک compatibility spike باید پیش از افزودن دائمی dependency، نسخه سازگار را pin و Debug/Release configuration را تأیید کند. Map/Camera/GPS/Track با fakeهای قطعی screenshot می‌شوند و smoke test واقعی دستگاه جدا باقی می‌ماند.

## Constitution Check

- **Preserve behavior / existing behavior as spec**: PASS — Behavioral Parity اجباری؛ visual changes intentional ثبت می‌شوند.
- **Backend compatibility / production mechanisms**: PASS — API، auth، BASE_URL، persistence و system coordinators حفظ می‌شوند.
- **Analyze architecture**: PASS — audit واقعی Theme و dependencies در [research.md](research.md) ثبت شده است.
- **Compose conventions**: PASS — pure State/Event، lifecycle، semantics، RTL و centralized theme الزام‌اند.
- **Incremental delivery**: PASS — Foundation → Contractor gate → دسته‌های مستقل → cleanup.
- **Migration impact**: PASS — [migration-inventory.md](migration-inventory.md)، coexistence و rollback بخشی از خروجی‌اند.
- **Build/tests**: PASS — foundation matrix و Debug/Release gate تعریف شده است.
- **Accessibility gate**: PASS — touch target حداقل 48dp و WCAG AA برابر 4.5:1 متن عادی و 3:1 متن بزرگ است.

بازبینی پس از طراحی: PASS. Design System منبع business data نیست، business concept نمی‌شناسد و module جدید ایجاد نمی‌کند.

## Target Architecture

```text
androidx.compose.material3
        ↓
presentation/design/theme tokens + FaraZamanTheme
        ↓
presentation/design/components/Fz*
        ↓
feature-local components (business → semantic mapping)
        ↓
pure XxxScreen(state, onEvent)
        ↓
XxxRoute / Activity / Fragment / ViewModel
```

قانون dependency یک‌طرفه است؛ Design System اجازه import از activity، supervisor، data، domain یا feature business models ندارد.

## Phases

1. **Inventory / Baseline**: Compose موجود، XML destinations، Material bridges، behavior و visual intent.
2. **Material 3 + Design System Foundation**: audit dependency؛ tokens؛ evolved Theme؛ حداقل FzButton/Card/TextField/StatusChip/Scaffold و feedback؛ previews/accessibility.
3. **State/Event Architecture Foundation**: contract/effect، lifecycle collection و static enforcement.
4. **Contractor MVP**: اولین مصرف production و architecture review blocking gate.
5. **Support/List**: notification/profile/pagination و feature mappings.
6. **Supervisor Shell**: work area/home/today/history و responsive shell.
7. **Forms / Map / Camera / GPS**: lifecycle adapters و sensitive gates.
8. **Legacy Cleanup**: حذف XML/Material bridge فقط پس از Definition of Done.

## Project Structure

```text
app/src/main/java/Ir/co/tfs/farazaman/presentation/
├── design/
│   ├── theme/       # FaraZamanTheme, Color, Typography, Shapes, Spacing, Elevation, Motion, ThemeMode
│   ├── components/  # only proven foundational Fz components
│   └── preview/     # reusable annotations/fixtures where production-safe
├── compose/         # feature screens during gradual migration
└── viewmodel/       # StateFlow owners/reducers

app/src/debug/java/Ir/co/tfs/farazaman/presentation/design/preview/ # preview catalog
app/src/test/java/Ir/co/tfs/farazaman/presentation/                 # architecture/reducer tests
app/src/androidTest/java/Ir/co/tfs/farazaman/                       # UI/matrix journeys
app/src/main/res/layout/                                            # retained per-screen until gate
specs/004-compose-ui-migration/                                     # architecture/migration records
```

**Structure Decision**: package `Ir.co.tfs.farazaman.presentation.design` داخل `:app` ایجاد می‌شود. `presentation.compose.FaraZamanTheme` منتقل/evolve و call siteها به source واحد هدایت می‌شوند؛ Theme دوم مجاز نیست. Preview annotationهایی که فقط tooling هستند در debug source قرار می‌گیرند.

## Foundation Scope

Foundation اولیه فقط موارد لازم برای Contractor و shell مشترک را شامل می‌شود: Theme/tokens، FzButton، FzCard، FzTextField، FzStatusChip، FzScaffold و loading/empty/error. TopAppBar/Badge/Dialog هنگامی foundation می‌شوند که audit مصرف فوری را تأیید کند؛ در غیر این صورت در story مصرف‌کننده ساخته می‌شوند.

### Component Extraction Boundary

- **Design System Components**: اجزای فاقد business concept مانند `FzButton`, `FzCard`, `FzTextField`, `FzStatusChip` و `FzScaffold`. این اجزا به‌دلیل policy سراسری branding/semantics/accessibility/variants می‌توانند پیش از Screenهای production و آگاهانه ساخته شوند.
- **Feature Shared Components**: اجزای business-aware مانند `ViolationCard`, `NotificationCard` و `ContractorHeader`. این اجزا جزو Foundation نیستند و فقط داخل Feature و پس از مشاهده تکرار یا نیاز مشترک واقعی استخراج می‌شوند.

Foundation نباید componentهای فرضی Feature را پیش‌بینی کند و Feature Shared Component نیز نباید به package `presentation.design` منتقل شود.

## Enforcement Strategy

- تست dependency/package برای ممنوعیت data/system imports در `*Screen` و business imports در design.
- static scan محدود به Screen packages برای Color literals، `TextStyle/fontSize` پراکنده، reusable `RoundedCornerShape` و Material 2 imports.
- allowlist با دلیل برای local one-off values و low-level rendering؛ rule ابتدا report-only و پس از پاک‌سازی baseline blocking می‌شود.
- direct Material 3 primitives مجاز برای layout/text/icon ساده‌اند؛ style کردن componentهای policyدار بیرون design ممنوع است.

## Migration and Rollback

- رفتار فعلی مرجع **Behavioral Parity** شامل data/action/navigation/validation/loading/error/lifecycle/permissions/business rules است و رعایت آن اجباری است.
- ظاهر XML baseline مقایسه است، اما مقصد Compose از Farazaman Material 3 Design System پیروی می‌کند.
- **Visual Intent Parity** شامل branding، information hierarchy، usability و accessibility است؛ pixel parity اجباری نیست و اختلاف intentional باید ثبت شود.
- visual delta عمدی در Migration Record با rationale ثبت می‌شود.
- قبولی checklist و automated tests برای Visual Intent کافی است و تأیید انسانی جداگانه شرط نیست.
- XML/container قبلی تا پذیرش حفظ می‌شود؛ rollback صفحه‌های عادی با کد/نسخه بعدی انجام می‌شود.
- Map/Camera/GPS/Track تا پایان rollout یک runtime flag محلی دارند که پیش‌فرض Compose است و از diagnostics/support مخفی و محافظت‌شده به XML تغییر می‌کند؛ کاربران عادی به آن دسترسی ندارند.

## Validation Gates

- Foundation: component preview catalog در Light+RTL، Dark+RTL، large font، small/normal phone و برای shell large/tablet؛ touch target حداقل 48dp و contrast عددی 4.5:1/3:1.
- Visual regression: Roborazzi record/verify برای تمام Screenها؛ system/native surfaces با fake قطعی و golden versioned.
- Contractor: architecture checks + behavioral/UI/accessibility matrix؛ review gate کتبی پیش از Support/Supervisor.
- هر دسته: `:app:testDebugUnitTest`, `:app:lintDebug`, `:app:assembleDebug`.
- system/cleanup: `:app:lintRelease`, `:app:assembleRelease` و smoke روی ARM؛ emulator Debug برای MapLibre.

## Complexity Tracking

| Complexity | Why Needed | Simpler Alternative Rejected Because |
|---|---|---|
| هم‌زیستی XML/Compose | rollback مستقل هر مقصد | big-bang migration ریسک عملیاتی دارد |
| لایه Design System داخل app | جلوگیری از styling drift و آمادگی semantic SDUI | Theme-only abstraction component/accessibility policy را متمرکز نمی‌کند |

Gradle module جدا ایجاد نمی‌شود؛ package boundary و tests برای Scope فعلی کافی است.

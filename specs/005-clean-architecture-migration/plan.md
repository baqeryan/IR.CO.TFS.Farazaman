# Implementation Plan: مهاجرت تدریجی Clean Architecture چندماژوله

**Branch**: `005-clean-architecture-migration` | **Date**: 2026-09-30 | **Spec**: [spec.md](spec.md)

## Summary

FaraZaman از `:app` تک‌ماژوله و لایه‌بندی ناهماهنگ، با برش‌های vertical و قابل انتشار به composition root کوچک، core moduleهای پایدار و feature moduleهای مستقل مهاجرت می‌کند. از نمونه مرجع، dependency inversion، feature modules، MVI/state و test doubles اقتباس می‌شود؛ Room/Moshi/NDK key storage و Navigation سراسری بدون نیاز پروژه کپی نمی‌شوند.

## Technical Context

**Language/Version**: Kotlin 1.9.22، Java 17 legacy  
**Primary Dependencies**: Compose Material 3، Hilt، Coroutines/Flow، Retrofit/OkHttp/Gson، AppAuth، MapLibre، Firebase، SignalR  
**Storage**: SQLite ContentProvider فعلی، SharedPreferences و فایل‌های داخلی؛ Room فقط scaffolding غیرفعال  
**Testing**: JUnit4، Robolectric، Mockito، Compose UI، Roborazzi، Espresso/device smoke، Gradle dependency checks  
**Target Platform**: Android minSdk 23، target/compileSdk 34، ARM Release و x86 Debug  
**Project Type**: Android multi-module application  
**Performance Goals**: startup و tracking بدون regression محسوس؛ هیچ I/O روی main thread؛ عدم افزایش leak در lifecycle smoke  
**Constraints**: selected `BASE_URL`، R8، permanent signing، Persian/RTL، legacy authority/actions و migration بدون توقف عملیات میدانی  
**Scale/Scope**: تمام گردش‌های تولیدی در پنج increment؛ coexistence موقت packageهای قدیمی و moduleهای جدید

## Constitution Check

- **Existing behavior is the specification**: PASS — characterization tests و parity gate قبل از جابه‌جایی اجباری است.
- **Production mechanisms**: PASS — auth، selected server، persistence، GPS، MapLibre، FCM و SignalR تعویض ناگهانی نمی‌شوند.
- **Analyze before change**: PASS — [research.md](research.md) تفاوت نمونه و FaraZaman را ثبت می‌کند.
- **Incremental delivery**: PASS — هر user story یک vertical slice مستقل و rollbackable است.
- **Migration impact record**: PASS PENDING APPROVAL — spec/plan/contracts اثر modules، Gradle، DI، R8 و ownership را ثبت کرده‌اند؛ implementation قبل از approval شروع نشود.
- **Testing and Release**: PASS — هر slice gateهای کوچک و در پایان minified Release smoke دارد.
- **Security**: PASS — secretها جابه‌جا یا log نمی‌شوند و token abstraction رفتار encryption فعلی را حفظ می‌کند.

بازبینی پس از طراحی: PASS PENDING APPROVAL. پیچیدگی چندماژوله با coupling فعلی توجیه می‌شود، ولی هر module فقط هنگام مهاجرت اولین مصرف‌کننده ساخته می‌شود.

## Target Dependency Graph

```text
:app (composition root, navigation, manifest/platform entry points)
  ├── :feature:auth / :feature:roles
  ├── :feature:supervisor / :feature:driver / :feature:tracking
  ├── :feature:violations / :feature:contractor / :feature:notifications
  └── :feature:settings

:feature:*  ──> :core:common, :core:model, :core:ui
              └> capability-specific core APIs only

:core:network ──> :core:common, :core:model
:core:security ─> :core:common, :core:model
:core:database ─> :core:common, :core:model
:core:location / :core:map ─> :core:common, :core:model
```

Feature-to-feature Gradle dependency ممنوع است. cross-feature navigation از route contracts در `:app` و cross-feature data از interfaceهای کوچک core انجام می‌شود.

## Project Structure

```text
app/                              # composition root, manifest, Application, platform services/adapters
core/
├── common/                       # Result/error, dispatchers, time abstractions; Kotlin-first
├── model/                        # shared domain value objects only
├── network/                      # selected-server Retrofit/OkHttp and central unauthenticated clients
├── security/                     # token/session secure implementations
├── database/                     # ContentProvider adapters; no forced Room migration
├── location/                     # lifecycle-neutral location contracts/policies
├── map/                          # MapLibre adapters and style/overlay implementation
├── ui/                           # FaraZaman design system and common Compose contracts
└── testing/                      # fakes, fixtures, dispatcher rules
feature/
├── auth/                         # city selection, OIDC/password, session bootstrap
├── roles/                        # role selection/profile routing contract
├── supervisor/                   # work area, mission, daily plan, history
├── driver/                       # driver mission shell
├── tracking/                     # track list/detail/waypoint/export/upload
├── violations/                   # violation forms, validation, camera/location orchestration
├── contractor/                   # contracted operations only
├── notifications/                # inbox, unread state and realtime-facing use cases
└── settings/                     # preferences/about/support
build-logic/                      # convention plugins only if duplication is proven by first modules
specs/005-clean-architecture-migration/
```

## Migration Strategy

1. Snapshot dependency graph، public Android contracts و characterization tests.
2. ایجاد حداقل core contracts و architecture tests؛ از ساخت همه moduleها پیشاپیش خودداری شود.
3. انتقال auth/roles به‌عنوان MVP و نگه‌داشت adapterهای compatibility در app.
4. انتقال supervisor سپس driver/tracking، با ContentProvider/GPS adapters موجود.
5. انتقال violations/map/location و بعد notifications/contractor/settings.
6. کوچک‌کردن composition root و حذف legacy فقط پس از parity و Release smoke.

هر انتقال با روش strangler انجام می‌شود: interface جدید → adapter روی implementation فعلی → انتقال consumer → تست → حذف consumer قدیمی. تعویض implementation زیرین یک feature مستقل بعدی است.

## Validation Gates

- Per task/module: compile/test همان module.
- Per slice: dependency checks، unit/contract tests، `:app:assembleDebug` و smoke گردش مربوط.
- Auth/data-sensitive: `:app:lintRelease` و `:app:assembleRelease` با تست login + first authenticated API.
- Tracking/map/system: device smoke روی ARM، lifecycle/permission/process recreation و verification فایل/database.
- Final: `:app:testDebugUnitTest`, `:app:lintDebug`, `:app:lintRelease`, `:app:assembleDebug`, `:app:assembleRelease` و architecture dependency report بدون violation جدید.

## Complexity Tracking

| Complexity | Why Needed | Simpler Alternative Rejected Because |
|---|---|---|
| چندماژوله‌شدن تدریجی | enforce کردن مرزها و کاهش coupling گردش‌های مستقل | package-only boundary در وضعیت فعلی بارها توسط Activity/Retrofit/Preferences دور زده شده است |
| coexistence adapterهای legacy | حفظ production behavior و rollback | rewrite هم‌زمان auth/GPS/database ریسک از دست‌رفتن داده میدانی دارد |
| چند core platform module | جداسازی network/security/database/map/location با lifecycle و testability متفاوت | یک `core` بزرگ به dependency dumping ground تبدیل می‌شود |

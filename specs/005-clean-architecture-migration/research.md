# Research: تطبیق نمونه Clean Architecture با FaraZaman

## منبع مرجع

نمونه `eslamfaisal/android-compose-clean-architecture-sample` ساختار `app` + `core:{common,network,database,security,ui}` + `features:*`، Clean layers، Compose/MVI، Hilt و تست‌پذیری را نمایش می‌دهد. جهت کلی برای FaraZaman مناسب است، اما repository نمونه نیز یک dependency مستقیم feature-to-feature دارد؛ بنابراین source of truth قوانین مستند و نیازهای FaraZaman است، نه کپی کامل implementation.

## تصمیم‌ها

### 1. Adopt: module boundaries و vertical slices

**Decision**: `:app` composition root، core capability modules و feature modules با domain/data/presentation داخلی.

**Rationale**: coupling فعلی Activityها با API، preference، ContentResolver و navigation با compile-time boundaries کنترل می‌شود.

### 2. Adapt: MVI به State/Event/Effect موجود

**Decision**: قرارداد Compose موجود FaraZaman (`state + typed event`) حفظ و one-shot effect صریح اضافه شود؛ reducer/use caseها pure باشند.

**Rationale**: با feature 004 و UI موجود هم‌راستا است و rewrite دوباره UI ایجاد نمی‌کند.

### 3. Reject now: جایگزینی ContentProvider با Room

**Decision**: `:core:database` ابتدا adapter روی `TrackContentProvider` و `DataHelper` است.

**Rationale**: trackهای واقعی، role separation، authority ثابت و upgrade compatibility مهم‌تر از شباهت به نمونه‌اند. migration به Room نیازمند spec و data migration مستقل است.

### 4. Reject now: Gson→Moshi، Groovy→Kotlin DSL و Navigation Compose سراسری

**Decision**: toolchain و serializer فعلی حفظ شوند.

**Rationale**: این تغییرها ارزش مستقیم برای boundary migration ندارند و سطح regression/R8 را زیاد می‌کنند.

### 5. Split network clients by trust boundary

**Decision**: selected-municipality authenticated client از central registry/update client جدا و قابل تست باقی بماند.

**Rationale**: جلوگیری از ارسال bearer token به host مرکزی و حفظ runtime `BASE_URL`.

### 6. Domain purity with pragmatic Android adapters

**Decision**: model/use case/repository contractها Kotlin-only؛ Service، Activity، AppAuth، ContentResolver، MapLibre و camera adapter در data/platform یا app entry point.

**Rationale**: Android framework قابل حذف نیست، اما نباید business policy را مالک باشد.

### 7. Module creation is demand-driven

**Decision**: module فقط هنگام اولین vertical slice ساخته شود؛ convention plugin بعد از مشاهده duplication واقعی.

**Rationale**: جلوگیری از empty module و build complexity زودهنگام.

## ریسک‌ها و کاهش ریسک

- **Circular dependencies**: architecture tests + ممنوعیت feature-to-feature.
- **Hilt aggregation/build time**: baseline قبل/بعد و محدودکردن bindingها به owner module.
- **R8 regression**: keep rules و minified smoke در هر slice network/auth.
- **Lost field data**: persistence implementation ثابت، backup/fixture و destructive migration ممنوع.
- **Double ownership of lifecycle**: ownership matrix برای GPS/SignalR/FCM/map/camera و تنها یک start/stop owner.
- **Dirty ongoing Compose migration**: feature 005 روی قرارداد و انتقال مالکیت تمرکز دارد و فایل‌های feature 004 را بدون نیاز بازنویسی نمی‌کند.

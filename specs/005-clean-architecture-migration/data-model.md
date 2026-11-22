# Data Model: مفاهیم مهاجرت معماری

## ArchitectureBoundary

- `modulePath`: مسیر Gradle مانند `:core:network`
- `owner`: تیم/feature مالک
- `publicContracts`: interface/modelهای قابل مصرف
- `allowedDependencies`: dependencyهای مجاز
- `forbiddenDependencies`: dependency/importهای ممنوع
- `migrationState`: planned | coexistence | authoritative | retired

## MigrationSlice

- `id`, `name`, `priority`
- `legacyOwners`: فایل‌ها/packageهای فعلی
- `targetModules`: ماژول‌های مقصد
- `characterizationTests`: baselineهای رفتار
- `rolloutGate`: تست/build/smoke لازم
- `rollbackPath`: adapter/entry point قابل برگشت
- `parityEvidence`: لینک test report یا review record

## SessionConfiguration

- selected municipality base URL
- login mode و OIDC client configuration
- access/refresh token state از طریق abstraction امن
- user profile و authoritative role response

**Invariant**: client شهرداری از selected base URL و client مرکزی بدون bearer token ساخته می‌شود.

## Mission و Track

- شناسه local/remote، role، state، timestamps و ارتباط mission metadata
- TrackPoint/Waypoint به‌صورت domain model مستقل از Cursor/ContentValues

**Invariant**: driver/supervisor role separation و retention داده uploadنشده حفظ می‌شود.

## UiContract

- `UiState`: immutable و قابل restore در حد نیاز
- `UiEvent`: ورودی typed کاربر/system
- `UiEffect`: navigation، permission، launcher و پیام one-shot

**Invariant**: Screen به repository implementation، Retrofit، SharedPreferences، ContentResolver یا Android Service دسترسی مستقیم ندارد.

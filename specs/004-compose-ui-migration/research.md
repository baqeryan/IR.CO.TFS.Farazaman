# Research: Material 3 Design System Migration

## Repository findings

- Kotlin `1.9.22`، AGP `8.4.2` و Compose Compiler `1.5.10` در Gradle ثبت شده‌اند.
- Compose BOM `2024.02.02` و unversioned `androidx.compose.material3:material3` فعال‌اند.
- `com.google.android.material:material:1.11.0` برای XML Views همچنان لازم است و Material 2 Compose dependency مستقیم دیده نشد.
- `presentation/compose/FaraZamanTheme.kt` اکنون Material 3 light/dark ColorScheme و RTL را فراهم می‌کند، ولی colors در همان فایل hard-code، typography/shapes تعریف‌نشده و extension tokens وجود ندارند.
- `RoleSelectionScreen` و `AppUpdateUi` مستقیم Material components را style می‌کنند؛ RoleSelection همچنین font، dp/sp، shape و resource colors محلی دارد.
- Previewهای debug برای Splash، RoleSelection، AppUpdate و Contractor وجود دارند؛ MultiPreview مرکزی و component catalog وجود ندارد.

## Decisions

### Evolve existing Theme; no second source

**Decision**: `FaraZamanTheme` موجود به `presentation/design/theme` منتقل و API نام آن حفظ شود.  
**Rationale**: call siteها و behavior live dark/RTL موجود حفظ می‌شوند.  
**Alternative rejected**: Theme جدید کنار قبلی؛ دو palette و drift ایجاد می‌کند.

### Keep current dependency versions

**Decision**: برای foundation فعلی upgrade انجام نشود.  
**Rationale**: Material 3 Stable موردنیاز حاضر است و upgrade با Kotlin/compiler/AGP و Release risk بی‌دلیل مخلوط می‌شود.  
**Alternative rejected**: رفتن به آخرین BOM صرفاً برای تازگی.

### Brand-controlled theme

**Decision**: dynamic color خاموش؛ ThemeMode/config قابل جایگزینی طراحی شود.  
**Rationale**: branding قابل پیش‌بینی و امکان Alternative/HighContrast/Compact آینده بدون تغییر Screen.  
**Alternative rejected**: dynamic color پیش‌فرض یا singleton palette غیرقابل تعویض.

### Semantic extension colors

**Decision**: Material ColorScheme برای نقش‌های استاندارد و CompositionLocal extension برای success/warning/info مصرف شود.  
**Rationale**: ColorScheme این statusها را پوشش نمی‌دهد و Feature نباید رنگ خام بداند.

### Foundation wrappers only where policy exists

**Decision**: Button/Card/TextField/StatusChip/Scaffold و feedback stateهای لازم برای MVP ابتدا ساخته شوند؛ سایر wrapperها با مصرف واقعی.  
**Rationale**: branding/variant/accessibility مرکزی بدون wrapper explosion.

### Separate foundation components from feature shared components

**Decision**: `Fz*` componentهای generic را می‌توان آگاهانه در Foundation ایجاد کرد؛ componentهای business-aware مانند `ViolationCard`, `NotificationCard` و `ContractorHeader` فقط پس از اثبات تکرار داخل Feature استخراج می‌شوند.  
**Rationale**: Design System باید مستقل از domain بماند و هم‌زمان استخراج زودهنگام Feature abstractions رخ ندهد.  
**Alternative rejected**: قرار دادن تمام shared UIها در یک package مشترک یا ایجاد پیشاپیش componentهای فرضی Feature.

### Three-level preview strategy

**Decision**: Design System catalog، Feature Component preview و Screen state preview جدا باشند؛ `@FarazamanPreview` matrix مشترک ارائه شود.  
**Rationale**: regression در پایین‌ترین لایه قابل تشخیص است.

### Semantic SDUI boundary

**Decision**: client mapping فقط type/variant معنایی را می‌پذیرد؛ raw styling بخشی از contract نیست.  
**Rationale**: backend به جزئیات rendering وابسته نمی‌شود. backend change خارج Scope است.

### Parity model

**Decision**: Behavioral Parity اجباری؛ Visual Intent Parity جای pixel parity.  
**Rationale**: Material 3 ممکن است اندازه/shape بصری را آگاهانه تغییر دهد، ولی UX و branding نباید regression کند.

### Incremental enforcement

**Decision**: static rules ابتدا baseline/report-only و سپس برای migrated packages blocking شوند.  
**Rationale**: legacy Compose فعلی false positive دارد و نباید foundation را قفل کند.

### Quantified accessibility gate

**Decision**: minimum touch target برابر 48dp؛ contrast متن عادی حداقل 4.5:1 و متن بزرگ حداقل 3:1.  
**Rationale**: معیار Android/WCAG AA قابل‌اندازه‌گیری و مناسب محیط عملیاتی است.  
**Alternative rejected**: اتکا به defaults Material بدون gate عددی.

### Automated screenshot tool

**Decision**: Roborazzi با Robolectric Native Graphics برای همه Design System/Feature/Screen goldens؛ native dependencies با fake قطعی. نسخه library در compatibility spike روی toolchain فعلی pin می‌شود.  
**Rationale**: ابزار رسمی Compose Preview Screenshot حداقل AGP 8.5 می‌خواهد، ولی پروژه AGP 8.4.2 دارد. Roborazzi taskهای record/verify/compare و Compose capture دارد.  
**Alternatives considered**: ابزار رسمی پس از upgrade جدا؛ checklist دستی؛ instrumented-only capture.

### Visual Intent acceptance

**Decision**: عبور از checklist و automated regression tests کافی است؛ human sign-off جدا الزام نیست.  
**Rationale**: gate تکرارپذیر می‌ماند؛ intentional delta همچنان در Migration Record ثبت می‌شود.

### Risk-scoped runtime rollback

**Decision**: فقط Map/Camera/GPS/Track runtime flag دارند؛ flag محلی، مخفی، پیش‌فرض Compose و در Activity غیر-exported است. دسترسی production با نقش موجود `superAdministrator` محافظت می‌شود. سایر صفحات rollback کد/نسخه‌ای دارند.
**Rationale**: rollback فوری system-critical بدون backend یا پیچیدگی تمام Screenها.  
**Alternatives considered**: flag همه Screenها، BuildConfig و remote configuration خارج Scope.

# Data Model: Design System and Screen Contracts

## ThemeConfiguration

- `mode`: System | Light | Dark
- `brandPalette`: token set انتخابی؛ فعلاً DefaultFarazaman
- `densityProfile`: فعلاً Default؛ extensible برای Compact
- `contrastProfile`: فعلاً Standard؛ extensible برای HighContrast
- `dynamicColorEnabled`: پیش‌فرض false

## DesignTokenSet

- `colors`: Material ColorScheme + semantic success/warning/info roles
- `typography`: Persian-aware semantic text styles
- `shapes`: semantic component shapes
- `spacing`: xs/sm/md/lg/xl و در صورت نیاز screen/content spacing
- `elevation`: none/low/medium/high
- `motion`: duration/easing semantic roles با respect به reduced motion policy
- `componentSizes`: touch target، button heights، icon/avatar/badge sizes

Tokenها immutable و فقط توسط ThemeConfiguration انتخاب می‌شوند.

## ComponentVariant

- Button: Primary | Secondary | Danger | Ghost
- Status: Default | Info | Success | Warning | Error
- Card/TextField/feedback: فقط variantهای اثبات‌شده در مصرف واقعی

variant معنایی است و color/radius/elevation خام حمل نمی‌کند. مقدار ناشناخته به Default امن نگاشت می‌شود.

## FoundationComponentContract

- داده نمایشی و semantic variant
- enabled/loading/error state حسب component
- accessibility semantics و minimum touch target
- styling فقط از tokens
- بدون import از business/domain models

## FeatureComponentContract

- داده نمایشی feature-specific
- mapping صریح business state → ComponentVariant
- بدون raw styling API
- محل نگهداری در package همان Feature
- استخراج به‌عنوان Feature Shared Component فقط پس از اثبات تکرار یا نیاز مشترک واقعی

Feature Shared Component به Design System منتقل نمی‌شود و Foundation آن را از قبل پیش‌بینی نمی‌کند.

## ScreenContract

- immutable `UiState`
- sealed `UiEvent`
- optional one-off `UiEffect`
- Route/state owner/system dependencies
- pure Screen که Theme را نصب نمی‌کند

## MigrationRecord

- screenId، owner، risk، legacy/Compose path
- Material 2/legacy bridges و removal condition
- status: NotStarted | InProgress | ArchitectureReview | ParityReview | Migrated | Blocked
- Behavioral Parity evidence
- Visual Intent baseline و intentional deltas
- preview/accessibility/matrix/test/build evidence
- coexistence/rollback note
- screenshot golden path، matrix و آخرین verification
- runtime flag metadata برای system-critical destinations

## MigrationFlag

- `screenId`: فقط Map | Camera | GPS | Track destinations
- `defaultRoute`: Compose
- `selectedRoute`: Compose | Legacy
- `visibility`: hidden diagnostics/support only
- `protectionPolicy`: غیرقابل مشاهده و تغییر برای کاربر عادی
- `owner`, `rolloutEndCondition`, `removalCondition`

Flag فقط presentation route را انتخاب می‌کند و business data، API یا persistence اصلی را تغییر نمی‌دهد.

## ScreenshotBaseline

- `screenId`, `stateName`, `themeMode`, `layoutDirection`, `fontScale`, `deviceProfile`
- deterministic fixture identifier
- versioned golden path و intentional delta reference
- native surface replacement: fake/static deterministic renderer

## State transitions

Screen: `Idle → Loading → Content | Empty | Error`; refresh داده قبلی را حفظ می‌کند.  
Form: `Editing → Validating → Submitting → SuccessEffect | EditingWithErrors`.  
Pagination: `Content → LoadingNext → Content | EndReached | PageError`.  
Migration: `NotStarted → InProgress → ArchitectureReview → ParityReview → Migrated` یا `Blocked`.  
System-critical route: `Compose(default) ↔ Legacy` فقط از diagnostics تا پایان rollout؛ سپس flag حذف می‌شود.

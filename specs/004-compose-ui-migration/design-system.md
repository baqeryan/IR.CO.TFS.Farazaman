# Farazaman Design System

## Architecture

Material 3 implementation detail زیر Theme/tokens/components فرازمان قرار می‌گیرد. Feature Screen فقط semantic API فرازمان و Feature Componentهای خودش را می‌بیند.

## Material 3 Policy

- Foundation رسمی: `androidx.compose.material3` موجود در Compose BOM پروژه.
- Stable API پیش‌فرض است.
- Experimental/Alpha فقط پشت wrapper، با rationale و fallback.
- Material 2 Compose جدید ممنوع؛ Material Components Views فقط bridge XML ثبت‌شده است.

## Tokens

Color، Typography، Shapes، Spacing، Elevation، Motion و ComponentSizes immutable و از Theme قابل دسترسی‌اند. مقادیر reusable در Screen تعریف نمی‌شوند.

## Semantic Colors

ColorScheme نقش‌های استاندارد Material را می‌دهد. extension فرازمان success/onSuccess، warning/onWarning و info/onInfo را برای light/dark فراهم می‌کند. Feature status هرگز رنگ خام انتخاب نمی‌کند.

## Typography

فونت فارسی و نقش‌های display/title/body/label در Theme مرکزی تعریف می‌شوند. Feature تنها semantic text style مصرف می‌کند و font size پراکنده نمی‌سازد.

## Shapes, Spacing, Elevation and Motion

- Shapeها بر اساس نقش component تعریف می‌شوند.
- spacing حداقل scale `xs/sm/md/lg/xl` دارد.
- elevation و motion نام معنایی دارند.
- reduced-motion/accessibility policy در سطح foundation رعایت می‌شود.

## Components and Variants

Foundation اولیه: FzButton، FzCard، FzTextField، FzStatusChip، FzScaffold و Loading/Empty/Error. variantها semantic هستند. wrapper دیگر فقط با branding، behavior، semantics، accessibility، variants، change-risk یا reuse واقعی اضافه می‌شود.

### Design System Components

اجزای generic و فاقد business concept هستند. به‌دلیل policy مرکزی branding، semantics، accessibility یا semantic variants می‌توان آن‌ها را از ابتدا و آگاهانه ساخت. این دسته در `presentation.design.components` قرار می‌گیرد.

### Feature Shared Components

اجزایی مانند `ViolationCard`, `NotificationCard` و `ContractorHeader` business-aware هستند، در package همان Feature باقی می‌مانند و فقط پس از اثبات تکرار یا نیاز مشترک واقعی استخراج می‌شوند. این اجزا نباید وارد Design System شوند و Foundation نباید آن‌ها را پیشاپیش ایجاد کند.

## RTL and Persian

Design System RTL-first است، start/end مصرف می‌کند و typography فارسی را مرکزی نگه می‌دارد. direction override موضعی فقط برای داده ذاتاً LTR با دلیل مجاز است.

## Accessibility

حداقل touch target 48dp، content description، semantic role، disabled state، scalable font، error announcement و stable test semantics بخشی از contract هر component است. کنتراست متن عادی حداقل 4.5:1 و متن بزرگ حداقل 3:1 است.

## Preview Strategy

- Design System: tokens و component variants/states.
- Feature Component: mapping business→semantic.
- Screen: Content/Loading/Empty/Error/Refreshing/Submitting/Validation/Pagination.

`@FarazamanPreview` matrix مشترک Persian RTL، light/dark، large font و small/normal phone را پوشش می‌دهد؛ shellها large/tablet نیز دارند.

تمام این سطوح با Roborazzi/Robolectric golden خودکار دارند. Map، camera، GPS و native surfaces در screenshot با fake قطعی نمایش داده می‌شوند؛ rendering واقعی آن‌ها در device smoke test پوشش داده می‌شود.

## Experimental API Policy

نیاز، نسخه، isolation، fallback و exit condition باید در Migration Record ثبت شود. Feature هیچ opt-in مستقیمی ندارد.

## SDUI Mapping Policy

SDUI فقط type/variant معنایی را به mapper client می‌دهد. hex، font size، radius و elevation خام پذیرفته نمی‌شوند. unknown variant به Default امن می‌رود.

## Theme Extensibility

ThemeConfiguration انتخاب token set را جدا می‌کند تا Default Farazaman امروز و Alternative Brand/High Contrast/Compact آینده بدون تغییر Screen ممکن باشد. presetهای آینده در Scope فعلی پیاده نمی‌شوند؛ Dynamic Color پیش‌فرض خاموش است.

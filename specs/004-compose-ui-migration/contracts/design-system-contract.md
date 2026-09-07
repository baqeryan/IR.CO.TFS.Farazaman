# Farazaman Design System Contract

## Public dependency surface

Featureها فقط این سطوح را مصرف می‌کنند:

- `FaraZamanTheme` و read-only semantic tokens
- Fz foundation components و semantic variants
- Preview annotation/fixtures بدون runtime dependency واقعی

## Prohibited dependencies

- Design System نباید activity، supervisor، data، domain یا business DTO/model import کند.
- API عمومی component نباید branding Color، raw radius، elevation یا arbitrary TextStyle از Feature بگیرد.
- Feature Screen نباید Material 2 Compose یا Experimental Material را مستقیم import کند.
- componentهای business-aware مانند `ViolationCard`, `NotificationCard` و `ContractorHeader` نباید وارد package Design System شوند.

## Component Categories

- Design System Component: generic، semantic و فاقد business concept؛ ایجاد آگاهانه در Foundation مجاز است.
- Feature Shared Component: business-aware و متعلق به Feature؛ فقط پس از اثبات تکرار یا نیاز مشترک واقعی استخراج می‌شود.

## Experimental API policy

استفاده فقط با سند شامل نیاز، API/version، risk، wrapper، fallback و removal/stabilization condition مجاز است. wrapper در package design می‌ماند و Feature API stable فرازمان را می‌بیند.

## SDUI mapping

ورودی مجاز semantic است: `componentType` و `variant`. raw `color/fontSize/radius/elevation` contract نیست. mapper client مقدار ناشناخته را به Default امن تبدیل می‌کند. جزئیات rendering صرفاً در Design System است.

## Accessibility policy

حداقل touch target برابر 48dp، semantic role، content description، disabled semantics، scalable text، error announcement و stable test semantics اجباری است. contrast متن عادی حداقل 4.5:1 و متن بزرگ حداقل 3:1 است.

## Screenshot contract

- تمام componentها و Screenها golden خودکار دارند.
- matrix از fixture قطعی استفاده می‌کند و شبکه، زمان، location، camera و Map renderer واقعی وارد capture نمی‌شوند.
- تغییر golden فقط همراه intentional delta ثبت‌شده مجاز است.
- verify بدون دستگاه اجرا می‌شود؛ system smoke روی دستگاه gate جداست.

# System-Critical Migration Control Contract

## Scope

فقط مقصدهای Map، Camera، GPS و Track در دوره rollout دارای runtime route flag هستند.

## Behavior

- default هر flag برابر Compose است.
- مقدار Compose یا Legacy فقط presentation route را تغییر می‌دهد.
- data، mission، track، upload، auth و backend contract مشترک باقی می‌مانند.
- تغییر flag روی ورود بعدی به مقصد اعمال می‌شود و انتشار APK جدید نمی‌خواهد.

## Access

- کنترل فقط در diagnostics/support مخفی و Activity با `exported=false` قابل مشاهده است.
- در production فقط کاربری که نقش موجود `superAdministrator` او تأیید شده باشد اجازه ورود و تغییر flag دارد.
- UI عادی و کاربران بدون دسترسی support نباید control یا مقدار flag را ببینند یا تغییر دهند.
- کنترل نباید PIN، client secret یا credential تازه‌ای hard-code کند.

## Lifecycle

هر flag در Migration Record دارای owner، آغاز rollout، exit condition و removal condition است. پس از پایان rollout و قبولی parity، flag و مسیر XML در cleanup همان مقصد حذف می‌شوند.

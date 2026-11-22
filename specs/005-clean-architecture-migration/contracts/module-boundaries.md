# Contract: مرزهای ماژول و مالکیت runtime

## Dependency rules

1. `:app` می‌تواند feature و core را compose کند؛ feature نباید `:app` را import کند.
2. `:feature:*` به feature دیگر وابسته نمی‌شود.
3. domain هر feature فقط Kotlin/Coroutines contractهای مجاز را می‌بیند.
4. data implementation به domain همان feature و core platform contractها وابسته است.
5. `:core:ui` هیچ business model یا data implementation را import نمی‌کند.
6. Android entry pointها effect را اجرا می‌کنند ولی business decision نمی‌گیرند.

## Runtime ownership

| Capability | Contract owner | Implementation owner | Lifecycle owner |
|---|---|---|---|
| selected-server HTTP | `:core:network` | `:core:network` | application/session scope |
| token/session | `:core:security` | Keystore/AppAuth adapters | application/session scope |
| track persistence | `:core:database` | legacy ContentProvider adapter | repository scope |
| GPS recording | tracking contract | Android foreground service adapter | service + explicit mission commands |
| MapLibre | `:core:map` | MapLibre adapter | route/container lifecycle |
| camera/location request | violations contract | Android launcher/coordinator adapter | Activity/route lifecycle |
| FCM/SignalR | notifications contracts | platform services/clients | application foreground/session policy |

## Rollback contract

تا پایان gate هر slice، entry point قدیمی یا compatibility adapter باید بتواند بدون تغییر schema/backend دوباره فعال شود. حذف legacy فقط پس از ثبت parity evidence مجاز است.

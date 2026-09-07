# Migration Inventory

این فایل source of truth وضعیت مهاجرت است و در T001 با تمام مقصدهای CODEMAP/Manifest تکمیل می‌شود.

| Screen/Flow | Current UI | Risk | Material Bridge | Target | Status | Screenshot | Behavioral Evidence | Visual Intent Delta | Rollback |
|---|---|---|---|---|---|---|---|---|---|
| FaraZamanTheme | Compose M3 | Foundation | None | Evolved single Theme | Not Started | Pending | Pending | Pending | Preserve current API |
| Role selection | Compose M3 | Low | Direct styled M3 | Design System consumer | Not Started | Pending | Pending | Pending | Code/version |
| App update UI | Compose M3 | Medium | Direct styled M3 | Design System consumer | Not Started | Pending | Pending | Pending | Code/version |
| Contractor flow | XML + debug previews | Medium | Material Views | First production migration | Not Started | Pending | Pending | Pending | Code/version |
| Support/list flows | XML/Fragments | Medium | Material Views | Compose after Contractor gate | Not Started | Pending | Pending | Pending | Code/version |
| Supervisor shell | XML/Fragments | Medium | Material Views | Responsive Compose shell | Not Started | Pending | Pending | Pending | Code/version |
| Map/camera/GPS/track | XML/native views | System-Critical | Material Views/native SDK | Compose host + adapters | Not Started | Pending with deterministic fake | Pending | Pending | Protected local runtime flag |

## Required fields before In Progress

Owner، exact source paths، state/events، side effects، dependencies، baseline screenshots/scenarios، bridge removal condition، coexistence window و rollback steps.

Behavior فعلی مرجع Behavioral Parity است. ظاهر XML فقط baseline مقایسه است؛ Compose مقصد از Farazaman Material 3 Design System پیروی می‌کند و هر اختلاف intentional در ستون Visual Intent Delta ثبت می‌شود.

## Contractor Architecture Gate

Support/Supervisor production migration فقط پس از ثبت قبولی State/Event، Design System dependency، preview matrix، RTL/dark/accessibility، Behavioral Parity و intentional visual deltas در این فایل آغاز می‌شود.

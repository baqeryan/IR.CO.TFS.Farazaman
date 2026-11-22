# Architecture migration approval

**Feature**: `005-clean-architecture-migration`  
**Approval date**: 2026-09-30  
**Approver**: repository owner/user in the active Codex session  
**Approval evidence**: explicit instruction «شروع کن انجام تسک ها» after reviewing the generated Spec Kit artifacts.

## Approved scope

- Incremental migration from the single `:app` module toward the module graph in `plan.md`.
- Strangler-style adapters over existing production mechanisms before moving implementations.
- Phase-by-phase delivery with Debug, Release, data-retention and lifecycle gates.
- Preservation of the concurrent `004-compose-ui-migration` work and all unrelated dirty files.

## Owners

| Area | Migration owner | Existing runtime owner |
|---|---|---|
| Gradle/module graph | Architecture migration | `settings.gradle`, root/app Gradle files |
| Authentication/session | Auth slice | `LoginActivity`, AppAuth and token/session helpers |
| Mission/tracking | Supervisor/driver slices | Activities/controllers and `GPSLogger` |
| Persistence | Core database adapter | `TrackContentProvider`, `DatabaseHelper`, `DataHelper` |
| System integrations | Map/location/violation slices | Activities, coordinators and platform services |
| Notifications/realtime | Notifications slice | FCM service, SignalR client and application lifecycle |

## Rollout

1. Freeze current contracts and measurements.
2. Add compile-time boundaries without changing runtime behavior.
3. Migrate Auth/Roles as the first production slice.
4. Migrate Supervisor, Driver/Tracking, system integrations and remaining features independently.
5. Remove a legacy owner only after its parity record is complete.

## Rollback

- Before each slice gate, keep the current Activity/Service/repository entry point usable.
- Revert composition wiring to the existing implementation without changing backend contracts or storage schema.
- Never roll back by clearing application data, deleting tracks, changing signing identity or changing provider authority.
- Stop the migration if selected-server routing, login, track retention, Release minification or lifecycle ownership regresses.

## Approval constraints

- This approval does not authorize speculative Contractor APIs, a Room migration, signing changes, or unrelated cleanup.
- Runtime smoke tests that require a configured municipality/account or physical device remain explicit release gates.

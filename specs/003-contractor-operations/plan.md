# Implementation Plan: Contractor Operations

**Branch**: `003-contractor-operations` | **Date**: 2026-09-07 | **Spec**: [spec.md](spec.md)

## Summary

Extend the existing Contractor role into an RTL field-operations shell. Contractor entry first uses a dedicated work-area selection screen based on the proven supervisor pattern, then opens a Contractor home with operation terminology, an action form, and a correction queue. Backend-dependent behavior uses an explicit unavailable gateway until contracts arrive; no endpoint or payload is invented.

## Technical Context

**Language/Version**: Kotlin/Java 17

**Primary Dependencies**: AndroidX, Material Components, ViewBinding, Hilt, existing camera/map utilities

**Storage**: Default SharedPreferences only for selected Contractor work-area presentation state; server remains authoritative once contracts exist

**Testing**: JUnit 4 unit tests and Android compile/resource validation

**Target Platform**: Android 6+ (minSdk 23, targetSdk 34), Persian RTL, light/dark

**Project Type**: Single-module Android application

**Performance Goals**: Local navigation feedback within 1 second; no blocking file/network work on main thread

**Constraints**: Runtime `BASE_URL`, existing auth, no speculative endpoint/DTO, R8-safe Release, no Supervisor/Driver behavior changes

**Scale/Scope**: One role route, three Contractor surfaces, one shared draft form pattern, backend seams

## Constitution Check

- PASS — Changes are additive and do not rewrite Supervisor or Driver flows.
- PASS — Existing authentication and selected municipality remain authoritative.
- PASS — No unknown backend contract is invented; external operations report unavailable until supplied.
- PASS — Existing camera, map, theme, and role mechanisms are reused.
- PASS — UI and validation receive focused tests and compile checks.
- PASS — Release/R8 validation remains required before publishing.

Post-design check: PASS. The design adds a small Contractor-specific boundary without parallel authentication, networking, or database sources of truth.

## Project Structure

### Documentation (this feature)

```text
specs/003-contractor-operations/
├── spec.md
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/contractor-gateway.md
└── tasks.md
```

### Source Code (repository root)

```text
app/src/main/java/Ir/co/tfs/farazaman/
├── activity/
│   ├── RoleSelectionActivity.kt
│   ├── ContractorWorkAreaSelectionActivity.kt
│   ├── ContractorOperationsActivity.kt
│   └── ContractorActionActivity.kt
└── contractor/
    ├── ContractorGateway.kt
    └── ContractorWorkContextStore.kt

app/src/main/res/layout/
├── activity_contractor_work_area_selection.xml
├── activity_contractor_operations.xml
└── activity_contractor_action.xml
```

**Structure Decision**: Keep the feature inside the existing module and Activity/layout navigation. A narrow Contractor package owns backend readiness and selected context.

## Complexity Tracking

No constitution violations.

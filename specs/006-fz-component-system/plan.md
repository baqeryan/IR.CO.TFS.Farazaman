# Implementation Plan: FaraZaman Component System

**Branch**: `[006-fz-component-system]` | **Date**: 2026-09-30 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/006-fz-component-system/spec.md`

## Summary

Build an incremental, FaraZaman-branded Compose component system whose public APIs use `Fz*`, covers the user-facing MudBlazor catalog by native-mobile outcome, and standardizes size, variant, semantics, RTL, dark mode and state. Extend the existing `core/ui` design system instead of importing a web library or replacing production screens. Deliver foundations and high-use controls first, retain adapters for existing APIs, then add layout/navigation, forms, feedback and advanced data components with a catalog and automated contract tests.

## Technical Context

**Language/Version**: Kotlin 1.9.22, Java/Kotlin 17

**Primary Dependencies**: Jetpack Compose Material 3, AndroidX, existing `core:ui`, Compose BOM already pinned by the project

**Storage**: N/A; components are state-driven and do not own application persistence

**Testing**: JUnit 4, Compose UI tests, Robolectric, Roborazzi screenshot verification, existing design-drift tests

**Target Platform**: Android minSdk 23, target/compile SDK 34; phones and supported tablets; Persian RTL first

**Project Type**: Single Android application with reusable `core:ui` Android library

**Performance Goals**: Smooth 60fps interaction for ordinary controls; large collection components compose only visible content; no avoidable recomposition from unstable public state

**Constraints**: Preserve existing screens and APIs during migration; Iran Yekan typography; FaraZaman palette; dark mode; RTL; minimum 48dp interactive target; R8/resource shrinking safe; no backend or persistence changes

**Scale/Scope**: Approximately 80 reference catalog families normalized into roughly 55 public native component families plus platform adapters, examples and tests

## Constitution Check

### Pre-design gate

- **Preserve Proven Functionality**: PASS. Existing components remain supported; migration adapters prevent a flag-day rewrite.
- **Backend Compatibility**: PASS. UI-only work changes no endpoint, payload, token or base URL behavior.
- **Analyze Before Architecture**: PASS. Existing `core/ui`, Compose screens and design-drift tests were inspected; extension is lower risk than a new UI framework.
- **Kotlin/Compose Conventions**: PASS. State hoisting, stable models, semantics, RTL and theme tokens are explicit contracts.
- **Reuse Production Mechanisms**: PASS. Existing theme, palette, typography and navigation ownership remain authoritative.
- **Incremental Delivery**: PASS. Five independently testable user-story phases are defined.
- **Separate Refactoring**: PASS. Component creation precedes and is separated from per-screen migrations.
- **Migration Impact**: PASS. See [migration-impact.md](migration-impact.md).
- **Existing Behavior as Specification**: PASS. Compatibility wrappers preserve current `Fz*` call sites.
- **Green Build/Tests**: PASS as a required gate for every phase.

### Post-design gate

PASS. Contracts introduce no new persistence, network, module or navigation source of truth. Advanced reference items without a native analogue are explicitly mapped rather than emulated with browser behavior.

## Project Structure

### Documentation (this feature)

```text
specs/006-fz-component-system/
├── spec.md
├── plan.md
├── research.md
├── data-model.md
├── migration-impact.md
├── quickstart.md
├── contracts/
│   └── component-catalog.md
├── checklists/
│   └── requirements.md
└── tasks.md
```

### Source Code

```text
core/ui/src/main/java/Ir/co/tfs/farazaman/presentation/design/
├── components/
├── state/
└── theme/

core/ui/src/test/java/Ir/co/tfs/farazaman/presentation/design/
├── components/
└── theme/

app/src/debug/java/Ir/co/tfs/farazaman/presentation/design/catalog/
└── ...

app/src/test/java/Ir/co/tfs/farazaman/presentation/design/
└── ...
```

**Structure Decision**: Extend the existing `core:ui` public design system and keep the interactive catalog in Debug sources so Release size is not inflated. Production screens migrate separately through existing call sites.

## Complexity Tracking

No constitution violations require exceptions. The breadth is managed through staged component families and compatibility adapters.


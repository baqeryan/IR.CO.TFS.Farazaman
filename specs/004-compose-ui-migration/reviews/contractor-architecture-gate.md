# Contractor Architecture Review

Date: 2026-09-08

## Scope

Reviewed the Contractor Compose migration, including work-area selection, operations, action form, corrections state, previews, and screenshot coverage.

## Findings

- Screens receive immutable UI state and emit typed events.
- Activity containers own navigation, camera launch, validation submission, and lifecycle concerns.
- Shared visual primitives are provided by the Farazaman Design System (`FzScaffold`, `FzCard`, `FzButton`, `FzTextField`, and feedback states).
- Contractor-specific state and labels remain outside the Design System.
- Backend-dependent Contractor gateway remains explicitly unconfigured; no speculative endpoint was introduced.
- Preview and Roborazzi coverage exists for the migrated states.

## Decision

**Accepted with follow-up:** Support and Supervisor migration may proceed incrementally. MapLibre, camera, and GPS screens remain behind their existing lifecycle bridges until dedicated instrumentation coverage is available.

## Follow-up

- Add instrumentation coverage before replacing MapLibre host layouts.
- Add full Contractor journey test when backend service contracts are available.

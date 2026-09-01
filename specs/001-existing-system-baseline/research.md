# Phase 0 Research: Existing-System Baseline

## Decision 1: Preserve the Single-Module Hybrid Architecture

**Decision**: Document `:app` and its current package boundaries as authoritative. Treat XML,
Compose, Activities, Fragments, ViewModels, repositories, helpers, and legacy Java as coexisting
production architecture.

**Rationale**: `settings.gradle`, active build configuration, manifest, and mapped sources confirm
one application module and a transitional architecture. The constitution forbids unjustified
rewrites.

**Alternatives considered**: A multi-module or uniform Clean Architecture target was rejected
because it would be a proposed migration, not current-system planning.

## Decision 2: Describe Mixed State Ownership Precisely

**Decision**: Record LiveData, StateFlow, Compose state/callbacks, Activity/Fragment fields,
preferences, Cursors, and helper-owned state as current mechanisms.

**Rationale**: Actual ViewModels use both LiveData and StateFlow, while legacy screens retain direct
state and orchestration. No single state standard governs the whole app.

**Alternatives considered**: Declaring one preferred state architecture was rejected because it
would misrepresent working code and imply refactoring.

## Decision 3: Keep Current Data Boundaries Authoritative

**Decision**: Treat SQLite ContentProvider storage, preferences, encrypted token storage, and files
as production data boundaries. Treat Room as inactive scaffolding.

**Rationale**: Active providers/helpers and callers use the legacy database and preferences; Room
providers are commented out.

**Alternatives considered**: A Room migration was rejected because no approved migration is present.

## Decision 4: Distinguish Server-Populated UI from General SDUI

**Decision**: Use “server-populated packaged UI” for business responses and “legacy downloadable
tracking layout” for the custom-layout facility. State that no general SDUI engine is confirmed.

**Rationale**: Searches found no SDUI schema, renderer registry, or general component/action engine.
Business DTOs populate compiled screens. `AvailableLayouts`, `DownloadCustomLayoutTask`,
`UserDefinedLayout`, and listener classes form a separate limited legacy mechanism.

**Alternatives considered**: Calling all dynamic form choices SDUI was rejected because it would
overstate runtime control of screen structure.

## Decision 5: Inventory Actions Without Inventing a Registry

**Decision**: Document native actions by dispatch owner: Compose callback, screen click listener,
Intent/Fragment routing, supervisor helper/controller, or legacy custom-layout listener.

**Rationale**: No centralized native-action registry class or key-to-handler map was found.

**Alternatives considered**: Designing a new registry was rejected as out-of-scope architecture.

## Decision 6: Inventory Endpoints Without Centralizing Them

**Decision**: Document Retrofit annotations, selected-server URL helpers, and fixed service hosts as
the endpoint inventory. Preserve their distinct ownership.

**Rationale**: Endpoint definitions are distributed across `data/api`, `service/remote`, update/map
configuration, and legacy URL construction. Runtime host selection is a critical compatibility
boundary.

**Alternatives considered**: A centralized endpoint registry was rejected because it does not exist
and would change architecture.

## Decision 7: Separate Technical Debt from Required Architecture

**Decision**: List inconsistency, duplication, weak typing, partial DI/repositories, and test gaps in
a non-actionable technical-debt section while explicitly retaining compatibility boundaries.

**Rationale**: The user requested separation, and the constitution requires existing behavior and
mechanisms to remain authoritative absent explicit migration.

**Alternatives considered**: Converting debt observations into tasks was rejected because this plan
does not authorize implementation or refactoring.

## Decision 8: Use Repository Evidence as the Resolution Boundary

**Decision**: Resolve technical unknowns through `PROJECT_CONTEXT`, `CODEMAP`, `RELEASE_NOTES`, Gradle
files, manifest, DI modules, ViewModels, repositories, API interfaces, persistence, custom layouts,
and action helpers. External deployment behavior remains outside scope.

**Rationale**: This provides sufficient evidence for a current architecture plan without claiming
live server configuration or telemetry.

**Alternatives considered**: Live backend inspection was not required and could expose environment-
specific behavior beyond repository scope.

All planning unknowns are resolved. No `NEEDS CLARIFICATION` item remains.

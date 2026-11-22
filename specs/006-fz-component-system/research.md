# Research: FaraZaman Component System

## Decision 1: Treat MudBlazor as a coverage reference, not a runtime dependency

**Decision**: Recreate user outcomes with native `Fz*` Compose APIs and the existing theme. Do not import or mechanically translate Blazor/CSS/DOM implementation.

**Rationale**: The reference library targets web rendering while FaraZaman is a native Android application. Native focus, back handling, touch targets, insets, lifecycle and accessibility differ materially.

**Alternatives considered**: WebView hosting and literal API parity were rejected for accessibility, offline, lifecycle and visual-integration risk.

## Decision 2: One shared size and variant vocabulary

**Decision**: Use `FzComponentSize` (`Small`, `Medium`, `Large`) across actions, chips, fields and selection controls. Use family-appropriate variants backed by semantic intent tokens.

**Rationale**: Current components encode one-off dimensions, which caused the reported button and chip inconsistencies.

**Alternatives considered**: Arbitrary dimensions at every call site and a single fixed size were rejected.

## Decision 3: State hoisting with optional state holders

**Decision**: Every interactive component accepts current state and emits callbacks. Convenience state holders remain saveable and caller-owned.

**Rationale**: This matches existing Compose state/event screens and keeps components independent of repositories, preferences and navigation.

**Alternatives considered**: Hidden internal mutable state and ViewModel dependencies in primitives were rejected.

## Decision 4: Catalog family normalization

**Decision**: Implementation-internal or browser-only entries are normalized: BreakpointProvider/Hidden become `FzResponsive`; RTLProvider/ThemeProvider become existing `FaraZamanTheme`; Element/Main/Render become layout primitives; ExitPrompt becomes back confirmation; FocusTrap is internal to modal surfaces; Hotkey becomes key-action handling; PageContentNavigation becomes mobile section navigation.

**Rationale**: This preserves the user outcome without exposing meaningless DOM abstractions.

**Alternatives considered**: Empty wrappers were rejected because they provide no product value.

## Decision 5: Test pyramid and catalog

**Decision**: Pure state receives unit tests; public visual families receive semantics tests; representative states receive light/dark RTL screenshots; design-drift tests prevent raw reusable styling literals.

**Rationale**: The current project already uses unit tests, Roborazzi and design-drift checks.

**Alternatives considered**: Screenshot-only and unit-only testing were rejected.

## Primary reference

- MudBlazor official source component directory: `https://github.com/MudBlazor/MudBlazor/tree/dev/src/MudBlazor/Components`
- MudBlazor is MIT licensed; implementation is original native code using public behavioral concepts with `Fz*` names.


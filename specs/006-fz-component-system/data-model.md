# Data Model: FaraZaman Component System

## FzComponentSize

- Values: `Small`, `Medium`, `Large`.
- Resolves family-specific container height, icon size, typography and content padding.
- Interactive resolved height preserves a minimum 48dp touch target.

## FzSemanticIntent

- Values: neutral, primary, info, success, warning, error.
- Resolves container, content, border and disabled colors from the active theme.

## FzSelectionMode

- Values: none, single, toggle, multiple.
- Single retains one selected value once available; toggle allows zero or one; multiple allows a stable set.

## FzSelectionState<T>

- Fields: mode, available stable values, selected values, enabled/read-only.
- Invariants: selected values exist in available values; single/toggle contain at most one value; read-only emits no mutation.
- Transitions: select, deselect, toggle, replace available values, clear where permitted.

## FzFieldState

- Fields: value presentation, label, supporting text, error, enabled, read-only, required, focused.
- Validation is caller-supplied; components display the resulting state.

## FzDataCollectionState<T>

- Fields: items, stable key, loading, empty/error state, selection, sort/filter/page descriptors.
- Collection components do not fetch or persist data.

## FzOverlayState

- Fields: visible, dismissible, modal, anchor where applicable, focus/back policy.
- State transitions are idempotent open/dismiss requests emitted to the owner.


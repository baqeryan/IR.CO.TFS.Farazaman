# Feature Specification: FaraZaman Component System

**Feature Branch**: `[006-fz-component-system]`

**Created**: 2026-09-30

**Status**: Approved for incremental implementation

**Input**: User description: "Implement the complete MudBlazor component catalog for this project with project-specific Fz names instead of Mud names."

## User Scenarios & Testing

### User Story 1 - Consistent foundations and actions (Priority: P1)

As a feature developer, I can use a documented FaraZaman component contract for sizing, variants, colors, shapes, spacing, state and interaction, so buttons, chips and action controls no longer diverge between screens.

**Why this priority**: Buttons and chips are already production pain points and every later component depends on the same foundations.

**Independent Test**: Render the component catalog in Persian light/dark themes and verify Small, Medium and Large buttons, icon buttons, floating actions, chips and chip sets in enabled, selected, loading and disabled states.

**Acceptance Scenarios**:

1. **Given** any action component, **When** its size changes, **Then** height, icon, typography and padding change together according to the shared size contract.
2. **Given** a single- or multi-selection chip set, **When** users select and deselect items, **Then** the selected values and visual states remain synchronized.
3. **Given** Persian RTL and dark mode, **When** action components render, **Then** icon placement, text direction and contrast remain correct.

---

### User Story 2 - Consistent layout, navigation and content display (Priority: P2)

As a feature developer, I can compose screens from FaraZaman layout, navigation and content-display components without recreating padding, elevation, responsive or RTL rules.

**Why this priority**: Current screens repeatedly implement cards, tabs, navigation, lists and spacing and therefore drift visually.

**Independent Test**: Build a catalog screen containing every layout/navigation/display component and verify it at compact and expanded widths in RTL light/dark themes.

**Acceptance Scenarios**:

1. **Given** nested layout components, **When** the available width changes, **Then** content reflows without clipping or overlapping controls.
2. **Given** tabs, menus, drawers, breadcrumbs or pagination, **When** selection changes, **Then** exactly one current destination is exposed where the contract requires single selection.
3. **Given** long Persian content, **When** it is shown in cards, lists, tables, timelines or trees, **Then** text wrapping and truncation follow the documented component policy.

---

### User Story 3 - Validated forms and selection controls (Priority: P3)

As a feature developer, I can build consistent forms with FaraZaman inputs, validation and selection controls, including locale-appropriate date and time entry.

**Why this priority**: Field operations depend on reliable forms, but these components require the foundational action and layout contracts first.

**Independent Test**: Complete a sample Persian form containing text, numeric, autocomplete, select, checkbox, radio, switch, slider, rating, file and date/time controls and verify validation, keyboard and disabled/read-only states.

**Acceptance Scenarios**:

1. **Given** invalid input, **When** validation runs, **Then** the field exposes a readable error without losing the entered value.
2. **Given** a selectable control, **When** keyboard, touch or accessibility actions change it, **Then** state and semantics report the same value.
3. **Given** a Persian date/time workflow, **When** the user selects a value, **Then** the displayed value respects project locale and RTL rules.

---

### User Story 4 - Feedback, overlays and transient interaction (Priority: P4)

As a feature developer, I can present alerts, dialogs, sheets, snackbars, tooltips, loading states, overlays and expandable content through one consistent FaraZaman API.

**Why this priority**: Feedback patterns must be reliable and accessible but can be delivered after core controls and forms.

**Independent Test**: Trigger each feedback and overlay family from the catalog and verify dismissal, focus, back handling, timeout and state restoration.

**Acceptance Scenarios**:

1. **Given** a modal component, **When** it opens, **Then** focus and back handling remain contained until it is dismissed.
2. **Given** a transient message, **When** its timeout expires or the user acts, **Then** it dismisses exactly once.
3. **Given** reduced-motion preference, **When** expandable or progress content changes, **Then** the component remains understandable without relying on animation.

---

### User Story 5 - Data-rich and advanced components (Priority: P5)

As a feature developer, I can display and manipulate large or hierarchical field-operation datasets using consistent FaraZaman tables, grids, charts, trees, timelines, steppers, carousels and virtualized collections.

**Why this priority**: These are high-complexity components and should reuse all earlier interaction, layout and feedback primitives.

**Independent Test**: Exercise sorting, paging, selection, expansion and virtualization with representative Persian datasets and verify stable state and acceptable responsiveness.

**Acceptance Scenarios**:

1. **Given** a large collection, **When** users scroll, sort, filter or page it, **Then** visible state remains responsive and selection is preserved.
2. **Given** hierarchical data, **When** nodes expand or collapse, **Then** focus and selected node remain predictable.
3. **Given** an advanced component unavailable as a direct mobile concept, **When** developers use its FaraZaman equivalent, **Then** the documented mobile behavior provides the same user outcome.

### Edge Cases

- Very long Persian labels, mixed Persian/Latin/numeric content, and large accessibility font scales.
- Empty, loading, partial, error, disabled and read-only states.
- Rapid repeated taps, simultaneous selection updates and state restoration after recreation.
- Compact screens, landscape, display cutouts, system bars and software keyboard insets.
- Collections with duplicate labels but distinct stable identifiers.
- Components whose web behavior has no safe native mobile equivalent.

## Requirements

### Functional Requirements

- **FR-001**: The system MUST expose public project components with the `Fz` prefix and MUST NOT expose `Mud`-prefixed production APIs.
- **FR-002**: The catalog MUST cover the user-facing families represented by the reference catalog: alerts; app bars; autocomplete; avatars; badges; breadcrumbs; buttons and groups; cards/paper; carousel; charts; checkbox/radio/switch; chips and chip sets; collapse/expansion; color/date/time pickers; containers/grid/stack/spacer; data grid/table/pagination/virtualized collections; dialogs/message boxes; dividers; drawers/navigation/menu/link; file upload/drop zones; fields/forms/text/numeric/select inputs; icons/images; lists/tree/timeline; overlays/popovers/tooltips; progress/skeleton; rating/slider/toggle; scroll/swipe; split panels; steppers/tabs/toolbars; typography; responsive visibility and RTL/theme providers.
- **FR-003**: Every visual component MUST support Persian RTL and both light and dark appearances.
- **FR-004**: Interactive components MUST document and implement enabled, disabled, read-only, selected, pressed, focused and error states where applicable.
- **FR-005**: Action, input and selection components MUST support Small, Medium and Large sizes through one shared size vocabulary.
- **FR-006**: Components with visual alternatives MUST use a shared variant vocabulary and semantic intent colors rather than screen-specific literals.
- **FR-007**: Selection groups MUST support documented single, toggle and multiple selection modes where applicable.
- **FR-008**: All interactive components MUST expose an accessible name, role, state and minimum touch target.
- **FR-009**: Components MUST define policies for long text, empty content and large accessibility fonts without unintended overlap.
- **FR-010**: Every component family MUST include a discoverable catalog example covering normal and boundary states.
- **FR-011**: Every component family MUST include automated verification for its state contract, and shared styling MUST be protected against design drift.
- **FR-012**: Existing production `Fz` components MUST remain source-compatible during staged migration unless a documented adapter and migration task is supplied.
- **FR-013**: Existing screens MUST migrate incrementally and remain operational between phases.
- **FR-014**: Components requiring platform services, files, navigation, date/time or external data MUST receive behavior through callbacks/state and MUST NOT create a second application data source.
- **FR-015**: Web-only reference behaviors MUST be translated to a documented native-mobile equivalent or explicitly marked as not applicable with the replacement component identified.

### Key Entities

- **Component Contract**: Public name, family, sizes, variants, semantic intents, states, slots, events and accessibility behavior.
- **Component State**: Immutable visible and interaction state controlled by the caller or a documented state holder.
- **Selection Model**: Stable values, selection mode and selected set used by chips, toggles, tabs, lists and advanced data components.
- **Catalog Example**: A runnable example demonstrating supported states, RTL, light/dark and accessibility boundaries.
- **Migration Adapter**: A compatibility boundary allowing existing screens to adopt the new contract incrementally.

## Success Criteria

### Measurable Outcomes

- **SC-001**: 100% of catalog component families have an `Fz`-prefixed public contract, catalog example and automated contract verification.
- **SC-002**: All action, input and selection controls pass Small, Medium and Large sizing checks with touch targets of at least 48dp for interactive surfaces.
- **SC-003**: All catalog examples render without clipping at 320dp width and at 200% font scaling in Persian RTL.
- **SC-004**: All catalog examples pass light/dark contrast and state-legibility review with no unresolved critical findings.
- **SC-005**: Existing production screens compile and their applicable automated tests remain passing after every migration phase.
- **SC-006**: A developer can locate the appropriate component, supported states and a runnable example in under two minutes using the component catalog.

## Assumptions

- The reference catalog defines coverage and behavioral inspiration, not a requirement to reproduce browser-only implementation details.
- Native mobile conventions take precedence where browser and mobile interactions differ.
- Existing FaraZaman colors, Iran Yekan typography, Persian locale and RTL rules remain authoritative.
- Delivery is incremental; foundational and commonly used components are completed before advanced data-rich components.
- No backend API, authentication, persistence or municipality routing changes are required.


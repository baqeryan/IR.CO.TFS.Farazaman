# Public Component Catalog Contract

All public production components use the `Fz` prefix.

## Foundations

- `FaraZamanTheme`, `FzResponsive`, `FzTypography`, `FzIcon`, `FzImage`, `FzSpacer`, `FzDivider`, `FzSurface`, `FzContainer`, `FzRow`, `FzColumn`, `FzGrid`.

## Actions and selection

- `FzButton`, `FzIconButton`, `FzFloatingActionButton`, `FzButtonGroup`.
- `FzChip`, `FzChipSet`, `FzToggle`, `FzToggleGroup`.
- `FzCheckbox`, `FzRadio`, `FzRadioGroup`, `FzSwitch`, `FzSlider`, `FzRangeSlider`, `FzRating`.

## Navigation

- `FzTopAppBar`, `FzBottomNavigation`, `FzNavigationRail`, `FzDrawer`, `FzNavigationDrawer`, `FzNavigationMenu`, `FzMenu`, `FzBreadcrumbs`, `FzTabs`, `FzPagination`, `FzToolbar`, `FzLink`, `FzScrollToTop`, `FzSectionNavigation`.

## Content and layout

- `FzCard`, `FzSurface`, `FzList`, `FzListItem`, `FzAvatar`, `FzBadge`, `FzCarousel`, `FzCollapse`, `FzExpansionPanel`, `FzSplitPanel`, `FzTimeline`, `FzTreeView`, `FzStepper`, `FzSwipeArea`, `FzHighlightedText`.

## Forms and pickers

- `FzForm`, `FzField`, `FzTextField`, `FzNumericField`, `FzAutocomplete`, `FzSelect`, `FzFilePicker`, `FzDropZone`, `FzColorPicker`, `FzDatePicker`, `FzDateRangePicker`, `FzTimePicker`.
- `FzInputMask` is a field transformation contract rather than a visual component.

## Feedback and overlays

- `FzAlert`, `FzDialog`, `FzMessageBox`, `FzSnackbar`, `FzTooltip`, `FzPopover`, `FzOverlay`, `FzSkeleton`, `FzLinearProgress`, `FzCircularProgress`, `FzLoading`, `FzEmptyState`, `FzErrorState`.
- `FzBackConfirmation` covers ExitPrompt; modal components enforce focus containment internally.

## Data and visualization

- `FzSimpleTable`, `FzTable`, `FzDataGrid`, `FzVirtualizedList`.
- `FzChart` supports line, bar, pie/donut and sparkline presentations before catalog completion.

## Platform-normalized mappings

| Reference family | FaraZaman contract |
|---|---|
| AppBar | `FzTopAppBar` |
| BreakpointProvider, Hidden | `FzResponsive` |
| Paper | `FzSurface` |
| ThemeProvider, RTLProvider | `FaraZamanTheme` |
| Element, Main, Render | Native layout primitives; no empty public wrapper |
| Input, InputControl | `FzField` and typed inputs |
| Picker | Typed picker contracts |
| FocusTrap | Internal modal behavior |
| Hotkey | `FzKeyAction` modifier/contract |
| PageContentNavigation | `FzSectionNavigation` |
| ExitPrompt | `FzBackConfirmation` |
| Virtualize | `FzVirtualizedList` |

## Cross-family rules

- Applicable controls accept `FzComponentSize`; default is `Medium`.
- State is caller-owned and mutations emit through callbacks.
- Icon-only actions require accessible labels.
- Appearance resolves through theme tokens and semantic intent.
- Existing public `Fz*` APIs remain compatible during migration.

## Completion matrix

`Implemented` means a public native `Fz*` contract exists. `Platform-mapped` means the browser-only concept is intentionally delivered through the named Android/Compose equivalent. Catalog examples are grouped into the five Debug destinations: Actions, Layout, Forms, Feedback and Data.

| Reference family | Status | Catalog | Automated verification |
|---|---|---|---|
| Alerts and message boxes | Implemented: `FzAlert`, `FzMessageBox` | Feedback | `FzFeedbackComponentsTest` |
| App bars and toolbars | Implemented: `FzTopAppBar`, `FzToolbar` | Layout | `FzLayoutComponentsTest` |
| Autocomplete and select | Implemented: `FzAutocomplete`, `FzSelect` | Forms | `FzFormComponentsTest` |
| Avatars, badges, icons and images | Implemented | Layout | `FzLayoutComponentsTest` |
| Breadcrumbs, menus and links | Implemented | Layout | `FzLayoutComponentsTest` |
| Buttons, icon buttons, FAB and groups | Implemented | Actions | `FzActionComponentsTest`, `FzButtonContractTest` |
| Cards and paper | Implemented: `FzCard`, `FzSurface` | Layout | `FzLayoutComponentsTest` |
| Carousel, split panel and swipe | Implemented | Layout | `FzLayoutComponentsTest` |
| Charts | Implemented: line, bar, pie, donut, sparkline | Data | `FzDataComponentsTest` |
| Checkbox, radio and switch | Implemented | Actions | `FzActionComponentsTest` |
| Chips and chip sets | Implemented | Actions | `FzActionComponentsTest`, `FzChipSetTest` |
| Collapse and expansion | Implemented | Layout | `FzLayoutComponentsTest` |
| Color, Persian date/range and time pickers | Implemented | Forms | `FzFormComponentsTest` |
| Containers, grid, stack and spacer | Implemented | Layout | `FzLayoutComponentsTest` |
| Data grid, table, pagination and virtualized list | Implemented | Data | `FzDataComponentsTest` |
| Dialogs and back confirmation | Implemented | Feedback | `FzFeedbackComponentsTest` |
| Dividers and surfaces | Implemented | Layout | `FzLayoutComponentsTest` |
| Drawers and navigation | Implemented | Layout | `FzLayoutComponentsTest` |
| File picker and drop zone | Implemented | Forms | `FzFormComponentsTest` |
| Fields, forms, text and numeric inputs | Implemented | Forms | `FzFormComponentsTest` |
| Lists, tree and timeline | Implemented | Layout | `FzLayoutComponentsTest` |
| Overlay, popover and tooltip | Implemented | Feedback | `FzFeedbackComponentsTest` |
| Progress, loading and skeleton | Implemented | Feedback | `FzFeedbackComponentsTest` |
| Rating, slider and toggle | Implemented | Actions | `FzActionComponentsTest` |
| Steppers, tabs and section navigation | Implemented | Layout | `FzLayoutComponentsTest` |
| Responsive visibility | Platform-mapped: `FzResponsive` | Layout | `FzLayoutComponentsTest` |
| Theme and RTL providers | Platform-mapped: `FaraZamanTheme` | All | `FzCatalogScreenshotTest` |
| Paper/DOM Element/Main/Render | Platform-mapped: native Compose layout primitives | Layout | `ComposeDesignDriftTest` |
| Focus trap | Platform-mapped: internal modal behavior | Feedback | `FzFeedbackComponentsTest` |
| Hotkey | Platform-mapped: `FzKeyAction` contract | Actions | `FzActionComponentsTest` |
| Exit prompt | Platform-mapped: `FzBackConfirmation` | Feedback | `FzFeedbackComponentsTest` |

The central `fzCatalogDestinations` registry asserts that all five catalog destinations are present exactly once. `FzCatalogScreenshotTest` covers the catalog at compact width in Persian RTL with light/dark and 100%/200% font-scale combinations.

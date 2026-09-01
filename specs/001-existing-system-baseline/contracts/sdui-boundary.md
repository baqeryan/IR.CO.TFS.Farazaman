# Server-Driven UI Boundary

## General SDUI Engine Status

No general SDUI engine is confirmed. The repository has no common remote screen schema, component
registry, renderer tree, action registry, or endpoint registry that allows the backend to define
arbitrary application screens.

## Server-Populated Packaged UI

Current business services supply data that changes content and available choices inside compiled
screens:

- municipality list and per-city login configuration;
- user roles and role routing;
- work areas/zones;
- employers and contracts;
- daily cleaning items and mission details;
- violation form choices;
- GIS layers, roads/addresses, and employer-fence geometry;
- update notes and mandatory/optional policy.

The client owns layout, widgets, validation, navigation, and native action wiring. DTOs and
feature-specific mapping decide how server values appear.

## Legacy Downloadable Tracking Layouts

The inherited custom-layout subsystem is a limited remote-layout mechanism:

1. `AvailableLayouts` resolves a configured/default repository and obtains layout names.
2. `DownloadCustomLayoutTask` retrieves the selected layout.
3. `CustomLayoutsUtils` and `UserDefinedLayout` parse/render pages and tracking buttons.
4. Listener classes execute supported native tracking/note/media/sensor actions.
5. Preferences retain repository and selected-layout configuration.

This subsystem is legacy, separately networked, and specific to tracking controls. Active production
use and governance are unknown. It must not be presented as the architecture for current business
forms without further evidence.

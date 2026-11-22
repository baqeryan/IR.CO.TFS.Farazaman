# Migration Impact Record

## Affected components

- Existing design components under `core/ui/.../design/components/` gain shared sizing and state contracts.
- Compose screens migrate incrementally after their required `Fz*` family is stable.
- XML/legacy screens are not rewritten by this feature.

## Compatibility

- Existing function names and defaults remain source-compatible wherever practical.
- New parameters are appended with defaults.
- Unavoidable replacements require a deprecated forwarding overload during migration.
- No API, authentication, database or preference contract changes.

## Rollout sequence

1. Foundations and compatibility adapters.
2. Action and selection primitives.
3. Layout/navigation/display.
4. Forms and pickers.
5. Feedback/overlays.
6. Advanced collections and visualization.
7. Screen-by-screen adoption after catalog verification.

## Coexistence

Old call sites and new component capabilities coexist. A screen migration does not require unrelated screens to migrate.

## Risks and mitigations

- Signature incompatibility: defaulted appended parameters and forwarding overloads.
- RTL/dark-mode regressions: semantics and screenshot tests.
- Release size: Debug-only catalog.
- Collection performance: lazy/virtualized implementations and representative scenarios.

## Rollback

Each family is delivered independently. Production screens can remain on the prior compatible API or revert their isolated migration without removing the new library.


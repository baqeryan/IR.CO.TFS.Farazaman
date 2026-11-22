# Quickstart Validation

## Foundations

1. Run `.\gradlew.bat :core:ui:testDebugUnitTest`.
2. Verify size, semantic-intent and selection-state contract tests pass.

## Integration

1. Run `.\gradlew.bat :app:testDebugUnitTest`.
2. Run `.\gradlew.bat :app:assembleDebug`.
3. Open the Debug component catalog.
4. Switch light/dark mode and verify Persian RTL examples.
5. Check compact width and increased system font scale.

## Per-family validation

For each family in [component-catalog.md](contracts/component-catalog.md):

1. Confirm the public `Fz*` contract exists.
2. Confirm normal and relevant disabled/selected/loading/error examples exist.
3. Confirm semantics expose label, role and state.
4. Confirm reusable styling uses tokens.
5. Run its Compose test and representative screenshot verification.

## Release safety

Run `.\gradlew.bat :app:lintRelease :app:assembleRelease` after each delivery phase. Confirm the Debug-only catalog is absent from Release.


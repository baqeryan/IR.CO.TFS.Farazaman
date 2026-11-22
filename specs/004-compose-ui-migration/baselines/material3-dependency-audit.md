# Material 3 and Screenshot Dependency Audit

## Confirmed toolchain

- Kotlin Gradle plugin: `1.9.22`
- Android Gradle Plugin: `8.4.2`
- Gradle wrapper: `8.13`
- Compose Compiler: `1.5.10`
- Compose BOM: `2024.02.02`
- Material 3: BOM-managed stable artifact

The existing Material 3 version provides the APIs required by the Foundation. No Kotlin, AGP, compiler or Compose upgrade is part of this migration.

## Screenshot spike

Roborazzi `1.29.0` is pinned with Robolectric `4.13`, Compose UI test support and native graphics. This release predates the newer AGP/Kotlin baselines and exposes the variant `recordRoborazziDebug` and `verifyRoborazziDebug` tasks required by the plan without forcing a toolchain upgrade.

The plugin and screenshot dependencies are test-only except for the Gradle plugin itself. Production and Release runtime classpaths do not contain screenshot test libraries. Screenshot tests use deterministic fixtures and `@GraphicsMode(NATIVE)`; actual MapLibre/camera/location/device smoke remains a separate gate.

## Compatibility fallback

If Gradle task discovery or the first native-graphics capture fails on this pinned combination, implementation stops at T006. An AGP or Kotlin upgrade must be proposed and validated as a separate migration; the toolchain is not upgraded implicitly as part of UI work.

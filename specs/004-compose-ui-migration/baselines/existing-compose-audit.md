# Existing Compose Audit

## FaraZamanTheme

- Source: `presentation/compose/FaraZamanTheme.kt`; Material 3 is already used and RTL is forced centrally.
- Light/dark schemes exist, but colors, typography and shapes are private/hard-coded and no Farazaman extension tokens or replaceable configuration exist.
- Evolution path: move the source of truth into `presentation/design/theme`, keep a compatibility import/wrapper temporarily, and do not introduce a second palette.

## RoleSelectionScreen

- Already exposes immutable `RoleSelectionUiState` and typed `RoleSelectionEvent`; Activity owns role/navigation effects.
- Directly styles Material `Button`, `Card` and icon controls; embeds font, spacing, shapes, sizes and resource colors.
- Has a main-source `@Preview` plus debug previews. Destination must consume Fz components/tokens while preserving supervisor/driver/contractor visibility, superAdministrator behavior, no-access/logout, notification badge, RTL back direction and live dark mode.

## AppUpdateUi

- State is supplied and typed events are emitted; install/download effects stay in `SplashActivity`.
- Direct Material dialog/button/progress primitives and local spacing remain. Mandatory update dismissal rules and all update states are behavioral parity requirements.
- Existing debug previews cover states but require the shared matrix and screenshot baseline.

## SplashScreen

- Receives display state but owns a local entrance animation side effect. This is presentation-only and must follow the central motion/reduced-motion policy.
- Font, size and animation timings are hard-coded. It uses Material 3 theme colors and has debug previews.

## Audit conclusion

Reuse and evolve all four surfaces. No existing Compose screen requires a wholesale rewrite before Foundation; compatibility aliases keep call sites compiling while styles move behind the Design System.


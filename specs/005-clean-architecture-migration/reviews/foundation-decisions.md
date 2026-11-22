# Foundation implementation decisions

## Core model uses Kotlin/JVM

Task T009 originally described `:core:model` as an Android library. The first dependency compile demonstrated that an Android variant cannot be consumed by the JVM-only `:core:testing` module without making testing Android-specific.

The module was therefore implemented as Kotlin/JVM with Java 17. This is a deliberate strengthening of FR-003: shared domain models cannot import Android APIs, and both Android feature modules and JVM test fixtures can consume the same artifact.

## Validation

- `:core:common:compileKotlin`: PASS
- `:core:model:compileKotlin`: PASS
- `:core:ui:compileDebugKotlin`: PASS
- `:core:testing:compileKotlin`: PASS
- `:app:assembleDebug`: PASS after wiring all four modules

## Design-system transfer

After explicit owner approval, the existing design-system/theme sources were physically transferred
to `:core:ui` while retaining their packages. This preserves all existing imports and avoids a
parallel theme source. The Persian font and the two resources owned by `FzPageTemplate` moved with
the source, and their `R` imports now point to `Ir.co.tfs.farazaman.core.ui.R`.

Validation after the transfer:

- `:core:ui:compileDebugKotlin`: PASS
- `:app:assembleDebug`: PASS
- Design-system, theme replaceability, Compose architecture and new Clean Architecture tests: PASS

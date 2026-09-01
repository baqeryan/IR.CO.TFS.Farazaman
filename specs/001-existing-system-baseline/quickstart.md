# Quickstart: Validate the Existing-System Baseline

This guide validates documentation against the repository. It does not modify application source or
require live credentials.

## Prerequisites

- Repository root: `C:\Users\Bagherian\source\repos\faraZaman-v2`
- JDK 17 and Android SDK 34 for build validation
- On Windows, use `gradlew.bat`
- Do not print or inspect secret values from local/signing configuration

## 1. Confirm Feature Artifacts

Verify these files exist:

- `specs/001-existing-system-baseline/spec.md`
- `specs/001-existing-system-baseline/plan.md`
- `specs/001-existing-system-baseline/research.md`
- `specs/001-existing-system-baseline/data-model.md`
- `specs/001-existing-system-baseline/contracts/`
- `specs/001-existing-system-baseline/quickstart.md`

Expected: no `NEEDS CLARIFICATION` or unresolved template placeholder remains.

## 2. Confirm Modules and Build Boundaries

Inspect `settings.gradle`, root `build.gradle`, and `app/build.gradle`.

Expected:

- only `:app` is included;
- min/target/compile SDK and Java/Kotlin targets match the plan;
- Debug and Release behavior matches the plan;
- no product flavor exists;
- Release minification, shrinking, signing, ABIs, and metadata generation are present.

## 3. Confirm UI, State, and Navigation

Use `docs/CODEMAP.md` to inspect splash, login, role selection, driver, supervisor, map, violation,
and track-manager entry points plus their mapped ViewModels.

Expected:

- XML and Compose coexist;
- LiveData and StateFlow both exist;
- explicit Intents/manual Fragment switching are active;
- no active navigation graph or single global state container is required by the baseline.

## 4. Confirm Data, DI, and Authentication

Inspect `di/AppModule.kt`, `di/NetworkModule.kt`, `di/AppUpdateModule.kt`, auth/token helpers,
repositories, `TrackContentProvider.java`, `DatabaseHelper.java`, and preference-backed stores.

Expected:

- Hilt is partial but active;
- selected municipality base URL configures business traffic;
- OIDC and password modes coexist;
- encrypted token storage and refresh/failure ownership match the plan;
- SQLite ContentProvider and preferences are production storage;
- Room providers remain inactive.

## 5. Confirm SDUI, Actions, and Endpoints

Compare:

- [SDUI boundary](./contracts/sdui-boundary.md)
- [native action inventory](./contracts/native-action-inventory.md)
- [endpoint inventory](./contracts/endpoint-inventory.md)

Expected:

- server data populates packaged business screens;
- downloadable layouts are limited to the legacy tracking-layout subsystem;
- no general SDUI engine, native-action registry, or endpoint registry is claimed;
- Retrofit endpoints and fixed/legacy URL owners match source annotations/configuration.

## 6. Run Smallest Relevant Automated Validation

Because only documentation artifacts changed, source tests are not required to prove document syntax.
For repository health when the Android environment is available, run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

Expected: existing unit tests pass.

For Release-sensitive confirmation, run separately:

```powershell
.\gradlew.bat :app:lintRelease
.\gradlew.bat :app:assembleRelease
```

Expected: lint and signed/minified Release build succeed when required SDK and signing configuration
are available. Runtime auth, first feature API, maps, tracking, media, and update paths still require
their documented smoke environments; do not log secrets during validation.

## 7. Review Technical Debt Separation

Read `plan.md` sections “Current Architecture” and “Technical Debt (Not Required Architecture).”

Expected: debt observations do not appear as required migrations or implementation tasks, and
required compatibility architecture is explicitly preserved.

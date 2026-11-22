# Baseline build, APK and startup measurements

**Captured**: 2026-09-30 on the current Windows development machine before module graph changes.

## Commands and results

| Variant | Command | Result | Wall time | APK | Size |
|---|---|---:|---:|---|---:|
| Debug | `.\gradlew.bat :app:assembleDebug --no-daemon --max-workers=1` | PASS | 61.13 s | `app/build/outputs/apk/debug/app-debug.apk` | 60,999,667 bytes |
| Release | `.\gradlew.bat :app:assembleRelease "-Dorg.gradle.jvmargs=-Xmx2g -XX:MaxMetaspaceSize=768m" --no-daemon --no-parallel --max-workers=1` | PASS | 142.62 s | `app/build/outputs/apk/release/farazaman.apk` | 24,674,647 bytes |

The Debug build executed 47 tasks (21 executed, 26 up-to-date). The Release build executed 61 tasks (25 executed, 36 up-to-date) and completed R8, resource shrinking, signing validation and release metadata generation.

## Startup baseline

Runtime startup timing was **not measured** in this environment because no controlled device/emulator and clean-install test harness were attached. This is an explicit unavailable baseline, not an inferred value. Before T103 can claim a startup comparison, record cold/warm startup on the same device/API/build type for both commits.

## Interpretation constraints

- These are incremental-machine measurements with existing Gradle caches, not clean CI benchmarks.
- Compare future values using the same commands, worker count, JVM options and machine.
- Release size is directly comparable only when ABI filters, signing and shrinker configuration remain unchanged.

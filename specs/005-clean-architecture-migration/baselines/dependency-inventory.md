# Baseline dependency inventory

**Captured**: 2026-09-30 before changing the Gradle module graph.

## Module graph

```text
root faraZaman-v2
└── :app (com.android.application)
```

`settings.gradle` contains only `include ':app'`. All production code, Android entry points, resources, local tests and instrumentation tests currently belong to the application module.

## Package boundaries inside `:app`

| Package | Current responsibility | Known boundary violation/pressure |
|---|---|---|
| `activity/` | Navigation, Compose hosts, platform effects and legacy orchestration | Some owners also call APIs, preferences, ContentResolver and services |
| `presentation/` | Compose UI, ViewModels, login and common state/event contracts | Still coupled to application-module implementations |
| `domain/` | Auth contracts/use case, validation and policies | Partial rather than enforced pure boundary |
| `data/` | DTOs, repositories, API and legacy database | Shares the same compile classpath with presentation |
| `service/` | GPS and remote service interfaces | Android and transport ownership is mixed |
| `supervisor/` | Mission, daily-plan, tracking and history orchestration | Business rules coexist with Android/platform calls |
| `notification/`, `realtime/` | FCM, unread state and SignalR | Application/session lifecycle ownership |
| `map/` | MapLibre style, host and overlays | Android renderer lifecycle implementation |
| `util/` | Security, URL, preferences and general helpers | Cross-cutting dependency surface |

## Build plugins

- Android application, Kotlin Android/KAPT, Hilt, Google Services, Safe Args, JaCoCo and Roborazzi.
- Kotlin `1.9.22`, Java 17, AGP `8.4.2`, Compose compiler `1.5.10`.

## Major runtime dependency groups

- UI/lifecycle: AppCompat, Activity, Lifecycle, Material Components, Compose Material 3.
- DI: Hilt/Dagger with KAPT.
- Network/auth: Retrofit 2.9, OkHttp 4.12, Gson, AppAuth.
- Persistence: SQLite through application ContentProvider; inactive Room scaffolding has no production ownership.
- Field/system: MapLibre 11.8, Android location foreground service, camera/media APIs.
- Messaging/realtime: Firebase Messaging and SignalR 9.0.10.
- Files/media: OSM trace API, Coil, GPX/ZIP utilities and Media3-related code in the application.

## Test dependencies

- JUnit4, Robolectric, Mockito, PowerMock, Compose UI test, Roborazzi, Espresso and AndroidX test rules.

## Migration implications

- Every new module initially duplicates a small amount of Android/Kotlin configuration; convention plugins are deferred until duplication is proven.
- Moving a class changes its R8/Hilt/resource visibility surface even when behavior is unchanged.
- Feature-to-feature dependencies are prohibited; shared contracts must be owned by an appropriate `:core:*` module.

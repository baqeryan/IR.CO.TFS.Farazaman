# Baseline lifecycle ownership matrix

**Captured**: 2026-09-30 from the current production paths.

| Capability | Current creator/start owner | Current stop/release owner | State owner | Migration invariant |
|---|---|---|---|---|
| GPS recording | Driver/Supervisor orchestration sends commands; `GPSLogger` starts foreground and requests GPS/network updates | `GPSLogger` removes updates and unregisters its stop receiver; mission controllers issue stop commands | Active track in ContentProvider/preferences | Exactly one service owns provider callbacks; UI must only issue typed commands |
| SignalR | `MyApplication` foreground/session policy calls `MobileSupervisorHubClient` | Application background/logout/auth failure calls `stop`; client cancels reconnect and connection | Application-scoped client | Feature may expose events, but application lifecycle remains the connection owner |
| FCM | Android instantiates `FarazamanFirebaseMessagingService` | Android service lifecycle | Server inbox is source of truth; lightweight unread preference is a cache | No feature screen starts/stops FCM; token registration stays selected-server aware |
| Notification badge receiver | `NotificationBadgeBinder` registers in lifecycle `onStart` | Binder unregisters in `onStop`/destroy fallback | View-local binding plus unread store | Registration must remain paired and package-scoped |
| AppAuth WebView/redirect | Login Activity/bottom sheet initiates authorization | Bottom sheet/Activity callback and logout cleanup release session UI/WebView data | Secure token/session helpers | Domain decides intent; Activity/route performs launcher/window effects |
| Camera capture | `ViolationImageCaptureCoordinator` creates pending MediaStore item and launches `TakePicture` | Result callback deletes failed/cancelled pending item | Activity-scoped coordinator | One Activity Result owner; no Composable launches raw contracts directly |
| MapLibre | Activity/Compose map host creates `MapView` | `ComposeMapHost` mirrors ON_START/RESUME/PAUSE/STOP and disposal lifecycle | Route/container plus map adapter | Renderer lifecycle stays outside ViewModel/domain |
| Runtime location acquisition | `LocationAcquisitionCoordinator` begins bounded acquisition | Coroutine/lifecycle cancellation removes listeners | Calling route/coordinator | Request must be bounded and cancellable; use case sees a domain result only |
| Auth state receiver | `BaseActivity`/session-aware surfaces register for package auth state | Lifecycle teardown unregisters | `AuthStateManager` and secure session | A single session source of truth triggers navigation effects |

## Known risks

- Activities/controllers currently combine lifecycle effects and business decisions.
- Multiple tracking entry points can issue service commands; typed command and active-track guards must prevent duplicate ownership.
- Process death and configuration changes are not fully represented by unit tests; device smoke remains mandatory for system slices.

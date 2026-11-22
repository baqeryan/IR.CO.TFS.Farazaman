# Baseline Android platform contracts

**Captured**: 2026-09-30 from `app/build.gradle`, `AndroidManifest.xml` and stable constants.

## Stable identities

| Contract | Baseline value |
|---|---|
| Namespace | `Ir.co.tfs.farazaman` |
| Application ID | `Ir.co.tfs.farazaman` |
| Track provider authority | `Ir.co.tfs.farazaman.provider` |
| FileProvider authority | `Ir.co.tfs.farazaman.fileprovider` |
| OIDC redirect | `ir.co.tfs.farazaman://signin-oidc` |
| Legacy redirect scheme | `osmtracker` |
| GPS service action | `osmtracker.intent.GPS_SERVICE` |
| Launcher | `.activity.SplashActivity` |
| Application | `.MyApplication` |

`TrackContentProvider.AUTHORITY` is derived from the stable `OSMTracker.PACKAGE_NAME`; it must continue to equal the manifest authority after R8.

## Exported surface

- `SplashActivity` is exported as the launcher.
- `TrackManager` and `OpenStreetMapUpload` are currently exported and therefore compatibility-sensitive.
- AppAuth `RedirectUriReceiverActivity` is exported for the two declared redirect filters.
- Application-owned GPS/FCM services, providers and receivers are not exported.

## Services and providers

- `FarazamanFirebaseMessagingService`: FCM event service.
- `GPSLogger`: foreground location service with `foregroundServiceType="location"`.
- `TrackContentProvider`: authoritative track SQLite interface.
- AndroidX `FileProvider`: controlled file sharing.

## Receivers

- `MediaButtonReceiver`: non-exported media-button receiver.
- `AppUpdateInstallReceiver`: non-exported update completion receiver.
- Runtime package-scoped receivers are registered for auth, tracking and unread-state events.

## Compatibility gate

Module migration must not change any value in this file without a separate approved migration, manifest/R8 tests and an upgrade-path review.

# Support, Supervisor and System-flow Baseline

## Support and notifications

- `ActionsNotificationsActivity` hosts Actions and Notifications tabs and defaults to Actions.
- Notifications preserve authenticated selected-server paging, empty/loading/error states, refresh on resume and FCM inbox broadcast, details, idempotent Visit/read count, confirmed Delete and unread badge synchronization.
- Actions preserve resume-mission result delivery and GPX export through `CreateDocument`.
- Profile preserves current user/role presentation and logout/navigation behavior.

## Supervisor shell

- A missing mission redirects to work-area selection. Home, Today and History fragments remain alive and are shown/hidden by the bottom navigation.
- Mission controller state, day registration, permission callbacks, violation routes, action notifications and snackbar anchoring remain container-owned.
- The floating/glass navigation must never cover the final scrolled content. Theme changes, Persian RTL and system back behavior update without process restart.

## Forms and camera

- Validation, form-data requests, image capture/removal/preview, permission denial/cancel, submit deduplication and recreation behavior are mandatory parity.
- Camera launchers and URI/file cleanup remain outside pure Screens.

## Map, GPS and track

- Baran style caching/fallback, MapLibre overlays/camera, map lifecycle and unavailable states remain unchanged.
- `GPSLogger` remains the lifecycle-owned foreground tracking service; Compose emits typed commands only.
- Track/point/waypoint ContentProvider schemas, GPX generation/export/upload, selected `BASE_URL` and background file/database work remain unchanged.
- Map/Camera/GPS/Track each keep an independently removable protected runtime fallback during rollout.

## Visual intent and comparison states

For each migrated surface capture content, loading, empty, error, refreshing/submitting/validation and pagination where applicable. Use deterministic Persian fixtures across RTL light/dark, large font and relevant small/normal/large/tablet sizes. Native map/camera/location/time/network content is replaced by deterministic adapters for screenshot tests.

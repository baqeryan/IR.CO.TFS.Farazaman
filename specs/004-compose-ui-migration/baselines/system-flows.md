# System Flow Baseline

Map, camera, GPS, and track flows retain their current behavioral contract during migration.

| Flow | Owner | Required parity | Restoration policy |
|---|---|---|---|
| Map | Activity/MapLibre lifecycle host | style load, overlays, camera position, back navigation | local migration flag selects Compose or legacy host |
| Camera | Activity result coordinator | permission, capture, cancel, URI persistence | pending capture state remains outside Screen |
| GPS | tracking controller/service | permission, start/stop, process recreation | service/controller remains source of truth |
| Track | ContentProvider/repository | list, detail, waypoint, export, upload | database/file work remains outside Compose |

Flags default to Compose for new rollout builds. A hidden non-exported diagnostics destination may select the legacy path for support. Flags are removed only after the corresponding rollout and lifecycle tests pass.

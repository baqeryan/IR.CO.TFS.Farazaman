# Current Native Action Inventory

No centralized native-action registry exists. This document groups current dispatch mechanisms.

## Application Navigation Actions

- Activity launch through explicit Intents: login, role selection, driver, supervisor work area,
  supervisor shell/profile, track/map/detail, violation, preferences, upload, and system settings.
- Fragment/tab actions through manual transactions and `SupervisorBottomNavHelper`.
- OIDC return through the AppAuth redirect Activity.

## Mission and Supervisor Actions

- `SupervisorMissionHelper` and `SupervisorMissionSetup`: resume/create/route mission work.
- `SupervisorTrackController`: start/stop supervisor tracking.
- `TodayVisitMissionActions`: open mission map and workflow actions from daily visits.
- `DailyPlanItemViolationHelper`: route daily items into violation flows.
- `SupervisorProfileHelper`: profile navigation/actions.

## Compose Actions

- Splash/update actions are callbacks for retry, download, permission, install, and continue routing.
- Role selection actions are callbacks that route the selected role through the host Activity.

## Map and Violation Actions

- Map Activities dispatch location, layer, waypoint, violation, road/address, and camera/form actions
  through view listeners, ViewModel calls, and explicit navigation.
- Violation forms dispatch selector, camera/gallery, image removal, validation, and submission actions
  directly from the screen/fragment boundary.

## Legacy Tracking Layout Actions

`UserDefinedLayout` and related layout parsing bind configured buttons to listener classes:

- `TagButtonOnClickListener`
- `TextNoteOnClickListener`
- `StillImageOnClickListener`
- `VoiceRecOnClickListener`
- `PageButtonOnClickListener`
- `SensorListener`
- `PressureListener`
- waypoint edit and related legacy actions

These listeners are the closest equivalent to an action-handler set, but they are not exposed as a
general registry shared by modern application screens.

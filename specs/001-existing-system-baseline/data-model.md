# Data Model: Existing-System Baseline

This artifact documents existing data shapes and ownership. It defines no migration or new schema.

## Municipality Instance

**Fields**: display identity, province/city information, selected base URL, OIDC client ID, scope,
login mode.

**Relationships**: Configures a session and determines the business API host and auth mode.

**Validation**: A usable base URL must satisfy the existing client configuration expectations.

**State transitions**: Registry result → user-selected → persisted runtime configuration → cleared
on logout or replaced by another selection.

## Authentication Session

**Fields**: access token, optional refresh token, token type, expiry, selected auth mode.

**Relationships**: Belongs to a selected municipality and gates user/role retrieval and authenticated
business requests.

**Validation**: A valid session requires a non-expired access token. Token strings use encrypted
storage; secrets are never part of documentation or logs.

**State transitions**: Unauthenticated → token acquired → profile/roles verified → active → refresh
in progress → active or invalid → logged out.

## User Profile and Role Set

**Fields**: account/profile response data and role-name/availability mapping.

**Relationships**: Belongs to the active session; roles drive driver/supervisor choices and routing.

**Validation**: Login is incomplete until both profile and role requests succeed.

## Work Area / Zone

**Fields**: server identity, display name, zone/work-area metadata.

**Relationships**: Selected by a driver or supervisor before mission work; may scope employer,
contract, mission, or map context.

## Mission

**Fields**: server identifiers/encryption values, role, work area, employer, contract, daily item,
dates/status, optional employer-fence WKT/name, associated local track ID.

**Relationships**: Connects server operational data with a local role-specific track and supervisor
daily/violation workflows.

**Validation**: Required selections and server identifiers depend on the current workflow and form
data. Mission JSON is keyed by track ID in preferences.

**State transitions**: Not created → selection/configuration → remote/local creation → active →
stopped/completed → retained for history/upload.

## Track

**Fields**: local ID, name, creation/start/end metadata, active status, role, directory/description
and legacy track attributes.

**Relationships**: Owns track points and waypoints; may be associated with mission preference JSON.

**Validation**: Role separates driver and supervisor tracks in the shared database.

**State transitions**: Created → active/recording → stopped → exported/uploaded or retained.

## Track Point

**Fields**: track ID, latitude, longitude, altitude, accuracy, speed, bearing, timestamp, provider and
legacy sensor metadata where captured.

**Relationships**: Ordered child of a Track; rendered as route geometry and exported/uploaded.

**Validation**: Recording is governed by current tracking policy and active-service ownership.

## Waypoint

**Fields**: track ID, location, timestamp, title/description, and optional media/note references.

**Relationships**: Child annotation of a Track; may originate from tracking-button native actions.

## Employer and Contract

**Fields**: server value/ID and display text; contracts are filtered or associated by employer.

**Relationships**: Server form data drives supervisor mission selection and violation context.

**Validation**: Selection must use values returned for the current municipality/workflow.

## Daily Cleaning Item

**Fields**: server identity/encryption, date, employer/contract/work description, visit/status fields.

**Relationships**: Appears in supervisor today/visit lists and can launch map, mission, or violation
actions.

## Violation

**Fields**: server classifications and IDs, date, road/location context, description/notes,
mission/employer/contract/daily-item context, image URIs/files, result ID/message.

**Relationships**: Created from map or supervisor workflows; uses server form data and may be retained
in local supervisor history.

**Validation**: `ViolationFormValidator` and screen-specific rules enforce required selections and
values before submission.

**State transitions**: Form-data loading → editing/evidence selection → validation error or
submitting → success or error.

## Server-Populated UI Data

**Fields**: roles, work areas, employers, contracts, form choices, GIS layers, mission details,
roads/addresses, fence geometry.

**Relationships**: Populates compiled screens and maps. It does not define arbitrary screen trees.

**Validation**: Values are interpreted by the specific feature DTO and UI mapper/caller.

## Legacy Custom Layout

**Fields**: repository configuration, remote layout filename/content, pages, button types, labels,
icons, and action-specific values.

**Relationships**: Downloaded/selected through legacy layout screens and rendered by
`UserDefinedLayout`; buttons dispatch to legacy listeners.

**Validation**: Remote source and layout parsing follow the existing validator/downloader/parser.
Production governance and active use are not confirmed.

## Update Metadata and Candidate

**Fields**: version code/name, download URL, mandatory flag, notes, publication metadata; local
partial/final cache file and validation status.

**Relationships**: Startup update state compares metadata to installed version and drives download,
validation, permission, and install UI.

**Validation**: Candidate must be a valid newer APK for the same package, from an allowed HTTPS host,
and signed by a compatible certificate.

**State transitions**: Checking → unavailable/available → downloading → ready → permission required
or installing → installed/error.

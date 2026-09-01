# Current Interface Contracts

These contracts describe current integration boundaries. They do not introduce new interfaces.

## Startup Routing Contract

- Launcher is the splash flow.
- Update failure is fail-open; a confirmed mandatory update remains enforced for the process.
- A usable stored session routes toward role selection; otherwise startup routes to login.

## Municipality Routing Contract

- Central registry traffic discovers municipalities and their auth configuration.
- Selected municipality `BASE_URL` is authoritative for business APIs and uploads.
- Identity, update, and map hosts remain separate fixed/configured external boundaries.

## Authenticated Request Contract

- Bearer credentials are attached through the shared authenticated client where that path is used.
- Refresh is serialized, and one retry may follow an authentication failure.
- Authorization values are redacted/not logged.
- Token acquisition must be followed by successful user-info and role retrieval.

## Persistence Contract

- Track rows are accessed through the stable non-exported ContentProvider authority.
- Track/point/waypoint database compatibility and role separation are preserved.
- Mission/user/config metadata remains preference-backed; token strings remain encrypted.
- Logout preserves the track database and persistent user files.

## Navigation Contract

- Cross-screen routing uses explicit Intents and AppAuth redirects.
- Supervisor content uses manual Fragment/tab switching and bottom sheets.
- No navigation graph contract is assumed.

## Foreground Tracking Contract

- Recording is owned by the foreground location service during active missions.
- Driver and supervisor controllers start/stop the same service and write to the shared provider.
- Location permissions and lifecycle ownership remain prerequisites.

## Release Contract

- Release remains minified, resource-shrunk, ARM-packaged, and signed by the permanent identity.
- Retrofit/Gson/AppAuth/generic metadata and stable provider/action identifiers must remain R8-safe.
- Release validation includes authentication and the first authenticated feature call, not only build
  completion.

## Update Contract

- Update discovery uses no municipality bearer token.
- Downloads use app cache and must pass host, archive, package, version, and signature validation.
- Platform unknown-source permission is requested only when required.

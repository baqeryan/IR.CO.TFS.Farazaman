# Validation Quickstart: Contractor Violation Correction

## Prerequisites

1. Obtain the authoritative Contractor role token and actionable-list/correction API schemas or captured successful requests described in [contracts/contractor-violation-contract.md](contracts/contractor-violation-contract.md).
2. Use a test account whose `/api/AccountUser/Roles` response includes Contractor access and whose selected municipality server returns actionable violations.
3. Use an emulator/device with a camera implementation and controllable location. Configure at least one known location and also test with location services disabled.
4. Do not use production credentials in source, tests, screenshots, or logs.

## Fast validation commands

Run from the repository root in PowerShell:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:lintDebug
```

Release/R8 validation:

```powershell
.\gradlew.bat :app:assembleRelease "-Dorg.gradle.jvmargs=-Xmx2g -XX:MaxMetaspaceSize=768m" --no-daemon --no-parallel --max-workers=1
```

Optional connected tests:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest
```

## Contract fixture checks

- Verify `roles` remains backward compatible and Contractor is parsed from the exact `Contractor` key in `actorRoles`, not from its Persian display value.
- Deserialize representative actionable-list content, empty content, malformed content, and API error fixtures with the exact DTOs.
- Verify correction serialization/multipart names against the captured backend contract.
- Verify repository success for HTTP 200 with body, HTTP 200 without body, and HTTP 204.
- Verify non-2xx and transport failures preserve the draft and do not create a local submitted tag.

## End-to-end scenarios

### Role routing

1. Contractor only: login completes and Contractor list opens directly.
2. Contractor plus Supervisor/Driver: existing role selection shows all authorized options; Contractor opens its list.
3. Driver-only and Supervisor-only accounts: existing destinations remain unchanged.
4. Release build: repeat Contractor routing to catch R8/Gson-only failures.

### List states and authority

1. Delayed API response shows Loading, then cards.
2. Empty successful response shows the Persian empty state.
3. API/network failure shows the standard error and retry/manual refresh.
4. Each card shows chapter, violation title, map action, and correction action.
5. Start two rapid refreshes; only the newest request result becomes current.
6. After a correction success, confirm no full list request is made and only the matching card shows “اصلاح ثبت شد”.
7. Manual refresh clears the local tag and replaces the list with server data.
8. Leave and re-enter the list; confirm fresh authoritative loading.

### Map

1. A valid violation coordinate opens the existing MapLibre/Baran style centered on the point.
2. Missing/invalid coordinates show an unavailable message and do not crash or hide the correction action.

### Camera and evidence

1. Add evidence and confirm only Camera is offered—no gallery/file picker.
2. Capture multiple images; preview, remove, replace, and add again.
3. Cancel a camera capture; existing description/images remain.
4. Remove the final image; submit is blocked.
5. Deny camera permission; show a clear message and keep the draft.

### Location and submit

1. With permission/provider enabled, submit obtains a fresh configured emulator location and sends it.
2. Force fresh timeout/failure while a valid last-known fix exists; confirm that fix is used.
3. Remove permission; submit is blocked and the permission flow is shown.
4. Disable location services; submit is blocked and the system settings action is offered.
5. With no fresh or last-known fix, submit is blocked; no zero/hardcoded coordinates are sent.
6. Tap Submit repeatedly while acquiring/submitting; observe exactly one request and a disabled/loading control.
7. Return HTTP 200 and separately HTTP 204; both show success, close the form, return to the list, and mark the card.
8. Return a server rejection or network failure; description/images remain, no tag appears, and retry can submit again.
9. Submit another correction for the same violation after the first flow; Android sends it and leaves the decision to the server.

## Regression checks

- Supervisor violation camera capture/removal/submission still behaves identically if shared capture code was extracted.
- Driver workflow and GPS tracking are unchanged.
- Runtime city/server selection still determines `BASE_URL`.
- Logout and login continue to clear/reload role state normally.
- No token, Authorization header, password, or image body is present in Logcat.
- Debug and signed Release both complete the first authenticated Contractor list request.

## Expected artifact scope

No database migration, new networking library, notification worker, gallery permission, broad storage permission, Driver feature work, or backend change should appear in the implementation diff.

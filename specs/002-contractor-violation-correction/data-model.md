# Data Model: Contractor Violation Correction

Wire field names and nesting below must be finalized from the existing backend contract. Domain/UI models should not expose ambiguous Gson maps when typed fields can be confirmed.

## Contractor role access

### `UserAccess` extension

- `hasDriverAccess: Boolean` — existing.
- `hasSupervisorAccess: Boolean` — existing.
- `hasContractorAccess: Boolean` — new, derived only from the exact `Contractor` key in `actorRoles`.
- `isSuperAdmin: Boolean` — existing.

Rules:

- A Contractor-only user routes directly to Contractor violations.
- Any user with more than one selectable workflow routes to existing role selection.
- Existing Driver and Supervisor destinations do not change.
- Super-admin deliberately implies access to every app-supported role, including Contractor; non-admin users still require the exact `Contractor` key in `actorRoles`.

## Actionable violation

### Wire DTO: `ContractorActionableViolationDto`

Exact fields are unknown / not confirmed from the Android repository. It must map the existing API response fields required for:

- stable violation identifier;
- Bill of Quantities chapter equivalent to `BillCleaningViolation.BillCleaningItemGroup.BillCleaningItemGroupName`;
- violation title equivalent to `BillCleaningViolation.ViolationType`;
- latitude and longitude, if the list contract supplies them;
- ContractId, OrganId, or other identifiers only when required unchanged by the existing correction contract.

### Domain/UI item: `ContractorViolationItem`

- `id: ViolationId` — immutable stable identifier.
- `chapterName: String` — user-visible chapter.
- `violationTitle: String` — user-visible title.
- `latitude: Double?`
- `longitude: Double?`
- `submissionContext: ContractorSubmissionContext` — opaque confirmed identifiers required by submit; never used to authorize/filter locally.
- `correctionSubmittedThisSession: Boolean` — presentation projection, not a server field and never persisted.

Validation/invariants:

- Items are rendered in the order returned by the authoritative API unless the contract specifies otherwise.
- No ContractId/OrganId filtering is performed on Android.
- Missing/invalid coordinates disable only the location-view action; they do not hide the item or block opening the correction form.
- Duplicate correction policy is not represented in this model; the server decides each submit.

## Correction draft

### `ContractorCorrectionDraft`

- `violationId: ViolationId` — immutable from navigation.
- `description: String`
- `imageUris: List<UriString>` — only URIs created by the camera capture flow.
- `submissionContext: ContractorSubmissionContext` — identifiers required by the confirmed endpoint.

Validation:

- Description must be nonblank.
- At least one successfully captured, still-readable camera image is required.
- Gallery/file-imported URIs are not accepted because no such launcher exists.
- Coordinates are not part of editable draft state and cannot be entered manually.

State preservation:

- ViewModel/SavedStateHandle may retain description and URI strings across configuration changes during the active form session.
- No database or preference persistence; no offline submission queue.
- Removing/replacing an image updates the list immediately and makes an empty list invalid.

## Location result

### `LocationAcquisitionResult`

- `Fresh(location)` — one-shot current fix obtained before timeout.
- `LastKnown(location)` — fresh attempt failed; best valid existing GPS/network fix selected.
- `PermissionRequired` — fine/coarse permission absent.
- `ServicesDisabled` — no usable location provider enabled.
- `Unavailable` — providers enabled and permission granted, but no fresh or valid last-known fix.
- `Cancelled` — owning lifecycle ended.

### `SubmissionLocation`

- `latitude: Double`
- `longitude: Double`
- `accuracyMeters: Float?`
- `source: FRESH | LAST_KNOWN`

Rules:

- Fresh is always attempted before last-known fallback.
- Latitude must be in `[-90, 90]`; longitude in `[-180, 180]`.
- Zero is not automatically invalid geographically, but no hardcoded `(0,0)` fallback may be created.
- Current project validity is provider availability plus a real positive-accuracy last-known fix. No unconfirmed age threshold is introduced.

## Correction request and response

### Wire request: `ContractorCorrectionRequestDto`

The existing API contract must determine whether the request is multipart JSON + image parts or another existing shape. It includes at least:

- violation identifier;
- description;
- latitude;
- longitude;
- one or more camera image parts;
- confirmed ContractId/OrganId/additional identifiers when required.

Android must not add authorization fields or derive missing identifiers beyond the contract.

### Repository result: `CorrectionSubmitResult`

- `Success` — any successful HTTP response, explicitly including 200 and 204, regardless of body.
- `ApiFailure(code, message)` — non-2xx response parsed with existing error conventions when possible.
- `NetworkFailure(message)` — transport/timeout failure.
- `InvalidLocalImage(uri)` — image cannot be read before request construction.

## UI states

### `ContractorViolationsUiState`

- `Loading(previousItems?)`
- `Content(items, isRefreshing, submittedIds)`
- `Empty`
- `Error(message, previousItems?)`

Transitions:

```text
enter -> Loading -> Content | Empty | Error
Content --manual refresh--> Loading/refreshing -> Content | Empty | Error
Content --correction success(id)--> Content with id in submittedIds
manual refresh or new screen entry -> clear submittedIds -> authoritative API state
```

Only the latest request generation may replace state; a stale earlier response is ignored/cancelled.

### `ContractorCorrectionUiState`

- `Editing(draft, validationMessages)`
- `AcquiringLocation(draft)`
- `Submitting(draft, progressVisible = true)`
- `Error(draft, message)`
- `Success(violationId)`

Transitions:

```text
Editing --submit invalid--> Editing with validation messages
Editing --permission missing--> Editing + permission message/request
Editing --services disabled--> Editing + enable-location prompt
Editing --valid--> AcquiringLocation
AcquiringLocation --fix--> Submitting
AcquiringLocation --unavailable--> Error with unchanged draft
Submitting --200/204/other 2xx--> Success -> Activity result -> finish
Submitting --API/network/file failure--> Error with unchanged draft
```

While `AcquiringLocation` or `Submitting`, additional submit actions are ignored and the submit control is disabled.

## Relationships

```text
UserAccess
  -> selects Contractor workflow
  -> ContractorViolationsUiState
       -> many ContractorViolationItem
            -> ContractorCorrectionDraft
                 -> SubmissionLocation
                 -> ContractorCorrectionRequestDto
                 -> CorrectionSubmitResult
```

There is no relationship to Driver state, local track databases, or an Android-enforced correction-count rule.

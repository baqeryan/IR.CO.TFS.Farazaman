# Contractor Violation Integration Contract


This document defines the Android-side contract boundary. It intentionally does not invent backend endpoint names or JSON fields absent from the repository.

## 1. Role contract

**Existing call**: `RolesService.getUserRoles()` → `GET api/AccountUser/Roles` using the current authenticated runtime-selected server.

Confirmed response structure:

- `roles`: the existing application-role dictionary; Android behavior for this field is preserved.
- `actorRoles`: the business-actor-role dictionary added alongside `roles`.
- Contractor actor-role key: `Contractor`.
- Contractor display value: `پیمانکار`.

`RolesManager` authorizes Contractor access from the `Contractor` key in `actorRoles`; it does not replace or merge away `roles`, and it does not treat the display label, ContractId, OrganId, or map position as authorization.

Expected Android outcomes:

| Returned access | Navigation |
|---|---|
| Contractor only | Directly open Contractor violations |
| Contractor plus another selectable role | Show existing role selection with Contractor option |
| No Contractor access | Do not expose Contractor route |

Existing Driver and Supervisor rows remain unchanged.

## 2. Actionable-violations API boundary

**Service location**: extend `app/src/main/java/Ir/co/tfs/farazaman/service/remote/ViolationApiService.kt`.

**Client**: the existing Hilt-provided authenticated Retrofit/OkHttp client from `NetworkModule.kt`; base URL is the current `BASE_URL` preference.

Required confirmed backend details before coding:

- relative endpoint and HTTP method;
- request body/query/path fields, if any;
- response envelope and nullability;
- violation identifier type;
- paths for chapter name and violation title;
- coordinate representation;
- any opaque identifiers required later for correction;
- error envelope.

Semantic contract:

- The response is already authorized/filtered by token, ContractId, OrganId, and contractor access.
- Android renders returned actionable items without duplicating those rules.
- An empty successful result maps to the empty UI state.
- A failed call maps to retryable Error and does not fabricate cached results.
- Explicit refresh and new screen entry reload this endpoint and replace local transient tags.

## 3. Correction API boundary

**Service location**: the same `ViolationApiService.kt`.

Required confirmed backend details before coding:

- relative endpoint and HTTP method;
- media type and exact multipart/body field names;
- violation ID and any ContractId/OrganId/additional identifier representation;
- description field;
- latitude/longitude representation and coordinate order;
- image collection field name, allowed MIME types, and server limits;
- success/error response bodies.

Minimum semantic payload:

| Value | Android source |
|---|---|
| Violation identifier | Immutable selected list item |
| Description | Contractor form |
| Images | One or more `TakePicture` camera results only |
| Latitude/longitude | Fresh fix, else valid last-known fix |
| Additional identifiers | Copied from confirmed list/navigation context exactly as required |

Response handling:

| HTTP/transport outcome | Android outcome |
|---|---|
| 200 OK | Success even if body is empty |
| 204 No Content | Success |
| Other 2xx | Success unless the existing contract explicitly states otherwise |
| Non-2xx | Failure; parse and show existing API message when possible; preserve form |
| Network/timeout | Failure; preserve form and permit retry |

Android sends repeated corrections normally. It does not count prior submissions or reject duplicates beyond preventing two simultaneous requests from the same active form.

## 4. Activity navigation/result contract

Use explicit Intent navigation, matching the existing application.

### Role selection → Contractor list

- Destination: proposed `ContractorViolationsActivity`.
- No token, password, or authorization data in extras; the shared client owns auth.

### List → read-only map

- Required: stable violation identity and valid contract-provided coordinates.
- Optional: chapter/title for display.
- Missing/invalid coordinates are handled before or at destination with a user-visible unavailable message.

### List → correction form

- Required: stable violation identifier and opaque submit context required by the confirmed contract.
- Optional: chapter/title for display.
- The form must not permit editing identifiers.

### Correction form → list

- `RESULT_OK` only after an HTTP-successful correction.
- Result extra: the stable violation identifier.
- List marks that identifier “اصلاح ثبت شد” locally and does not call the list API.
- Cancel/back/error returns no success result and adds no tag.

## 5. Camera contract

- Only `ActivityResultContracts.TakePicture` is registered for evidence addition/replacement.
- No gallery, document, or general file picker contract is exposed.
- A capture result is added only when the camera reports success and the URI is readable.
- Preview/remove/replace/add operate on the active form URI list.
- Submit requires at least one URI that can be opened for multipart creation.
- Removed, cancelled, or obsolete app-created media entries are cleaned when safe; retained evidence stays available after a failed request during the active form session.

## 6. Location contract

At every submit attempt:

1. fine/coarse permission is checked;
2. a usable GPS or network provider is checked;
3. one fresh fix is requested with lifecycle cancellation and bounded timeout;
4. only after fresh failure is a valid GPS/network last-known fix considered;
5. no request is sent if neither exists.

The form exposes no coordinate fields. Permission or service failures block the request and show the existing permission/settings guidance.

## 7. Security and release contract

- Use runtime `BASE_URL`, `AuthInterceptor`, token refresh, and existing TLS behavior.
- Never log token/Authorization headers, passwords, full image bytes, or sensitive payload content.
- Do not add trust-all TLS, a second client, or broad storage permissions.
- Release validation must exercise role parsing, actionable list deserialization, and the first authenticated correction request under R8.

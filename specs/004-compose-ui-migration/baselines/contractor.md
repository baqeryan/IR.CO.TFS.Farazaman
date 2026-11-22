# Contractor Behavioral and Visual Baseline

## Work-area selection

- If `ContractorWorkContextStore` already contains context, immediately open operations and finish selection.
- Otherwise show the curved green header, back/profile actions and the explicit unconfigured-service message. No speculative API call is allowed.

## Operations

- Missing context redirects to work-area selection and finishes.
- Shows organ and contract titles. Routes to action and corrections; changing area clears the stored context and returns to selection.
- Start operation deliberately shows the unavailable backend message.

## Action

- Back closes the Activity. Description and at least one captured image are required.
- Camera capture, URI ownership, horizontal image list, removal and full-screen preview are Activity/coordinator effects, not Screen responsibilities.
- Valid submit currently displays the unconfigured backend message; it must not fabricate a request.

## Corrections

- Current shell displays the empty/not-connected state only. The actionable-violations endpoint and DTO are unknown and remain out of implementation scope.
- A future item may show violation details/image/location and lead to an action-like correction form only after the service contract arrives.

## Location

- Accepts latitude/longitude Intent extras, rejects non-finite/out-of-range/zero coordinates, and shows an unavailable state.
- Valid coordinates load cached/fallback Baran style, place a read-only marker and center MapLibre. All MapView lifecycle callbacks are forwarded.

## Visual intent

Persian RTL, established curved header/green branding, clear hierarchy, full-width primary actions, readable empty/error states and system back semantics are retained as intent. Pixel parity with XML is not required.


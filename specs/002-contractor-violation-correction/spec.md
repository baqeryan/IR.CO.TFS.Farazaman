# Feature Specification: Contractor Violation Correction

**Feature Branch**: `main`

**Created**: 2026-09-01

**Status**: Draft

**Input**: User description: "Add a Contractor role that can view server-authorized actionable violations, inspect their locations, and submit camera- and location-backed corrections through the existing workflow."

## Clarifications

### Session 2026-09-01

- Q: After a correction succeeds, should the correction form close and return to the violations list, or remain open? → A: Close the form, return to the list, and mark the corresponding card as "Correction Submitted" (Persian UI: "اصلاح ثبت شد").

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Enter the Contractor Workflow (Priority: P1)

As an authenticated user with Contractor access, I can choose the Contractor role and enter a dedicated workflow showing only the actionable violations the server has made visible to me.

**Why this priority**: A contractor cannot perform any correction work until the role is recognized and the authoritative work queue is available.

**Independent Test**: Sign in with Contractor access, choose Contractor when applicable, and verify that the displayed list and its states correspond to the authenticated server response without entering either the Supervisor or Driver workflow.

**Acceptance Scenarios**:

1. **Given** a user whose only available role is Contractor, **When** login and role retrieval complete, **Then** the user enters the Contractor workflow without being routed into the Driver or Supervisor workflow.
2. **Given** a user with Contractor and one or more other roles, **When** the role-selection screen is shown, **Then** Contractor is a selectable option and choosing it opens the Contractor workflow.
3. **Given** the server returns actionable violations, **When** the Contractor list loads, **Then** each returned violation is shown once and no locally inferred contract or organization filtering changes the result.
4. **Given** the server returns no actionable violations, **When** loading completes, **Then** a clear empty state is shown.
5. **Given** the list request fails, **When** the failure is received, **Then** the user sees the standard error presentation and can manually retry or refresh.

---

### User Story 2 - Register a Violation Correction (Priority: P1)

As a Contractor, I can provide a correction description and camera evidence for an actionable violation, then submit the correction with an automatically acquired location.

**Why this priority**: Registering verifiable corrective action is the primary business outcome of the feature.

**Independent Test**: Open one actionable violation, enter a description, capture at least one image, provide an available device location, and submit; verify success is recognized and the selected list card is marked locally without reloading the entire list.

**Acceptance Scenarios**:

1. **Given** an actionable violation, **When** the Contractor chooses Register Correction, **Then** a correction form opens for that violation and does not permit changing its identifier.
2. **Given** a correction description, at least one camera-captured image, and an available valid location, **When** the Contractor submits, **Then** the correction is sent using the existing correction contract and only one submission remains active.
3. **Given** the correction receives a successful response containing a body, **When** submission completes, **Then** a success message is shown, the form closes, the user returns to the violations list, and the corresponding card is tagged "Correction Submitted."
4. **Given** the correction receives a successful response without a body, **When** submission completes, **Then** it is handled identically to any other successful submission.
5. **Given** a correction submission is in progress, **When** the user repeatedly activates Submit, **Then** no duplicate simultaneous request is started and a loading state remains visible.

---

### User Story 3 - Capture and Manage Correction Evidence (Priority: P2)

As a Contractor, I can capture, preview, remove, replace, and add correction images so that the submitted evidence accurately represents the completed work.

**Why this priority**: Corrections require visual proof, while evidence management prevents accidental submission of unusable or incorrect images.

**Independent Test**: Capture multiple images through the camera, preview them, remove one, replace one, add another, and verify that only the final selected images are included when submission is attempted.

**Acceptance Scenarios**:

1. **Given** the correction form, **When** the Contractor adds an image, **Then** only the device camera is offered; gallery and general file selection are unavailable.
2. **Given** one or more captured images, **When** the Contractor previews, removes, replaces, or adds evidence, **Then** the visible evidence set immediately reflects the action.
3. **Given** no captured images remain, **When** the Contractor attempts submission, **Then** submission is blocked and a clear image requirement is shown.
4. **Given** the camera is cancelled or fails, **When** the user returns to the form, **Then** existing description and previously captured images remain available whenever the current screen session is still active.

---

### User Story 4 - Use a Valid Automatic Location (Priority: P2)

As a Contractor, I receive clear guidance when location cannot be obtained, and the application never submits manually entered or unavailable coordinates.

**Why this priority**: Geographic evidence is mandatory for a valid correction and must accurately reflect the device's available location.

**Independent Test**: Exercise fresh-location success, fresh-location failure with a valid last known location, missing permission, disabled location services, and no available location; verify submission proceeds only in the first two cases.

**Acceptance Scenarios**:

1. **Given** location permission and enabled location services, **When** submission starts, **Then** the application first attempts to obtain a fresh location.
2. **Given** fresh location acquisition fails and a valid last known location exists, **When** submission continues, **Then** that last valid location is used.
3. **Given** neither a fresh nor a valid last known location exists, **When** submission is attempted, **Then** submission is blocked with a clear location-unavailable message.
4. **Given** location permission is missing, **When** submission is attempted, **Then** submission is blocked and the user receives a clear permission message.
5. **Given** device location services are disabled, **When** submission is attempted, **Then** submission is blocked and the user is asked to enable location services.

---

### User Story 5 - Inspect and Refresh the Work Queue (Priority: P3)

As a Contractor, I can view a violation's location and manually refresh my work queue while understanding which corrections I submitted during the current visit to the screen.

**Why this priority**: Location inspection supports field work, while manual refresh reconciles temporary local feedback with authoritative server state.

**Independent Test**: Open a violation location, submit a correction, observe the local tag without an automatic list reload, then manually refresh and verify the list is replaced by the latest authoritative response.

**Acceptance Scenarios**:

1. **Given** a violation with a valid location, **When** View Violation Location is selected, **Then** the location is displayed through the application's existing map experience.
2. **Given** a correction was successfully submitted, **When** the user returns to the current list, **Then** the matching card shows a distinguishable local tag and the full list has not been automatically reloaded.
3. **Given** a local action tag exists, **When** the user manually refreshes or leaves and later re-enters the screen, **Then** the list is reloaded from the server and the response becomes the sole authority for visible items and status.
4. **Given** the Contractor attempts another correction for the same violation, **When** it is submitted, **Then** the application sends it normally and accepts the server's success or failure decision without enforcing a client-side correction limit.

### Edge Cases

- A user has Contractor plus Driver and Supervisor roles; choosing each existing role must preserve its current routing and choosing Contractor must not alter the Driver workflow.
- The server returns a violation with a missing or invalid location; the list remains usable, and attempting location viewing reports that the location is unavailable without preventing correction registration.
- A manual refresh is triggered while an existing list request is active; the UI must avoid presenting stale results as newer than the latest completed refresh.
- The actionable list changes on the server while the user is viewing it; no background reconciliation is required, and the next manual refresh or screen re-entry adopts the latest server response.
- The user removes the final captured image after completing the rest of the form; submission remains blocked until another image is captured.
- A fresh location attempt times out but a last known location exists and satisfies the application's existing validity rules; the fallback location is used.
- Permission is revoked or location services are disabled while the form is open; the conditions are rechecked at submission time.
- A submission fails because of connectivity, validation, authorization, or a server decision; no success tag is added, and the current description and captured-image set remain available during the active screen session whenever possible.
- A successful response contains no correction object or response body; it is still treated as success.
- The user taps Back during an active submission; the application must not create a second request or falsely mark success, and normal lifecycle-safe behavior applies to the in-progress result.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST recognize Contractor access from the existing post-login role information.
- **FR-002**: The existing role-selection experience MUST display Contractor when an authenticated user has Contractor access.
- **FR-003**: Users with only Contractor access MUST be routed into the Contractor workflow, while existing Supervisor and Driver routing behavior MUST remain unchanged.
- **FR-004**: The Contractor workflow MUST request the authenticated user's actionable violations from the existing authoritative service.
- **FR-005**: The system MUST display exactly the actionable violations returned by the service and MUST NOT duplicate contract, organization, or contractor-authorization filtering on the client.
- **FR-006**: The violations list MUST expose loading, content, empty, and error states and MUST support manual refresh.
- **FR-007**: Each violation card MUST display the Bill of Quantities chapter and violation title supplied for that violation.
- **FR-008**: Each violation card MUST provide View Violation Location and Register Correction actions.
- **FR-009**: If a violation lacks a usable location, the system MUST report location unavailability without crashing or hiding unrelated list actions.
- **FR-010**: A correction draft MUST remain bound to its source violation and MUST include a non-empty correction description before submission.
- **FR-011**: A correction MUST include at least one image captured through the device camera.
- **FR-012**: Gallery selection and general file selection MUST NOT be offered as correction-image sources.
- **FR-013**: Users MUST be able to preview, remove, replace, and add camera-captured images before submission using behavior consistent with existing violation evidence handling.
- **FR-014**: Geographic coordinates MUST be acquired automatically; users MUST NOT manually enter or edit correction coordinates.
- **FR-015**: At submission time, the system MUST first attempt to obtain a fresh valid location.
- **FR-016**: If fresh location acquisition fails, the system MUST use the last known location only when it satisfies the application's existing location-validity rules.
- **FR-017**: If no fresh or valid last known location is available, correction submission MUST NOT proceed and a clear location-unavailable message MUST be shown.
- **FR-018**: If location permission is missing at submission time, submission MUST NOT proceed and a clear permission message MUST be shown.
- **FR-019**: If device location services are disabled at submission time, submission MUST NOT proceed and the user MUST be prompted to enable them.
- **FR-020**: The submitted correction MUST follow the existing service contract, including the violation identifier, description, captured images, latitude, longitude, and any contract, organization, or related identifiers already required by that contract.
- **FR-021**: Both an existing successful response with content and an existing successful response with no content, including outcomes corresponding to 200 OK and 204 No Content, MUST be treated as successful submissions.
- **FR-022**: Only one correction submission request MAY be active for a correction form at a time.
- **FR-023**: A visible loading state MUST be shown while a correction submission is active, and repeated submit actions MUST NOT create duplicate simultaneous requests.
- **FR-024**: After success, the system MUST show a success message, close the correction form, return the user to the current violations list, and mark the corresponding card "Correction Submitted" (Persian UI: "اصلاح ثبت شد").
- **FR-025**: After success, the corresponding card MUST display a distinguishable local tag such as "Correction Submitted" or "Action Taken" for the lifetime of the current list state.
- **FR-026**: A successful correction submission MUST NOT automatically refresh the entire violations list.
- **FR-027**: Manual refresh or screen re-entry MUST discard temporary local action tags and reload the server-authoritative violation list.
- **FR-028**: The client MUST NOT define or enforce how many corrections may be submitted for one violation; every non-simultaneous attempt MUST be decided by the service response.
- **FR-029**: On submission failure, the system MUST NOT add a success tag and MUST preserve the current description and captured images for the active screen session whenever possible.
- **FR-030**: Service and connectivity failures MUST be shown through the application's established error presentation without exposing sensitive authentication data.
- **FR-031**: The feature MUST reuse the existing authenticated session and selected operational environment and MUST NOT create an independent authorization source.
- **FR-032**: Correction submission requires online service access; offline queuing or deferred correction submission MUST NOT be introduced.

### Scope Boundaries

**In scope**:

- Contractor role recognition and extension of the existing role-selection and routing flow.
- Server-authoritative actionable violation list, empty/error/loading states, manual refresh, and violation-location viewing.
- Camera-only correction evidence, automatic required location, correction submission, and temporary current-session action feedback.

**Out of scope**:

- Changes to the service contract, backend authorization, workflow status management, or API development.
- Supervisor approval or rejection of corrections, push notifications, or notification infrastructure.
- Driver-role completion or modification.
- Offline correction submission, global architecture changes, or unrelated refactoring.

### Key Entities *(include if feature involves data)*

- **Contractor Role**: An existing authenticated role entitlement that permits entry into the Contractor workflow; it may coexist with other roles.
- **Actionable Violation**: A server-authorized work item visible to the Contractor, identified by its existing violation identifier and carrying at least a Bill of Quantities chapter, violation title, and any available location and contract-related references required by the existing service contract.
- **Correction Draft**: The current user's unsent corrective-action input for one violation, including description, captured evidence, submission state, and automatically selected location.
- **Captured Image**: Camera-produced visual evidence that can be previewed, removed, replaced, or added before submission.
- **Correction Location**: A fresh or valid last known geographic position selected automatically at submission time.
- **Local Action Tag**: Temporary feedback attached to a violation card after a successful submission; it exists only in the current list state and is not an authoritative workflow status.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: In role-routing acceptance tests, 100% of users with Contractor access can enter the Contractor workflow, and existing Driver and Supervisor routes produce no behavioral regressions.
- **SC-002**: For every tested server response, the visible actionable-violation identifiers match the authoritative returned set exactly, with zero client-added or client-removed items.
- **SC-003**: On a normal operational connection, at least 95% of list attempts show content, empty, or actionable error feedback within 5 seconds.
- **SC-004**: At least 90% of representative Contractors can complete a correction with description, camera evidence, and available location in under 3 minutes without assistance.
- **SC-005**: In submission tests, 100% of attempts without an image, usable location, required permission, or enabled location services are blocked before sending and show the appropriate corrective guidance.
- **SC-006**: In repeated-tap tests, each correction form has no more than one simultaneous submission request.
- **SC-007**: In success-response tests, both successful responses with content and successful responses without content produce the same visible success outcome in 100% of cases.
- **SC-008**: In failure-response tests, 100% of failed submissions avoid a success tag and retain the entered description and captured images while the active form session remains available.
- **SC-009**: After successful submission, the card receives local action feedback without an automatic full-list reload in 100% of tested cases; the next manual refresh or screen re-entry exactly reflects the latest authoritative response.

## Assumptions

- The confirmed role response preserves `roles` and adds `actorRoles`; Contractor access is identified by the exact `Contractor` key in `actorRoles` with display value `پیمانکار`.
- The existing actionable-violation and correction contracts already provide or accept all identifiers required for authorized retrieval and submission.
- The application's existing rules determine whether a last known location is valid, including any age or accuracy constraints, and its current fresh-location acquisition window remains applicable.
- Correction descriptions are required and must contain non-whitespace content.
- Description and captured-image preservation is guaranteed for ordinary submission failures during the active form session; durable offline drafts and recovery after process removal are outside scope.
- A usable map destination is expected for most violations, but absence of valid coordinates does not prevent opening the correction form.
- Existing authentication, selected municipality/environment routing, camera evidence behavior, permission handling, location services, map experience, loading indicators, and error presentation remain available for reuse.

## Dependencies

- Existing post-login role retrieval and multi-role selection behavior.
- Existing authenticated, runtime-selected service environment and authoritative Contractor authorization.
- Existing actionable-violation retrieval and correction-submission contracts.
- Device camera, location permission, enabled location services, and online connectivity.
- Existing map, image-management, loading, navigation, and error-display behavior.

## Constitution Alignment

- The feature extends the proven role and violation workflows without replacing them and explicitly excludes Driver changes and unrelated refactoring.
- The existing backend contracts, authorization decisions, authenticated session, and runtime-selected environment remain authoritative.
- Existing camera, image, location, map, loading, error, and navigation mechanisms are reused; no parallel source of truth is introduced.
- Delivery can be split into independently testable role/list, evidence/location, and submission-feedback increments while preserving a usable application after each increment.

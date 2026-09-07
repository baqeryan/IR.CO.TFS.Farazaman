# Contractor Gateway Contract Boundary

The client exposes a boundary for these operations but MUST return `Unavailable` until the corresponding server contract is supplied:

1. Load authorized Contractor work areas.
2. Create or restore an active Contractor operation.
3. End/upload a Contractor operation.
4. Submit an action with description and camera evidence.
5. Page Supervisor violations authorized for correction.
6. Load violation details, evidence, and location.
7. Submit a correction tied to one immutable violation identifier.

For every operation the future catalog must define method/path, authentication, request/response JSON, identifier types, paging, image transport, success/empty semantics, errors, and idempotency. Runtime municipality routing and bearer authentication are mandatory. Until then the UI says the service is not connected and never displays false success.

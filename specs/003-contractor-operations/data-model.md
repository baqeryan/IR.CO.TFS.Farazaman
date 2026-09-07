# Data Model: Contractor Operations

## ContractorWorkContext

- `organId`: selected employer identifier
- `organTitle`: display title
- `contractId`: selected contract/work-area identifier
- `contractTitle`: display title
- `operationState`: `NOT_STARTED`, `ACTIVE`, or `ENDING`

Both identifiers must be positive and titles non-blank before entering operations. This local context is transitional presentation state, not proof of server authorization.

## ContractorActionDraft

- `description`: user-entered work description
- `imageUris`: ordered camera evidence references
- `submitting`: duplicate-submission guard

Validation is deliberately limited until the contract is supplied. The UI captures data but never claims server success while the gateway is unavailable.

## SupervisorViolationForCorrection

- Server identifier and display summary
- Read-only details and supervisor evidence
- Optional valid geographic location
- Server-authoritative correction status

No concrete field types beyond existing placeholders are committed until service examples arrive.

## CorrectionDraft

- Immutable source-violation reference
- Description
- Ordered camera evidence references
- Submission state

## State transitions

`NOT_STARTED -> ACTIVE -> ENDING -> NOT_STARTED` only after future gateway success. An unavailable or failed gateway leaves the prior state unchanged.

# Research: Contractor Operations

## Decision: Dedicated Contractor shell with shared visual language

**Rationale**: Contractor terminology and future contracts differ from Supervisor, while visual consistency is required. A dedicated shell avoids conditionals leaking into proven Supervisor behavior.

**Alternatives considered**: Reusing `NewSupervisorActivity` with role flags was rejected because it would couple Contractor data to Supervisor mission persistence and violation actions.

## Decision: Explicit unavailable backend gateway

**Rationale**: Service paths and payloads are unknown. A typed availability boundary makes unfinished integration visible and prevents calls to guessed endpoints.

**Alternatives considered**: Fake responses or adapting Supervisor mission endpoints were rejected because they could create invalid production records.

## Decision: Reuse camera-only capture and selected-image adapter

**Rationale**: These are established lifecycle-aware mechanisms and meet the requested action/correction evidence experience.

**Alternatives considered**: A file picker or gallery flow was rejected because it expands scope and conflicts with the camera-first requirement.

## Decision: Retain the existing authoritative correction placeholder

**Rationale**: Existing layouts model the correction queue, map, and form. They remain empty/unavailable until the catalog arrives instead of showing fabricated violations.

**Alternatives considered**: Local samples were rejected because users could mistake them for operational data.

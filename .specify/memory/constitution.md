<!--
Sync Impact Report
- Version change: unversioned scaffold -> 1.0.0
- Modified principles: template placeholders -> ten FaraZaman evolution principles
- Added sections: Existing-System Constraints; Incremental Development and Quality Gates
- Removed sections: none (template placeholders were resolved)
- Follow-up TODOs: TODO(RATIFICATION_DATE): original adoption date is not available
-->
# FaraZaman Constitution

## Core Principles

### I. Preserve Proven Functionality
Existing working functionality MUST NOT be rewritten, replaced, or behaviorally altered without a
documented reason tied to a requirement, defect, security concern, operational constraint, or
approved migration. Changes MUST be scoped to the smallest practical surface. This protects a
production application whose established workflows carry operational knowledge not fully captured
elsewhere.

### II. Maintain Backend Compatibility
All changes MUST remain backward compatible with the existing municipality backend APIs unless an
explicitly approved API migration defines otherwise. Existing endpoints, payloads, response
handling, authentication expectations, and runtime-selected municipality routing MUST be preserved.
Any unavoidable incompatibility MUST include a coordinated rollout and rollback plan.

### III. Analyze Before Changing Architecture
The relevant existing architecture and documented code paths MUST be analyzed before introducing
new layers, patterns, libraries, storage systems, navigation approaches, or dependency boundaries.
Architectural proposals MUST explain the current flow, the concrete problem, rejected lower-impact
options, and why the proposed change fits the hybrid legacy and modern codebase.

### IV. Follow Kotlin and Compose Conventions Where Applicable
New Kotlin code MUST follow idiomatic Kotlin, coroutine, lifecycle, and Android conventions. New or
modified Compose UI MUST use unidirectional state flow, stable state models, lifecycle-aware state
collection, accessible semantics, theme tokens, Persian localization, and RTL-safe layout. These
rules do not require rewriting working Java or XML UI solely to modernize its technology.

### V. Reuse Production Mechanisms
Existing authentication, shared authenticated networking, runtime `BASE_URL` selection, token
management, and persistence mechanisms MUST be reused unless the approved work explicitly specifies
a migration. New code MUST NOT create parallel sources of truth or bypass established security,
refresh, selected-server, SQLite/ContentProvider, or preference behavior without documented need.

### VI. Deliver Incrementally
New functionality MUST be introduced in small, independently reviewable increments that preserve a
usable application state. Each increment MUST define its compatibility boundary and validation
path. Large replacement efforts require an explicit staged migration with coexistence and rollback
criteria.

### VII. Separate Refactoring from Features
Refactoring and feature implementation SHOULD be submitted separately whenever practical. When they
cannot be separated, the combined change MUST identify which edits preserve behavior, which alter
behavior, and why separation would create greater risk or prevent delivery. Unrelated cleanup is
outside feature scope.

### VIII. Document Migration Impact
Every architectural change MUST document affected components, data and API compatibility, rollout
sequence, coexistence period, operational risk, rollback procedure, and required updates to tests
and project documentation. An architectural change without this migration-impact record MUST NOT be
approved.

### IX. Treat Existing Behavior as Specification
Observed production behavior, including legacy edge cases and user-visible flows, MUST be treated as
part of the product specification unless an approved requirement explicitly supersedes it. When
documentation and behavior differ, the discrepancy MUST be recorded and resolved before changing
the behavior.

### X. Keep the Build and Tests Green
Generated and hand-written implementations MUST compile. The smallest relevant tests MUST pass
before broader validation, and all existing applicable tests MUST remain passing. Release-sensitive
changes MUST also account for R8, resource shrinking, signing compatibility, and the first
authenticated production API path; a Debug-only success is insufficient evidence for such changes.

## Existing-System Constraints

- FaraZaman remains a single production Android application module unless an approved architecture
  change documents the migration impact.
- Persian locale and RTL behavior MUST be preserved across XML and Compose surfaces.
- The runtime-selected municipality `BASE_URL` MUST remain authoritative for municipality APIs and
  uploads; the central registry URL MUST NOT replace it in feature code.
- Existing OIDC/password authentication, encrypted token storage, authenticated Retrofit/OkHttp
  flow, legacy SQLite ContentProvider, and preference-backed state MUST remain authoritative until
  an explicit migration is approved.
- Tokens, authorization headers, passwords, client secrets, and signing credentials MUST never be
  logged or committed. The permanent release signing identity MUST NOT be regenerated or replaced.
- Network, database, and expensive file work MUST stay off the main thread, and lifecycle-owned
  resources MUST be released.

## Incremental Development and Quality Gates

1. Locate the affected workflow through `docs/CODEMAP.md` and inspect only its relevant context and
   implementation paths before proposing changes.
2. Record current behavior and compatibility constraints, including relevant backend contracts,
   selected-server routing, authentication, persistence, Persian/RTL UI, and Release behavior.
3. Implement the smallest coherent increment. Keep behavior-preserving refactors separate when
   practical and avoid unrelated formatting or cleanup.
4. Add or update tests for changed logic and contracts. Run the smallest relevant test or build
   first, then the broader checks proportionate to risk.
5. For architectural changes, attach the migration-impact record required by Principle VIII and
   obtain explicit approval before implementation.
6. Update project context, code maps, or release notes only when confirmed architecture, important
   locations, or behavior changes.

## Governance

This constitution governs specifications, plans, tasks, implementation, and review for FaraZaman.
Where another local practice conflicts with it, this constitution takes precedence unless a more
specific mandatory security or legal requirement applies.

Amendments MUST be proposed in writing with the exact principle or section changes, rationale,
compatibility impact, and any required migration plan. Approval by the project owner or designated
maintainers is required before adoption. Amendments take effect only after this file's version and
Last Amended date are updated and the Sync Impact Report records the change.

Constitution versions follow semantic versioning: MAJOR for backward-incompatible governance changes
or principle removals/redefinitions, MINOR for new principles or materially expanded obligations,
and PATCH for clarifications that do not change obligations.

Every specification and plan MUST include an explicit constitution check. Every implementation
review MUST verify applicable principles, compatibility constraints, migration documentation, build
success, and test results. Any exception MUST be documented with its scope, owner, expiration or
remediation condition, and approval; undocumented exceptions are non-compliant.

**Version**: 1.0.0 | **Ratified**: TODO(RATIFICATION_DATE): original adoption date is unknown | **Last Amended**: 2026-09-01

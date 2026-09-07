# Screen Migration Definition of Done

## Architecture

- [ ] Immutable State and typed Event contract
- [ ] Side effects and dependencies outside Screen
- [ ] Material 3 foundation under FaraZamanTheme
- [ ] No significant hard-coded design tokens or parallel styling
- [ ] Fz component policy and business→semantic mapping followed
- [ ] Feature Shared Components were extracted only after demonstrated reuse and remain feature-owned

## Preview and Accessibility

- [ ] Independent state previews
- [ ] Persian/RTL, Light/Dark, large font and relevant device sizes pass
- [ ] Touch target ≥48dp, content description, role and disabled/error semantics pass
- [ ] Contrast ≥4.5:1 for normal text and ≥3:1 for large text passes
- [ ] Automated screenshot golden verifies with deterministic fixtures

## Parity

- [ ] Behavioral Parity: data/actions/navigation/validation/loading/error/lifecycle/permissions/business rules
- [ ] XML appearance used as comparison baseline while Compose follows Farazaman Material 3 Design System
- [ ] Visual Intent Parity: branding/information hierarchy/usability; pixel parity is not required
- [ ] Intentional visual differences documented in Migration Record

## Validation and Cleanup

- [ ] Unit/UI/build gates pass
- [ ] Legacy bridge removal condition met
- [ ] System-critical runtime flag defaults to Compose, is support-protected and has a removal condition
- [ ] References verified and legacy resources removed
- [ ] Migration status set to Migrated with rollback evidence

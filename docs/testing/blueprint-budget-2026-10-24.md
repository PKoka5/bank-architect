# Blueprint review budget checkpoint

24 October 2026, uncommitted development above published 0.9.8 source
`675ccbd0e708ed6ea3396f0f35ff54ad96dfea21`. Continues the
[first 2.0 slice](blueprint-review-2026-10-23.md); no publication is authorized.

## Changes

- One projection-grouping path retains separate run atoms, exact ordered stage
  signatures and complete row groups. One canonical width-enumeration helper
  serves generation and group validation. Identity, allowed-width and complete
  canonical-candidate checks remain in place.
- Shared target application preserves input order, existing-lock early returns,
  immutable results and Main precedence: coins, graceful, runes, quick tools.
- Main and Teleports use the same charged-jewellery metadata query.
- Potion and resource layout facts use one family/kind/value check. Errors retain
  item ID and expected/actual facts; diagnostic wording now uses generic
  `Semantic` and `value` instead of separate potion/resource labels.
- Role scores reuse the existing accuracy and power accessors. Per-axis negative
  clamps, Prayer, defence/bonus arithmetic and null rejection remain unchanged.
- Category previews wrap their resulting item order once. Branch order,
  `plainRun` updates, row completion and physical expansion are unchanged.

No item tables, presets, classification policies or simulation baselines changed.
Independent read-only reviews found no behavioral issues in these changes.

## Verification

Offline Java 11 `gradlew build` succeeded in 56 seconds: **2,138 tests**, zero
failures/errors/skips. All **1,950 scenario banks completed**. All four existing
simulation fingerprints matched. The twelve new regression tests cover separate
horizontal/vertical families with identical member signatures, required metadata
failures and mismatches, and explicit combat-role scores with mixed negative
stats. No compiler warning appeared in this incremental build; the pre-existing
sidebar unchecked-operation note remains a known earlier-build observation.

`git diff --check` passes. Source/resources and test/resources are unchanged in
this checkpoint; new tests and documentation are not part of the runtime JAR.

Logs:

- `build/tmp/layout-projection-focused-2026-10-24.log`
- `build/tmp/blueprint-budget-build-2026-10-24.log`
- `build/tmp/blueprint-budget-size-2026-10-24.log`

## Review budget

Highest local estimate: **196,998**, leaving **3,002 estimated tokens** below
200,000. This pass reclaimed **1,083** against the previous 198,081 checkpoint.
Other estimates: cl100k preserved 194,680 / collapsed 196,710; o200k preserved
194,960. Calibration remains the maintainer's 200,414 at
`def1e856e101ff0e57dd96adccab2cf1d886b076`.

These are main-Java-only estimates, not the official Hub count. Tests/docs were
reviewed separately and no runtime data was moved out of Java to obtain this
reduction. Recheck the budget after further features and before any publication;
larger 2.0 stages need their own headroom plan.

Local artifact: `build/libs/ironman-bank-architect-0.9.8.jar`, **1,124,309 bytes**,
SHA-256 `9059b45cb6316cf77c0058940f210ef54112b654d402f2ad92c8bad9d4e61fe6`.
The filename retains the published base version; this is a different local
development artifact. No commit, push, tag, release or Hub update was made.

# Selected GitHub fixes - 22 October 2026

Local changes above published 0.9.6 source
`d0c4348921d2ca71883f2dd9a5bcadaa4c1c015d`, together with the
[blueprint-tab reorder work](blueprint-tab-reorder-2026-10-22.md).
These changes have not been committed, pushed, merged or published.

## Selected scope

- [Issue #46](https://github.com/PKoka5/bank-architect/issues/46) /
  [PR #47](https://github.com/PKoka5/bank-architect/pull/47): exact Obsidian
  platebody ID 21301 now has progression tier 3, matching the legs. The primary
  Melee body position favors it over Rune bodies; Fighter torso still wins when
  available. The exact Wiki revision is recorded in the development research TSV.
- [Issue #48](https://github.com/PKoka5/bank-architect/issues/48) /
  [PR #49](https://github.com/PKoka5/bank-architect/pull/49): adopt the targeted
  reviewed-placeholder correction, retaining the existing Alch policy. Reviewed
  stock may itself be a placeholder; known tool, elemental staff, Mystic-colour
  and slash-weapon replacements accept recorded bank placeholders as evidence.
  The existing same-style/slot higher-tier armour proof remains available.
- No GE-price eligibility policy was added. Unreviewed stock still needs its
  original non-placeholder, quantity, High Alchemy value, actual-ownership,
  dominance and backup evidence. Unknown source metadata remains ineligible.
  Existing session retention and its invalidation conditions remain unchanged.
- The shield/defender regression is covered without changing equipment scoring.
  The preferred defender remains selected when withdrawn with its placeholder.
- [Issue #45](https://github.com/PKoka5/bank-architect/issues/45), custom wider
  bank grids, stays deferred. README now states the standard eight-column scope.

A recorded placeholder is planning evidence, not current inventory. It retains
quantity zero and does not add to owned tag counts. Releasing the upgrade's
placeholder removes that evidence; ordinary classification resumes on a fresh
analysis. Manual corrections, saved editor destinations and captured physical
orders still take priority. No real bank action is automated.

## Verification

- Java 11 offline full `build`: **1,910 tests**, zero failures, errors or skips.
- **1,950 scenario banks completed**: 150 standard scenarios and 1,800 aggregate
  cleanup scenarios. All four existing simulation fingerprints match unchanged;
  no baseline was regenerated.
- New tests cover both Main and Ironman, actual primary body-cell placement,
  Obsidian and replacement placeholders, manual corrections and captured order.
- Six reviewed stock/replacement families exercise fresh analyses through
  quantities 25 -> 1 -> 0 placeholder -> 25, with real and withdrawn upgrades,
  released placeholders, missing values, disabled gathering and custom placement.
  Unreviewed quantity/value/ownership/dominance safeguards remain tested.
- Existing tests with the old reviewed-placeholder policy were updated precisely.
  Placeholder stock still counts as zero owned items. A captured bank keeps its
  physical order even when the automatic functional tag changes.
- Independent read-only review found no blocker in the selected production diff.
- Removed five unused getters and their unused layout identity fields/parameters
  after repository-wide reference review. Retained review-entry fields used by
  compact text. Existing tests cover guide routes, block editing and summaries.
- `git diff --check` passes. The existing sidebar unchecked-operation compiler
  note remains; there is no new dependency or runtime permission.

Logs are local development artifacts:
`build/tmp/selected-fixes-build-2026-10-22.log` and
`build/tmp/selected-fixes-review-size-2026-10-22.log`.

## Review size and artifact

The development-only estimator reports 194,939 / 196,625 (cl100k) and
195,272 / **196,968** (o200k). Use the highest figure: **3,032 estimated tokens
of headroom** below 200,000. Calibration remains the official 200,414 count for
`def1e856e101ff0e57dd96adccab2cf1d886b076`; there is no new official Hub count.
This is an estimate of main Java changes, not certification of Hub eligibility.

Changes outside that scope were inspected separately: the bundled tier catalog
adds one numeric row; its provenance is development-only. Tests and documentation
are not runtime code. The JAR has 264 entries and no test, simulation, research,
tooling or documentation entries.

Current local development JAR (still named `ironman-bank-architect-0.9.6.jar`):
1,115,435 bytes; SHA-256
`50da2d9967c5b98156403cdf0b2edcfcf03968f3fe5f3fd33429cb290442581b`.
It differs from the published 0.9.6 artifact.

## Remaining live check

Check the combined work in game: move a complete blueprint destination, confirm
that a bundled preset becomes Custom, withdraw/return reviewed Alch stock and its
replacement with placeholders enabled, then verify saved personal placement.
Publishing and closing the corresponding issues follow the in-game check.

The later [Wiki strategy pilot](wiki-strategy-alch-2026-10-22.md) adds the
seven-item d'hide correction: 1,936 tests and 1,950 scenarios pass, with a highest
local estimate of 197,035. Figures above describe the earlier selected-fix checkpoint.

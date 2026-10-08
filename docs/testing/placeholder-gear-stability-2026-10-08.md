# Placeholder gear stability — 8 October 2026

## Reproduction and behavior

The gear sorter and canonical bank reader already preserve placeholders and their
equipment stats. The disruption came from fresh Alch decisions: withdrawing owned
Bandos left placeholders, which could return older Rune gear from Alch to Combat.
That changed the blueprint and restarted guidance even though the preferred set's
logical bank slots remained present.

`BankAnalysis` now remembers Alch destinations from its latest successful analysis.
It can retain those destinations across a withdrawal when logical IDs and physical
occurrence counts remain the same, a placeholder is present, and all preferences,
assignments, equipment facts and prices still match. Explicit player choices win.
Changed positive quantities of an Alch candidate are evaluated again, rather than
retaining a decision based on an old stock quantity. Placeholders retain quantity zero.

This is analysis-session memory, not proof of current ownership. A fresh analysis
module with only placeholders cannot create new Alch decisions based on empty slots.
New/removed IDs or occurrences, changed preferences/facts, invalidation and plugin
restart require fresh decisions. Captured bank layouts remain the persistent option
for an owner-selected arrangement. No inventory/equipment reads were added.

History is copied under the analysis monitor and committed only by the latest
successful generation. Closed, invalidated or superseded work cannot publish history.
Gear stats and layout options now compare immutable values instead of object identity.

## Community follow-up

[Issue #41](https://github.com/PKoka5/bank-architect/issues/41) already matched the
published per-destination **Keep current order** behavior. Its existing tests were
rerun successfully. The owner authorized a reply and closure; the
[reply](https://github.com/PKoka5/bank-architect/issues/41#issuecomment-6060984460)
explains the checkbox and the issue is closed as completed. The 0.9.0 Hub update
was merged on 6 October in [PR #17853](https://github.com/runelite/plugin-hub/pull/17853).

The reported YouTube complaint says the bank became broken after following the
process but supplies no version, preset, screenshots or specific misplaced items.
It does not establish this withdrawal problem as its cause. Read-only investigation
found deterministic input ordering and existing guards for search/Bank Tags,
fillers, widget/container disagreement and unexpected transitions. The default BIS
grid deliberately uses filler gear to complete rows; alternate gear layouts exist.
The exact community reproduction remains unresolved.

## Review size and verification

The obsolete `NextMoveAdvisor` had no production callers; the live overlay uses
`TabRouteAdvisor`. It and its obsolete class-specific tests were removed. Relevant
builder and semantic-layout completion assertions now exercise the active advisor.
No live guidance feature or safety check was removed.

- **24 new Main/Ironman regression cases pass.** They cover withdrawn/returned
  preferred sets, repeated reopening, two complete BIS rows and completed guidance
  in Swap/Insert, actual owned counts, fresh placeholder-only decisions, released
  placeholders, new items, preset/options/stats/user-choice changes, changed stock
  quantities, duplicate physical occurrences, and queued or already-running work
  superseded by invalidation.
- The two core withdrawal regressions were also run against the original analysis:
  both fail with Rune gear returning to Combat. The fixed source was restored byte
  for byte before the successful full run.
- `gradlew.bat check jar exportFinalItemPlacements`: **1,427 tests**, no failures,
  errors or skips; **1,950 simulations complete** and all four reviewed hashes match.
  The existing Keep current order suite also passes independently.
- A read-only peer review checked context equality, history ownership and generation
  guards. `git diff --check` passes.
- Highest local calibrated token estimate: **196,597**, **3,403 headroom**. Calibration
  remains official **200,414** at `def1e856e101ff0e57dd96adccab2cf1d886b076`.
  The estimate covers main Java and is not the official Hub count; other changed
  resources/tests/build tooling/docs were inspected separately. The local jar has
  268 entries, contains no tests/tools/caches, and omits the obsolete advisor.

These changes remain local; no commit, release or Hub submission was requested.

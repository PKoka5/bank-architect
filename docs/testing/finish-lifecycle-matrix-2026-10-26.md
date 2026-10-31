# Completion lifecycle matrix and shared guide drawing

26 October 2026. Continues [the runtime cleanup checkpoint](finish-runtime-cleanup-2026-10-26.md)
under [the bounded completion plan](../plans/finish-1.0.md). This is a local
verification checkpoint, not release approval or closure of issue #48.

## Shared move rendering

`BankGuideOverlay` now shares endpoint geometry and cell/edge-badge drawing
between grid moves and item-to-tab moves. The separate no-visible-endpoint and
whole-tab branches retain their behavior, including the missing-geometry fallback
and source-first guidance when no drag is active. No guide action or bank access
was added.

Before the production edit, `DragScrollOverlayTest` was extended to cover both
Swap and Insert, dragging and stationary guidance, and four source/target
positions: above, fully visible, below and partly clipped. The test asserts
source/target highlight availability and absence of synchronization failures.
The 64 matrix images plus four existing scroll images were saved. After the
refactor **all 68 PNG hashes were identical**. This is a same-environment
before/after comparison, not a portable font-rendering golden test.

## Finite domain matrix: 180 cases

`CompletionLifecycleMatrixTest` runs five fixtures through Main and Ironman,
three modes (automatic, accepted proposal, captured physical layout), and six
independent event sequences. Each parameterized test creates fresh analysis and
starts from the fixture baseline. The known-item and unknown-ID additions each
use the original bank plus that single addition, rather than accumulating items.

| Fixture | Entries | Gear facts | Cases |
| --- | ---: | --- | ---: |
| `real-bank-fixture-2026-07-19.tsv` | 770 | Bundled catalog; no invented equipment stats | 36 |
| `reported-midgame-combat-85.tsv` | 85 | Existing frozen full stat vectors and classifications | 36 |
| `reported-void-combat-94.tsv` | 94 | Existing ID/quantity/placeholder subset; stats unknown | 36 |
| `main-bank-combat-2026-10-12.tsv` | 194 | Existing Combat subset with frozen classification/placeholder fields; stats unknown | 36 |
| Small beginner fixture in the test | 11 | Reviewed ordinary IDs; bundled catalog, stats unknown | 36 |

The three Combat subsets are not claimed to be complete player banks. Existing
normalized quantities are retained; the beginner fixture includes stacked coins
and runes. Catalog fallback remains available for unknown IDs. The captured mode
deliberately reverses each recommended tab and rotates destinations before
capturing, so its preservation checks do not merely repeat automatic order.

The six sequences assert:

1. Identical requests retain the same layout.
2. A reviewed new item and a separate unknown-ID control conserve bank facts;
   kept layouts retain old order/destinations and append the newcomer at the end
   of its recommended destination.
3. Withdrawal to a real zero-quantity placeholder and return preserve current
   quantities/flags; kept layout order and destinations remain intact.
4. Full removal and return leave no phantom entry and restore the kept target
   without rewriting dormant saved records.
5. A whole destination move remaps membership in automatic mode and preserves
   exact item order in kept modes, including saved routes.
6. Serialized plan/order reload, switching to another named profile and back,
   and a new `BankAnalysis` instance restore the selected target.

Every analysis checks conservation of IDs, occurrence counts, quantities and
placeholder state. Every resulting target is recognized as complete by the
actual tab guide in both Swap and Insert when presented with its physical bank
order. This validates target consistency; it does not simulate a human performing
all moves from every fixture's initial order.

## Coverage boundaries and existing evidence

The matrix uses the actual `BankAnalysis`, planner, capture/apply methods,
`BankLayoutProfiles`, `BlueprintOrderProfiles`, and tab guide. Its profile
roundtrip is domain-level: the accepted string is retained in a local map and
the context models preset/plan identity. It does not claim to exercise RuneLite
configuration events or all setting-precedence combinations.

Those integration contracts remain covered by `AcceptedBlueprintLifecycleTest`
and `ProfileOptionsSynchronizationTest`: real plugin storage, named-profile
independence, restart, stale queued operations, damaged snapshots and settings
that visibly pause kept layouts. Existing accepted-tab and editor tests cover
reverse/Main moves, dormant records and custom destinations. No such assertion
was removed or replaced by this matrix.

No Alch values are fabricated by the matrix. `FixedAlchRoutingTest` covers the
literal list, exclusions, disabled gathering, personal routes and captured tags;
`ReviewedAlchWithdrawalStabilityTest` covers Flexible stock, upgrade placeholders,
withdrawal/return and explicit choices. These focused banks cover the two modes
without multiplying every setting through the 180 cases. Existing endgame and
reviewed progression tests remain the evidence for BIS selection and set layout;
the matrix is not another source of combat-stat truth.

The 1,950 existing scenario banks continue to check guided sorting and
convergence. Their four report fingerprints remain frozen and unchanged.

## Final verification for this checkpoint

- Full Java 11 offline `build`: **2,599 tests**, zero failures/errors/skips.
- The lifecycle class accounts for **180**, all passing. The overlay visibility
  test adds two parameterized cases containing the 64 render states.
- All **1,950 scenario banks** pass with all four baseline fingerprints intact.
- No compiler warning/error in the final log; tracked whitespace check passes.
- Highest local review estimate: **196,042**, or **3,958 estimated headroom**.
  Other estimates: 192,851 / 195,698 / 193,196. This is 159 lower than the prior
  checkpoint; the 195,000 target still needs **1,042** estimated reduction.
- Calibration remains maintainer 200,414 at
  `def1e856e101ff0e57dd96adccab2cf1d886b076`. Tokenizer and complete Hub scope
  remain unknown. The new test class and docs were inspected separately from
  the estimator's main-Java scope; they are not included in its token saving.

Logs: `build/tmp/finish-guide-before.log`, `finish-guide-after.log`,
`finish-lifecycle-matrix.log`, `finish-lifecycle-full.log` and
`finish-lifecycle-size.log`. Render hashes: `build/tmp/finish-guide-baseline/images.json`.
Images: `build/reports/drag-scroll/`. Parameterized results:
`build/test-results/test/TEST-com.pkoka5.ironmanbankarchitect.organize.CompletionLifecycleMatrixTest.xml`.

Development JAR: **937,623 bytes**, SHA-256
`6308c4d2d770024d44a8b84ddcd2de9f9f56d20b3afe51ed8e921deffe093b23`.
The lifecycle test class is absent from the JAR. No version, commit, branch,
published artifact or Hub submission changed.

## Remaining finish work

The automated finite matrix is complete. This does not complete every finish
criterion: token headroom target, the exact #48 evidence, the owner's combined
in-game check, maintenance handover/rehearsal and explicit final publication
remain open. Existing nearby shield/defender tests do not justify claiming the
reporter's unspecified case fixed.

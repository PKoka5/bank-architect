# Completion round 1: first verified slice

The owner authorized implementation of [the completion plan](../plans/finish-1.0.md)
on 26 October 2026. This checkpoint is progress within round 1, not completion of
the round or authorization to publish.

## Shield/defender investigation (#48)

Read the original issue, follow-ups and all five uploaded screenshots. No new
reporter response identified the exact shield/defender variants, settings or
retained-placeholder state. The screenshots are supporting visual evidence, not
an exact item-ID export or a full bank fixture.

The existing withdrawal regression concentrated on the defender becoming a
placeholder with an Anti-dragon shield present. The new
`MeleePrimaryEquipmentTest.withdrawingEitherDragonfireShieldOrDefenderKeepsTheReviewedOffhandOrder`
also withdraws the shield itself and returns it. It covers:

- Main and Ironman;
- Anti-dragon shield 1540 and charged/uncharged Dragonfire shields 11283/11284;
- Dragon defender 12954, ornament variant 19722 and Rune defender 8850;
- the existing independent frozen-stat subset and entirely missing stats;
- both withdrawal directions, with quantities 1 -> 0 placeholder -> 1.

All 216 analyzed states retain the same Combat item order and the reviewed
primary offhand for that pair. The stronger shield still beats the lower Rune
defender; there is no blanket defender override. The existing test inventory
does not have full frozen stats for every cosmetic/state variant, so some
combinations intentionally exercise the reviewed fallback with partial facts.
Quantities, placeholders and item counts are checked at each step.

These focused cases pass on the current planner without a production scoring
change. They do not establish that the reporter's exact case is resolved. #48
remains open for the requested identities and export. This task did not post
another duplicate follow-up or close the report.

## Quick-tool data extraction

`IronmanQuickToolSelector` now loads its fixed identities from
`organize/quick-tool-tiers.tsv` through the existing `OrderedItemFamilies` and
`RequiredResource` infrastructure. The table has **49 identities in 25 tiers**.
Hammer/chisel/pickaxe/axe selection order, equivalent-variant preference order,
quick-access ranks and pickaxe/axe replacement checks are preserved. No algorithm
or stored setting format changed. Loaded family lists are immutable.

An independent extraction from the Java literals at published
`b3583ca8e48bedb37514e89006cb677e5b7ea75a` matched every row and member, in order.
The normalized declaration fingerprint is
`624e1fd43e6aa6f8902d0812d56f7a4a7a862330aad5ddf7262dec8a87e64af5`.
`OrderedItemFamiliesTest.quickToolTiersMatchThePublishedJavaLiterals` pins that
baseline; existing tool selection, replacement and placeholder tests retain
behavior coverage. Existing malformed-table tests cover the shared parser.

This is reviewed static data, not executable rules moved into a resource. The
complete Hub scope remains unknown; the source estimator does not certify that
moving data reduces the official count by the same amount.

## Verification

- Focused shield/defender and reviewed Alch-withdrawal tests pass.
- Full Java 11 offline `build`: **2,419 tests**, zero failures/errors/skips.
- All **1,950 scenario banks** pass. All four simulation fingerprints remain
  unchanged; no reference was regenerated.
- No compiler warning in the final build log. Tracked and new-file whitespace
  checks pass. The estimator emits routine Git line-ending notices separately.
- Final highest local main-Java estimate: **197,529**, leaving **2,471 estimated
  tokens**. Other estimates: 194,512 / 197,181 / 194,856.
- The highest estimate fell by **234** from 197,763. This is a modest verified
  improvement, not achievement of the 195,000 target. A further **2,529** estimated
  reduction is still needed to reach that target.
- Calibration is unchanged: maintainer 200,414 at
  `def1e856e101ff0e57dd96adccab2cf1d886b076`. Tests/resources/docs were inspected
  separately from the estimator's main-Java scope.

Logs: `build/tmp/finish-r1-shield.log`, `build/tmp/finish-r1-full.log` and
`build/tmp/finish-r1-size.log`.

Local development JAR: `ironman-bank-architect-0.9.9.jar`, **939,852 bytes**,
SHA-256 `196d3017c154b4e80e8b35b92948e100dce531a701314e1bd98808384f21d8c2`.
The published artifact is untouched. No commit, push, version bump or release.

## Remaining work

Round 1 still needs the larger behavior-neutral headroom reduction, an honest
disposition of the exact #48 case and the owner's combined in-game check.
Rounds 2 and 3 (finite lifecycle matrix and maintenance handover) have not been
completed by this slice. No unused-method deletion was made from heuristic
search results: framework callbacks and test-supported invariants must remain.

The preset audit found that the live chooser and share-code decoder accept only
Main/Ironman; the decoder reads the enum and then explicitly rejects other types.
PvM/PvP/Skiller definitions and mapping still have internal test consumers. Their
removal is a separate scope/coverage decision, not an unexamined token shortcut.

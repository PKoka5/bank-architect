# Main diary rewards grouped with Teleports

Owner-requested placement policy, 26 October 2026. Local, unpublished change.

Main now routes all 48 regional achievement-diary tiers and both completion-cape
IDs (19476, 13069) to the Teleports tag. The tag can be moved through the existing
planner. Group membership does not claim that every low-tier reward has its own
teleport: original catalog categories, subcategories and functional facts remain
unchanged. Quest props without a reward tier and the cosmetic diary hood retain
their original roles. Ghommal's hilts retain their existing separate policy.

The existing regional diary grid is retained in Main's packed utility layout.
Ironman routing and geometry use the previous path. No new tag, setting, network
access or bank action was added. Accepted-blueprint rule generation advances to
11; older supported snapshots stay readable and active with matching context.

Explicit item corrections still take priority. A legitimate pre-change Main
editor route remains active when its original automatic tag matches the old
policy and its target tag still occupies the saved destination. A dormant route
is not activated merely by this change. Captured banks keep their physical
positions while outdated automatic labels may refresh, consistent with existing
capture behavior. Accepted snapshots retain their explicitly kept tags/order.

The saved-route retention predicate now receives the individual destination as
well as the ID, so migration can distinguish captured records and actual old
Main classifications. Fixed-Alch retention keeps its existing condition; the
full Fixed/Flexible and snapshot regression suite still passes.

`MainDiaryPlacementTest` adds six tests covering every regional tier and both
capes, placeholders, mixed rune/teleport grid geometry, moved tags, explicit
corrections, legacy and dormant editor routes, accepted positions, Ironman and
non-reward props. Existing Main-only expectations were updated in the functional
placement/export tests and six rows of `reviewed-item-roles.tsv`. Their original
catalog facts and Ironman expectations were retained; the rationale records the
owner's grouping policy separately from intrinsic teleport functionality.

Verification:

- Full Java 11 offline build: **2,605 tests**, no failures/errors/skips or compiler
  warnings. This includes the 180-case lifecycle matrix.
- All **1,950 scenario banks** pass; all four frozen simulation fingerprints
  remain unchanged.
- Tracked whitespace check passes. Tests and the reviewed-role fixture were
  inspected outside the estimator's main-Java scope.
- Highest local review estimate **196,342**, leaving **3,658 estimated tokens**.
  Other estimates: 193,189 / 196,006 / 193,526. The 195,000 target remains open.
- Calibration remains maintainer 200,414 at
  `def1e856e101ff0e57dd96adccab2cf1d886b076`; the official Hub scope/tokenizer
  remain unknown. This estimate does not certify a submission.

Final logs: `build/tmp/main-diary-full-final.log` and
`build/tmp/main-diary-size-final.log`. Earlier focused/full runs exposed old
Main-role expectations and overly broad saved-tag retention; both were corrected
before the successful final build. No simulation reference was regenerated.

No commit, push, version bump or publication. Remaining completion work from
the lifecycle checkpoint is unchanged, including additional review headroom,
the exact #48 evidence, owner in-game check and maintenance handover/rehearsal.

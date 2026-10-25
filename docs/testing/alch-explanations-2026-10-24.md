# Alch explanation checkpoint

24 October 2026, local 2.0 development above published source
`675ccbd0e708ed6ea3396f0f35ff54ad96dfea21`. The owner wants to continue finishing
the feature work; no publication is planned.

## Behavior

Flexible Alch returns a nullable explanation directly from its existing policy
branch. A successful explanation chooses Alch; null retains normal placement.
No separate heuristic reconstructs a reason after classification.

- Exact tool tiers and staff/Mystic/slash roles return the qualifying recorded
  item ID. Their existing boolean APIs delegate to that lookup. The same-tier
  tool check still precedes recording that tier, preserving ornament equivalence.
- Mystic keeps the same canonical and explicit Combat-pin precedence. A colour
  keeper is described as a reviewed replacement, not a strict upgrade.
- Reviewed armour distinguishes progression facts from full-stat coverage.
- Unreviewed bulk/spare stock names its actual full-stat witness and separately
  reports the count of higher-scoring bank entries. Different items can provide
  those two proofs; the explanation does not merge them.
- Reviewed witnesses say whether the item is physically present or represented
  by a bank placeholder. Unreviewed witnesses retain actual-ownership requirements.
- Fixed-list and Dragon-halberd exceptions do not invent a witness. Personal,
  saved and retained category reasons preserve precedence. Retained historical
  Alch stores no historical witness and does not claim one.

Private gear evidence buckets use ID order for stable witness selection. Only
membership/any-match/count decisions use those buckets, so their order cannot
change routing. Slash witnesses follow the existing canonical upgrade list.
Reasons appear through existing blueprint tooltips and destination-change rows;
reason-only changes do not increase destination-change counts.

## Verification

Offline Java 11 `gradlew build` passed in 50 seconds: **2,183 tests**, zero
failures/errors/skips. All **1,950 scenario banks completed**, and all four
unchanged simulation fingerprints matched. No new compiler warnings appeared.
Independent read-only review found no correctness or policy issues.

New coverage comprises **11 helper tests** and **34 Main/Ironman preview cases**:
exact recorded witness IDs; all **1,811 frozen role decisions**; incompatible
families and ornaments; Mystic pins; real/placeholder/removal cases; armour
tradeoffs; separate stat/score witnesses; fixed/special/manual/saved/retained
paths; permutation stability; duplicate physical quantities and change-count
neutrality. The existing warm-analysis reason expectation now names Dragon mace.

No item tables, simulation baselines or resources changed. `git diff --check`
passes. Test and documentation changes were inspected separately from the
main-Java estimate. No game actions or additional runtime data sources were added.

Logs: `build/tmp/alch-explanations-focused-2026-10-24.log`,
`build/tmp/alch-explanations-final-build-2026-10-24.log`,
`build/tmp/alch-explanations-final-size-2026-10-24.log`.

Highest local estimate: **197,390**, leaving **2,610 estimated tokens** below
200,000. Other estimates: cl100k preserved 195,092 / collapsed 197,088;
o200k preserved 195,389. Calibration remains the maintainer's 200,414 at
`def1e856e101ff0e57dd96adccab2cf1d886b076`. These are not official Hub counts.

Local artifact: `build/libs/ironman-bank-architect-0.9.8.jar`, **1,125,279 bytes**,
SHA-256 `5005a47ec1fd4329e12d73bbd43da78da461b768141551bb9e4994170248d6e5`.
This retains the base filename but differs from the published release.

# Final item placement audit — 6 October 2026

## Scope and evidence

The new development exporter runs the **actual preview builder** for both default
Main and Ironman presets. It records catalog metadata, the base mapped category,
effective metadata, the final layout tag and physical tab, rather than treating a
catalog category as proof of the final placement. Separate context columns disclose
unavailable runtime stats/prices and the absence of upgrades or personal choices.

The exporter covers 32,585 positive-ID named registry records per preset, or 65,170
isolated scenarios. It excludes 1,157 explicit cache/null-name records and the exact
bank filler. Included records remain bankability-unverified; these numbers are not
a count of real bankable items. CERT records are retained without inferred normalization.
Invalid rows and duplicate IDs fail before output; individual planner failures stay visible.

The complete ledger keeps missing-source records, unlike the historical prioritized
findings list. The available independent Wiki identity snapshot was collected on
**2026-07-15** and remains visibly stale. Exact cached identities do not establish
current functions, disposal eligibility or reacquisition. Source dates/revisions are
supplied provenance, accompanied by input hashes, not a verified fresh-fetch manifest.

The curated reference is independent of the exported classification. It includes
previously reviewed cosmetic/functional cases, today's corrections, actual currencies
and neighboring noncurrency examples. Expected default placement is our product policy,
which can differ from the item's function: Frog token remains near cosmetic rewards,
an unbound Tiara goes to Tools, and Ironman gathers some diary utilities on Main.
The reference records the sources and rationale for these decisions. It is initial
curated coverage, not a claim that the whole catalog has been manually reviewed.

## Thirteen additional exact-ID corrections

The earlier Vale, Rada/Ghommal/Western and five activity-currency fixes are preserved.
This audit adds the following exact-ID corrections without changing preset definitions:

| IDs | Item | Corrected function / placement | Evidence |
| --- | --- | --- | --- |
| 9768, 9769 | Hitpoints cape, trimmed | Gear / cape, instead of Currency | [Cape equipment, revision 14905372](https://oldschool.runescape.wiki/w/Cape?oldid=14905372) |
| 20017 | Ring of coins | Cosmetic transformation, instead of Currency | [Transformation rings](https://oldschool.runescape.wiki/w/Ring#Transformation_rings) |
| 22842 | Pearl barbarian rod | Fishing tool, instead of raw resource | [Alry's shop, revision 14893175](https://oldschool.runescape.wiki/w/Alry_the_Angler%27s_Angling_Accessories?oldid=14893175) |
| 24719 | Hallowed token | Agility time consumable in Tools, instead of Currency | [Token, revision 15189742](https://oldschool.runescape.wiki/w/Hallowed_token?oldid=15189742), [Hallowed goods](https://oldschool.runescape.wiki/w/Mysterious_Hallowed_Goods?oldid=14771101) |
| 28152 | Nature offerings | Woodcutting supply, instead of Combat | [Forestry update](https://oldschool.runescape.wiki/w/Update%3AForestry%3A_The_Way_of_the_Forester_-_Part_One) |
| 4485 | White pearl | Quest item for manual review, instead of Currency | [Mountain Daughter transcript](https://oldschool.runescape.wiki/w/Transcript%3AMountain_Daughter) |
| 13108, 13109 | Wilderness sword 1/2 | Weapon with no own teleport; Ironman Frequently Used, Main Gear | [Tier-specific diary rewards](https://oldschool.runescape.wiki/w/Achievement_Diary/Rewards#Wilderness) |
| 13137, 13138 | Kandarin headgear 1/2 | Reusable light source; Ironman Frequently Used, Main Tools | [Wedge reward transcript](https://oldschool.runescape.wiki/w/Transcript%3AThe_%27Wedge%27) |
| 31946 | Echo pearl | Construction material, instead of Currency | [Fathom pearl recipe, revision 15119944](https://oldschool.runescape.wiki/w/Fathom_pearl?oldid=15119944), [exact Echo ID, revision 15017853](https://oldschool.runescape.wiki/w/Exchange%3AEcho_pearl?oldid=15017853) |
| 22840 | Golden tench | Cosmetic collectible, instead of generic Cleanup | [Official Post Kebos changes](https://oldschool.runescape.wiki/w/Update%3APoll_Blog%3A_Post_Kebos_Changes) |

Exact identity evidence is the July snapshot; indexed primary role sources were reviewed
on 6 October. Direct Wiki item pages were often blocked; a current complete API snapshot
was not retrieved. Historical linked revisions must not be described as newly fetched
game data. Nature offerings revision 15236763 is dated 20 June; Hallowed token revision
15189742 is dated 22 April.

Pearl rod IDs 22844, 22846 and 23122 retain their existing Tools role. Molch and Abyssal
pearls remain actual Currency controls. Wilderness sword and Kandarin headgear tiers
3/4 retain Teleports. Same-name certs, icons, placeholders, quest stages and restricted
copies never inherit a canonical override solely because their names match.

No rule recommends discarding White pearl. Cosmetic items and quest utilities remain
protected from opportunistic gear promotion or Alch classification. Captured physical
layouts and explicit player tags retain their existing priority; ordinary old editor
routes still use their original-tag guard as documented in the currency fix.

## Future game update workflow

The [offline tracker](../../tools/research/item-classification-audit/CHANGE_TRACKING.md)
accepts an explicitly versioned RuneLite identity index and independent source facts.
It detects new/deleted IDs, renamed items and changed supplied fields on existing IDs.
Snapshots retain missing/partial facts with their old provenance; comparisons never
silently replace a baseline or approve item roles. Dates, coverage gaps and stale facts
remain visible. Observed classifier/export metadata is excluded from independent facts.

Before a release, refresh identities and gameplay facts, review changes and relevant
variant families, update exact-ID facts and independent expectations, run normal checks
plus the final-placement ledger, and deliberately save the next observation baseline.
Unknown or ambiguous use stays manual review; prices or the existence of a high-alch
value cannot establish that an item should be removed. Player choices remain leading.
No network or telemetry is added to the plugin.

Positive runtime equipment metadata can still place an unregistered item in Gear.
The original UNKNOWN/UNCATEGORIZED catalog state now blocks **automatic Alch**,
even when stats, price, stack size and stronger owned gear would otherwise qualify.
Explicit player Alch assignments remain valid. With no runtime stats, unknown items
retain the manual-review destination.

The tracker cannot detect a changed game function when the supplied source fields do
not change. This makes source refresh and update-note review part of the workflow,
rather than a promise of automatic perfect classification.

The offline workflow was exercised against the explicitly cached RuneLite API
**1.13.1** source jar (SHA-256
`08d297f6a6cef7e7b808cb538b54083a698726bcf3a522a7e533340e219383a5`).
This is not a claim of the latest game identities. The index contains 34,606 positive
IDs versus 33,742 registry IDs and reports 864 new identity candidates. These include
explicit cache records and bankability-unverified records; none were automatically
imported as gameplay rules. The 14,295 namespace/inferred-classification differences
also reflect migration between registry and research-index formats, not verified game
function changes. The historical identity source covers only 15,506 index records
and remains partial/stale. Comparing did not overwrite the saved observation baseline.

## Remaining review work

The ledger distinguishes source identity coverage from manually reviewed roles. The
Gear/non-equipable signal includes legitimate cannon parts, ammunition, incomplete
items and internal records, so its total is not an error count. Cooking candidates
include 1869–1881, 1921, 2118, 2152–2158 and 7062–7084. Several chopped ingredients
and toppings are edible; no blanket ingredients rule was applied without exact evidence.

Quest-flagged currencies such as Champions token, Sphinx's token, Dragon token and
special Coins/marks records still require exact function and bankability checks. Neither
a quest flag nor missing source data proves an item is useless. These candidates remain
visible for the next source-review batch; full current-catalog manual review is ongoing.

## Verification

Final follow-up verification ran on **8 October 2026**, including the
[placeholder stability fix](../testing/placeholder-gear-stability-2026-10-08.md).

- `gradlew.bat check jar exportFinalItemPlacements`: passed, **1,427 tests**, no
  failures, errors or skips, and **1,950 completed bank simulations**.
- All four simulation report hashes match the explicitly reviewed baselines.
  The thirteen corrections changed only two of 150 single-bank report rows:
  Nature offerings seed 20260729 took 69 moves instead of 71; Pearl barbarian rod
  seed 20260749 took 205 instead of 206. All scenarios completed.
- Single-bank Cleanup records were unchanged. In the aggregate, Golden tench
  left Cleanup in six occurrences and White pearl entered quest review in three.
  All 9,284 shared surviving records matched. The resulting 9,285 distinct IDs
  and 46,497 occurrences agree with the reviewed metadata.
- A separate reproduction with only these thirteen overrides temporarily removed
  reproduced all four prior report hashes exactly. The production resource was
  restored byte for byte; baseline changes were made deliberately after reviewing
  these deltas, rather than regenerated from a failing check.
- Both Main/Ironman unknown-item regression cases fail with the automatic-Alch
  guard removed and pass with it restored. Both withdrawal regressions fail against
  the old analysis and pass with the session fix. These intentional negative runs
  are separate from the successful final test result.
- The final export includes **65,170 scenarios**, no pipeline failures. All **170
  reviewed scenarios for 85 independently reviewed IDs match**. The complete ledger
  retains 30,840 unreviewed scenarios with exact cached identities and 34,160 with
  no exact source; their coverage is not implied by the reviewed subset.
- Both self-contained PowerShell tool suites pass: final-placement context/source
  validation and independent mismatch guards; changed-item tracking, retained
  provenance, malformed/stale inputs, determinism and explicit baseline writes.
- The local jar has **268 entries**, no tests, simulators, research tools, caches,
  documentation or obsolete advisor. SHA-256:
  `75d9e4b726a9213d9022671048391a1ceb93eed77520e8b268f208ad32716a00`.
- `git diff --check` passes. Dependency versions and runtime permissions are unchanged.

Highest calibrated local review estimate: **196,597**, leaving **3,403 tokens** below
200,000. Calibration remains the maintainer's **200,414** for
`def1e856e101ff0e57dd96adccab2cf1d886b076`, reported 30 September. This main-Java
estimate is not the official Hub count. Outside its scope, exact-ID/name-rule
resources, independent expectations, tests, Gradle exporter/baseline edits and
development documentation/tools were inspected separately. Research caches and tools
are excluded from the plugin jar. Recheck both the estimate and these changes before
publication; record any new official count with its exact source revision.

No commit, push, release notice or Plugin Hub submission is part of this local audit.

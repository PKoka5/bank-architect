# Ironman Herblore: complete recipes and compact leftovers

## Behavior

The user reported confusing Herblore layouts when an Ironman owns only part of a recipe. Previously, two matching recipe cells were enough to claim a fragment; optional filler could place other items between fragments. Incomplete high-tier recipes also claimed shared secondaries before a lower complete recipe could use them.

The automatic Ironman layout now claims only complete recipes occupying eight separate physical bank cells:

`grimy herb | clean herb | herb seed | unfinished potion | secondary | dose 3 | dose 2 | dose 1`

Complete rows lead the tab in herb progression order. They must precede the compact remainder to stay aligned without invented gaps. Leftovers follow in unbroken runs: grimy herbs, clean herbs, herb seeds, unfinished potions, secondaries, part doses and other items. Herb stages use canonical progression where available; dose families retain descending dose order. Non-herb Farming items retain the existing Farming layout after the Herblore runs.

Only accepted complete rows reserve shared ingredients. Where two complete recipes share one actual ingredient stack, the higher-tier recipe owns that cell once; the lower recipe stays in the compact remainder. Placeholders count as present cells. Consolidated items representing multiple physical bank slots cannot complete an eight-cell row and are retained in the remainder for later physical expansion.

The original configuration key and serialized `fillHerbloreRows` field are retained. Its UI label is now **Complete Herblore recipe rows**: enabled means complete rows plus compact leftovers; disabled means every item grouped by kind. Explicit tag sequences or relevant saved block orders use the compact path so those choices take priority. Unrelated Combat block orders do not suppress Herblore rows. Saved or captured physical blueprint order is still applied last.

Main continues to use the existing `layoutMain` sorter and keeps part doses with Supplies. Five exact ingredient subtype corrections apply to both presets: Red spiders' eggs 223, Limpwurt root 225, Snape grass 231, White berries 239 and Mort myre fungus 2970 now have `HERBLORE/secondary`. Main's recipe policy is unchanged; these verified subtypes can improve its secondary grouping.

Recipe secondary matching uses exact ordinary IDs instead of name substrings. This excludes raw Unicorn horn 237 (Antipoison needs dust 235) and Chocolate bar 1973 (Energy needs dust 1975), and does not mistake seeds for secondaries. Sources: [Unicorn horn dust](https://oldschool.runescape.wiki/w/Unicorn_horn_dust), [Chocolate dust](https://oldschool.runescape.wiki/w/Chocolate_dust), and the repository's frozen item registry. Only the existing standard recipe per herb receives a complete row; alternate recipes remain compact by kind.

## Verification

- **129 focused tests** pass across Herblore, Farming, preset sorting, preview building, Main potion grouping, reported cape/Crystal roles and exact classification overrides.
- **1808 full-suite tests** pass, zero failures, errors or skips.
- All **1950 fixed-seed scenario banks** complete. No stalled, blocked or non-terminating scenario appears.
- Coverage includes each of eight missing recipe cells, each withdrawn placeholder cell, complete row prefixes, 20 input shuffles, shared ingredient allocation, all eleven canonical secondaries, exact product matching, multiple physical entries, Main, moved doses, explicit tag/block arrangements and saved physical orders.
- Two older tag-interleaving expectations were updated after inspection: seed runs now precede unfinished/secondary runs, and an explicitly chosen tag sequence takes priority even when the part-dose tag stays with herbs. Exact item permutations remain asserted.
- Compilation retains the existing unchecked-operation note for `IronmanBankArchitectPanel`; no new compiler warning was introduced by these files.
- Final offline `build -x simulateRandomBanks -x aggregateCleanupReview` reuses the freshly generated, reviewed reports and passing full test result. All four fingerprint checks pass. `git diff --check` passes. The local JAR contains 264 entries, SHA256 `15bdfc33eecbdcb480e29297314b6928fe0809f668da5678e771ed93766d2f6b`; archive inspection excludes the Herblore regression tests and research/simulation helpers.

## Reviewed simulation fingerprint

The four previous reports were preserved under `build/tmp/before-herblore-hybrid`. Only four `RANDOM_TABS` rows changed, with no changes to item counts, tab counts, completion status, collapses, creations, distributions, transfers, returns, tab reorders or errors:

| Seed | Moves before | Moves after | Swaps before | Swaps after |
|---|---:|---:|---:|---:|
| 20260744 | 45 | 44 | 17 | 16 |
| 20260746 | 194 | 195 | 89 | 90 |
| 20260754 | 101 | 100 | 42 | 41 |
| 20260756 | 128 | 127 | 57 | 56 |

These are target-order permutation costs from the new Herblore order. The 150 routing banks use **12141 moves / 5266 swaps**, previously 12143 / 5268. The other three reports remain unchanged. An independent read-only audit confirmed the row changes and normalized hashes before the routing fingerprint was updated in `build.gradle`:

- Routing: `28adedb12c406913a836b193ffc92fc20f10471e7c00bd97fe1b27e4d3ffb2ee`
- Cleanup: `309480b04ce63ce74996f4b15768e6c9a84e16611338e44c5abf689148bb3091`
- Aggregate cleanup: `fce40d023f6950a6813d97276f3836c73ab801e7c0c2c13235a4e7423f207ce3`
- Aggregate metadata: `76901b09b75f9d8219f3e1ba8b522940af373160718cd09cbf40439db088c3fe`

## Review size and publication

The highest local main-Java estimate is **197540**, down from 197901 before this change, leaving **2460** estimated tokens. Other estimates are cl100k preserved 195740 / collapsed 197211, o200k preserved 196054 / collapsed 197540. Calibration remains the maintainer's **200414 at def1e856e101ff0e57dd96adccab2cf1d886b076**, reported 2026-09-30. Resources, test changes and documentation were inspected separately. This estimate is not the official Hub count; additional headroom remains a release-preparation task.

No commit, push, issue comment/closure, branch change or publication was performed. The local change is ready for an ingame check of sparse and complete Herblore layouts.

Subsequent outfit variant fixes and the current artifact are documented in [Skilling outfit verification](skilling-outfit-variants-2026-10-13.md).

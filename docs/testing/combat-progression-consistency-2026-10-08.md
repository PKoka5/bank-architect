# Combat progression consistency — 2026-10-08

Pre-release verification snapshot. These changes are included in [0.9.2](../release-0.9.2.md); that record contains the final publication checks.

This records verification of the progression-only step. Subsequent local slot-fallback changes and their current combined build/token results are recorded in [Gear slot coverage](gear-slot-coverage-2026-10-08.md).

Local correction after reviewing a partial Main blueprint export from a fresh installation. The player described using the plugin on the evening of October 6, so 0.9.0 is the likely Hub version; the exporter did not include a version or layout settings. The confirmed score defect also exists in published 0.9.1 (`413355be10760da9d32de9632dbbb4e2f797784f`). No new publication is part of this change.

## Reproduced defect

Exact progression metadata used `stage * 200`, whereas missing variants used an independent name heuristic. Rune platelegs (1079) scored 400 before runtime bonuses; Rune plateskirt (1093) scored 600. Rune med helms and chainbodies similarly received 600 while their curated full-helm/platebody counterparts received 400. Adamant chainbody received 500 while Adamant platebody received 400. Equal equipment stats therefore did not imply equal progression scores, and small stat advantages could not overcome the accidental bias.

Five new regression tests failed against unchanged 0.9.1: the actual variant mismatch, an unlisted Rune fixture, within-stage stat ordering, recent upgrade metadata, and both default preset grids. After correction the expanded nine-test class also covers lower metal variants, wizard colour variants, name-only fallback, appearance variants, and actual published Oathplate/Rune-leg stat vectors.

The export's first rows do follow the default slot rows and style columns. Lower rows contain vertically grouped Initiate, Lunar, and Void sets; cannon parts are together. Filling spare cells with other gear is existing behavior. The supplied text stops partway through Combat item 201 of 256; it cannot establish the entire bank layout or the exact runtime stat vectors. Private bank quantities and the raw export are not added to the repository. Integration banks use public item IDs and synthetic quantities/stats explicitly identified as fixtures.

## Focused changes

- Name fallback now uses the same five progression stages as exact metadata. Specific names retain precedence through the existing maximum-score mechanism; runtime stats remain an additive ranking signal. No pairwise dominance comparator is introduced.
- Exact ID metadata remains authoritative. Equivalent ordinary metal variants share a progression base; real stat differences can still determine order. Mystic boots and Wizard boots retain their distinct stage exceptions.
- Recent melee gear receives the following original curated stages. These are placement decisions, not a claim of universal best-in-slot status.

| IDs | Items | Stage |
|---|---|---|
| 30750, 30753, 30756 | Oathplate helm, chest, legs | 5 |
| 30777, 30779, 30781 | Radiant Oathplate appearance variants | 5 |
| 29801, 29804 | Amulet of rancour, its `(s)` appearance | 5 |
| 28945 | Echo boots | 4 |

- Both Oathplate chest IDs are explicitly functional body armour rather than Unknown. Name-only fallback recognizes their body slot and the new melee names. Runtime stats still take precedence for slot/style.
- The existing set packing, placeholder participation, manual bank guidance, and Alch dominance policy remain in use. New exact tiers can provide replacement evidence for reviewed obsolete armour; a high placement score alone still does not prove an Alch replacement.

## Primary item facts consulted

- [Rune plateskirt](https://oldschool.runescape.wiki/w/Rune_plateskirt): same equipment bonuses as Rune platelegs, differing weight. This supports equal progression bases; med/full helms and chain/plate bodies are **not** asserted to have equal real bonuses.
- [Oathplate armour](https://oldschool.runescape.wiki/w/Oathplate): melee slash accuracy and strength; the radiant appearance preserves functionality. Its comparison with Torva depends on encounter and attack type. Published helm/chest/legs bonuses are used in the regression class.
- [Yama equipment recommendations](https://oldschool.runescape.wiki/w/Yama_strategies) and [highest equipment bonuses](https://oldschool.runescape.wiki/w/Best_magic_defence): Rancour's melee role and progression relative to Fury/Torture.
- [Boots](https://oldschool.runescape.wiki/w/Botos) and [Fortis Colosseum](https://oldschool.runescape.wiki/w/Volatility): Echo boots are upgraded Guardian boots with a recoil role. They are not treated as a universal strength upgrade over Primordial.
- Exact canonical IDs/names were checked against the repository's RuneLite game-value registry. Cached/indexed Wiki content establishes item facts, not current market prices or a complete live catalog. No third-party plugin code or layout is used.

## Validation and baseline review

Final `gradlew.bat build --offline` passes: all 1,436 tests succeed with zero failures/errors/skips, including existing Alch/dominance and placeholder stability regressions. The fixed protocol completes all 1,950 simulated scenarios without a failed outcome, and all four reviewed report hashes match. `git diff --check` passes. No new build warning is emitted by the final run.

The simulation reference changes were inspected before updating their guards:

- The 150-scenario report keeps the same input counts, plan tabs, statuses, and error fields. Total moves change from 12,166 to 12,167. Only three `RANDOM_TABS` runs change by one swap/move: seed 20260754 needs one fewer; seeds 20260756 and 20260766 each need one more. The last delta follows alignment of the lower metal name fallbacks. Bare Black/White/Bronze matches were not retained: a material-colour token could otherwise raise black wizard clothing above its blue counterpart.
- The single cleanup report remains identical: 1,255 item IDs and 3,945 occurrences.
- Aggregate cleanup removes only the two corrected Oathplate chest IDs, three occurrences each. It changes from 9,285 to 9,283 distinct IDs and from 46,497 to 46,491 occurrences. Registry hash, protocol, universe, and all 1,800 successful aggregate outcomes remain unchanged. Cleanup reporting reads classification directly; score changes do not account for those two removals.
- An independent read-only agent review found no unintended role changes or evidence of item loss.

The development-only review estimator uses official calibration **200,414** at `def1e856e101ff0e57dd96adccab2cf1d886b076`. Its four final estimates are 195,560; 196,481; 195,625; and **196,563**. The highest estimate leaves **3,437** tokens below 200,000 and is 30 tokens above published 0.9.1's highest estimate.

The estimate covers main Java only; the nine progression records, two role overrides, name tokens, tests, documentation, and baseline guard changes were inspected separately. Resources remain strict original TSV data with no runtime network or new execution mechanism. The new test class contains only public item identities and synthetic bank entries; documentation is development-only. Official Hub tokenizer/scope remain unknown, so this estimate is not official token-limit approval. Changes remain local and are not part of the submitted 0.9.1 revision.

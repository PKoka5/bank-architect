# Full primary Combat columns

## User contract and implementation

The user clarified that **Melee, Ranged and Magic must each have a full vertical equipment column**, so cape, boots, weapon and accessories can be collected along with the armour. Shared equipment has one real physical location, **with Melee first**, with the other styles directly beside it. The old three-row armour overview did not meet that requirement.

The default grid now uses owned equipment in this order: head, body, legs, cape, boots, necklace, weapon, shield, gloves, ring. Missing slots compact within their own style column. Accessories with real positive offensive bonuses may be candidates for multiple styles; after the Melee choice claims an ID, other styles can choose an unused compatible alternative. Selection scores each accessory for the desired style. This changes bank navigation, not the Alch dominance proof.

Primary columns lock real cells in the **whole equipment grid**, instead of limiting geometry to a small selected front pool. Secondary family remainders are packed atomically into the free cells to the right and below. Full primary columns and family cohesion supersede the earlier requirement for strictly contiguous secondary style sections. Search is bounded to two 10000-candidate passes. The second pass compacts large condition matrices while retaining small armour alternatives; even the final ordered fallback preserves every valid primary lock. Small banks unable to contain the last primary cell use compact whole-style runs. Duplicate physical entries use compact grouping, preserving their stacks and placeholders. Explicit user orders remain authoritative.

This is an equipment placement policy, not a target-specific DPS calculation or a boss loadout validator.

## Exact reported bank

The new export contains the same 85 real Combat IDs as the earlier normalized fixture, with the newly improved weapon ordering. Tests use the existing frozen equipment vectors from RuneLite's public snapshot captured on 2026-10-12. The independent `FullCombatColumnsTest` specifies these exact columns:

| Row | Melee | Ranged | Magic |
| --- | --- | --- | --- |
| 0 | Helm of neitiznot | Slayer helmet (i) | Farseer helm |
| 1 | Bandos chestplate | Black d'hide body | Mystic robe top |
| 2 | Bandos tassets | Black d'hide chaps | Mystic robe bottom |
| 3 | Fire cape | Ava's accumulator | Imbued zamorak cape |
| 4 | Avernic treads | Mixed hide boots | Infinity boots |
| 5 | Amulet of fury | Bow of faerdhinen | Eye of ayak (uncharged) |
| 6 | Osmumten's fang | — | Mage's book |
| 7 | Dragon defender | — | — |
| 8 | Barrows gloves | — | — |
| 9 | Berserker ring (i) | — | — |

Follow-up on 22 October 2026: the [general Ranged head correction](ranged-head-selection-2026-10-22.md)
changes this fixture's Ranged head to Archer helm. The table above records the
original October 13 checkpoint; the tests and regenerated reports use the corrected selection.

The dashes describe the end of a selected column, **not blank bank entries**. Real remaining items occupy those cells. The subsequent [ring/weapon follow-up](ring-weapon-clusters-2026-10-13.md) places Lightbearer with the other rings beside the selected Berserker ring, replacing its initial support cell 3. Every actual item is preserved exactly once. Main retains its existing Lunar staff transport destination, giving 84 Combat entries and 85 total preview entries in this isolated fixture.

Generated and visually inspected output: `build/reports/combat-quality/IRONMAN-full-combat-columns-85.png` and `.html`, plus the Main/endgame and populated condition reports. The renderer outlines selected equipment by identity rather than assuming a contiguous primary front. The midgame column and family assertions are independently specified in the test, not calculated by production helpers.

## Initial verification before the ring/weapon follow-up

Java 11 offline `gradlew build`: successful; **1769 tests, zero failures/errors/skips**. Coverage includes both presets, the exact 85-item bank, every equipment slot, shared accessories, real bank capacity, source shuffles, withdrawals leaving placeholders, manually saved order, condition variants and the 238-item all-Barrows-condition bank. Existing tests of the obsolete armour-only front were replaced with explicit full-column targets; conservation, placeholder, canonical family and vertical armour gates remain strict. The slot-coverage fixture includes a real additional Prayer ring so its tenth-row target is physically possible.

All **1950 fixed-seed scenario banks** complete. Compared with `build/tmp/before-full-columns`, **5/150 routing rows change**: RANDOM_TABS seeds 20260731 (-2 moves), 20260743 (+2), 20260746 (-3), 20260755 (-1), 20260766 (+3). Total manual moves: **12143 → 12142**. Cleanup review and aggregate reports are unchanged. The changed routing hash was reviewed before updating the build gate:

- report.tsv: `04488d649a8e3676d7c660e14f090161193c01690fe0999cd1997466f332ba69`
- cleanup-review.tsv: `309480b04ce63ce74996f4b15768e6c9a84e16611338e44c5abf689148bb3091`
- aggregate/cleanup-review.tsv: `fce40d023f6950a6813d97276f3836c73ab801e7c0c2c13235a4e7423f207ce3`
- aggregate/metadata.tsv: `76901b09b75f9d8219f3e1ba8b522940af373160718cd09cbf40439db088c3fe`

## Review scope

Development-only estimator, calibration 200414 tokens at `def1e856e101ff0e57dd96adccab2cf1d886b076`: cl100k preserved **195960**, collapsed **197335**; o200k preserved **196248**, collapsed **197637**. Highest estimate leaves **2363** tokens. Main Java only is counted; this is not the official Hub count. This change adds no production resources, dependencies, assets, networking, telemetry or input automation. Test expectations, an independent regression test and documentation changed outside the estimator scope.

The local development JAR has 264 entries, 1112376 bytes, SHA256 `e4b5f75abf0a190ce2e7ea5e1a8c081daed60387e3aca9fc4e3e540b1051ca28`. Archive inspection excludes test fixtures, quality reports and development helpers. Whitespace checks pass, with existing LF-to-CRLF notices. The build remains a local development artifact pending ingame validation. No commit, push, branch change or publication was performed. Future banks may require compact geometry when insufficient real cells exist, and custom saved physical orders intentionally retain their chosen layout.

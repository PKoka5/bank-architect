# Secondary weapons and rings beside full Combat columns

## Report and placement policy

The user reported scattered secondary Melee weapons and rings, with Lightbearer unexpectedly at the top of the exported 85-item Combat tab. The full Melee, Ranged and Magic equipment columns remain the primary requirement.

Lightbearer's reviewed role is now `support-ring`. It is not forced into an early support cell or selected as the primary strength ring. The full-column packer collects remaining loose rings into one atomic block after identifying owned armour families. Its preferred origin is beside the last selected ring. An actual equipment family such as Lunar equipment retains its own ring, just as a Barrows weapon stays with its armour family.

Pure secondary weapon blocks are allocated before armour remainders in the first bounded search. This lets loose Melee weapons share the available region to the right of the three primary columns instead of having unrelated armour inserted between them. Unreviewed weapon IDs with known equipment slots join that allocation order. A single quest weapon may fill an earlier free cell without imposing a monotonically increasing first-cell constraint on every later role. Primary gear selection and Alch classification do not change.

The search remains bounded to two passes of 10000 candidate checks. If reserving weapons first exhausts the first pass, the second pass returns to canonical family allocation order while compacting large condition matrices. Small armour alternatives remain available. Shapes are keyed by their block, so reordering cannot separate a block from its shapes. Even the final dense fallback retains valid primary locks. Compact layouts without full-column locks retain their existing grouping behavior; no synthetic blank cells are introduced. Explicit saved physical orders remain authoritative.

## Exact reported bank

The independent `RingAndWeaponClustersTest` uses the frozen normalized `reported-midgame-combat-85.tsv` fixture and the public builder for both presets. Its expectations are independently specified:

- All 23 primary gear targets remain identical to the full-column contract.
- The 13 secondary Melee weapons occupy cells `3,4,5,6,7,11,12,13,14,15,19,20,21`.
- Berserker ring (i) remains at cell 72. Lightbearer and the other five loose rings occupy cells 73 through 78, without prescribing their internal order.
- Main retains Lunar staff in transport: 84 Combat entries and 85 total entries in this isolated fixture. Ironman retains all 85 in Combat.
- Source shuffles, changed quantities and withdrawn primary/ring placeholders keep the exact physical order. Every real entry, quantity and placeholder survives once. Saved manual order overrides both automatic clusters.

Generated Ironman/Main full-column PNG and HTML reports are under `build/reports/combat-quality/`. The Ironman 85-item PNG was visually inspected: full primary columns, the two-and-a-half-row Melee weapon region and the adjacent ring row match the specified targets.

## Verification

Java 11 offline `gradlew build`: successful. **1779 tests, zero failures, errors or skips**, including the ten new cluster tests, existing full equipment-slot tests, sparse/duplicate layouts, complete Lunar families, and the 238-entry bank containing all 144 Barrows condition IDs. Existing family-shape and feasible-vertical-column checks remain strict.

All **1950 fixed-seed scenario banks** complete. Compared with the preserved reports in `build/tmp/before-ring-weapon-clusters`, only **1/150 routing rows** changes: `RANDOM_TABS`, seed `20260731`, 43 items, 88 to 89 moves and 26 to 27 swaps. Both runs complete; all other routing fields remain equal. Total moves change **12142 to 12143**. The three cleanup reports are byte-equivalent after line-ending normalization. This geometry-only difference was inspected before updating the build fingerprint:

- `report.tsv`: `30d344d7a5ab2d06039996130a181fcf2001b5532f5a8331e684ad7fc361073f`
- `cleanup-review.tsv`: `309480b04ce63ce74996f4b15768e6c9a84e16611338e44c5abf689148bb3091`
- `aggregate/cleanup-review.tsv`: `fce40d023f6950a6813d97276f3836c73ab801e7c0c2c13235a4e7423f207ce3`
- `aggregate/metadata.tsv`: `76901b09b75f9d8219f3e1ba8b522940af373160718cd09cbf40439db088c3fe`

## Review scope and artifact

To preserve review headroom, the change removes unused gear helper wrappers and a no-op stats argument, and replaces nine identical tag/block/record call chains with one helper. Private forwarding getters and a repeated tag-count increment are also simplified. Operation order and stat semantics are unchanged; the final full build includes these reductions and passes all four reviewed fingerprints.

The development-only estimator remains calibrated to the maintainer's **200414** tokens at `def1e856e101ff0e57dd96adccab2cf1d886b076`, reported on 2026-09-30. Final estimates: cl100k preserved **196087**, collapsed **197472**; o200k preserved **196397**, collapsed **197799**. The highest estimate leaves **2201** tokens. This estimates main Java only, not the official Hub tokenizer or scope. The changed support-role row, test expectations, the new test and documentation were inspected separately. No production dependency, asset, telemetry, networking, external process or input automation is added.

The local development JAR has **264 entries**, **1113145 bytes**, SHA256 `baf438c5b1b32be1a026d3dad429ddf1fdd87ccc6a30c0c4a7fa732bb65aab9b`. Archive inspection excludes normalized player fixtures, test classes, research/simulation helpers and the retired prototype helpers. Whitespace checks pass; Git emits LF-to-CRLF notices for existing mixed-line-ending files.

The artifact is ready for an ingame test. Complex or sparse banks may still require compact fallback geometry; selected gear and physical item conservation remain protected. No commit, push, branch change or publication was performed.

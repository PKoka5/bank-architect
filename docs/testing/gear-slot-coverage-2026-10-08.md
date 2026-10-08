# Gear slot coverage — 2026-10-08

Pre-release verification snapshot. These changes are included in [0.9.2](../release-0.9.2.md); that record contains the final publication checks.

Follow-up to the [Combat progression correction](combat-progression-consistency-2026-10-08.md). These changes remain local. They have not been committed, pushed, or added to the submitted 0.9.1 revision.

This records the slot-audit step. Subsequent Alch history changes and the latest combined build/token results are recorded in [Alch addition stability](alch-addition-stability-2026-10-08.md).

## What the audit establishes

All eleven bank equipment slots were already supported. The previous issue concerned item recognition and scoring, not missing equipment slot types. `GearSlot.fromRuneLiteSlot` matches the installed standard RuneLite API. The [official API enum](https://github.com/runelite/runelite/blob/master/runelite-api/src/main/java/net/runelite/api/EquipmentInventorySlot.java) includes fourteen entries: the eleven worn-item positions plus ARMS, HAIR, and JAW at indices 6, 8, and 11. Those three positions are explicitly excluded from bank equipment mapping. No player equipment or inventory container is read during this audit or by the feature.

| Slot | RuneLite index | Default Combat placement |
|---|---:|---|
| Head | 0 | Head row |
| Cape | 1 | Cape row |
| Neck | 2 | Neck row |
| Weapon | 3 | Melee/ranged/magic weapon cells |
| Body | 4 | Body row |
| Shield | 5 | Shield row |
| Legs | 7 | Legs row |
| Hands | 9 | Hands row |
| Feet | 10 | Feet row |
| Ring | 12 | Jewellery/remaining gear, permitted as spare row filler |
| Ammo | 13 | Ammunition block after equipment |

Rings and ammo are supported; neither has a dedicated primary setup row. That is existing layout behavior. Two-handed weapons map to the weapon slot and survive planning, but this bank organizer does not validate weapon/shield compatibility as an equipped combat loadout.

## Focused recognition fixes

Slot precedence is now **runtime equipment stats → known catalog slot → name fallback**. A catalogued body/neck/feet/etc. item therefore does not lose its slot merely because its display name is unusual. Real runtime stats still override mistaken catalog metadata and misleading names. Catalog weapon fallback retains the style split; ammo and ring keep their existing ranks.

Original name data now covers missing forms such as sallets, mitres, cuirasses, Barrows robetops/leathertops, cuisses/chausses, bracers/gauntlets, sandals/flippers, scythes, axes and claws. The leading space in the axe token is retained by the TSV loader and prevents treating a pickaxe name as an ordinary axe weapon. These name groups are used by gear sorting only; they do not change item categories or move items into Combat automatically.

Specific prayer names take precedence over generic robe/headwear words, preventing Monk's robes from falling into the magic column when stats are absent. Missing ranged/magic/melee hints for known gear names were also added, including Sunfire fanatic gear, Masori, Crystal armour, Anguish, Ranger boots, Scythe, and Tumeken's shadow.

This audit checks slot support and placement fallback. It does not establish that every item in the generated registry has perfect category, progression, or encounter-specific best-in-slot metadata. Name-only tests explicitly start with items already assigned to Combat. Existing automatic Alch replacement safeguards are unchanged.

## Meaningful regression coverage

`GearSlotCoverageTest` adds six cases:

1. Compare every entry of the actual RuneLite enum against the explicit eleven-slot mapping and the three excluded positions; a new API slot requires review.
2. Feed all eleven slots through both presets and all three Combat layouts, with and without placeholders. Full synthetic stat vectors override misleading item names. Verify style cells, equipment rows, ring/ammo behavior, item IDs, quantities, placeholder state, and no invented bank entries.
3. Verify all eleven declared catalog slots despite misleading names and absent runtime stats.
4. Check uncommon gear names cover wearable, weapon, ring, and ammo ordering without stats, independently of category routing.
5. Keep Monk's robes in the prayer column alongside melee/ranged/magic leg pieces.
6. Verify runtime equipment slots override wrong catalog slots and names.

No private bank export is copied into fixtures. Public equipment names and synthetic bank entries/stat vectors are used.

## Reviewed simulation delta

Comparison is against the preceding local progression-only step, whose report hash was `e0eb6782473df8cf59064f67511e0124d2dd1ef76bf6b159b56028319960bd31`. All 150 single-run scenarios remain `COMPLETED / COMPLETE`; item counts, tab counts, statuses, error fields, and non-sort move counts match.

Only six `RANDOM_TABS` rows change their swap/move count:

| Seed | Moves before → after |
|---:|---|
| 20260731 | 93 → 95 |
| 20260738 | 81 → 82 |
| 20260754 | 100 → 101 |
| 20260756 | 128 → 126 |
| 20260762 | 198 → 200 |
| 20260766 | 131 → 130 |

Total moves change from 12,167 to 12,170. The single cleanup report, aggregate cleanup report, and aggregate metadata remain byte-identical to the previous step. All 1,800 aggregate scenarios complete with zero failure outcomes. Therefore this follow-up changes gear ordering, with no observed tab-routing or cleanup-role changes.

The reviewed single-run hash is now `331d5ea825c55d91756cd473cf0d1471647c820050a689019d8e88249e4671c2`; the other three baseline hashes are retained. A read-only review confirmed slot precedence, arithmetic ranks, name-token scope, and prayer priority.

## Final verification and review budget

`gradlew.bat build --offline` passes with all four reviewed report hashes matching. All **1,442 tests** pass, with zero failures, errors, or skips; all **1,950 simulations** complete. `git diff --check` passes. The final build emits no new warning.

The review estimator retains official calibration **200,414** at `def1e856e101ff0e57dd96adccab2cf1d886b076`. Current estimates are 195,709; 196,618; 195,775; and **196,701**. The highest leaves **3,299** tokens below 200,000. That is 138 tokens above the preceding local progression-only step and 168 above submitted 0.9.1's highest estimate.

The estimator measures main Java only. Name-resource additions, tests, development documentation, and the manually reviewed Gradle hash update were inspected separately. No third-party plugin code/layout, runtime network call, game-state manipulation, telemetry, or inventory/equipment read is added. This is a local estimate, not the official Hub tokenizer/scope or token-limit approval. The current changes remain unpublished.

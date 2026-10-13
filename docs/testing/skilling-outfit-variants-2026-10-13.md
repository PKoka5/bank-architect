# Skilling outfit variants

## Report and fix

The supplied Ironman export contains Prospector helmet **12013**, jacket **12014**, legs **12015**, and boots **29478**. The old outfit definition knew only boots **12016**, so the alternate boots occupied row 1, column 8 instead of row 4, column 3. The existing normalized 161-entry Tools regression now requires **29478 at index 26**, directly below the legs, in both presets.

Added reviewed real-item variants to the outfit tables:

| Variant | Ordered canonical IDs |
|---|---|
| Prospector alternate forms | 29472, 29474, 29476, 29478 |
| Forestry Lumberjack | 28173, 28169, 28171, 28175 |
| Graceful, Agility Arena | 21061, 21064, 21067, 21070, 21073, 21076 |
| Smiths uniform with ice gloves | 27023, 27025, 27027, 27031 |

The first three also have item-set records; all fourteen newly registered ordinary IDs have explicit `TOOL/skilling-outfit` subtypes. This prevents domain mapping alone from placing a Forestry item on the right tab with the wrong generic Tools tag. Smiths gloves (i) already had the correct subtype and substitute for ordinary gloves. Placeholder, equipped, note, uppercase cache-alias and unverified special-game forms were not newly added. The original 34 tool-family rows still pass their historical checksum, with each new row asserted separately before that comparison.

Sources: the repository's frozen item registry; [Prospector cache records](https://oldschool.runescape.wiki/w/RuneScape%3ACache/2024/221.6); [Lumberjack outfit](https://oldschool.runescape.wiki/w/Lumber_jack_set); [cape variants](https://oldschool.runescape.wiki/w/Capes); [Smiths' Uniform](https://oldschool.runescape.wiki/w/Smithing_armor). The historical `tools.graceful-brimhaven` key remains intact for saved layouts; it contains Colossal Wyrm IDs, while the added Agility Arena family has its own key.

## Placement behavior

Complete variants retain separate columns. Remaining compatible pieces are pooled by the reviewed clothing-slot position, one piece per slot per column. Compatibility is explicit for base/golden/alternate Prospector, Lumberjack/Forestry, Angler/Spirit Angler, Graceful colours, Farmer forms, Eye colours and Smiths gloves variants. Two Farmer forms become two four-piece columns rather than one eight-piece column. An extra pair of boots cannot turn an existing four-piece outfit into a five-piece outfit.

Eye boots and owned lantern forms attach to one best matching Eye column, including mixed-colour pieces. No physical item is duplicated. The dense fallback follows the same owned outfit groups.

Automatic placement reserves every member at a bounded, unoccupied vertical origin. More than eight outfits can continue in later rows. If a column cannot fit, placement tries an unoccupied consecutive block. It does not invent empty cells; if neither shape fits around previous reservations, the canonical dense remainder remains the fallback. Priority Runecrafting/container rows start after the last reserved outfit endpoint. Existing supplied locks are preserved and saved physical blueprint order still applies last.

The Ironman Main tab has a separate Graceful policy. It still prefers the most complete colour family, with the base family breaking a tie; missing clothing slots can now be filled from other registered Graceful colours already on that tab. Wear order remains hood, top, legs, gloves, boots, cape. Complete selected colours remain unchanged, and one slot never receives two hoods. Unexpected names retain the old fallback instead of borrowing unidentified slots. Retagged pieces remain excluded from the native Main geometry.

## Verification

- **80 focused cases** pass for outfit geometry, real variant classifications, catalogue coverage, reported Tools/Cannon placement and Main quick access.
- Final Java 11 offline **build succeeds: 1840 tests**, zero failures, errors or skips, and **1950 completed fixed-seed scenario banks**.
- All four reports match the preserved `build/tmp/before-outfit-variants` reports and pass their existing fingerprint gates. No baseline update was needed.
- Real-catalog tests cover twelve complete Graceful colours simultaneously, mixed Prospector/Forestry/Angler/Farmer/Eye/Smiths pieces, duplicate slot alternatives, shuffles, withdrawn placeholders, tiny tabs, a 26-entry mixed vertical/compact layout and saved physical orders. A real default-Iron bank with sixty currencies/runes/teleports verifies six mixed Graceful pieces in Main column 7, including cape/boots placeholders and exact source conservation.
- The earlier focused failures exposed unanchored outfits beyond the first eight and a generic Forestry tag in the sparse Main case; the fixes retain the strict geometry expectations. No failure remains. The previous unrelated unchecked-operation compiler note is unchanged; the final build adds no new warning.
- `git diff --check` passes. JAR inspection excludes the regression tests, reported fixtures, research and simulation helpers.

Current local JAR: **264 entries**, **1115874 bytes**, SHA256 `909b69769f91ca7e8a713f99a713c813ab1058e9974f72d8e3bd4f49c9586412`.

## Review budget and release state

Final main-Java estimates: cl100k preserved **196635**, collapsed **198001**; o200k preserved **196984**, collapsed **198364**. Highest estimate leaves **1636** tokens, which is insufficient for the standing goal of several thousand tokens of release headroom. Additional simplification remains a release-preparation task. Calibration is still the maintainer's exact **200414 at def1e856e101ff0e57dd96adccab2cf1d886b076**, reported 2026-09-30. Resource additions, test changes and documentation were reviewed separately; the estimator does not count these and is not RuneLite's official review count.

No dependency, runtime network call, telemetry, automation, inventory/equipment read or game-state mutation was added. No commit, push, issue comment/closure, branch change or publication was performed. The local outfit fix is ready for an ingame check.

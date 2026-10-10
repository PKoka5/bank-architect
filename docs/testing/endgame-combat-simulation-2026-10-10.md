# Endgame Combat simulation — 2026-10-10

## Scope

Historical pre-change results. The implemented follow-up and current validation
are in [Endgame Combat roles](endgame-combat-roles-2026-10-10.md).

Development-only follow-up to the ammunition grouping patch. Production code,
catalogs and settings are unchanged by this test extension. The simulator calls
the current production preview builder with fresh Main and Ironman defaults.
It reads cached public equipment stats and high-alch values; it never starts a
game client or performs bank actions.

The representative collector bank contains 244 distinct IDs: 120 selected
from 30 ordered gear families, plus 124 extra IDs covering boss sets, raid and
specialist weapons, jewellery, capes, boots, gloves, cannon parts, lower
progression gear/tools and ammunition. Families include Torva, Oathplate,
Masori (f), Ancestral, Virtus, Inquisitor, Crystal, all six Barrows sets and all
three Moon sets. Additional gear includes Bandos, Justiciar and elite Void.

This is not every boss item or every degradation, charge and colour state.
Family selection takes one active/base item per equipment slot. Three variants
use the same 244 IDs: owned, 21 selected gear items withdrawn as placeholders,
and reversed input order. Each variant runs both presets.

## Results

All six new previews pass; all 20 previews in the expanded runner pass.

| Check | Result |
| --- | --- |
| IDs, quantities and placeholder state | Preserved without duplicates or invented blanks |
| Independent armour positions | Oathplate, Masori (f), Ancestral and Sunfire occupy the expected four columns in rows 1–3 |
| Secondary equipment families | Ordered vertical columns or filled column-major rectangles |
| Cannon base, stand, barrels and furnace | One adjacent rectangular block |
| Placeholder/reversed input stability | Identical Combat ID order within each preset |
| Finished ammunition | 22 stacks in Drops; none in Combat |
| Drops group order | 5 arrows, 9 bolts, 4 darts, 4 cannonball stacks, then 23 Alch items |
| Contiguity | Each ammo family, all ammo together and Alch occupy one run |

Each Main variant has 193 Combat items and each Ironman variant has 192.
Both presets have 45 Drops items, including the 23 Alch items. Rune thrownaxe
(805) is a retained Combat control, separate from the four finished dart stacks.

The twelve armour positions are explicit expected IDs and physical slots.
Other primary-target checks reuse the production selection plan and verify its
placement; they cannot independently prove that the selected equipment is best.
The armour expectations express this plugin's placement policy, not universal
best-in-slot or target-dependent damage calculations.

## Findings needing follow-up

### Weapon priority

Both presets select Armadyl godsword, Bow of faerdhinen (c) and Kodai wand for
the frontline weapon positions while Scythe of vitur, Twisted bow and Tumeken's
shadow are also present. This is stable across all three fixture variants.

The current selection combines catalog tier with weighted equipment stats.
It does not model attack speed or special/passive effects sufficiently to
establish endgame weapon priority. Passing layout assertions therefore does
not make this an adequate endgame weapon selection policy. A follow-up should
define explicit all-round priority while retaining useful specialist weapons,
with independent expected weapon IDs in regression tests.

### Preview build time

Measured around the production preview builder using `System.nanoTime()`:

| Fixture | Main | Ironman |
| --- | ---: | ---: |
| Endgame owned | 5,265 ms | 5,356 ms |
| Endgame placeholders | 5,317 ms | 5,131 ms |
| Endgame reversed | 5,145 ms | 5,562 ms |
| Busy 65-item bank | 53 ms | 48 ms |
| Latest 825-item export | 139 ms | 221 ms |

These are individual local cached-data timings, excluding Gradle startup,
assertions and report output. They are not a controlled benchmark or a live
client frame-time measurement. Dense Combat/set coverage merits profiling;
overall bank size alone does not explain this difference. The full offline
runner completed successfully in 38 seconds.

Read-only code review identifies the many-set search as a likely hotspot.
`BoundedLayoutPacker` retains up to 128 states and evaluates up to 1,000,000
candidate origins. Vertical groups have equal local scores, so feasible
placements can require repeated canonical block-vector tie comparisons,
including placements subsequently discarded by the priority queue. This is
a code-supported hypothesis; no profiler or search counters were captured in
this run. Existing large-packer coverage uses 336 items but only four groups,
so it does not cover this many-set stress case.

The smallest follow-up is to replay only the endgame gear request through the
development-accessible `planDetailed` path, record its existing `SearchStats`
and elapsed time, and capture a short CPU profile. Search limits should not be
reduced without checking that vertical set placement still survives.

## Reproduction and data identity

Use `tools/research/combat-layout-simulation/README.md` and its optional latest
export argument. Output from this run is the ignored local file
`build/reports/combat-layout-visual/endgame-simulation.json`; the console log is
`build/tmp/endgame-timing.log`.

- RuneLite stats, cached 2026-10-09:
  `https://static.runelite.net/item/stats.ids.min.json`
- Stats SHA-256:
  `99cdcb7514b2cf48655094d94e502d95d4ac708e328b25579d21511c4db3f1b8`
- Wiki high-alch mapping:
  `https://prices.runescape.wiki/api/v1/osrs/mapping`
- Mapping SHA-256:
  `9852ffc77384a30802ef3769bf769de6b288fc9043fad20de1f035573f887cf8`

The prior production patch's 1,583 JUnit cases and offline build are documented
separately in `ammo-darts-bolts-grouping-2026-10-10.md`; they were not rerun for
this development-only extension. The latest main-Java review-size estimate
remains 196,938 (3,062 estimated tokens below the limit). This extension is
outside main Java and the plugin JAR; the local estimate is not an official
Plugin Hub count. Changes remain local and unpublished.

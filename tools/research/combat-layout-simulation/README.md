# Combat layout visualization simulator

Development-only code, outside all Gradle source sets and the plugin JAR. It
calls the current `BankOrganizationPreviewBuilder` with fresh Main and Ironman
defaults, the historical owner-supplied exports, an optional latest export and
seven named scenarios. It never starts
a RuneLite client, reads inventory/equipment, or performs game actions.

## Inputs

Cache these public data files locally before running offline:

- RuneLite equipment stats: `https://static.runelite.net/item/stats.ids.min.json`
- Wiki high-alch mapping: `https://prices.runescape.wiki/api/v1/osrs/mapping`

Download data only in development tools, never in the plugin. The exporter
itself performs no network requests. Wiki mapping absence yields zero high-alch
value; absent equipment records use the plugin's normal metadata fallback.
It does not reconstruct ItemManager's worn/charged variant canonicalization.
Equipment fields use the same conversion as the current production adapter,
including magic damage in tenths and attack speed.

Blueprint exports must retain complete `row=... | id=... | name=... |
quantity=... | placeholder=...` lines and `TAB ... | key=... | name=... |
items=...` headers. Historical tags/categories are not used as current truth.
Wrapped metadata continuations are joined to their original row or tab header.
The last incomplete row in a truncated export is skipped and the result is
explicitly flagged partial. No missing bank items are invented.

## Run

Set Java 11 and substitute the paths to the two owner-supplied exports:

```powershell
.\gradlew.bat --offline --console=plain `
  --init-script tools/research/combat-layout-simulation/run.gradle `
  simulateCombatLayoutVisual `
  '-PvisualOutput=build/reports/combat-layout-visual/simulation.json' `
  '-PvisualStats=build/tmp/combat-layout-visual/runelite-item-stats.json' `
  '-PvisualMainExport=<Main export path>' `
  '-PvisualIronmanExport=<Ironman export path>' `
  '-PvisualAlchMapping=build/tmp/combat-layout-visual/wiki-item-mapping.json' `
  '-PvisualLatestExport=<optional latest export path>'
```

The optional init script compiles only this developer source, placing classes
under `build/tmp/combat-layout-visual/classes`. Standard builds and review-size
estimates of main Java are unaffected.

## Checks and output

Every scenario runs both presets: partial Main export, complete Ironman export,
sparse Oathplate/Rune, the same Oathplate as placeholders, partial degraded
Barrows sets, a busy combat/cannon bank, and three endgame variants. Each preview must preserve every
input ID, quantity and placeholder state, without duplicates or invented blanks.
The optional latest export adds two preview runs, for twenty in total. Physical
best-gear targets are checked against the full equipment capacity, excluding
ammunition. Secondary-family geometry uses the original physical indices while
excluding primary IDs; it does not compress the remaining items into a fake
tail. Each remaining family must form an ordered vertical column or filled
column-major rectangle. A single-row bank necessarily permits a horizontal
rectangle. Cannon grouping and both sparse Oathplate/Alch results are checked
explicitly.

The representative endgame fixture contains 251 distinct IDs, selecting one
active/base item per slot from 30 catalog families and adding boss gear,
specialist weapons, lower progression gear and finished ammunition. It is not
an exhaustive list of every boss drop or charged/degraded/colour variant. The
same fixture runs owned, with 26 selected items as placeholders, and with
reversed input order. Combat order must remain identical within each preset.
Twelve independently specified armour IDs must occupy the first three rows of
the melee, ranged, magic and prayer columns. These fixed expectations supplement
the primary-target checks that reuse the production sorter. Scythe, Twisted bow
and Shadow have independent expected frontline positions as well. Secondary-family
geometry, cannon grouping, ammunition routing and contiguous Drops groups are
also asserted for these fixtures.

JSON arrays retain the preview's actual physical order. `front=true` denotes
the logical selected setup, which can occupy noncontiguous physical cells.
`primaryTargets` maps those IDs with a reachable target to zero-based physical
positions; `primaryRows` counts rows containing these targets. The legacy
`alignedRows` field remains available, but a contiguous setup-prefix or
`secondaryStart` no longer describes the layout. `metadataFallbackCount` includes non-wearable items
without equipment stats, such as cannon parts. Resource hashes identify the
data actually used.

Each result also includes the complete `drops` array in its actual physical
order, rather than only the `alch` subset. `dropsGroups` reports counts,
zero-based `physicalIndices`, and the number of contiguous `runs` for all
finished ammunition together, arrows, bolts, darts, cannonballs and Alch.
A present group has `runs=1` when every member is adjacent; `runs>1` exposes
scattered items. Empty groups have zero runs. These metrics are descriptive for
the export scenarios. Endgame scenarios assert one run for each present group
and the arrows, bolts, darts, cannonballs family order. They do not independently
assert every within-family tier ordering. Original export
Drops rows and their ammunition metrics are available as `originalDrops` and
`originalDropsAmmoGroups`; their historical layout tags remain unused.

`previewBuildMs` times only the production preview build, excluding subsequent
assertions, JSON output and Gradle startup. These are individual local timings,
not a controlled benchmark or an in-game frame-time measurement. Endgame
`selectedWeapons` records the weapons chosen by the reviewed frontline priority
and placement score. Structural checks do not establish target-dependent DPS.
See `docs/testing/endgame-combat-roles-2026-10-10.md` for the current policy,
independent regression checks and timings. The earlier
`docs/testing/endgame-combat-simulation-2026-10-10.md` preserves the pre-change
weapon selection and performance findings.

The local conversation raster uses cached official item sprites and this JSON.
Raw bank data, images and the conversation fragment stay in ignored `tmp/`
and `build/` directories. Only this simulator and the reviewed findings belong
in source control. See `docs/testing/combat-layout-visual-simulation-2026-10-09.md`.

# Bank Architect 0.9.6

Prepared on **13 October 2026** above published 0.9.5 source
`8808c84cbf9433f9c76f6b045df2fcd18e9da0b0`.
[Hub update 18309](https://github.com/runelite/plugin-hub/pull/18309) was merged
on 13 October; this release therefore uses a new submission.

## Player-facing changes

- **Bank blueprint preview:** Open Blueprint temporarily draws the planned
  items on the real bank area, preserving native tabs, controls and scrolling.
  Main shows all planned sections. Tab switches retain preview; draft edits,
  Undo and Cancel update it. Other bank clicks dismiss preview and consume the
  gesture; preview never moves, withdraws or deposits items.
- **Complete Combat columns:** best owned Melee, Ranged and Magic gear forms
  vertical equipment columns, including capes, boots, weapons and accessories.
  Shared items have one physical position. Remaining armour sets stay vertical;
  functional weapon groups and rings stay together, including Lightbearer.
- **Main supplies and teleports:** complete 4-3-2-1 potion families lead the
  incomplete families. Reviewed real food/utility fillers can finish an odd
  half-row; without sufficient fillers the tab stays dense. Teleport tablets
  group by their reviewed sprite colours.
- **Ironman Herblore:** complete eight-item recipes lead the tab, followed by
  compact incomplete ingredients by kind. Only complete recipes reserve their
  secondary; placeholders count as present. The stored configuration key remains
  compatible, with the label **Complete Herblore recipe rows**.
- **Skilling outfits:** reviewed Prospector recolours, Eye colours, Angler and
  Spirit Angler, Forestry Lumberjack, Farmer, Graceful and Smiths variants retain
  outfit grouping, including compatible mixed variants. Complete colour sets
  lead; missing pieces never create fake items or blank cells.
- **Classification:** cannon components sit with Slayer tools; Construction
  and Crafting capes are teleports, and Deadman Bowfa/Crystal variants have their
  reviewed Combat roles. Existing manual assignments and saved item orders win.

The first sidebar opening after upgrading shows **What's new: 0.9.6**. Dismissal
is remembered locally for this release. The Updates icon retains all twelve
bundled release entries. Every real bank move remains manual.

## Validation

- Offline Java 11 `build`: **1,839 tests**, zero failures, errors or skips.
- **1,950 fixed-seed scenario banks** complete; all four reviewed simulation
  fingerprints match. Score/catalog budget cleanup did not change those outputs.
- Real normalized midgame fixtures and mixed/endgame synthetic banks cover full
  primary columns, secondary sets, weapon/ring grouping, condition variants,
  shuffled inputs, placeholders and exact item conservation in both presets.
- Preview event handling, native tab switches, scrolling, saved/custom layouts,
  stale-state rejection and outfit geometry have regression coverage. The owner
  checked the preview and preceding layouts in game; no live endgame bank check
  or claim about every possible gear combination is made.
- Independent read-only reviews of the final UI/model and score/catalog cleanup
  found no blockers. Existing unchecked-operation compiler note in the sidebar
  remains; no new dependency or compiler warning was introduced.
- Release notice rendered and visually checked at 204 pixels. `git diff --check`
  passes. JAR inspection excludes tests, normalized player fixtures, retired
  prototype helpers, simulators and research code.

Detailed records: [Combat columns](testing/full-combat-columns-2026-10-13.md),
[mixed progression](testing/mixed-progression-combat-2026-10-13.md),
[weapon groups and item fixes](testing/weapon-groups-and-item-fixes-2026-10-13.md),
[Herblore](testing/ironman-herblore-hybrid-2026-10-13.md),
[outfit variants](testing/skilling-outfit-variants-2026-10-13.md),
[bank preview](testing/bank-preview-2026-10-11.md).

## Review-token budget

Calibration remains the official **200,414** count reported on 30 September 2026
for `def1e856e101ff0e57dd96adccab2cf1d886b076`.

| Tokenizer / normalization | Local estimate |
| --- | ---: |
| cl100k, preserved whitespace | 193,766 |
| cl100k, collapsed whitespace | 195,571 |
| o200k, preserved whitespace | 194,084 |
| o200k, collapsed whitespace | **195,902** |

The highest leaves **4,098 estimated tokens** below 200,000. These estimates
cover main Java only; Hub preprocessing, tokenizer and full input scope remain
unknown. Changed resources, tests, docs and build settings were inspected
separately. A passing build does not certify the official Hub count.

Repeated score accessors became one named-component API without changing the
18-component comparison order or guarded movement evidence. Catalog lookups
share existing set definitions; test-only UI defaults and retired prototype
helpers moved outside production. Necessary test coverage is retained.

## Artifact and publication

- JAR: `build/libs/ironman-bank-architect-0.9.6.jar`.
- Size: **1,112,841 bytes**, **264 entries**.
- SHA-256: `a623534c0b07d2550969e5fd5e94e7978a3a8fba746e814b90b3a9854f3bb3e2`.

Publication uses `v0.9.6`. The GitHub release and Hub request record the exact
source revision; the Hub submission changes only `plugins/bank-architect`.
RuneLite distribution follows Hub review and merging.

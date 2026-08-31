# Sorting consistency — design

Branch `sorting-consistency` (off `main`, absorbs the `sequential-layout` framework).
Goal: the blueprint's item order should match what a player expects from the
sorters, consistently across categories, with taste left to per-category options.

## Symptoms and their causes

| Symptom (from real exports) | Root cause | Fix class |
|---|---|---|
| Teleport tablets split around glories/games/passage/dueling | Charged jewellery sorts by its internal `jewellery.*` family key; the letter j alphabetizes between "Falador" and "Lumberyard" | Sorter bug fix (F2) |
| Hammer + saw seated before bronze pickaxe | Beam packer places multi-item family blocks before singles, ignoring the sorter's order | Packer redesign (F3) |
| Builder's boots on the clue tab, rest of the set on quests | Registry data: boots catalogued `CLUE/cosmetic`, set catalogued `CLEANUP/quest-item` | Data + lint (F4) |
| Stamina(4) far from Stamina(3/2/1) | Deliberate grab-area design: full potions lead, part doses trail | New option (F5) |

## Features

### F1 — Absorb the per-category framework (from `sequential-layout`)

Merge the closed PR #17 work as the foundation: `TabOrder`/`GearOrder` enums,
per-mode map in `BankLayoutOptions`, per-category config dropdowns in sections,
`semanticLayout(fallback, request, sequential)`, gear/farming/herblore sequential
paths, `IronmanMainItemSorter.sortSequential`.

Renames (labels only; config keys and enum constants unchanged):
- `TabOrder.PACKED` → label **"Packed"**, `TabOrder.SEQUENTIAL` → label **"Sorted"**.
- `GearOrder.PACKED` → label **"Style columns"**; other gear labels unchanged.

### F2 — Family collation fix (no option)

Within a rank, single items run by name and charged families follow **after**
them as a group — positioning a family by its `jewellery.*` key wedged it into
the tablets (j falls between Falador and Lumberyard), and positioning it by
display name does the same (so does g). Families order among themselves by base
display name ("Amulet of glory(6)" → "amulet of glory"), with the family key
breaking ties so families sharing a base name (standard vs imbued rings of
wealth) stay separate runs. Charges descend within a family, as before. One
sort for both modes; the sequential-only workaround from PR #17's branch is
deleted.

Effect: tablets and the Ectophial run A→Z, then glory / games necklace /
passage / dueling as whole families — in both Packed and Sorted.

### F3 — Order-preserving packing ("Packed" redefined)

The real bank always compacts, so density is not a degree of freedom; the packer
only chooses a sequence. Today it reorders freely (families early, singles as
filler anywhere) to guarantee no family straddles a row edge — the source of
every "why is X before Y" complaint.

New behaviour of Packed for every category that uses the beam packer: **place
groups in the sorter's order; when a family would straddle a row edge, make the
smallest local nudge (pull forward one or two nearby singles to finish the row)
so the family starts on the next row; if no nearby single exists, let it
straddle.** The reorder-freely policy is retired, with no toggle preserving it.

Implementation shape: candidate generation and validation stay; the beam search
is replaced (or constrained) by a linear pass over the sorter's sequence with a
bounded look-ahead window for nudge candidates. Deterministic, same
inputs → same layout. Sorted mode continues to bypass the packer entirely.

### F4 — Set-consistency lint + registry fixes

A test (or audit task following `exportEffectiveItemClassifications`'s pattern)
that walks item families/sets and fails when pieces of one set classify to
different categories. Seed fixes it must confirm: Builder's outfit (boots
10865 → quest-item with the rest), Plague jacket/trousers reviewed for the same
treatment. Whitelist mechanism for genuinely intentional splits.

### F5 — Potion dose grouping (new option, default unchanged)

Supplies category gains one dropdown: **"Potion doses"** —
- **"Grab area"** (default, today's behaviour): full potions lead, part doses trail.
- **"By family"**: each potion family runs 4→1 together, using the existing
  `ItemSortMetadata` DOSE variant data.

### F6 — Rune order (new option, default unchanged)

Runes category gains one dropdown: **"Rune order"** —
- **"Alphabetical"** (default, today's behaviour under the main-tab sorter).
- **"Elemental"**: the canonical sequence (air, water, earth, fire, mind, body,
  cosmic, chaos, nature, law, death, blood, soul, astral, wrath), revived from
  the legacy teleport sorter's rune ranking.

## Config surface after this branch

A section per category, but only where a category has a real decision:

- **Guidance** — existing overlay/hint items.
- **Tab layout dropdowns exist only where Packed and Sorted genuinely
  differ** — where a category has curated geometry to keep or give up: the
  runes/teleports/currency tabs (four-wide rune block, diary grid), the
  resources tab (raw above processed), and the clues & cosmetics tab (outfit
  columns). Everywhere else the two modes are a nudge apart, so no option is
  offered and packed stands. Gear's own four-way order is its version of this
  choice.
- **Combat gear** — order (**Style columns / By slot / By style / By style,
  weapon first**), *Fill part-empty gear rows*, *Gather outclassed gear for
  alching* (moved here from the abstract Classification section).
- **Food & potions** — **Potion doses: Grab area / By family** (F5).
- **Herblore** — *Fill part-empty Herblore rows*.
- **Runes, teleports & currency** — **Tab layout** and **Rune order:
  Alphabetical / Elemental** (F6).
- **Resources** / **Clues & cosmetics** — **Tab layout** each.

Nothing else — F2/F3/F4 are corrections, not options. Categories without a
section have no decisions to make.

## Testing

- Collation: teleport tab (tablets + 4 jewellery families) is name-ordered in
  both modes; regression for the old `jewellery.` wedge.
- Packing: pickaxe precedes hammer in Packed tools; a 4-family meeting a row
  edge starts the next row when a nudge candidate exists, straddles when not;
  order inversions beyond the nudge window are asserted absent.
- Lint: fails on a deliberately split set fixture; passes on the fixed registry.
- Doses: family mode runs 4→1 contiguously; grab-area mode byte-identical to today.
- Runes: elemental mode follows the canonical sequence with unknown runes after;
  alphabetical mode byte-identical to today.
- Full suite green except the pre-existing `registryFingerprint` failure on main
  (expected to change legitimately when F4's registry fixes land — update the
  recorded fingerprint if that test's protocol allows, else document).

## Out of scope

Gear style attribution (staves classify melee via crush+strength; neutral gear
defaults melee) — known, separate branch. Per-tab (rather than per-category)
order choices. Auto-guide (separate branch/PR #18).

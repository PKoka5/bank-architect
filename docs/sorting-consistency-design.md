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
groups in the sorter's order; when a family would straddle a row edge, the
family defers behind the whole run of singles that follows it, to the latest
point it still fits a row cleanly; singles are never plucked out of their flow,
and when no clean point is in reach the family wraps in place.** The
reorder-freely policy is retired, with no toggle preserving it.

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

Two concepts everywhere: **Grid / List**.

- **Grid** keeps a tab's curated shape - gear sets down columns, ore above its
  bar, empty containers above filled, outfit columns, the rune block and diary
  grid - and always fills its rows to hold the shape (a grid that does not
  fill is not a grid; the old per-tab fill toggles are retired with their
  degraded middle states).
- **List** is pure reading order, wrapping at the row edge like text, junk-free
  by construction. Gear's list reads each curated set as one run in slot
  order, strongest set first, loose gear and weapons flowing after.

Sections: Guidance; Combat gear (Layout, alch pile); Food & potions (Potion
doses, "Keep dose sets on one row" checkbox - the one list-side exception,
letting a dose family slide behind the items after it rather than wrap);
Runes, teleports & currency (Layout, Rune order, Teleport order); Tools,
Resources, Clues & cosmetics (Layout each). The Herblore section is gone: its
recipe-rows-versus-kind choice is already stated by where the player puts the
Part Doses tag, and its fill toggle retired with the rest.

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

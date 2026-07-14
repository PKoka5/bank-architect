# Resource skill-zone consensus

Date: 2026-07-14

## Scope and independence boundary

This note aggregates recurring organization principles across the ten locally normalized,
manually imported community templates. It does not reproduce a template, its exact placements,
labels, or layout. The product rule derived here is original: use consensus evidence to create
deterministic skill zones, then let the bounded semantic layout engine choose concrete geometry
from the player's owned items.

The cohort and source-handling constraints are documented in
`local-import-cohort-patterns.md`. Exact imported layouts remain local and git-ignored.

## Measurement method

For each template, the tab with the largest reviewed resource-family population was selected.
Items were assigned to broad analytical families with narrow item-name and canonical-ID facts:

- Mining and Smithing: ores, coal, and metal bars;
- Woodcutting: logs, planks, and bark;
- Fletching: shafts, feathers, headless arrows, tips, limbs, strings, and unfinished bows;
- Crafting: gems, hides/leather, glass materials, and textiles;
- Construction: nails, clay, limestone, and high-level building materials.

Fishing/Cooking and Prayer were measured when present, but they were not consistently colocated
with the five core resource families. Row purity is the largest analytical family share among
recognized resource items on a physical eight-column row.

This measurement is evidence about organization shape, not production classification. Production
membership must be exact-ID backed where ambiguity can affect another category.

## Cohort results

- all 10 templates colocate Mining/Smithing, Woodcutting, Fletching, and Crafting in their primary
  resource area;
- 9 of 10 also colocate Construction materials there;
- the mean recognized-resource row purity is 80.3%;
- coherent full or near-full skill-family rows occur repeatedly, rather than one alphabetical
  resource stream;
- Fishing/Cooking appears in the same area in only 3 templates;
- Prayer appears in the same area in only 2 templates.

Pairwise median-position evidence among templates containing both families:

| Earlier family | Later family | Support |
|---|---|---:|
| Mining/Smithing | Fletching | 10/10 |
| Mining/Smithing | Crafting | 9/10 |
| Mining/Smithing | Construction | 9/9 |
| Woodcutting | Fletching | 9/10 |
| Crafting | Fletching | 9/10 |
| Mining/Smithing | Woodcutting | 8/10 |
| Woodcutting | Crafting | 6/10 |
| Woodcutting | Construction | 6/9 |
| Construction | Crafting | 5/9 |
| Construction | Fletching | 5/9 |

The evidence therefore defines a partial order, not one universal total order. Mining/Smithing is
the strongest early anchor and Fletching is the strongest late anchor. Woodcutting and Crafting are
stable zones but their relative order may be selected by packing quality. Construction is a bridge
zone whose best neighbor may be Mining/Smithing or Woodcutting.

## Production model

The Resources tab should use this hierarchy:

`primary skill zone -> workflow family -> canonical tier/stage -> geometry`

Initial primary zones:

1. `mining-smithing`
2. `woodcutting`
3. `crafting`
4. `construction`
5. `fletching`
6. `fishing-cooking`
7. `prayer`
8. `other-resource`

The numbered list is a deterministic fallback order. It is not permission to override stronger
semantic placement or to copy any observed template order. The packer should encode supported
precedence and neighborhood relations separately from fallback ordering.

### Single-primary-zone rule

Every bank entry has exactly one primary resource zone. Cross-skill use does not duplicate an item:

- logs belong to `woodcutting`, even though Fletching and Construction consume them;
- planks belong to `woodcutting` as the processed wood row;
- metal bars belong to `mining-smithing`, even when Crafting consumes them;
- gems and glass materials belong to `crafting`;
- arrow and bow components belong to `fletching`;
- nails and dedicated building materials belong to `construction`.

Later activity tags may express secondary uses, but those tags must never create a second placement.

## Geometry rules

- a zone is contiguous when the available dense bank footprint permits it;
- a workflow family stays intact before zone-level compactness is optimized;
- owned items only: missing tiers never create phantom entries;
- widths remain bounded to 1 through 8 and are selected from semantic evidence plus real footprint;
- the engine may use horizontal runs, vertical runs, stage matrices, or explicit row groups;
- no invented blank items or separators;
- real compatible spillover items may occupy unused cells inside a nominal block;
- fallback items remain a dense permutation of the player's actual bank entries.

## First implementation slice

Introduce a package-private resource-zone classification layer shared by the fallback sorter and
resource semantic rule construction. The first slice should cover only the five core zones with
existing reviewed facts:

- Mining/Smithing: current metal facts;
- Woodcutting: current canonical log and plank facts;
- Crafting: current gem facts plus existing narrow glass/hide/textile classification;
- Construction: current narrow nail/clay/limestone facts;
- Fletching: current exact `ammo-component` IDs and narrow bow-component facts.

The slice must preserve every existing item, category, quantity, placeholder state, and source slot.
It should change only ordering and semantic grouping inside a Resources sort-mode category.

## Required regression evidence

- a mixed Resources input becomes contiguous skill zones rather than an alphabetic stream;
- Mining/Smithing precedes Fletching;
- Woodcutting and Crafting each remain internally contiguous;
- logs and planks remain separate ordered rows inside Woodcutting;
- incomplete families use exact fallback without phantom members;
- every output is a dense permutation with no blank item;
- all presets using `RESOURCES` receive the same proven behavior;
- unrelated categories and the generic Skiller-specific tabs remain unchanged.

# Local Import Family Candidate Analysis

Analysis date: 2026-07-14 (Europe/Paris)

This is the stricter second analysis pass over the same ten manually imported popular Ironman
templates. No templates were re-imported and no network source was accessed. Exact item IDs and
positions remain in the git-ignored local cache; this document contains only cross-template
aggregates and independently designed research conclusions.

## Reproducibility and boundary

- Cohort: 10 templates, 95 tabs, and 9,484 positive placements.
- Cohort, catalog, thresholds, and analyzer-schema fingerprint:
  `3bb0a668fc4931ec85bf0015fc9d352c5eed62d6c1b9f0379107c544d00c86ef`.
- Existing exact-ID sort metadata covers 1,094 placements (11.5%); 8,390 placements remain outside
  a detailed family.
- A recurring adjacency requires at least three distinct templates. It is retained as a discovery
  edge only when the items are adjacent in at least 60% of the templates where both share a tab.

The discovery graph found 496 recurring exact-ID pairs before the confidence filter and retained 325
edges, forming 169 connected candidate components. Those components prioritize catalog work only:
transitive adjacency can connect several genuine workflows and is not semantic proof.

Semantic evidence uses only curated families with an ordered `DOSE`, `SERVINGS`, or
`WORKFLOW_STAGE` variant kind. Each family casts at most one observation per template. A duplicate ID
or a family split across tabs is marked ambiguous rather than assigned a shape. Multi-family blocks
are formed only when cohesive family atoms of the same explicit class touch directly; broad-category
items and foreign spillover cannot connect them.

## Missing metadata concentration

| Current broad category | Placements without family metadata | Unique item IDs | IDs in a recurring adjacency |
|---|---:|---:|---:|
| Unknown | 3,435 | 1,149 | 152 |
| Gear | 2,263 | 801 | 76 |
| Skilling | 1,069 | 272 | 62 |
| Rune | 490 | 86 | 28 |
| Potion | 281 | 155 | 8 |
| Farming | 269 | 105 | 6 |
| Teleport | 197 | 67 | 8 |
| Cleanup | 180 | 49 | 7 |
| Herblore | 120 | 32 | 6 |
| Currency | 86 | 18 | 0 |

Broad category and display name are candidate generators, not sufficient production facts. The local
registry contains context-specific IDs with identical names, charged states spread across several
categories, and intact, degraded, broken, tutorial, minigame, and placeholder variants.

## Curated family-block evidence

Only widths with support from at least three distinct templates appear below. A class-specific
preferred width additionally requires five templates, three families, 60% of eligible-template
support, and a lead of at least two templates over the runner-up. `None` means the evidence remains
insufficient. A preferred width ranks candidates for that class; it never prevents the planner from
evaluating widths 1 through 8.

| Curated family class | Eligible templates | Block observations | Family support | Supported width evidence | Preferred width |
|---|---:|---:|---:|---|---:|
| Gem raw/processed stages | 7 | 7 | 5 | width 5 in 5 templates | 5 |
| Herb workflow stages | 7 | 11 | 14 | width 3 in 6 templates | 3 |
| Metal raw/processed stages | 6 | 9 | 6 | width 3 in 3 templates | None |
| Potion dose families | 6 | 9 | 18 | width 4 in 4 templates | None |

The analyzer also retains the complete aggregate support vectors needed for exact integer regret
calculation. Vector positions are widths 1 through 8; `template` counts distinct supporting
templates and `family` counts distinct supporting families at that width. Zero therefore means a
measured zero, not a value hidden by the three-template reporting threshold.

| Curated family class | Template-support vector (widths 1..8) | Family-support vector (widths 1..8) |
|---|---|---|
| Gem raw/processed stages | `0, 2, 0, 0, 5, 0, 0, 0` | `0, 5, 0, 0, 5, 0, 0, 0` |
| Herb workflow stages | `0, 1, 6, 1, 0, 0, 0, 0` | `0, 10, 14, 4, 0, 0, 0, 0` |
| Metal raw/processed stages | `0, 2, 3, 2, 1, 0, 0, 0` | `0, 2, 6, 5, 5, 0, 0, 0` |
| Potion dose families | `0, 0, 0, 4, 0, 0, 0, 2` | `0, 0, 0, 16, 0, 0, 0, 15` |

The family-level observations explain these shapes:

- herb workflow members are most often cohesive left-to-right in ascending workflow-stage order;
- uncut/cut gems and ore/bar pairs more often use source-above-processed columns;
- potion variants overwhelmingly use horizontal descending-dose runs, but the present cohort does
  not clear the stricter threshold for a universal four-wide potion block;
- incomplete families create ragged blocks without implying invented empty target cells.

These results supersede broad-category bounding boxes when making semantic claims. The earlier
widths 1-8 table remains useful evidence that the generic packer must support every width, but it
cannot choose a family-specific default by itself.

## Highest-value next metadata slices

An aggregate audit of uncovered exact IDs identifies these review candidates. Counts overlap where
one item participates in more than one possible workflow, so they are not additive.

| Candidate slice | Uncovered placements | Cohort signal | Production caution |
|---|---:|---|---|
| Standard rune membership | 199 | 22 canonical rune IDs across all 10 templates | Membership can be curated; names do not prove one order or width. |
| Charged teleport families | 149 | 13 families and 62 observed charge-state IDs | Curate every charge and eternal/uncharged state explicitly. |
| Raw/cooked food pairs | 104 additional; 190 total family placements | 56 direct pair adjacencies, predominantly vertical | Exclude name lookalikes and curate exact cooking relations. |
| Canonical log tier run | 95 | Present in all 10 templates | Species and tier ordering require domain metadata. |
| Missing potion-dose families | 79 | Six families, with horizontal dose runs dominant | Fits the existing exact-ID `DOSE` model after source review. |
| Recurrent equipment body/legs | 60 | Four exact pairs with vertical support in 3-4 templates each | Use typed equipment slots and lifecycle states, not names. |

The safest implementation sequence is: missing canonical dose families, exact raw/cooked pairs,
charged teleport families, standard-rune membership plus a dedicated block schema, then typed
equipment relations. Rune membership is deliberately separate from rune order and block width so the
plugin can learn useful cohesion without reproducing any community layout.

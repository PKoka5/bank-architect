# Local Import Cohort Patterns

Analysis date: 2026-07-13 (Europe/Paris)

This report describes aggregate patterns measured across ten manually imported, popular Ironman
community templates. It is research input for original Bank Architect rules, not a source of presets.
Exact layouts remain local and git-ignored. No third-party code, UI, naming, resources, or layout is
shipped or reproduced here.

## Measurement quality

- Cohort: 10 templates, 95 tabs, and 9,484 positive item placements.
- The plugin's item registry recognizes all 9,484 placements.
- Existing categories are incomplete: 3,705 placements (39.1%) are currently `UNKNOWN`.
- Detailed sort-family metadata covers 1,094 placements (11.5%) after adding canonical herb stages,
  six ore/bar families, and five uncut/cut gem families.
- A relationship is called recurring below only when it occurs in at least three distinct templates.

The cohort is intentionally small and popularity-selected. Results are useful as evidence for
defaults, but not as proof that every Ironman prefers the same organization.

## Strong aggregate signals

### Variants usually run horizontally

Within the currently covered sort families, same-family members are horizontally adjacent 388
times and vertically adjacent 72 times. Potion-dose families are especially clear:

| Direction between adjacent doses | Horizontal | Vertical |
|---|---:|---:|
| Descending dose count | 191 | 1 |
| Ascending dose count | 23 | 2 |

An original rule can therefore place dose variants in a horizontal run, descending by default. This
should remain configurable because a meaningful minority uses the opposite direction.

### Processing chains form compact pairs

Repeated relationships support compact, readable processing chains:

- grimy herb immediately followed by its clean herb: support from 6–7 templates depending on herb;
- ore above its corresponding bar: support from 5 templates for adamantite, gold, mithril, runite,
  and silver;
- uncut gem above its cut gem: support from 4 templates for sapphire, emerald, ruby, diamond, and
  dragonstone;
- raw food above its cooked form: support from 4 templates for shark and manta ray;
- adjacent material progressions such as iron → steel → mithril → adamantite → runite bars, ores,
  nails, planks, and gems: typically support from 5–7 templates.

These observations suggest a general original primitive rather than copied layouts: a `family run`
for ordered variants and a `source → processed` pair that may be oriented horizontally or vertically
per section.

### Equipment body pieces commonly run vertically

Several independently created layouts place a body directly above its matching leg piece. Recurring
exact-ID examples include Blood Moon, Eclipse Moon, and Proselyte pieces, each with support from 4
templates. Related helm → body observations exist, but alternate item IDs currently fragment their
exact-ID support. This is evidence for investigating an original equipment-set column ordered head →
torso → legs; it is not yet strong enough to make that ordering unconditional. Weapons, off-hands,
gloves, boots, and jewellery need separate typed rules.

### Families also form multi-row blocks

Pair adjacency alone does not capture every recurring structure. A four-neighbour connected-component
measurement finds 16 rune block candidates with at least eight items across all 10 templates:

- width 4 is the most common shape (6 of 16 blocks);
- 14 of 16 blocks have dense items in at least 75% of their occupied rows;
- 12 of 16 keep the same start column in at least 75% of their rows;
- 11 of 16 end in a shorter final row;
- median bounding-box density is 82.1%.

This supports a semantic multi-row block primitive with scored candidate widths, ordered row roles,
and a ragged final row. The detector measures the bounding box of each four-neighbour component with
at least eight items from the same broad known category. It excludes the 39.1% currently classified
as `UNKNOWN`; a candidate is therefore shape evidence, not proof of one semantic family.

Across 148 candidates in the seven broad categories represented in at least three templates, every
width from 1 through 8 occurs with support from at least three distinct templates:

| Width | Candidate blocks | Distinct templates |
|---:|---:|---:|
| 1 | 3 | 3 |
| 2 | 6 | 5 |
| 3 | 19 | 9 |
| 4 | 31 | 9 |
| 5 | 18 | 9 |
| 6 | 15 | 8 |
| 7 | 8 | 5 |
| 8 | 48 | 10 |

Consequently, width 4 is evidence for one common rune shape, not a universal default. The layout
engine should evaluate allowed widths and permit compatible blocks to share a physical row. This
does not establish one literal rune order: the detector currently knows only
the broad `RUNE` category, not why each row was chosen. Gear, skilling, potion, Herblore, farming,
and teleport categories also produce connected block candidates, but their broad classifications
must be split into item-ID-backed families before becoming production defaults.

## What the evidence rejects

Tab position is not a stable semantic key. For example, tab index 1 is predominantly gear in six
templates, skilling in two, runes in one, and teleports in one. Similar disagreement exists at every
tab index. The plugin should therefore assign sections by item meaning and player configuration, not
learn that a fixed source tab number always represents a fixed category.

The evidence also does not justify copying whole tabs, fixed coordinates, template names, or a
specific player's category order. Sentinels are preserved during normalization but remain
semantically unexplained, so no production rule should depend on them yet.

## Recommended original rule model

Use the aggregate findings as confidence-weighted constraints:

1. Classify an item into an original Bank Architect section.
2. Group known family members into a horizontal ordered run.
3. Apply typed relationships such as source → processed or head → torso → legs.
4. Compose related rows into semantic blocks and score every evidence-compatible width from 1–8.
5. Pack blocks into an eight-column preview without splitting meaningful rows when avoidable.
6. Treat category order, direction overrides, locks, and unknown items as player-owned decisions.
7. Record the rule and confidence that produced every proposed position.

Family metadata coverage has moved from 5.4% to 11.5% through the first ID-backed herb and resource
slices,
but still needs substantial expansion. Keep an explicit fallback for the 39.1% of placements whose
current category is unknown. The research supports the shape of the algorithm; it does not yet
support automatic placement of every bank item.

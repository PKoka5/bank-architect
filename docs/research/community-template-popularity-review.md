# Community Template Popularity Review

Review date: 2026-07-13 (Europe/Paris)

This report uses public total-import counts as a popularity signal. The website's rolling 30-day
`Popular` value is not exposed in the allowed server-rendered HTML and is therefore not used.
Import counts change over time; the values below are a dated snapshot, not constants for production.

## Most imported overall

| Rank | Template | Imports | Items | Tabs | Initial interpretation |
|---:|---|---:|---:|---:|---|
| 1 | Ingus Bank | 1,769 | 1,047 | 10 | Account type not declared in title |
| 2 | DC Bank | 1,000 | 916 | 10 | Account type not declared in title |
| 3 | Iron Man Bank | 609 | 1,138 | 9 | Explicit Ironman |
| 4 | Max Main | 516 | 1,075 | 10 | Explicit main |
| 5 | Mid-Late Iron Core / Jun | 292 | 1,059 | 10 | Explicit mid/late Ironman |
| 6 | Semi-organized Main | 240 | 695 | 9 | Explicit main |
| 7 | Med-High Level Bank Template | 200 | 967 | 9 | Account type not declared in title |
| 8 | Ironman - Late Game | 173 | 861 | 10 | Explicit late-game Ironman |
| 9 | Midgame Ironman | 159 | 803 | 10 | Explicit mid-game Ironman |
| 10 | Midgame/Early End Iron | 136 | 1,029 | 9 | Explicit mid/late Ironman |

Popularity is only one quality input. It must be capped or logarithmic and must not make a popular
layout authoritative by itself.

## Most imported explicit Ironman templates

| Rank | Template ID | Template | Imports | Items | Tabs |
|---:|---:|---|---:|---:|---:|
| 1 | 70 | Iron Man Bank | 609 | 1,138 | 9 |
| 2 | 53 | Mid-Late Iron Core / Jun | 292 | 1,059 | 10 |
| 3 | 32 | Ironman - Late Game | 173 | 861 | 10 |
| 4 | 127 | Midgame Ironman | 159 | 803 | 10 |
| 5 | 82 | Midgame/Early End Iron | 136 | 1,029 | 9 |
| 6 | 104 | Optimal Layout IronMan | 93 | 945 | 10 |
| 7 | 169 | Optimal Midgame Iron Base | 69 | 764 | 10 |
| 8 | 39 | Endgame Ironman | 62 | 1,286 | 10 |
| 9 | 61 | Ironman - Endgame | 59 | 935 | 9 |
| 10 | 209 | Ironman Bank - Early Midgame | 43 | 623 | 8 |

## Public preview observations

The ten public Open Graph previews were inspected locally without accessing excluded endpoints.
They show only a small selected grid, not the complete template.

- IDs 70, 53, 104, and 61 show combat-gear-heavy grids and are useful candidates for studying set,
  style, equipment-slot, and tier patterns.
- IDs 32 and 39 show food/fishing/cooking-heavy grids and offer a contrasting workflow layout.
- ID 82 shows a rune, teleport, and charged-jewellery-heavy grid.
- ID 209 shows an early/mid-game utility-oriented grid with sparse reserved space.
- ID 127 contains mixed combat/rune signals and may be useful for testing whether a layout keeps
  styles coherent or interleaves them.
- ID 169 renders almost no useful preview content despite declaring 764 items and 10 tabs. This is a
  concrete example of why preview images cannot be treated as complete layout evidence.

The images do not reliably identify which tab is shown and may omit most items. Image-derived evidence
must therefore remain `IMAGE_INFERRED`, below exact local-import evidence.

## Recommended first exact-import cohort

If templates are imported manually through the public plugin workflow, start with this ten-template
cohort:

1. ID 70 — strongest popularity signal and a large general Ironman bank;
2. ID 53 — popular mid/late Ironman comparison;
3. ID 127 — popular mid-game baseline;
4. ID 209 — smaller early/mid-game bank;
5. ID 32 — late-game food, farming, and Herblore evidence;
6. ID 104 — late/endgame gear and rune evidence;
7. ID 39 — very large endgame bank and food/resource evidence;
8. ID 61 — alternate endgame gear layout;
9. ID 82 — runes, teleports, jewellery, food, and supplies;
10. ID 169 — lower-popularity mid-game comparison and preview-quality outlier.

This cohort is deliberately stratified rather than simply taking the ten highest counts. It covers
progression, bank size, several workflows, and an outlier. Exact imported records should remain local
and git-ignored; versioned output should contain only aggregate findings that cannot reconstruct an
individual layout.

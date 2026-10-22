# Wiki strategy context corpus — 22 October 2026

Development research above published 0.9.6 (`d0c4348921d2ca71883f2dd9a5bcadaa4c1c015d`).
This is a structured review queue, not a runtime import of Wiki rankings.

## Coverage

| Measure | Captured |
| --- | ---: |
| Recommended-equipment Bucket rows / distinct contexts | 483 |
| Pages with those contexts | 157 |
| Distinct page + method labels | 418 |
| Ranked equipment cells | 15,109 |
| Cell-link references, deduplicated within a cell | 20,231 |
| Exact resolved item IDs | 886 |
| Resolved IDs present in the current RuneLite registry | 878 |
| Resolved IDs absent from that registry | 8 |
| Source pages with revisions (table pages + category-only pages) | 174 |
| Table contexts with a separately captured source page | 483 / 483 |

The direct Strategies category contains 113 namespace-zero pages. Seventeen have
no equipment Bucket rows; sixty-one Bucket pages are outside that direct category.
The 174-page union is captured locally. This does not claim that all strategy prose,
subcategories or linked subpages have been interpreted. The older July snapshot
had 453 rows; its aggregate output is retained separately and is not this corpus.

## Identity and conditions

The offline exporter preserves page, method, repeated-method occurrence, slot,
source rank, raw cell, footnotes and unclassified cell references. It also records
exact version facts, ambiguous candidate IDs, source hashes and review flags.
Image and text references to the same target within one cell are deduplicated.
Links in conditions or footnotes are not automatically equipment alternatives.

Reference statuses: 18,385 resolved-default; 1,623 unknown-page; 137 ambiguous;
76 unknown-fragment; 57 no-link; 10 missing-item-id. The final two categories have
no resolved identity. All 20,288 reference records, including the 57 empty-link
records, require manual review. Group links such as blessed armour and blessings,
redirects, variants and non-item skill links remain explicit review gaps.

Eight exact Wiki IDs have no current registry row: 34027 Hallowfell; 34401 Necklace
of Fangs; 34407/34413/34419/34425 elemental amulets of air/water/earth/fire;
34428 Elemental amulet; 34715 Obligator (charged). These are research findings,
not new production mappings. A matching name is insufficient to infer an ID or
charged/degraded variant. Unknown classifications retain the existing automatic
Alch exclusion.

Source-page revisions are joined by title to the independently downloaded Bucket
rows. They identify the captured source evidence, **not** an atomic Wiki snapshot
or proof that a particular Bucket row came from exactly that revision. Human
review must connect the conditions and item effects to their sources.

## Reviewed decisions

The [context review](wiki-strategy-context-review-2026-10-22.md) and its compact
13-row facts ledger cover twelve set/specialist contexts plus Scurrius crafting.
Set bonuses, task restrictions and specialist functions cannot be replaced by a
global equipment score. Exact-stat and live-pipeline checks rejected three
plausible but incorrect bug examples involving Void, Obsidian and Guthan.

The first production change from this context pass protects Rune mace 1432 when
a real Scurrius' spine 28798 is present and no Bone mace 28792 (even a placeholder)
is recorded. Earlier automatic session Alch retention cannot bypass that veto.
Explicit player choices remain authoritative. No new positive disposal candidate,
GE-price rule, global strategy-rank replacement or runtime network call is added.
The earlier seven-item d'hide full-stat correction remains in place.

Subsequent paired replacement proposals must specify exact variants, applicable
activity and method, lost effects, bank-only evidence, placeholders and player
overrides. Absence from a strategy table never proves that an item is disposable.
New source items enter this development review queue; updates are reviewed and
released normally rather than silently downloaded into the plugin.

## Reproduction and provenance

Run the [documented exporter](../../tools/research/wiki-recommended-equipment/README.md)
against the local snapshots in `build/tmp/wiki-strategy-snapshot-2026-10-22/`,
passing `--pages source-pages.json` and writing to its `contexts/` child directory.
The complete downloaded tables and page text remain ignored development files.
No third-party plugin code, layouts or resources are used.

| Local file | SHA-256 |
| --- | --- |
| recommended-equipment.bucket.json | `44c24a6853edd084082b179cd79d93c0458b9edb4b195545e812815f28814bdb` |
| infobox-item.bucket.json | `0537a5f1aa0a1fa7794e84379de9938026976e379cf442b57af3f20dea9be6a9` |
| source-pages.json | `95a63b5490dfa23abf67b6ab919355cdc3a00834a615d1682da72a1da5275b15` |
| contexts/strategy-contexts.json | `70f48d7113118a7f122758aa9c269cc1f6166c6efde14ecf7a1e6071e450357b` |

The exporter has 12 local Node tests covering identity resolution, method separation,
conditions, source provenance, deterministic output, validation and absence of
network access. Production regression results are recorded in the corresponding
[development verification](../testing/wiki-strategy-contexts-2026-10-22.md).

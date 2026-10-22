# OSRS Wiki recommended-equipment audit

This developer-only workflow compares the OSRS Wiki's public recommended-equipment Bucket data
with Bank Architect's generated item registry. It never runs inside the RuneLite plugin.

The audit is a review signal, not a Gear whitelist. Wiki tables can describe combat, skilling,
transport, and utility loadouts, and absence from the tables is not evidence that an item is not
combat equipment.

Run:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File `
  tools\research\wiki-recommended-equipment\audit-recommended-equipment.ps1
```

Use `-Refresh` to replace the local Bucket snapshots. Raw Wiki rows, resolved occurrences, and the
machine-readable aggregate are written below `cache/`, which is git-ignored. Only independently
reviewed aggregate conclusions belong in `docs/research/` or production classification rules.

The script uses the public MediaWiki `action=bucket` endpoint with a descriptive user agent. It
paginates at the documented 5,000-row maximum and does not scrape item pages.

## Contextual Alch review

The 22 October 2026 [armour audit](../../../docs/research/wiki-strategy-alch-armour-2026-10-22.md)
and [weapon/tool audit](../../../docs/research/wiki-strategy-alch-weapons-2026-10-22.md)
cover all 100 existing reviewed Alch candidates. Cached table presence or aggregate
best rank cannot prove replacement. Verify the activity, method, slot, alternatives,
set conditions and specialist effects against source pages before adding a rule.
The first pilot only restricts tier-only proof for seven ordinary d'hide pieces;
the source revisions and independent regression vectors are recorded in the audit.

## Export page, method and slot contexts

`export-strategy-contexts.cjs` reads local snapshots without network access or dependencies:

```powershell
node --test tools/research/wiki-recommended-equipment/export-strategy-contexts.test.cjs
node tools/research/wiki-recommended-equipment/export-strategy-contexts.cjs
```

By default it reads the audit's two `cache/*.bucket.json` files and the repository
item registry. `--recommended`, `--infobox`, `--registry` and `--output-dir` override
those paths. The default output directory is ignored `cache/contexts/`.

Optional `--pages source-pages.json` accepts a local array of MediaWiki page objects
containing `title` and `revisions[0]` (`revid`, `timestamp`, `slots.main.content`).
Source capture uses the public API with `action=query`, `prop=revisions`,
`rvprop=ids|timestamp|content`, `rvslots=main`, `formatversion=2` and explicit titles
in batches of 25. Store those downloaded snapshots below `cache/` or `build/tmp/`.
For the October review, titles were the union of Bucket page names and direct
namespace-zero members of `Category:Strategies`; category-only pages still need
manual review when they have no Bucket equipment table.

Outputs are deterministic JSON/TSV context inventories and cell references, with
input hashes, raw cells, footnotes, method metadata, version-resolution status and
optional source-page revision evidence. A cell link can name a condition or source,
so every reference remains unclassified. Explicit unresolved fragments never fall
back to the page's default item, and ambiguous versions remain unresolved. Candidate
IDs on unresolved references are review hints, not resolved identities.

Page revisions identify the separately captured source pages, joined by title.
They do **not** prove that Bucket rows were generated from those exact revisions.
Reviewers must reconcile the table conditions with the captured page. No rank,
presence, absence or aggregate score generates a runtime Alch/replacement rule.

The [October corpus report](../../../docs/research/wiki-strategy-corpus-2026-10-22.md)
records coverage and gaps. The original [reviewed-contexts.tsv](reviewed-contexts.tsv)
contains compact reviewed facts, with [source explanations](../../../docs/research/wiki-strategy-context-review-2026-10-22.md).
Its `related_example_ids` are alternatives or optional context examples, never an
implicit AND dependency. These developer files are excluded from the plugin JAR.

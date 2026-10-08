# Offline item change tracking

`track-item-changes.ps1` compares supplied item data before a plugin release. It does
not download anything, call game APIs, change runtime data, or approve item roles.
Snapshots and reports default to ignored `build/` files.

## Establish an explicit baseline

Use the exact registry source revision and collection date. Source facts need their
own identifier, revision or immutable snapshot identifier, and collection date.
Do not label the historical Wiki cache as freshly collected.

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tools/research/item-classification-audit/track-item-changes.ps1 `
  -RegistrySource 'bank-architect item registry' -RegistryRevision '<exact Git revision>' -RegistryDate '2026-10-06' `
  -SourcePath 'tools/research/item-classification-audit/cache/effective-with-source-facts.tsv' `
  -SourceIdentifier 'OSRS Wiki exact-ID cache' -SourceRevision '<immutable snapshot ID or SHA-256>' -SourceDate '2026-07-15' `
  -SourceCompleteness Partial -SnapshotPath 'build/item-baseline.json' -WriteSnapshot
```

`-WriteSnapshot` is required for every snapshot write, including replacement of an
existing baseline. Writing records observations; it does **not** mark review rows
resolved. Review and keep the baseline outside disposable build output when it is
needed for the next game update. Keep the exact source snapshots with it.

## Compare after refreshing development sources

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tools/research/item-classification-audit/track-item-changes.ps1 `
  -RegistryPath '<new registry TSV>' -RegistrySource 'bank-architect item registry' `
  -RegistryRevision '<new exact Git revision>' -RegistryDate '<collection date yyyy-MM-dd>' `
  -SourcePath '<new source facts TSV>' -SourceIdentifier 'OSRS Wiki exact-ID cache' `
  -SourceRevision '<new immutable snapshot ID or SHA-256>' -SourceDate '<collection date yyyy-MM-dd>' `
  -SourceCompleteness Partial -PreviousSnapshot 'build/item-baseline.json'
```

Comparison writes `build/item-changes-review.tsv` and
`build/item-changes-report.md`; it leaves the previous snapshot untouched. Once
the changes have been independently reviewed, explicitly write the next snapshot.
Source facts are optional; omitting them produces missing-source warnings and
retains previous facts rather than inventing a function removal.

### Compare a newer RuneLite identity index

The bundled production registry does not automatically acquire new IDs. Generate
a development index from an **explicit local** RuneLite API source jar, using the
existing original research script:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tools/generate-item-id-research-index.ps1 `
  -SourceJar '<exact local runelite-api-version-sources.jar>' -OutputDir 'build/new-item-index'
```

Then use `-ResearchIndexPath` instead of `-RegistryPath`:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tools/research/item-classification-audit/track-item-changes.ps1 `
  -ResearchIndexPath 'build/new-item-index/item-id-research-index.tsv' `
  -RegistrySource 'RuneLite gameval ItemID.java' -RegistryRevision '<exact RuneLite version plus source-jar SHA-256>' `
  -RegistryDate '<collection date yyyy-MM-dd>' -PreviousSnapshot 'build/item-baseline.json'
```

The two input parameters are mutually exclusive. The research format has nine
columns. Positive `TOP_LEVEL` IDs are review candidates; `CERT` and `PLACEHOLDER`
IDs retain change rows as explicit cache context. Nonpositive IDs have an explicit
excluded count. A missing direct Javadoc name falls back to the constant-derived
label and is recorded as `INFERRED_CONSTANT_LABEL`, never a verified game name.
Only top-level heuristic categories are kept as inferred roles; excluded
namespaces use `CACHE_CONTEXT`. Supply refreshed independent facts too when
available. Comparing a new index with old registry heuristics may expose inferred
classification differences; these require review and are not proof of a game
function change.

Use the same input format and namespace convention for successive game-update
baselines. Migrating the bundled four-column registry to the research index can
produce many `NAMESPACE_CHANGED` and inferred classification rows solely because
the formats describe records differently. Record that migration explicitly;
those rows are not evidence that the game changed thousands of item functions.

## What is compared

- Registry input: four nonempty TSV columns, positive item ID, name, inferred
  classification, constant name. Duplicate or malformed IDs fail before writes.
- Change types: `NEW_ID`, `REMOVED_ID`, `NAME_CHANGED`,
  `REGISTRY_CLASSIFICATION_CHANGED`, `CONSTANT_CHANGED`,
  `NAMESPACE_CHANGED`, `SOURCE_FACTS_CHANGED`, and `SOURCE_FACTS_UNAVAILABLE`.
- Source input: a TSV with `ItemId`. The existing classifier export columns
  (`Name`, `ItemCategory`, `Subcategory`, tab/workflow/tags/variant fields) are
  excluded from independent facts. The final-placement export's preset, scenario,
  catalog/effective categories, mapped keys, layout tags, final tabs, contexts,
  usage tags, status and error fields are also excluded. A derived-only classifier
  export fails validation instead of being accepted as a source of truth. Other supplied columns are compared, including
  Wiki name, examine, equipment stats, actions or function fields when available.
- If `WikiStatus` is supplied, only `VERIFIED_WIKI_ID` records may replace facts.
  Without that column, the data is labelled supplied observations; exact ID alone
  does not verify the source's gameplay claims.
- Same-ID changes list their changed fields. New nonempty fact fields also require
  review. Blank cells, absent rows, removed source columns, and unverified matches
  preserve previous values and their exact provenance. They remain visibly
  unavailable in the review list.
- Explicit `INTERFACE`, `PLACEHOLDER`, `DUMMY`, `NULL` constant tokens and `null`
  names retain diff rows as `EXPLICIT_CACHE_RECORD` / `CACHE_CONTEXT_ONLY`.
  The research index's `CERT` and `PLACEHOLDER` namespaces have the same treatment.
  Other records are **not** asserted to be bankable or obtainable.

Snapshot schema 1 stores numerically sorted records, sorted fact fields, source
coverage, file hashes, declared completeness, freshness warnings and per-fact
provenance, including each fact source's SHA-256. Given identical inputs and
`-AsOfDate`, snapshot bytes are deterministic.
The completeness declaration describes the supplied source, not independent proof
that all bankable items were included. No stored expectations are derived from the
classifier's current output.

## Future game updates and limitations

Refresh both item identities and gameplay facts using development tools before a
release. Merely upgrading RuneLite does not refresh a bundled registry or this
source cache. Review new IDs **and** changed facts on existing IDs, then add original
exact-ID rules and independent expected tests for the affected families. Preserve
manual player assignments and check both presets and contextual Alch decisions.

Source and registry dates after `-AsOfDate` fail validation. Both stale sources
and stale registries default to a warning after 30 days; `-AsOfDate` and
`-MaxSourceAgeDays` make these checks reproducible. Retained previous facts also
produce `RETAINED_FACTS_STALE` when their original provenance is too old, even
if the current source file is fresh or absent. Missing, partial and stale data
are never evidence that an item is useless or safe to alch. An absent registry ID
is a removal candidate only; confirm the registry itself is complete.

This is a deterministic diff of **supplied fields**. It cannot detect a changed
game function when the source data has not changed, or facts absent from those
fields. A zero-change report with stale or missing sources does not certify a
release. There is no network fallback inside the plugin.

Run the self-contained development tests with:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tools/research/item-classification-audit/test-track-item-changes.ps1
```

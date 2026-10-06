# Community Template Public-Page Research

This developer-only workflow measures the publicly accessible Bank Templates community gallery.
It is not used by the RuneLite plugin at runtime.

## Originality and access boundary

- Treat community templates as statistical observations, never as presets to copy.
- Do not copy third-party code, UI, resources, naming, layout files, configuration, or architecture.
- Do not publish or ship individual player layouts.
- Keep the raw and per-template normalized cache local and excluded from git.
- Check in only reviewed aggregate findings and independently designed Bank Architect rules.
- Respect the source `robots.txt`. At the time of the source audit, public pages and the sitemap are
  allowed while `/api/` and `/tools/partials/` are excluded. The discovery script deliberately does
  not access either excluded path.

## Run

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File `
  tools\research\community-templates\discover-public-pages.ps1
```

The script:

1. refreshes and validates `robots.txt`;
2. refreshes the sitemap and discovers the current template count at runtime;
3. fetches each allowed public detail page at no more than one request per second;
4. reuses cached detail pages on later runs;
5. writes local coverage, failures, and normalized public summaries under `cache/`.

Use `-RefreshPages` only when the detail pages genuinely need refreshing. The script is restartable:
successfully cached pages are not downloaded again by default.

## Known source limitation

The allowed server-rendered detail page exposes template metadata plus a variable, incomplete set of
truncated, ordered tab summaries. It does not expose complete item IDs, every tab, or exact slot positions. The
interactive page may obtain more data through excluded paths, but this workflow does not bypass the
published access boundary. Consequently, its output can prove gallery coverage but cannot prove
complete positional-layout coverage.

## Normalize manually imported templates

Templates imported through the plugin's normal public workflow are stored locally as separate JSON
files. Normalize only explicitly selected public repository IDs:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File `
  tools\research\community-templates\normalize-local-imports.ps1 `
  -RepoIds "70,127,209"
```

The normalizer deliberately skips `owned-banks.json` and every unrequested template. It writes exact
tab, slot, row, column, item-ID, sentinel, and source-hash records under the git-ignored research
cache. A non-positive layout value is retained as an unexplained `sentinel`; its meaning must not be
fabricated without independent evidence.

## Analyze the normalized cohort

After normalization, produce a reproducible aggregate analysis:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File `
  tools\research\community-templates\analyze-local-cohort.ps1
```

The analyzer reads only the git-ignored normalized imports and the plugin's own item catalogs. It
writes aggregate category coverage, recurring adjacent pairs, variant direction, connected
category-block shapes, and tab-index consistency back into the ignored cache. Block output contains
only cross-template counts and shape summaries, never coordinates from an individual layout. It does
not emit or check in complete player layouts. A block signal is the bounding box of a connected broad
category component with at least eight known-category items; it is a research candidate, not proof of
one semantic family or production-ready ordering.

For account-specific research, pass an explicit reviewed cohort to **both** analyzers:

```powershell
.\tools\research\community-templates\analyze-local-cohort.ps1 `
  -RepoIds "8,14,26,55" `
  -OutputPath "tools/research/community-templates/cache/main-2026-10-05/aggregate-analysis.json"
.\tools\research\community-templates\analyze-family-candidates.ps1 `
  -RepoIds "8,14,26,55" `
  -OutputPath "tools/research/community-templates/cache/main-2026-10-05/family-analysis.json"
```

This initial Main cohort is four selected July imports, predominantly late/endgame.
It is not a representative sample of all Main banks. IDs must be positive integers;
missing selected inputs fail rather than silently reducing the sample. Only selected
normalized files are read. Without `-RepoIds`, both tools retain the historical
all-input behavior, which must not be labelled a Main-only analysis.

## Analyze exact-ID family candidates

Run the stricter second pass on the same normalized cache:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File `
  tools\research\community-templates\analyze-family-candidates.ps1
```

This pass has two deliberately separate outputs in one git-ignored JSON file:

- an exploratory exact-ID adjacency graph used only to prioritize missing metadata; an edge needs
  support from at least three templates and must be adjacent in at least 60% of the templates where
  both items share a tab;
- semantic measurements for existing ID-backed `DOSE`, `SERVINGS`, and `WORKFLOW_STAGE` families.

The semantic pass casts one vote per family per template, rejects duplicate IDs and families split
across tabs as ambiguous, and builds multi-family blocks only from directly touching cohesive family
atoms with the same namespace, variant kind, and stage signature. Broad category members and foreign
items cannot bridge those atoms. Its checked-in report contains only aggregates; exact candidate
items and every per-template placement remain in the ignored local cache.

Family report schema 4 adds `CoPresentTemplateSupport` (both items anywhere in a
template), `CoTabShareOfCoPresent` and `AdjacentShareOfCoPresent`. The existing
`AdjacencyConfidence` remains conditional on both items sharing a tab. Always
report these denominators together: strong adjacency within a tab does not establish
that players usually group those items in the same tab. Candidate thresholds remain
exploratory and do not approve a production rule. Check source/layout duplicates and
independent author support separately before interpreting a majority.

## Compare original Main role probes

`main-role-probes.json` defines limited exact-ID research comparisons from our
own catalog, not exhaustive role lists or preset destinations:

```powershell
.\tools\research\community-templates\measure-role-probes.ps1 `
  -RepoIds "8,14,26,55" `
  -OutputPath "tools/research/community-templates/cache/main-2026-10-05/role-probe-analysis.json"
```

Both sides must be present. Repeated relevant IDs are excluded as ambiguous;
identical layout hashes receive one observation. Outputs distinguish any shared
tab from all present selected members sharing one tab. Neither count establishes
adjacency, all family members, player intent or role correctness. Author/revision
independence still requires review. Explicit selection is required; partial web
summaries must never be passed as complete inputs.

Ignored output includes source, registry, probe and analyzer hashes plus
aggregate results without per-template coordinates. Reviewed results are in
[the Main role report](../../../docs/research/main-role-groups-2026-10-05.md).

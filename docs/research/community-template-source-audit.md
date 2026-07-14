# Community Template Source Audit

Audit date: 2026-07-13 (Europe/Paris)

## Repository architecture

- Language/build: Java 11 with Gradle Wrapper 8.7.
- RuneLite dependency: `latest.release`, resolving to RuneLite client 1.12.32 during this audit.
- Entry point: `IronmanBankArchitectPlugin` reads an open bank through supported RuneLite APIs,
  collects equipment stats and high-alchemy values on the client thread, then analyzes plain data on
  a single background executor.
- Bank input: `BankSnapshotReader` records canonical item ID, quantity, global bank-container slot
  index, and placeholder state. Bank fillers and invalid entries are ignored. Duplicate IDs are
  aggregated and retain the first observed slot; a real owned stack supersedes the same-ID
  placeholder state.
- Metadata: `CompositeItemCatalog` combines the curated static catalog and resource registry.
  Unknown IDs become explicit `UNKNOWN` catalog items rather than disappearing.
- Tab classification: `PresetCategoryMapper` maps each catalog category to one of exactly ten
  `BankPreset` categories. Runtime analysis currently always selects the all-round Ironman preset.
- Within-category ordering: `PresetItemSorter` dispatches to specialized deterministic sorters for
  currency, teleports, gear, supplies, Herblore, tools, resources, boss loot, clues, and review.
  These combine metadata with name/subcategory heuristics and stable item-ID fallbacks.
- Layout output: `BankOrganizationPreview` concatenates the ten category lists into one flat planned
  item sequence. `NextMoveAdvisor`, `TabRouteAdvisor`, the sidebar, and overlay turn that sequence
  into manual guidance; they do not automate bank actions.
- Blueprint model: a separate `BankProfile -> BlueprintTab -> BlueprintSection -> VisualBlock ->
  BlueprintSlot` hierarchy exists, but the current all-round blueprint contains only small combat
  and Herblore examples and is not yet the complete runtime placement model.
- Persistence/control: RuneLite configuration currently contains only `suggestNextMove`. There are
  no persisted item pins, slot/section/tab locks, category overrides, exclusions, progression stage,
  confidence, or rule-provenance records.
- Tests: 38 Java test files containing 334 JUnit 4 test methods were present during the audit.

## Why category placement is stronger than finished layout placement

The current pipeline has useful specialized ordering, but it loses information needed for a complete
bank layout:

1. `BankSnapshot` does not retain each item's source tab, only its position in the flattened bank
   container.
2. `BankPreset` requires exactly ten categories but has no sections, family constraints, target
   coordinates, configurable grid width, spacing policy, or confidence/provenance.
3. Most order decisions are comparator ranks. They can create a stable sequence, but cannot express
   multi-cell visual constraints, competing workflow membership, locks, or minimal disruption.
4. The flat preview treats category boundaries as conceptual tabs but does not carry explicit target
   tab/row/column objects through the full pipeline.
5. The move advisers operate after flattening; they cannot optimize a target layout against user
   locks or score two similarly good layouts by move count.

The extension point should therefore be an original intermediate placement model between catalog
classification and the existing preview/guide adapters, not a replacement of the complete plugin.

## Public template source discovery

- Gallery: <https://exchange-insights.gg/tools/osrs-bank-templates>
- Sitemap: <https://exchange-insights.gg/sitemap.xml>
- Access policy: <https://exchange-insights.gg/robots.txt>
- The live sitemap contained 188 unique community-template detail URLs and 188 unique IDs during the
  initial audit. IDs ranged from 6 through 327 and are intentionally non-contiguous.
- The coverage extractor downloaded and parsed all 188 allowed detail pages with zero failures. The
  pages collectively declared 1,706 tabs and 171,162 item placements.
- Public detail pages expose name, author label, declared item count, declared tab count, total import
  count, shared date, and a variable, incomplete set of truncated ordered tab summaries.
- Across this snapshot, 335 of the 1,706 declared tabs had an SSR summary. Two templates had no tab
  summary and the maximum exposed by one template was five.
- Exact item IDs, all tab contents, and absolute positions were not present in the allowed SSR HTML.
- `robots.txt` allows public pages but explicitly excludes `/api/` and `/tools/partials/`. Those paths
  were not accessed.

## Coverage interpretation

The discovered count of 188 is a verified snapshot, not a hardcoded production constant. The public
page extractor re-counts sitemap entries whenever it runs. Until the source owner offers an allowed
bulk export or grants permission for a documented dataset endpoint, the research can honestly report
complete gallery-page coverage but not complete positional-template coverage.

The first complete run therefore has the following status:

- discovered: 188;
- downloaded: 188;
- partially parsed: 188;
- failed: 0;
- duplicate template IDs: 0;
- templates exposing exact positions in allowed SSR HTML: 0;
- templates missing exact positions: 188.

No community layout, raw page, or per-template normalized record belongs in the plugin or version
control. Only aggregate conclusions that cannot reconstruct an individual template may become
versioned research or independently designed production rules.

## GitHub dataset check

Public repository checked: <https://github.com/TheSpryt/bank-templates>

- The repository is public and BSD-2-Clause licensed, but its tree contains no community-template
  dataset or export directory.
- The only bundled preset data file is
  `src/main/resources/com/banktemplates/presets/presets.json`. It is three bytes and contains `[]`.
- The preset file has one history entry, from the initial commit, and has never contained bundled
  presets in the public repository history.
- All five public branches expose the same three-byte empty preset file and no alternative dataset.
- Eleven public releases exist from v1.0 through v1.5.4; none has downloadable release assets.
- The owner's seven public repositories include the plugin but no public Exchange Insights backend,
  community-template data repository, or bulk-export repository.
- GitHub issue and pull-request search found no published JSON export, dataset, or bulk-export
  mechanism.

The plugin README confirms that community browsing is an optional connection to a third-party server
and that templates are community-sourced with no bundled presets. GitHub can therefore confirm the
absence of bundled data, but it cannot provide the 188 layouts or their exact item positions.

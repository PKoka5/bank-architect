# Existing Bank Plugin Research

Research date: 2026-07-09

This is product research only. It is based on RuneLite Plugin Hub descriptions and public product
positioning, not third-party source code. Do not copy, import, adapt, or mirror third-party plugin
code, UI, resources, names, layouts, configuration structures, or implementation details.

## Summary

RuneLite Plugin Hub already has strong plugins for bank tag layouts, activity setups, cleanup,
search, screenshots, value tracking, exports, and some organization assistance. Bank Architect
should not become an autosorter, value dashboard, or setup checker clone.

Bank Architect's strongest lane is:

`read-only whole-bank understanding -> owned-item categorization -> suggested blueprint -> manual guidance -> later export toward Bank Tag Layouts`

Plugin Hub describes Hub plugins as third-party and not affiliated with RuneLite, so Bank Architect
should keep strong originality boundaries and explicit safety docs.

## Plugin Findings

| Plugin | Problem + workflow | What Bank Architect can learn | What to avoid copying | Overlap / relation | Feature idea + safety concern |
|---|---|---|---|---|---|
| Bank Tag Layouts | Virtual layouts inside bank tags; users enable layout from tag tabs and drag tag items into positions. | This is the de facto layout destination. | UI, config, drag behavior, layout storage. | High overlap for final visual layout, but complementary. | Export/import toward BTL later; never reimplement its core. |
| Inventory Setups | Saves gear setups for activities. | Activity-scoped setups are familiar and useful. | Setup panel UX, missing-item checker framing. | Medium overlap only for future goal/setup mode. | Keep missing items out of the main organizer. |
| Wasted Bank Space | Shows items wasting bank space. | Cleanup is a known user pain. | Its item rules and cleanup UI. | Medium overlap for later advisor. | Add advisory cleanup buckets only, never "drop/sell now." |
| Data Export | Exports bank to clipboard as CSV for spreadsheets. | Text/CSV bank snapshots are useful for debugging and sharing. | CSV schema and command flow. | Complementary; important dev/product inspiration. | Add dev/importable snapshot fixtures later; clipboard export can support bug reports. |
| Bank Screenshot | Takes screenshots of the bank. | Visual sharing matters for bank-layout communities. | Screenshot workflow and storage naming. | Low overlap. | Optional shareable blueprint image much later. |
| Bank Helper | Heatmap colors bank items by GE/HA value, labels cheap stacks as junk, and colors untradeables separately. | Value coloring can aid review. | Heatmap color language and junk labels. | Medium overlap for cleanup priority. | Use value only as secondary metadata, not primary sorting. |
| Bank Cleaner | Compares armour/weapons within equipment type to find redundant gear. | Redundancy detection is valuable but domain-heavy. | Gear comparison logic and sell/drop recommendations. | Medium overlap for cleanup advisor. | Later "review redundant gear" with conservative language. |
| Bank Value / Tracker / History / Changes | Sidebar value, value over time, item value changes. | Users like value summaries, but this is well-served. | Dashboard/history/value-first product. | Low-to-medium overlap. | Show value only as optional context for cleanup priority. |
| Bank Tab Organizer | Category overlays, stats/skill sorting, step-by-step reordering. | Very close to organization guidance; validates demand. | Names, UI, category model, step-by-step UX. | High overlap and biggest differentiation risk. | Differentiate as owned-item blueprint planner, not autosorter/reordering assistant. |
| Bank Slot Sync | Syncs variants, recolors, and ornament kits to Bank Tag Layout slots. | Variant-aware slots are a real advanced need. | Sync behavior and BTL mutation details. | Complementary. | Model "slot accepts variants" for export, but do not sync third-party layouts ourselves. |
| Bank and POH Searcher | Searches bank and POH storage locations. | External storage visibility is valuable. | Search UI and storage database behavior. | Complementary. | Later external-storage advisor can point to POH, Seed Vault, and STASH without becoming search. |
| Auto Bank Sorter | Sorts into skill tabs and requires Bank Tag Layouts. | Demand exists for generated layouts. | Autosorter positioning, category taxonomy, dynamic re-sorting. | High overlap/risk. | Avoid "auto sort" language; emphasize manual blueprint and originality. |
| Bank Organizer | Preset/toggleable categories, exclusions, color coding. | Users need category control and exclusions. | Category UI/config model. | High overlap. | Add simple user exclusions later, but keep MVP curated. |
| Bank Tag Generation / Wiki Bank Tools | Creates or highlights bank tags from OSRS Wiki categories. | Wiki-category import is a known pattern. | Wiki category mapping and tag generation UX. | Medium overlap. | Use curated rules first, not generic wiki categories. |
| Storage / POH Storage / CostumeRoomHighlighter / Potion Storage plugins | Search, filter, or visualize external storage and POH/potion storage. | External storage is a major bank-space solution. | Their storage overlays and filter UI. | Complementary. | Cleanup advisor should say "candidate for external storage," not manage storage. |
| Search plugins: Fuzzy Bank Search, Regex Bank Search, Bank Slot/multisearch | Better bank search by typo, regex, equipment slot, or multiple names. | Search is well-covered. | Search syntax and feature set. | Low overlap. | Use internal filtering only for blueprint UI, not a search plugin clone. |
| Recent/Banked Items, Placeholders Warning, Bank Highlighter, Bank Notes | Recent item recall, placeholder warning, manual colors, notes. | Small focused bank utilities win when they solve one job. | Their UI affordances and config styles. | Low-to-medium overlap. | Placeholder/spacer awareness matters for future layout export. |

## Final Comparison

### Features Bank Architect should definitely build

- Whole-bank owned-item scan and categorization.
- Curated category catalog: currency, runes, teleports, herblore, potions, common tools.
- Suggested blueprint with tabs, sections, rows, cells, and unknown/review bucket.
- Neutral overlay until physical slot validation exists.
- Later physical validation: green correct physical slot, orange owned elsewhere, red wrong item, gray unknown.
- Local/debug snapshot export or import path for testing and user bug reports.

### Features Bank Architect should leave to existing plugins

- General bank search, fuzzy search, and regex search.
- Bank value dashboards and value history.
- Bank screenshots as a primary feature.
- Activity setup management as the main product.
- Direct Bank Tag Layout editing or syncing.
- Value heatmaps and manual color tagging.

### Features Bank Architect should integrate or export toward

- Bank Tag Layouts as the primary future export target.
- CSV/text snapshot style, inspired by Data Export, for debugging and fixture generation.
- External storage concepts: Seed Vault, POH, STASH, and Potion Storage recommendations.

### Plugin Hub risky areas

- Any autosorter or automatic reordering language.
- Any mouse, keyboard, click, or drag automation.
- Mutating bank tabs, widgets, or native layouts.
- Copying UI, config, category structures, or naming from existing plugins.
- Runtime network calls, telemetry, or external processes.
- Cleanup wording that sounds like "drop/sell this now."

### Gaps no existing plugin solves well

- A read-only, whole-bank, owned-item-first blueprint planner.
- Ironman-oriented organization by workflows, not market value.
- Blueprint generation that can later export to Bank Tag Layouts without replacing Bank Tag Layouts.
- Clear physical-slot validation semantics separated from "you own this somewhere."
- Conservative unknown/review handling for items outside the curated catalog.

## Recommended MVP After Research

- Keep roadmap order: C3 -> C4a -> C4b -> C5 -> C6 -> C7 -> C8 -> D -> E -> F.
- C3 must fix product framing and make overlay neutral.
- C5 catalog should remain curated, not bulk-generated from all ItemIDs.
- C6 should produce a whole-bank organization plan only from owned items.
- D should export toward Bank Tag Layouts instead of recreating its editing workflow.
- F setup/missing-item checks should stay optional and separate from main mode.

## Source Notes

- RuneLite Plugin Hub overview and third-party status: https://runelite.net/plugin-hub/
- Plugin Hub descriptions reviewed for: Bank Tag Layouts, Inventory Setups, Wasted Bank Space,
  Bank Screenshot, Bank Helper, Bank Cleaner, Bank Value, Bank Value Tracker, Bank Tab Organizer,
  Bank Slot Sync, Bank and POH Searcher, Data Export, and adjacent bank/storage/search plugins.
- Public behavior may inspire product direction, but implementation must stay original to this
  repository.

# Main community templates: initial measurements

Measured on 2026-10-05 after the owner requested community-template data as the
basis for an original All-round Main algorithm and category design.
Inputs are selected public imports cached in July 2026. This is an initial
four-template study, not a completed comparison of most Main banks.

## Source cohort

- [8: Max Main](https://exchange-insights.gg/tools/osrs-bank-templates/8/max-main)
- [14: Semi-organized Main](https://exchange-insights.gg/tools/osrs-bank-templates/14/semi-organized-main)
- [26: Main - End Game](https://exchange-insights.gg/tools/osrs-bank-templates/26/main-end-game)
- [55: Main PvM focussed](https://exchange-insights.gg/tools/osrs-bank-templates/55/main-pvm-focussed)

These have four distinct cached layout hashes and four author labels in the July
metadata snapshot. Labels and differing hashes do not prove source independence;
near-duplicate/revision checks remain necessary. The cohort is biased toward
late/endgame. The July gallery metadata had 20 explicit Main-title candidates;
only these four have complete selected observations in our current cache.

Today, public indexed detail summaries were accessible, but direct gallery and
sitemap refresh failed, including DNS resolution from the development shell.
No fresh complete import or excluded endpoint was used. Current gallery coverage
and popularity are not established by this study.

## Reproduced extraction

Both developer-only analyzers now accept explicit `-RepoIds "8,14,26,55"`.
The shared selector reads only the selected normalized inputs, deduplicates
requested IDs and rejects missing inputs. Without a selection the historical
all-input behavior remains; those mixed-account results are not Main results.

Commands are documented in [the research tool README](../../tools/research/community-templates/README.md).
Outputs stay in the ignored directory
`tools/research/community-templates/cache/main-2026-10-05/`.

| Measurement | Initial result |
| --- | ---: |
| Selected templates | 4 |
| Tabs | 39 |
| Positive item placements | 3,942 |
| Placements matched to our item registry | 3,942 |
| Placements with existing family/variant sort metadata | 484 (12.28%) |
| Curated family signals | 47 |
| Recurring adjacency edges passing exploratory thresholds | 142 |

Placements are template entries, not quantities or a count of independent votes.
The follow-up exact-ID check found 21 Bank filler entries among the 3,942 positive
positions; they are not all genuine bank items. The
[role comparisons](main-role-groups-2026-10-05.md) exclude fillers from every probe.
Matching an ID in our registry does not establish correct semantic classification.
The current category analysis uses our registry's labels, so its category-block
counts cannot independently prove that those same categories are appropriate.
Family metadata covers only a small fraction of placements; gear and other
uncovered relationships require further independent role/family curation.

Family-analysis schema 4 fingerprint:
`f62f7a4958702abc0d3352c24eee8f6013f55fb732073f66573b95c8c22e7404`.
It includes source layout hashes, our catalog hashes and analysis thresholds.

## Initial family observations

An eligible family observation has at least two distinct supported members.
Missing members, split-tab families and duplicate-ID ambiguity are reported
separately. Cohesion is measured with the existing exact-family analyzer; it is
not a recommendation to copy coordinates or to purchase missing items.

| Existing catalog family | Cohesive / eligible | Split-tab observations |
| --- | ---: | ---: |
| Diamond | 4 / 4 | 0 |
| Emerald | 4 / 4 | 0 |
| Prayer potion | 2 / 2 | 0 |
| Ranging potion | 2 / 3 | 1 |
| Super restore | 2 / 2 | 0 |
| Stamina potion | 1 / 2 | 1 |

These suggest research candidates, not universal defaults. Even cohesive gem
families use different orientations and directions across the sample. The dose
results leave our proposed Main dose policy open for a larger comparison.

## Denominators and interpretation

For recurring item-pair candidates, schema 4 reports:

- **CoPresentTemplateSupport:** both items appear anywhere in the template.
- **CoTabTemplateSupport:** both appear in a shared tab.
- **AdjacentTemplateSupport:** both are immediately adjacent in a shared tab.
- The same-tab and adjacency shares of co-presence, alongside the existing
  adjacency confidence conditioned on sharing a tab.

The 142 edges still pass the historical exploratory thresholds: adjacency in
at least three templates and at least 60% of same-tab observations. The new
denominators describe those candidate edges only. They are not a complete
co-location matrix, and an adjacency graph component is not a validated category.

To choose categories, the next pass needs co-location evidence for independently
identified roles/families, including pairs that are rarely adjacent. Compare
combined versus separate combat-style groups, supplies, materials, functional
outfits, cosmetics and loot across progression/activity strata. Use equal source
votes first and show **x/y eligible templates**. Popularity is a separate
sensitivity comparison, not a count of Main players.

## Checks

- Both selected-cohort analyzers completed with exactly four templates, 39 tabs
  and 3,942 positive placements.
- Independent input counting verified all **142** co-presence denominators and
  new ratios; adjacency support <= same-tab support <= co-presence support.
- Repeating ID 8 in the selection still counted four templates with unchanged
  totals. A missing selected ID failed without writing an analysis output.
- PowerShell syntax checks passed; read-only tool review found no blocker.
  `git diff --check` passed with routine LF/CRLF advisories.
- Only development tools and planning/research documentation changed. No plugin
  runtime code or resource changed; no code update was committed or published.

## Next step

The [expanded candidate search](main-template-candidates-2026-10-05.md) found nine
more explicitly Main-titled public detail pages: 13 sourced candidates including
the original four. They expose partial summaries only, so the exact cohort and
the measurements above remain unchanged. Prioritize the explicit midgame and
smaller/compact-bank comparisons when complete inputs become available.

Expand and review the Main cohort to 12-20 usable templates, especially midgame
and smaller banks. Preserve complete-input versus partial-summary distinctions,
check author/layout clusters and measure the full role co-location evidence.
Then revise the provisional categories in [the Main plan](../plans/main-preset-2026-10-05.md)
and translate supported patterns into original rules using our shared catalog,
sorters and dense bank-layout constraints.

# Item classification audit

## Actual Main and Ironman placement

The original effective exporter below remains compatible with its historical cache format.
For release work, also export the real preview builder's final tags and destinations:

```powershell
.\gradlew.bat exportFinalItemPlacements
powershell -NoProfile -ExecutionPolicy Bypass -File tools\research\item-classification-audit\audit-final-item-placements.ps1 -FailOnReviewedMismatch
```

Output lives under ignored `build/reports/item-classification/`. `review-ledger.tsv`
keeps **every** included ID/preset scenario, even without exact source facts.
`priority-review.tsv` highlights mismatches and review signals. Neither automatically
changes classifications. Explicit cache records and the bank filler are reported separately;
remaining records are **not** certified bankable items. The source cache supplied by default
is the historical **2026-07-15** snapshot, visibly stale after 30 days. Supply its actual
collection date/revision when using another file; the report records input SHA-256 hashes.
`-RequireFreshSource` refuses missing/stale source input, but age alone never certifies roles.

The independently reviewed reference is
`src/test/resources/com/pkoka5/ironmanbankarchitect/research/reviewed-item-roles.tsv`.
It records exact IDs, expected catalog roles, both default tags/tabs, source revision,
review date and rationale. Normal Gradle `check` tests these expectations against the real
catalog and builder. Do not fill this reference by exporting the current classifier: expected
roles must be reviewed independently. Add positive cases and nearby nonmatching tiers,
charged states or similar names when changing a rule.

The exhaustive export uses isolated quantity-one items without runtime stats, prices,
upgrades, player assignments or captured layouts. It is an observation, not a correctness
certificate for mixed banks. Existing contextual Alch, custom assignment, captured-bank,
placeholder and mixed-bank tests remain required. The comparison rejects other contexts,
duplicate scenarios and missing Main/Ironman counterparts instead of mislabeling them.

Run the developer tool tests:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tools\research\item-classification-audit\test-audit-final-item-placements.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File tools\research\item-classification-audit\test-track-item-changes.ps1
```

## New game items and changed functions

Follow [the item change workflow](CHANGE_TRACKING.md) before releases. Refresh both
RuneLite identities **and** independent gameplay facts; a RuneLite dependency upgrade
does not update the bundled registry. Compare against an explicitly saved previous
snapshot. Review `NEW_ID`, changed names and changed facts on existing IDs, including
tiers and variants whose functions differ. Missing or stale source facts remain open
review work, never evidence of no use or safe disposal.

For each actionable change, either add sourced exact-ID facts and independent placement
tests, or record an explicit unresolved decision and retain conservative manual review.
Only then deliberately save the next baseline. Snapshot creation records observations;
it does not approve them. This release workflow is a developer responsibility; the plugin
performs no downloads, telemetry or automatic update of item rules.

## Historical catalog and source comparison

This developer-only workflow exports Bank Architect's effective Ironman classification and compares
it with cached OSRS Wiki facts. It never runs inside the RuneLite plugin and never changes production
classification rules.

Run the effective export:

```powershell
.\gradlew.bat exportEffectiveItemClassifications
```

Run the source comparison and aggregate report generator:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File `
  tools\research\item-classification-audit\audit-effective-classifications.ps1
```

Use `-Refresh` only when a new local source snapshot is required. The fetcher uses HTTPS, a
descriptive user agent, a minimum one-second request interval, and the OSRS Wiki public bulk APIs.
Raw TSV/JSON data is written below `cache/`, which is git-ignored. Only reviewed aggregates and
source links belong under `docs/research/`.

The report is a contradiction detector, not an automatic correction list. Missing or ambiguous
source facts are marked `UNVERIFIED`; absence from a Wiki table is never treated as proof.

For a strictly offline coverage pass against the existing identity snapshot:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tools\research\item-classification-audit\audit-cached-coverage.ps1
```

This joins every effective ID to the July 15 snapshot and writes a complete review ledger and
aggregate summary under `build/`. It detects quest lifecycle, restricted namesakes and static
equipment review candidates without changing classifications. Nonzero stat magnitude includes
penalties: a candidate is not an automatic Gear correction. Source identity coverage is distinct
from manually verified usage coverage or current disposal eligibility.

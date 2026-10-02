# Item classification audit

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

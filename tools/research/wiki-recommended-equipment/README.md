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

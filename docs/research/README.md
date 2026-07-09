# Research Data

This folder contains generated research artifacts. They are not production catalog data.

## Product Research

`existing-bank-plugin-research.md` captures C4a product research on existing RuneLite Plugin Hub
plugins related to banks, inventories, setups, exports, cleanup, bank tags, layouts, value, search,
and external storage. It is product positioning research only; do not copy third-party code, UI,
resources, naming, layouts, configuration structures, or implementation details.

## Item ID Research Index

`item-id-research-index.tsv` is generated from the local RuneLite API source cache by:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tools\generate-item-id-research-index.ps1
```

The generator reads `net/runelite/api/gameval/ItemID.java` from the cached RuneLite sources jar and
classifies constants with simple heuristics. The output is useful for catalog research, owned-bank
layout planning, and finding candidate items for curated category batches.

Production rules:

- Keep `StaticItemCatalog` curated.
- Do not bulk-import this TSV into production.
- Treat `exclude-main-catalog` rows as research-only unless explicitly reviewed.
- Treat `LOW` confidence rows as suggestions, not truth.
- Prefer organizing items the player actually owns over building missing-item checklists.

## Category Classifier Report

`category-classifier-report.md` and `category-classifier-detail.tsv` are generated from the
production item registry by:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tools\generate-category-report.ps1
```

Use this report to improve broad classifier rules across the full item registry instead of tuning
rules only from one player's current bank.

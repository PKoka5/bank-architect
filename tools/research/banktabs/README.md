# BankTabs Pixel Research

Dev-only tooling for learning bank-organization patterns from manually collected OSRS bank screenshots.

This folder is not used by the RuneLite plugin at runtime.

## Folder Shape

```text
input-screenshots/   local screenshots you intentionally save for research
output-crops/        generated per-slot crops and contact sheets
reference-icons/     local item-icon references named with item IDs
match-results/       generated TSV candidate matches
reviewed-patterns/   human-reviewed aggregate pattern notes
```

## Safety Rules

- Do not bundle Reddit screenshots with the plugin.
- Do not scrape Reddit from the plugin.
- Do not copy exact third-party layouts into production.
- Use screenshots only to discover repeated, reviewed organization patterns.
- Production output should be original Bank Architect rules, not imported templates.

## Workflow

1. Manually save screenshots into `input-screenshots/`.
2. Crop the visible bank grid into item-slot images with `crop-bank-grid.ps1`.
3. Later, match cropped slots against a local item-icon reference set.
4. Manually review matches.
5. Save only aggregate pattern notes, such as:
   - gear columns by set/style;
   - common teleport/rune/jewellery ordering;
   - resource and cleanup cluster order;
   - placeholder spacing habits.

## Crop Example

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tools\research\banktabs\crop-bank-grid.ps1 `
  -InputImage tools\research\banktabs\input-screenshots\example.png `
  -OutputDir tools\research\banktabs\output-crops\example `
  -OriginX 12 `
  -OriginY 48 `
  -Columns 8 `
  -Rows 14 `
  -SlotWidth 36 `
  -SlotHeight 32 `
  -GapX 2 `
  -GapY 2
```

Adjust `OriginX`, `OriginY`, slot size, and gaps per screenshot until the crops align with actual bank slots.

## Next Tooling Step

Add item-icon references to `reference-icons/`.

Reference filenames should start with the item id, followed by any readable name:

```text
11834-bandos-tassets.png
11235-dark-bow.png
```

Then run `match-slot-icons.ps1` to compare cropped slots with those local references:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tools\research\banktabs\match-slot-icons.ps1 `
  -CropsDir tools\research\banktabs\output-crops\example `
  -ReferenceDir tools\research\banktabs\reference-icons `
  -OutputTsv tools\research\banktabs\match-results\example.tsv `
  -TopN 5
```

The matcher writes:

```text
screenshot_id	slot	candidate_rank	candidate_item_id	candidate_name	score	confidence
```

The confidence output is only a review aid. It must not write production category rules automatically.

Reviewed pattern notes belong in `reviewed-patterns/`. Those notes should describe repeated ideas in our own words, not reproduce somebody's exact bank.

## Current Limitation

The cropper supports PNG/JPEG through Windows `System.Drawing`.

Many Reddit downloads are WebP. Convert those screenshots to PNG before cropping. If ImageMagick or ffmpeg is installed later, we can add a bulk conversion helper.

# BankTabs Pixel Research

Dev-only tooling for learning bank-organization patterns from manually collected OSRS bank screenshots.

This folder is not used by the RuneLite plugin at runtime.

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

Add an icon-matching script that compares these slot crops with a local item-icon reference set and writes:

```text
screenshot_id,slot,candidate_item_id,candidate_name,confidence,review_status
```

The confidence output is only a review aid. It must not write production category rules automatically.

# Bank Architect

Bank Architect is a read-only RuneLite sidebar plugin that analyses your bank and creates a deterministic manual organisation blueprint. The current release candidate includes the **Ironman — All-Round Bank** preset.

## Features

- Scans the open bank through supported RuneLite APIs after you choose **Analyze My Bank**.
- Organises owned items into ten purpose-driven blueprint tabs.
- Preserves quantities, real placeholders, and every owned item exactly once.
- Uses semantic layouts for equipment sets, achievement diary items, runes, potion workflows, skilling outfits, resources, and other reviewed item families.
- Shows the complete blueprint with item icons and provides a text export for review.
- Offers an optional guide that highlights one manual bank move at a time.

## Using the plugin

1. Open your bank and select **Analyze My Bank** in the Bank Architect sidebar.
2. Select **Show My Bank** to inspect the generated blueprint.
3. Optionally copy the blueprint export or enable the Bank Guide.
4. When using the guide, keep the vanilla bank on **All items** and **Swap** mode and perform every suggested move yourself.

The blueprint is based only on items you currently own. Missing items are never invented and Bank Architect does not create artificial blank slots.

## Safety and privacy

Bank Architect is an advisory tool. It never clicks, drags, types, sends packets, manipulates game state, or automates bank actions. It does not use reflection, native code, external processes, telemetry, or runtime network requests. It does not read the player's inventory or equipped items.

## Development

The project targets Java 11 and uses the RuneLite Plugin Hub development structure.

Run all tests:

```powershell
.\gradlew.bat clean test
```

Start the local RuneLite development client:

```powershell
.\gradlew.bat run
```

## Data sources

The plugin ships curated, pinned item metadata derived from Old School RuneScape Wiki data. Source URLs, revision identifiers, retrieval dates, and licenses are recorded in [`item-sort-metadata-sources.tsv`](src/main/resources/com/pkoka5/ironmanbankarchitect/catalog/item-sort-metadata-sources.tsv).

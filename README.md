# Bank Architect

Bank Architect is a read-only RuneLite sidebar plugin for planning an organized
Ironman bank. It scans the bank you have open, creates a deterministic
blueprint for the items you own, and can highlight one safe manual move at a
time. You remain in control of every bank action.

The currently selectable workflow is **Ironman — All-Round Bank**, which sorts
owned items into ten purpose-driven destinations.

## Installation

After the plugin is published to the Plugin Hub:

1. Open RuneLite's configuration sidebar and select **Plugin Hub**.
2. Search for **Bank Architect** and choose **Install**.
3. Open the **Bank Architect** sidebar tab.

Bank Architect has no separate installer, account, or external service.

## Ironman — All-Round Bank workflow

1. **Scan:** open your bank and select **Analyze My Bank**. The plugin reads the
   open bank through supported RuneLite APIs.
2. **Review the blueprint:** select **Show My Bank** to inspect the proposed
   Main section and nine purpose-driven tabs. The blueprint preserves owned
   item IDs, quantities, and real placeholders; it does not invent missing
   items or blank slots.
3. **Prepare the bank:** open the vanilla **All items** view and clear bank
   search and bank-tag filters. Either rearrange mode works; **Insert** mode
   usually needs fewer drags than **Swap** and the guide reports both counts.
4. **Follow manual guidance:** select **Show Bank Guide**. The overlay describes
   one supported manual tab or item move at a time. You perform every collapse,
   drag, swap, and drop yourself.
5. **Finish sorting:** the guide re-reads the bank after each manual action and
   advances only when the observed state is safe and consistent with the plan.

You can also copy the text blueprint for personal review without starting the
move guide.

## Correcting a classification

Classification fails closed, so an item the bundled data cannot place
confidently lands in **Storage & Cleanup** rather than being guessed into a tab
from a name resemblance. When that is wrong, or when you simply want an item
somewhere else, you can say so:

1. Select **Assign Categories** in the sidebar.
2. Right-click the item in your bank and choose **Bank Architect**, then the
   destination you want.
3. Select **Analyze My Bank** again to rebuild the blueprint.

Your correction wins over every automatic rule and is stored locally with the
plugin's settings, so it survives a client restart. **Reset Corrections** clears
them all; choosing **Use automatic classification** clears a single item.
Corrections apply to the real item, so making one on a placeholder works too.

## Safety and privacy

Bank Architect is analysis and guidance software, not bank automation.

- It reads the open bank through supported RuneLite APIs and draws a sidebar,
  blueprint dialog, and input-transparent guide overlay.
- While **Assign Categories** is on it adds its own options to the bank
  right-click menu. Those options only record your choice in local settings;
  they perform no bank action.
- It never clicks, drags, types, sends packets, changes widgets, manipulates
  game state, or performs bank actions.
- It does not use reflection, native code, external processes, runtime network
  requests, analytics, telemetry, or external services.
- It does not read the player's inventory or equipped items.
- The player always performs and confirms every bank move manually.

Guidance fails closed when it cannot safely interpret the bank. In particular,
it pauses unless the bank is open in vanilla **All items** with search and
bank-tag filters cleared. It also pauses on unsupported tab states, unsafe
geometry, or a bank view that no longer matches the expected plan. Each advised
move is verified against the bank change you actually made — in Swap mode an
exchange, in Insert mode a single-item shift — and guidance pauses rather than
guessing when the observed change is something else.

The generated blueprint stays in the running client. Bank Architect does not
upload bank contents or send them to the OSRS Wiki or another service.

## Local data

Classification and sorting use curated datasets bundled inside the plugin jar.
Their sources, retrieval dates, revisions, and licences are pinned in the
repository. There are no runtime Wiki calls and no remotely updated rules.
Unknown or weakly supported classifications fail closed into the
**Storage & Cleanup** review tab instead of being confidently routed from a
name resemblance. Your own corrections are the way out of that tab and are the
only classification input that is not bundled with the plugin.

## Current limitations

- **Ironman — All-Round Bank** is the only selectable preset. Internal Main,
  PvM, PvP, and Skiller foundations are not available in the interface.
- Roadmap features that still require complete pinned data or a maintainer
  policy are not presented as shipped. This includes GE-value loot ordering
  and selectable additional presets.
- Storage & Cleanup is a deliberate review destination. It can contain an item
  the player chooses to keep; the plugin never drops or removes anything. Items
  that land there wrongly can be reassigned by hand.
- Manual guidance supports only the bank states and move types it can validate
  safely. It pauses rather than guessing.

## Screenshots

The sidebar with a completed whole-bank scan while the guide walks the
distribution phase — one highlighted manual drag at a time:

![Bank Architect sidebar and distribution guidance](docs/screenshots/bank-guide-distributing.png)

The generated blueprint with the Main section and nine purpose-driven tabs:

![Bank blueprint dialog](docs/screenshots/bank-blueprint.png)

The sorting phase inside a planned tab, showing the validation grid, the
highlighted FROM/TO swap, and the exact minimum number of drags remaining (in
Insert mode the same grid highlights a MOVE/DROP pair instead):

![Sorting guidance with minimum swap count](docs/screenshots/bank-guide-sorting.png)

## Support

Questions, bug reports, and suggestions go through
[GitHub Issues](https://github.com/PKoka5/ironman-bank-architect/issues).
Bank Architect is a third-party Plugin Hub plugin; RuneLite itself does not
provide support for it.

## Development

The project targets Java 11 and follows RuneLite's standard Plugin Hub project
layout.

Run the tests:

```powershell
.\gradlew.bat test
```

Start the local RuneLite development client:

```powershell
.\gradlew.bat run
```

The pinned data-source manifest is
[`item-sort-metadata-sources.tsv`](src/main/resources/com/pkoka5/ironmanbankarchitect/catalog/item-sort-metadata-sources.tsv).

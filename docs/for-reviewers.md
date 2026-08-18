# Notes for Plugin Hub reviewers

This plugin is larger than most Hub submissions, so this page exists to make the
one question that matters — *does it automate anything?* — quick to answer.
Everything below is checkable with `grep`.

## The short version

The plugin reads the open bank, computes a target layout, and draws. The player
performs every bank action by hand. There is no network access, no reflection,
no external process, no file I/O, and no third-party dependency.

## Only four files touch live client state

Of 120 production source files, exactly four import anything that reads or
reacts to the running game:

| File | Lines | What it does |
|---|---|---|
| `bank/BankSnapshotReader.java` | 79 | Reads `InventoryID.BANK` into a plain list of item IDs and quantities. |
| `IronmanBankArchitectPlugin.java` | ~330 | Plugin lifecycle, and the bank right-click menu entries described below. |
| `overlay/BankGuideOverlay.java` | ~1380 | Draws the move guidance. Mostly geometry. |
| `overlay/BankCategoryOverlay.java` | ~250 | Draws a colour per item. Nothing else. |

```sh
grep -rlE 'net\.runelite\.api\.(Client|widgets|ItemContainer|MenuEntry|events|Menu;)' src/main
```

Twelve further files import `net.runelite.api.gameval.ItemID` and nothing else —
those are compile-time item-ID constants in classification tables, with no
client access. The remaining 104 files import no RuneLite API at all: they are
the item catalogue, the layout engine, and the sorters, all pure functions over
plain data. That is where the line count lives.

## Things a reviewer will reasonably want to check

**The menu entries.** `onMenuOpened` adds a "Bank Architect" submenu to a bank
item, but only while the player has switched on assign mode in the side panel.
Choosing an entry writes one `itemId=categoryKey` pair into plugin config and
re-runs the analysis. It performs no menu action, sets no selected widget, and
sends nothing to the server. See `applyCategoryOverride`.

**The one `java.net` import.** `catalog/ResourceItemSortMetadataCatalog.java`
imports `java.net.URI`. It is used to validate that the source-attribution
strings in a bundled TSV are absolute HTTPS URLs, so the data manifest cannot
carry a malformed citation. No connection is ever opened. It is the only
`java.net` reference in the plugin.

**Bundled data is 1.9 MB.** Seven pinned TSV/text datasets under
`src/main/resources`, the largest being an item registry of ~32,500 entries.
They are read with `getResourceAsStream` and are never written, downloaded, or
refreshed at runtime. Classification is fully offline and deterministic; the
plugin makes no Wiki or price-API calls.

**Threading.** Analysis runs on the client's injected
`ScheduledExecutorService`. The plugin creates no threads of its own. Item
stats and prices are collected on the client thread first, so the background
task works only from plain maps.

**A separate window.** "Show My Bank" opens a `JDialog` with a read-only
preview of the planned layout. It is disposed in `shutDown`.

## Why another bank plugin

Most bank organisers on the Hub render a *virtual* layout: the bank interface is
repainted so it looks organised while the real bank is untouched. This plugin
does the opposite — it guides the player through physically rearranging the real
bank, one manual drag at a time, and therefore never writes to a bank widget.
That is also why it is stricter than it has to be: it does not modify widgets at
all.

## Fail-closed behaviour

Move guidance stops rather than guesses. It requires the vanilla All items view
with search and bank-tag filters cleared, a bank state that matches the analyzed
plan, and safe widget geometry. Each advised move is verified against the change
the player actually made — a transposition in Swap mode, a single-item shift in
Insert mode — and guidance pauses when the observed change is anything else.

The destination-colour overlay only draws and therefore has no such gates.

## Verification

```sh
./gradlew test                # 848 tests
./gradlew simulateRandomBanks # 150 generated banks, all reach a complete plan
./gradlew aggregateCleanupReview
```

The simulations are deterministic: they replay fixed seeds through the whole
planner and assert every bank terminates in a complete, dense layout with no
stalled or non-terminating route.

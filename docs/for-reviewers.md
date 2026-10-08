# Notes for Plugin Hub reviewers

This plugin is larger than most Hub submissions, so this page exists to make the
one question that matters — *does it automate anything?* — quick to answer.
The live-client boundary and behavior described below can be checked in the source.

## The short version

The plugin reads the open bank, computes a target layout, and draws. The player
performs every bank action by hand. There is no network access, no reflection,
no external process, no file I/O, and no third-party dependency.

## Live-client read boundaries

These files contain the supported bank/client adapters:

| File | What it does |
|---|---|
| `bank/BankSnapshotReader.java` | Reads `InventoryID.BANK`, placeholder definitions and `BANK_TAB_1..9` counts into plain snapshots and physical tab orders. |
| `bank/BankItemIds.java` | Resolves placeholders to canonical IDs through supported item definitions. |
| `IronmanBankArchitectPlugin.java` | Plugin lifecycle, bank analysis adapter, local blueprint persistence and the bank right-click menu entries below. |
| `overlay/BankGuideOverlay.java` | Reads visible bank/widget state and draws manual move guidance. |
| `overlay/BankCategoryOverlay.java` | Reads bank widgets and draws destination colours. |

```sh
rg -l 'net\.runelite\.api\.(Client|widgets|ItemContainer|MenuEntry|events|Menu;)' src/main/java
```

Classification and layout code also uses compile-time `gameval.ItemID` constants.
The planner and sorters otherwise operate on immutable plain data, not live bank
actions. Source and line totals are deliberately omitted because they change
between releases.

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

**Bundled data.** Pinned TSV/text datasets under `src/main/resources` include
the item registry, classification overrides, item roles and semantic sets.
They are read with `getResourceAsStream` and are never written, downloaded, or
refreshed at runtime. Classification is fully offline and deterministic; the
plugin makes no Wiki or price-API calls.

**Threading.** Analysis runs on the client's injected
`ScheduledExecutorService`. The plugin creates no threads of its own. The bank
snapshot, item stats, prices, category corrections, layout, and layout options
are captured on the client thread first, so the background task works only from
immutable plain-data snapshots and values.

**Local blueprint editing.** **Open Blueprint** opens a `JDialog` with the
planned layout. Select/Swap/Insert, category assignment and Undo change a local
draft only; Save persists the draft to plugin config after fresh bank and
profile checks. The sidebar Layout editor assigns categories and tags. Dialogs
are disposed during shutdown. Copy/export actions use the clipboard.

**Capture the current bank.** **Save current bank...** reads the full supported
bank container and tab counts on the client thread and saves them as a new named
active layout. It rejects fillers, invalid tab boundaries, stale contents and
profile overflow before configuration writes. It preserves prior saved layouts
and changes no game state. Captured destinations are explicit local item-order
records; unknown future storage versions cannot be overwritten.

## Why another bank plugin

Two things separate this from the bank organisers already on the Hub, and they
are also where most of the line count comes from.

**It computes a layout for your bank rather than applying a template.** The
common approach is a fixed template, or a set of category rules that decide
*which tab* an item belongs to and stop there. A template only works if your
bank resembles the one it was built from; a tab assignment leaves the inside of
the tab in whatever order the items happened to be. Here the destination is only
the first step. `organize/layout/` is a placement engine that packs
each tab from the items you actually own: semantic rule sets propose
blocks — gear sets, rune blocks, potion doses grouped by dose, resource zones by
skill, tool and outfit sets — and a scored packer chooses a placement that fits
the tab's real width and item count. Per-category sorters order what is
left. Nothing is placed that you do not own, and no filler is invented.

**Gear is grouped by combat style and slot, with the best first.** Melee, ranged
and magic get their own lanes, each slot forms a row, and within a slot the
order is decided from real equipment stats plus a reviewed gear tier catalogue
— so a strength set, a ranged set and a mage set end up as recognisable blocks
instead of an alphabetical pile. The same stat comparison drives the alch
review, alongside reviewed alch suitability, quantity, value and owned-alternative
checks. Quantity alone cannot move unreviewed gear into the alch pile.

Both claims are checkable. `docs/research/` holds the dated studies the rules
were built from, and `./gradlew aggregateCleanupReview` replays 1,800 generated banks
through the whole planner and asserts every one reaches a complete, dense layout
with no stalled or non-terminating route.

Separately: most organisers render a *virtual* layout, repainting the bank
interface so it looks organised while the real bank is untouched. This plugin
does the opposite — it guides the player through physically rearranging the real
bank, one manual drag at a time. That is why it never writes to a bank widget at
all, which is stricter than the rules require.

## Fail-closed behaviour

Move guidance stops rather than guesses. It requires the vanilla All items view
with search and bank-tag filters cleared, a bank state that matches the analyzed
plan, and safe widget geometry. Each advised move is verified against the change
the player actually made — a transposition in Swap mode, a single-item shift in
Insert mode — and guidance pauses when the observed change is anything else.
For a destination with **Keep current order**, a stable manual reorder is also
accepted when it preserves that destination's members and leaves every other
destination and all tab boundaries unchanged.

The destination-colour overlay only draws and therefore has no such gates.

## Verification

```sh
./gradlew test                # full unit suite
./gradlew simulateRandomBanks # 150 generated banks, all reach a complete plan
./gradlew aggregateCleanupReview
./gradlew build --offline      # full suite and reviewed simulation baselines
```

The simulations are deterministic: they replay fixed seeds through the whole
planner and assert every bank terminates in a complete, dense layout with no
stalled or non-terminating route.

The [0.9.2 release record](release-0.9.2.md) documents final tests, outstanding live
checks, jar checksum, changed scope and calibrated review-token estimates.
Local estimates are not the official Plugin Hub count.

Version 0.9.2 keeps established session Alch decisions when IDs are added and
existing facts/choices remain compatible. Fresh armour placeholders can supply
only a higher exact curated tier in the same style/slot for reviewed stock;
other replacement rules retain real-ownership requirements. Only a
generation-current successful analysis commits session history; invalidation and
shutdown clear it. The [fresh-placeholder review](testing/placeholder-armour-alch-2026-10-08.md)
covers policy and regressions. Live guidance remains manual and validates
observed transitions, including adjacent whole-tab moves.

The [classification audit](research/final-placement-audit-2026-10-06.md) records
exact-ID corrections, independent reference coverage and deliberately reviewed
simulation report changes. New exporters and update-tracking tools run only during
development, have no runtime entry point, and are excluded from the plugin jar.
Unregistered items cannot enter automatic Alch merely from stats, value or quantity;
explicit player assignments retain their normal priority.

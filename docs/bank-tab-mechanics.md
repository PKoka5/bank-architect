# OSRS bank tab mechanics (verified)

Ground truth for the tab-aware guidance planner. Sources: the OSRS client
scripts (`bank_gettabrange`, `bankmain_build`, via the public cs2-scripts
mirror), RuneLite `VarbitID`, and the OSRS Wiki Bank page (CC BY-NC-SA 3.0).

## Container layout

- The bank is ONE flat item container (capacity 1220).
- Tab membership is defined by per-tab item COUNTS in varbits:
  `BANK_TAB_1..BANK_TAB_9` = varbits 4171..4179.
- Container order is **tab 1 first, then tab 2 ... tab 9, then the main
  (untabbed) tab as the remainder**:
  - tab 1 = slots `[0, count1)`
  - tab k = slots `[sum(count1..k-1), sum(count1..k))`
  - main tab = slots `[sum(all counts), 1220)`
  (verbatim from clientscript `bank_gettabrange`.)
- The **visual** All-items view draws the MAIN tab first, then tabs 1..9
  with separator lines (`bankmain_build` draws the untabbed range before
  tab ranges). Visual order is therefore NOT container order once tabs
  exist; any widget-to-container mapping must go through child indices,
  never through on-screen position.
- `BANK_CURRENTTAB` (varbit 4150): 0 = All items view.
- `BANK_INSERTMODE` (varbit 3959): 0 = Swap, 1 = Insert.

## Player actions and their exact effects

| Action | Effect on container | Effect on tab counts |
|---|---|---|
| Swap drag (item A onto item B) | positions of A and B exchange | none — items crossing a boundary change tab membership |
| Insert drag | item removed at source, inserted at target; range between shifts by one | none for same-tab moves; crossing boundaries shifts membership by position |
| Drag item onto an existing tab icon | item removed, appended at the END of that tab | that tab +1, source tab -1 |
| Drag item onto the "+" icon | new tab created holding that item | new tab count = 1, source tab -1 |
| Collapse tab (right-click tab icon) | all its items move back to the main tab | that tab 0; higher tabs renumber down |
| Emptying a tab (withdraw/move all) | — | tab auto-removes, higher tabs renumber |

Notes:
- A tab's icon is its first item.
- Placeholders occupy container slots like items; some items never leave
  placeholders (clue scrolls etc.).
- Bank fillers occupy slots and block a clean mapping — guidance stays
  fail-closed when fillers are present.

## Blueprint mapping

The Ironman preset has 10 categories; the bank has 9 tabs + the main tab.
Mapping: category 1..9 -> bank tab 1..9, category 10 (Storage & Cleanup
Review) -> main tab. Because the container order is tabs-first-then-main,
the blueprint's concatenated category order IS the target container order.

## Route algorithm (tab-aware guidance V2)

Target state: container order == blueprint order AND tab counts == category
sizes. Every advised action is one of the verified player actions above;
the plugin never automates anything.

Phase 0 — Preconditions (fail-closed, as today): vanilla All-items view,
Swap mode, no fillers/duplicates/stale scan.

Phase 1 — Tab skeleton: while fewer than 9 tabs exist, advise dragging the
planned FIRST item of the next missing category onto the "+" icon. This
simultaneously creates the tab and places that category's anchor item.
Existing tabs are reused, never collapsed unless their target count is 0.

Phase 2 — Append pass (the workhorse): for each category k in blueprint
order, advise dragging its next planned item onto tab k's icon. Each drag
is simultaneously a count fix AND an order fix (append = next planned
position). Skip items already inside their target tab. Arrow points from
the item to the tab icon.

Phase 3 — Interior swaps: items that were already in their target tab may
be internally misordered; finish with the existing shortest-distance swap
guidance, now scoped per tab section.

Cost: every item outside its target tab costs exactly one drag (phase 2);
items inside their tab cost at most one swap (phase 3). This is within a
factor ~2 of the theoretical minimum number of manual drags, and every
individual instruction is trivially executable (drag to a tab icon that is
always on screen, or a short in-tab swap).

Progress definition ("complete"): all tab counts match category sizes AND
container order matches the blueprint.

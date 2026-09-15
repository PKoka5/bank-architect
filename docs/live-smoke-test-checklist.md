# Live smoke-test checklist (Phase 6A)

This checklist implements Phase 6A of `remaining-work-roadmap.md`. It is executed manually by the
maintainer on a real bank with the current build. The plugin never performs any input; every drag
and click below is a manual player action. Record the session in the results template at the
bottom, in the style of the probe record in `bank-tab-mechanics.md`.

**Status: draft — pending maintainer approval.** Run only after approval, on a bank arrangement
you are willing to reorganize (or after noting your current arrangement).

## Preconditions

### September 15 reliability update — live verification still pending

- [ ] With Guide on bank open enabled, deposit a new item, change an existing
      stack, withdraw to a placeholder and release a placeholder. Confirm analysis
      refreshes and the next advice matches the new bank. Repeat with rapid changes.
- [ ] With that setting disabled, deposit a new item during a pinned move. Expect
      a request to analyze again, not an instruction to undo a drag. Remove the
      deposited item and check the original advice can resume.
- [ ] Rearrange items without changing contents in Swap and Insert modes: analysis
      should not restart for each drag. A wrong structural move still pauses safely.
- [ ] Keep a dragon pickaxe/axe placeholder alongside a lower-tier tool. The better
      tool's placeholder remains in Frequently Used; returning the item keeps its
      position. Release the placeholder and check the fallback. Check gathering
      disabled and manual category assignments as well.
- [ ] Keep two used Blue Moon sets. Preview and guidance retain every physical
      copy. Depositing another copy requests reanalysis, not removal of real gear.
- [ ] Fillers at the beginning, middle and end produce BANK FILLERS FOUND with a
      count and manual recovery guidance, rather than indefinite SYNCING BANK.
      Clear them manually and confirm guidance can resume after analysis.

### September 15 placement update — check after the implementation sequence

- [ ] Assign platinum tokens to Frequently Used with Currency and Teleports on
      the same tab. Check the tooltip, tag count and ordering in Grid and List.
      Move Frequently Used to another tab and verify the tokens follow it.
- [ ] Restart with the saved correction; then use automatic classification on
      the tokens and confirm they return to Currency. Export the blueprint and
      verify `layoutTag` distinguishes the chosen tag from catalogue metadata.
- [ ] Assign mystic hat and black wizard hat to Combat Gear. Check that mystic
      leads the weaker hat, including the main mage row when the bank has enough
      other gear to build that row.
- [ ] Check Crystal, Blood/Blue/Eclipse Moon sets in Best in slot, Sets together
      and List. Record the layout mode and export for any remaining split; the
      best-in-slot matrix intentionally differs from set grouping.
- [ ] Assign the reported clue platebodies to Clues and confirm the override
      survives analysis without changing other useful combat gear.

### General preconditions

- [ ] Build from the current main commit; `./gradlew test`, the fixed 50-bank simulation
      (150/150 COMPLETED), and `./gradlew build` all pass locally.
- [ ] RuneLite client with the plugin installed from that local build.
- [ ] Bank requirements: vanilla All-items view, no search filter and no active bank tag; the
      overlay must itself confirm these gates before showing guidance. Either rearrange mode
      works — run the sorting phase once in Swap and once in Insert mode and confirm the
      instruction wording and the minimum-drag count change accordingly.
- [ ] Note bank size (item count), current tab count, and rough current organization.
- [ ] Active preset: `Ironman — All-Round Bank` (the only released preset).
- [ ] Destination colours: after a scan, confirm every bank item is tinted with its planned tab
      colour and that the legend sits beside the bank without covering slots. Check it in the All
      items view, inside a tab, with a bank-tag filter active, and while scrolled. Confirm the
      config toggle hides it and that the opacity setting takes effect.
- [ ] Category corrections: with **Assign Categories** on, right-click a bank item, confirm the
      **Bank Architect** submenu lists the ten destinations, pick one, re-analyze and confirm the
      item moved tab in the blueprint. Confirm the correction survives a client restart, that
      **Use automatic classification** clears one item, and that **Reset Corrections** clears all.
      With assign mode off, confirm the bank right-click menu is untouched.

## Session steps

1. **Scan and plan.** Open the bank, run the analysis from the sidebar. Record: generated plan
   summary (tabs, category counts, unknown/review count) and that the plan is dense with no
   invented cells.
2. **Structural phases.** Follow the guidance through recovery/collapse/create/distribute as
   offered. Record each move type the guide requests, and confirm after every manual drag that the
   guide advances only after the bank state matches (no premature arrows, no stale arrows).
3. **Deliberate deviation.** At least once, perform a different-but-safe manual move than advised.
   Confirm the session either accepts it as a proven safe drag or pauses without arrows and
   recovers on a later tick; `MANUAL_RECOVERY_REQUIRED` only when structural collapse would be the
   sole continuation.
4. **Sorting phase.** Follow the anchor-walk swaps in at least one section to completion. Record
   the shown SWAPS LEFT estimate at sort start and confirm it decreases after each swap.
5. **Geometry checks.** While guidance is visible: scroll the item grid, resize the client height,
   close and reopen the bank, and switch between a numbered tab and the All view. Confirm the
   overlay recomputes geometry, keeps arrows only when validated, and renders nothing when safe
   geometry is unavailable.
6. **Gate checks.** Temporarily enable Insert mode, then a bank search, then an active tag (if
   used). Confirm guidance fails closed each time (MECHANICS_MISMATCH or equivalent neutral HUD)
   and resumes when the supported state returns.
7. **Completion.** Continue until the plan reports COMPLETE, or stop and record every reproducible
   blocker verbatim (status, HUD text, bank state).
8. **Read-only confirmation.** Throughout: confirm no click, drag, keypress, or bank/native-widget
   mutation originates from the plugin, and no network access is observed.

## Results template

```
Date / build commit:
Bank size / tabs before:
Plan summary (tabs, categories, unknown count):
Phase transitions observed (RECOVERING/REPAIRING/CREATING/DISTRIBUTING/SORTING/COMPLETE):
Deviation test result:
SWAPS LEFT at sort start → swaps performed:
Geometry checks (scroll / resize / reopen / tab switch):
Gate checks (insert / search / tag):
Result: COMPLETE | stopped (reason)
Blockers found (verbatim status + reproduction):
Classification observations (items routed somewhere surprising):
Read-only confirmation: yes/no
```

A failed step is recorded, fixed, and the affected steps re-run before the session counts as the
Phase 6A record. Classification observations are input for later curation; they do not fail the
smoke test unless a plan is not dense/valid.
# September 15: blueprint editor (pending live checks)

- Open the sidebar with no acknowledged release: What's new appears. Close and
  reopen without dismissing: it remains unread. Click Got it - continue: normal
  controls appear. Reopen/restart: the same release notice stays dismissed.

Maintainer confirmed that the click-selection and Swap/Insert editor interaction
works well in game. Other checks below remain pending unless separately confirmed.

- Click an item: a green selection border appears. Click it again to deselect.
  Choose Swap and click another item: their positions exchange. Choose Insert:
  the selected item ends at the clicked slot, shifting intervening items. Test
  left/right moves, Undo, Cancel and Save. Tab changes clear the selection.

- Open Show My Bank, Edit this tab, put coins first and platinum tokens second.
  Check insertion before/after, a second row, scrolling, Undo and Cancel.
- Save, analyze again and restart RuneLite: order survives; export and guide agree.
- Drop one duplicate item onto another tab header. Choose a tag if prompted, then
  reorder in that tab. Verify only one physical copy moves in the proposal.
- Undo a cross-tab move and Cancel a draft spanning several tabs; nothing persists.
- Save a cross-tab move, switch profiles and return. Reset the destination tab;
  incoming moves are released and bank-menu category corrections remain intact.
- Withdraw/deposit during Edit or change profile/layout; stale Save is rejected.
- Remove an item completely and return it later; saved order resumes. New items append.
- Move a destination tag to another tab: old individual destination becomes inactive.
- Check dark-theme contrast, mouse insertion markers and multi-tag selection in RuneLite.
- Confirm all real bank moves still require the player's own manual actions.

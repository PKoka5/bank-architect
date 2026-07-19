# Live smoke-test checklist (Phase 6A)

This checklist implements Phase 6A of `remaining-work-roadmap.md`. It is executed manually by the
maintainer on a real bank with the current build. The plugin never performs any input; every drag
and click below is a manual player action. Record the session in the results template at the
bottom, in the style of the probe record in `bank-tab-mechanics.md`.

**Status: draft — pending maintainer approval.** Run only after approval, on a bank arrangement
you are willing to reorganize (or after noting your current arrangement).

## Preconditions

- [ ] Build from the current main commit; `./gradlew test`, the fixed 50-bank simulation
      (150/150 COMPLETED), and `./gradlew build` all pass locally.
- [ ] RuneLite client with the plugin installed from that local build.
- [ ] Bank requirements: vanilla All-items view, Swap mode (not Insert), no search filter and no
      active bank tag; the overlay must itself confirm these gates before showing guidance.
- [ ] Note bank size (item count), current tab count, and rough current organization.
- [ ] Active preset: `Ironman — All-Round Bank` (the only released preset).

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
   the shown MIN SWAPS at sort start and confirm the count decreases monotonically per swap.
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
MIN SWAPS at sort start → swaps performed:
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

# Real-bank blueprint review

Date: 2026-07-19

## Source and context

The maintainer generated a blueprint export ("Copy Blueprint Export") of a real 770-item Ironman
bank against the active `Ironman - All-Round Bank` preset at commit 280b1f1 and reviewed the full
ten-tab result. This is the first classification review against a real player bank rather than
protocol-v1 random sampling, and it is complementary to that benchmark: a real bank is a targeted
~770-ID sample that surfaces low-frequency IDs the top-250 aggregate review never ranked.

## Overall result

- 770 items placed across ten tabs; the plan is dense with no invented cells.
- Only 18 items (2.3%) routed to Storage & Cleanup, all verifiably quest/junk records.
- Confirmed live: charge runs from Phase 3A slice 1 (dueling, games, burning, passage, digsite via
  the legacy table), the achievement-diary matrix, the Graceful column, herb recipe rows
  (grimy → clean → seed → unf → secondary → dose 3/2/1), ore→bar and uncut→cut column alignment,
  clue scroll + scroll box interleave, and the pass-2 live-cat decision (Hell cat in
  Clues & Cosmetics).
- The "communiqué" mojibake in the maintainer's saved export file is a file-save encoding artifact
  (ANSI save of clipboard text); the registry and the clipboard export are correct UTF-8/Java
  strings. No plugin change needed.

## Findings

### Fixes (curation round input)

1. **M'speak amulet (4021) and Seal of passage (9083)** route to CLEANUP while their peers
   (greegrees, Catspeak amulet(e), Gas mask) are TOOL / quest-utility. Both are repeatable-use
   quest utilities and should join the quest-utility family in Skilling Tools.
2. **Gas mask (1506)** is classified TOOL / quest-utility but lands in Clues & Cosmetics instead of
   Skilling Tools with the other quest utilities. Inspect the routing rule; one of the two
   (classification or placement) is inconsistent.
3. **Orange spice (7484)** is POTION / potion in Potions & Food; it is a cooking ingredient and
   belongs with cooking materials in Resources (complete spice-dose family, exact IDs).

### Policies to confirm and document as deliberate (not code changes)

4. **Minigame-currency split**: Hallowed mark and Mark of grace sit in Main while Tokkul, Stardust,
   Numulite, Golden nugget, Frog token, and Trading sticks sit in Clues & Cosmetics. Maintainer
   direction: formalize the current split as an explicit documented rule — actively-spent
   progression currencies in Main, collection/minigame currencies in Clues & Cosmetics — so the
   distinction is a policy, not an accident.
5. **Tome split**: Tome of fire (charged) in Combat Gear; Tome of fire (empty) with its pages in
   Slayer & Boss Loot. Confirm this is the intended pattern (empty book lives with its charge
   material) and document it for all tome families.
6. **Alch/stock gear** (e.g. Mithril platebody ×820, Rune platebody, Adamant 2h) routes to
   Slayer & Boss Loot. Confirm this is the deliberate alch-candidate rule and document its
   boundary.

## Structural follow-up

Add the 770 item IDs of this export as a checked-in **real-bank regression fixture**: a test that
classifies every fixture ID and asserts its expected tab, so future classification changes are
automatically validated against a real bank distribution, not only uniform random sampling. The
fixture stores item IDs and expected destinations only (no quantities), and it must be updated
deliberately alongside any intended routing change.

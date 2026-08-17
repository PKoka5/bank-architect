# OSRS bank tab mechanics and safe bucket route

Research/design contract for tab-aware manual guidance. Runtime code stays
read-only: it may inspect bank state and draw instructions, but the player
performs every collapse, drag and swap.

Sources checked in July 2026:

- current public RuneLite client scripts:
  <https://github.com/runelite/cs2-scripts/tree/master/scripts>
- RuneLite `InterfaceID`, `InventoryID`, `VarClientID` and `VarbitID` from the
  current API;
- OSRS Wiki Bank page: <https://oldschool.runescape.wiki/w/Bank>.

## Container, All view and blueprint numbering

- The bank is one flat item container with 1410 possible slots.
- `BANK_TAB_1..BANK_TAB_9` store the nine physical numbered-tab counts.
- Flat container order is physical tabs first, then untabbed/main:
  - physical tab 1 = `[0, count1)`;
  - physical tab k starts at the sum of all earlier physical counts;
  - main starts at `sum(count1..count9)`.
- The vanilla infinity icon opens **All items**. It is not a main-only tab.
- All items renders the untabbed/main range first, then physical tabs 1..9.
- Dynamic item child indices still identify flat container slots and are
  validated against `InventoryID.BANK`; screen order must not be inferred from
  flat slot numbers across section boundaries.
- `BANK_CURRENTTAB == 0` is the vanilla All-items view.
- `BANK_INSERTMODE == 0` is Swap mode; any other value is Insert mode.

## Rearrange modes

Both modes are supported. They differ only in how a same-section reorder
transforms the flat item container:

- **Swap** exchanges the dragged item with the drop slot occupant. Sorting a
  section of `n` items needs exactly `n - permutation cycles` drags.
- **Insert** removes the dragged item and reinserts it at the drop slot index,
  shifting every slot in between by one. Sorting needs exactly
  `n - longest increasing subsequence` drags.

Insert is the cheaper mode on a shuffled section, because the cycle count grows
like `ln n` while the longest increasing subsequence grows like `2*sqrt(n)`.

Two consequences shape the guidance:

- A planned drop **slot index** is invalid the moment an insert drag starts,
  since everything between source and drop shifts. Insert steps are therefore
  anchored to the item that currently occupies the drop slot.
- Dropping on a slot to the *right* of the source lands the item *behind* that
  slot's occupant, because removing the source shifts the section left first.
  The planner aims one slot earlier in that direction.

An insert whose source and drop slot lie in the same section never changes any
tab count, so section boundaries survive. Cross-section inserts are never
advised and are rejected by transition verification.

The product's blueprint numbering intentionally follows the visual/player
model:

- **Blueprint tab 1 = Main/untabbed**. It already exists and is never created.
- **Blueprint tabs 2..10 = physical tab candidates** backed by
  `BANK_TAB_1..BANK_TAB_9`.
- Empty candidates among blueprint tabs 2..10 are omitted because OSRS cannot
  retain an empty physical tab. Remaining candidates map densely in blueprint
  order while retaining their original blueprint number, key and name.

Target flat container order is therefore the non-empty blueprint tabs 2..10,
followed by blueprint tab 1/main. The blueprint dialog may continue to display
the player-facing order: Main/tab 1 first, then tabs 2..10.

## Supported manual actions

| Action | Contract |
|---|---|
| Collapse highest physical tab | Lower physical tab counts/content stay intact; collapsed items return to main in server-defined order. |
| Drag main item to `+` | Creates the next dense physical tab containing that one anchor. |
| Drag main item to an existing tab icon | Target count rises by one and the item becomes a member of that target tab. Exact landing position is deliberately irrelevant. |
| Drag misplaced numbered-tab item to its correct existing tab | Source count falls by one, target count rises by one and only that item changes bucket. Source must retain at least one item. |
| Drag misplaced numbered-tab item to infinity/All | Intended recovery to Main/untabbed; source must retain at least one item. The server result remains a required live probe. |
| Same-section Swap | Two item positions inside one physical tab or inside main exchange; counts do not change. |
| Cross-section item-grid drag | Unsupported by `bankmain_reorder`; guidance never suggests it. |

## Safe, scroll-efficient bucket route

The invariant is a **clean bucket skeleton**. Every existing physical tab k:

- maps to dense target bucket k;
- has a positive count no larger than that target bucket;
- contains only item IDs belonging to that target bucket.

Internal order does not matter during bucket construction.

### Phase 0 - fail closed

- Require vanilla All items and no active search/tag filter. Either rearrange
  mode is accepted; the sorting phase plans for whichever one is active.
- Require exactly nine contiguous tab-count inputs.
- Require dense, unique canonical bank IDs with no fillers or true gaps.
- Require the live and planned item sets to match exactly.

### Phase 1 - recover mistakes, then repair structure

- If a tab with at least two items contains a foreign item and its correct
  physical destination already exists, guide that one item directly there.
- If that foreign item belongs to Main, guide it to the infinity/All target.
- These local moves reduce the foreign-item count without deleting or
  renumbering any tab.
- Only structural states that cannot be repaired locally may use highest-tab
  collapse during an explicit analysis route.
- After an unexpected in-session move, destructive collapse is never accepted
  as automatic recovery. Guidance pauses when no local recovery is available.
- Reassess after every collapse. Highest-first prevents lower numbering from
  changing and eventually leaves a clean leading skeleton or zero tabs.

### Phase 2 - create missing buckets in target order

- Create missing non-empty blueprint tabs in blueprint order.
- For the next required target, choose its first planned item from Main and
  guide it to `+`, so the singleton anchor already occupies its final slot.
- The anchor comes only from main, so no existing tab can disappear.

### Phase 3 - distribute for append-ready order

- Once every target bucket exists, prefer the item planned for the target's
  current count/index. The verified live mechanic appends it to that exact
  next slot, avoiding a later local swap.
- If that item is already misplaced inside the partial target bucket, follow
  the prefix permutation backwards and append the still-unplaced item that
  closes that open path into a cycle. This maximizes final permutation cycles
  and therefore minimizes later swaps (`n - cycles`) without search.
- If the selected item is unexpectedly unavailable, fall back to the first
  eligible Main item. Correctness never depends on append order: the live
  transition check accepts membership and the local cycle planner repairs any
  remaining permutation.
- Validate section membership and counts after every drag. Do not assume the
  server placed the item at the start or end.

### Phase 4 - sort locally in visual order

- When section memberships/counts are complete, sort main/blueprint tab 1
  first.
- Then sort physical buckets in visual order (blueprint tabs 2..10).
- For each section, anchor at the first mismatched slot and walk its cycle:
  each advised swap drags the anchor's occupant to that item's final slot, so
  the displaced item lands back on the anchor. The player keeps picking up
  from one unchanged slot until the cycle closes, then the anchor advances to
  the next mismatched slot. This reaches the exact lower bound
  `n - permutation cycles` without hopping between interleaved cycles.

### Completion and termination

Complete requires exact physical counts, exact membership and exact internal
order for every physical bucket and main.

The route terminates because each action decreases a finite measure:

1. local recovery decreases the number of foreign bucket members;
2. structural repair decreases the number of existing dirty/surplus tabs;
3. creation decreases the number of missing target buckets;
4. distribution decreases the number of physical-target items left in main;
5. every local swap fixes at least one previously incorrect slot.

## Transition acknowledgement

- Collapse: same unique item set; lower counts/content preserved; target and
  higher counts become zero; main landing order ignored.
- Create: earlier buckets preserved; new count is one; anchor is the new
  bucket's sole member; main loses only the anchor.
- Distribution: target count rises by one; target membership gains only the
  advised item; other buckets are unchanged; main loses only that item.
- Tab-to-tab recovery: source count falls by one, target count rises by one,
  source loses only the item, target gains only it and all other sets remain.
- Return-to-Main recovery: source count falls by one, source loses only the
  item, Main gains only it and all other tab sets remain.
- Local swap: counts unchanged and the exact two advised slots exchange.

If independently sampled varbits and container IDs briefly disagree, guidance
shows no new action until the same unexpected snapshot remains stable across a
later RuneLite game tick. A different
manual action is reassessed only when exact item-set and tab-count deltas prove
one non-destructive drag; a foreign item then becomes a local recovery step.
An unexpected tab removal or state that would require structural collapse is
paused as `MANUAL_RECOVERY_REQUIRED`.

## Required live mechanics probe before release

**Status: probe executed and passed in-game on 2026-07-13** (append lands at
the end of the target tab as `{A, B, X}`; the checks below behaved as
described). Guidance V2 is cleared for live use.

Use disposable items and record pre/post IDs, section sets and counts:

1. Existing target `{A, B}`, X in main: drag X to the target icon and confirm
   target membership becomes `{A, B, X}`, target count rises by one, main loses
   X and other buckets remain unchanged. Record the actual landing position,
   but do not depend on it.
2. Drag X to `+`: confirm the next dense singleton bucket is created.
3. Collapse the highest tab: confirm lower buckets stay unchanged and the
   collapsed membership returns to main.
4. Perform one Swap inside main and one inside a physical tab; confirm counts
   stay unchanged and only the selected slots exchange.
5. With at least two disposable items in the source tab, move one foreign item
   to another existing tab and confirm exact source/target set and count deltas.
6. With at least two disposable items in the source tab, drop one item on the
   infinity/All target and confirm it leaves that tab and joins Main while all
   other tabs remain unchanged.

Guidance remains a development feature until this probe is recorded.

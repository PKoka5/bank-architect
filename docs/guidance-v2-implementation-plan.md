# Guidance V2 bucket-route implementation record

This document records the implemented tab-aware manual guidance. The player
performs every bank action; the plugin only reads supported RuneLite state,
calculates one recommendation and draws source/target guidance.

## Plan model

`BankTabPlan` is built from the ten-category organization preview:

- category 1 is Main/untabbed;
- non-empty categories 2..10 map densely to physical tabs 1..N;
- physical target order is categories 2..10 followed by category 1/main;
- original blueprint category number/key/name remain attached to every dense
  physical target so empty-category compression never produces a misleading
  label.

## Pure advisor phases

`TabRouteAdvisor.assess(int[], BankTabPlan, int[])` validates unique canonical
IDs and exactly nine contiguous count inputs, then emits one of:

- `COLLAPSE_TAB` for the highest dirty or surplus physical bucket;
- `DRAG_TO_NEW_TAB` for the next required bucket anchor from main;
- `DISTRIBUTE_TO_TAB` for the first top-to-bottom main item assigned to a
  physical bucket;
- `TRANSFER_TO_TAB` for a misplaced item whose correct physical destination
  exists and whose source tab remains non-empty;
- `RETURN_TO_MAIN` for a misplaced Main item using the infinity/All target,
  again only while the source remains non-empty;
- `SWAP_SECTION` for a swap whose endpoints are proven to share one physical
  bucket or main;
- `COMPLETE` or a typed blocked reason.

Progress is phase-aware (`RECOVERING`, `REPAIRING`, `CREATING`, `DISTRIBUTING`,
`SORTING`, `COMPLETE`) so recovery is visibly distinct from structural rebuild.

## Session acknowledgement

`TabRouteAdvisor.Session` pins the logical recommendation while widgets scroll
and verifies the next bank state before advancing:

- collapse accepts server-defined main order but preserves lower buckets;
- create requires the advised singleton bucket;
- distribution validates count and set membership, not landing position;
- tab-to-tab and tab-to-Main recovery validate exact source/destination set and
  count deltas while ignoring landing order;
- section swap requires the exact two-slot exchange.

An unexpected state is held without arrows until the same snapshot survives a
later RuneLite game tick, then reassessed through the non-destructive route. A
different manual action is accepted only when its item sets and tab-count
deltas prove one safe drag; a foreign item then becomes a local recovery
action. Half-updated snapshots and unexpected tab removal are rejected. If the
only available continuation would be structural collapse, the session returns
`MANUAL_RECOVERY_REQUIRED`. The pinned state
survives bank close/open and guide/highlight toggles; a new analysis plan is the
deliberate reset boundary.

## RuneLite inputs and fail-closed gates

The overlay reads only:

- `InventoryID.BANK`;
- `BANK_CURRENTTAB`, `BANK_INSERTMODE`, `BANK_TAB_1..BANK_TAB_9`;
- search state from `VarClientID.MESLAYERMODE` and `MESLAYERINPUT`;
- native tag state from `VarClientID.BANKTAGS_ACTIVE_TAG`;
- `InterfaceID.Bankmain.ITEMS`, `ITEMS_CONTAINER` and `TABS`.

It requires vanilla All items, Swap mode, no search/tag filter, complete
widget-to-container ID mapping and safe visible geometry.

## Tab targets and overlay rendering

Current scripts expose the infinity/All target at child 10 and action-bearing
numbered tab children at `10 + physicalTab`:

- infinity/All target: `View all items` action;
- existing target: `View tab` and `Collapse tab` actions;
- next dense plus target: `New tab` action.

The resolver validates the requested child, bounds and action every render.
Blueprint numbering is displayed separately: Main is tab 1; a dense physical
tab retains its original blueprint number among 2..10.

Rendering is split into:

- clipped item-grid graphics for validation and same-section swap endpoints;
- unclipped connectors from an item to a validated tab target;
- a compact large-font HUD beside the bank when space permits, with a
  collision-avoiding in-grid fallback, separate from the longer sidebar
  explanation.

During repair/create/distribution the final red/orange validation grid is
suppressed. It returns during local sorting and completion.

## Regression coverage

- category 1 main mapping and category 2..10 dense compression;
- wrong singleton and dirty-highest collapse cases;
- localized foreign-item recovery to numbered tabs and Main;
- unordered but membership-clean buckets are reused;
- anchors created in target order;
- main distribution follows current top-to-bottom order;
- arbitrary target landing positions accepted by membership validation;
- main sorted before physical buckets;
- all swaps remain inside one section;
- empty main/physical categories, malformed counts, duplicates and gaps;
- transition acknowledgement for distribution, recovery, create, collapse and
  same-section swaps;
- rejection of unexpected collapse and counts-first half-snapshots;
- one-game-tick quiet latch before unexpected snapshots become actionable;
- plan-boundary/session invalidation;
- exhaustive small-state permutation/count-partition termination with
  deliberately prepended tab drops.

## Release gate

Before publication, perform and record the disposable-item membership probes
listed in `docs/bank-tab-mechanics.md`. In particular, an existing-tab drop
must prove target membership/count changes; its exact landing position is an
observation, not an algorithm dependency.

# Resource-zone grid-offset design

Date: 2026-07-14

## Goal

Allow a contiguous resource skill zone to be planned independently while retaining its real
position in the bank's eight-column grid. A zone may begin after another dense zone at any physical
column from 0 through 7. Local layout target indices remain dense and zero-based; geometry uses the
zone's physical start column.

This enables hard-contiguous skill zones without blank separators, invented items, copied template
layouts, or rule-key ordering tricks.

## Coordinate contract

Add an immutable `gridStartColumn` fact to `LayoutRequest`:

- valid range: 0 through 7;
- existing constructors default to 0 and retain byte-for-byte behavior;
- the fact is placement context, not a dense rank or source bank slot;
- a copy method such as `withGridStartColumn(int)` is preferred over changing every rule-set API.

For a request-local dense target `t`:

`physicalTarget(t) = gridStartColumn + t`

For a physical cell belonging to the request window:

`localTarget(p) = p - gridStartColumn`

The physical window is the half-open interval:

`[gridStartColumn, gridStartColumn + request.size())`

Physical row and column are always derived from a physical target:

`row = physicalTarget / 8`

`column = physicalTarget % 8`

`LayoutPlacement.targetIndex`, locked targets, the optional dense current order, movement, and the
fallback vector remain request-local. `PlacedBlock` start row and column remain physical grid facts
relative to the first physical row containing the request window.

## Bounded packer changes

The packer must centralize conversions in its request `Context`; scattered `+ offset` arithmetic is
not acceptable. Suggested context helpers:

- `physicalTarget(int localTarget)`;
- `localTarget(int physicalTarget)` with a window check;
- `containsPhysicalTarget(long physicalTarget)`;
- `physicalTarget(int row, int column)`.

Required changes:

1. `rowCount` becomes `ceil((gridStartColumn + size) / 8)`.
2. Initial occupancy masks mark columns before `gridStartColumn` in the first row unavailable.
3. Initial masks also mark columns at or beyond `gridStartColumn + size` in the final row
   unavailable. When the window occupies one row, both masks are combined.
4. Candidate origins and nominal rectangles use physical coordinates and must keep the complete
   nominal rectangle inside the physical window, including slack cells.
5. Meaningful candidate cells use physical row/column for occupancy masks, then convert to a local
   target before indexing `itemIdAtTarget`, `lockedItemAtTarget`, or final item vectors.
6. Locked targets remain local. Convert a locked target to physical before deriving the implied
   candidate origin. Compare a candidate cell against the local locked target after converting the
   cell back from physical.
7. State placement and pending-child comparison store semantic items in local dense arrays. They
   must never index a local array with a physical target.
8. Footprint, row-start, and semantic-span scores use physical geometry. Any lookup in the final
   dense vector converts physical to local first.
9. Movement scoring stays local and unchanged.
10. Fallback completion stays local and unchanged.

The fallback transition remains valid when a semantic block cannot fit inside a small or ragged
zone window. The planner must not borrow an item from another zone or invent padding to make a block
fit.

## Independent geometry validation

`CompleteLayoutGeometryValidator` must reconstruct block cells in physical coordinates and convert
them to local targets before comparing with semantic and final vectors.

For every nominal row:

- calculate physical first/last targets;
- require the complete nominal row to lie inside the request physical window;
- report both physical and local context in conflicts where useful.

For every meaningful item:

- calculate its physical target;
- reject it if outside the window;
- convert it to the local target;
- compare reconstructed, semantic-state, and final-plan items at that local target.

This validator must not reuse mutable search masks or trust conversions performed by the packer.

## Builder integration

Once the offset engine is proven, the Resources path may restore per-zone planning:

1. partition `LayoutEntry` objects by exactly one `ResourceSkillZone`;
2. iterate zones in enum order;
3. set the zone request's `gridStartColumn` to `planned.size() % 8`;
4. run the existing zone fallback sorter and semantic engine;
5. append the zone-local dense output;
6. never insert separators or blank items.

The concatenated result is then both zone-contiguous and physically valid because each zone was
planned against its actual starting column.

The temporary global Resources engine call must remain until the offset implementation and all
regressions are green.

## Required tests

### Request contract

- existing constructors expose offset 0;
- copying with offsets 0 and 7 succeeds and preserves entries/rules/dense-order facts;
- offsets below 0 and above 7 fail immediately;
- the original request remains unchanged.

### Packer geometry

- offset 2 plus a seven-wide horizontal row uses real same-request fallback items to reach the next
  physical row and never wraps;
- the same semantic row falls back when the request has insufficient real items to reach a valid
  footprint;
- offset 4 plus a five-wide two-row matrix keeps both rows column-aligned;
- offsets 1 through 7 are covered parametrically with at least one feasible and one ragged case;
- a local locked target implies the correct physical origin;
- all returned `LayoutPlacement` targets remain local `0..size-1`;
- dense permutation, quantities, and placeholder state survive.

### Validation

- a forged block whose nominal rectangle touches a column before the window is rejected;
- a forged block whose nominal rectangle exceeds the window tail is rejected;
- a meaningful cell reconstructed at the wrong local target is rejected;
- default-offset golden tests remain unchanged.

### Resources integration

- semantic Mining/Smithing, Woodcutting, Crafting, and Fletching fixtures remain contiguous by
  primary zone;
- a zone beginning at every possible physical start column never produces a wrapping horizontal
  row;
- raw/processed matrices remain vertically aligned after a non-zero zone prefix;
- incomplete zones fall back without phantom members;
- output across every Resources-mode preset is a dense permutation with no blanks;
- non-Resources and generic Skiller categories remain unchanged.

## Non-goals

- no changes to confidence tiers or the eighteen-component score tuple;
- no new rule-key prefixes or lexical ordering hacks;
- no automation, bank actions, mouse input, or game-state manipulation;
- no exact reproduction of any community template;
- no UI separators or synthetic bank entries;
- no Fletching semantic rules until the offset-aware zone seam is proven.

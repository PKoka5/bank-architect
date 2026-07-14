# Semantic Block Layout Engine

Status: implementation specification, 2026-07-14

This document turns the aggregate community-template research into an original Bank Architect
algorithm. It defines behaviour and interfaces; it does not copy any community layout, third-party
code, UI, naming, coordinates, or preset.

## Integration boundary

The active runtime pipeline already performs bank reading, classification, tab routing, manual swap
guidance, sidebar rendering, overlay rendering, and export. The new engine changes only the dense
order inside one planner category:

```text
BankSnapshot
  -> BankOrganizationPreviewBuilder
  -> category routing
  -> SemanticBlockLayoutEngine (one category at a time)
  -> BankCategoryPreview
  -> existing BankTabPlan / TabRouteAdvisor / sidebar / overlay / export
```

The final seam belongs immediately before `BankCategoryPreview` is created. The engine must not use
the historical `blueprint.VisualBlock` model and must not run inside an overlay render.

## Non-negotiable runtime invariants

For an input category with `n` real bank entries, a successful result must:

1. contain exactly `n` entries and the exact same item-ID multiset;
2. use positive, unique canonical item IDs; duplicates are a typed conflict because current guidance
   cannot execute them safely;
3. preserve quantity and real OSRS placeholder state, and reuse the request's canonical immutable
   item metadata rather than accepting caller-supplied replacement names, categories, subcategories,
   or tags;
4. contain no null, Bank Filler, dummy, synthetic family member, or `BankPreviewItem.blank()`;
5. occupy target indices `0..n-1` densely in an eight-column row-major grid;
6. remain inside its category/tab boundary;
7. preserve every hard lock or return a typed conflict without a partial plan;
8. be immutable, deterministic, and independent of map/insertion iteration order, including the
   exact order and contents of typed conflict lists for invalid input;
9. fall back to the existing deterministic dense sorter when a semantic shape is infeasible.

`SlotKind.EMPTY` and `BankPreviewItem.blank()` remain historical/advisory representations. A runtime
reserved cell is possible only when a real entry, including a real OSRS placeholder, occupies it.

## Placement context

Movement scoring and locks require context that `BankPreviewItem` does not currently carry. The
engine request therefore uses a separate placement entry containing:

- the existing `BankPreviewItem`;
- the source flat bank slot;
- an optional proven dense category-local rank;
- an optional category-local locked target index.

The runtime may initially provide an empty lock set, but the pure engine and tests must not invent a
different lock meaning. A lock is a required final target, not a promise that the item never moves
during manual execution. A lock outside `0..n-1`, two items locked to one target, or a requested
empty reservation is a conflict.

Flat source slots do not imply a dense category order: before tab distribution, category items may
be spread across Main and several numbered tabs. Movement metrics are neutral unless the request
provides a proven complete, unique `currentDenseCategoryOrder` over exactly the target item set.
An entry-level dense rank is corroborating context only: it is invalid without that request-wide
order and, when present, must equal the item's index in it. A negative, out-of-range, duplicate, or
otherwise uncorroborated rank is never treated as proof.

## Rule model

A semantic rule contains a stable `ruleKey`, exact item-ID/family selection, confidence tier,
reviewed member order, shape primitive, allowed widths, optional evidence-backed width preference,
and spillover compatibility. Its topology must be explicit rather than inferred from one flat item-ID
list. The minimal logical representation is an ordered vector of semantic atoms. Every atom has a
stable atom/family/row key and an ordered vector of `(memberKey, itemId)` members:

- for horizontal and vertical rules, an atom is one reviewed item family;
- for a stage matrix, atoms are the fixed-order family columns and `memberKey` identifies the
  reviewed stage; only projected atoms with the same ordered stage-key signature may share a matrix;
- for a row-group matrix, each atom is one explicit reviewed row group.

Equivalent immutable types are acceptable, but the model must retain atom keys, stage/member keys,
and row-group boundaries. A derived flat member-ID view may be supplied for overlap validation; it
must not be the sole source of geometry.

V1 rules must select disjoint item sets. Overlap is a typed rule conflict; priority-based ownership
is deliberately deferred. The four V1 primitives are:

- `HORIZONTAL_RUN`: one projected family of `m >= 2` members forms one atomic row. A nominal width
  is valid exactly when `m <= width <= 8`; the run never wraps.
- `VERTICAL_RUN`: one projected family of `m >= 2` members forms `m` one-cell rows. Its meaningful
  width is exactly 1; it may later share the remaining physical columns with compatible blocks.
- `STAGE_MATRIX`: fixed-order families are columns and reviewed stages are rows. For width `w`, the
  fixed family order is split by deterministic next-fit into consecutive chunks of at most `w`
  columns; each chunk emits its stage rows. No family permutations or alternative partitions exist.
- `ROW_GROUP_MATRIX`: each explicit projected row group remains one row and never wraps. Width is
  valid when every row length is at most that width.

Rules use exact metadata/family keys. Display names may remain a stable legacy fallback but are not
evidence for a production block relationship.

## Candidate generation

For each applicable rule and each block group deterministically derived from its ordered atoms:

1. Project the reviewed family onto items actually owned.
2. Never create a cell for an absent family member.
3. Treat a family with at least two present members as a semantic atom.
4. Consider widths 1 through 8, then reject the structurally impossible widths defined above.
5. Reject widths that split an atomic horizontal run, cross a physical row, or break stage/row
   order. Lock compatibility is evaluated later for each concrete 3B placement origin.
6. Permit a left-filled ragged final semantic row.
7. Represent candidate geometry as explicit immutable rows. Each row records its local start offset
   and its contiguous ordered meaningful item IDs; row lengths and boundaries may not be inferred by
   wrapping one flat vector at `width`. Empty geometry is slack, not a dummy item or reserved bank
   cell.
8. Give each candidate a stable identity from rule key, atom/family/row keys, orientation, width,
   every row boundary/start offset, and the complete row-major item-ID vector.

Candidate generation is an internal post-validation phase. The engine first returns the validator's
ordered typed conflicts for malformed input; calling the generator directly with an invalid request
is programmer misuse and fails fast rather than creating a second conflict-reporting path.

Complete families, or incomplete families with the same stage signature, may share a stage matrix.
Different incomplete signatures form a separate compact block or use dense fallback. They never
reserve phantom cells. Every block group uses fixed atom/family order and produces at most one
canonical local shape per width, so V1 has at most eight local shapes per block group before
positioning.

Only two class-wide width preferences currently clear the research threshold:

- gem raw/processed matrices: width 5;
- herb workflow blocks: width 3.

Metal and potion blocks consider widths 1-8 without a forced universal width, while structurally
impossible widths are rejected. A complete four-dose horizontal atom, for example, rejects widths
1-3. Potion dose member order is nevertheless strongly supported as horizontal and descending.

## Lexicographic score

Final plans are compared by an integer tuple, minimized from left to right. Candidate-local scoring
contains only orientation/width evidence and canonical-shape fragmentation; packing-only components
remain zero until a complete placement exists. There is no weighted floating-point total:

```text
(
  highMissedRelations,
  highMissedCompleteness,
  mediumMissedRelations,
  mediumMissedCompleteness,
  lowMissedRelations,
  lowMissedCompleteness,
  orientationEvidenceRegret,
  widthEvidenceRegret,
  semanticFragmentation,
  semanticRowBreaks,
  startColumnDeviation,
  lockedPrefixSpillover,
  spilloverCompatibilityCost,
  spilloverTransitions,
  nominalFootprintSlack,
  semanticSpan,
  unrestrictedSwapLowerBound,
  totalRankDisplacement
)
```

Only atoms with `m >= 2` are eligible. For eligible family `f` with `m` owned members from `M`
reviewed members:

```text
relations(f)    = max(0, m - 1)
completeness(f) = floor(1000 * m / M)
```

Choosing fallback for an eligible atom charges its relations and its completeness value in its
confidence tier. A singleton contributes neither value. This guarantees that movement savings
cannot destroy stronger semantics.

For a width with eligible-template support:

```text
supportRate(w) = floor(1000 * distinctTemplateSupport(w) / eligibleTemplates)
regret(w)      = bestSupportRate - supportRate(w)
```

Width regret becomes active only with at least five templates, three families, 60% eligible support,
and a two-template lead over the runner-up. Otherwise all widths tie on this component.
The width-preference tie rank is dense over distinct template-support counts: rank zero has the
highest support, equal counts share a rank, and the rank is the number of distinct higher support
counts. Inactive width evidence assigns rank zero to every width.

V1 rules prescribe exactly one primitive/orientation, so `orientationEvidenceRegret` is zero. Other
components are defined as follows:

- `semanticFragmentation`: for a canonical local shape, `max(0, subblockCount - 1)`; for a stage
  matrix this is `max(0, ceil(familyCount / width) - 1)`;
- `semanticRowBreaks`: number of atomic horizontal runs crossing a physical row boundary; any value
  above zero is invalid, so valid final plans contain zero;
- `startColumnDeviation`: per multi-row block, sum of the absolute distance of every meaningful row
  start from its modal start column; equal modes choose the smaller column;
- `lockedPrefixSpillover`: number of real non-semantic cells before a meaningful row segment that
  cannot be removed because a locked foreign entry occupies the prefix;
- `spilloverCompatibilityCost`: sum of the compatibility cost for real spillover entries inside a
  block's nominal `width * height` rectangle;
- `spilloverTransitions`: count of adjacent meaningful-to-spillover or spillover-to-meaningful
  boundaries inside nominal block rectangles, scanning each physical row independently;
- `nominalFootprintSlack`: sum of `width * height - meaningfulItemCount` over placed blocks;
- `semanticSpan`: sum of `maxTargetIndex - minTargetIndex + 1` over placed blocks.

Spillover compatibility cost is `0` for an explicitly compatible rule, `1` for the same typed
subcategory, `4` for only the same planner category, and invalid across categories. Compatibility
is directional from the surrounding block rule; another group from that same rule is implicitly
compatible. The typed-subcategory comparison matches any canonical projected item in the block and
never treats `unknown` as typed evidence.

Movement is deliberately late and is calculated only for a proven complete dense category order:

```text
unrestrictedSwapLowerBound = n - cycles(current-to-target permutation)
totalRankDisplacement = sum(abs(currentRank - targetRank))
```

The lower bound allows unrestricted swaps and does not model tab transfers or guidance restrictions.
When the proven order is absent, both movement components are zero for every plan. They are also zero
for partial beam states. This must be enforced by the scoring API: callers may not set either
movement component directly after claiming that proof is absent.

Exact ties are handled after the numeric tuple by a separate complete-plan key. A plan may contain
multiple semantic blocks, so one scalar width, primitive, and origin is insufficient. Store one
immutable placed-block fact per selected candidate, canonically ordered by stable rule key and
atom/family/row-key vector. The plan comparator lexicographically compares the aligned vectors in
this order:

1. stable block identity (`ruleKey` plus atom/family/row keys);
2. width-preference rank;
3. width;
4. orientation/primitive ordinal;
5. start row;
6. start column;
7. explicit candidate-row geometry;
8. the complete final target-order item-ID vector.

Each placed-block width remains in `1..8`; an empty semantic plan uses an empty block-fact vector,
not a fictitious width-zero block. The final item vector is always present, including for pure dense
fallback. Tie-key construction must therefore be able to distinguish a difference in the second or
later block even when every earlier block fact is equal.

## Bounded deterministic packer

The general two-dimensional problem with locks is NP-hard. V1 therefore uses a deterministic beam
search per category. Its public pure seam is
`plan(LayoutRequest, stableFallbackItemIds)`. The fallback vector is mandatory and must be an exact
unique permutation of the request item IDs; request-entry order, source bank slots, and the optional
current dense order are never substituted for the existing micro-sorter order. One facade call is
for exactly one planner category; catalog `ItemCategory` is not used as that boundary.

The search then proceeds as follows:

1. Allocate `ceil(n / 8)` rows represented by eight-bit occupancy masks. Mark tail indices
   `n..rows*8-1` permanently unavailable. Every candidate's complete nominal rectangle, including
   slack, must remain before the tail; only its meaningful cells become occupied.
2. Store locks as `lockedItemAtTarget[index]`, not generic occupancy. A candidate may claim a locked
   cell only for that same item ID.
3. Order block groups by locked projected-member count descending, confidence HIGH through LOW,
   missed completeness descending, stable rule key, then atom-key vector.
4. Enumerate every valid candidate width, start row, and start column.
5. Keep each candidate row as one contiguous meaningful segment.
6. Allow compatible blocks to share a physical row side by side.
7. Offer a dense-fallback transition for every group and retain a separately completed pure-fallback
   incumbent outside beam pruning.
8. Retain the best `K = 128` partial states using the partial lexicographic tuple plus an optimistic
   lower bound. The bound counts only irreversible penalties from closed choices; unresolved rules,
   fragmentation, spillover, footprint, and movement optimistically contribute zero. A committed
   fallback immediately contributes its missed semantic values. Candidate attempts remain
   lightweight pending children; full occupancy and item arrays are copied only for the at-most-K
   children that survive each group layer.
9. Fill free cells with real entries in the existing stable micro-sorter order, never with blanks.
10. Reconstruct every meaningful semantic target independently from the canonical placed-block
    facts, compare that reconstruction with both the search state and final dense plan, and then
    revalidate the exact multiset, density, locks, and row boundaries before returning.

For `b` block groups, `c <= 8` canonical local shapes per block group before positioning,
`R = ceil(n/8)` rows, average block height `h`, and beam width `K = 128`, expected work is:

```text
O(K * b * c * R * 8 * h)
```

The measured largest imported tab has 336 items, or at most 42 physical rows.

Beam truncation makes V1 a deterministic bounded approximation, not a proof of global optimality.
It chooses the best retained complete state or the protected pure-fallback incumbent. Expansion
order is canonical. The deterministic one-million-expansion cap counts each in-grid
candidate-origin feasibility attempt before collision or lock rejection; fallback transitions do
not consume the cap. Reaching the cap closes every unresolved group through fallback and never uses
a wall-clock decision.

For a locked family member at candidate-local cell `(r, c)` and target index `L`, a valid placement
origin is forced by:

```text
baseRow  = floor(L / 8) - r
startCol = (L mod 8) - c
```

Every lock in that candidate must imply the same origin. A locked foreign item on a meaningful cell
invalidates the placement. If no semantic placement works, dense fallback must still respect locks.

## Implementation slices

### 3A1 - Models, validation, and score order

- immutable request, entry, rule, candidate, score, placement, result, and typed-conflict models;
- explicit atom/family/stage/row topology and explicit candidate row boundaries, sufficient for all
  four V1 primitives without phantom cells;
- request/rule validation, including positive unique IDs, disjoint rules, invalid locks, and no
  fillers/blanks;
- exact numeric score comparison followed by the separate deterministic tie comparator;
- a complete-plan tie key containing a canonical vector of placed-block facts, not one block's
  scalar geometry;
- validator tests against hand-built complete plans;
- exact reverse-order regression tests for both successful and conflicted inputs; tests must compare
  conflict lists directly rather than converting them to sets;
- successful plan validation canonicalizes each output placement back to the immutable request item,
  and result-success factories are not a public bypass around validation;
- no change to `PresetItemSorter`, preview, overlay, panel, exporter, or catalog resources.

Only new unregistered pure-core classes and tests are allowed in this slice. No existing production
class may reference them, making the absence of runtime behaviour change mechanically reviewable.

### 3A2 - Horizontal and vertical local shapes

- canonical `HORIZONTAL_RUN` and `VERTICAL_RUN` generation;
- widths 1-8 considered with structural rejection;
- fixed member order, incomplete projections, and no phantom cells.

### 3A3 - Matrix local shapes and evidence

- canonical `STAGE_MATRIX` and `ROW_GROUP_MATRIX` generation;
- fixed family order and deterministic next-fit chunks;
- width-evidence regret and the gem-width-5/herb-width-3 rule facts.

### 3B - Bounded packer

- occupancy masks, lock-origin checks, dense fallback transitions, `K = 128` beam, and real
  spillover filling;
- tests for `3 + 5` side-by-side blocks, locks, ragged rows, deterministic iteration, idempotence,
  expansion caps, and a 336-entry fixture;
- generated output must be accepted by the existing manual guidance path.

### 3C - Resources integration

Status: implemented and covered by category-builder plus manual-guidance regressions.

- integrate at category layout time, not after an already packed specialized layout;
- retain `BankItemSnapshot.slotIndex` in the builder's category-local placement entries; an optional
  dense category order remains separate and is supplied only when actually proven;
- migrate exact metal and gem `WORKFLOW_STAGE` families to `STAGE_MATRIX`;
- raw above processed, gem width 5 preference, no forced metal width;
- incomplete pairs compact or fall back without phantom cells;
- retain the current `ResourceItemSorter` dense order as spillover/fallback;
- existing 359-test baseline plus new engine and integration tests must remain green.

### 3D - Potion dose integration

Status: implemented for the 22 exact canonical four-dose families currently in the local metadata.

- integrate only at `SUPPLIES` category-layout time;
- retain `SupplyItemSorter.sort(...)` as exact dense spillover/fallback;
- use one high-confidence horizontal-run rule with reviewed `(4) → (3) → (2) → (1)` members;
- compact incomplete families without phantom doses and keep singletons in fallback;
- never infer a family from a display-name suffix, category, or item-ID arithmetic;
- do not force width 4: the current cohort supports horizontal order but does not clear the
  universal-width evidence threshold.

Later slices add herb width-3 workflows and finally equipment blocks after typed equipment-family
metadata exists. Runes and charged teleports remain data-gated.

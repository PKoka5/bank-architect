# Endgame Combat layout: structure and quality gates

Approved design and implementation notes, 12 October 2026.

## Updated user contract: full equipment columns

The user's subsequent bank export and clarification supersede the earlier three-row primary overview below. Default Combat now prioritizes **complete vertical Melee, Ranged and Magic equipment columns**, including cape, boots, necklace, weapon, shield, gloves and ring. Shared positive-offence accessories have one physical owner, chosen for Melee first; unused compatible alternatives remain available to the other styles. There are no repeated virtual items.

The whole real equipment grid supplies capacity, not only a small selected front pool. Primary targets occupy columns 0, 1 and 2; actual family remainders fill free cells to the right and below as atomic blocks. Consequently, secondary style ranges need not be strictly contiguous. Full primary columns and family cohesion take priority over that former rule. Small banks unable to contain the last real primary target use complete compact style runs without invented gaps. Explicit physical orders and category corrections continue to win.

This requirement is independently tested by `FullCombatColumnsTest` against the reported 85-item bank. Its Melee column contains Fire cape and shared Avernic treads, Ranged contains the owned Ava's accumulator, and Magic contains the imbued cape. The earlier text is retained as the history of the replaced overview policy.

The [ring/weapon follow-up](../testing/ring-weapon-clusters-2026-10-13.md) keeps those primary targets unchanged. Loose rings, including Lightbearer, share an atomic block near the selected rings. Secondary weapon groups receive the first placement attempt beside the full columns; the bounded second attempt restores canonical family allocation when that reservation cannot fit the remaining armour. Owned equipment families retain their own weapons and rings. Current test counts, reviewed routing differences and the highest token estimate are recorded in that follow-up report.

The later [large-group follow-up](../testing/weapon-groups-and-item-fixes-2026-10-13.md) lets pure weapon groups longer than the available right-hand width wrap into the next row of that region. It retains the compact fallback, existing armour geometry and the bounded search, so large special-attack or bow groups can stay near their other weapons without displacing primary gear.

## Goal

An endgame Combat tab must be understandable from its complete physical
eight-column grid. Preserving item counts and keeping individual sets vertical
does not establish that the whole bank is organized well.

The former planner selected a four-style front and placed secondary families through
free cells across the entire tab. That can preserve every family while mixing
styles. Its primary selection also excluded rings; condition variants could
make one family disproportionately tall.

## Proposed order

1. **General setups and frequently needed support.** Best reviewed owned gear
   for Melee, Ranged and Magic remains recognizable near the front. Prayer has no fixed primary column.
   Rings and shared accessories get deliberate nearby positions, not an
   accidental place in the tail. This is a bank-navigation overview, not a
   target-specific DPS calculation or a fully equippable loadout.
2. **Melee section.** Keep remaining armour sets and useful alternatives in
   explicit Strength, slash, stab, crush, tank and Slayer role blocks. Matching
   weapon switches belong near the relevant role; special-attack weapons have
   their own compact group.
3. **Ranged section.** Keep its armour families and bow/crossbow/rapid-fire/
   specialist weapon groups together. Preserve reviewed weapon/armour synergy
   rather than choosing every slot independently. Cannon components form one compact block per finish in Tools; ammunition remains in Drops.
4. **Magic section.** Keep robe families and powered, Ancient, elemental and
   specialist casting weapons in recognizable groups.
5. **Remaining Prayer/shared/alternate equipment.** Shared high-use pieces
   already placed at the front are not repeated. Lower-priority retained gear
   has a predictable destination and does not fill arbitrary earlier cells.

Role ordering is explicit reviewed metadata. It must not depend on alphabetic
internal rule keys or one score that makes unrelated combat functions equivalent.
Player category corrections and saved physical orders retain priority.

## Plan blocks before cells

Use a small Combat-specific planning layer within the existing gear path:

1. Resolve each bank item's canonical ID, equipment slot, compatible style/role,
   armour family, condition variant and placeholder state from reviewed facts
   and supported bank-item metadata.
2. Select front items and assign each real entry to exactly one physical owner.
   Shared equipment may have multiple useful roles but occupies one bank cell.
3. Build whole family and weapon-role blocks in the reviewed section order.
   A front-selected set member stays at the front; its owned remainder is one
   explicit secondary block.
4. Pack each section independently at its actual physical start column, using
   the existing layout tools. Section ranges must be contiguous. Free space in
   a section is filled with real items assigned to that section, not equipment
   borrowed from a different style.
5. Check the final physical output against the quality rules below. Report
   deliberate compact geometry and metadata fallbacks in development output.

Reuse the repository's current set/role metadata and existing geometry tools.
Replace duplicated Combat placement responsibilities rather than layering
another generic sorter on top of them. Do not copy third-party plugin layouts.

## Family identity is separate from item condition

Model a family in canonical equipment-slot order, with owned condition variants
attached to each slot. Active, inactive, degraded and appearance variants remain
their real IDs. A family with several versions of its body is not a set with
several different wearable body slots.

Prefer a primary vertical set column and adjacent condition variants. Do not
turn every owned Barrows condition into one long purported equipment setup.
Overlapping families, such as shared set components, need one deterministic
physical owner; any other role is a reference, not a fabricated duplicate.

## Dense-bank geometry: explicit priorities

The real bank cannot retain arbitrary blank cells. A five-item tab cannot put
five pieces at indices 0, 8, 16, 24 and 32 without other real items filling the
intervening positions.

Apply these priorities in order:

1. Preserve all actual entries, quantities, placeholders and user instructions.
2. Keep each remaining family and functional weapon group cohesive.
3. Keep automatic section ranges contiguous and in the reviewed order.
4. Prefer vertical canonical set order where physically feasible.
5. Align section boundaries and accessories where actual items permit it.

When a strict vertical shape cannot fit inside its section, choose an explicit
compact canonical group. Do not scatter the family or borrow another style to
manufacture an attractive outline. Avoid layout-mode changes triggered by one
extra item crossing a bank-size threshold.

## Independent release checks

Create development-only quality fixtures with independently reviewed expected
item IDs, styles, roles, families, slot order and front priorities. Do not compute
expected values by calling the same production sorter being tested.

Check the final grid for:

- Exact preservation of every physical input entry, quantity and placeholder.
- Correct independently specified front armour, weapons, rings and accessories.
- Contiguous sections; no unexplained cross-style or cross-role placement.
- Cohesive family remainders in canonical order, with vertical geometry when
  feasible and a documented compact shape otherwise.
- Reviewed family and role progression order inside each section.
- One correctly grouped cannon per owned finish in Tools, without bank-edge wrapping.
- Identical targets after input shuffles and withdrawals leaving placeholders.
- Stable primary choices and relative block order when unrelated loot or
  cosmetics are added. Adding useful gear changes only the affected role unless
  dense geometry requires neighboring blocks to move.
- Preservation of explicit pins and saved custom orders. Automatic quality
  policy must not silently repair or replace a user's chosen arrangement.

Hard failures must not be hidden by an averaged neatness score. Record geometry
fallbacks, block origins, search-cap use and timing separately as diagnostics.

## Scenario matrix and visual report

Use fresh Main and Ironman plans, independently captured/reviewed gear stats and
intentional missing metadata. Include:

- Representative complete endgame banks and both supplied Main cases when full
  exports are available; the current first export is incomplete.
- Strongly unequal style populations, missing Prayer gear and sparse accessories.
- Incomplete sets and sizes around 7/8/9 and 15/16/17 physical cells.
- Shared components, multiple condition/appearance variants and partial cannons.
- Withdrawals, deposits, upgrades, explicit corrections and saved custom order.

For each scenario, render the actual eight-column result into a development
HTML/PNG report with item identities, section boundaries, block members and
quality violations. Show real input items only. Inspect these reports before
accepting layout changes; route-convergence tests alone are insufficient.

## Delivery order

1. Build the independent quality report and record the current planner's gaps.
2. Define the approved section/role policy with visible example grids.
3. Refactor Combat to assign whole blocks to sections before physical placement.
4. Add condition-variant handling and deliberate near-front accessory placement.
5. Run the scenario matrix, existing tests and routing simulations; review any
   changed benchmark results before updating fingerprints.
6. Test the blueprint and manual guide ingame, then prepare a versioned release.

Keep quality reporting in development/test code. The final highest local
review-size estimate is 196,939, leaving 3,061 estimated tokens. Maintain several
thousand tokens of headroom and re-estimate the exact source before publication;
official Hub tokenization and review scope remain uncertain.

Only supported bank reads and local blueprint planning are in scope. Players
continue every real bank action manually. No inventory/equipment reads,
automation, network activity or telemetry are introduced by this design.

## Historical armour-overview implementation, superseded above

The primary pool contains only actual selected gear for three styles, including rings. Head/body/legs use columns 0/1/2 and rows 0/1/2 only when every desired target fits inside that actual pool. Weapons and rings occupy early free support cells, followed by accessories. A small pool stays in compact style runs; no secondary families supply capacity.

Secondary styles are independent contiguous sections at their real starting column. Armour families precede functional weapon groups, with progression scores within each type. Atomic packing reserves whole owned families and prefers vertical canonical columns, then filled compact rectangles, then canonical contiguous runs. Families may fill earlier free cells within their own style when dense geometry prevents strict ordering of all first cells. Canonical role priority determines allocation order rather than promising monotonically increasing first cells.

Condition variants share their equipment-slot row when a complete owned matrix fits; uneven conditions stay in a canonical compact group. Shared IDs appear once. Search uses at most two passes of 10,000 candidate checks per section. If the complete search reaches its cap, a second pass preserves the highest-priority feasible small column and compacts the other families. Canonical dense family order is always the final complete fallback. Explicit category choices and saved physical orders override automatic placement.

The old unused global semantic-set request builder is removed; its tests now exercise the replacement. Reviewed functional weapon roles are protected from generic Alch dominance, and their exact identities can restore Combat classification when runtime equipment metadata is missing. Explicitly curated Alch policy remains authoritative.

When a logical ID represents multiple physical bank entries, its affected front/secondary section uses canonical compact grouping. Other sections calculate their start from the real physical slot count, avoiding shifted vertical targets after expansion. Unequal condition counts likewise use the explicit compact fallback. The old geometry-only validator and constant-zero alignment field have been retired from production; their necessary test assertions remain development-only.

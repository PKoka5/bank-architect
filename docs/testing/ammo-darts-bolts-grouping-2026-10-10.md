# Finished ammunition and Drops grouping — 2026-10-10

Follow-up to `ammo-drops-placement-2026-10-10.md`: the owner also chose
finished bolts and darts for Drops, and requested contiguous ammunition groups.

## Behavior

- Main and Ironman use the same default: arrows (including brutal arrows),
  bolts (including Bolt rack), darts and cannonballs route to `boss-loot`.
  Placeholders keep that destination.
- Effective equipment metadata accepts ammo and weapon roles, including darts
  reported in RuneLite's weapon slot. Finished-name suffixes distinguish
  ammunition from Bolt pouch, tips, shafts, unfinished parts and quest props.
  Barbed bolts (881) receive an exact GEAR/ammo catalog correction: the cached
  official RuneLite stats identify equipment slot 13, not a skilling component.
- Existing upgrade, charge and key sorting is retained. Ammunition sorts in
  contiguous arrows → bolts → darts → cannonballs runs; Alch forms its own run.
  Existing ammunition tier metadata is reused, with Runite normalized to Rune.
  Unlisted tier names retain the existing alphabetical fallback.
- Automatic Alch classification cannot separate expensive outclassed bolts
  from the ammunition run; an explicit player choice of Alch still wins.
- Explicit corrections, valid editor destinations and captured banks retain
  precedence. Legacy dart editor routes can retain the canonical `gear` tag.
  Relocated Ammunition groups retain their existing destination.
- Drops now uses the existing tag, block and item ordering passes. A saved
  block order preserves relative arranged blocks and inserts unarranged blocks
  according to the existing nearest-predecessor rule. No settings/schema or
  runtime capability was added. Every bank move remains manual.

## Verification

- 1,583 JUnit cases pass, with no failures, errors or skips. The expanded
  parameterized ammunition suite contains 32 cases over both presets and
  exercises 159 distinct finished ammunition IDs, with/without equipment
  metadata, poison/enchantment variants, weapon-slot darts, placeholders,
  quantities, manual corrections, editor routes, captures and custom ordering.
  Realistic Onyx-bolt stats (120 ranged strength, 9,000 high-alch value) versus
  stronger Dragon bolts prove the automatic-Alch guard, relocated ammunition
  group protection and precedence of an explicit Alch correction.
- All 14 development previews preserve IDs, quantities and placeholders and
  retain the physical best-gear and vertical secondary-gear checks.
- The latest complete user export contains 825 distinct IDs and 12 placeholders.
  Both presets put all 19 ammunition stacks in one contiguous run: 8 arrows,
  6 bolts, 1 dart and 4 cannonballs. Each kind occupies one run, in that order.
  All 20 Alch items form another run. Ironman's Drops has 60 items and Combat
  has 89; Main's Drops has 59 items and Combat has 96 due to existing preset
  differences. Original Drops held 12 ammunition stacks across 6 separate runs.
  These are cached-data simulations, not screenshots of a running client.
- The default fixed-seed cohort completes all 150 banks. Aggregate cleanup
  completes all 1,800 scenario banks, with no unsupported plans, build errors,
  blocked/stalled guidance or nonterminating runs.
- Final offline `build` passes with all four reviewed simulation fingerprints.
  The JAR's changed production bytecode matches the compiled classes and no
  development runner or tests are bundled. `git diff --check` passes with
  Git's routine LF/CRLF notices; the final build emits no compiler warning.

## Reviewed baseline

Independent comparison against `build/tmp/ammo-darts-bolts-before` finds
58 changed rows among 150, spanning 21 seeds. Only destination and move counters
change (`planTabs`, `totalMoves`, `swaps`, `minSwapsAtSortStart`, `creates`,
`distributes`, `collapses`, `transfers`, `tabReorders`). Scenario identity,
item counts, outcomes, final statuses and errors remain equal. All 149 actual
sorting scenarios retain swaps equal to the computed minimum; the one no-sort
sentinel remains unchanged.

Cleanup reports and aggregate metadata remain byte-identical after canonical
CRLF normalization. The reviewed `report.tsv` fingerprint is:

`06fc26cd98197687b3439f8cb0c9042e7badc6089bce163a557107294ae92a9d`

Moves change from 12,137 to 12,181 and swaps from 5,298 to 5,305. These different
targets do not establish a general sorting-speed improvement.

## Review size

Highest local main-Java estimate: 196,938, leaving 3,062 estimated tokens.
Other estimates: 195,676 / 196,921 / 195,672. Calibration remains the
maintainer's 200,414 count at `def1e856e101ff0e57dd96adccab2cf1d886b076`.
The official tokenizer and scope remain unknown. This estimate does not certify
Plugin Hub eligibility. Outside main Java, this follow-up changes one exact
classification row, tests, README and development-only simulation reporting.

Changes are local and unpublished; an in-game visual check remains.

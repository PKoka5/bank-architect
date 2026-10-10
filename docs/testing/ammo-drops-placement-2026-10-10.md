# Arrows and cannonballs in Drops — 2026-10-10

The owner chose Slayer & Boss Loot as the default home for arrows and
cannonballs in Main and Ironman. Players can assign them to Combat themselves.
This supersedes the previous default ammunition destination for these items.

## Behavior

- The preview routes combat-classified `ammo` with an arrow/cannonball name to
  the existing `boss-loot` tag. The seven reviewed metal brutal-arrow names
  are included despite not containing the word arrow. Placeholders follow the
  same default; quantities do not decide the destination.
- Item catalog category, subcategory, usage tags and stats remain intact.
  Other ammunition, blessings, Bolt pouch, cannon parts, components and
  quest props keep their existing roles.
- Explicit tag/category corrections win. Valid legacy editor routes retain
  their original ammunition tag until the existing router applies their
  destination. New Drops-to-Combat routes also survive serialization/reset.
  A stale noncaptured editor route cannot inherit another item's capture flag.
- Captured physical banks and an explicitly relocated whole Ammunition group
  retain the player's arrangement. Moving Slayer & Boss Loot changes the
  default destination of this subset without introducing a new tag/schema.
- This changes blueprint guidance only. Players perform every bank action.

## Verification

- 1,569 JUnit cases pass, zero failures, errors or skips. Eighteen new
  parameterized cases cover both presets, reviewed real variants, equipment
  stats/no stats, placeholders, quantities, legacy/new editor routes, captures,
  manual corrections and custom destinations. Existing tag-weaving tests pin
  their Bronze arrow explicitly to Ammunition and retain every prior assertion.
- All 14 development Combat previews preserve IDs, quantities and placeholders;
  actual best-gear targets and secondary vertical geometry pass.
  In the latest complete 824-item export, Combat changes from 115 to 103 items
  for Main and 108 to 96 for Ironman. Alch remains 20 for both previews.
  These are cached-data simulations, not running-client screenshots.
- The default 150-bank simulation and the 1,800-bank aggregate both finish
  entirely COMPLETE, without errors, blocked/stalled or nonterminating runs.
  An additional 450-bank run also completed, but is not the baseline cohort.
- Full compilation notes existing unchecked/unsafe operations in
  `IronmanBankArchitectPanel.java`, which this patch does not modify.
- Final offline `build` passes with all four reviewed baseline fingerprints.
  It reuses the just-generated simulation reports rather than repeating those
  runs. The JAR contains the production changes and no tests, research runners
  or conversation HTML. `git diff --check` passes with Git's LF/CRLF notices.

## Reviewed baseline

Independent comparison against `build/tmp/ammo-drops-before` finds 39 changed
rows among 150, spanning 14 seeds. Only destination/move counters change:
`planTabs`, `totalMoves`, `swaps`, `minSwapsAtSortStart`, `collapses`, `creates`,
`distributes`, `transfers` and `returns`. Identity, item counts, outcomes,
final statuses, tab reorder counts and errors stay equal.

All 149 actual sorting runs still match their exact swap minimum; the one
no-sort sentinel remains unchanged. Cleanup, aggregate cleanup and aggregate
metadata are byte-identical after canonical CRLF normalization. Only the
`report.tsv` fingerprint is updated:

`bf0295b3a0647ffb4cedea08f0ad62a793864efcfb159f8e7528f9ffd6e4ed9c`

Moves change from 12,150 to 12,137 and swaps from 5,315 to 5,298. Changed targets
mean these numbers do not establish a general sorting-speed improvement.

## Size and release

Highest local main-Java estimate: 196,692, leaving 3,308 estimated tokens.
Other estimates: 195,409 / 196,671 / 195,409. Calibration remains the maintainer's
200,414 count at `def1e856e101ff0e57dd96adccab2cf1d886b076`. Official Hub scope
and tokenizer remain unknown; these estimates do not certify submission size.
Tests, docs and the reviewed build fingerprint are outside the estimator's
main-Java scope. No runtime resource, dependency or external-call capability
was added by this patch.

Changes are local and unpublished. The final build uses the freshly generated
simulation reports for baseline verification; in-game visual testing remains.

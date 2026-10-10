# Vertical Combat grid — 2026-10-10

The owner reported that the prior local quality patch made the best outfits
and secondary sets horizontal. Its strict same-slot row fillers abandoned
the style columns too readily. Hard style subgrids also left secondary sets
with too little physical height. This correction supersedes those geometry
choices in the October 9 quality report. Classification, scoring and Alch
policies remain unchanged by this correction.

## Current behavior

- Best owned gear occupies Melee, Ranged, Magic and Prayer columns 1–4.
  Equipment rows follow head, body, legs, cape, neck, shield, hands, feet and
  weapon. Globally absent rows are skipped; every reserved target must be
  inside the real equipment count.
- The entire equipment grid shares one semantic packing request. Secondary
  rules contain only non-primary family members. Loose real gear fills the
  space around vertical columns. Style/progression order remains the fallback
  preference instead of a hard boundary that prevents vertical geometry.
- Ammunition remains a separate terminal block and cannot manufacture space
  for a missing wearable. Very small banks retain dense real entries when a
  vertical column cannot physically fit; no blank items are invented.
- Cannon finishes reserve separate complete horizontal row runs outside
  primary targets. Normal and ornamented components cannot wrap across a
  row edge because one combined run is too wide to fit.
- Saved or captured player order still takes precedence. Quantities do not
  affect selection or geometry; existing placeholders retain their positions.

## Owner export simulation

The latest supplied export contains 824 unique complete items, six placeholders
and ten tabs. All entries are preserved in both fresh preset previews using
the same locally cached RuneLite equipment stats and Wiki Alch values as the
October 9 simulation. Existing exported tags are not replayed as manual pins.

| Item | Physical row / column, both presets |
| --- | --- |
| Helm of neitiznot | 1 / 1 |
| Bandos chestplate | 2 / 1 |
| Bandos tassets | 3 / 1 |
| Mystic robe top | 2 / 3 |
| Mystic robe bottom | 3 / 3 |
| Proselyte hauberk | 2 / 4 |
| Proselyte cuisse | 3 / 4 |
| Secondary Mixed hide top / legs / boots | 1, 2, 3 / 8 |
| Secondary Monk robe top / bottom | 2, 3 / 5 |

All four normal cannon components occupy row 1, columns 4–7 in this export.
Preview counts: Main Combat 115 / Alch 20; Ironman Combat 108 / Alch 20.
These are development simulations, not running-client screenshots. The fresh
defaults do not reconstruct saved assignments or ItemManager's variant mapping.

## Verification

- 1,551 JUnit tests pass, zero failures, errors or skips. New regression checks
  use actual eight-column indices, both presets, shuffled source order, best
  gear and secondary columns, cannon shapes, sparse banks and placeholders.
- Withdrawal/return, manual order and INSERT/SWAP completion checks remain.
- All 14 actual preview simulations preserve IDs, quantities and placeholders.
  Every valid reserved primary target is checked against the actual position;
  secondary family checks retain absolute indices rather than compressing the
  primary pieces out of the grid. All secondary geometry checks pass.
- The fixed broad runs complete all 150 random banks and 1,800 aggregate banks,
  with zero unsupported, build-error, blocked, stalled or nonterminating outcomes.
- The final offline `build` passes all tests and all four reviewed baseline
  fingerprints. No compiler warnings were reported.
- The updated conversation raster contains the actual simulation arrays and
  cached official sprites: 538,314 bytes, valid script syntax, unique root/data
  IDs and no missing sprites. It reuses the previously inspected responsive
  renderer. A fresh browser screenshot could not be taken because no browser
  surface is currently enabled; runtime visual QA remains the in-game check.

Independent baseline review against `build/tmp/combat-vertical-before` finds
only eight changed RANDOM_TABS rows. Only moves, swaps and minimum-swap counters
change; item/tab counts, outcomes and every other field stay identical. All
149 actual sorting runs still match their exact swap lower bound, and the
unchanged no-sort sentinel retains -1. Both cleanup reports and aggregate
metadata are byte-identical after canonical CRLF normalization. The reviewed
new report hash is `a84217837d3197acaf01e8ff971958339853ecc865db9f684eeaef41598d5a3f`.

Total moves 12,151 to 12,150; swaps 5,316 to 5,315. Targets changed, so these
figures do not establish a general sorting-speed improvement.

## Token planning and release status

Highest final local main-Java estimate: 196,459, leaving 3,541 estimated tokens.
Other estimates: 195,151 / 196,435 / 195,154. Calibration remains maintainer count
200,414 at `def1e856e101ff0e57dd96adccab2cf1d886b076`. The official Hub scope and
tokenizer remain unknown; this is not submission approval. No production
resource, dependency or external-call capability was added by this correction.
Research runners, cached data and the conversation raster stay outside the JAR.

Changes are local and unpublished. Run an in-game visual check before release.

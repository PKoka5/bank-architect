# Independent Combat quality report

`CombatQualityReportTest` exercises the public preview builder with fresh Main
and Ironman defaults. Its test-only fixture is
`src/test/resources/com/pkoka5/ironmanbankarchitect/organize/combat-quality-facts.tsv`.
The file freezes equipment vectors from the repository's original RuneLite
item-stats snapshot, cached on 9 October 2026, and separately authors expected
ownership, families, canonical slots and primary IDs. It never calls a
production sorter or family helper to calculate expected output.

## Run

With Java 11 configured, run:

```powershell
.\gradlew.bat --offline --console=plain test --tests '*CombatQualityReportTest' --tests '*CombatLargeConditionQualityTest'
```

Every checked scenario writes its actual physical eight-column Combat output to
`build/reports/combat-quality/<preset>-<scenario>.html` and `.png` before the
assertion. The HTML also shows the actual Tools grid and each family's geometry.
Files are development output only; no report, fixture or renderer is bundled in
the plugin. This command performs no game actions and makes no network requests.

## Gates

- Preserve every real input ID, quantity and placeholder, exactly once.
- Every independently reviewed retained gear item stays in Combat by default;
  generic dominance cannot remove its distinct specialist function.
- Independently expected Melee, Ranged and Magic primary gear occupies the
  actual front envelope; no fourth Prayer primary is required.
- Where the full three-armour core fits, its head/body/legs occupy columns
  0/1/2 and rows 0/1/2. Three raid weapons and Ultor occupy the first available
  support cells in that fixture. No secondary item fills the front envelope.
- Remaining Melee, Ranged, Magic and Prayer/shared items occupy ordered,
  contiguous physical style sections. Justiciar, Verac and Dharok own Melee
  sections independently of the prayer bonuses of individual members.
- Each remaining family or functional weapon group has a complete rectangular
  shape or an explicit compact run; its real wearable members retain the frozen
  canonical order. Compact geometry is printed instead of hidden by a score.
- A 33-cell Melee section with complete Oathplate and Inquisitor must preserve
  at least one of them vertically. In the broader fixture, 17 or more real
  Ranged/Magic section cells must preserve its independently reviewed Armadyl/
  Virtus column. An all-horizontal planner fails these gates. The original
  smaller Ranged/Magic sections do not have enough cells for a three-high
  column; their explicit compact fallback is valid.
- Normal and ornamented cannons each occupy one rectangle in Tools, without
  wrapping around the bank edge.
- Dharok head/body degradation variants stay attached to their canonical slot
  within two adjacent rows, instead of becoming a long fake equipment column.
- Input shuffles, withdrawals leaving placeholders and unrelated deposits keep
  the expected Combat targets unchanged.
- Saved physical order and explicit category corrections remain authoritative.
  The manual report is marked policy-exempt: its chosen cells are not repaired
  by automatic quality expectations.

The matrix includes a 102-entry base bank, a 112-entry condition bank, a
110-entry bank with enough real Ranged/Magic cells for vertical columns, unequal
style populations, incomplete families, missing Prayer gear and sparse banks
around 7/8/9 and 15/16/17 cells. It is representative, not an exhaustive set of
every game item. These quality gates supplement conservation/routing simulations;
they do not prove target-specific DPS rankings or replace ingame preview checks.

The broader fixture reuses original frozen inactive Bowfa/Crystal, Shadow and
Eye of ayak vectors and the independently cached Iban's staff (u) vector from
`CombatLayoutQualityRegressionTest`. Its Master wand deliberately has no
equipment vector, exercising the supported catalog/name metadata fallback.

`CombatLargeConditionQualityTest` adds a 238-entry stress bank with all 144 real
Barrows IDs: six families, four canonical slots each, and the base plus five
degradation states per slot. Only exact ID vectors already captured in the
original fixture are reused; missing vectors deliberately use metadata fallback.
No bonus values are invented. Three owned/shuffled/withdrawn runs must preserve
every stack, canonical condition grouping and deterministic targets within a
loose time bound. Their actual grids and local preview timings are written beside
the other reports. This verifies finite complete planning, not live DPS rankings.

A separate case adds physical copies of a primary Torva helm, a placeholder of
that same canonical ID, and a secondary Virtus body. It checks the full physical
multiset rather than assuming unique IDs, counts the expanded primary envelope,
and ensures the unaffected Armadyl column remains vertical at its actual shifted
section origin. The unique-ID report checker is intentionally not used for it.

# Wiki strategy Alch pilot - 22 October 2026

Local development checkpoint above published 0.9.6 source
`d0c4348921d2ca71883f2dd9a5bcadaa4c1c015d`, following the
[selected GitHub fixes](selected-alch-obsidian-2026-10-22.md).

## Scope

The [armour/jewellery audit](../research/wiki-strategy-alch-armour-2026-10-22.md)
and [weapon/staff/tool audit](../research/wiki-strategy-alch-weapons-2026-10-22.md)
account for all **100 existing reviewed Alch candidates**, split 60/40.
This means their current policy was reviewed, not that every Wiki strategy has
been exhaustively imported. The historical July recommendation cache provides
coverage leads; current item/strategy revisions support the focused correction.

Seven ordinary d'hide bodies/chaps now require full equipment-stat dominance.
The compact `requires-dominance` row uses the existing family-table parser.
Higher numerical armour stage alone no longer proves these items obsolete.
Actual full dominators still qualify; known higher-stage dominator placeholders
retain support. Same-stage/unknown-tier placeholders retain the earlier boundary.
No candidate membership, general Rune policy, owner Dragon halberd exception,
specialist weapon guard or unreviewed-stock threshold was changed.

## Verification

- Java 11 offline `build`: **1,936 tests**, no failures, errors or skips.
- **1,950 completed scenario banks**; all four simulation fingerprints unchanged.
- New `WikiStrategyAlchProtectionTest`: 26 parameterized cases across both presets.
  Every protected ID is checked with tradeoffs, full replacements, stock quantities
  25/1/0/25 and withdrawn upgrades. Exact Wiki vectors reproduce Black d'hide vs
  Crystal/unfortified Masori, and blessed/fortified Masori replacement positives.
- Checks cover missing/partial stats, wrong slot/style, unknown namesakes,
  manual Gear/Alch choices, saved destinations, captured physical order, disabled
  gathering, absent Alch values and item/quantity/placeholder conservation.
- Existing Main tests now require a genuinely better synthetic blessed vector;
  the old non-dominating higher-tier expectation was corrected. The frozen
  compatibility test still verifies unchanged 100-ID membership and legacy roles.
- Initial focused failures identified those old assumptions plus a test using
  the global tier catalog rather than the effective Alch override tier. The test
  now checks the same effective tier source as the router.
- Independent read-only review found no blocker in the scoped routing change.
  No new compiler warning was observed; the existing sidebar unchecked-operation
  note remains. `git diff --check` passes.

Logs: `build/tmp/wiki-strategy-alch-build-2026-10-22.log` and
`build/tmp/wiki-strategy-alch-review-size-2026-10-22.log`.

Highest local main-Java review estimate: **197,035**, leaving **2,965 estimated
tokens** below 200,000. Other estimates: 195,008 / 196,688 (cl100k), 195,345
(o200k preserved). Calibration remains official 200,414 at
`def1e856e101ff0e57dd96adccab2cf1d886b076`. This is not an official Hub count.
The seven-ID resource row and development-only tests/docs were inspected separately.

Development JAR at this checkpoint: 1,115,662 bytes, 264 entries, no test,
research, simulation, documentation or tooling entries. SHA-256:
`2dc9bfa24f587b1105c8b08a946d96a1e43bb050c08545fa7ae505497bf7a048`.
It still uses the 0.9.6 filename and differs from the published artifact.

No commit, push, merge or publication was performed. Live bank checks remain.
Contextual Stab/Crush/training and Scurrius crafting questions are documented
follow-ups, not new automatic disposal decisions in this pilot.

The subsequent [Ranged head correction](ranged-head-selection-2026-10-22.md)
passes 1,954 tests and 1,950 scenarios, with highest local token estimate 197,140.
Numbers above describe the earlier d'hide checkpoint.

# Bank Architect 0.7.1

Fixes [issue #40](https://github.com/PKoka5/bank-architect/issues/40).
0.7.0 was already merged in Hub PR #17640, so this is a new patch submission.

## Behavior

- Ironman Main chooses a working Imcando hammer (25644 or off-hand 29775)
  ahead of hammer 2347, and Jeweller's chisel 34024 ahead of chisel 1755.
- A placeholder preserves the choice while its item is out of the bank.
- With both working hammer variants present, 25644 wins deterministically.
  The broken Imcando hammer 25633 is not a quick-access upgrade.
- Ordinary tools remain the fallback; unselected versions stay with Tools.
  Turning off Frequently Used gathering keeps these tools in Tools.
- Saved category/tag assignments remain authoritative. No new setting, bank
  automation, inventory/equipment read or network request is introduced.
- Jeweller's chisel was missing from the bundled item registry and now has
  supplemental TOOL metadata. Its ID/name/Tools role are independently recorded
  in `wiki-item-categories.tsv`; no third-party plugin code or structure is used.
- The version and local release notice advance to 0.7.1. Players who dismissed
  0.7.0 see the patch notice once on opening the sidebar and can dismiss it.

## Validation

- Full offline build: 1,125 tests, no failures/errors/skips.
- All 1,950 simulated bank scenarios complete; all four existing baseline
  hashes pass unchanged.
- Regression tests cover both Imcando variants, placeholders, simultaneous
  variants, ordinary fallback, the broken hammer, Main slot order, manual
  assignments, disabling gathering, and local notice acknowledgement/restart.
- Release notice rendered and visually inspected at 204px sidebar width.
- Existing panel unchecked/unsafe-operations compiler note remains. The first
  new test run failed because its test constructed row-filling preferences
  rather than disabling gathering; the corrected test and full build pass.
- Highest local review-token estimate: **196,973**, headroom **3,027**.
  Calibration: `def1e856e101ff0e57dd96adccab2cf1d886b076`, maintainer count
  **200,414** on 2026-09-30. This is an estimate, not an official Hub count.
- Outside main Java, this patch changes only the version, tests and this release
  document. Production resources and simulation baselines are unchanged.
- Jar SHA-256: `8ed91c347f501124530a6f3daeea94282efee7a45d314c0c8efaae5b21a0d673`.

## Publication

Publish v0.7.1 and open a new Hub PR changing only `plugins/bank-architect` to
the actual source commit. Hub client availability follows review and distribution.
The owner authorized fixing the issue and publishing the patch on 2026-10-02.

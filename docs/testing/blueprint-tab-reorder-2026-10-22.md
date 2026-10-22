# Complete blueprint-tab reordering

Local development work above published 0.9.6 source
`d0c4348921d2ca71883f2dd9a5bcadaa4c1c015d`, checked **22 October 2026**.
Addresses [issue #43](https://github.com/PKoka5/bank-architect/issues/43).

## Behavior

Tab Layout headings have up/down buttons that insert the entire destination
one position earlier or later. Main and empty destinations participate. The
selected destination follows its moved contents. The first Up and last Down
buttons are disabled. A pending save disables repeated requests and stops the
temporary bank preview.

One permutation remaps assigned tag bundles, Keep current order bits, every
saved tab order and every captured/manual per-item destination. Absent item IDs,
duplicate occurrences, original tags and capture flags survive. Other profiles,
item corrections, sorting preferences and tag-keyed block arrangements retain
their existing values. Editing a bundled preset creates a Custom layout.

The model validates lifecycle identity and the original editing context on the
client thread before writing. It computes both remapped structures first,
stores them for the active custom profile, then requests analysis. Stale contexts,
invalid indices, no-op moves and unsupported item-order formats cannot overwrite
settings. This operates on the blueprint; all real bank actions remain manual.

## Ammunition compatibility

The earlier ammunition policy inferred explicit placement from an absolute tab
number. Moving an entire Combat bundle must not turn ordinary Drops ammunition
into Combat gear, or undo an existing deliberate Ammunition destination.

Old plans retain their previous interpretation. Whole-tab moves freeze the
effective choice with `~ammo-drops` or `~ammo-tag` in the serialized plan.
Subsequent real edits to the Ammunition tag restore the usual tag-assignment
policy. Other edits preserve the frozen choice. An inverse move restores the
same layout and routing even when its storage text now includes that marker.
Profile and share-code round-trips preserve the choice.

## Verification

- Java 11 offline `build`: **1,881 tests**, zero failures, errors or skips.
- **1,950 fixed-seed scenario banks** complete. All four existing reviewed
  simulation fingerprints match; no baseline was regenerated.
- Domain tests cover all 100 source/target combinations in both presets,
  Main, empty destinations, inverse moves, current-order bits, four ammunition
  groups, legacy custom placements and saved/captured/dormant orders.
- UI tests cover controls, accessibility, pending and rejected requests, stale
  contexts, selected destination, newer saved-order formats and narrow-sidebar
  button bounds. Persistence tests exercise custom creation, restart, independent
  profiles/presets, captured routes and unchanged preferences.
- The first full run found one test expecting identical serialized text after
  an inverse move. It was corrected to assert actual destination/tag/keep-order
  semantics and ammunition policy; saved item orders still match exactly.
- Independent read-only implementation review found no blocker. A queued
  completion may repaint a detached panel after shutdown; lifecycle checks
  prevent settings writes from an invalidated request.
- Existing sidebar unchecked-operation compiler note remains. `git diff --check`
  passes. No dependency or runtime permission was added.

Highest local main-Java review estimate: **197,092**, leaving **2,908 estimated
tokens** below 200,000. Calibration remains the official 200,414 for
`def1e856e101ff0e57dd96adccab2cf1d886b076`. This is not the official Hub count.
New test files and README edits were inspected separately. Recheck before a
future submission and preserve several thousand tokens of headroom.

The work is local and awaiting an in-game check. Its development JAR still uses
the current 0.9.6 filename; it is distinct from the published 0.9.6 artifact.

The subsequent [combined validation](selected-alch-obsidian-2026-10-22.md) includes
the selected Obsidian/Alch fixes and unused-code cleanup: 1,910 passing tests,
1,950 completed scenario banks, unchanged baselines and a highest local token
estimate of 196,968. The figures above record the earlier tab-only checkpoint.

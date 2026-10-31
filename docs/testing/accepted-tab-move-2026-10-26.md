# Kept blueprint follows whole-tab moves

The owner chose to carry a kept blueprint's saved item order along with a complete
tab move. Individual item corrections and other personal layout changes still
pause the saved target. This is a local follow-up to the
[remaining Claude review work](remaining-review-2026-10-26.md).

## Behavior

`AcceptedBlueprint.withDestinationMoved` remaps a supported snapshot only when it
matches the context immediately before the move. The whole-tab action updates
the plan, explicit item orders and accepted snapshot together on the client
thread. The refreshed context keeps the saved target active, including after a
restart. Existing stale-edit guards and EDT completion callbacks remain in use.

The snapshot remap preserves per-tab item and tag order, including temporarily
absent items and older rules versions. Duplicate item IDs get occurrence keys in
their new natural tab order so the resulting snapshot remains canonical. The
generic explicit-order move retains its existing behavior.

Already paused, future-format and damaged snapshots are preserved verbatim and
are not reactivated. No new snapshot is created for someone without one. A no-op
move leaves the stored record untouched. The rules version stays at 10 because
this is a deliberate manual edit, not a new automatic classification rule.

The blueprint review text and README explain the exception for whole-tab moves.
Bank items continue to be moved manually by the player.

## Verification

- All 100 source/destination pairs, including Main and no-op moves, are checked
  with duplicate IDs, an absent item, an older rules version and inverse moves.
- Lifecycle tests exercise both account presets, restart persistence, paused and
  unsupported records, and a queued move racing a newer stored snapshot.
- Existing tests still require individual corrections, plan edits and saved item
  or block orders to pause the snapshot.
- Offline full build passes **2,417 tests**, zero failures/errors/skips, and
  **1,950 scenario banks**. All four existing simulation references match.
- No compiler warning appears in the build log. Tracked and new-file whitespace
  checks pass. The estimator emits Git line-ending notices, not build failures.

Logs: `build/tmp/accepted-tab-move-focused.log`,
`build/tmp/accepted-tab-move-full.log`, `build/tmp/accepted-tab-move-size.log`.

Highest local main-Java estimate: **197,763**, or **2,237 estimated tokens** below
200,000. Other estimates: 194,760 / 197,413 / 195,105. Calibration remains the
maintainer's 200,414 at `def1e856e101ff0e57dd96adccab2cf1d886b076`. This is not
the official Hub count. Tests, resources and documentation are outside the
estimator's main-Java scope and were inspected separately.

The local development `ironman-bank-architect-0.9.9.jar` has **939,159 bytes**,
SHA-256 `4596aaab439ae6a8334a1456819b4276a2af6ca6151c23d7d905cd00b23ef13f`.
It supersedes the local artifact in the preceding review note; the published
0.9.9 artifact is untouched. No commit, push, release or Hub update was made.

An in-game smoke check remains: keep a blueprint, move a complete tab in the
planner, confirm its saved order stays active, then restart and check again.

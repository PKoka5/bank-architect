# Bank Architect 1.0 — draft

**31 October:** superseded by [the authorized release record](release-1.0.0.md).
The pending-state text below is retained as development history.

Local release proposal, 26 October 2026. **Not published and not release-ready.**
Validation updated 31 October for the locally fixed visage report #50.
The 1.0 marker is provisional until the owner accepts the remaining completion
gates. Runtime notice/version still say 0.9.9; this draft does not dismiss or
replace a published notice. Published source remains
`b3583ca8e48bedb37514e89006cb677e5b7ea75a`.

## Proposed player-facing changes since 0.9.9

- Draconic, Wyvern and Skeletal visage route to Combat as shield components in
  both presets, outside primary equipment and automatic Alch. Real placeholders
  and personal placements retain their supported behavior.
- Main groups every regional diary reward tier and both diary cape variants
  with Teleports, retaining the regional grid. Lower tiers keep their actual
  catalog function. Personal corrections and valid saved placements retain
  their precedence. Ironman keeps its own default arrangement.
- Blueprint and sidebar tab numbering agree. Finished ammunition remains in
  Loot as the independently movable Ammunition group; the equipment tag is
  labeled Ammo Slot Equipment. Legacy ammunition choices remain readable.
- Moving a whole blueprint tab carries its kept order and destinations with
  it, including dormant saved entries. Unsupported or damaged kept targets have
  an explicit recovery control that preserves unrelated profile choices.
- Sidebar/Tab Layout mouse-wheel scrolling works. Settings shown by RuneLite
  stay synchronized with the active preset/profile.
- Reviewed gear variants and incomplete handedness/stat information are handled
  conservatively. Reanalysis preserves eligible session Alch evidence and
  prevents stale queued work from replacing a newer result.

Existing preview, comparison, editor, current-bank capture, Fixed/Flexible Alch,
primary gear and manual guidance remain the product. Standard eight columns,
Main and Ironman are the supported scope. Every real bank action stays manual;
no runtime network call, inventory/equipment read or automation is introduced.

## Validation available now

- **2,618 tests**, including the finite **180-case** domain lifecycle matrix,
  and **1,950 scenario banks** pass. Issue #50 removes Skeletal visage from
  aggregate Cleanup (one ID/twelve occurrences); the two aggregate fingerprints
  were reviewed and updated. Standard reports remain unchanged. The matrix supplements existing plugin-event/persistence tests;
  its frozen Combat subsets are not complete endgame-bank samples.
- **68 overlay images** match before/after the shared endpoint-rendering cleanup.
- The pre-visage source-only clean-build rehearsal passed without old outputs/private research
  caches. Two source locations produce the same JAR bytes with Java 11.0.31+11,
  Gradle 8.7 and cached RuneLite 1.13.4. Dynamic dependency resolution and
  cold-machine setup are outside this offline proof. The final changed source
  still needs its release-commit clean-clone validation.
- Highest local review estimate **196,396**, with **3,604 estimated tokens** of
  headroom. This exceeds the 195,000 planning target; official count is unknown.
  Calibration remains maintainer 200,414 at
  `def1e856e101ff0e57dd96adccab2cf1d886b076`.
- Current development JAR: 938,473 bytes, SHA-256
  `be443abe7b1d2fd91e7bd2413fb6246f8046a8c78310233042c6896ea35dc126`.
  This is not a versioned 1.0 release artifact.

Evidence: [latest maintenance rehearsal](testing/maintenance-rehearsal-2026-10-26.md),
[visage fix](testing/visage-components-2026-10-31.md),
[Main diary policy](testing/main-diary-teleports-2026-10-26.md),
[lifecycle matrix](testing/finish-lifecycle-matrix-2026-10-26.md),
[runtime cleanup](testing/finish-runtime-cleanup-2026-10-26.md),
[review follow-up](testing/claude-review-follow-up-2026-10-25.md),
[ammo labels](testing/ammo-group-tab-numbering-2026-10-25.md),
[wheel scrolling](testing/sidebar-wheel-2026-10-25.md),
[remaining review](testing/remaining-review-2026-10-26.md) and
[kept tab moves](testing/accepted-tab-move-2026-10-26.md).

## Conditions before publication

1. Recover the planned review reserve; inspect non-main-Java changes separately.
   A passing estimate/build never certifies the official 200,000-token limit.
2. Resolve or explicitly scope the unspecified shield/defender case from #48.
   Related passing cases do not prove that report fixed.
3. Owner verifies the combined unpublished Main/Ironman workflow in game.
4. Set the final version, prepare bundled notice/history and verify first-open,
   dismissal, restart and history navigation. Record the final artifact/hash.
5. Obtain explicit commit/publication authorization, validate a clean clone of
   that exact source commit, then publish and verify the release/Hub state.

After accepted completion, follow [MAINTAINING.md](../MAINTAINING.md): bugs,
compatibility and reviewed item corrections, with no promised feature expansion
or release cadence. No condition above is waived by calling the version 1.0.

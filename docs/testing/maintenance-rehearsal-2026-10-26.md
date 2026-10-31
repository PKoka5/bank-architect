# Maintenance handover and source-build rehearsal

26 October 2026. Entry point: [MAINTAINING.md](../../MAINTAINING.md). Continues
[the completion plan](../plans/finish-1.0.md). No publication or new runtime
feature was authorized by this rehearsal.

## Transferable instructions

The entry point links existing build, data-generation, audit, gear-policy,
compatibility and release instructions. It records the supported eight-column
Main/Ironman scope, maintenance triage, personal-choice precedence, token budget,
snapshot recovery and required owner checks. README links it and describes the
post-completion maintenance policy without declaring completion prematurely.
Earlier exploratory roadmaps remain historical; the finish plan is authoritative.

## Separate source build

661 tracked/nonignored working-tree files were copied into
`build/tmp/maintenance-rehearsal-2026-10-26/`, with a SHA-256 source manifest.
No existing build outputs, project `.gradle` state, ignored research caches,
tokenizer dependencies or private raw exports were copied. Deleted tracked
files were not restored. The reviewed build/doc refinements were synchronized
and the manifest updated explicitly.

This is a materialized current-source build, **not a clean clone of a final
release commit**: the approved working changes are still uncommitted. The global
Gradle/wrapper cache was warm. Toolchain: Windows, Temurin **11.0.31+11**, Gradle
wrapper **8.7**, RuneLite client **1.13.4** resolved offline from cache for
`latest.release`. This does not verify today's remote dependency release or a
cold-machine installation.

## Item-maintenance negative control

Only in the copy, Karamja gloves 1 (11136) temporarily received the valid-looking
but intentionally incorrect subcategory `maintenance-probe`, replacing
`achievement-utility`. The original source and compiled table were backed up.

1. `GeneratedRegistryTest`: 9 tests, **2 expected failures** identified the stale
   checked-in table and the exact mismatching subcategory. The check did not
   silently regenerate it.
2. Explicit `compileItemRegistry`: succeeded; exactly one compiled row changed.
3. Fresh `GeneratedRegistryTest` and `ReviewedItemRolesTest`: 11 tests passed.
   This is an important limit: the latter ledger does not contain 11136, so its
   success did not validate this item's function.
4. Existing `CombatFunctionalRoleCoverageTest`: 10 tests, **2 expected failures**
   rejected the subcategory for Main and Ironman against independent expectations.
   The handover now explicitly requires the item-specific test as well as the
   partial ledger and full build.
5. Both source and compiled bytes were restored. Every copied source file
   matched the manifest again before the final clean build. No probe value was
   applied to the original workspace or retained in the final artifact.

This replays the review/compile/verify workflow using an existing reviewed item;
it is not research approving a new game item or a freshness certification for
the historical source cache.

## Results and artifact preparation

The restored copy passed `clean build --offline --console=plain`: **2,605 tests**,
zero failures/errors/skips and **1,950 completed scenario banks**. All four
simulation fingerprints matched, without changing references. The final regular
workspace build also passes. No compiler warning was reported in either log.

Initially all 255 file payloads in the two JARs matched but archive hashes
differed because of ZIP metadata. `build.gradle` now disables preserved file
timestamps and enables reproducible entry ordering. Repackaging both source
locations then produced identical bytes:

- `ironman-bank-architect-0.9.9.jar`: **938,352 bytes**, **268 entries** including
  directories, SHA-256
  `147bcc645d291720bb9fe9cc0a137d1f4a954eaed0052d9848997f3babd8b0fd`.
- Runtime facts/seed/quick-tool tables are included. Test classes, derivation
  inputs/helpers, research tools, private exports and reports are absent.
- These are development artifacts retaining the existing version string, not
  replacements for the already published 0.9.9 asset.
- All 661 manifest entries matched in both source locations after the exercise.

Review-size verification on the source copy reproduced **196,342** as the highest
local estimate, with **3,658 estimated headroom**; others were
193,189 / 196,006 / 193,526. It reused the original repository's read-only Git
baseline and installed tokenizer via process-local environment variables; it
did not create a branch/index or install dependencies. Calibration remains
200,414 at `def1e856e101ff0e57dd96adccab2cf1d886b076`.

The estimate covers main Java. The two packaging settings, README and maintenance
documents were inspected separately; the official scope/tokenizer remain unknown.
The local 195,000 target still needs **1,342** estimated reduction. This rehearsal
does not approve a Hub submission.

## Evidence and remaining gates

Logs under `build/tmp/`: `maintenance-stale-probe.log`,
`maintenance-probe-generate.log`, `maintenance-reviewed-probe.log`,
`maintenance-functional-probe.log`, `maintenance-clean-build.log`,
`maintenance-final-build.log`, `maintenance-reproducible-jar.log`,
`maintenance-dependency.log` and `maintenance-review-size.log`.
Manifest: `maintenance-rehearsal-manifest.json`. Build/test reports remain in
the original and copied `build/` folders. Intentional negative-control failures
are distinct from the passing restored final build.

[The 1.0 draft](../release-1.0.0-draft.md) makes the proposed release reviewable.
Final version/notice preparation, extra review margin, exact #48 evidence and
the combined owner in-game check remain open. A clean clone of the final source
commit and explicit publication authorization are still required. No commit,
push, branch switch, tag, Hub update or release was performed.

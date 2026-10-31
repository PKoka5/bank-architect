# Round 1: supported presets and runtime cleanup

26 October 2026. Continues [the first checkpoint](finish-round-1-2026-10-26.md)
under [the bounded completion plan](../plans/finish-1.0.md). Local work only;
published source remains `b3583ca8e48bedb37514e89006cb677e5b7ea75a` (0.9.9).

## Supported-preset boundary

The live chooser already offered only Main and Ironman. The share-code decoder
already rejected PvM, PvP and Skiller, even though their enum constants, bundled
definitions and mapping branches remained internally reachable from tests.
Those unshipped definitions and branches have now been removed. Main/Ironman
definitions and routing are unchanged, as are stored plans, profiles, tag keys
and supported share codes. Unsupported preset strings still fail decoding.

Four tests solely for the unshipped presets were retired. Generic sorting and
custom supply-category behavior remain covered with explicit custom categories;
supported-preset, historical-definition, import and profile tests still pass.
The historical definition fixture remains unchanged; its compatibility assertion
now explicitly selects the two supported preset types.

## Catalog and test setup

The ten seed catalog records now use the same validated bundled-table decoder
as the generated registry, with a separate six-column seed format for workflow
keys. The ordinary registry still requires exactly five columns. The immutable
seed map is loaded through `RequiredResource`, so missing or damaged data fails
with catalog-unavailable context instead of silently producing partial results.

Every ID, name, category, subcategory, ordered tag and workflow key was compared
against an independent extraction of the published Java declarations. The frozen
normalized record fingerprint is
`cc7af5d89766f79ee94f89a3b2cd17332510decb164c5d3457d4ac20b03e1b8f`.
Two added tests pin that fingerprint and verify field counts, missing/empty
resources, malformed records and duplicate IDs/tags. Empty final TSV columns are
intentional. The resource contains static records, not executable policy.

Five planner overloads only supplied default arguments for tests; both runtime
analysis and the development combat simulator already supply all eight inputs.
Those defaults now live in test-only `PreviewFixtureBuilder`, which delegates to
the unchanged full production entry point. The 68 caller-file changes are
mechanical. No planner decisions, validation, fallback logic or assertions were
moved out of production. The helper is absent from the packaged JAR.

## Verification

- Java 11 offline `build` passes: **2,417 tests**, zero failures/errors/skips.
  Relative to the prior checkpoint: four unshipped-only tests retired and two
  catalog regressions added; the remainder still runs.
- All **1,950 scenario banks** pass. All four frozen simulation fingerprints
  match, without changing the reference reports.
- No compiler warning or error in the final build log. Tracked whitespace check
  passes; the two empty trailing seed-table fields are required schema columns.
- Both seed and quick-tool data resources are present in the development JAR.
- Highest local main-Java estimate: **196,201**, leaving **3,799 estimated
  tokens**. The other estimates are 193,034 / 195,848 / 193,386.
- Reduction from the previous checkpoint: **1,328**. The 195,000 target is still
  **1,201** away. This checkpoint does not mark round 1 complete.

The calibration remains maintainer count 200,414 at
`def1e856e101ff0e57dd96adccab2cf1d886b076`. The official tokenizer and complete
preprocessing scope remain unknown. Resources, tests, development tools and docs
were inspected separately; the main-Java estimate is not Hub approval and does
not prove an equal official saving from the data extraction.

Logs: `build/tmp/finish-cleanup-presets-final.log`,
`build/tmp/finish-cleanup-catalog-focused.log`,
`build/tmp/finish-cleanup-full.log` and
`build/tmp/finish-cleanup-size-fixtures.log`.

Local development JAR: `ironman-bank-architect-0.9.9.jar`, **937,466 bytes**,
SHA-256 `86e23cb91fbe625f25f8330deb02b434c86a7742a849d695216c984eec870c14`.
No published artifact, version, commit, branch or release was changed.

## Still open

Further focused simplification is needed for the local 195,000 target. The exact
shield/defender report in #48 still awaits the reporter's identities and sequence;
the previous checkpoint's 216 nearby states are not a claimed reproduction.
The owner still needs to check the combined unpublished work in game. The finite
lifecycle matrix and maintenance handover remain subsequent rounds.

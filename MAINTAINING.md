# Maintaining Bank Architect

Start here without relying on private chat history. The active completion scope
is [finish-1.0](docs/plans/finish-1.0.md); earlier roadmaps and research milestones
are historical context, not additional feature obligations. The owner authorized
the [1.0.0 release](docs/release-1.0.0.md) on 31 October 2026. That record states
the release decision, validation boundaries and remaining maintenance objectives.

## Supported product and triage

Support Main/Ironman and their saved custom layouts on the standard eight-column
bank. Every real bank action is manual. Keep the runtime local: no network,
telemetry, automation or inventory/equipment reads. Write original code; do not
copy another plugin's code, assets or layout. See [AGENTS.md](AGENTS.md).

After completion, prioritize reproducible crashes, lost choices/data, unsafe or
wrong guidance, unintended Flexible Alch, and OSRS/RuneLite/Hub incompatibility.
Batch reviewed item corrections and variants when useful. Unknown valid items
use conservative review and manual assignment; they are not disposal evidence.
Fixed Alch intentionally follows its literal list. Missing stats do not prove
an item redundant. There is no release schedule or promise to add new features.

For a report, ask for version, preset/custom profile, mode, exact IDs/names,
before/after blueprint exports, placeholder state and a short reproduction.
Check whether manual settings explain the result. Exports may reveal bank
contents: let the reporter remove unwanted information. Reproduce before
claiming a fix. Close out-of-scope preferences with an explanation of existing
controls; related tests alone do not prove the reported bug fixed.

## Build and test

Use JDK 11 and the checked-in Gradle 8.7 wrapper. Set `JAVA_HOME` and put its
`bin` first on `PATH`. From the repository root:

```powershell
.\gradlew.bat build --offline --console=plain
```

Offline builds require an already populated dependency/wrapper cache. A new
machine must resolve dependencies online first. `latest.release` in
`build.gradle` is intentionally dynamic; record the actual RuneLite version for
a release/rehearsal. An offline build does not verify compatibility with a newer
remote release.

`build` runs tests, 150 standard and 1,800 aggregate scenario banks, then verifies
four frozen simulation fingerprints. Results are in `build/test-results/test/`
and `build/reports/`. Never update hashes just to pass: review the individual
behavioral differences and document intentional changes. Custom `-Psim...`
experiments run separately from this default gate. See [Development](README.md#development)
and [combat quality reports](tools/research/combat-layout-simulation/QUALITY-REPORT.md).

## Review an item change

1. Follow [change tracking](tools/research/item-classification-audit/CHANGE_TRACKING.md):
   record the exact identity, function, variants, source revision/date and
   unresolved facts. A dependency upgrade alone does not update bundled facts.
2. Choose the smallest correct layer: catalog identity/category, usage tag,
   ordered family, primary candidate or contextual Alch rule. Preserve the
   distinction between intrinsic function and preset placement. For example,
   Main groups lower diary tiers with Teleports without inventing a teleport.
3. Add independently reviewed expectations and nearby counterexamples: other
   tiers, charge states, noted/placeholder/dummy/restricted identities. Do not
   generate expected answers from the classifier being tested.
4. For catalog-source edits, explicitly regenerate and inspect the output:

```powershell
.\gradlew.bat compileItemRegistry --offline --console=plain
.\gradlew.bat test --tests '*GeneratedRegistryTest' --tests '*ReviewedItemRolesTest' --offline --console=plain
.\gradlew.bat build --offline --console=plain
```

Run generation and verification as separate invocations so the new resource is
copied before verification. A normal build detects stale output and never repairs
it automatically. Compilation proves derivation matches inputs, not gameplay
correctness. `ReviewedItemRolesTest` is a partial reference: also run/add the
item-specific placement/function test. For example, Karamja gloves are covered
by `CombatFunctionalRoleCoverageTest`, not that partial ledger. The full build
runs both. Review generated and input diffs together.

Detailed formats/exclusions: [compiled registry](tools/research/melee-bis/COMPILED-REGISTRY.md).
Independent placement audit: [classification audit](tools/research/item-classification-audit/README.md).
Gear candidates: [Melee](tools/research/melee-bis/README.md) and
[Ranged/Magic](tools/research/ranged-magic-bis/README.md). TSV row/member order can
affect layout. Keep executable layout policy in Java. Research caches and tools
must stay outside the production JAR.

## Personal choices and saved-state compatibility

Automatic catalog/preset defaults come before explicit player corrections.
Valid editor routes/orders and captured physical positions have their existing
precedence; stale original tags may make an old route dormant. Do not activate
all old routes globally to handle one classification change. Accepted targets
retain existing order/destinations while new items append. Personal settings
can pause a kept target without deleting it; whole-tab moves remap its records.

Keep supported readers and dormant entries unless a documented migration safely
preserves them. Damaged/future snapshots must be preserved with a visible recovery
path, not silently reset. Changing `AcceptedBlueprint.RULES` marks a new rule
generation; it does not by itself invalidate older supported targets. Exercise
stale queued operations and profile switching when changing persistence.
See [lifecycle matrix](docs/testing/finish-lifecycle-matrix-2026-10-26.md),
[kept-tab moves](docs/testing/accepted-tab-move-2026-10-26.md), and
[Main diary compatibility](docs/testing/main-diary-teleports-2026-10-26.md).

## Prepare a release locally

- Review all source/resource/test/tool diffs and the full build. Check the JAR
  contains runtime facts and bundled notices, and excludes test classes, raw
  derivation inputs, research caches, private exports and generated reports.
- Run the [review-size estimator](tools/review-size/README.md) from a Git checkout
  with its locked development dependencies. Use the highest of all four estimates
  and inspect non-main-Java changes separately. Target at most **195,000** locally;
  every Hub submission must stay strictly below **200,000 official tokens**.
  Comments/Javadoc are stripped. Do not remove tests, hide executable rules in
  data, or treat a passing build/local estimate as official token approval.
- Record any new maintainer count together with its exact submitted source SHA;
  recalibrate from that pair. Current calibration: 200,414 at
  `def1e856e101ff0e57dd96adccab2cf1d886b076` (30 September 2026).
- Prepare a release record like [0.9.9](docs/release-0.9.9.md), including actual
  runtime/dependency versions, tests, simulations, token uncertainty, owner checks,
  artifact size/SHA-256 and known limits. Keep development artifacts distinct from
  already published assets. Do not overwrite an existing release asset.
  JAR entries use fixed timestamps and stable ordering; compare hashes using
  identical sources, toolchain and dependencies. This is not a promise that a
  future dynamic RuneLite dependency resolves to identical bytes.
- When preparing an authorized version, align `build.gradle`, bundled
  `release-notes.properties`, the new history entry and release record. Verify
  first opening, dismissal, restart and Updates history; an unread notice must
  not be dismissed merely by visiting history.
- Have the owner check the combined Main/Ironman workflow in game: analyze,
  preview, wheel scrolling, profile/custom changes, kept/captured target,
  placeholders, whole-tab movement, Swap/Insert and guide completion. Check
  supported filtered-view recovery without taking automated bank actions.
- Record remaining blockers honestly. Commit/push/tag/publish/update the Hub only
  with explicit owner authorization. The Hub manifest must identify the exact
  reviewed source commit. Verify distribution state afterward; a submitted PR
  is not an approved release.

A source-only clean build with warm caches is useful but is not a cold-machine
install or a clean clone of the final release commit. Record those boundaries.
Final 1.0 publication requires the completion plan's remaining gates, including
review margin and the owner check; this document does not declare them passed.

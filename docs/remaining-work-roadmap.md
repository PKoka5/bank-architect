# Remaining Work Roadmap

This roadmap describes the work remaining after Guidance V2, the active ten-category Ironman
preset, the random-bank simulator, and the first simulator-driven cleanup curation round. The core
Ironman product is functionally complete and the live mechanics gate passed on 2026-07-13. The work
below improves classification quality, adds data-backed semantics, prepares optional presets, and
turns the current development build into a publishable RuneLite plugin.

The roadmap is a sequencing and acceptance contract. It does not authorize runtime network access,
bank mutation, input automation, or speculative classification.

## Permanent constraints

Every phase retains the product contracts in `v1-product-contract.md`,
`universal-preset-roadmap.md`, `bank-organisation-rules.md`, and `bank-tab-mechanics.md`:

- the player performs every bank action manually;
- runtime code remains read-only and uses no network calls, reflection, external processes, native
  code, or telemetry;
- classification and family membership use exact item IDs from local registry or pinned local data;
- display-name fragments, numeric suffixes, and item-ID arithmetic are not evidence of a family;
- a family remains fail-closed until every supported player-facing state has reviewed ID coverage;
- unknown or weakly supported items route to the preset's review destination;
- plans remain dense, preserve real owned entries, and never invent Bank Fillers or empty cells.

Any implementation phase that changes executable code or shipped catalog data must finish with:

```text
./gradlew test
./gradlew simulateRandomBanks -PsimBanks=50 -PsimSeed=20260718
./gradlew build
```

The fixed-seed simulation must report all 150 scenario banks as `COMPLETED`. Classification phases
must also regenerate their relevant audit or cleanup report, record before/after distinct-ID and
occurrence counts, and prove that `0`, `-1`, and an unknown high ID still fail closed.

## Ordering and dependencies

The critical path to an initial Plugin Hub submission is:

`scaled cleanup measurement -> cleanup exit criterion -> live smoke test -> publication preparation`

Additional presets are not on that critical path. The active Ironman preset can be published while
Main, PvM, PvP, and Skiller remain internal foundations. Data-gated family work may interleave with
cleanup curation when a reviewed family appears in the cleanup report, but no later feature may
weaken the cleanup exit criterion.

Preset routing-policy work may begin before the cleanup loop ends because all presets share the
catalog. Preset selection cannot ship until the session-reset contract is approved. PvM value
sorting and most PvP-specific behaviour have additional data gates, so Main and Skiller are the
first reasonable preset candidates; PvP is deliberately last.

The phases below express this ordering. A phase may overlap another only where its prerequisites say
so.

## Phase 1 - Scale and stabilize cleanup measurement

**Workstream:** A - iterative cleanup curation.

### Scope

- Define a checked-in simulation protocol using at least three recorded seeds and at least 200 bank
  runs per seed. Each run continues to exercise all three simulator scenarios.
- Add a test-scope aggregation path that merges cleanup observations across seeds without changing
  production routing. One row remains one item ID with canonical name, source constant, and summed
  occurrence count.
- Make ordering deterministic: occurrence count descending, then item ID ascending.
- Record the seed set, simulator item-count range, registry fingerprint or revision, scenario count,
  and total generated banks beside each aggregate.
- Preserve the existing single-run `cleanup-review.tsv` for quick local verification, or document a
  compatible replacement before removing it.

### Data and design prerequisites

- Decide the stable seed list and whether aggregation is one Gradle invocation or a separate merge
  task. The result must be reproducible without network access or external processes.
- Define how repeated appearances of the same sampled bank item across scenarios are counted. The
  current meaning - one occurrence per simulated scenario bank - is acceptable if documented and
  retained consistently.
- Confirm which local registry records are player-facing, CERT, PLACEHOLDER, cache-only, activity
  specific, or otherwise non-bankable. These markers inform review; they must not become broad
  production heuristics.

### Out of scope

- Production classification changes.
- Changing simulator sampling to favour desired conclusions.
- Treating item names, constants, or declaration markers as automatic production classifications.

### Acceptance criteria

- Re-running the aggregate with identical inputs produces byte-identical row ordering and counts.
- The report includes all sampled items whose effective Ironman route is `storage-cleanup` and no
  item routed elsewhere.
- Aggregate metadata makes the run independently reproducible.
- Test-scope reporting tests cover aggregation, tie ordering, and an empty cleanup result.
- `./gradlew test`, the fixed 50-bank simulation, and `./gradlew build` all pass; the fixed simulation
  remains 150/150 `COMPLETED`.

Phase 1 blocks the formal completion claim for Workstream A. It does not block continued small
curation rounds against the existing fixed-seed report.

## Phase 2 - Run bounded cleanup-curation rounds

**Workstream:** A - iterative cleanup curation. This phase repeats until the definition of done below
is satisfied.

### Scope

- Review the highest-occurrence aggregate rows, separating clearly bankable false-cleanup items from
  legitimate quest remains, junk, holiday/activity items, historical records, and cache variants.
- Select roughly 15-25 complete families per round. Prefer complete coverage of fewer families over
  partial coverage of a large theme.
- Cross-check every player-facing state against the local registry/gameval data before coding.
- Extend the existing canonical override and catalog structures; do not create a second classifier.
- Add positive category and preset-tab tests for every family plus negative unknown-ID controls.
- Record per-round input protocol, reviewed families, dispositions for borderline rows, and
  before/after distinct-ID and occurrence counts.
- Decide borderline families before implementation. Mature keg ales are the first known decision:
  document whether they are consumable supplies, brewing resources, collection items, or retained
  review items, and apply that decision to the complete family only.

### Data and design prerequisites

- Phase 1's aggregate protocol, or an explicitly documented interim fixed-seed baseline.
- A complete exact-ID inventory for every proposed family, excluding CERT and PLACEHOLDER copies only
  where the local declaration audit proves they are not player-facing.
- A written destination decision for any family whose bank purpose is not obvious.
- A source note identifying the local gameval/registry revision used for the family review.

### Out of scope

- Attempting to classify all roughly 11,400 unknown-source records in one pass.
- Rescuing an ID only because its display name resembles a resource, weapon, potion, or teleport.
- Reclassifying documented historical, minigame, quest-only, or placeholder copies to improve a
  headline metric.
- Adding sort-family geometry unless the same round also has complete semantic data for it.

### Acceptance criteria for each round

- Every shipped family has complete reviewed player-facing ID coverage and direct positive tests.
- CERT, PLACEHOLDER, unknown, and deliberately excluded controls continue to route to review.
- The aggregate report contains fewer clearly bankable false-cleanup occurrences, with exact
  before/after numbers recorded.
- No curated family loses members, changes destination unexpectedly, or becomes dependent on a name
  heuristic.
- `./gradlew test`, the fixed 50-bank simulation, and `./gradlew build` pass; simulation remains
  150/150 `COMPLETED`.

### Definition of done for the cleanup loop

Workstream A is done only when one recorded aggregate protocol satisfies all of these conditions:

1. The aggregate covers at least three fixed seeds and 200 generated banks per seed, with all three
   simulator scenarios, and every scenario bank completes.
2. The top 250 cleanup rows have an explicit reviewed disposition: legitimate cleanup,
   non-player-facing/cache record, intentionally deferred borderline family, or classification bug.
3. No classification bug or clearly bankable player-facing family remains in that top 250.
4. No intentionally deferred borderline family remains without a named maintainer decision owner and
   a concrete evidence requirement.
5. A second review pass after the final curation round finds no new clearly bankable family in the
   top 250. The two passes must use the same registry revision and aggregation protocol.
6. The final report records total cleanup distinct IDs and occurrences, cumulative IDs and
   occurrences moved by all rounds, and the residual disposition counts.
7. Unknown controls still fail closed and the required test, fixed-seed simulation, and build gates
   pass.

This criterion does not require cleanup to become numerically small. A large remainder is acceptable
when its highest-frequency rows are demonstrably legitimate review material rather than hidden bank
resources, gear, supplies, or utilities.

## Phase 3 - Deliver deterministic data-gated families

**Workstream:** B - charged jewellery, potion/food extensions, and farming relationships.

This phase may interleave with Phase 2. A family found by cleanup review can be delivered through
this phase when it needs richer ordering metadata than a category override.

### Phase 3A - Charged jewellery, family by family

#### Scope

- Inventory one jewellery family at a time, including every supported charge, uncharged state,
  eternal state, imbued state, and player-facing alternate state.
- Add an exact-ID family key and explicit charge order only after the family inventory is complete.
- Route each state through an explicit preset policy. Ordering metadata must not silently decide its
  destination.
- Start with families that are both common in Ironman banks and fully verifiable in local gameval
  data. Each family is an independently releasable slice.

#### Prerequisites

- A reviewed per-family ID manifest with every supported state and an explicit exclusion list.
- A decision for uncharged, eternal, imbued, and teleport-depleted states.
- Tests that demonstrate the family cannot absorb similarly named quest, minigame, CERT, or
  PLACEHOLDER records.

#### Out of scope

- Charge recognition from parentheses, suffixes, display names, or adjacent item IDs.
- A universal jewellery ordering rule before individual families are complete.

#### Acceptance criteria

- Each delivered family has complete ID membership, deterministic charge order, explicit Ironman
  routing, and positive/negative tests.
- Partial families remain unchanged and fail closed where appropriate.
- `./gradlew test`, the fixed 50-bank simulation, and `./gradlew build` pass; simulation remains
  150/150 `COMPLETED`.

### Phase 3B - Potion-dose and food-table extensions

#### Scope

- Extend potion metadata family by family for newer, divine, blighted, barbarian-mix, minigame, and
  raid variants only when their semantics are separately reviewed.
- Extend the local food table with pinned source records, explicit immediate healing, role, remaining
  servings, and restrictions.
- Preserve the Ironman split in which curated dose four is ready-to-use Supplies and curated lower
  doses join Herblore workflow lines.

#### Prerequisites

- Complete canonical IDs and pinned source-manifest entries for each proposed family.
- Explicit decisions for restricted, variable, delayed, and multi-bite behaviour.
- A clear separation between normal, minigame, raid, divine, blighted, and barbarian-mix families.

#### Out of scope

- Inferring potion doses or healing from names.
- Reading live Hitpoints, inventory, or equipment to change ordering.
- Treating total multi-bite healing as immediate healing per consume action.

#### Acceptance criteria

- Every new family has complete exact-ID membership and table validation tests.
- Existing food and potion ordering remains deterministic and preset routing remains explicit.
- Unknown food and potion-like IDs receive no invented facts.
- `./gradlew test`, the fixed 50-bank simulation, and `./gradlew build` pass; simulation remains
  150/150 `COMPLETED`.

### Phase 3C - Sapling and protection-payment relationships

#### Scope

- Build a shipped local relationship table connecting each reviewed sapling/tree family to its exact
  protection-payment item IDs and quantities.
- Use pinned Wiki recipe/payment revisions as review sources and ship a source manifest following the
  existing food-table pattern.
- Add a semantic relationship only where both sides and the quantity are reviewed.

#### Prerequisites

- Maintainer approval of the table schema, pinned-source manifest format, and update process.
- Complete canonical IDs for saplings, relevant seed/tree states, and every payment item in scope.
- A decision on whether the first version affects routing only, sorting only, or both.

#### Out of scope

- Runtime Wiki access.
- Matching saplings and payments by species names.
- Inventing missing payment items or empty alignment cells in a bank plan.

#### Acceptance criteria

- The table is reproducible from its documented pinned sources and validated for duplicate or missing
  family members.
- Relationship tests cover complete, partial-owned, and unknown-ID banks without phantom cells.
- `./gradlew test`, the fixed 50-bank simulation, and `./gradlew build` pass; simulation remains
  150/150 `COMPLETED`.

## Phase 4 - Resolve the GE-value design gate

**Workstream:** B - Main/PvM loot sorting.

This is a design and source-approval phase before it is an implementation phase. It may run in
parallel with Phases 2 and 3, but value-based production code must not start until the gate closes.

### Scope

- Identify whether RuneLite exposes a permitted local value source usable by this plugin without a
  new runtime network dependency.
- Specify what value means: GE price, high-alchemy value, or another explicitly named fact.
- Define freshness, missing-value, offline, startup, and stale-data behaviour.
- Define deterministic fallbacks and prove that value availability cannot move unknown items out of
  their review destination.
- Record the approved source and policy in a dedicated design document before implementation.

### Data and design prerequisites

- Maintainer decision on the permitted local RuneLite API/source and dependency boundary.
- A freshness policy with an observable timestamp or revision and a maximum acceptable age.
- A privacy and runtime review confirming no plugin-owned telemetry or network request is introduced.

### Out of scope

- Assuming the RuneLite API, cache, or price manager is permitted without review.
- Bundling an unexplained price snapshot.
- Blocking deterministic routing when a value is absent.

### Acceptance criteria

- The design either approves a concrete source and fallback policy or explicitly records that GE
  sorting remains blocked.
- If implementation follows, tests cover current, stale, absent, and equal values, and
  `./gradlew test`, the fixed 50-bank simulation, and `./gradlew build` pass with 150/150 simulation
  completion.

A blocked GE-value rule does not block the initial Ironman Plugin Hub release. It does block claims
that Main or PvM has complete value-based loot ordering.

## Phase 5 - Complete and expose additional presets

**Workstream:** C - Main, Skiller, PvM, and PvP.

### Phase 5A - Approve preset switching and shared routing contracts

#### Scope

- Define the preset-switch user experience and the exact session state that resets.
- Preserve today's deliberate boundary: starting a new analysis resets guidance. A preset change
  must either start a new analysis or be rejected while an analysis session is active.
- Define persistence expectations for selected preset, saved blueprints, locks, progress, and sidebar
  state.
- Audit shared sort modes so preset policies do not depend on magic category keys.

#### Prerequisites

- Maintainer approval of the reset and confirmation behaviour.
- A state-transition inventory for panel, controller, plan, advisor session, overlay, and saved local
  blueprint data.
- A rollback decision for a preset switch that cannot produce a supported dense plan.

#### Out of scope

- Adding a selector before the reset contract is approved.
- Reusing an in-progress guidance session against a differently routed plan.

#### Acceptance criteria

- A written state-transition contract names every retained and reset state.
- Tests cover switching before analysis, during analysis, after completion, and after an unsupported
  plan result.
- `./gradlew test`, the fixed 50-bank simulation, and `./gradlew build` pass with 150/150 simulation
  completion.

### Phase 5B - Main and Skiller

#### Scope

- Complete exact routing policies for every category in `preset-category-roadmap.md`.
- Reuse shared sort modes and add preset policy only where the same item intentionally routes
  differently.
- Add per-preset unknown/review tests and full-bank plan tests.
- Expose each preset only after its routing audit has no unexplained category fallback outside its
  review destination.

#### Prerequisites

- Phase 5A's approved reset contract.
- A routing matrix covering every effective `ItemCategory`, curated special family, and fallback.
- For Main, an explicit decision whether selection may ship before GE-value sorting. If it may, the
  UI and docs must describe deterministic non-value loot ordering without implying GE order.

#### Out of scope

- Preset-specific magic keys in shared sorters.
- Treating Ironman routing as automatically correct for Main or Skiller.

#### Acceptance criteria

- Every category and known special family has one tested destination per preset.
- Unknown and weak items fail closed to `junk-review` or `loot-clues-storage` as defined.
- Switching presets starts from the approved clean session boundary.
- `./gradlew test`, the fixed 50-bank simulation, and `./gradlew build` pass with 150/150 simulation
  completion.

### Phase 5C - PvM, then PvP

#### Scope

- Complete PvM routing for melee, ranged, magic, special-attack, supplies, boss tools, and loot.
- Integrate GE-value sorting only if Phase 4 approved and implemented it; otherwise retain and
  disclose deterministic fallback ordering.
- Treat PvP as the last preset. Add risk, replacement-set, combat-requirement, Wilderness utility,
  and loot-key facts only through reviewed exact-ID data.

#### Prerequisites

- Phase 5A and the shared routing matrix.
- For PvM, decisions on style overlap, switch items, and non-value loot fallback.
- For PvP, approved local sources and schemas for risk values, replacement sets, combat
  requirements, and untradeable/reclaim behaviour.

#### Out of scope

- Guessing gear style, risk, requirements, or replacement membership from names.
- Calling PvP complete while its required facts are absent.

#### Acceptance criteria

- PvM and PvP each pass a complete routing audit with all weak facts failing closed.
- Full-bank fixtures demonstrate dense plans, no invented cells, and correct review destinations.
- `./gradlew test`, the fixed 50-bank simulation, and `./gradlew build` pass with 150/150 simulation
  completion.

Phase 5 may continue after the first Plugin Hub release. No internal preset should appear in the UI
merely to make the roadmap look complete.

## Phase 6 - Real-bank validation and Plugin Hub publication

**Workstream:** D - release validation and publication.

The live smoke test may begin while Phases 2-5 continue because Guidance V2 already passed its
mechanics probe. Final publication preparation requires Workstream A's definition of done, but does
not require optional presets or GE-value sorting.

### Phase 6A - Recorded live smoke test

#### Scope

- Exercise the active Ironman preset against a representative real bank with the sidebar, overlay,
  and five guidance phases.
- Record preconditions, bank size, tab state, generated plan summary, every status transition,
  completion result, and any manual-recovery pause in the style of `bank-tab-mechanics.md`.
- Verify overlay alignment while scrolling, changing bank height, reopening the bank, and moving
  between All view and the required supported state.
- Confirm that no click, drag, keypress, bank mutation, or native-widget mutation originates from the
  plugin.

#### Prerequisites

- A disposable or backed-up test arrangement and a maintainer-approved test checklist.
- Workstream A at or near its exit criterion so obvious classification defects do not dominate the
  session.
- A current build that passes the required verification commands.

#### Out of scope

- Automated input or scripted bank movement.
- Expanding runtime permissions to make the smoke test easier.
- Treating one bank as proof that all classifications are complete.

#### Acceptance criteria

- The recorded session reaches exact plan completion or documents and fixes every reproducible
  blocker before another successful run.
- Overlay geometry fails closed whenever safe alignment is unavailable.
- The manual-action and read-only product contract is observed throughout.
- `./gradlew test`, the fixed 50-bank simulation, and `./gradlew build` pass with 150/150 simulation
  completion after any fixes.

### Phase 6B - Plugin Hub submission package

#### Scope

- Update the README for installation, the active Ironman workflow, manual-only guidance, safety
  boundaries, limitations, and local data behaviour.
- Add original screenshots, an original icon, and polished plugin metadata/configuration.
- Create a local Plugin Hub checklist covering repository layout, metadata, licensing, dependencies,
  code review concerns, and user-facing privacy/safety statements.
- Review the distributable artifact to confirm test utilities, reports, research caches, and local
  source-review material do not ship unintentionally.
- Prepare the submission changes for maintainer review; committing, pushing, and opening a pull
  request remain separate explicit actions.

#### Prerequisites

- Workstream A's definition of done.
- A passed Phase 6A smoke-test record.
- Maintainer approval of name, description, icon, screenshots, configuration defaults, support
  channel, and release version.
- A locally reviewed current Plugin Hub requirements checklist. Requirements may change, so they
  must be rechecked at submission time rather than assumed by this roadmap.

#### Out of scope

- Adding runtime dependencies solely for publication assets or analytics.
- Claiming support for internal presets.
- Publishing without explicit maintainer authorization.

#### Acceptance criteria

- README, metadata, configuration, icon, and screenshots consistently describe the shipped active
  Ironman feature set and manual-only operation.
- The local Plugin Hub checklist has no unresolved required item.
- The built artifact contains no test/report/cache files and introduces no new runtime network path.
- `./gradlew test`, the fixed 50-bank simulation, and `./gradlew build` pass with 150/150 simulation
  completion from the submission candidate tree.

## Open design questions

These questions require maintainer decisions or source review. They are not resolved by the current
repository and must not be answered implicitly in code.

1. **GE-value source and freshness.** Which local RuneLite value source, if any, is permitted? What
   maximum age is acceptable, how is age observed, and what deterministic ordering applies when a
   value is absent or stale?
2. **Preset-switch experience.** Is a preset change allowed only before analysis, does it ask to
   start a new analysis, or does it automatically reset? Which selected-preset, blueprint, lock,
   progress, panel, and overlay states persist?
3. **Main without GE sorting.** May Main become selectable with a disclosed deterministic fallback,
   or is value sorting part of its minimum complete contract?
4. **PvP fact sources.** Which pinned local sources can establish risk, reclaimability, replacement
   sets, combat requirements, and untradeable status, and how often must those facts be reviewed?
5. **Borderline cleanup destinations.** Where do mature keg ales and similar deliberately banked but
   low-frequency families belong? Each decision needs a complete-family destination rule before
   implementation.
6. **Aggregate seed protocol.** Which fixed seeds become the long-term Workstream A benchmark, and
   should the aggregate report be a first-class Gradle task or a documented composition of existing
   runs?
7. **Release scope.** This roadmap recommends publishing the completed Ironman preset without waiting
   for optional presets. The maintainer must confirm that product boundary before submission work.
8. **Live smoke-test evidence.** Which bank is representative, what evidence can be recorded without
   exposing private account information, and what kinds of manual-recovery pause are acceptable for
   the initial release?
9. **Plugin Hub requirements at submission time.** The checklist must be refreshed when submission
   begins. Any new requirement that conflicts with the product's read-only, no-network, or
   no-telemetry boundary requires a maintainer decision rather than a silent exception.

## Final completion state

The remaining roadmap is complete when:

- Workstream A meets its measurable exit criterion and its residual cleanup rows are reviewed;
- all data-gated features that are claimed as shipped have complete local ID/source manifests and
  tests, while blocked features remain honestly documented as blocked;
- every selectable preset has a complete routing policy and uses the approved reset boundary;
- the active release passes a recorded real-bank smoke test;
- the Plugin Hub submission package passes its current local checklist and all required verification
  commands;
- optional internal presets and unresolved data gates are not represented as released features.


# Bank Architect 0.8.0

Save a manually adjusted bank as the active blueprint, with fixes for analysis,
saved layouts and repeat sorting. Prepared on 2026-10-04 from the working tree
above released source `3cd61736a7e7779279299521ef5a41695c1c8757` (0.7.1).

## Player-facing changes

- **Open Blueprint > Save current bank...** saves Main, all numbered tabs and
  their current item order as a new active named layout. Previous layouts remain
  available. Mixed-category tabs, placeholders and multiple physical copies are
  supported. Existing names receive a free numbered suffix.
- Recorded positions remain the target after closing/reopening the bank and
  future analyses. New items follow their automatic categories; missing items
  retain dormant positions. Explicit item assignments and **Reset tab order**
  remain available. Layout share codes still share category assignments, not
  individual captured item positions.
- Fixes `LOCK_TARGET_OUT_OF_RANGE` when fishing tools or a tinderbox coexist
  with outfits in a small Tools tab. The existing strict layout validator remains
  active; all items and quantities are preserved.
- Profile changes reload current corrections and layouts. Saved plans, block
  orders and item orders survive imports with long or legacy separator-containing
  names, without overwriting another saved layout.
- Selecting, Swap, Insert and Undo keep the blueprint editor's scroll position.
- Graceful recolour selection remains stable after reanalysis. Legacy potion
  corrections retain both their destination and their items.
- Alch suggestions require reviewed suitability or genuinely superior owned
  alternatives; quantity alone no longer sends unreviewed gear to the alch pile.
- Spiky vambraces classify as combat gear. Classification remains consistent
  across system languages.
- Shared validation and matching reduce repetition; stale analysis requests
  are skipped before expensive work and the item-icon cache is bounded.
- Opening the sidebar after this update shows a bundled **What's new** message.
  Players who dismissed 0.7.1 see 0.8.0 once and can dismiss it; acknowledgement
  survives reopening and restarting.

All real bank moves remain manual. Bank capture reads supported bank APIs only;
it adds no game actions, inventory/equipment reads, network calls or telemetry.

## Validation

- `./gradlew.bat build --offline`: **passed**, with **1,191 tests**, zero
  failures, errors or skips.
- **1,950 bank simulations completed**; all four existing simulation baseline
  hashes remain unchanged.
- Notice tests verify upgrade from a dismissed 0.7.1 message, acknowledgement,
  reopening and restart. The rendered 204px sidebar notice was visually checked;
  all notes and the dismiss button fit.
- Regression tests cover bank capture, fresh tab boundaries, fillers/stale
  snapshots, duplicate and placeholder recapture, profile limits/preservation,
  reset, new/missing items, classification changes and explicit assignments.
- The owner confirmed the in-game test round passed after the capture and fix
  checks were proposed. Individual live steps were not separately recorded.
- Read-only release audit found no remaining blocker in the reviewed changes.
  Existing sidebar unchecked/unsafe-operation compiler note and Git LF/CRLF
  advisories remain.

## Review-token budget

`npm.cmd run check --prefix tools/review-size` was run after the final version and
notice edits. Calibration remains the maintainer's **200,414** count for source
`def1e856e101ff0e57dd96adccab2cf1d886b076` on 2026-09-30.

| Tokenizer / normalization | Local estimate |
| --- | ---: |
| cl100k, preserved whitespace | 196,575 |
| cl100k, collapsed whitespace | 197,415 |
| o200k, preserved whitespace | 196,591 |
| o200k, collapsed whitespace | **197,441** |

Use the highest estimate: **2,559 tokens of headroom** below 200,000. This is
not the official Hub count or an assertion of Hub eligibility. Record any new
maintainer count together with its exact source revision and recalibrate.

Outside the main-Java estimate, the only changed bundled resource contains five
exact `GEAR/hands` overrides for spiky vambraces IDs `10077`, `10079`, `10081`,
`10083`, `10085`; these rows were checked for duplicates. Other changes are
the version, regression tests, README and review/release documentation. No
dependencies or simulation baselines change. Tests, docs and review tooling are
not bundled with the plugin.

## Artifact

- Jar: `build/libs/ironman-bank-architect-0.8.0.jar`
- Size: **1,088,182 bytes**
- SHA-256: `8f3eb3706c12d7c1e71ac51c73945bc1c19fd335849038ff4af8fe2665a8c24d`

## Publication

The owner confirmed the in-game test round and explicitly authorized commit,
push, GitHub release and Plugin Hub publication on 2026-10-04.

The publication uses tag `v0.8.0` for this reviewed source and the verified jar.
The Plugin Hub submission changes only `plugins/bank-architect` to that tag's
actual source commit. GitHub's release and the Hub PR record the resulting
revision and publication status. Hub client availability follows maintainer
review and distribution. The estimator is checked again on the published source.

Supporting records: [capture](reviews/current-bank-blueprint-2026-10-04.md),
[analysis fix](reviews/fishing-tools-analysis-fix-2026-10-04.md),
[code review fixes](reviews/code-review-fixes-0.7.1.md).

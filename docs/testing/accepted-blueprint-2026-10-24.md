# Accepted blueprint checkpoint

24 October 2026, local 2.0 development above published source
`675ccbd0e708ed6ea3396f0f35ff54ad96dfea21`. No release or Hub update is authorized.

## Behavior

The owner chose to append new items at the end of their matching tab. Review now
offers a separate read-only proposal viewer and an explicit **Keep this proposal**
action. The accepted snapshot preserves real-item order, tabs and categories.
Absent entries remain dormant and regain their stored position when they return.
Quantities, placeholder state and catalog metadata remain current. Empty planner
cells are excluded; acceptance may therefore make the displayed grid more compact.

Acceptance from a builtin preset creates a Custom profile. Each base preset and
named profile has independent local storage. Deletion clears its accepted record.
Personal changes pause the snapshot, while keeping its raw value intact. The
persistent context contains raw personal inputs rather than resolved defaults,
session counters or other profiles' item orders. A later explicit adoption
replaces the complete snapshot, including currently absent entries.

Analysis atomically publishes the effective guidance target and its separate
latest proposal. Review compares the accepted record against the proposal, even
when the target itself stays unchanged. Viewing a proposal publishes no active
bank overlay and cannot edit, save or change guidance. Accepted Alch destinations
never become retained automatic classification evidence.

Accepted records have a format version, rules revision and personal-context
fingerprint. Whole-record validation requires canonical encoding, bounded size,
matching captured routes, valid tags and physical capacity. Unsupported or damaged
records are preserved and stop guidance; selecting another profile remains the
recovery path. Older known-format rules revisions stay usable. Existing comparison
references remain comparison-only. Increment `AcceptedBlueprint.RULES` when future
classification/layout rules change, without automatically replacing stored targets.

Comparison now detects relative order changes using the common item multiset.
Inserted or missing entries, changed quantities, placeholders and absolute slot
shifts do not create false reorders, including interchangeable duplicate copies.

## Verification

Final offline Java 11 build passed in 52 seconds: **2,250 tests**, zero
failures/errors/skips. All **1,950 scenario banks completed**. All four unchanged
simulation fingerprints matched. The existing unchecked-operation compiler note
for `IronmanBankArchitectPanel` remains; no new warning category was introduced.

New coverage: 18 snapshot tests, 15 relative-order tests, 15 real-plugin lifecycle
tests, 14 analysis cases and five UI regression tests. These include restart and
profile isolation, stale/closed/changed-bank saves, proposal/target separation,
strict malformed data rejection, personal-setting suspension, current metadata,
duplicate occurrences, Alch retention, read-only isolation and a new analysis
arriving while acceptance is pending. Narrow editor rendering was inspected.

No runtime item tables, simulation baselines, release notices or resources changed.
Tests and documentation were inspected separately from the main-Java estimate.
The artifact contains no development files. `git diff --check` passes.

Logs:

- `build/tmp/accepted-core-tests-2026-10-24.log`
- `build/tmp/accepted-integration-tests-2026-10-24.log`
- `build/tmp/accepted-ui-tests-2026-10-24.log`
- `build/tmp/accepted-blueprint-final-build-2026-10-24.log`
- `build/tmp/accepted-blueprint-final-size-2026-10-24.log`

Local artifact: `build/libs/ironman-bank-architect-0.9.8.jar`, **1,132,204 bytes**,
270 entries, SHA-256
`096bfbc64a42a5795cb206a957cb460f3934236993bd447b13c83ecd768ba1d9`.
The filename retains the published base version; this is a different local build.

## Open: review-token headroom

Highest local estimate: **199,957**, only **43 estimated tokens** below 200,000.
Other estimates: cl100k preserved 197,861 / collapsed 199,573; o200k preserved
198,237. Calibration remains the maintainer's 200,414 at
`def1e856e101ff0e57dd96adccab2cf1d886b076`.

This is insufficient headroom and is **not a submission-ready checkpoint**.
The official tokenizer/preprocessing/scope are unknown. Regain several thousand
estimated tokens before expanding production features or preparing a submission.
The small analysis-state constructor cleanup preserves behavior but does not solve
that remaining work. No functionality or necessary tests were removed to lower
the estimate, and no runtime logic was moved into resources to conceal its size.

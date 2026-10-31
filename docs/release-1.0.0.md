# Bank Architect 1.0.0

Release prepared 31 October 2026 above published 0.9.9 source
`b3583ca8e48bedb37514e89006cb677e5b7ea75a`. The owner explicitly authorized
publication and replies/closure for the remaining issues after reviewing the
local fixes. This record supersedes [the earlier draft](release-1.0.0-draft.md).

## Player-facing changes

- Clearer blueprint/sidebar numbering, working sidebar/Tab Layout mouse-wheel
  scrolling and an independently movable Ammunition group in Loot. The distinct
  equipment tag is labeled Ammo Slot Equipment; legacy choices remain readable.
- Whole blueprint-tab moves carry kept item order and saved destinations,
  including dormant entries. Explicit recovery can forget only the active kept
  target while retaining other profile choices.
- Active profile settings synchronize with RuneLite. Reanalysis retains eligible
  session Alch evidence and refuses stale queued results. Reviewed equipment
  variants and missing handedness/stat information receive safer handling.
- Main groups regional diary rewards and both diary cape variants with
  Teleports, preserving the regional grid. This is placement policy, not a claim
  that every low tier has its own teleport. Ironman retains its default policy.
- Draconic, Wyvern and Skeletal visage go to Combat as shield upgrade components
  in both presets, outside primary equipment and automatic Alch. Explicit
  personal assignments and supported kept/captured positions retain priority.
- Repeated runtime catalog/setup code is simplified while supported Main/Ironman
  data readers and assertions remain. Unshipped PvM/PvP/Skiller definitions are
  retired; those presets were already absent from the chooser and rejected on import.
- Maintenance instructions, a fixed lifecycle validation matrix and reproducible
  JAR packaging make the supported product easier to maintain.

The first sidebar opening after upgrading shows What's new: 1.0.0. Dismissal is
remembered locally for this release. Updates retains all **16** history entries.
Every real bank move stays manual; no new runtime network, telemetry,
inventory/equipment access, automation or permission is introduced.

## Verification and limits

The final offline Java 11 `build` passes **2,618 tests**, with zero failures,
errors or skips, and **1,950 completed scenario banks**. All four reviewed
simulation fingerprints pass. The visage correction intentionally removes one
Skeletal visage ID/twelve occurrences from aggregate Cleanup; the two aggregate
hash changes were inspected individually. Standard reports remain unchanged.
No compiler warning is reported. The 180-case domain lifecycle matrix supplements
existing real plugin storage/event tests; Combat subsets are not full endgame banks.

The 1.0 notice/history, first opening after dismissing 0.9.9, dismissal/restart,
header navigation and sidebar widths 204/225 are verified. The notice is visually
checked at both widths. The owner requested publication without another combined
in-game run of the final unpublished batch; no additional live-play test is claimed.

The exact original shield/defender sequence in #48 remains unconfirmed. Its known
Alch-stock portion was addressed in 0.9.7 and is retained here; related shield
placeholder regressions are not proof that the reporter's unspecified case is
fixed. The owner's requested tracker cleanup closes that remaining follow-up as
not planned pending a concrete reproduction, with an invitation to reopen.
Issue #50 is addressed by the targeted visage fix and will be closed completed.

## Review budget and publication decision

| Tokenizer / normalization | Local estimate |
| --- | ---: |
| cl100k / preserved | 193,240 |
| cl100k / collapsed | 196,053 |
| o200k / preserved | 193,584 |
| o200k / collapsed | **196,396** |

The highest leaves **3,604 estimated tokens**. The 195,000 planning target is
unmet; the owner requested publication after being shown this higher estimate.
This release retains several thousand estimated tokens, as required by AGENTS,
while the larger reserve remains a maintenance objective. No new unverified
runtime refactor was added for the purpose of reaching a round number.

Calibration remains maintainer 200,414 at
`def1e856e101ff0e57dd96adccab2cf1d886b076` (30 September 2026). Official
tokenizer, preprocessing and full scope remain unknown. Resources, tests, build
changes and docs were inspected separately. This is not an official count;
every Hub submission must remain strictly below 200,000 official tokens.

## Source, artifact and distribution

The release source is identified by Git tag `v1.0.0`. After committing, that exact
revision must pass the clean-clone build before being pushed/tagged for release.
The distributable JAR is built from that clean checkout, with its size and
SHA-256 recorded in the GitHub release body. ZIP timestamps and entry order are
fixed. The offline dependency resolved to cached RuneLite client 1.13.4 with
Temurin 11.0.31+11 / Gradle 8.7; this does not assert a cold-cache install or a
newer remote dependency version.

The artifact includes runtime facts, candidate/family tables and bundled notes.
It excludes test classes, derivation inputs/helpers, research caches, reports,
private raw exports and documentation. The seed TSV's empty final columns are
intentional and explicitly covered by `.gitattributes`.

The [GitHub release](https://github.com/PKoka5/bank-architect/releases/tag/v1.0.0)
and Plugin Hub request must point to the exact same source commit. GitHub
publication does not immediately update installed plugins: Hub availability
depends on upstream review/merge. Do not claim distribution until verified.

Logs: `build/tmp/release-1.0.0-final-build.log`,
`build/tmp/release-1.0.0-notice.log` and `build/tmp/release-1.0.0-size.log`.
The clean-checkout log and final artifact hash belong in the release publication
evidence alongside the exact source SHA.

Detailed evidence: [maintenance rehearsal](testing/maintenance-rehearsal-2026-10-26.md),
[visage fix](testing/visage-components-2026-10-31.md),
[Main diary policy](testing/main-diary-teleports-2026-10-26.md),
[lifecycle matrix](testing/finish-lifecycle-matrix-2026-10-26.md),
[runtime cleanup](testing/finish-runtime-cleanup-2026-10-26.md),
[review follow-up](testing/claude-review-follow-up-2026-10-25.md),
[remaining review](testing/remaining-review-2026-10-26.md),
[ammo labels](testing/ammo-group-tab-numbering-2026-10-25.md),
[wheel scrolling](testing/sidebar-wheel-2026-10-25.md) and
[kept tab moves](testing/accepted-tab-move-2026-10-26.md).

The supported maintenance scope is [MAINTAINING.md](../MAINTAINING.md): Main and
Ironman on eight columns, reproducible bugs, compatibility and reviewed item
corrections, without a release cadence or promised feature expansion.

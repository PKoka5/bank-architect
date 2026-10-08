# Bank Architect 0.9.1

Prepared on **8 October 2026** above published source
`a18e79723daf3fa3b501c8feb9b2d5ef19b3fa7c` (0.9.0).
The owner confirmed the in-game test passed and explicitly authorized publication.

## Player-facing changes

- Taking out preferred gear while leaving placeholders keeps established Gear/Alch
  placement during the same RuneLite session when logical item slots, captured facts
  and layout choices remain the same. Fresh placeholders cannot create new Alch
  replacement decisions. New/removed items or changed preferences trigger fresh
  decisions; captured bank blueprints remain the persistent owner-selected option.
- Vale offerings and spendable Golden nuggets, Numulite, Stardust, Tokkul and Trading
  sticks use Currency. Rada's blessings and Ghommal's hilts use Teleports; Western
  banner tiers distinguish utilities from teleports.
- Reviewed corrections cover Hitpoints capes, Ring of coins, Pearl barbarian rod,
  Hallowed token, Nature offerings, White pearl, lower Wilderness sword/Kandarin
  headgear tiers, Echo pearl and Golden tench. Higher diary tiers retain teleports.
- Unknown/unclassified items cannot enter automatic Alch solely from equipment
  stats, value, quantity and stronger gear. Explicit player corrections and captured
  destinations retain priority; quest review does not imply safe disposal.
- The sidebar shows the new bundled **What's new** message once after upgrading.
  Dismissal persists through reopening and restarting.
- The unused dense-only advisor was removed; live tab-aware manual guidance and its
  existing safety checks remain covered by tests.

All real bank moves remain manual. No runtime network, telemetry, game actions,
inventory/equipment reads or dependency changes were added.

## Development workflow and scope

The actual final-placement exporter, independently reviewed 85-ID reference and
offline changed-item tracker establish a repeatable review workflow. They are
development-only and excluded from the plugin jar. The current full export observes
65,170 isolated default-preset scenarios without runtime stats/prices/upgrades or
personal choices, with zero pipeline failures and 170 reviewed matches.

This is not complete current-catalog verification. Historical July identity evidence
remains visibly stale; missing or ambiguous gameplay facts stay open review work.
See the [classification audit](research/final-placement-audit-2026-10-06.md) and
[placeholder review](testing/placeholder-gear-stability-2026-10-08.md).

## Validation

- Final `gradlew.bat build --offline`: passed, **1,427 tests**, no failures, errors
  or skips, and **1,950 completed bank simulations**.
- All four explicitly reviewed simulation report hashes match. The classification
  audit documents the small expected deltas and reverse reproduction; baselines
  were not automatically regenerated to hide a failed check.
- **24 Main/Ironman placeholder regressions** cover withdrawal/return, complete
  BIS rows in Swap/Insert, quantities, new/removed items, settings/facts changes,
  physical duplicates and superseded/invalidated analysis work.
- Update-notice tests verify upgrade from dismissed 0.9.0, acknowledgement,
  reopening/restarting and future updates. The 204px sidebar render was inspected.
- Both PowerShell research-tool test suites and the 85-ID independent placement
  comparison pass. Read-only release review found no blocker.
- Existing Swing unchecked/unsafe-operation compiler note remains; Git LF/CRLF
  advisories concern line endings. `git diff --check` passes.
- Final jar: **268 entries**, no tests, simulators, tools, research caches, private
  bank dumps, documentation or obsolete advisor.

## Review-token budget

`npm.cmd run check --prefix tools/review-size` was run after the release-notice edits.
Calibration remains the maintainer's **200,414** count for
`def1e856e101ff0e57dd96adccab2cf1d886b076`, reported on 30 September 2026.

| Tokenizer / normalization | Local estimate |
| --- | ---: |
| cl100k, preserved whitespace | 195,531 |
| cl100k, collapsed whitespace | 196,451 |
| o200k, preserved whitespace | 195,596 |
| o200k, collapsed whitespace | **196,533** |

The highest estimate leaves **3,467 tokens of headroom** below 200,000. It is not
the official Hub count or a guarantee of eligibility. Record any new official count
with its exact source revision and recalibrate.

Outside the main-Java scope, bundled exact-ID/name-rule TSV changes, independent
test/reference fixtures, Gradle version/export/baseline changes and development
tools/docs were inspected separately. No raw cache or research tool is bundled.
The estimator is checked again on the committed release source.

## Artifact and publication

- Jar: `build/libs/ironman-bank-architect-0.9.1.jar`
- Size: **1,098,494 bytes**
- SHA-256: `da16c24572f07064b60f50f7516ead3cb26e49906de60cd957b5127395d115da`

Publication uses tag `v0.9.1` for this reviewed source and verified jar. The Hub
submission changes only `plugins/bank-architect` to the tag's actual source commit.
GitHub's release and Hub PR record the resulting source revision and publication
status; client distribution follows maintainer review and merging.

The current [Hub update instructions](https://github.com/runelite/plugin-hub/blob/master/README.md)
and [rejected-feature guidance](https://github.com/runelite/runelite/wiki/Rejected-or-Rolled-Back-Features)
were refreshed before submission.

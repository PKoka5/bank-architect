# Bank Architect 0.8.1

Keep a tab's own item order while retaining manual guidance for moving items
between tabs. Prepared on 2026-10-05 above released source
`2efa38004bfb779ff58c22e8b07b7d54813fbaef` (0.8.0).

## Player-facing changes

- **Open Blueprint > select a tab > Keep current order** accepts that tab's
  current item order, including Main. The guide still checks membership and
  helps the player move items into and out of it; other tabs retain their
  blueprint order.
- Completion, remaining move estimates and slot colors follow the same policy.
  Incoming items can be manually appended by dropping them on the tab header.
- The blueprint grid still shows its saved/automatic order; its status explains
  that guidance accepts the live order. Turning the option off restores normal
  guidance without deleting saved item positions or captured destinations.
- The working choice persists locally. **Tab Layout > Save as** includes it
  in a named layout; loading and sharing a layout preserves the choices.
- Opening the sidebar after upgrading shows the new bundled **What's new**
  message once. Dismissing it persists across reopening and restarting.
- Shared gear-family data and removal of duplicate work reduce allocations and
  review size without changing the existing placement rules.

Implements the per-tab ordering request in [issue #41](https://github.com/PKoka5/bank-architect/issues/41).
All real bank moves remain manual. No runtime resource, dependency, network,
telemetry, inventory/equipment read or game-action changes were added.

## Validation

- `gradlew.bat build --offline`: **passed**, **1,208 tests**, zero failures,
  errors or skips.
- **1,950 bank simulations completed**; all four existing baseline hashes match.
- **17 new regression tests** cover persistence and sharing, saved-order
  restoration, compressed tabs, Main, membership transfers, both rearrange
  modes, progress, reopening, stable kept-only reorders, duplicate placeholders,
  cache refresh, checkbox state and overlay colors.
- Release-notice tests verify upgrade from dismissed 0.8.0, acknowledgement,
  reopening and restarting. The rendered 204px notice and 560px/760px blueprint
  editor were visually checked.
- The owner confirmed the in-game test passed and explicitly authorized
  publication on 2026-10-05.
- Read-only release audit found no blocker. The existing sidebar compiler note
  about unchecked/unsafe operations and Git LF/CRLF advisories remain.
- Final jar contains **273 entries** and no tests, simulators, research tools,
  documentation or scratch files. `git diff --check` passes.

## Review-token budget

`npm.cmd run check --prefix tools/review-size` was run after the final release
notice edits. Calibration remains the maintainer's **200,414** count for source
`def1e856e101ff0e57dd96adccab2cf1d886b076` on 2026-09-30.

| Tokenizer / normalization | Local estimate |
| --- | ---: |
| cl100k, preserved whitespace | 196,425 |
| cl100k, collapsed whitespace | 197,232 |
| o200k, preserved whitespace | 196,553 |
| o200k, collapsed whitespace | **197,370** |

The highest estimate leaves **2,630 tokens of headroom** below 200,000. It is
not the official Hub count or a certification of eligibility. Record any new
maintainer count with its exact source revision and recalibrate.

Outside the main-Java estimate, changes are the Gradle version, four new tests,
updated notice tests, README and review/release documentation. These were
inspected separately. No bundled resource, dependency or simulation baseline
changes were added; tests, docs and review tooling are excluded from the jar.

## Artifact and publication

- Jar: `build/libs/ironman-bank-architect-0.8.1.jar`
- Size: **1,090,033 bytes**
- SHA-256: `f6acd9ff7d598d5012739a62a2bce3f7d46770f0db23cd91af908f9915826295`

Publication uses tag `v0.8.1` for this reviewed source and the verified jar.
The Plugin Hub submission changes only `plugins/bank-architect` to that tag's
actual source commit. GitHub's release and Hub PR record the resulting source
revision and publication status. Client availability follows maintainer review
and distribution. The estimator is checked again on the committed source.

The current [Plugin Hub update instructions](https://github.com/runelite/plugin-hub/blob/master/README.md),
[rejected-feature guidance](https://github.com/runelite/runelite/wiki/Rejected-or-Rolled-Back-Features)
and linked Jagex guidelines were refreshed before submission.

Supporting record: [Keep current order](reviews/keep-current-order-2026-10-05.md).

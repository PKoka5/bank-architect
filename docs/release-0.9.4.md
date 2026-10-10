# Bank Architect 0.9.4

Prepared on **10 October 2026** above published 0.9.3 source
`443d769c061cabe79522fbd620d9eb30343ab2f6`. The owner authorized publication
and adding this fix to the existing open
[Plugin Hub update 18226](https://github.com/runelite/plugin-hub/pull/18226).
The previously published 0.9.3 tag and release are retained.

## Player-facing change

While dragging the advised bank item through a long tab, the blue destination
stays visible as a direction badge when outside view, then highlights the
destination slot when it becomes visible. Scroll instructions point toward
DROP/TO while holding the item. Cancelling restores the source instruction.
This works with Swap and Insert, including canonical placeholder identities.

The release includes the Combat, ammunition grouping and placement improvements
documented in [0.9.3](release-0.9.3.md). Bundled update notes now identify 0.9.4
and include the scroll fix alongside those improvements, since the current Hub
revision is still 0.9.2. Dismissing the notice remains persistent.

All bank moves remain manual. The fix reads existing RuneLite drag observations;
it adds no input automation, game-state manipulation, dependency, runtime
network/telemetry or inventory/equipment reads. Route selection and live bank
validation remain unchanged.

## Validation

- Offline `build`: **1,631 tests**, zero failures, errors or skips; no
  compiler/test warnings. Git's LF/CRLF notices concern normalization only.
- **1,950 sorting simulations complete** (150 fixed and 1,800 aggregate).
  All four simulation fingerprints match 0.9.3 without changing baselines.
- **65 focused tests pass**, including 20 new actual-render regression cases
  covering both scroll directions, target visibility/clipping, cancellation,
  wrong drag identities, placeholders and stale-bank blocking.
- Independent production review reports no blockers. Generated overlay renders
  and the 204px-wide update notice were visually inspected. No live-client
  drag/scroll test is claimed.
- `git diff --check` passes. Resources, test-only API fixtures, documentation
  and the Gradle version change outside the estimator scope were inspected.
  No additional resources or development code are bundled.

Details: [drag/scroll regression review](testing/drag-scroll-guidance-2026-10-10.md).

## Review-token budget

Calibration remains the official **200,414** count for
`def1e856e101ff0e57dd96adccab2cf1d886b076`, reported on 30 September 2026.

| Tokenizer / normalization | Local estimate |
| --- | ---: |
| cl100k, preserved whitespace | 196,305 |
| cl100k, collapsed whitespace | 197,465 |
| o200k, preserved whitespace | 196,352 |
| o200k, collapsed whitespace | **197,532** |

The highest leaves **2,468 estimated tokens** below 200,000. These are local
main-Java estimates; the official Hub tokenizer, preprocessing and input scope
are unknown. Recheck the exact committed source before submission. A successful
build does not certify the official token count.

## Verified artifact and submission

- JAR: `build/libs/ironman-bank-architect-0.9.4.jar`.
- Size: **1,105,071 bytes**, **270 entries**.
- SHA-256: `5c4f3a2c8d0b100794864d4e4cad118ccb526beda020f998b21daa705c9801d4`.
- No tests, simulator/research classes, source files or generated render images
  are included in the JAR.

Publication uses tag `v0.9.4`. The GitHub release and the existing Hub pull
request record the exact source revision. The Hub update changes only
`plugins/bank-architect`; client distribution follows Hub review and merging.

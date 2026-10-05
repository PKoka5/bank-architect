# Keep current order — issue #41

Owner-authorized local implementation on 2026-10-05, based on
<https://github.com/PKoka5/bank-architect/issues/41>. Source base:
`2efa38004bfb779ff58c22e8b07b7d54813fbaef` (published 0.8.0).

## Player behavior

- Open Blueprint exposes **Keep current order** for the selected logical
  destination, including Main and empty destinations.
- Guidance still checks tab membership and advises manual transfers. Normal
  drops on a tab header append incoming items. The kept tab's internal order
  never needs to match the automatic or saved order.
- Other destinations still require their usual order. Completion, remaining
  move estimates and green/amber slot validation use the same effective order.
- The grid continues to show the saved/automatic arrangement; its status
  explicitly explains the live-order policy. Disabling the checkbox restores
  normal guidance without deleting manual positions or captured destinations.
- Working choices persist locally. Save as includes them in a named layout;
  loading a layout restores its choices. Share codes include these flags.
- All game item movements remain manual.

## Implementation and review

`BankLayoutPlan` stores a ten-bit destination mask using a reserved `~keep`
marker within each serialized destination. Legacy layouts have no flags;
older readers ignore the marker as an unknown tag. Existing profile/share
parsers split their `~` separators with limits, so the marker stays within the
plan field. Tag moves and completion retain the mask. Profile matching compares
the completed serialized plan, including the mask.

`BankTabPlan` retains stable session identity and projects observed order for
kept destinations. An occurrence queue preserves quantities, placeholders and
the individual entries when IDs repeat. Projection results are cached against
copies of the live IDs and tab counts; layouts with no flags reuse their
original flattened list. Membership checks still precede guide sorting.

The overlay shares this projection and reuses its existing item-membership map.
Session transition checks remain active. After a stable bank tick, deliberate
reordering confined to kept sections is accepted only when numbered tab counts
match the planned structure, each section retains its members and all other
sections stay identical.

For review headroom, standard same-package import groups were consolidated in
the panel, plugin and preview builder. Explicit `java.util` imports retain
unambiguous List/Timer resolution. Gear-family definitions are now cached
directly, the sorter reuses its family-size map, and an unused card-header
branch was removed. These changes remove duplicate scans and allocations.

## Verification

- `gradlew.bat build --offline`: passed, **1,208 tests**, zero failures/errors/skips.
- **17 new regression tests** cover persistence and sharing, saved-order
  restoration, compressed tabs, Main, incoming/outgoing routing, both rearrange
  modes, progress, reopening, kept-only manual transitions, duplicate
  placeholders, cache refresh, checkbox state and overlay colors.
- **1,950 bank simulations** completed; all four fixed report hashes match.
- The 760 px and 560 px editor renders were inspected; the checkbox and full
  explanation fit. Images: `build/reports/keep-current-order/editor.png` and
  `editor-narrow.png`.
- Existing sidebar unchecked/unsafe compiler note remains. `git diff --check`
  passes; Git's LF/CRLF notices are line-ending advisories.
- No bundled resource or dependency changes. Development-only reflection in
  the overlay regression test is excluded from the plugin jar.

The owner confirmed the in-game test passed and explicitly authorized publishing
on 2026-10-05. The [0.8.1 release record](../release-0.8.1.md) records the final
release validation, artifact and publication details.

## Review-token estimate

`npm.cmd run check --prefix tools/review-size` calibrates against maintainer
count **200,414** for `def1e856e101ff0e57dd96adccab2cf1d886b076`.

| Tokenizer | Whitespace | Estimated total |
|---|---|---:|
| cl100k | Preserved | 196,468 |
| cl100k | Collapsed outside literals | 197,271 |
| o200k | Preserved | 196,596 |
| o200k | Collapsed outside literals | **197,409** |

Highest local estimate leaves **2,591 tokens** below 200,000. This is an estimate
of main-Java changes, not the official Hub count. The four new test files and
README/review documentation are outside that estimator's scope and were
inspected separately; no runtime resource or dependency changes were added.
Rerun the estimator and outside-scope audit before any later publication.

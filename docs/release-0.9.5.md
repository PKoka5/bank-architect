# Bank Architect 0.9.5

Prepared on **11 October 2026** above published 0.9.4 source
`4c513e7fd560fd9846c327dc0daab43c9124017e`. The owner authorized publishing
the sidebar shortcuts before developing the separate bank-preview feature.
[Plugin Hub update 18226](https://github.com/runelite/plugin-hub/pull/18226)
for 0.9.4 was merged on 11 October, so this release uses a new Hub submission.

## Player-facing changes

Three header icons provide quick access to:

- **Updates:** eleven bundled release entries with a version selector.
- **Settings:** Bank Architect's RuneLite configuration page.
- **GitHub issues:** the repository's new-issue page in the player's browser.

History keeps the current blueprint, preset and guide intact. Reading it does
not dismiss an unseen update notice. On the first sidebar opening after this
update, **What's new** identifies 0.9.5; **Got it - continue** acknowledges it
locally for this version. All bank item moves remain manual.

To preserve review-token headroom, repeated preset and tag declarations moved
into two local UTF-8 resources. Existing keys, names, order, routes, presets
and saved-blueprint behavior are retained. No new dependency was added.

## Validation

- Offline Java 11 `build`: **1,637 tests**, zero failures, errors or skips.
- **1,950 sorting simulations** complete; all four previous baselines match.
- **64 focused tests** pass. Compatibility coverage pins five preset identities,
  fifty ordered categories, thirty tags, forty-one routes and ten catch-alls
  against pre-migration behavior.
- Independent source reviews found no blockers. Header, history and notice
  renders were visually checked with RuneLite's dark look and feel at 204 and
  225 pixels. No live-client settings/browser test is claimed.
- `git diff --check` passes. The focused compilation retained the existing
  sidebar unchecked-operation note; the final build added no warnings.
- New resources, tests, docs and version changes outside the main-Java estimator
  were inspected. No development code or fixtures are included in the JAR.

Details: [sidebar action review](testing/header-actions-2026-10-11.md).

## Review-token budget

Calibration is the official **200,414** count reported on 30 September 2026 for
`def1e856e101ff0e57dd96adccab2cf1d886b076`.

| Tokenizer / normalization | Local estimate |
| --- | ---: |
| cl100k, preserved whitespace | 195,466 |
| cl100k, collapsed whitespace | 196,671 |
| o200k, preserved whitespace | 195,612 |
| o200k, collapsed whitespace | **196,837** |

The highest leaves **3,163 estimated tokens** below 200,000. These are local
main-Java estimates; the official Hub tokenizer, preprocessing and input scope
are unknown. Recheck the exact committed source before submission. A passed
build does not certify the official token count.

## Verified artifact and submission

- JAR: `build/libs/ironman-bank-architect-0.9.5.jar`.
- Size: **1,112,420 bytes**, **277 entries**.
- SHA-256: `ac71d8b2cba49c9d97b82c07c7e96bd868dc19b0559a844ca15eedbcfc0c5e77`.
- Six new bundled resources: three original header glyphs, release history and
  the two declaration tables. No test, research, simulator or render artifacts.

Publication uses tag `v0.9.5`. The GitHub release and new Hub pull request record
the exact source revision. The Hub submission changes only
`plugins/bank-architect`; RuneLite distribution follows Hub review and merging.

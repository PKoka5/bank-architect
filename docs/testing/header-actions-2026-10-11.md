# Sidebar header actions - 2026-10-11

Release 0.9.5, based on published 0.9.4 source
`4c513e7fd560fd9846c327dc0daab43c9124017e`.

## Behavior

- Updates opens eleven bundled release entries, 0.6.0 through 0.9.5, with a
  version selector. Reading history preserves the preset, blueprint and guide.
  Back returns to an unseen update notice until **Got it - continue** is clicked.
- Settings posts RuneLite's public `OverlayMenuClicked` configuration event for
  this plugin's overlay. RuneLite handles navigation to its configuration page.
- GitHub issues uses RuneLite `LinkBrowser` to open
  `https://github.com/PKoka5/bank-architect/issues/new` directly after a click.
  **Copy Blueprint Export** remains available in the blueprint dialog for players
  who want to include their layout in a report.
- All three actions remain available in planner, notice and history views.
  Original 16-pixel glyphs and release text are bundled locally.

## Validation

Final Java 11 offline `build`: **1,637 tests**, zero failures, errors or skips.
All **1,950 bank simulations** completed and four simulation baselines matched.
The final focused run passed **64 tests**, including header, notice, preset,
tag, mapper and declaration-compatibility coverage.

Compilation reported the existing unchecked-operation note in the sidebar
panel during the focused run. The final build emitted no additional warnings.
`git diff --check` passes; Git's LF/CRLF notices concern normalization only.

Swing renders with RuneLite's dark look and feel were visually inspected at
204 and 225 pixels. Buttons, version selector, notes and return control fit.
The render tests restore the previous look and feel afterward. These renders
and callback tests do not establish live-client configuration or browser behavior.

The JAR contains **277 entries**. Its six new resources are three original PNGs
(245, 417 and 301 bytes), release notes (4,752 bytes), preset declarations
(4,016 bytes) and tag declarations (1,622 bytes). No test fixtures, development
scripts, research classes, simulator classes or generated renders are bundled.

## Declaration compatibility and review size

Repeated preset and tag declarations moved from Java into two ordered UTF-8
tables. Runtime sorting and classification algorithms are unchanged. Tests
compare public behavior against a fixture captured from the pre-migration
classes: all five preset identities, fifty ordered categories, thirty tags,
forty-one explicit subcategory routes and ten catch-all routes match.

The estimator is calibrated against source
`def1e856e101ff0e57dd96adccab2cf1d886b076`, reported as **200,414 tokens** on
30 September 2026. Final estimates are 195,466 / 196,671 (`cl100k_base`,
preserved/collapsed whitespace), and 195,612 / **196,837** (`o200k_base`).
The highest leaves **3,163 estimated tokens** below 200,000. This is a local
main-Java estimate, not the official Hub count. Resource data, tests, docs and
version changes were inspected separately. See [release record](../release-0.9.5.md).

For future releases, update the Gradle version, `RELEASE_ID`, ordered `versions`
key, new history entry and latest `notice` together. Recheck the exact source
before submission and preserve several thousand tokens of headroom.

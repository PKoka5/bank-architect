# Temporary blueprint preview - 2026-10-11/12

Local development after published source
`8808c84cbf9433f9c76f6b045df2fcd18e9da0b0` (0.9.5).
No version, release notice, published artifact or Hub manifest changes belong
to this prototype.

## Design and interaction

Opening the blueprint with the real bank open temporarily draws the blueprint
over the bank item viewport. Its eight-column arrangement preserves
physical duplicates, placeholders and planned empty slots. Draft edits publish
copied full-bank snapshots across the Swing/client-thread boundary; Undo and Cancel update
the preview, while only Save persists the draft.

The renderer only reads public bank widgets and draws an opaque clipped layer.
It never changes native widget children, positions, scroll offsets, item slots
or bank state. Item images reuse the plugin's existing bounded image cache.
The 12 October revision removes the separate dark panel, outlined cells, heading
and footer from the viewport. Explanation stays in the blueprint window.
The renderer reads the native bank frame's containing graphic or filled backing:
textures tile from that widget's canvas anchor or scale over its original bounds.
Public widget/general sprite overrides are respected and their converted images
use a bounded sixteen-entry cache, cleared on shutdown.

Item images retain their native 36 by 32 pixels. Column spacing comes from live
bank item positions, with a 48-pixel fallback when too few columns exist. Native
row spacing normally uses 36 pixels; gaps between tabs cannot inflate it.
The item layer's canvas origin is read separately from its scrolled child
positions before applying the preview's own pixel offset. Partial rows clip at
the viewport edge. Empty slots have
no drawn border and placeholders use their actual game definition, without
additional invented transparency.

The owner's in-game screenshots exposed an incorrect viewport assumption:
`ITEMS_CONTAINER` includes the native controls, so painting its entire rectangle
covered the tab row, scrollbar, Bank Tags and bottom buttons. The corrected
paint rectangle derives its vertical limits from the visible `TABS`,
`SCROLLBAR` and `BOTTOM` widgets, its right edge from the scrollbar and its
left edge from the native first item column. The whole-bank `UNIVERSE` remains
only the input-exit hitbox. Missing or inconsistent control geometry closes
preview rather than painting across controls. The item layer owns native
scrolling; the preview reads its bounds without changing its scroll state.
The category and guidance overlays pause drawing during preview and retain
their existing preferences and sorting session.

- The bank's native View tab and View all items actions remain usable while
  preview is active. The renderer reads `BANK_CURRENTTAB`; it never sets a
  varbit or generates a native tab action. Numbered views follow the existing
  dense physical-tab plan, skipping empty blueprint destinations. A native tab
  without a corresponding planned tab displays an empty preview.
- Main / All items draws the Main section first, followed by every planned
  numbered tab, each starting on a fresh row with a separator above it. Row
  padding does not invent bank items. The separator color comes from a native
  line when available, and a detected row gap supplies its spacing. The half-row
  fallback is an approximation that still needs an in-game spacing check.
- Wheel input over the bank grid scrolls preview pixels only, with independent
  offsets for each native view; wheel events elsewhere in the covered bank are
  consumed without changing the native bank scroll position.
- The first bank press outside a validated native tab view ends preview and
  consumes the complete gesture, even
  if its drag or release finishes outside the bank. The next bank action works
  normally. The checkbox can resume preview while the blueprint stays open.
- Hitboxes exist before the first rendered frame and remain guarded between
  tab updates. A stale frame consumes input without scrolling the new view.
- A left press on a validated native tab passes through, including its release,
  so ordinary tab clicks keep preview active. Up to five pixels of pointer
  movement tolerate click jitter. A larger tab drag ends preview and passes
  the player's manual gesture through. Collapse tab, New tab and item actions
  are excluded from the menu allowlist.
- Other public bank menu actions, bank closure, logout, an already open menu or
  held/dragged mouse input end preview. Unsafe or undersized viewport geometry
  also returns to the real bank. Eight sprite columns and at least one complete
  native row must fit. Missing item geometry or backing data closes preview.
- Blueprint closure and plugin shutdown remove preview. Shutdown unregisters
  both input listeners; queued work checks overlay/panel identity across restart.
- New analysis, changed editing context and a pending save withdraw stale
  draft previews. Ending preview alone does not discard an unsaved draft.

Input listeners append through RuneLite's MouseManager so Stretched Mode's
coordinate translation precedes them. No generated input, packets, external
processes, network calls or inventory/equipment reads were introduced.

## Research boundaries

High-level source research found that Bank Templates' applied layouts reposition
client-side bank widgets and preserve working withdrawal interactions. Its
separate preview window is not itself equivalent to applying a layout. Sources
at the Hub revision examined:

- [BankLayoutRenderer](https://github.com/TheSpryt/bank-templates/blob/0fef80d11b24262ef8c190782ace2241e2e477f6/src/main/java/com/banktemplates/BankLayoutRenderer.java)
- [TemplatePreview](https://github.com/TheSpryt/bank-templates/blob/0fef80d11b24262ef8c190782ace2241e2e477f6/src/main/java/com/banktemplates/TemplatePreview.java)
- [TemplateManager](https://github.com/TheSpryt/bank-templates/blob/0fef80d11b24262ef8c190782ace2241e2e477f6/src/main/java/com/banktemplates/TemplateManager.java)

Bank Templates' editor opening itself does not apply a template. The active
applied layout draws through native game item widgets. Built-in Bank Tags also
uses native item widgets for its layouts, explaining the normal bank appearance:
[LayoutManager](https://github.com/runelite/runelite/blob/42a6f17a6a2e8e478aa763890ecd0181a59dad38/runelite-client/src/main/java/net/runelite/client/plugins/banktags/tabs/LayoutManager.java).
Native item dimensions come from the public RuneLite
[Constants](https://github.com/runelite/runelite/blob/42a6f17a6a2e8e478aa763890ecd0181a59dad38/runelite-api/src/main/java/net/runelite/api/Constants.java).

This repository implements its own draw-only preview. No third-party code,
resources, UI, layout or configuration were copied or adapted.

## Validation

Automated overlay tests use strict public API proxies that reject unexpected
bank reads or mutations. Thirty-one renderer/input tests cover clipping, native
texture anchoring and scaling, solid backings, resizing, item-layer origin,
single-item fallback, native tab section gaps, dense tab mapping, Main-first
All views, empty Main, native placeholders, duplicate slots,
initial/update hitbox races, whole-gesture
consumption, native tab clicks/drag jitter/menu allowlists, per-view scroll
and lifecycle exit conditions. Editor integration tests exercise
the actual controls for tab selection, Swap/Insert, Undo/Cancel/Save, queued
snapshot isolation and stale draft rejection.

The screenshot regression fixture deliberately makes the outer bank container
larger than the item band and paints distinguishable native controls before
preview. Every pixel outside the independently expected paint area must remain
identical, including the tab row, Bank Tags, scrollbar, bottom controls and frame.
Control rectangles must also remain unchanged. Nine missing/hidden/invalid
control cases close without painting or retaining the input guard. The strict
outer-container proxy rejects `getScrollY()`; only the item layer owns scrolling.

Generated normal, resized, tiled and scaled renderer images are under
`build/reports/bank-preview/`. Their test icons establish geometry and clipping,
not the final appearance of real RuneLite item images.
`preserves-chrome.png` shows the colored-control regression fixture.
`native-all-sections.png` shows section padding and native separator placement.
Both fixtures were visually inspected after the final clean build.

The repeated Alch declarations moved to `organize/alch-candidates.tsv` and six
ordered teleport name groups were appended to `catalog/classification-names.tsv`.
Resource-rule construction and tool/outfit family handling share existing code.
These changes preserve classification and sorting behavior: a frozen fixture
checks all 65,536 candidate IDs, 12 elemental staff IDs and 1,811 role replacement
cases; the original 81 name groups and the six extracted lists were checked
against the prior source. No simulation baselines were regenerated.

The unused first-generation block/slot prototype was removed from the shipped
source to restore review-token headroom. Fourteen model, matcher, mapping and
preset files moved byte-for-byte to test sources; their original tests remain
executable. A test-only `LegacyBankGuideController` preserves historical block
and match contracts. The production controller now retains only the active
analysis, guide-progress, bank-lifecycle and category-assignment paths. Its
unused prototype constructor, block/match getters and text APIs were retired;
no current production consumer used them. Runtime tests use the production
no-argument controller, including stale progress reset after bank closure.
The final clean build verified that no prototype classes remain in the plugin JAR.

Final 12 October offline Java 11 clean `build`: **1,675 tests**, zero failures, errors or skips.
All **1,950 bank simulations** completed and the four existing report hashes
matched. The native geometry, background and input tests all pass. Compilation
reported the existing unchecked-operation note in the sidebar panel. The final
build emitted no additional warnings. `git diff --check` passes; Git's LF/CRLF
messages concern normalization only.

The local JAR contains **264 entries**, **1,105,961 bytes**, SHA-256
`c02348c3f63b98307ca308585dd30d45789afdaaf4446cca1276eb06f6ff7232`.
Both package-relative tables are present: classification names (8,202 bytes)
and Alch candidates (817 bytes). Compatibility fixtures, development helpers,
research/simulator classes, generated images and docs are absent from the JAR.
Its development filename still uses 0.9.5; it must not replace the published
0.9.5 artifact.

## Review-size estimate and future release constraint

The estimator uses the maintainer-reported **200,414 tokens** for source
`def1e856e101ff0e57dd96adccab2cf1d886b076` on 30 September 2026.
Final totals are 193,953 / 195,342 (`cl100k_base`, preserved/collapsed whitespace)
and 194,126 / **195,532** (`o200k_base`). The highest leaves **4,468 estimated
tokens** under 200,000. This measures main Java only, not the official Hub
tokenizer or full review input. Resources, tests and documentation were inspected
separately; no official count for this working tree is available.

The current estimate provides several thousand tokens of headroom. Rerun the
estimator on the exact source before a future submission. No Hub publication
or token-limit certification is part of this local prototype. Update the
version and release history together only when preparing its own release.

## In-game check still needed

1. Analyze with the bank open, then open Blueprint and move its window so the
   bank remains visible. Click the bank's own numbered tabs and Main / All
   items. Confirm preview remains on and Main shows all planned tab sections
   in order, starting each section on a fresh row. Check an empty Main and
   a native numbered tab with no planned counterpart.
2. Check real item icons, quantity labels, placeholders and vertical spacing.
   Verify the bank's actual frame descendant supplies the texture, including
   interface styles, and check native scroll alignment at top/middle/bottom.
   Confirm the normal tab row, left Bank Tags, scrollbar, frame and bottom
   controls remain visible at different bank sizes.
3. Scroll preview, switch tabs and return; verify separate preview offsets are
   restored. Toggle it off and verify native bank scrolling was not changed
   by preview wheel events.
4. Click an item once to exit; verify no withdrawal or item drag occurred.
   The next deliberate bank click should work normally.
   Ordinary native tab clicks should switch view and retain preview; a manual
   tab drag should end preview and follow the real bank's normal behavior.
5. Edit a draft, Undo and Cancel; verify preview follows without persisting edits.
6. Close/reopen bank and blueprint, restart the plugin, resize and enable
   Stretched Mode. Verify preview exits cleanly and guidance can resume.

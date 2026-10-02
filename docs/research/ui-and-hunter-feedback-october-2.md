# UI and Hunter feedback — 2026-10-02

Scope: clearer existing controls, category assignment from the blueprint, and the
user's Hunter/cosmetic placement feedback. Website and YouTube-comment proposals
remain outside this change.

## Changes

- Sidebar: labelled Blueprint, Sorting Guide and Assign Categories actions, visible
  On/Off state, and three short workflow steps. Text/layout widths accommodate the
  scrollbar in the 225px sidebar.
- Blueprint: select an item in Edit mode, choose Assign category, then Save. Category
  changes share existing per-occurrence destination persistence, Undo and Cancel.
  Choosing the current category is a no-op; stale previews cannot save changes.
- Filled butterfly/moth jars use Potions; the empty jar uses Hunter tools. All pouch
  sizes/open states and Huntsman's kit use the Hunter tools group.
- Exact entries from the existing cosmetic outfit/family catalog receive the
  Cosmetics tag, including when Clues and Cosmetics have different destinations.
  Combat equipment outside that catalog remains governed by its existing rules.
  Specific additional cosmetic item reports can extend the catalog later.
- Nine legacy registry name-rule groups moved verbatim to a bundled TSV, preserving
  group/needle order and the whole-word `herb` exception. The plugin reads this local
  resource only. Missing/malformed data blocks analysis rather than guessing.

## Validation and reviewed simulation delta

A temporary before/after comparison checked all 33,742 registry entries and found
identical category/subcategory pairs for the name-rule extraction alone. The
temporary test was removed after comparison. Permanent tests cover malformed rules,
Hunter placement, all catalogued cosmetic outfits, same-tab/cross-tab assignment,
Undo/Cancel, stale contexts and saved-route round trips.

The original HEAD was built in `tmp/ui-feedback-baseline` for an exact aggregate
comparison. Exactly ten rows (42 occurrences) leave Cleanup; no rows are added or
otherwise changed:

| Item ID | Item | Occurrences |
| --- | --- | --- |
| 1055 | Blue halloween mask | 6 |
| 10012 | Butterfly jar | 6 |
| 29295 | Small meat pouch | 6 |
| 29462 | Small meat pouch (open) | 6 |
| 1053 | Green halloween mask | 3 |
| 1057 | Red halloween mask | 3 |
| 10014 | Black warlock | 3 |
| 10018 | Sapphire glacialis | 3 |
| 29301 | Medium fur pouch | 3 |
| 29309 | Huntsman's kit | 3 |

Cleanup coverage changes from 9,335 distinct IDs / 46,731 occurrences to 9,325 /
46,689. All 150 random-bank and 1,800 aggregate scenarios complete. The single-run
report and cleanup hashes remain unchanged. Only the reviewed aggregate cleanup
and metadata hashes are updated in `build.gradle`.

## Manual check before release

1. Open a bank, Analyze, open Blueprint and enter Edit mode.
2. Select an item, assign another category on the same tab; test Undo and Cancel.
3. Assign a category on another tab, Save, close/reopen Blueprint, then reopen bank.
4. Confirm manual sorting guidance follows the saved destination.
5. Check filled jars beside potions/moths, all Hunter tools/pouches together, and
   cosmetics on a separate Cosmetics destination.
6. Check sidebar buttons and blueprint footer at the user's window size.

Final local validation: 1,105 tests pass; `test build` passes including all 1,950
simulations and the reviewed reference hashes. Swing renders were inspected at
225px (sidebar) and 760px (blueprint). The compiler still reports the existing
unchecked/unsafe-operation note in the sidebar class.

The four main-Java token estimates are 197,000 / 197,486 / 196,989 / 197,482.
Use the highest, **197,486**, with an estimated margin of **2,514**. New TSV data is
bundled local data, and the migration was compared across every registry item;
tests, resource changes and two verification hashes were reviewed separately.

No live in-game verification has been performed for this change. Local review-token
estimates exclude resources/tests and are not an official Hub count. Re-run the
estimator and inspect all excluded files before publishing.

## Sidebar art direction follow-up

The user confirmed the functional changes in game and requested a bank-workbench
visual direction. Original Swing painting in `WorkbenchStyle` gives primary
actions bronze material, secondary actions dark metal, and explicit hover,
pressed and disabled states. The sidebar uses warm charcoal, parchment text and
a blue instruction card. No artwork or implementation was copied from another
plugin. Obsolete icon-only button drawing was removed.

The instruction card displays the full guidance text, rather than truncating to
the first sentence. Introductory steps disappear after analysis. The card keeps
its natural height; Details stays at the bottom on tall windows, and short
windows scroll. Renders at 225x400 and 225x800 were inspected. This first sidebar
pass does not yet add textures or a large next-item sprite, or restyle the
blueprint editor. Remaining tall-window space is deliberately unfilled pending
a useful bank-overview design.

`test build` passes, including all 1,950 simulations and baseline checks. The
existing unchecked-operation compiler note remains. Latest estimates are
197,551 / 198,003 / 197,554 / 198,012: highest **198,012**, margin **1,988**.
This is below the numerical ceiling but tighter than the requested several
thousand tokens of headroom; recover more margin before publishing or adding
further Java UI features. Live verification of this visual pass is still pending.

## Bank overview and blueprint styling follow-up

The sidebar overview now uses two columns of larger destination tiles, showing
category captions, tab identity and counts. Long captions are shortened visually;
the tooltip retains the full name. Main actions precede the overview so they are
reachable on short windows. The blueprint editor shares the original workbench
buttons, bronze tab borders, dark item wells and blue work surface. Its movement
controls occupy a second footer row, and status text wraps. Green item selection,
Swap/Insert, drag handling and Save/Cancel semantics are retained.

76 literal name groups from the classification refiner and four item sorters now
live in `classification-names.tsv`. Rule positions and matching operators remain
in Java. The loader rejects missing, malformed, reindexed or incomplete tables;
callers receive cloned arrays. The resource is bundled local data, not downloaded
or generated at runtime. Future table edits must preserve existing numeric keys.

Before/after classification exports contain 32,586 effective records and 606
excluded cache records and are byte-identical (SHA-256 checks):

- Effective: `3cd8e32ec7a1b7e837c011bbcc70a5e6bea163052fda8b60ca067c2a4fae439b`
- Excluded: `46221bc136e653b625b293e5e32e66d1128a988456ded1e1254760a437b3ce99`

All tests and `test build` pass, including 1,950 bank simulations and all four
unchanged baseline hashes. Both 560px and 760px editor renders were inspected;
the narrow render asserts every action button fits. Sidebar width checks pass.
The existing unchecked-operation compiler note remains; no new live client
verification was performed. The new resource and jar inclusion were inspected
separately from the estimator's Java-only scope.

Final estimates: 196,366 / 196,812 / 196,433 / **196,887**. Highest estimated
margin: **3,113** tokens below 200,000. Official Hub scope/count remains unknown.
These local changes have not been committed or published.

## Final direction: neutral RuneLite UI

The user subsequently chose a neutral RuneLite appearance. This supersedes the
workbench styling described above: `WorkbenchStyle` and its custom button/tab
delegates are removed. Sidebar and editor use RuneLite `ColorScheme` colors and
standard Swing controls, inheriting the application's look-and-feel. Layout
improvements, category tiles/counts, wrapped guidance, green item selection and
the two-row editor footer remain. The neutral renders were inspected at both
sidebar heights and editor widths; live RuneLite verification remains pending.

All 1,107 tests, 1,950 simulations and the build pass. The existing unchecked
compiler note remains. Highest estimated review size is **196,165**, giving
**3,835** tokens of estimated headroom and saving **722** versus the final
workbench pass. This is a local Java-only estimate, not an official Hub count.

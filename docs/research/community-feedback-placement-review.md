# Community placement review — 2026-09-15

## Reproduced and corrected

### Platinum tokens assigned to Frequently Used

A synthetic bank with coins (995), hammer (2347), platinum tokens (13204), law
runes (563), house teleports (8013) and Tokkul (6529) reproduced the report.
The stored override `13204=frequently-used` correctly selected the tag and tab,
but the default main sorter still placed platinum after the teleport. The
pre-fix main order was `[995, 2347, 563, 8013, 13204]`.

The effective layout tag now travels with each preview item, including physical
duplicates and placeholders. Main sorting respects a changed tag's role;
retagged items are excluded from their original automatic geometry. Unchanged
items retain their existing rules. Original catalogue classification remains
available for research and family identification. The tooltip displays the
effective tag and text exports distinguish it as `layoutTag`.

Tests cover Grid/List, save/load of overrides, moving the tag to a different tab,
reset to automatic currency placement, physical expansion, and explicit tag order.
The existing right-click "Use automatic classification" action already clears
one override; no second clear action was needed.

This fixes tag-based placement. Arbitrary coins-first/tokens-second positioning
belongs to the planned blueprint editor, not to a new forced currency default.

### Mystic versus black wizard hat

Exact registry ID 1017 is the ordinary black Wizard hat (`BLACKWIZHAT`). It had no
curated tier, so the name fallback gave it 500 points. Mystic hat 4089 had tier 2
(400 points), incorrectly placing the starter hat first even after both were
assigned to gear. This was reproduced in the full preview builder.

Added 1017 at the same starter tier as the already catalogued blue wizard hat
579. The tier is a planner decision; the supporting equipment progression is
consistent with the [Wiki magic-armour comparison](https://oldschool.runescape.wiki/w/Armour/Magic_armour)
and [Mystic robes stats](https://oldschool.runescape.wiki/w/Mystic_robes), consulted
September 15: wizard headwear has +2 magic attack, mystic +4. The precise ID and
name come from the bundled item registry. No runtime lookups were added.

The regression covers both absent stats and representative magic-headwear stats.
This is an ordering fix; the separate report that a hat originally landed in
boss loot is not reproduced by this case and remains subject to live evidence.

## Existing behavior verified

- Crystal body versus ancient d'hide: existing set-cohesion tie-break test passes;
  a stronger loose item can still win under Best in slot, as designed.
- Blood/Blue/Eclipse Moon tiers and Blue Moon columns: existing regressions pass.
  Added a full three-set List check with real IDs and head/body/legs order.
- Bandos platebody 12480 and heraldic rune platebody 23209 follow a manual Clues
  override; Bandos chestplate 11832 retains its normal gear destination.
  No blanket reclassification of clue rewards was introduced.

These are synthetic regression cases, not the original reporters' full banks.
The maintainer will check in game after the planned implementation work. Layouts
that intentionally arrange best-in-slot columns need not keep every set together;
Sets together and List are the relevant alternatives for that preference.

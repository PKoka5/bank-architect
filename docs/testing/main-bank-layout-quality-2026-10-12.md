# Main bank layout quality — 12 October 2026

Local development after published source
`8808c84cbf9433f9c76f6b045df2fcd18e9da0b0` (0.9.5), alongside the tested bank
preview. No release, version change or Hub submission is included.

## Evidence and scope

Two player-provided Main exports were each truncated at 50,017 bytes. The first
contains 142 complete Combat entries out of 258 reported items; the second contains
all 194 Combat items. Neither contains usable potion entries. The regression
fixture stores the second Combat section's public item IDs, names, category,
subcategory and placeholder flags, with private quantities normalized to 0/1.
Layout tests use explicit synthetic role vectors to isolate placement policy;
the exports do not contain live equipment statistics or saved preferences.

The previous logical `bySet` order grouped families by style and strength, but
physical semantic placement broke ties using alphabetical family identities.
This put weaker families ahead of stronger ones and mixed unrelated styles.
Loose alternatives also sorted equipment-slot first across every style.

## Changes

- Combat-specific ordered semantic requests encode the existing sorter's family
  preference in deterministic rule identities. The generic layout packer is
  unchanged. Both style columns and the alternative set view use that order.
- Best-owned style columns and cannon grouping remain. Remaining eligible set
  pieces form vertical columns; a piece selected for a best-owned setup keeps
  its front position. Loose remainder groups by style, slot and score. Spare
  equipment rows are anchored only in full rows without secondary families,
  keeping scarce column space available for sets and retaining tiny-bank order.
- Shayzien 5 and Elite black are exact vertical families. The original 78 gear
  family rows retain their original member order and fingerprint.
- Main/BY_FAMILY supplies calculate distinct owned dose masks from exact metadata.
  Complete 4–3–2–1 families lead, including real placeholders. Quantity, repeated
  variants and lookalike names cannot fake completeness. Area-restricted variants
  count separately. Default Ironman grab-area and Herblore recipe rules remain.
- Complete potion pairs naturally occupy columns 1–4 and 5–8. An odd complete
  region borrows four real food/utility singles to finish its last row, starting
  incomplete doses below it. Variant families and physical duplicate stacks
  cannot be borrowed as singles. With fewer than four suitable fillers, the
  remaining half-row contains the start of the incomplete section. Explicit
  sequential layouts retain their unnudged order. No placeholders are invented.
- Tablet colors use a disjoint exact-ID resource, retaining alphabetical order
  within each color and existing rune, device, jewelry and scroll roles. Unknown
  tablets follow reviewed colors; canonical placeholders use the same ordering.
- Exact non-combat corrections remove water bowls, the marble magic wardrobe,
  Amulet of chemistry, camouflage clothing and Amulets of the eye from automatic
  Combat classification. Player corrections and saved physical orders still win.

## Primary source checks

Tablet grouping is based on actual official RuneLite cache sprites, rather than
assuming a spellbook implies a color:

- Blue: [Varrock](https://static.runelite.net/cache/item/icon/8007.png),
  [house](https://static.runelite.net/cache/item/icon/8013.png),
  [redirected house](https://static.runelite.net/cache/item/icon/11743.png),
  [Teleport to boat](https://static.runelite.net/cache/item/icon/31443.png).
- Light blue: [Moonclan](https://static.runelite.net/cache/item/icon/24949.png).
- Purple: [Annakarl](https://static.runelite.net/cache/item/icon/12775.png),
  [Target](https://static.runelite.net/cache/item/icon/24336.png),
  [Summon boat](https://static.runelite.net/cache/item/icon/31441.png).
- Green: [Arceuus library](https://static.runelite.net/cache/item/icon/19613.png),
  [Barrows](https://static.runelite.net/cache/item/icon/19629.png), and adjacent
  muted-green [Wilderness crabs](https://static.runelite.net/cache/item/icon/24251.png).
- Remaining reviewed colors: [orange Volcanic mine](https://static.runelite.net/cache/item/icon/21541.png)
  and [pink Wise old man](https://static.runelite.net/cache/item/icon/21863.png).

The [Amulet of the eye](https://oldschool.runescape.wiki/w/Amulet_of_the_eye)
supplies unlimited teleports and no combat bonuses. The
[skilling equipment table](https://oldschool.runescape.wiki/w/XP_clothing)
identifies Amulet of chemistry as Herblore equipment, and the
[construction flatpack table](https://oldschool.runescape.wiki/w/Calculator:Construction/Flatpacks)
identifies the marble wardrobe as furniture. Exact set IDs and public names were
also checked against this repository's original registry and the supplied exports.
No third-party plugin code, resource, layout or UI was copied or adapted.

## Regression and benchmark review

New tests exercise physical Combat family order, best-owned armour, cannon,
complete Shayzien/Elite-black columns, shuffled inputs, withdrawals/placeholders,
and conservation of all 194 reported items. Main supplies tests exercise the
actual public builder and packing path, multiple complete and incomplete
families, missing/duplicate/lookalike variants, tiny and odd-family banks, saved
orders and Ironman routing. Tablet tests cover real color exceptions, the Main
builder, shuffles, comparator transitivity and unrelated teleport roles.

The full tests initially found a tiny-bank reserve-head lock that could put a
weaker hat before the primary item; limiting reserve-row anchors to full rows
fixed it. Tests also protect vertical endgame armour from excessive filler locks.

All 1,950 fixed-seed simulations completed. Review compared the generated reports
with copies taken before this change. Only `report.tsv` changed: 10 of 150
scenario rows have different move/swap counts, all tab counts stay the same and
all outcomes remain COMPLETE. Total moves change from 12,180 to 12,182; individual
move deltas range from −9 to +3. This is a placement change, not a speed claim.
Both cleanup reports and aggregate metadata are byte-identical. The reviewed
`report.tsv` fingerprint changes from
`f935328b87c53256136a41ddaf690897ecddd5c330a120c73aee18f3d756de39` to
`76c08f48ef2b80441e157adbb89115b918ddc86726ce0158385634e0a43b8914`;
only that expected fingerprint was updated after inspecting the differences.

Final offline Java 11 `build` passes: **1,697 tests**, zero failures, errors or
skips, all **1,950 simulations** completed, and all four reviewed report
fingerprints pass. No new compiler warnings were emitted; the existing sidebar
unchecked-operation note remains known from the previous clean build.
`git diff --check` passes.

The local development JAR has **265 entries**, **1,109,983 bytes**, SHA-256
`6df9619f0ad3e3d8f699d297e8b8d7ecbb70b65317b46684c2a1ff79f47b6060`.
Its new tablet-color table is 481 bytes. No legacy prototype, tests, player
fixture, research/simulator code, development helpers or documentation is
bundled. Its filename still uses 0.9.5 and must not replace the published 0.9.5
artifact; the next release needs its own version and release notes.

The final development estimator uses official reported **200,414** at source
`def1e856e101ff0e57dd96adccab2cf1d886b076`. Estimates are 195,127 / 196,391
for `cl100k_base` and 195,316 / **196,596** for `o200k_base`, with preserved /
collapsed whitespace respectively. The highest leaves **3,404 estimated tokens**
below 200,000. This measures main Java only; the new and extended resource tables,
test sources/fixture, build fingerprint and docs were inspected separately.
The official Hub preprocessing and scope are unknown; this is not an official
token count or a certification for publication.

In-game follow-up: restart the development client, select an automatic Main
profile, Analyze Bank again and inspect Combat, supplies and tablets in both
blueprint and bank preview. Saved manual physical orders intentionally retain
priority. Check the other player's exact live-stat layout if a complete export
and plugin settings become available; the current files do not supply them.

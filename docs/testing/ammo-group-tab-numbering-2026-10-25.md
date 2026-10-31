# Ammo group and blueprint tab numbers

Local development above published `b3583ca8e48bedb37514e89006cb677e5b7ea75a`.
The owner confirmed the preceding sidebar wheel fix in game, then supplied
Reddit feedback and [a screenshot](https://i.imgur.com/JJPalc2.png) showing
Blueprint's TAB 8 beside sidebar Tab 7 and ammo mixed into Slayer & Boss Loot.
The owner explicitly chose a separate Ammunition group within Loot.

## Tab numbering

Blueprint previously displayed main at index zero but added one to every other
index. The shared view/draft title function now displays MAIN and TAB 1 through
TAB 9, matching sidebar and bank destination indices. The ten-tab regression
checks the ordinary editor, its draft and the read-only proposal viewer, retaining
the item counts. Destinations, save indices and bank actions are unchanged.
The pre-existing text export's positional TAB field and explicit placement field
are retained for compatibility; this change concerns the visible Blueprint tabs.

## An independent Loot ammunition group

- New `loot-ammunition` tag: **Ammunition**, under the existing Loot category.
  Default finished arrows, bolts, darts and cannonballs, including placeholders,
  retain their physical Loot destination but display under this separate group.
  Move the group anywhere through Tab Layout without moving Slayer & Boss Loot.
- The old `ammunition` key remains supported, with the distinct display name
  **Ammo Slot Equipment**. Its category, original lookup and old manual group
  relocation behavior remain intact. Blessings, pouches, other thrown weapons,
  cannon parts, crafting components and quest props retain their previous roles.
- An old serialized custom plan inherits the new group directly beside its
  existing boss-loot group. Explicit item corrections, captured layouts and
  saved destinations keep priority. Old boss-loot source records remain valid
  when their ammo now has the new automatic source tag.
- An older combined Loot block order that actually names an ammo block keeps
  its original combined group while the destinations still coincide. A new
  explicit Ammunition order or separate destination wins. Dormant settings
  remain stored; nothing is silently reset.
- Fixed Onyx bolts remain Alch; Flexible and dedicated Ascension ammunition
  retain their previous disposal rules. Main follows the explicit tag order,
  putting miscellaneous Loot with the other non-ammo loot before the new group.
  Current accepted-snapshot rule descriptions advance to 9; old snapshots remain
  readable and retain their deliberately kept targets.

The original definition corpus still pins old tag identities, category, order
and subcategory routing. The Ammo Slot Equipment display change and new Loot tag
are explicit exceptions. The normal plan has 31 standard tags; optional Combat
style tags remain inactive by default.

## Verification

Final Java 11 offline build: **2,405 tests**, zero failures/errors/skips,
**1,950 completed scenario banks** and **all four unchanged simulation hashes**.
Focused tests cover the full finished-ammo families with absent/present equipment
stats, placeholders, both presets, independent group moves, old serialized plans,
old/new source records, captures, personal corrections and block/item orders.
All whole-tab move permutations continue to preserve the ammo policy.

Earlier failures were expected old label/count expectations and the previous
two-tag assumption when moving Alch first. Tests now explicitly move Alch ahead
of both other tags and check the new independent group, without weakening the
conservation, old-choice or disposal assertions. The final build reports no
compiler warning; whitespace checks pass. Blueprint rendering was inspected.

Logs: `build/tmp/ammo-group-numbering-focused-final.log`,
`build/tmp/ammo-group-numbering-full-second.log`,
`build/tmp/ammo-group-numbering-size.log`. The prior simulation reports were
preserved in `build/tmp/ammo-numbering-before/` before the full comparison.

Highest main-Java estimate: **197,190**, leaving **2,810 estimated tokens**.
Other estimates: 194,147 / 196,853 / 194,479. Calibration remains the maintainer's
200,414 at `def1e856e101ff0e57dd96adccab2cf1d886b076`. This is not the official
Hub count; resource membership, tests and docs were inspected separately.

Local development JAR (still named 0.9.9): **937,741 bytes**, SHA-256
`a7fbc211242c0a8dcc6bb1e9958aea4e439f390c695ee9d9d121817b55b58a82`.
It is not the published 0.9.9 artifact and has not been uploaded. HEAD remains
`b3583ca8e48bedb37514e89006cb677e5b7ea75a`; nothing was committed, pushed,
published or posted to Reddit. The owner subsequently confirmed the tab/ammo
follow-up looked good in game and approved renaming the original combat tag
from Combat Ammunition to Ammo Slot Equipment. This final naming change affects
only the display label, compatibility expectation and documentation; stored keys
and placements stay unchanged. The JAR identity above predates that final label.
The focused definition/tag, ammo and sidebar suites pass after the label update
(`build/tmp/ammo-slot-label-final.log`). The refreshed local JAR has 937,747 bytes,
SHA-256 `cae434e3247fa48f56236bea2baf02110fa0c2b0f1f75b700ee95dcb4b1da740`.
The main-Java token estimate is unchanged; release metadata is not yet advanced.

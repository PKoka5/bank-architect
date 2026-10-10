# Endgame Combat roles and validation - 2026-10-10

## Placement policy

This follow-up implements the findings in
[the earlier 244-item simulation](endgame-combat-simulation-2026-10-10.md).
Fresh Main and Ironman presets use the same Combat selection policy:

- General Strength armour leads the melee column: Torva, including Sanguine
  appearances, when present. Oathplate slash gear, Inquisitor crush gear and
  Justiciar defence gear retain their vertical secondary families.
- Scythe, Twisted bow and Shadow receive explicit frontline priority when
  available. Special-attack switches do not displace an available ordinary
  weapon; a bank containing only a switch can still select it.
- Remaining slash, stab, crush, demonbane, ranged, casting, elemental and
  special-attack weapons form original role groups. Weapons belonging to an
  existing Barrows or Moon family remain with that family.
- When the selected ranged weapon is Bowfa, owned Crystal head/body/legs lead
  that column. Exact inactive and colour variants retain their intended role.
- Eye of ayak, Confliction gauntlets and amulet of rupture have explicit current
  classification/progression coverage. Existing Avernic treads coverage remains.
- Shared gear occupies one bank cell. User category choices and saved physical
  blueprint orders retain priority. Placeholders preserve the intended layout.

The front is a navigation policy, not a target-specific DPS calculation or an
equippable loadout. Attack weaknesses, passives, spells and two-handed/offhand
constraints can make another retained setup preferable. Frontline preferences
are separate from the stat dominance and reviewed replacement rules for Alch.

## Research

Public game information informed an original policy; no third-party plugin
code, UI, resources or layout was copied.

- [Ranged progression guide](https://oldschool.runescape.wiki/w/Guide:Ranged_Gear_Progression):
  distinguishes Crystal/Bowfa from Masori with other ranged weapons.
- [Magic progression guide](https://oldschool.runescape.wiki/w/Guide:Magic_Gear_Progression):
  retains distinct powered, Ancient, elemental and demonbane roles, with modern
  glove, neck and weapon progression.
- [Oathplate](https://oldschool.runescape.wiki/w/Oathplate),
  [Inquisitor's mace](https://oldschool.runescape.wiki/w/Mace_inq) and
  [Osmumten's fang](https://oldschool.runescape.wiki/w/Fangf): relevant accuracy
  and passive roles prevent treating every melee option as interchangeable.
- The supplied [melee guide](https://oldschool.runescape.wiki/w/Guide:Melee_Gear_Progression)
  was a research starting point. Its full page was unavailable in this session;
  the verified specific item pages above support the policy.

## Implementation and resource review

`GearItemSorter` applies reviewed preference before its existing placement score
and selects Crystal with Bowfa. Exact weapon roles also supply style and slot
when equipment metadata is absent or inactive. `GearSetSemanticRuleSet` merges
role groups after existing set families, reserving every ID only once.

`BoundedLayoutPacker` skips inserting a strictly worse child into a full bounded
heap. Equal-score tie handling, the 128-state beam and 1,000,000-origin cap remain
unchanged. No runtime network requests or game actions were added.

Reviewed resources outside the main-Java token estimator:

- New `combat-gear-roles.tsv`: 123 distinct IDs; cached equipment slots agree
  with all weapon rows and the six Torva armour entries.
- One Sanguine Torva family: ordered gear-family count 77 to 78.
- Twelve exact fallback classifications: six Scythes, two Shadows, two Eyes,
  Confliction and rupture. This corrects Scythe/Shadow placement without stats.
- Four tier entries: catalog count 344 to 348.
- Five existing fallback name lists moved unchanged from Java into
  `classification-names.tsv`: group count 76 to 81. All previous groups remain.
- Test, fixture, README, Gradle baseline and development-tool changes were
  inspected separately. Development source and test classes stay outside the JAR.

## Verification

The full offline build passes, including 1,611 JUnit cases and the four reviewed
simulation fingerprints. The new endgame class contributes 28 parameterized
cases across Main and Ironman. Its 108 cached stat vectors were independently
compared with the public RuneLite data, with zero mismatches.

Independent expected positions cover Torva/Sanguine Torva, raid weapons,
Confliction/rupture, Eye progression, charged/inactive Bowfa with Crystal,
specialist vertical families and cannon adjacency. Tests include six Scythe
appearances with both Shadow states, missing equipment metadata, banks without
raid weapons, sparse banks, bulk specialist stacks, placeholders, reversed
input, category pins and saved blueprint order.

All 20 development preview runs pass. The representative endgame bank now has
251 distinct IDs: 120 from 30 families and 131 additional items. Three variants
(owned, 26 placeholders, reversed input) run both presets. Combat ID order is
identical within each preset and all IDs, quantities and placeholders survive.
This is representative coverage, not every boss item or every item state.

| Endgame result | Main | Ironman |
| --- | ---: | ---: |
| Combat stacks | 199 | 198 |
| Alch stacks | 24 | 24 |
| Drops stacks | 46 | 46 |
| Owned preview build | 869 ms | 936 ms |
| Placeholder preview build | 947 ms | 927 ms |
| Reversed preview build | 821 ms | 923 ms |

Drops contains 22 ammunition stacks followed by 24 Alch stacks. Every ammo
family, all ammo together and Alch each occupy one contiguous run. Compared
with the earlier fixture, added Voidwaker correctly outclasses Rune scimitar
under the existing complete-stat dominance rule, accounting for the extra
Alch item. Specialist weapon stacks remain retained.

The earlier smaller 244-ID fixture took 5.1-5.6 seconds. The expanded fixture
now takes 0.82-0.95 seconds locally. These are individual cached-data timings,
not a controlled benchmark or in-game frame-time measurement.

### Deliberate simulation baseline changes

An independent agent compared preserved before/after reports before accepting
new fingerprints. All 150 fixed runs and all 1,800 aggregate runs complete;
every other outcome is zero and errors are empty.

- Only one fixed-run row changes: seed `20260731`, `RANDOM_TABS`, moves 90 to
  89, swaps 28 to 27 and minimum swaps 28 to 27. Every other cell is unchanged.
  All 149 runs entering sorting meet their computed minimum; the existing
  already-complete run retains its `0/-1` sentinel.
- Fixed cleanup report is unchanged. Aggregate cleanup removes exactly seven
  false cleanup IDs: `22325,22486,25736,25738,25739,31113,33639`. No new cleanup
  entries or modified retained occurrence counts appear.
- Aggregate distinct IDs change 9273 to 9266 and occurrences 46446 to 46422.
  All other metadata is unchanged. The real-bank fixture changes only Eye
  `31115` from Storage & Cleanup to Combat.

The three changed canonical CRLF SHA-256 fingerprints in `build.gradle` are:

| Report | SHA-256 |
| --- | --- |
| `report.tsv` | `f935328b87c53256136a41ddaf690897ecddd5c330a120c73aee18f3d756de39` |
| `aggregate/cleanup-review.tsv` | `fce40d023f6950a6813d97276f3836c73ab801e7c0c2c13235a4e7423f207ce3` |
| `aggregate/metadata.tsv` | `76901b09b75f9d8219f3e1ba8b522940af373160718cd09cbf40439db088c3fe` |

## Reproduction and review size

Use [the development runner instructions](../../tools/research/combat-layout-simulation/README.md).
Local output: `build/reports/combat-layout-visual/endgame-roles-simulation.json`.
Cached RuneLite stats SHA-256:
`99cdcb7514b2cf48655094d94e502d95d4ac708e328b25579d21511c4db3f1b8`.
Cached Wiki high-alch mapping SHA-256:
`9852ffc77384a30802ef3769bf769de6b288fc9043fad20de1f035573f887cf8`.
Inputs were cached on 2026-10-09, with no runtime download feature.

The highest local main-Java review estimate is 197,230 (2,770 estimated tokens
of headroom), calibrated against official count 200,414 at source revision
`def1e856e101ff0e57dd96adccab2cf1d886b076`. This is not the official Hub count;
its tokenizer and scope are unknown. Resource and test changes are excluded
and were reviewed above. Changes remain local and unpublished.

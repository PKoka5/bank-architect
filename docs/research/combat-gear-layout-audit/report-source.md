# Combat gear research record

Status: internal source record

Date: 2026-08-30

## Scope

The plugin needs one useful combat tab order for accounts at any progression level. It does not
build boss presets, rank PvP setups, or group cosmetic outfits by taste.

RuneLite equipment data can rank ordinary gear by slot and combat stats. It cannot describe set
activation, required weapons, optional partners, or broken states. Those facts belong in a small
reviewed catalog.

## Assumptions

- The bank is eight columns wide and removes empty spaces.
- The plugin knows what is in the bank, but not what activity the player plans to do next.
- One owned item can appear only once, even when several loadouts could use it.
- OSRS Wiki equipment pages describe game mechanics. RuneLite's local item IDs identify the exact
  variants used by the development client.

## Claims used by the catalog

| Claim | Source | Layout decision |
|---|---|---|
| Crystal armour buffs both Crystal bow and Bowfa. Each armour piece contributes. | [Crystal equipment](https://oldschool.runescape.wiki/w/Crystal_equipment) | Either weapon can complete the ranged set. Active colour variants share armour roles. |
| Void needs gloves, top, robe, and one style helm. Regular and elite torso pieces count. | [Void Knight equipment](https://oldschool.runescape.wiki/w/Void_Knight_equipment) | Melee, ranged, and magic recipes compete for the shared torso. Owned gear breaks the tie. |
| Justiciar damage reduction needs all three pieces. | [Justiciar armour](https://oldschool.runescape.wiki/w/Justiciar_armour) | Faceguard, chestguard, and legguards are required. |
| Inquisitor gives a per-piece crush bonus, a full-set bonus, and a larger bonus with its mace. | [Passive effects](https://oldschool.runescape.wiki/w/Passive_effect), [Project Rebalance](https://oldschool.runescape.wiki/w/Update%3AProject_Rebalance_-_Item_%26_Combat_Adjustments) | The three armour pieces are required. The mace is optional. |
| Full Obsidian armour buffs compatible Obsidian melee weapons. Berserker necklace stacks with it. | [Melee armour](https://oldschool.runescape.wiki/w/Armour/Melee_armour), [Melee training](https://oldschool.runescape.wiki/w/Pay-to-play_melee_training) | Armour and a compatible weapon are required. The necklace is optional. |
| Swampbark reaches its maximum bind extension with helm, body, and legs. | [Swampbark armour](https://oldschool.runescape.wiki/w/Swampbark_armour), [Poll 79 changes](https://oldschool.runescape.wiki/w/Update%3AMore_Poll_79_Changes) | Helm, body, and legs are required. Gloves and boots are optional family pieces. |
| Every Bloodbark piece improves blood-spell healing. | [Bloodbark armour](https://oldschool.runescape.wiki/w/Bloodbark_armour) | The planner reserves a set block only for all five pieces. |
| Ancient sceptres improve Bloodbark healing. Bloodbark does not improve Sanguinesti healing. | [Bloodbark armour](https://oldschool.runescape.wiki/w/Bloodbark_armour), [Ancient sceptre](https://oldschool.runescape.wiki/w/Ancient_sceptre) | Ancient sceptres are optional Bloodbark partners. Sanguinesti is not. |
| A charged Serpentine helm can envenom with charged toxic ranged and magic weapons. | [Serpentine helm](https://oldschool.runescape.wiki/w/Serpentine_helm) | Charged ranged and magic pairings are supported. Uncharged helms are unusable. |
| Each Barrows set needs its brother's four pieces. Amulet of the damned improves the effect. | [Barrows equipment](https://oldschool.runescape.wiki/w/Burrows_armour), [Passive effects](https://oldschool.runescape.wiki/w/Passive_effect) | Six four-piece recipes. One shared amulet can sit beside the highest-ranked set. |
| Moon effects use the matching weapon and armour. Broken banked pieces need repair. | [Moon equipment](https://oldschool.runescape.wiki/w/Moon_equipment), [Eclipse moon armour](https://oldschool.runescape.wiki/w/Eclipse_moon_armour), [Blue moon armour](https://oldschool.runescape.wiki/w/Blue_moon_armour) | Three four-piece recipes. Broken pieces stay at the end. |
| Virtus improves Ancient Magicks per piece. | [Passive effects](https://oldschool.runescape.wiki/w/Passive_effect) | Virtus uses ordinary magic ranking instead of a set recipe. |
| Torva, Ancestral, Oathplate, Masori, Armadyl, and Hueycoatl hide need no universal activation recipe. | [Passive effects](https://oldschool.runescape.wiki/w/Passive_effect), [Oathplate armour](https://oldschool.runescape.wiki/w/Oathplate_armour), [Hueycoatl hide armour](https://oldschool.runescape.wiki/w/Hueycoatl_hide_armour) | These families use stats and progression tiers. |
| Shayzien and Ancient Warriors' effects apply only in specific activities. | [Passive effects](https://oldschool.runescape.wiki/w/Passive_effect), [Ancient Warriors' equipment](https://oldschool.runescape.wiki/w/Ancient_Warriors%27_equipment) | Keep the pieces together without giving them general combat priority. |

## Decisions and limits

- Matching names are not enough to prove a set effect. Visual families and active sets are separate.
- Per-piece effects, such as Crystal and Bloodbark, still benefit from keeping complete families
  together without making incomplete pieces useless.
- Shared optional items cannot be copied. Amulet of the damned goes beside the highest-ranked Barrows
  set that uses it.
- Void has three uses for the same torso pieces. The strength of the player's other gear decides
  which style receives them.
- Ordinary new gear is handled through RuneLite stats. A catalog change is needed only for a new
  relationship or broken state that those stats cannot show.
- The sources were checked on 2026-08-30. Game updates may change these mechanics, so each catalog
  change needs a current source and a focused regression test.

Research stopped after every catalog fact had a direct mechanic source, the variant IDs matched the
local RuneLite API, and further results repeated the same facts or described encounter-specific gear
outside this planner's scope.

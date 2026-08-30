# Dynamic combat gear layout research record

Status: internal canonical research record

Audience: Ironman Bank Architect maintainers

Date: 2026-08-30

## Scope and direct answer

The decision concerns a general-purpose OSRS bank layout for any account, from early to end game.
It excludes boss-specific presets, PvP-only equipment modes, and subjective fashion grouping.

The correct boundary is dynamic equipment planning plus curated mechanic facts. Live RuneLite stats
can rank any ordinary owned item by style, slot, and strength. They cannot reveal full-set activation,
compatible weapon requirements, optional synergies, or unusable lifecycle states. Those facts belong
in a small data catalog, not account-specific Java branches.

## Assumptions

- The bank is eight columns wide and compacts items, so the plugin cannot rely on invented blank cells.
- The plugin reads bank contents and equipment metadata, not the player's intended encounter.
- An owned item may appear once in the plan even when it can participate in multiple setups.
- Current OSRS Wiki item and equipment pages are authoritative for game mechanics. RuneLite's local
  item IDs are authoritative for the development client's current variant identifiers.

## Claim ledger

| Claim | Source | Access note | Confidence | Production decision |
|---|---|---|---|---|
| Crystal armour buffs both Crystal bow and Bowfa, with each piece contributing. | [Crystal equipment, OSRS Wiki](https://oldschool.runescape.wiki/w/Crystal_equipment) | Retrieved 2026-08-30; page crawl was approximately five months old. | High | One weapon role accepts both weapon families; active colour variants share armour roles. |
| Void requires gloves, top, robe, and one style helm; regular and elite torso pieces count. | [Void Knight equipment, OSRS Wiki](https://oldschool.runescape.wiki/w/Void_Knight_equipment) | Retrieved 2026-08-30. | High | Three competing style recipes share torso roles; owned complementary gear breaks the tie. |
| Justiciar damage reduction requires all three pieces. | [Justiciar armour, OSRS Wiki](https://oldschool.runescape.wiki/w/Justiciar_armour) | Retrieved 2026-08-30. | High | Three required armour roles. |
| Inquisitor grants 0.5% per piece, 2.5% as a full set, and 7.5% with its mace. | [Passive effect, OSRS Wiki](https://oldschool.runescape.wiki/w/Passive_effect), [Project Rebalance item adjustments](https://oldschool.runescape.wiki/w/Update%3AProject_Rebalance_-_Item_%26_Combat_Adjustments) | Retrieved 2026-08-30. | High | Three required armour roles; mace is optional synergy. |
| Full Obsidian armour buffs compatible Obsidian melee weapons; Berserker necklace stacks. | [Melee armour, OSRS Wiki](https://oldschool.runescape.wiki/w/Armour/Melee_armour), [Pay-to-play melee training](https://oldschool.runescape.wiki/w/Pay-to-play_melee_training) | Retrieved 2026-08-30. | High | Armour and compatible weapon required; necklace optional. |
| Swampbark reaches maximum bind extension with helm, body, and legs. | [Swampbark armour, OSRS Wiki](https://oldschool.runescape.wiki/w/Swampbark_armour), [Poll 79 changes](https://oldschool.runescape.wiki/w/Update%3AMore_Poll_79_Changes) | Retrieved 2026-08-30. | High | Three required core roles; gloves and boots optional. |
| Each Bloodbark piece improves blood-spell healing; full set reaches the maximum. | [Bloodbark armour, OSRS Wiki](https://oldschool.runescape.wiki/w/Bloodbark_armour) | Retrieved 2026-08-30. | High | Reserve a family block only when all five pieces are owned. |
| Ancient sceptres increase Bloodbark blood-spell healing, while Bloodbark does not affect Sanguinesti healing. | [Bloodbark armour, OSRS Wiki](https://oldschool.runescape.wiki/w/Bloodbark_armour), [Ancient sceptre, OSRS Wiki](https://oldschool.runescape.wiki/w/Ancient_sceptre) | Retrieved 2026-08-30. | High | Ancient sceptre variants are optional Bloodbark partners; Sanguinesti is not. |
| A charged Serpentine helm can envenom with charged toxic ranged and magic weapons. | [Serpentine helm, OSRS Wiki](https://oldschool.runescape.wiki/w/Serpentine_helm) | Retrieved 2026-08-30. | High | Two weapon-style recipes; uncharged helms are unusable lifecycle variants. |
| Each Barrows set needs its own four pieces; Amulet of the damned enhances the effect. | [Barrows equipment, OSRS Wiki](https://oldschool.runescape.wiki/w/Burrows_armour), [Passive effect, OSRS Wiki](https://oldschool.runescape.wiki/w/Passive_effect) | Retrieved 2026-08-30. | High | Six four-role recipes; shared amulet is optional and emitted once. |
| Moon set effects use the matching weapon and armour. Broken banked pieces need repair before reuse. | [Moon equipment, OSRS Wiki](https://oldschool.runescape.wiki/w/Moon_equipment), [Eclipse moon armour](https://oldschool.runescape.wiki/w/Eclipse_moon_armour), [Blue moon armour](https://oldschool.runescape.wiki/w/Blue_moon_armour) | Retrieved 2026-08-30. | High | Three four-role recipes; broken variants are maintenance items. |
| Virtus benefits Ancient Magicks per piece rather than requiring a separate activation item. | [Passive effect, OSRS Wiki](https://oldschool.runescape.wiki/w/Passive_effect) | Retrieved 2026-08-30. | High | Leave to live magic stats instead of inventing a recipe. |
| Torva, Ancestral, Oathplate, Masori, Armadyl, and Hueycoatl hide do not require a universal full-set activation recipe. | [Passive effect, OSRS Wiki](https://oldschool.runescape.wiki/w/Passive_effect), [Oathplate armour, OSRS Wiki](https://oldschool.runescape.wiki/w/Oathplate_armour), [Hueycoatl hide armour, OSRS Wiki](https://oldschool.runescape.wiki/w/Hueycoatl_hide_armour) | Retrieved 2026-08-30. | High | Treat them as stat-driven progression and visual families. |
| Shayzien and Ancient Warriors effects are activity-restricted. | [Passive effect, OSRS Wiki](https://oldschool.runescape.wiki/w/Passive_effect), [Ancient Warriors' equipment](https://oldschool.runescape.wiki/w/Ancient_Warriors%27_equipment) | Retrieved 2026-08-30. | High | Exclude from universal setup priority. |

## Evidence reconciliation

- Visual families and activation mechanics answer different questions. Matching names do not prove
  that items must be worn together.
- Some effects apply per piece, such as Crystal and Bloodbark. The planner still benefits from a
  complete-family block when the full relationship is owned, while incomplete pieces remain useful
  in dynamic style setups.
- Optional shared items cannot reserve multiple physical copies. Amulet of the damned is attached to
  the highest-ranked resolved Barrows block and omitted from later blocks without invalidating them.
- Void has three valid uses for the same torso pieces. Ranking the recipes with compatible owned gear
  is more account-sensitive than hard-coding melee, ranged, or magic first.

## Coverage and limitations

The first audit covered Crystal and Bowfa, Moons, Barrows, Void, Masori assembler, and mixed melee.
This follow-up added Crystal bow and colour variants, Justiciar, Inquisitor, Obsidian, Swampbark,
Bloodbark, Void ornament and trouver variants, and optional Barrows synergy. It also reviewed Virtus,
Torva, Ancestral, Oathplate, Masori, Armadyl, Hueycoatl hide, Shayzien, and Ancient Warriors. The
stat-driven families and activity-specific effects are deliberately excluded from universal
activation recipes. Bloodbark and Serpentine weapon synergies were added where stats cannot express
the cross-item mechanic.

The geometry is also data-driven. With enough real entries, two or more owned armour cores form
vertical columns ordered helmet, body, legs, and weapon. Physical entry counts, including separate
charge or durability variants, determine band width. Banks that cannot fill the geometry fall back to
dense blocks without synthetic placeholders.

This is not a frozen list of every combat item in OSRS. It does not need to be. Ordinary new items are
handled automatically when RuneLite exposes their equipment stats. The only maintenance obligation is
to add a fact when a new mechanic introduces a relationship that those stats cannot express.

The OSRS Wiki search backend exposed pages from a crawl roughly five months old. No conflicting source
was found for the encoded mechanics. Future game updates can still supersede these facts, so each data
change should include a direct Wiki check and regression.

## Search stop condition

Research stopped after every production fact had a direct OSRS Wiki mechanic source, variant IDs were
verified against the local RuneLite API used by the development client, and further results repeated
the same relationships or described encounter-specific recommendations outside this planner's scope.

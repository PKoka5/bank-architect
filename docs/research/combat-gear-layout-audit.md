# Combat gear layout audit

Audit date: 2026-08-30

## Decision

The combat tab uses two kinds of information:

1. Equipment slots, combat stats, and progression tiers rank ordinary gear the player owns.
2. A reviewed item catalog keeps together sets and item combinations whose effects cannot be read
   from stats alone.

This is a bank layout, not a best-in-slot guide. New ordinary gear joins the layout through its slot
and stats. The catalog needs an update only when a new item has a set effect, special relationship,
or unusable state that RuneLite stats do not describe.

## How loadouts are arranged

The planner builds melee, ranged, magic, and prayer loadouts from the strongest remaining item in
each slot. A loadout can contain one head, body, legs, weapon, cape, neck, hands, feet, shield, and
ring before alternative items are added.

When enough gear exists to fill every row, the planner lines up helmets, bodies, legs,
and weapons in columns. Otherwise it uses the same order without empty spaces. Different charge or
durability variants keep their own bank slots and may widen a column.

This works for early and late accounts. Rune, dragon, Bandos, Torva, black dragonhide, Crystal,
Masori, Mystic, Ahrim's, Virtus, and Ancestral gear all use the same rules.

## Reviewed item relationships

The catalog defines 20 loadouts. Required pieces must all be owned before the planner treats the
set as active. Optional pieces sit beside an active set when owned. Three different roles are enough
to keep an incomplete set together, but the planner does not claim its set effect is active.

| Relationship | Rule |
|---|---|
| Crystal ranged | Crystal bow or Bow of faerdhinen with an active Crystal helm, body, and legs. Active colour variants count. |
| Blood, Eclipse, and Blue Moon | The matching weapon with all three usable armour pieces. |
| Barrows | Each brother's weapon, head, body, and legs. Amulet of the damned is optional. |
| Void | The matching helm with gloves, top, and robe. Supported regular, elite, ornament, and trouver variants count. |
| Justiciar | Faceguard, chestguard, and legguards. |
| Inquisitor | Great helm, hauberk, and plateskirt. The mace is optional. |
| Obsidian | Helmet, platebody, platelegs, and a compatible Obsidian melee weapon. Berserker necklace is optional. |
| Swampbark | Helm, body, and legs. Gauntlets and boots stay with the family when owned. |
| Bloodbark | All five pieces. Incomplete pieces remain ordinary magic gear. |
| Serpentine | A charged helm with a compatible charged toxic ranged or magic weapon. |

The main sources for these rules are the OSRS Wiki pages for
[Crystal equipment](https://oldschool.runescape.wiki/w/Crystal_equipment),
[Void Knight equipment](https://oldschool.runescape.wiki/w/Void_Knight_equipment),
[Justiciar armour](https://oldschool.runescape.wiki/w/Justiciar_armour),
[Swampbark armour](https://oldschool.runescape.wiki/w/Swampbark_armour),
[Bloodbark armour](https://oldschool.runescape.wiki/w/Bloodbark_armour),
[Serpentine helm](https://oldschool.runescape.wiki/w/Serpentine_helm), and
[Moon equipment](https://oldschool.runescape.wiki/w/Moon_equipment).
The detailed claim record is in [report-source.md](combat-gear-layout-audit/report-source.md).

## Broken and inactive gear

Broken, zero-durability, and inactive gear stays at the end of the tab. It cannot complete a
loadout while banked. The catalog covers known Crystal, Moon, Barrows, Void, damaged Torva, and
uncharged Serpentine variants. Names containing `broken`, `damaged`, or `inactive` provide a fallback
for new items.

Moon gear at zero durability must be repaired before it can be equipped again, so a broken banked
piece cannot start a Moon loadout.

## Families and priority

Keeping items together does not automatically raise their priority. This prevents a situational
weapon from outranking a stronger general loadout simply because it belongs to a set.

Reviewed families include Void helms and torso pieces, Ghostly robes, Elite black armour, each
Shayzien tier, dwarf cannon parts, and incomplete Crystal, Moon, Barrows, Justiciar, Inquisitor,
Obsidian, Swampbark, and Bloodbark sets.

Separate utility scores cover items whose value comes from a passive, special attack, task bonus,
or target-specific effect. Examples include Slayer helmets, Toxic blowpipes, Serpentine helms,
imbued rings and god capes, Salve upgrades, Tome of fire, Dragon daggers, Burning claws, Emberlight,
Scorching bow, Purging staff, and Twinflame staff. Karil's crossbow is lowered on its own, but not
when the full Karil set is active.

## Gear left to the general rules

- Masori assembler is ranged cape gear. It is not required for Crystal or Masori armour.
- Masori, Torva, Ancestral, Virtus, Oathplate, Hueycoatl hide, Bandos, Armadyl, metal armour,
  dragonhide, and ordinary robes use stats and progression tiers. They do not need set recipes.
- Virtus remains ordinary magic gear because its Ancient Magicks benefit works per piece.
- Shayzien gear stays together but gets no general priority boost because its protection is specific
  to lizardman shamans.
- Ancient Warriors' set effects do not receive general priority because they are PvP-specific.
- Target-specific items may receive a utility score, but the plugin does not guess the player's next
  encounter.

## Guarantees and tests

- Only entries already in the bank appear in the blueprint. The planner never adds blank cells.
- Repeated physical entries and real placeholders remain separate bank slots. Placeholders can keep
  an inactive family together and fill an aligned row, but they never activate a set or add score.
- Shared pieces, such as the Void torso, can appear in only one resolved loadout.
- Ammunition stays together at the end, ordered by family and tier.
- Tests cover early and late gear, Crystal variants, all Void styles, Moon and Barrows sets,
  incomplete families, Ghostly and Elite black gear, cannon parts, utility priority, broken gear,
  repeated entries, vertical alignment, and ammunition word boundaries.

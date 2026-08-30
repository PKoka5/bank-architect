# Combat gear layout audit

Audit date: 2026-08-30

## Decision

Combat Gear is organized with two layers:

1. Live RuneLite equipment stats, progression tiers, and reviewed utility adjustments build
   general-purpose melee, ranged, magic, and prayer setup heuristics from the items the player owns.
2. A small exact-ID catalog preserves game relationships that stats cannot reveal, such as a full
   Void set, Crystal armour weapon compatibility, Barrows set effects, incomplete set families, and
   equipment whose practical value comes from a passive or special mechanic.

The algorithm has no account-specific item rule. New ordinary equipment automatically participates
through its equipment slot, combat style, and score. A catalog change is needed only when Jagex adds
or changes a mechanic-driven relationship or lifecycle variant.

## Dynamic setup construction

For each combat style, the planner repeatedly selects higher-ranked remaining items by equipment slot.
The score combines equipment stats, a pinned progression tier, and reviewed utility adjustments. It is
useful for consistent bank ordering, but it does not model every activity, attack cycle, or damage
formula and is not a best-in-slot recommendation. When a setup uses fewer than the bank's eight columns,
the remaining cells receive compatible high-ranked sidegrades regardless of slot.

When a consecutive batch contains at least two usable armour cores and enough real entries from those
blocks to fill four rows, the planner uses a vertical loadout matrix. Each setup keeps its helmet, body,
legs, and weapon in the same column.
Different charges or durability states widen that setup's column band by the number of physical bank
entries they occupy. If the bank cannot form the matrix without gaps, the same dynamic blocks fall back
to a dense layout. No item family or account inventory is hard-coded into this geometry.

This rule works across progression levels. Rune, dragon, Barrows, Bandos, Torva, black dragonhide,
Crystal, Masori, Mystic, Ahrim's, Virtus, and Ancestral do not need account-specific layout code. Their
live stats and the reviewed progression catalog determine which owned item leads each slot.

The old two-body exception has been removed. A primary setup now contains at most one head, body,
legs, weapon, cape, neck, hands, feet, shield, and ring before any sidegrade is added. The highest-ranked
generic head, body, and legs candidates are protected from being borrowed as filler for an exact set,
so a useful progression core remains available for its own setup.

## Mechanic-driven catalog

The catalog currently defines 20 loadouts across the relationships below. Required roles activate a
block only when every role is owned. Optional roles are placed beside the block when owned but do not
pretend to be required for activation. The same role data also derives incomplete-set families. Three
distinct owned roles are enough to preserve a functional family, while variants from one role count
once and two isolated roles remain available
to the dynamic progression planner. This keeps a three-piece Verac group together without letting a
Karil's crossbow and one useful armour piece inherit the armour piece's priority.

| Relationship | Encoded rule |
|---|---|
| Crystal ranged | Crystal bow or Bow of faerdhinen, plus active Crystal helm, body, and legs. Active colour variants are equivalent. |
| Blood, Eclipse, Blue Moon | The corresponding weapon plus all three usable armour pieces. |
| Six Barrows sets | Each brother's weapon, head, body, and legs. Amulet of the damned is an optional synergy. |
| Melee, ranged, magic Void | The matching helm plus gloves, top, and robe. Regular, elite, ornament, and trouver variants are equivalent where the game treats them as equivalent. |
| Justiciar | Faceguard, chestguard, and legguards. |
| Inquisitor | Great helm, hauberk, and plateskirt, with the mace as an optional stronger synergy. |
| Obsidian | Helmet, platebody, platelegs, and a compatible melee Obsidian weapon. Berserker necklace is optional. |
| Swampbark | Helm, body, and legs provide the maximum bind extension. Gauntlets and boots are optional family pieces. |
| Bloodbark | All five pieces are kept together when the full family is owned. Incomplete pieces remain ordinary magic gear. |
| Serpentine ranged and magic | A charged Serpentine helm is kept with a compatible charged toxic weapon. Uncharged helms cannot activate the relationship. |

Crystal armour buffs both the Crystal bow and Bowfa, so the earlier Bowfa-only rule was incomplete.
Each armour piece contributes to the bonus. [Crystal equipment](https://oldschool.runescape.wiki/w/Crystal_equipment)

Void requires gloves, top, robe, and one style helm. Elite top and robe add ranged or magic damage but
share the base set relationship. [Void Knight equipment](https://oldschool.runescape.wiki/w/Void_Knight_equipment)

Justiciar's damage reduction requires the full three-piece set. Inquisitor pieces improve crush, with
an additional full-set bonus and a stronger interaction with the Inquisitor's mace. Obsidian armour
requires all three armour pieces and an Obsidian weapon for its set effect; Berserker necklace stacks
with it. [Justiciar armour](https://oldschool.runescape.wiki/w/Justiciar_armour),
[Passive effects](https://oldschool.runescape.wiki/w/Passive_effect)

Swampbark's maximum bind extension now requires only helm, body, and legs. Bloodbark instead grants
healing improvements per piece, so only the complete five-piece family receives a reserved block.
[Swampbark armour](https://oldschool.runescape.wiki/w/Swampbark_armour),
[Bloodbark armour](https://oldschool.runescape.wiki/w/Bloodbark_armour),
[Ancient sceptre](https://oldschool.runescape.wiki/w/Ancient_sceptre)

An Ancient sceptre is an optional Bloodbark synergy; a Sanguinesti staff is deliberately not treated
as one because Bloodbark does not improve its healing. A charged Serpentine helm can envenom with a
charged Toxic blowpipe, Trident of the swamp, or Toxic staff of the dead family. These cross-item
mechanics cannot be derived from equipment stats alone.
[Serpentine helm](https://oldschool.runescape.wiki/w/Serpentine_helm)

## Lifecycle rules

Broken, zero-durability, and inactive equipment is placed in a final maintenance block. It cannot
complete a loadout while sitting in the bank. The catalog contains reviewed IDs for Crystal, Moon,
Barrows, Void, damaged Torva, and uncharged Serpentine states. A normalized name fallback also catches
new items whose names include `broken`, `damaged`, or `inactive`.

Moon items reaching zero are not re-equipable until repaired. A Moon set effect can remain briefly if
an already-equipped item breaks, but a banked broken item cannot be used to start that setup.
[Moon equipment](https://oldschool.runescape.wiki/w/Moon_equipment)

## Family and utility layers

Family membership and combat priority are separate facts. A family controls adjacency and member
order. A utility adjustment controls when an item or completed block appears relative to ordinary
fallback gear. This prevents family cohesion from turning a situational weapon into a universal
best-in-slot recommendation.

Explicit families cover all owned Void helms around the shared core, Ghostly hood, robe pieces,
cloak, gloves, and boots, Elite black armour, each Shayzien armour tier, and both normal and
ornamented dwarf cannon parts. Derived families cover incomplete Crystal, Moon, Barrows, Justiciar,
Inquisitor, Obsidian, Swampbark, and Bloodbark sets.

The reviewed utility catalog has two scopes. Standalone adjustments raise independently useful gear,
including Slayer helmet variants, charged Toxic blowpipes and Serpentine helms, imbued rings and god
capes, Salve upgrades, Tome of fire, Dragon dagger variants, Burning claws, Emberlight, Scorching bow,
Purging staff, and Twinflame staff. Loadout adjustments apply only after every required role activates,
which keeps shared Void pieces ordinary until a complete melee, ranged, or magic Void setup exists.
Karil's crossbow receives a standalone negative adjustment, but that adjustment is suppressed when the
complete Karil loadout activates. Ordinary equipment still enters dynamically from live slots, stats,
and progression tiers.

## Checked but deliberately not hard-wired

- Masori assembler is a ranged cape, not a required Crystal or Masori member.
- Masori, Torva, Ancestral, Virtus, Oathplate, Hueycoatl hide, Bandos, Armadyl, metal armour,
  dragonhide, and ordinary robes are stat-driven progression families. They do not need activation
  recipes. Their reviewed tiers improve the fallback when live stats are unavailable.
- Virtus has a per-piece Ancient Magicks benefit. It remains dynamic magic gear because no additional
  item is required to make a piece useful.
- Shayzien armour is activity-specific protection against lizardman shamans. Its five pieces remain
  an ordered family, but the family receives no universal combat priority boost. Ancient Warriors'
  set effects are restricted to PvP activities and likewise do not outrank general combat setups.
- Target-specific equipment does not activate a fabricated universal loadout. Reviewed items can
  receive a utility adjustment, but the bank snapshot still does not claim which encounter the
  player intends to fight.
- Incomplete activation families never receive a set-effect claim. Three or more distinct owned roles stay
  adjacent as a family; smaller fragments return to the generic style planner unless an explicit
  visual or assembly family says otherwise.

## Determinism and extension

- Only owned items are emitted. No blank bank cells are invented.
- Each logical item ID is classified once before physical expansion.
- Multiple physical entries for one item ID remain multiple cells. Owned entries and real placeholders
  are preserved independently. Charge and durability
  variants cannot silently overflow an eight-cell block.
- Competing recipes that share pieces, such as the three Void styles, are ranked using the strength
  of compatible gear the player owns. The highest-ranked supported style receives the shared pieces.
- Ammunition remains a terminal block, grouped by family and tier.
- The Java sorting interface stays unchanged. Future mechanics may require relationship facts,
  utility adjustments, lifecycle facts, or a visual-family entry, plus focused regressions for the
  affected seam.

## Verification contract

Focused regressions cover:

- arbitrary early, middle, and late gear tiers arriving in mixed input order;
- Bowfa and Crystal bow with base and coloured Crystal armour variants;
- dynamic Void style selection from the rest of the owned bank;
- all owned Void helms beside the shared Void core;
- complete Moon and Barrows activation sets;
- incomplete three-role Barrows families, same-role variant rejection, and two-role Karil separation;
- Ghostly, Elite black, and dwarf cannon family cohesion;
- high-impact utility priority, base versus imbued god capes, and Karil crossbow demotion;
- broken Moon armour and Barrows zero states in maintenance;
- no invented blanks, lost items, or duplicated items;
- vertical helmet, body, legs, and weapon alignment using arbitrary item IDs rather than named sets;
- placeholder exclusion, damaged Torva, uncharged Serpentine helms, and duplicate physical entries;
- full catalog key coverage so a deleted or accidentally added mechanic recipe fails a test;
- ammunition word boundaries so `Barrows` cannot be mistaken for `arrows`.

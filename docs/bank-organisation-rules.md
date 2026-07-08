# Bank Organisation Rules

This document captures product rules for how Ironman Bank Architect understands and groups bank
items. It describes intent and design rules, not implementation details.

These rules are product inspiration and plugin design rules. They are not hard claims about any
one specific YouTube video unless independently verified.

## HERB_001 — Herblore prep workflows

A Herblore prep workflow is a left-to-right chain that turns a farmed or grimy ingredient into a
finished potion, ending in its partial-dose byproducts. Example order:

`seed → grimy herb → clean herb → unfinished potion → secondary → partial doses`

The seed and the finished full-dose potion are treated as boundary items: the seed feeds the
Farming side of the chain, and partial doses are what remains after some doses are drunk.

## POT_001 — Potion / Consumables dose grids

Potions are grouped as a dose grid: `(4) → (3) → (2) → (1)`. A 4-dose potion is a combat or
supply item, not a Herblore prep item — it belongs with ready-to-use combat/consumable supplies,
not the ingredient-to-unfinished-potion prep chain in HERB_001.

## GEAR_001 — Complete gearset blocks

A gearset block lists one full loadout in a fixed role order:

`Helmet → Cape → Amulet → Body → Legs → Gloves → Boots → Weapon`

This role order may later expand to include offhand, ring, ammo, and spec weapon slots as
additional gearset blocks are added.

## TELE_001 — Compact rune and teleport utility grouping

Runes and teleport utilities (teleport tabs, tablets, staves, jewellery charges) are grouped
compactly by destination or spell-book utility, favouring density over per-item detail.

## SKILL_001 — Tool → raw resource → processed resource workflow

Gathering and processing skills follow a tool-to-output chain, for example:

- `pickaxe → ore → bar`
- `axe → logs → planks/fletching outputs`

## CLEAN_001 — External storage recommendations

Some item groups are better suited to dedicated external storage than a bank slot: Seed Vault,
POH Costume Room, STASH units, Potion Storage, and similar. These are advisory-only suggestions
in the plugin — no automation moves items into external storage.

## TAG_001 — Future Bank Tag Layout export

The blueprint engine should support fixed 8-column grids, including empty slots and placeholders,
so a blueprint can later be exported as a clean, ready-to-use Bank Tag Layout.

## Future catalog work (not yet implemented)

Super attack (4), item ID 2436, is not part of the HERB_001 Irit prep workflow. It is expected to
join a future Potion / Consumables / PvM Supplies catalog area alongside other full-dose combat
potions.

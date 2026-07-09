# Preset Category Roadmap

Bank Architect uses blueprint categories, not native bank tab mutation. These categories describe
where owned bank items should be planned in the generated layout. The player still moves every item
manually.

The first preset set is based on public OSRS bank organization patterns: general players commonly
group banks by gear, supplies, skilling, quests/clues, farming, loot, and junk/review; Ironman banks
often split currencies/teleports, supplies, gear/slayer, skilling, farming/herblore, loot, cosmetics,
and clue/storage review.

## Ironman - All-Round Bank

Ironman keeps gear together because Ironman accounts accumulate many sidegrades, niche pieces,
slayer items, clue gear, god items, and progression items.

1. Currency & Account Utilities
2. Teleports, Runes & Jewellery
3. Combat Gear
4. Potions, Food & PvM Supplies
5. Farming & Herblore
6. Skilling Tools
7. Raw & Processed Resources
8. Slayer, Boss Loot & Unique Drops
9. Clues, Cosmetics & Collection Log
10. Storage & Cleanup Review

## Main - General Bank

1. Currency & Tradeables
2. Teleports & Runes
3. Combat Gear
4. Potions & Food
5. Skilling Supplies
6. Farming & Herblore
7. Bossing & Slayer Loot
8. Clues & Collection Log
9. Cosmetics & Outfits
10. Junk, Sell & Storage Review

## PvM Bank

1. Core Currency & Utilities
2. Teleports & Escape Items
3. Potions, Food & Restores
4. Melee Gear
5. Ranged Gear & Ammo
6. Magic Gear & Runes
7. Spec Weapons & Switches
8. Slayer & Boss Tools
9. Loot, Drops & Splits
10. Low-Use Gear & Review

## PvP Bank

1. Coins, Risk & Utility
2. Teleports, Escapes & Return Sets
3. Food, Potions & Combo Eats
4. Melee PK Gear
5. Ranged PK Gear & Ammo
6. Magic PK Gear & Runes
7. Spec Weapons & KO Items
8. Wilderness Tools & Supplies
9. Replacement Sets
10. Loot, Keys & Review

## Skiller Bank

1. Currency & Utilities
2. Teleports & Runes
3. Farming
4. Herblore Materials
5. Fishing & Cooking
6. Woodcutting & Fletching
7. Mining & Smithing
8. Crafting, RC & Construction
9. Tools, Outfits & Pets
10. Loot, Clues & Storage Review

## Implementation Notes

- Preset categories are planning categories, not automatic sorter targets.
- The generated item registry recognizes item IDs; curated catalog and rule mapping decide where
  items belong.
- `Uncategorized` means recognized by ID but not yet assigned a curated semantic category.
- Future C5/C6 work should reduce `Uncategorized` by adding curated item categories and name/tag
  rules, not by copying third-party plugin taxonomies.

# Cleanup exit review, pass 1

Date: 2026-07-18

## Protocol and aggregate result

This first Workstream A exit review used approved cleanup benchmark protocol v1:
seeds `20260718`, `314159265`, and `271828182`; 200 generated banks per
seed; three scenarios per generated bank; 1,800 scenario banks total; and
registry SHA-256
`449712144c522f622f975c9b7667a9f84c43da57260da40fa428ea2d7515b038`.
All 1,800 scenario banks completed before and after curation.

| Measurement | Before | After | Net moved from cleanup |
|---|---:|---:|---:|
| Distinct item IDs | 9,481 | 9,434 | 47 |
| Occurrences | 47,550 | 47,241 | 309 |

The input aggregate SHA-256 was
`92D115BA72390F6250BBB5B4285814EA9040291783725A9BC9E50C3889B25764`.
The final aggregate SHA-256 is
`8D697A27E3C0212DCB86476F7487FB446F4C4EE9FCAE2A65E46EC8B911930B65`.

Curation moved 54 sampled IDs / 348 occurrences out of cleanup. Completing
those families also corrected ten spoilt/burnt Gnome states from accidental
gear routing to explicit cleanup; seven of those states were sampled, adding
7 IDs / 39 occurrences. The resulting net change is therefore 47 IDs / 309
occurrences.

## Families curated in this pass

Every family uses exact normal-game player-facing IDs from the local
registry/gameval-derived index. ALUFT delivery/cache variants, CERT,
PLACEHOLDER, and internal dummy records remain excluded.

| Family | Routed IDs | Category | IRONMAN tab |
|---|---:|---|---|
| Cocktail spirits | 4 | POTION | potions-food |
| Gnome cocktails, ready and unfinished | 33 | POTION / SKILLING | potions-food / resources |
| Gnome bowls, ready and unfinished | 15 | POTION / SKILLING | potions-food / resources |
| Gnome crunchies, ready and unfinished | 14 | POTION / SKILLING | potions-food / resources |
| Gnome battas, ready and unfinished | 23 | POTION / SKILLING | potions-food / resources |
| POH barrel drinks and empty beer glass | 7 | POTION / TOOL | potions-food / skilling-tools |
| Keldagrim shirts and trousers | 6 | CLUE | clues-cosmetics |
| Tower of Life satchels | 6 | CLUE | clues-cosmetics |

Ten reviewed spoilt/burnt lifecycle states remain explicitly in CLEANUP. This
pass adds routing only and no recipe, dose, charge, or variant ordering
metadata.

## Final top-250 disposition summary

| Disposition | Rows |
|---|---:|
| Legitimate cleanup | 67 |
| Non-player-facing or cache record | 172 |
| Intentionally deferred borderline | 11 |
| Classification bug / clearly bankable item | 0 |
| **Total** | **250** |

The table below is the final aggregate order: occurrence count descending,
then item ID ascending. Every row has exactly one disposition.

| itemId | Canonical name | Occurrences | Disposition | Reason / prior reference |
|---:|---|---:|---|---|
| 1668 | Invis Necklace1 | 21 | Non-player/cache | Round 3: internal invisible-necklace state. |
| 10489 | Bar magnet | 21 | Legitimate cleanup | Round 2: one-use Animal Magnetism quest material. |
| 5599 | Tin | 18 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 8656 | Banner | 18 | Non-player/cache | Round 3: POH/build-option object, not a carried item. |
| 14424 | Holy Book G Page1 | 18 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 17316 | Eadgar Dried Troll Thistle | 18 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 18776 | Oil Lamp Lit | 18 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 611 | Locating crystal | 15 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 3283 | 20 Bind | 15 | Non-player/cache | Round 3: spellbook/interface record, not a bank item. |
| 4426 | Comfy mattress | 15 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 4434 | Weathervane pillar | 15 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 5810 | Keg Dragon Bitter 4 | 15 | Non-player/cache | Registry: CERT / excluded declaration; prior excluded-copy disposition. |
| 6307 | Village Trade Sticks2 | 15 | Non-player/cache | Internal/count-view or puzzle-state record, not a bank item. |
| 6930 | Bookindex2 | 15 | Non-player/cache | Internal/count-view or puzzle-state record, not a bank item. |
| 7514 | Dyed orange | 15 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 7727 | Poh Teapot Giltporcelain Empty | 15 | Non-player/cache | Registry: CERT / excluded declaration; prior excluded-copy disposition. |
| 9509 | Aluft Wizard Blizzard | 15 | Non-player/cache | Registry: CERT / excluded declaration; prior excluded-copy disposition. |
| 9611 | Green square | 15 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 9622 | Violet triangle | 15 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 10894 | Brain tongs | 15 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 11750 | Crystal bow 9/10 (i) | 15 | Non-player/cache | Rounds 2-3: historical or obsolete combat copy. |
| 11754 | Crystal bow 5/10 (i) | 15 | Non-player/cache | Rounds 2-3: historical or obsolete combat copy. |
| 12360 | Leprechaun Hat | 15 | Non-player/cache | Registry: CERT / excluded declaration; prior excluded-copy disposition. |
| 12657 | Junk | 15 | Legitimate cleanup | Explicit junk record. |
| 13700 | Waterrune | 15 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 15370 | Salamander Tar Green | 15 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 15377 | Hunting Trousers Jungle | 15 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 15737 | Elite Void Knight Top | 15 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 15902 | Cavalier Red | 15 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 16289 | Reddye | 15 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 17139 | Olaf2 Gate Key 4 | 15 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 17197 | Rats Tail | 15 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 17451 | Rag Vinegar | 15 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 18707 | Aluft Delivery Box | 15 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 18993 | Aluft Chocolate Bomb | 15 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 19262 | Mistrune | 15 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 19467 | Cup Guthix Rest 3 | 15 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 20440 | Evil Chicken Head | 15 | Non-player/cache | Registry: CERT / excluded declaration; prior excluded-copy disposition. |
| 21585 | Fossil Medium 3 | 15 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 21674 | Paragraph of text | 15 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 22197 | Birdhouse Mahogany | 15 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 22330 | Deadman starter pack | 15 | Non-player/cache | Round 3: alternate-mode/activity copy. |
| 22426 | Deed | 15 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 22526 | Coin pouch | 15 | Legitimate cleanup | Player-facing but inventory-only pickpocket counter, not a bank resource. |
| 23010 | Fguild Book Flowers | 15 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 23034 | Karuulm Notes 6 | 15 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 23797 | Sote Baxtorian Book | 15 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 24029 | Prif Elven Clothes Top 4 | 15 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 24937 | Mahogany cupboard | 15 | Non-player/cache | Round 3: POH/build-option object, not a carried item. |
| 25051 | Trailblazer Steel Trophy | 15 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 25109 | League Second Inventory Open | 15 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 25119 | Hw20 Candy Blue | 15 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 25703 | Ds2 Ungael Notes Complete | 15 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 25824 | Research notes | 15 | Non-player/cache | Registry: TOP_LEVEL / excluded declaration; prior excluded-copy disposition. |
| 25997 | Sigil of the formidable fighter | 15 | Non-player/cache | Round 3: alternate-mode/activity copy. |
| 26605 | Treasure clue one | 15 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 26946 | Pharaohs Sceptre | 15 | Non-player/cache | Registry: CERT / excluded declaration; prior excluded-copy disposition. |
| 27144 | Pride22 Flower Crown Asexual | 15 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 27397 | Adventurers Top T2 | 15 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 27677 | Wild Cave Accursed Uncharged Recol | 15 | Non-player/cache | Registry: CERT / excluded declaration; prior excluded-copy disposition. |
| 28387 | Unfinished serum | 15 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 28711 | Trailblazer reloaded rejuvenation pool | 15 | Non-player/cache | Round 3: alternate-mode/activity copy. |
| 29527 | Weight (1kg) | 15 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 30428 | Raging echoes cane | 15 | Non-player/cache | Round 3: alternate-mode/activity copy. |
| 30946 | Vmq4 Basement Key | 15 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 31209 | Bingo Torch Scroll | 15 | Non-player/cache | Registry: CERT / excluded declaration; prior excluded-copy disposition. |
| 31261 | Sailor's mirage | 15 | Non-player/cache | Activity/macro record; prior activity-copy disposition applies. |
| 31516 | Coral Umbral Frag | 15 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 31881 | Bottle of dwarvern wizard's stout bomb | 15 | Non-player/cache | Activity/macro record; prior activity-copy disposition applies. |
| 33696 | Venator Heart | 15 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 85 | Shiny key | 12 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 503 | Macro Broken Mithril Hatchet | 12 | Non-player/cache | Registry: CERT / excluded declaration; prior excluded-copy disposition. |
| 585 | Bailing bucket | 12 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 966 | Tile | 12 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 980 | Cert Highwayman Mask | 12 | Non-player/cache | Registry: TOP_LEVEL / excluded declaration; prior excluded-copy disposition. |
| 1560 | Pet kitten | 12 | Deferred borderline | Maintainer: confirm this exact live cat ID can be banked and choose cosmetics or cleanup. |
| 1589 | Cert Grip Keys | 12 | Non-player/cache | Registry: TOP_LEVEL / excluded declaration; prior excluded-copy disposition. |
| 1667 | Cert Black Necklace | 12 | Non-player/cache | Registry: TOP_LEVEL / excluded declaration; prior excluded-copy disposition. |
| 1807 | Silver pot | 12 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 1852 | Bedabin key | 12 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 1904 | Burnt Cake | 12 | Non-player/cache | Registry: CERT / excluded declaration; prior excluded-copy disposition. |
| 1988 | Grapes | 12 | Non-player/cache | Registry: CERT / excluded declaration; prior excluded-copy disposition. |
| 1994 | Jug Wine | 12 | Non-player/cache | Registry: CERT / excluded declaration; prior excluded-copy disposition. |
| 2006 | Burnt Stew | 12 | Non-player/cache | Registry: CERT / excluded declaration; prior excluded-copy disposition. |
| 2377 | Ogre tooth | 12 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 2380 | Crystal | 12 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 2469 | Flowers Waterfall Quest Purple | 12 | Non-player/cache | Registry: CERT / excluded declaration; prior excluded-copy disposition. |
| 2652 | Piratehat | 12 | Non-player/cache | Registry: CERT / excluded declaration; prior excluded-copy disposition. |
| 3072 | Cert Macro Cube Bluesquare | 12 | Non-player/cache | Registry: TOP_LEVEL / excluded declaration; prior excluded-copy disposition. |
| 3076 | Cert Macro Cube Redcircle | 12 | Non-player/cache | Registry: TOP_LEVEL / excluded declaration; prior excluded-copy disposition. |
| 3109 | Stone ball | 12 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 3231 | Symbol | 12 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 3269 | Storeroom key | 12 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 3423 | Oliveoil4 | 12 | Non-player/cache | Registry: CERT / excluded declaration; prior excluded-copy disposition. |
| 3557 | Casket (hard) | 12 | Deferred borderline | Maintainer: prove this step-specific clue casket is a current bankable ID, then choose clue routing. |
| 3583 | Casket (medium) | 12 | Deferred borderline | Maintainer: prove this step-specific clue casket is a current bankable ID, then choose clue routing. |
| 3725 | 2/5ths full bucket | 12 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 3741 | Frozen key | 12 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 3898 | Giant pen | 12 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 3899 | Iron sickle | 12 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 3976 | Reinitialisation 13 Inactive | 12 | Non-player/cache | Internal/count-view or puzzle-state record, not a bank item. |
| 3980 | Reinitialisation 15 Inactive | 12 | Non-player/cache | Internal/count-view or puzzle-state record, not a bank item. |
| 3998 | Reinitialisation 24 Inactive | 12 | Non-player/cache | Internal/count-view or puzzle-state record, not a bank item. |
| 4043 | Rock | 12 | Non-player/cache | Activity/macro record; prior activity-copy disposition applies. |
| 4057 | Castlewars No1 | 12 | Non-player/cache | Internal/count-view or puzzle-state record, not a bank item. |
| 4221 | Crystal bow 3/10 | 12 | Non-player/cache | Rounds 2-3: historical or obsolete combat copy. |
| 4428 | Animate rock scroll | 12 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 4571 | Book page 3 | 12 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 4575 | Schematic | 12 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 4645 | 82 Ice Blitz | 12 | Non-player/cache | Round 3: spellbook/interface record, not a bank item. |
| 4669 | Fd Crushed Garlic | 12 | Non-player/cache | Registry: CERT / excluded declaration; prior excluded-copy disposition. |
| 5035 | Dwarf Shirt3 | 12 | Non-player/cache | Registry: CERT / excluded declaration; prior excluded-copy disposition. |
| 5427 | Sack Potato 4 | 12 | Non-player/cache | Registry: CERT / excluded declaration; prior excluded-copy disposition. |
| 5536 | Tiara Earth | 12 | Non-player/cache | Registry: CERT / excluded declaration; prior excluded-copy disposition. |
| 5584 | Cupric ore powder | 12 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 5909 | Chef's delight(m2) | 12 | Legitimate cleanup | Round 2 maintainer decision: mature brewing stays cleanup. |
| 5946 | Antidote 3 | 12 | Non-player/cache | Registry: CERT / excluded declaration; prior excluded-copy disposition. |
| 6074 | Mourning Book1 | 12 | Non-player/cache | Registry: CERT / excluded declaration; prior excluded-copy disposition. |
| 6098 | Toxic powder | 12 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 6113 | Kelda hops | 12 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 6191 | Macro Quiz Weapon1 | 12 | Non-player/cache | Activity/macro record; prior activity-copy disposition applies. |
| 6199 | Mystery box | 12 | Deferred borderline | Maintainer: confirm random-event mystery-box bankability and intended long-term bank use. |
| 6653 | Crystal trinket | 12 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 6695 | Desert lizard | 12 | Non-player/cache | Guide/interface icon record, not the named bank item. |
| 6699 | Burnt potato | 12 | Legitimate cleanup | Burnt food is intentional cleanup. |
| 6827 | Star bauble | 12 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 6849 | Bell bauble | 12 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 6950 | Magical orb | 12 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 6990 | Stone head | 12 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 7002 | Stone head | 12 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 7125 | Pirate Bandana Red | 12 | Non-player/cache | Registry: CERT / excluded declaration; prior excluded-copy disposition. |
| 7129 | Pirate Torso Blue | 12 | Non-player/cache | Registry: CERT / excluded declaration; prior excluded-copy disposition. |
| 7246 | Casket (hard) | 12 | Deferred borderline | Maintainer: prove this step-specific clue casket is a current bankable ID, then choose clue routing. |
| 7318 | Casket (medium) | 12 | Deferred borderline | Maintainer: prove this step-specific clue casket is a current bankable ID, then choose clue routing. |
| 7468 | Pot of cornflour | 12 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 7557 | 100Guide Waterrune Dum | 12 | Non-player/cache | Registry: CERT / excluded declaration; prior excluded-copy disposition. |
| 7580 | Snake over-cooked | 12 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 7581 | Overgrown hellcat | 12 | Deferred borderline | Maintainer: confirm this exact live cat ID can be banked and choose cosmetics or cleanup. |
| 7587 | Coffin | 12 | Non-player/cache | Activity/macro record; prior activity-copy disposition applies. |
| 7623 | Burgh Rubble Bucket 1 | 12 | Non-player/cache | Registry: CERT / excluded declaration; prior excluded-copy disposition. |
| 7709 | Poh Teapot Porcelain 2 | 12 | Non-player/cache | Registry: CERT / excluded declaration; prior excluded-copy disposition. |
| 7713 | Poh Teapot Porcelain Leaves | 12 | Non-player/cache | Registry: CERT / excluded declaration; prior excluded-copy disposition. |
| 7842 | Ogre ribs | 12 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 7908 | Big frog leg | 12 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 7960 | Empty box | 12 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 8123 | Steel-plated door | 12 | Non-player/cache | Round 3: POH/build-option object, not a carried item. |
| 8173 | Tree | 12 | Non-player/cache | Round 3: POH/build-option object, not a carried item. |
| 8253 | Teak staircase | 12 | Non-player/cache | Round 3: POH/build-option object, not a carried item. |
| 8255 | Marble staircase | 12 | Non-player/cache | Round 3: POH/build-option object, not a carried item. |
| 8285 | King arthur | 12 | Non-player/cache | Round 3: POH/build-option object, not a carried item. |
| 8358 | Teak throne | 12 | Non-player/cache | Round 3: POH/build-option object, not a carried item. |
| 8371 | Steel cage | 12 | Non-player/cache | Round 3: POH/build-option object, not a carried item. |
| 8664 | Banner | 12 | Non-player/cache | Round 3: POH/build-option object, not a carried item. |
| 8674 | Banner | 12 | Non-player/cache | Round 3: POH/build-option object, not a carried item. |
| 9001 | Pirate Bandana Eyepatch Brown | 12 | Non-player/cache | Registry: CERT / excluded declaration; prior excluded-copy disposition. |
| 9032 | Pottery scarab | 12 | Legitimate cleanup | Quest-junk pottery artefact. |
| 9094 | Kindling | 12 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 9112 | 70 Tele Moonclan Group | 12 | Non-player/cache | Round 3: spellbook/interface record, not a bank item. |
| 9126 | 84 Statboost Pot Share | 12 | Non-player/cache | Round 3: spellbook/interface record, not a bank item. |
| 9541 | Aluft Spicy Crunchies | 12 | Non-player/cache | Registry: CERT / excluded declaration; prior excluded-copy disposition. |
| 9589 | Dossier | 12 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 9616 | Blue pentagon | 12 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 9651 | Large ornate key | 12 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 9913 | White destabiliser | 12 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 9919 | Evil root | 12 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 9963 | Kebbit | 12 | Non-player/cache | Guide/interface icon record, not the named bank item. |
| 10179 | Feathered journal | 12 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 10492 | Research notes | 12 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 10493 | Translated notes | 12 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 10562 | Queen help book | 12 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 10905 | Brain Inv Skull Staple Countview 2 | 12 | Non-player/cache | Internal/count-view or puzzle-state record, not a bank item. |
| 10985 | Fuse | 12 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 10999 | Goblin book | 12 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 11042 | Key | 12 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 11152 | Dream vial (water) | 12 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 11359 | Ancient page | 12 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 11777 | Black mask (7) (i) | 12 | Non-player/cache | Rounds 2-3: historical or obsolete combat copy. |
| 12336 | Trail Briefcase | 12 | Non-player/cache | Registry: CERT / excluded declaration; prior excluded-copy disposition. |
| 12551 | Casket (hard) | 12 | Deferred borderline | Maintainer: prove this step-specific clue casket is a current bankable ID, then choose clue routing. |
| 12764 | Bh White Paint | 12 | Non-player/cache | Registry: CERT / excluded declaration; prior excluded-copy disposition. |
| 13187 | Package | 12 | Legitimate cleanup | Quest, junk, holiday, lore, or one-use item; review/drop storage is intentional. |
| 13476 | Arceuus Corpse Giant | 12 | Non-player/cache | Registry: CERT / excluded declaration; prior excluded-copy disposition. |
| 13881 | Dibber | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 14066 | Gnome Hat Cream | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 14077 | Pack Firerune | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 14294 | Fur | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 14436 | Zaros Page1 | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 15302 | Snakepet | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 15352 | Hunting Fur Tiger Shabby | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 15539 | Brut Document 11 | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 15657 | Ii Jar Generator | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 15909 | Wise Spectacles | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 15937 | Royal Crown | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 16171 | Ntk Statuette Gold | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 16373 | Brain Broken Anchor | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 16457 | Four Diamonds Translation Primer | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 16565 | Enakh Statue Head Lazim | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 16585 | Tol Rivets | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 16612 | Specimen Brush | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 16644 | Nitroglycerin | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 16662 | Misc Giant Nib | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 16690 | Lunar Moonclan Headgear | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 16800 | Vial Enchanted | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 16836 | Peng Cowbell | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 16959 | Troll Key Eadgar | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 17004 | Ics Little Bookofembalming | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 17020 | Murderpot | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 17055 | Elena Picture | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 17163 | Cavewitchcat | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 17186 | Forget Gardener Letter | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 17341 | Slug2 Page2 | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 17351 | Slug2 Rune Earth | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 17381 | Hundred Pirate Giant Crab Meat | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 17385 | Hundred Pirate Giant Crab Meat 2 | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 17413 | Chickenquest Pot Cornflour | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 17595 | Jail Key | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 17675 | Red Bead | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 17785 | Bucket Ectoplasm | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 17937 | Mm Ancient Monkey Skull | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 18206 | Lazycatobject Hell | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 18386 | Dagganoth Range Feet | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 18405 | Dorgesh Frog Spawn Gumbo | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 18411 | Dorgesh Whole Roasted Frog | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 18537 | Arceuus Corpse Demon | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 18590 | Magictraining Proghat Dull | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 18593 | Magictraining Bookofmagic | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 18599 | Magictraining Infinitytop | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 18678 | Seers Headband Elite | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 18713 | Village Spider Carcass | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 18791 | Bullseye Lantern Lit | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 18823 | Vm Pottery Inv | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 18864 | Shellround Swamp | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 18867 | Shellround Blue | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 18989 | Aluft Chocchip Crunchies | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 19017 | Aluft Shaker Pineapple Punch | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 19034 | Crunchy Tray | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 19048 | Premade Worm Crunchies | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 19081 | Brandy | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 19091 | Lime | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 19153 | Dragon Bitter | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 19198 | Potato Baked | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 19239 | Rcu Pouch Medium | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 19594 | Ballista Limbs | 12 | Non-player/cache | Registry: PLACEHOLDER / excluded declaration; prior excluded-copy disposition. |
| 19765 | Challenge scroll (medium) | 12 | Deferred borderline | Maintainer: prove this step-specific challenge scroll is a current bankable ID, then choose clue routing. |
| 19861 | Casket (hard) | 12 | Deferred borderline | Maintainer: prove this step-specific clue casket is a current bankable ID, then choose clue routing. |
| 19897 | Puzzle box (hard) | 12 | Deferred borderline | Maintainer: prove this step-specific puzzle box is a current bankable ID, then choose clue routing. |
| 20325 | Trail Slidingpuzzle Cerberus 19 | 12 | Non-player/cache | Clue sliding-puzzle cache state, not a carried puzzle-box ID. |
| 20331 | Trail Slidingpuzzle Gnomechild 01 | 12 | Non-player/cache | Clue sliding-puzzle cache state, not a carried puzzle-box ID. |
| 20406 | Dragon scimitar | 12 | Non-player/cache | Round 3: alternate-mode/activity copy. |
| 20424 | Black d'hide chaps | 12 | Non-player/cache | Round 3: alternate-mode/activity copy. |

## Deferred borderline families

| Family / rows | Decision owner | Evidence required |
|---|---|---|
| Live cat objects: 1560, 7581 | Maintainer | Demonstrate that each exact ID can appear in the bank on the pinned registry revision, then choose `clues-cosmetics` or intentional cleanup. |
| Legacy step-specific clue states: 3557, 3583, 7246, 7318, 12551, 19765, 19861, 19897 | Maintainer | Demonstrate that the current client can produce and bank each exact ID, rather than a modern tier casket/puzzle ID or cache state, then approve clue routing. |
| Random-event mystery box: 6199 | Maintainer | Confirm bank deposit support and a deliberate long-term bank use, then choose supplies/loot or intentional cleanup. |

There is no ownerless deferral and no unresolved classification bug in the
final top 250.

## Cumulative Workstream A accounting

| Round | Distinct IDs moved | Occurrences moved | Measurement note |
|---|---:|---:|---|
| Round 1 | 29 | 102 | Original single-seed era |
| Round 2 | 104 | 747 | Protocol v1 |
| Round 3 | 91 | 495 | Protocol v1 |
| Exit review pass 1 | 54 | 348 | Protocol v1 gross moved out; net 47 / 309 after cleanup corrections |
| **Cumulative** | **278** | **1,692** | Requested cross-era additive moved-out accounting |

Round 1 predates the aggregate benchmark, so its occurrence count is retained
for historical cumulative accounting but is not statistically comparable to
the protocol-v1 counts. The cumulative row counts items moved out of cleanup;
the final aggregate additionally includes the seven IDs / 39 occurrences that
this pass correctly moved from accidental gear routing into cleanup.

The residual protocol-v1 cleanup aggregate contains 9,434 distinct item IDs
and 47,241 occurrences. Within its top 250, the residual dispositions are 67
legitimate cleanup, 172 non-player/cache, 11 maintainer-owned deferrals, and
zero classification bugs.

## Exit-condition status after pass 1

Conditions 1-4, 6, and 7 are satisfied: protocol v1 covers three seeds and
1,800 completed scenario banks; all final top-250 rows are dispositioned; no
classification bug remains; every deferral has an owner and evidence gate;
cumulative and residual totals are recorded; and unknown-ID controls plus the
required build/simulation gates pass. Condition 5, the confirming second pass
against the same registry revision and protocol, remains deliberately pending.

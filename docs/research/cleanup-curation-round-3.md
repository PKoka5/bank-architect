# Cleanup curation round 3

Date: 2026-07-18

## Input and measured result

This bounded review used cleanup benchmark protocol v1 from
`docs/cleanup-review-benchmark-protocol.md`: seeds `20260718`, `314159265`, and
`271828182`; 200 generated banks per seed; three scenarios per generated bank;
1,800 scenario banks total; and registry SHA-256
`449712144c522f622f975c9b7667a9f84c43da57260da40fa428ea2d7515b038`.
The regenerated aggregate, rather than the single-seed quick-check report, was
the review source.

| Measurement | Before | After | Moved from cleanup |
|---|---:|---:|---:|
| Distinct item IDs | 9,572 | 9,481 | 91 |
| Occurrences | 48,045 | 47,550 | 495 |

All 1,800 scenario banks completed in both measurements. The post-curation
aggregate SHA-256 is
`92D115BA72390F6250BBB5B4285814EA9040291783725A9BC9E50C3889B25764`.
The reviewed families cover 177 exact player-facing IDs. Ninety-one of those
IDs occurred in the baseline cleanup aggregate and account for the full
measured reduction. None of the 177 reviewed IDs appears in the regenerated
cleanup aggregate.

## Curated families

Every family was checked against the local registry/gameval-derived research
index. CERT, PLACEHOLDER, blank internal, historical activity, and alternate
game-mode records were excluded unless explicitly listed as a normal-game
player-facing state.

| Family | Player-facing IDs | Category | IRONMAN tab |
|---|---:|---|---|
| Six-god Treasure Trails coifs | 6 | CLUE | clues-cosmetics |
| Six-god Treasure Trails croziers | 6 | CLUE | clues-cosmetics |
| Six-god Treasure Trails cloaks | 6 | CLUE | clues-cosmetics |
| Six-god Treasure Trails mitres | 6 | CLUE | clues-cosmetics |
| Six-god Treasure Trails robe tops | 6 | CLUE | clues-cosmetics |
| Six-god Treasure Trails robe bottoms | 6 | CLUE | clues-cosmetics |
| Six-god Treasure Trails stoles | 6 | CLUE | clues-cosmetics |
| Treasure Trails metal canes | 4 | CLUE | clues-cosmetics |
| Treasure Trails headbands | 8 | CLUE | clues-cosmetics |
| Treasure Trails boaters | 8 | CLUE | clues-cosmetics |
| Treasure Trails cavaliers | 6 | CLUE | clues-cosmetics |
| Treasure Trails berets | 4 | CLUE | clues-cosmetics |
| Standalone Treasure Trails novelty clothing | 5 | CLUE | clues-cosmetics |
| Chompy-bird kill-count hats | 18 | CLUE | clues-cosmetics |
| Fremennik cloaks | 11 | CLUE | clues-cosmetics |
| Champion scrolls | 11 | CLUE | clues-cosmetics |
| Temple Trekking skill tomes | 21 | SKILLING | resources |
| Kyatt, larupia, and graahk hunter-fur outfits | 9 | GEAR | combat-gear |
| Wood, jungle, desert, and polar camouflage outfits | 8 | TOOL | skilling-tools |
| Rangers' tunic and tights | 2 | GEAR | combat-gear |
| Amylase crystal and pack | 2 | SKILLING | resources |
| Normal-game Voidwaker components and assembled weapon | 4 | UNIQUE / GEAR | slayer-boss-loot / combat-gear |
| Rod of Ivandis production, charge, and mould states | 14 | SKILLING / GEAR / TOOL | resources / combat-gear / skilling-tools |

This round adds category/tab routing only. It deliberately does not add charge,
tier, colour, or degradation ordering metadata.

## Intentional cleanup dispositions

Round 2's mature brewing, bar magnet, historical crystal-bow, and excluded
record dispositions remain binding and are not repeated below.

| Rows/family left in cleanup | Disposition |
|---|---|
| Garden of Tranquillity ornamental seed family | Existing reviewed quest-only override: these seeds serve Queen Ellamaria's quest garden and have no continuing normal-bank Farming workflow. |
| `EASTER_EGG_2005_PURPLE` (item 4561) | Historical event record; the current player-facing Purple sweets item (10476) is already curated as food. |
| Numeric spell records such as `20 Bind`, group teleports, and potion-share entries | Spellbook/interface records rather than bank items; retain fail-closed routing. |
| POH furniture/build-option records such as placed banners, staircases, and cupboards | Construction interface/object records, not carried flatpack or material items. |
| Deadman, League, Battle Royale, macro, and other activity-specific copies | Alternate-mode/activity records remain cleanup even when their display name resembles normal gear or supplies. |
| `INVIS_NECKLACE1` / `INVIS_NECKLACE2` | Internal invisible-necklace state records with no reviewed normal player-facing bank workflow. |


# Cleanup curation round 2

Date: 2026-07-18

## Input and measured result

This bounded review used cleanup benchmark protocol v1 from
`docs/cleanup-review-benchmark-protocol.md`: seeds `20260718`, `314159265`, and
`271828182`; 200 generated banks per seed; three scenarios per generated bank;
1,800 scenario banks total; and registry SHA-256
`449712144c522f622f975c9b7667a9f84c43da57260da40fa428ea2d7515b038`.
The aggregate, rather than the single-seed quick-check report, was the review
source.

| Measurement | Before | After | Moved from cleanup |
|---|---:|---:|---:|
| Distinct item IDs | 9,676 | 9,572 | 104 |
| Occurrences | 48,792 | 48,045 | 747 |

All 1,800 scenario banks completed in both measurements. The post-curation
aggregate SHA-256 is
`099482638118F941C5E60AFB2DDA2AB4D86840FAE0A8EEBEBB43634A314F1B15`.
The rules cover 166 exact player-facing IDs. Of those, 104 occurred in the
baseline cleanup aggregate; the other complete-family states were not sampled
there or, for the lit bullseye lantern, were already curated. None of the 165
new override IDs appears in the regenerated cleanup aggregate.

## Curated families

Every family was checked against the local registry/gameval-derived research
index. CERT and PLACEHOLDER records were excluded and remain fail-closed.

| Family | Player-facing IDs | Category | IRONMAN tab |
|---|---:|---|---|
| Standard candles | 2 | TOOL | skilling-tools |
| Candle lanterns | 5 | TOOL | skilling-tools |
| Cocktail shaker | 1 | TOOL | skilling-tools |
| Treasure Trails sextant | 1 | CLUE | clues-cosmetics |
| Crystal keys | 2 | UNIQUE | slayer-boss-loot |
| Shade keys | 25 | UNIQUE | slayer-boss-loot |
| Bullseye lantern components and states | 5 | SKILLING / TOOL | resources / skilling-tools |
| Elegant outfits | 28 | CLUE | clues-cosmetics |
| Prayer harness packs | 3 | GEAR | combat-gear |
| God banners | 2 | CLUE | clues-cosmetics |
| God-book page sets | 6 | CLUE | clues-cosmetics |
| Fossilised remains | 25 | SKILLING | resources |
| Revenant ether | 1 | UNIQUE | slayer-boss-loot |
| Treasure Trails strange devices | 2 | CLUE | clues-cosmetics |
| Sinhaza shrouds | 5 | CLUE | clues-cosmetics |
| Wilderness sceptres | 8 | GEAR | combat-gear |
| Crystal crowns | 8 | CLUE | clues-cosmetics |
| Repairable broken ancient sceptres | 9 | GEAR | combat-gear |
| Dizana's quivers | 12 | GEAR | combat-gear |
| Sulphur blades | 1 | GEAR | combat-gear |
| Butterfly wings | 3 | SKILLING | resources |
| Brimhaven voucher | 1 | CURRENCY | currency-utilities |
| Demon masks | 3 | CLUE | clues-cosmetics |
| Clan cloaks | 8 | CLUE | clues-cosmetics |

This round adds category/tab routing only. It deliberately does not add ordering
metadata for charged, degraded, tiered, or colour states.

## Intentional cleanup dispositions

| Rows/family left in cleanup | Disposition |
|---|---|
| Mature keg ales and the mature brewing family, including Asgarnian ale(m2) | Maintainer decision: these are rarely used in-game and deliberately belong in storage-cleanup; later rounds should not re-litigate them. |
| Bar magnet | One-use Animal Magnetism quest material, not a durable bank utility. |
| Historical gradual-degradation crystal bows and old imbued copies | Historical/removed duplicate states, not current player-facing gear families. |
| CERT, PLACEHOLDER, cache-only, macro, and activity copies adjacent to curated families | Non-player-facing records remain fail-closed and were not rescued to improve the benchmark total. |


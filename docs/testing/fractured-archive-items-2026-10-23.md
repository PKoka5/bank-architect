# Fractured Archive reward placement — 23 October 2026

Addition above published 0.9.7, included in the verified 0.9.8 build.
The owner authorized publication after this audit. No raid action, input
automation, runtime download or dependency change is introduced.

## Sources and exact identity

The [official release post](https://secure.runescape.com/m=news/the-fractured-archive---out-today?oldschool=1)
dates the raid to 20 October and records subsequent hotfixes. Item facts below
come from pinned Wiki infobox revisions, read through the public revision API
on 23 October. Names are descriptive labels; exact item IDs select the rules.

The IDs and constants were independently checked against top-level
`net/runelite/api/gameval/ItemID.java` in the local RuneLite 1.13.3 source JAR,
SHA-256 `a58c6ad1aca817b1509898e99f1cb0a6a9236bd37df095e8557dadb79a106228`.
This API version already has all 24 states; refreshing dependencies is unnecessary.
Developer-only raw research caches are ignored and are not packaged.

| Item / ordinary IDs | Pinned Wiki revision | Placement |
| --- | --- | --- |
| Ascension crossbows 34681 | [15378507](https://oldschool.runescape.wiki/w/Ascension_crossbows?oldid=15378507) | Combat, rapid Ranged weapon |
| Obligator 34715 / uncharged 34717 | [15379822](https://oldschool.runescape.wiki/w/Obligator?oldid=15379822) | Combat, Crush weapon |
| Rondache 34719 | [15379785](https://oldschool.runescape.wiki/w/Rondache?oldid=15379785) | Combat, defensive support shield |
| Zorya's tome 34725 / uncharged 34727 | [15379800](https://oldschool.runescape.wiki/w/Zorya%27s_tome?oldid=15379800) | Combat, Magic specialist weapon |
| Ascension bolts 34684 | [15379326](https://oldschool.runescape.wiki/w/Ascension_bolts?oldid=15379326) | Loot, ammunition |
| Diamond ascension bolts 34690 / (e) 34696 | [15379797](https://oldschool.runescape.wiki/w/Diamond_ascension_bolts?oldid=15379797), [15377674](https://oldschool.runescape.wiki/w/Diamond_ascension_bolts_(e)?oldid=15377674) | Loot, ammunition |
| Onyx ascension bolts 34702 / (e) 34708 | [15379798](https://oldschool.runescape.wiki/w/Onyx_ascension_bolts?oldid=15379798), [15377507](https://oldschool.runescape.wiki/w/Onyx_ascension_bolts_(e)?oldid=15377507) | Loot, ammunition |
| Ascension shards 34669 | [15377485](https://oldschool.runescape.wiki/w/Ascension_shards?oldid=15377485) | Loot, equipment charge |
| Earth / water / wind / fire fragments 34671 / 34673 / 34675 / 34677 | [15377461](https://oldschool.runescape.wiki/w/Fragment_of_living_earth?oldid=15377461), [15377462](https://oldschool.runescape.wiki/w/Fragment_of_deep_water?oldid=15377462), [15377465](https://oldschool.runescape.wiki/w/Fragment_of_whispering_wind?oldid=15377465), [15377466](https://oldschool.runescape.wiki/w/Fragment_of_burning_fire?oldid=15377466) | Loot, permanent upgrade |
| Horn of Bran 34679 | [15378763](https://oldschool.runescape.wiki/w/Horn_of_Bran?oldid=15378763) | Loot, prayer unlock |
| Archivist's folio inert 34722 / charged 34723 | [15379434](https://oldschool.runescape.wiki/w/Archivist%27s_folio?oldid=15379434) | Teleports |
| Living record tiers 1–5, 34659 / 34661 / 34663 / 34665 / 34667 | [15378103](https://oldschool.runescape.wiki/w/Living_record_(tier_1)?oldid=15378103), [15378104](https://oldschool.runescape.wiki/w/Living_record_(tier_2)?oldid=15378104), [15378109](https://oldschool.runescape.wiki/w/Living_record_(tier_3)?oldid=15378109), [15378108](https://oldschool.runescape.wiki/w/Living_record_(tier_4)?oldid=15378108), [15378112](https://oldschool.runescape.wiki/w/Living_record_(tier_5)?oldid=15378112) | Cosmetics, one family |

Only canonical bank item states are added. Certificate, placeholder-cache and
stack-display alias IDs are not inferred from neighboring numbers. The bank
snapshot's existing placeholder normalization remains responsible for real
placeholders. Dummy crossbow 34714, unbankable key 34658 and temporary raid
supplies are outside this change. This is not a claim to cover every raid object
or a pet update.

## Placement policy

- Zorya's tome is a two-handed powered Magic weapon, not an offhand book.
  Ascension crossbows are one two-handed Ranged item with their own ammunition.
  They do not prove ordinary crossbows obsolete in Flexible Alch. Their exact
  `restricted-ammo` usage tag excludes them from the general replacement-stat
  index, while retaining ownership, choices and layout. Fixed bypasses that
  index and remains literal.
- Rondache has a defensive mitigation/special role and is excluded from primary
  shield selection. It does not displace an Avernic defender on strength alone.
- All six weapon/shield states get the existing raid navigation tier 5. This is
  a layout priority, not a universal DPS comparison. Existing Twisted bow and
  Shadow priorities remain; Obligator groups with Crush specialists.
- Ascension ammo has explicit longest-first material prefixes. Onyx variants,
  Diamond variants and plain Ascension bolts stay in one compact block; older
  ammunition keeps its relative order.
- No new reward enters automatic Alch in either mode. The literal Fixed list
  still has 105 exact IDs: ordinary Onyx bolts (e) 9245 is listed, Ascension Onyx
  bolts (e) 34708 is not. Manual tags and saved physical choices retain priority.

## Regression method

`FracturedArchiveItemsTest` contains 22 parameterized tests across Main and
Ironman. It covers all 24 names/categories/subcategories, missing equipment
metadata, placeholders, exact-ID exclusions, real Wiki stat vectors beside
existing endgame gear, ammunition order, and physical copy conservation.
Impossible high-stat controls isolate automatic-Alch protection; those synthetic
vectors describe neither real equipment nor a DPS ranking.

A mixed Rune crossbow + Bowfa + Ascension regression reproduced an incorrect
Flexible Alch suggestion before the evidence guard: Bowfa's modeled stats
dominated Rune and Ascension supplied the second higher score, although neither
preserves ordinary enchanted-bolt use. The corrected case uses real stat vectors
and the Rune crossbow's [9,720 high-alch value](https://oldschool.runescape.wiki/w/Rune_crossbow),
tests quantities 3/25 in both presets, and checks that Fixed still sends Rune
crossbows to Alch in the same bank.

Appending 24 IDs grows the random item universe from 32,585 to 32,609. Since the
simulator shuffles the whole universe, the same seeds now generate different
banks. Before updating the benchmark protocol, the exact old registry (LF hash
`97331c2f6826461713807b576e6b17a0dc4fd8ffdcc5e6b7ec94f79191ff96bf`) was placed
first on a development-only replay classpath. All other code/resources were new.
All 1,950 old scenario banks completed and all four old fingerprints matched.
The replay was repeated after the crossbow evidence guard, using a local copy of
the unchanged aggregate runner with protocol-1 constants; production code and
all other resources remained current:

| Replay report | SHA-256, canonical CRLF |
| --- | --- |
| report.tsv | `28adedb12c406913a836b193ffc92fc20f10471e7c00bd97fe1b27e4d3ffb2ee` |
| cleanup-review.tsv | `309480b04ce63ce74996f4b15768e6c9a84e16611338e44c5abf689148bb3091` |
| aggregate/cleanup-review.tsv | `fce40d023f6950a6813d97276f3836c73ab801e7c0c2c13235a4e7423f207ce3` |
| aggregate/metadata.tsv | `76901b09b75f9d8219f3e1ba8b522940af373160718cd09cbf40439db088c3fe` |

The new registry's LF hash is
`2f577111a105866c26b03b142895ac719ee89a455d060535252580b0b329f028`.
Benchmark protocol 2 keeps the same seeds, sample counts, scenarios and meaning
of occurrences. All 1,950 expanded-universe scenario banks completed, with zero
unsupported plans, build errors, blocked advisors, stalls or nontermination.
Aggregate cleanup has 9,257 distinct IDs and 46,782 occurrences, versus 9,266
and 46,422 in the old-universe replay. Different sampled banks explain the
changed counts; these are not paired performance measurements. Targeted tests
cover all 24 new states rather than assuming a random sample covers them all.

The expanded reports were reviewed before replacing the four build baselines:

| Expanded report | SHA-256, canonical CRLF |
| --- | --- |
| report.tsv | `6403583c57f42c8fca16a296af54d92571d5597f3cdc773fb6061a4497a97ec1` |
| cleanup-review.tsv | `00c1f0eac9310e3ec869fe7c29aebdf4e3aa85c800b02adc01afd124f84f1fdf` |
| aggregate/cleanup-review.tsv | `ba33e9144824c0e1d0e9671f3933385f6dd6605f9b60bcbfc7ab43794a013edd` |
| aggregate/metadata.tsv | `b4eb3aa665c848f66a832f7cd0b8ecb5d64079ee6b13a2e3c3d26dd175048431` |

Final build, artifact and review-size verification are recorded in the
[0.9.8 release record](../release-0.9.8.md). Offline fixtures do not replace
an ingame check of the new rewards on a bank that owns them.

Logs: `build/tmp/fractured-archive-replay-2026-10-23.log` and
`build/tmp/fractured-archive-expanded-2026-10-23.log`. Baseline changes require
explicit review of the expanded universe, not automatic regeneration.

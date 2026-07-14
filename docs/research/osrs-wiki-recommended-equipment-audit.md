# OSRS Wiki recommended-equipment audit

Audit date: 2026-07-14 (Europe/Paris)

## Decision

`Used in recommended equipment` is adopted as a strong positive review signal, not as a Gear
whitelist. Presence can confirm combat relevance or expose a missing classification. Absence cannot
evict an item from Gear, and a recommendation cannot overrule an established Tools, Teleports,
currency, or skilling meaning.

The RuneLite plugin performs no network calls. The source is queried only by the developer research
script, and raw Wiki rows remain in a git-ignored local cache.

## Source and reproducibility

The public OSRS Wiki MediaWiki `action=bucket` endpoint exposes the `recommended_equipment` and
`infobox_item` buckets. The first stores activity/style/slot recommendation tables; the second maps
linked Wiki item pages to canonical item IDs. The audit script paginates the documented 5,000-row
maximum and uses a descriptive research user agent.

- Wiki recommended-equipment bucket rows: **453**.
- Wiki infobox-item bucket rows: **16,524**.
- Resolved recommendation occurrences: **17,797**.
- Unique recommended canonical item IDs: **889**.
- IDs matched by Bank Architect's registry: **889/889**.
- Unresolved generic or non-item link targets: **69**. These are retained as uncertainty and never
  expanded by name guessing.

Reproduce with:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File `
  tools\research\wiki-recommended-equipment\audit-recommended-equipment.ps1
```

Use `-Refresh` only when a new Wiki snapshot is intentionally required.

## What the signal measures

Each resolved observation retains:

- canonical item ID;
- equipment slot and rank in its Wiki table;
- number of distinct recommending pages;
- a conservative `combat-like`, `noncombat-like`, or `ambiguous` context label.

The context label is a review aid. It is deliberately not a production classifier: an activity can
mix combat, movement, skilling, teleports, and protective utility in one loadout.

## Production-catalog comparison

Resolving all 889 IDs through `ResourceItemRegistry.INSTANCE` produced:

| Current category | Recommended IDs |
|---|---:|
| `GEAR` | 616 |
| `CLEANUP` | 131 |
| `TOOL` | 84 |
| `SKILLING` | 23 |
| `TELEPORT` | 12 |
| `HERBLORE` | 10 |
| `CURRENCY` | 6 |
| `POTION` | 5 |
| `CLUE` | 1 |
| `RUNE` | 1 |

This baseline intentionally understates the final preview result. At runtime,
`BankOrganizationPreviewBuilder.effectiveCatalogItem(...)` promotes supported equippable items
using RuneLite gear-slot and combat-stat facts. For example, the registry-only review can show
Fighter torso, Bandos chestplate, or Mage's book as unresolved, while the actual exported preview
correctly contains them in Gear.

There are 184 recommended IDs outside baseline `GEAR` with at least one combat-like page, 115 with
at least three, and 95 with at least five. Sixty-five are baseline `CLEANUP`, have at least five
combat-like pages, and no noncombat-like page. These are high-value audit candidates, not 65
automatic overrides; most are already safely promoted by live equipment facts.

## Player-export cross-check

The fresh blueprint exported after the first utility override round is useful for evaluating both
the Wiki signal and the actual runtime promotion path:

- unique owned items: **747**;
- items exported as Gear: **101**;
- Gear items present in the Wiki recommendation dataset: **77**;
- Gear items with at least one combat-like recommendation: **73**;
- Gear items absent from all recommendation tables: **24**.

The positive matches include Barrows gloves, Amulet of fury, Fire cape, Dragon boots, Dragon
defender, Bandos chestplate, Fighter torso, Helm of neitiznot, Lightbearer, Ava's accumulator,
Mage's book, Proselyte, Mixed hide, and the player's common weapon/ammunition upgrades.

The 24 absent items demonstrate the false-negative problem. They include real or potentially useful
combat equipment such as Adamant 2h sword, Barrelchest anchor, Berserker ring (i), Crystal bow
(inactive), Ring of the gods, Salve amulet(i), Shadow sword, Warrior ring, Yew longbow, and uncharged
weapon states. A hard whitelist would incorrectly remove all of them.

The same fresh export confirmed that Ram skull helm, Gold helmet, Warm gloves, Tortugan shield,
Mirror shield, Spiny helmet, Slayer gloves, Boots of stone, and Insulated boots had all left Gear as
intended. A second exact-ID cleanup then moved Spotted/Spottier cape, Flippers, Willow blackjack,
Enchanted emerald sickle, and Pointed myre snelm to their functional Tools groups. Mind helmet and
Ardougne knight platebody remain Gear because they are real defensive armour, not mere quest-access
objects.

## Confirmed non-Gear recommendations

The positive signal also contains legitimate non-Gear items:

- Rada's blessings are recommended in ammunition slots but remain persistent account utilities in
  Main under the current product taxonomy.
- Charged teleport jewellery can be recommended in combat setups but remains in Teleports.
- Dragon pickaxe and Dragon axe can appear in combat or hybrid activities but remain primary Tools.
- Graceful and other skilling outfits occur in recommended tables but remain vertical outfit
  columns in Tools.
- Book of the dead is recommended for some combat activities but remains a teleport/utility item in
  the current preset.

These cases prove that recommendation presence is evidence of usefulness, not proof of the desired
Bank Architect tab.

## Safe classification policy

1. Exact reviewed item semantics remain authoritative.
2. RuneLite equipment slot plus meaningful combat stats remains the primary generic Gear signal.
3. Combat-like Wiki recommendations can confirm Gear or nominate an unresolved ID for review.
4. Repeated noncombat-like recommendations nominate skilling outfits and utilities for Tools review.
5. Teleports, currencies, skilling tools, diary utilities, and quest access items retain their
   functional tab even when equipped in a recommendation.
6. Absence from the Wiki recommendation dataset is neutral.
7. Only canonical item IDs may receive an override; placeholders, certs, minigame copies, and
   duplicate records require separate negative controls.

## Next implementation slice

Do not import the 889-ID Wiki dataset into the plugin. Instead:

1. compare high-confidence Wiki candidates with an actual preview built using live gear facts;
2. isolate recommended combat items that still fail to reach Gear after that runtime promotion;
3. review those exact IDs and their placeholder/minigame neighbours;
4. add only the confirmed misses to `CanonicalItemClassificationOverrides` with regressions;
5. optionally use broad recommendation frequency as research evidence for Gear ordering, but retain
   an independently designed deterministic sorter.

## Source references

- [OSRS Wiki recommended-equipment template documentation](https://oldschool.runescape.wiki/w/Template%3ARecommended_equipment/doc)
- [RuneScape Wiki Bucket editing/API documentation](https://runescape.wiki/w/Help:Editing/Bucket)
- [OSRS Wiki Bucket migration note for recommended equipment](https://oldschool.runescape.wiki/w/User:Mudscape/Bucket_migration)

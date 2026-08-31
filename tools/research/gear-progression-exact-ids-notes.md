# Gear progression exact-ID research notes

Research date: 2026-07-17. This is offline/development research for Bank Architect; it is not runtime plugin data and makes no production classification change.

The merged TSV contains 356 rows and 325 unique canonical item IDs: 93 melee rows, 115 ranged rows, 83 magic rows, 5 dedicated hybrid-utility rows, 15 prayer rows, and 45 tool-state rows. The 107 Gemini concepts expand to 136 exact item-state targets because generic families such as blessed dragonhide, god capes, imbued rings, and charged/uncharged equipment have multiple canonical IDs.

## Schema and stage model

The schema is:

`style, slot, stage, stageLabel, itemId, displayName, variantNote, source, wikiRevision`

Stages are now indexed consistently across combat gear and tools:

1. `Starter`
2. `Early`
3. `Mid`
4. `Late`
5. `End`

All pre-merge stages 1-4 were shifted to 2-5 without changing their original meaning. The new Starter rows come only from the Gemini names list. Existing tool bands therefore become Early (basic/level 1-20), Mid (roughly level 21-60), Late (common level-61 upgrades), and End (level 65/71 prestige or crystal tools); there are no Starter tool rows in this research batch.

## Source and merge rules

- Exact IDs were reconciled against the repository's generated RuneLite item registry and OSRS Wiki item/equipment pages.
- `wikiRevision` contains a revision-pinned Wiki URL for every row.
- Gemini-provided IDs were ignored completely. Only its 107 item names, style labels, and stage placements were used.
- `source=rapport` means the item or family was explicitly named only in the primary progression report.
- `source=aangevuld` marks a missing slot filled with a sensible carry-forward or Wiki equipment-progression option.
- `source=gemini-namenlijst` means the exact item-state row was added only because of the Gemini names list; all such IDs were independently verified.
- `source=beide` means the exact item-state and resulting stage were already represented by the primary table and were also named by Gemini. Generic Gemini family names were expanded only to already documented canonical states; for example, “Blessed d'hide body/chaps” covers the six god variants and “Imbued god cape” covers all three capes.
- When Gemini named an item at a different stage, both stages were retained. No existing carry-forward row was silently deleted.
- The no-duplicate rule is applied to the practical key `style + itemId + stage`. Cross-style reuse already existed in the research model (for example, Amulet of glory) and remains intentional.
- Gemini placed Barrows gloves under Hybrid/Utility while the primary report already had the same exact item at Mid melee. To avoid duplicating the same item/stage, the existing Mid row remains under melee and is marked `source=beide`. The dedicated `hybrid-utility` rows are Brimstone ring, Slayer helmet (i), Lightbearer, Rada's blessing 4, and Elysian spirit shield; all six Gemini Hybrid/Utility concepts are nevertheless represented.

Current source counts are: 170 `rapport`, 50 `aangevuld`, 104 `beide`, and 32 `gemini-namenlijst` rows. The 15 Prayer-progression rows were explicitly requested in the follow-up prompt and therefore use `source=rapport`.

Useful aggregate progression sources: [Melee armour](https://oldschool.runescape.wiki/w/Armour%2FMelee_armour), [Ranged armour](https://oldschool.runescape.wiki/w/Armour/Ranged_armour), [Magic armour](https://oldschool.runescape.wiki/w/Armour/Magic_armour), [Ranged gear progression](https://oldschool.runescape.wiki/w/Guide%3ARanged_Gear_Progression).

## Stage conflicts retained intentionally

These are all exact item-ID stage conflicts introduced or exposed by the Gemini merge. “Gemini stage” is the names-list placement; “other stage” is the pre-existing primary/carry-forward placement after reindexing.

| Item ID | Canonical item / family | Gemini stage | Other retained stage | Reason |
|---:|---|---|---|---|
| 1153 | Iron full helm | Starter | Early | Starter list conflicts with the primary Early metal ladder. |
| 1115 | Iron platebody | Starter | Early | Same metal-ladder conflict. |
| 1067 | Iron platelegs | Starter | Early | Same metal-ladder conflict. |
| 1191 | Iron kiteshield | Starter | Early | Same metal-ladder conflict. |
| 1323 | Iron scimitar | Starter | Early | Same metal-ladder conflict. |
| 1167 | Leather cowl | Starter | Early | Starter list conflicts with the primary Early leather ladder. |
| 1129 | Leather body | Starter | Early | Same leather-ladder conflict. |
| 1095 | Leather chaps | Starter | Early | Same leather-ladder conflict. |
| 1387 | Staff of fire | Starter | Early | Gemini treats it as a Starter staff; the primary report includes all elemental staves in Early. |
| 1727 | Amulet of magic | Starter | Early | Gemini treats it as Starter; the primary report placed it in Early Magic. |
| 2412 | Saradomin cape / Mage Arena 1 cape family | Mid | Early | The primary table used one god cape as an Early slot-completion aid; Gemini places the family in Mid. |
| 6570 | Fire cape | Mid | Late | The Late row is an explicit carry-forward from Mid. |
| 12954 | Dragon defender | Mid | Late | The Late row is an explicit carry-forward until Avernic defender. |
| 11773 | Berserker ring (i), Nightmare Zone ID | Mid | Late | Only this imbue ID had an existing Late carry-forward; all three normal imbue IDs remain in Mid. |
| 7462 | Barrows gloves | Mid | Late | Primary melee Mid plus ranged Late use; Gemini labels the item Hybrid/Utility Mid. |
| 22981 | Ferocious gloves | Late | End | End row is a carry-forward of the best listed melee gloves. |
| 19547 | Necklace of anguish | Late | End | End row is a carry-forward of the best listed ranged neck. Gemini's “Amulet of anguish” is normalized to the canonical name. |
| 12002 | Occult necklace | Late | End | End row is a carry-forward of the best listed magic neck. |
| 19544 | Tormented bracelet | Late | End | End row is a carry-forward of the best listed magic gloves. |
| 21791 | Imbued saradomin cape | Late | End | One representative imbued cape was carried into End; the full three-cape family remains in Late. |

No other exact item ID has a Gemini-vs-primary stage disagreement.

## Elysian versus Spectral verification

Gemini's item name `Elysian spirit shield` and its description are consistent with the Wiki, despite the initial concern. Elysian spirit shield is canonical item ID `12817`; its passive effect has a 70% chance to reduce received damage by 25%. Spectral spirit shield has a different passive effect: it reduces the effectiveness of Prayer-draining attacks by 50%. The TSV therefore keeps **Elysian spirit shield**, not Spectral, at Hybrid/Utility End.

Revision-pinned sources: [Elysian spirit shield](https://oldschool.runescape.wiki/w/Elysian_spirit_shield?oldid=15186436), [Spectral spirit shield](https://oldschool.runescape.wiki/w/Spectral_spirit_shield?oldid=15186438).

## Prayer progression research

The `prayer` style is intentionally a sparse defensive Prayer-bonus progression, not another offensive combat style and not a ten-slot best-in-slot loadout.

| Stage | Included progression | Verified reason |
|---|---|---|
| Starter | No rows | The prompt allows an empty Starter stage. Repeating Holy symbol here would add no distinct lower tier; it begins at Early instead. |
| Early | Monk's robe top, Monk's robe, Holy symbol | The robe pieces give +6/+5 Prayer and Holy symbol gives +8. This is the Wiki's practical free-to-play Prayer setup core. |
| Mid | Initiate sallet/hauberk/cuisse, Holy symbol | Initiate gives +3/+6/+5 Prayer with mithril-equivalent Defence. Holy symbol is retained because armour does not replace the neck slot. |
| Late | Proselyte sallet/hauberk/cuisse/tasset, Book of balance | Proselyte gives +4/+8/+6 Prayer with adamant-equivalent Defence. Cuisse and tasset are equivalent body-type leg variants. Book of balance supplies +5 Prayer plus balanced +4 attack and defence bonuses. |
| End | Sunfire fanatic helm/cuirass/chausses | Sunfire gives +6/+10/+8 Prayer, currently the highest Prayer bonus in the head, body and legs slots, while retaining rune-like physical Defence. |

Proselyte is **not** the current pure Prayer-bonus endpoint. Sunfire fanatic armour, released with the Fortis Colosseum, exceeds every corresponding Proselyte piece by +2 Prayer. It requires 40 Defence and 60 Prayer and is much harder for an Ironman to obtain, so End is appropriate.

The supplied “female-only” wording for Proselyte legs is no longer mechanically accurate. Proselyte cuisse is the platelegs-style item and Proselyte tasset is the plateskirt-style equivalent; both have identical stats, and current OSRS does not restrict either item by gender/body type. Both canonical IDs are retained because they are distinct bankable variants.

Elite Void was reviewed but not added. Elite void top and robe give only +3 Prayer each, compared with Proselyte's +8 body/+6 legs and Sunfire's +10/+8. Elite Void remains valuable for its Ranged/Magic set effects and neutral hybrid Defence, but it is not a defensively led Prayer-bonus upgrade.

Ancient wyvern shield was also checked because it was named as a possible duplicate concern. Neither state was already in the TSV, but the shield has +0 Prayer; its purpose is Magic/physical defence and wyvern-breath protection. It therefore does not belong in this Prayer progression. Book of balance is used as the Late shield because god books provide +5 Prayer; Falador shield 4 and broodoo shields can match that Prayer bonus but were not part of the requested progression.

Holy symbol ID `1718` did not previously occur in the TSV. Its Early and Mid rows are an intentional same-style carry-forward, not a cross-style duplicate. Cache/quest collision ID `4682`, which shares the display name, is excluded.

Revision-pinned sources: [Prayer items](https://oldschool.runescape.wiki/w/Prayer_items?oldid=15263671), [Monk's robes](https://oldschool.runescape.wiki/w/Monk%27s_robes?oldid=14545871), [Holy symbol](https://oldschool.runescape.wiki/w/Holy_symbol?oldid=15261933), [Initiate armour](https://oldschool.runescape.wiki/w/Initiate_armour?oldid=14982224), [Proselyte armour](https://oldschool.runescape.wiki/w/Proselyte_armour?oldid=15093439), [Book of balance](https://oldschool.runescape.wiki/w/Book_of_balance?oldid=15225459), [Sunfire fanatic armour](https://oldschool.runescape.wiki/w/Sunfire_fanatic_armour?oldid=14911113), [Elite Void Knight equipment](https://oldschool.runescape.wiki/w/Elite_Void_Knight_equipment?oldid=15260639).

## Deliberate variant decisions

- Imbued rings include the normal bankable Nightmare Zone, Soul Wars and PvP Arena IDs. Battle Royale/cache copies (`23595`, `23624`, etc.) are excluded.
- Barrows equipment includes only the ordinary repairable base IDs (`4708/4712/4714` and `4732/4736/4738`). The 100/75/50/25/0 cache states are deliberately excluded from this progression table.
- Toxic blowpipe, Serpentine helm, tridents, Scythe, Tumeken's shadow, crystal tools/armour and Dizana's quiver include their meaningful charged/uncharged or active/inactive IDs.
- Crystal armour uses the ordinary colourless active/inactive IDs. Cosmetic recolours and Gauntlet-only basic/attuned/perfected armour are excluded.
- `Ultor ring=28307`, `Venator ring=28310`, and `Magus ring=28313` are the live canonical IDs. The same names at `25485-25487` are beta constants and are excluded.
- Blessed dragonhide is expanded to all thirty canonical equipment pieces: five slots for each of the six gods. Ornament/cache duplicates are excluded.
- Gemini's singular or informal labels were normalized to canonical names without trusting its IDs: `Iron arrows` becomes `Iron arrow`, `Blood moon top` becomes `Blood moon chestplate`, `Amulet of anguish` becomes `Necklace of anguish`, and `Mages' book` becomes `Mage's book`.

## Ironman/stage doubts

- Amulet of glory and blue dragonhide in Early are optimistic for a fresh Ironman. They are retained because the primary report explicitly placed them there.
- Wizard boots are clue-dependent; Obsidian cape requires Mor Ul Rek access; Brimstone ring requires an Alchemical Hydra grind. Their Gemini stages reflect the supplied names list rather than smooth Ironman availability.
- Mixed hide requires relatively high Crafting/Hunter access; Hunters' sunlight crossbow requires 74 Ranged. Both sit at the upper edge of Mid.
- Inquisitor's armour, Torva, Scythe, Twisted bow, fortified Masori, Zaryte equipment, Ancestral, Tumeken's shadow, Kodai, Dizana's quiver and the DT2 rings are true end-game or extremely RNG-heavy Ironman acquisitions.
- Elysian spirit shield is a Corporeal Beast unique and is an exceptionally long Ironman grind; End is appropriate.
- Sunfire fanatic armour comes from the Fortis Colosseum starting at wave 4. It is statistically the Prayer-bonus endpoint, but substantially less accessible to an Ironman than shop-bought Proselyte.
- 3rd age pickaxe and axe are not rational progression targets for an Ironman. They remain only because the requested bronze-to-crystal tier inventory asked for the complete traditional tool ladder.
- Archers ring in the Early fill and Mage Arena cape in the Early magic fill are slot-completeness aids, not recommendations that a new Ironman should grind those immediately.

## Remaining ambiguity

- “Fishing rod variants” can mean only functional rod types, or every cosmetic pearl variant. This table includes both functional and pearl variants but excludes nets, lobster pots and non-rod fishing tools.
- Machetes have no linear Woodcutting-level requirement comparable to pickaxes and axes. Their stages follow the Wiki's regular → opal → jade → red-topaz performance tier, not a level gate.
- The phrase “locked/normal crystal armor” was interpreted as the actual inactive/active bank states. No separate player-bankable item named “locked crystal armour” exists in the canonical registry.
- The reports did not define whether a style-stage must contain exactly one item per slot or every valid alternative. The TSV records every explicitly named alternative and uses carry-forward rows only where a useful slot would otherwise be absent.
- The earlier twelve combat style/stage combinations covered ten practical equipment slots. Adding Starter, Hybrid/Utility and the deliberately sparse Prayer style makes the dataset a progression inventory rather than a promise that every style-stage contains all ten slots.

## Peer-set completion pass, 2026-08-31

Added 18 rows. The original pass picked one representative armour set per style and stage, so
sibling sets from the same source were left untiered. Untiered gear falls back to the name
heuristics in `GearItemSorter`, and none of these names match a heuristic keyword, so the sibling
sets scored 0 while their tiered peers scored 800. In the gear tab that split families of the same
origin far apart, which is what a player reported for the Perilous Moons armour: Blood moon was
tiered and the Eclipse and Blue sets were not.

- Perilous Moons: added the Eclipse (ranged) and Blue (magic) head/body/legs rows next to the
  existing Blood moon (melee) rows, all at stage 4.
- Barrows: added Dharok's, Guthan's, Torag's and Verac's head/body/legs rows at stage 4, next to
  the existing Karil's (ranged) and Ahrim's (magic) rows.

All 18 IDs are repairable base forms verified against the generated item registry, and all use
`source=aangevuld` because they complete a family the primary report named only partially. The
degraded `100/75/50/25` states are still covered by the tier catalog's name fallback, as they
already were for Karil's and Ahrim's.

# Functional items and usage tags — 2 October 2026

Second focused batch from the original [placement research](plugin-hub-placement-and-cleanup-october-2.md), following the [cosmetic batch](item-role-audit-october-2.md). No third-party plugin rules or datasets were copied.

## Corrected placement

| Exact ID | Item | Previous | Corrected | Evidence |
| --- | --- | --- | --- | --- |
| 2402 | Silverlight | Cleanup / quest-item | Gear / weapon | Exact-ID equipment record: weapon slot, nonzero combat stats. |
| 6745 | Dyed Silverlight | Cleanup / quest-item | Gear / weapon | Exact-ID equipment record; distinct quest-stage version kept separate. |
| 6746 | Darklight | Cleanup / quest-item | Gear / weapon | Exact-ID equipment record: weapon slot, nonzero combat stats. |
| 6111 | Ghostly cloak | Cleanup / quest-item | Gear / cape | Exact-ID cape stats and existing Ghostly robes set membership. |

Independent item sources: [Silverlight](https://oldschool.runescape.wiki/w/Silverlight), [Darklight](https://oldschool.runescape.wiki/w/Darklight), [Ghostly cloak](https://oldschool.runescape.wiki/w/Ghostly_cloak). Exact IDs, equipment slots and combat stat magnitudes were cross-checked against the independently collected July 15, 2026 Wiki snapshot, not inferred from display names. Current search also corroborates [weapon types](https://oldschool.runescape.wiki/w/Weapon_types), [attack speed](https://oldschool.runescape.wiki/w/7_tick_weapons) and [magic equipment](https://oldschool.runescape.wiki/w/Magic_armor). Search content is cached; direct item-page fetches were blocked. No current upgrade, reacquisition or disposal conditions were inferred.

## Loaded usage facts

The new `item-usage-tags.tsv` provides exact-ID facts through a small required-resource reader. It enriches the existing `CatalogItem.tags` field and survives preview creation and manual assignment. These are usage facts, independent of routing tags and physical bank destinations. No cleanup recommendation is produced from them yet; the sidebar is unchanged.

| IDs | Loaded facts | Independent evidence |
| --- | --- | --- |
| 542, 544 | prayer-gear | [Prayer equipment](https://oldschool.runescape.wiki/w/Bis_melee), existing exact-ID catalog. |
| 772, 9084 | transport-access, quest-use | [Fairy rings](https://oldschool.runescape.wiki/w/Teleports_close_to_fairy_ring), existing quest classification. Transport role does not assert an account still needs a staff. |
| 775 | cooking-utility | Existing reviewed exact-ID Cooking gauntlets rule; [Cooking gauntlets](https://oldschool.runescape.wiki/w/Cooking_gauntlets). |
| 776, 1580 | smithing-utility, blast-furnace | [Smithing training](https://oldschool.runescape.wiki/w/Smithing_guide_osrs), [bar dispenser](https://oldschool.runescape.wiki/w/Bar_dispenser). |
| 2402, 6745, 6746 | quest-weapon | Exact-ID weapon records plus recorded quest origin. No generic weaker-weapon disposal assumption. |
| 4298, 4300, 4302 | thieving-utility, quest-use | [Clothing requirements](https://oldschool.runescape.wiki/w/Costumes), [HAM Hideout](https://oldschool.runescape.wiki/w/HAM_Fanatics%27_Camp). |
| 4300 | clue-required | Exact [Ham robe](https://oldschool.runescape.wiki/w/Hame_robe) page records emote clues. Other HAM pieces are not automatically given this fact. |
| 4304, 4306, 4308, 4310 | thieving-utility | HAM Hideout describes full-outfit utility. No claim of increased pickpocket success chance. |
| 6106–6111 | magic-gear | Existing exact-ID Ghostly set and equipment records; [magic equipment](https://oldschool.runescape.wiki/w/Magic_armor). |
| 11061 | special-attack, prayer-utility | [Ancient mace](https://oldschool.runescape.wiki/w/Ancient_mace), [Prayer boost sources](https://oldschool.runescape.wiki/w/Template%3ATemporary_skill_boost/Prayer). |
| 19689, 19691, 19693, 19695, 19697 | warm-clothing, wintertodt | [Clue hunter outfit](https://oldschool.runescape.wiki/w/Clue_hunter_outfit) explicitly excludes Helm of raedwald. ID 19687 therefore gets no warm-clothing fact. |

There are **29 newly tagged exact IDs**, giving **37 nonempty tag records** including the eight existing static Herblore records. This is initial curated coverage, not a full-catalog completeness claim. Restricted, placeholder and name-sharing records never inherit these tags automatically. More clue roles remain pending independent review; lack of a tag is not evidence of lack of a use.

The new reader rejects missing/empty resources, unsupported schema, nonpositive or duplicate IDs, invalid tag syntax, duplicate tags and extra columns. Loaded sets and maps are immutable. Metadata can be expanded without writing repeated Java lists; production data has no external runtime fetches.

## Verification and budget

- Final full build passed: 1,112 tests, zero failures/errors; all 1,950 simulation scenarios completed and all four reference hashes passed. Whitespace check passed; only Git line-ending conversion warnings were reported.
- Targeted tag/preview regression tests and the effective exporter passed. Full comparison confirms exactly four destination changes and 29 new tagged IDs; effective/excluded record counts remain 32,586 / 606.
- Tests cover unchanged cosmetic destinations for useful HAM/Clue hunter items, functional gear routing, transport tags surviving manual Frequently Used assignment, warm-clothing exception, no quest-stage staff inheritance, and retention of existing static Herblore tags.
- Full tests initially found one deliberate fixture difference: Darklight. Only ID 6746's expected route was explicitly updated from storage-cleanup to combat-gear; the 770-row fixture was not regenerated.
- All 1,950 simulations completed. Before-change reports were saved separately. Short report and short cleanup hashes are unchanged. Aggregate cleanup removes only 2402, 6745 and 6746, three occurrences each: 9,321 / 46,665 becomes 9,318 / 46,656. Only the two corresponding aggregate reference hashes were explicitly updated after inspection.
- Highest local review estimate: **196,628**, headroom **3,372**, calibrated against maintainer count 200,414 at `def1e856e101ff0e57dd96adccab2cf1d886b076`. Resources/tests are outside this estimator and were inspected separately. This is not official Hub approval.

No commit, push, branch change, Hub submission, automation, inventory/equipment read or account-progress feature was added. Further catalog coverage and explained conditional cleanup remain later work.

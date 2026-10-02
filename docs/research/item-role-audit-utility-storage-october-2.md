# Repeatable utilities and conditional storage — 2 October 2026

Fourth focused batch from the original [placement research](plugin-hub-placement-and-cleanup-october-2.md). Independent Wiki evidence and the repository's existing catalog were consulted; no third-party plugin classifications were imported.

## Exact classification decisions

| ID | Item | Previous catalog | Reviewed catalog | Reason |
| --- | --- | --- | --- | --- |
| 22398 | Ivandis flail | CLEANUP / quest-item | GEAR / weapon | Exact-ID weapon record; used across multiple Myreque quests and for Bloom. |
| 22986 | Bonecrusher necklace | SKILLING / skilling | GEAR / neck | Exact-ID neck-slot combat record; Prayer utility does not make it a raw resource. |
| 25781 | Ash sanctifier | SKILLING / skilling | TOOL / skilling-utility | Reusable Prayer tool. Ironman already had a mapper exception sending it to Tools; the base catalog is now consistent too. |
| 24416 | Rune pouch (l) | CLEANUP / cleanup | RUNE / rune-container | Exact record identifies the protected regular rune-storage container, not a restricted minigame substitute. |
| 9433 | Bolt pouch | GEAR / gear | GEAR / ammo | Preserve the owner's reviewed Combat preference; separate it from ordinary gear through the existing Ammunition tag. |
| 2963 | Silver sickle (b) | CLEANUP / cleanup | TOOL / quest-utility | Primary reviewed workflow is repeatable Bloom; retain Prayer role as an additional fact. |

These are static catalog corrections. Some prior Cleanup equipment could already be recovered as Gear by runtime stat information; this does not make its static classification correct. Explicit personal routing continues to take precedence. Namesakes, notes, restricted records and unfinished sickles are not automatically changed.

Exact item/equipment identities were cross-checked in the independent July 15, 2026 bulk Wiki snapshot. Additional independent references: [Ivandis flail](https://oldschool.runescape.wiki/w/Ivandis_flail), [A Taste of Hope](https://oldschool.runescape.wiki/w/A_taste_of_hope_osrs), [Sins of the Father](https://oldschool.runescape.wiki/w/Sins), [Bonecrusher necklace](https://oldschool.runescape.wiki/w/Bonecrusher_necklace), [Ash sanctifier](https://oldschool.runescape.wiki/w/Ash_sanctifier), [Ironman Prayer training](https://oldschool.runescape.wiki/w/Prayer_training_ironman), [Rune pouch](https://oldschool.runescape.wiki/w/Rune_pouch), [Bolt pouch](https://oldschool.runescape.wiki/w/Bolt_pouch), [Silver sickle (b)](https://oldschool.runescape.wiki/w/Silver_sickle_%28b%29), [Bloom objects](https://oldschool.runescape.wiki/w/Dead_vine_%28scenery%29). Direct item pages were often blocked; consulted search content was cached, so no fresh numeric bonuses or account-specific conditions are claimed.

## Added usage facts

Added 33 exact-ID rows to `item-usage-tags.tsv`. Effective records with nonempty tags now total **203** (195 resource entries and the original eight static Herblore entries). New groups cover:

- The six reviewed utilities above.
- Bonecrusher, Holy wrench, Crystal saw and its recharge seed, full/empty Ectophial, Ghostspeak amulet variants, Ring of visibility and Magic secateurs.
- Steel key ring, Tackle box, Coal bag, Gem bag, Herb sack, Seed box, Soul bearer, Master scroll book variants and Huntsman's kit.
- Seven narrowly reviewed alternative-storage candidates below.

Full and empty Ectophial records remain distinct; only the empty one gets `refill-required`. A `transport-access` fact describes its role, not whether it is currently ready to teleport. Blessed and unblessed sickles remain distinct. Resource-container facts describe functionality; they do not assert storage contents or capacity.

The Rune pouch note (24587), unblessed/unfinished sickles, other enchanted weapon stages and reclaim/quest-completion conditions remain review backlog. None is inferred safe to discard.

## First storage options

| Exact IDs | Option | Evidence / condition |
| --- | --- | --- |
| 5325 Gardening trowel; 5341 Rake; 5343 Seed dibber; 952 Spade | Tool Leprechaun | [NPC storage dialogue](https://oldschool.runescape.wiki/w/Transcript%3ATool_Leprechaun) names these tools. Existing stored items/capacity and the player's preferred accessibility are unknown. Spade also retains a clue-utility role. |
| 5295 Ranarr seed; 5315 Yew seed; 5373 Yew sapling | Seed vault | Independently verified standard Farming identities; [Farming Guild](https://oldschool.runescape.wiki/w/Farming_guild%27) describes seed/sapling storage. Guild access and the player's preferred workflow are unknown. No rule is generalized to all quest seeds. |

These facts appear in the existing item tooltip as `storage option ...`. Only such items gain the extra line: **Storage is optional; check access and capacity.** They keep their ordinary blueprint destination, and a player-assigned Frequently Used destination remains visible. There is no quest/diary/storage-state read, no inventory/equipment read, and no automatic storage/removal action. This is conditional information, not a cleanup eligibility engine.

The key ring remains a key container, with no guessed list of compatible keys imported from another plugin. Reclaim and loss behavior also require item-specific review; for example [Yanni Salika](https://oldschool.runescape.wiki/w/Yanni_Salika) describes a replacement key ring whose keys must be added again.

## Validation and budget

- Final full build passed: **1,118 tests**, zero failures/errors; **1,950 simulations completed**, all four reviewed baseline checks passed, and the exporter retained 32,586 effective / 606 excluded records. Whitespace check passed; Git only reported line-ending conversion warnings.
- Targeted regressions and classification export passed; compare against `build/classifications-skilling-audit.tsv` for the six exact catalog changes and 33 added usage rows.
- Full tests initially exposed the owner's prior Bolt pouch Combat preference. Preserved that preference and tested the Ammunition subgroup; the original preference test remains unchanged.
- All 1,950 simulations completed. Prior reports were saved separately. Both short report hashes are unchanged. Aggregate Cleanup loses only ID 24416, three occurrences: 9,318 / 46,656 becomes 9,317 / 46,653. Only the two aggregate hashes were explicitly updated after inspecting this exact difference.
- Highest local review estimate: **196,733**, headroom **3,267**. Resource/test changes were inspected separately; this is not an official Hub count.
- Existing panel compiler note: unchecked/unsafe operations. No new runtime external services, automation, commits, pushes or publication.

Full catalog coverage, player-specific cleanup decisions and storage-state verification remain future work.

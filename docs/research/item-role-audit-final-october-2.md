# Final placement and conditional cleanup batch - 2 October 2026

Completes the concrete clue/quest/utility and explained-cleanup changes for this test build.
This does **not** certify all item identities, all gameplay roles or account-specific disposal
eligibility. Earlier batch reports remain historical snapshots; their proposed-role fields are
not production rules. Production only loads the exact facts actually reviewed here and in the
preceding batches. No competitor implementation or classification dataset was imported.

## Reviewed additions

- Reviewed **70 canonical easy-emote requirement IDs**, using the independent Wiki requirement
  table and July 15 identity snapshot. Added 68 new rows; HAM robe already had its clue role,
  and HAM boots gains it alongside its existing Thieving role. Equipable Leather boots **1061**
  is distinguished from the Tower of Life namesake **6893**. No poisoned, noted, trimmed or
  restricted variant automatically inherits an ordinary requirement.
- Reviewed the remaining Silver/Emerald/Ruby/Blisterwood/Diamond sickle stages. Known Bloom
  variants get that role; unblessed Silver sickle does not. The newer Diamond stages and
  enhanced Blisterwood sickle have independently matched quest/equipment records, but no
  guessed Bloom role or disposal condition is added.
- Rune pouch note **24587** now travels with rune storage. Its role is **exchange for rune pouch**;
  it is not described as an operational container. Chef's hat gains Cooking utility.
- Added Thieving roles to all nine exact Oak/Willow/Maple blackjack variants and a Prayer role
  to Unholy symbol **1724**. The quest namesake **4683** is unchanged.
- Added conditional fancy-dress-box storage for the three Lederhosen and five Zombie outfit
  pieces. The unrelated Zombie head **19912** is excluded. The tooltip identifies the required
  costume room/box and the Ultimate Ironman full-outfit retrieval condition.

The resource now contains **293 exact usage rows**; with the eight existing static Herblore
records, the effective export has **301 records with usage facts**. This is semantic coverage,
not the number of items with an ordinary classification or routing tag.

### Classification corrections

**45 exact records** changed category or subcategory relative to the preceding utility batch:

| Group | Exact IDs | Reviewed category/subcategory |
| --- | --- | --- |
| Wizard headwear | 579, 1017, 7394, 7396 | GEAR/head |
| Leather cowl | 1167 | GEAR/head |
| Unholy symbol | 1724 | GEAR/neck |
| Desert shirt | 1833 | CLUE/cosmetic |
| Chef's hat | 1949 | TOOL/cooking-tool |
| Tiara | 5525 | RUNE/runecrafting-focus; existing Ironman mapper keeps it in Tools |
| Silver sickle | 2961 | GEAR/weapon; no Bloom fact |
| Ordinary metal claws | 3095-3101, 6587 | GEAR/weapon |
| Mystic hats | 4089, 4099, 4109, 23047 | GEAR/head |
| Brutal arrows | 4773, 4778, 4783, 4788, 4793, 4798, 4803 | GEAR/ammo |
| Blackjacks previously in Review | 4599, 6408, 6410, 6412, 6414, 6416, 6418, 6420 | TOOL/thieving-tool |
| Emerald/Ruby stages | 22433, 24693, 24695 | TOOL/quest-utility |
| Functional quest weapons | 24697, 33709, 33711, 33713 | GEAR/weapon |
| Rune pouch note | 24587 | RUNE/rune-container routing; exchange role only |

Willow blackjack **4600** and enchanted Emerald sickle **22435** already had a Tools route;
it is retained. Some equipment previously classified as Cleanup could already be recovered
by runtime gear stats. These corrections make the static catalog consistent; they do not
claim every affected item was misplaced in the owner's live bank.

## Conditional cleanup guidance

Existing item tooltips now distinguish the chosen destination from known uses and explain:

- Clue-required items should be kept, or the player should check the matching STASH unit and
  full item set. The plugin does not know whether that unit is built or filled.
- Quest roles require checking current and later uses before removal. Quest completion alone
  does not establish disposal eligibility, and this build does not read quest progress.
- Review/unknown items are explicitly not a recommendation to discard.
- Alternative storage is optional and access/capacity are unknown.

The automatic outclassed-gear rule now retains any item with known additional usage facts,
including reviewed and bulk-production stock. This is deliberately conservative: weaker
stats do not erase other roles, and the physical blueprint cannot split a stack to retain one
copy. Existing untagged stock still follows the existing alch rule. Explicit player assignments
are applied last and remain authoritative. There are no game actions, inventory/equipment
reads, runtime network requests or new account-state assumptions.

## Whole-export coverage pass

The new development-only `audit-cached-coverage.ps1` joins **all 32,586 effective IDs** against
the existing independent source snapshot. It performs no network calls or production writes.
The full per-ID ledger is `build/item-role-coverage.tsv`; generated aggregates are:

| Status | Records |
| --- | ---: |
| Exact cached identity | 11,826 |
| Equipment destination review | 390 |
| Quest lifecycle review | 2,491 |
| Restricted or legacy review | 799 |
| No exact cached source | 17,080 |

These statuses are a coverage measurement, not a claim that every candidate is bankable or
wrongly classified. Nonzero stat magnitude can include penalties; cosmetic flowers are an
example of why blanket Gear promotion is inappropriate. Restricted context must be checked
on the exact source page even when a cached category flag is absent. The original broader
audit attempted a missing-cache request and failed to connect; the new pass explicitly avoids
that dependency. No source refresh or current-gameplay certification is claimed.

Unknown lifecycle and reclaim conditions remain manual review. A complete, current manual
audit of the entire catalog and a quest-state disposal engine are **not completed features**.
They are longer-term work, not silently inferred rules in this test build.

## Independent evidence

Exact IDs, names, equipment slots and quest flags: repository's independent OSRS Wiki bulk
snapshot, collected **2026-07-15**. Consulted search results can be cached; direct pages were
often robots-blocked. The following support the narrow gameplay roles and conditions:

- [Easy clue requirements](https://oldschool.runescape.wiki/w/Easy_clue_scroll)
- [Bloom casting items](https://oldschool.runescape.wiki/w/Dead_vine_%28scenery%29)
- [In Aid of the Myreque](https://oldschool.runescape.wiki/w/Aid_of_the_myreque)
- [A Taste of Hope](https://oldschool.runescape.wiki/w/A_taste_of_hope_osrs)
- [Rune pouch note identity and exchange](https://oldschool.runescape.wiki/w/Module%3AExchange/Rune_pouch_note)
- [Rune pouch](https://oldschool.runescape.wiki/w/Runepouch)
- [Head chef access dialogue](https://oldschool.runescape.wiki/w/Transcript%3AHead_chef)
- [Blackjack family](https://oldschool.runescape.wiki/w/Category%3ABlackjacks)
- [Fancy dress box outfits](https://oldschool.runescape.wiki/w/Mahogany_fancy_dress_box)
- [Zombie mask storage and UIM condition](https://oldschool.runescape.wiki/w/Zombie_mask)

## Validation

- 1,122 tests pass, including known-use protection against the ordinary/reviewed/bulk alch
  routes, namesake/variant exclusions, and conditional tooltip text.
- The real-bank fixture intentionally changes only **4793 Mithril brutal** from Review to
  Combat in this batch. Its original 770-item coverage remains intact.
- All **1,950 simulations complete**. The aggregate removes exactly **32 corrected IDs** and
  **153 occurrences**: 9,317/46,653 becomes **9,285/46,500**. There are no new Cleanup IDs or
  changed surviving occurrence counts. Short Cleanup removes exactly six corrected IDs;
  16 short simulation rows change move counts while remaining complete.
- Isolated before-change proof temporarily restored only this batch's classification rows,
  usage facts and alch guard. All four resulting reports match the previously saved reports
  exactly; current sources were restored in `finally`. Four new hashes were reviewed and
  updated explicitly, rather than regenerated blindly.
- Highest local main-Java estimate: **196,915**, leaving **3,085** tokens. Resources/tests/tooling
  are inspected separately and not included in that estimate. Official Hub count remains unknown.
- Existing panel unchecked/unsafe-operations compiler note remains. No commit, push or
  publication has been performed. See the [ingame test checklist](../testing/ingame-october-2.md).

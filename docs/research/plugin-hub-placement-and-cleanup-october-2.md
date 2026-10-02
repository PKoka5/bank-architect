# Item placement and cleanup research — 2 October 2026

## Scope and evidence

Product research requested by the owner. Reviewed the [Plugin Hub](https://runelite.net/plugin-hub/), relevant public READMEs and our own current classification export. This is not a runtime comparison of every plugin, a review of third-party algorithms, or an audit of every bankable item. README statements describe advertised behavior; they do not establish correctness. No third-party implementation, classification dataset, UI or configuration was copied.

Search results and fetched pages can be cached. Several OSRS Wiki pages were inaccessible, and accessible/indexed game pages were sometimes over a year old. Gameplay examples below are research candidates requiring current item-specific verification before production rules. Failed fetches are not evidence that a plugin has disappeared.

## Relevant plugins

### Placement and tagging — public READMEs reviewed

| Plugin / primary source | Advertised approach | Independently proposed improvement for this repository |
| --- | --- | --- |
| [Auto Bank Sorter](https://github.com/Frailrain/Bank-Skill-Sorting) | Skill-oriented virtual tags; items can participate in multiple skills; variant handling. | Separate multiple usage roles from the single physical destination. Preserve player overrides when defaults change. |
| [Bank Tab Organizer](https://github.com/1504681/BankOrganizerPlugin) | Rules, subcategories, custom assignment and manual sorting guidance. | Audit exact item facts before fallback name matching; make the reason for a destination visible. |
| [Bank Organizer & Cleanup](https://github.com/Ideonomy-web/bank-organizer) | Category options and exclusions; quest-aware cleanup claims. | Protect cross-quest, clue and utility uses before recommending removal. |
| [Bank Assistant](https://github.com/jamesjmurtagh/runelite-bank-organiser-plugin) | Virtual grouped view, overrides and activity checklists; best-effort name matching. | Audit functional exceptions and overlapping activities. Its virtual bank behavior is not a proposed implementation here. |
| [Banker](https://github.com/randytkrx/banker) | Custom categories and rules, including main/uncategorized handling. | Keep unknown classifications reviewable and personal placement persistent. |
| [Sorted Bank](https://github.com/g-Clef-Cannon/SortedBank) | Alternate sorted bank presentation and virtual categories. | Distinguish virtual display order from our manually arranged physical bank blueprint. |
| [Bank Tag Generation](https://github.com/MitchBarnett/wiki-bank-tag-integration) | Generates tags from Wiki categories and monster drops. | Use independent Wiki research to discover candidates; a Wiki category alone does not determine optimal placement. No runtime Wiki integration proposed. |
| [Bank Friction Analyser](https://github.com/Nubles/OSRS-Bank-Friction-Analyser-Runelite) | Local usage observations and suggestions about commonly withdrawn groups and repeated bank friction. | Potential later opt-in personalization. First fix static facts; no usage logging added in this task. |

### Cleanup and item usefulness — public READMEs reviewed

| Plugin / primary source | Advertised approach | Independently proposed improvement |
| --- | --- | --- |
| [Dumb Old Man](https://github.com/MyLilBackpack/bank-junk-identifier) | Quest progress, upgrade conditions, alternative storage, reclaimable items, reasons and keep exceptions. | Separate placement from a conditional keep/store/reclaim/review assessment. Quest completion by itself must not mean disposable. |
| [Wasted Bank Space](https://github.com/mcgeer/WastedBankSpace) | Highlights items with alternative storage and provides explanations. | Explain the destination and prerequisites; storage eligibility does not mean the player owns or has unlocked that storage. |
| [Bank Cleaner](https://github.com/Jerpent/bank-cleaner-plugin) | Equipment comparison using stats, speed, slot and weapon type, with exclusions and explanations. | Protect unique effects and quest/clue/activity roles before treating weaker stats as redundancy. Its README acknowledges niche uses require judgment. |
| [Emote Clue Items](https://github.com/larsvansoest/emote-clue-items) | Clue equipment and STASH tracking. | Add independently verified clue roles to functional and cosmetic items, including items used in multiple clues. |
| [Banked Experience](https://github.com/TheStonedTurtle/banked-experience) | Training potential in banked supplies. | Identify training resources and processing chains; low monetary value is not evidence of Ironman uselessness. |

### Additional Hub candidates

The Hub also lists Bank Tag Layouts, Inventory Setups, Bank Equipment Stat Filter, Visual Bank Tags, Tagkeeper, Bank Slot Sync, Combo Tags, Wiki Gear Setups, Slayer Bank Tab, Wiki Bank Tools and Bank Templates. These are secondary candidates for loadouts, tag visibility, variants or equipment context; listing inspection alone is not a detailed implementation review. Bank Templates remains excluded as a source of code, UI, resources or structure under AGENTS.md.

Skill Bank Organizer and Bank Sorter were marked incompatible in the consulted Hub listing. Their listing is not a recommendation to install them. Wiki Bank Tools' repository fetch failed; no detailed claims about it are relied upon.

### Revision tracking

The following Hub marker revisions were observed; READMEs were pinned where retrieval succeeded. Root READMEs can differ from submitted artifacts.

| Repository | Hub marker revision |
| --- | --- |
| Frailrain/Bank-Skill-Sorting | f58539184ef5a36c3b04d2b548f1ef516524aac4 |
| 1504681/BankOrganizerPlugin | 22730948e4d291d4a45bda674dc25d839a56908d |
| Ideonomy-web/bank-organizer | 60a00fd04c0b5cebd69b9457491bb3cfce8d53dc |
| jamesjmurtagh/runelite-bank-organiser-plugin | 2a39da30345a453d3040351f4ed021d3f6ed7678 |
| Nubles/OSRS-Bank-Friction-Analyser-Runelite | 0eecf242f845a47ca2c40855594fee27ac6919cc |
| randytkrx/banker | 6b18dc8dbff6e228188c0f3068567249e920c536 |
| g-Clef-Cannon/SortedBank | ea64b140263f19e865e992b84cfb861314d9d4c8 |
| MitchBarnett/wiki-bank-tag-integration | 47a06f8a6046ca5e303bc225d5a581acb238b981 |

Auto Bank Sorter's repository About text and README disagree about needing Bank Tag Layouts; the consulted README states native Bank Tags support. Do not infer dependencies from About text alone.

## Our current baseline

Inspected `build/classifications-after-style.tsv` from the current local update, not merely committed HEAD. It contains **32,586 effective IDs**, with **606 excluded records** in the accompanying export. These include legacy and special records and must not be described as 32,586 bankable items.

| Effective category | Records |
| --- | ---: |
| CLEANUP | 13,557 |
| GEAR | 8,538 |
| SKILLING | 3,234 |
| POTION | 2,225 |
| CLUE | 1,359 |
| TOOL | 1,152 |
| FARMING | 821 |
| TELEPORT | 595 |
| HERBLORE | 522 |
| CURRENCY | 297 |
| RUNE | 197 |
| UNIQUE | 89 |

Only **8 exported records have nonempty semantic tags**, and 8 have workflow keys. Registry-loaded items currently use empty semantic tag sets. This does not mean most items lack destinations: category, subcategory and sorting rules already provide placement. The existing 29 routing tags across 10 bank categories are a different concept from additional `CatalogItem.tags` usage roles.

The large CLEANUP count is an audit target, not proof that 13,557 useful bank items are being discarded. The plugin never discards anything. We must distinguish legacy records, actual bankable quest objects, cosmetics and functional exceptions.

No quest-progress reads were found in the current main-Java search. Existing static classifications cannot establish player-specific quest disposal eligibility. Existing gear sorting and outclassed-gear options must be reviewed before extending them; this research does not imply they are absent.

## Proposed original model

Every item has **one default physical destination**, plus **zero or more usage facts**. A separate assessment explains whether it should remain accessible, can be stored elsewhere, can be reclaimed under verified conditions, or requires review.

Example: a staff can belong in Teleports, also have quest/fairy-ring roles, and be personally pinned to Frequently Used. Additional roles must not duplicate the physical item or silently reverse the player's preference.

Placement precedence: explicit player assignment, independently verified item-specific facts, reviewed family rules, then conservative fallback. Personal assignments survive reanalysis, bank reopening and catalog updates.

Usage facts should cover skill/activity, equipment slot/style, outfit membership, clue requirements, quest dependencies, unique effects, processing chains, storage and reclaim prerequisites. Keep routing vocabulary separate from these facts. Do not expose dozens of tags everywhere in the sidebar: show the destination and a short reason, with additional roles in details.

Cleanup assessment must check future quest uses, clues, diaries, unique utility, Ironman reacquisition cost, player protection and account prerequisites. Unknown progress or incomplete facts means review/keep, not a claim that removal is safe. Weaker combat stats alone are insufficient.

A future documented feature may read supported quest/diary/storage state APIs if available. No inventory/equipment reads, automation, runtime network calls or telemetry are proposed. A storage suggestion remains conditional until prerequisites are known.

## Gameplay research candidates

| Candidate | Why it needs multiple facts | Independent reference / limitation |
| --- | --- | --- |
| Dramen staff and Lunar staff | Quest origin does not erase transport or personal frequently-used roles. | [Dramen staff](https://oldschool.runescape.wiki/w/Dramen_staff): indexed Wiki discusses fairy rings, diary exception and other uses; cached content requires current confirmation. Audit Lunar staff separately. |
| Ancient mace | Quest equipment can retain a unique practical effect. | [Ancient mace](https://oldschool.runescape.wiki/w/Ancient_mace). Verify its current effect and uses before writing rules. |
| HAM clothing and other clue outfits | Low-stat or cosmetic appearance does not establish uselessness. | Check current exact clue requirements independently; do not import another plugin's clue list. |
| Earned holiday items | Some items can be reclaimed, subject to unlock and item-specific conditions. | [Diango](https://oldschool.runescape.wiki/w/Diango). This does not apply to every cosmetic or everything associated with Diango. |
| Seeds/saplings and costume-room items | Storage is different from destruction; eligibility and access are conditional. | [Seed vault](https://oldschool.runescape.wiki/w/Seed_vault), [treasure storage](https://oldschool.runescape.wiki/w/Treasure_storage). Recheck exact supported items and account restrictions. |

These are audit examples, not new implemented classifications or automatic disposal recommendations.

## Implementation order and acceptance

1. **Create an independent audit ledger.** Exact IDs, existing route, proposed route, usage roles, source URL/date, confidence, variant family, and reason. Start with bankable owned items; do not bulk-promote cache constants into production facts.
2. **Audit high-impact placement groups.** Functional gear versus cosmetics; quest utility; clue overlaps; hunter tools/containers/supplies; outfit completeness; runes/teleports; crafting and training chains. Recheck the recent butterfly, pouch, lantern and Sunfire corrections for variants.
3. **Add compact usage metadata.** Use independently curated resources and a small shared reader rather than repeated Java item lists. Resolve conflicts explicitly and preserve overrides. Prove placement remains stable across reopening.
4. **Add explained, conditional cleanup guidance.** Begin with verified alternative-storage facts and keep protections. Quest disposal comes only after lifecycle facts and supported progress checks are documented and tested.
5. **Measure quality.** Review a representative 100–200-item bankable sample across progression levels, then expand exact-ID families. Track wrong destinations, missing roles, outfit separation, variants and false removal suggestions. Run existing classification audits, organizer simulations and meaningful regressions after code changes. No blanket claim of being the best sorter without comparable evidence.

The initial sample is a milestone, not completion of the requested full catalog improvement. Maintain the ledger as successive item groups are reviewed.

## Token budget and current change

The previous local highest estimate is **196,165 review tokens**, leaving **3,835** to the strict 200,000 ceiling. This is not an official Hub count. This research adds documentation only and makes no production code changes; tests were not rerun for documentation. Before publishing any implementation, rerun `tools/review-size/README.md`'s estimator, inspect resources separately and retain several thousand tokens of headroom. Metadata resources are not assumed to be exempt from official review scope.

No code, branch, commit, push or Hub submission was changed by this research task.

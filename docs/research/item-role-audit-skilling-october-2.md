# Skilling outfits and visible usage facts — 2 October 2026

Third focused batch from the [placement research](plugin-hub-placement-and-cleanup-october-2.md), following the [functional-item batch](item-role-audit-functional-october-2.md). Changes remain original and manually guided; no competitor data or code was used.

## Placement audit

Checked all 83 exact-ID entries in the existing 15 `tools` sets: they already classify as TOOL. Reviewed the other named skilling outfits in the effective export. No blanket category change was justified.

Guild hunter headwear/top/legs/boots (29263, 29265, 29267, 29269) had the generic `skilling-equipment` subcategory. They now use `skilling-outfit`, joining the existing outfit routing behavior. Added two exact outfit families to the tool layout resource:

- Guild hunter: 29263, 29265, 29267, 29269.
- Golden prospector: 25549, 25551, 25553, 25555.

Both now have head-to-feet vertical column rules when sufficient layout space is available, through the existing layout engine. Guild hunter also has a common family name in the existing sequential sorter. The 32 existing tool resource rows remain unchanged; the two new families are appended. Tests verify the resulting columns.

These are exact bankable IDs, not broad display-name assignments. Cert/restricted/cache variants are not promoted merely because they resemble outfit names. Already correct quest-tools retain their existing functional treatment; no new disposal policy was added.

## Usage coverage

Added **133 exact-ID usage rows** to `item-usage-tags.tsv`. Total effective items with nonempty semantic tags is now **170**: 162 resource-tagged items plus eight existing static Herblore records. This remains curated initial coverage, not full catalog coverage.

| Item group | New exact IDs | Usage facts |
| --- | ---: | --- |
| Reviewed Graceful sets/recolours | 66 | skilling-outfit, weight-reducing |
| Angler and Spirit angler | 8 | skilling-outfit, fishing-utility |
| Carpenter | 4 | skilling-outfit, construction-utility |
| Farmer variants | 8 | skilling-outfit, farming-utility |
| Lumberjack | 4 | skilling-outfit, woodcutting-utility |
| Prospector and Golden prospector | 8 | skilling-outfit, mining-utility |
| Pyromancer | 4 | skilling-outfit, firemaking-utility, warm-clothing, wintertodt |
| Rogue | 5 | skilling-outfit, thieving-utility |
| Smiths | 4 | skilling-outfit, smithing-utility |
| Zealot | 4 | skilling-outfit, prayer-training |
| Guild hunter | 4 | skilling-outfit, hunter-utility |
| Raiments of the Eye colour variants and shared boots | 13 | skilling-outfit, runecrafting-utility |
| Warm gloves | 1 | warm-clothing, wintertodt |

Warm gloves intentionally do not receive `skilling-outfit`: matching the Pyromancer appearance does not make them part of its experience set. No universal experience-boost claim is attached to skilling-outfits: several sets provide other kinds of utility. Zealot's training role does not imply a rule about retaining/discarding any particular bone supply.

Independent reference pages: [skilling equipment](https://oldschool.runescape.wiki/w/XP_clothing), [Graceful outfit](https://oldschool.runescape.wiki/w/Graceful_outfit), [Lumberjack outfit](https://oldschool.runescape.wiki/w/Lumberjackoutfit), [Pyromancer outfit](https://oldschool.runescape.wiki/w/Firemaking_outfit), [Rogue equipment usage](https://oldschool.runescape.wiki/w/Money_making_guide/Pickpocketing_master_farmers), [Zealot training](https://oldschool.runescape.wiki/w/Bonemeal_%28bones%29), [Guild hunter outfit](https://oldschool.runescape.wiki/w/Guild_hunter_outfit), [Golden prospector kit](https://oldschool.runescape.wiki/w/Golden_prospector_kit). Exact identities/order come from the repository's independently reviewed outfit resources and effective registry. Consulted Wiki search content was cached and some direct pages were blocked; this is not a fresh October bulk snapshot. New facts describe stable skill association, not current numeric bonuses, account prerequisites or removal eligibility.

## User-visible explanation

Blueprint cells now show a wrapped tooltip containing:

1. Item name/quantity, retaining existing compact labels.
2. Placement, from the effective routing tag, including a personal assignment.
3. Known uses, from the separate semantic tag set, written with spaces rather than internal hyphens.

The sidebar and full blueprint editor share this cell renderer. No permanent sidebar space was added. Empty semantic tags omit the Uses line; absence of a role is not evidence that an item lacks that use. HTML metacharacters in item text are escaped by the existing sidebar formatter. The immutable usage facts are accessible from `BankPreviewItem` without changing sorting or bank actions.

Rendered and visually inspected `build/reports/ui/item-usage-tooltip.png`: a HAM robe personally assigned to Frequently Used still displays its Thieving, quest and clue functions on separate wrapped lines. The Helm of raedwald exception remains tested.

## Verification

- Targeted tag, panel and outfit-layout regressions passed; tooltip was rendered and inspected.
- Full effective export retains 32,586 / 606 effective/excluded records. Exactly four subcategories change (Guild hunter), with no broad-category changes; 133 previously untagged IDs gain roles.
- Full build passed: **1,115 tests**, zero failures/errors, **1,950 simulations completed**. All four simulation baselines remain unchanged and passed; no baseline hashes were rewritten in this batch.
- Two existing tests initially flagged the old Guild hunter subcategory and the old tool-family fingerprint. Updated only those reviewed expectations. New column tests retain the behavior checks.
- Highest local review estimate: **196,699**, headroom **3,301**. Changes to resources, tests and documentation are excluded from that estimate and were reviewed separately. The official Hub tokenizer/scope are unknown.
- Compiler repeated the existing unchecked/unsafe operations note for `IronmanBankArchitectPanel`; it is not a test failure.

No commit, push or publication. Continue exact-ID audits for quest lifecycle, clue overlaps, supplies and storage prerequisites; current metadata does not produce player-specific disposal advice.

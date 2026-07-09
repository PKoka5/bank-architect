# BankTabs Pattern Research

Status: dev-only research plan
Date: 2026-07-09

## Goal

Use public OSRS bank organization screenshots and discussions as inspiration for better Bank Architect sorting rules.

This is not a source for copied layouts. The output we want is original, reviewed product logic:

- category order patterns;
- subcategory order patterns;
- common item clusters;
- cleanup and external-storage candidates;
- account-type differences between ironman, main, PvM, skiller, and PvP banks.

## Safety Boundaries

This research stays outside the RuneLite plugin runtime.

Allowed:

- manually review public screenshots and discussions;
- save local dev-only notes;
- run local pixel or icon matching experiments against screenshots the developer intentionally places in a research folder;
- convert repeated patterns into original category and sorting rules after human review.

Not allowed:

- copy a user's exact bank layout;
- bundle Reddit screenshots, user images, or third-party assets with the plugin;
- scrape Reddit from the plugin;
- add runtime network calls;
- generate production rules directly from unreviewed pixel matches;
- use usernames or post-specific layouts as product names, presets, or examples.

## Sources Checked

- r/BankTabs is dedicated to organized RS3 and OSRS bank tab screenshots and includes OSRS, OSRS Iron, upgrade, cleanup, and reorganization posts: https://www.reddit.com/r/BankTabs/
- r/BankTabs new feed shows current examples such as maxed ironman, GIM, main, PvM, upgrade-path, and garbage-cleanup banks: https://www.reddit.com/r/BankTabs/new/
- A BankTabs OSRS iron post discusses splitting ironman gear into magic, melee, and ranged, while acknowledging many players keep one gear tab: https://www.reddit.com/r/BankTabs/comments/1el80wq/osrs_iron_bank_tab_desc_in_comments/
- OSRS bank organization discussion commonly recommends a quick-access tab plus gear, skilling, loot, and item-type tabs: https://www.reddit.com/r/osrs/comments/1fqn7al/is_there_a_guide_on_bank_organization/
- Ironman organization discussion mentions currency/default tab, alchables, junk, equipment/consumables, and near-term skilling tabs: https://www.reddit.com/r/ironscape/comments/1dv7mnl/any_tips_on_bank_organisation/
- Ironman organization discussion highlights preplanning, bank fillers, and gear columns arranged for fast manual gearing: https://www.reddit.com/r/ironscape/comments/w75ho4/how_do_you_organize_your_bank/
- Bank Tag Layouts discussion points out that layout tools work well for herblore, gear, and general organization, which supports our future export direction without replacing that workflow: https://www.reddit.com/r/2007scape/comments/1537meb/bank_tag_layouts_is_so_cool_anybody_come_up_with/

## Early Pattern Notes

### Whole-bank tab patterns

Good-looking banks tend to start with a fast-access tab, then move into larger domain tabs.

Common order:

1. Currency, teleports, runes, jewellery, account utilities
2. Combat gear
3. Potions, food, PvM supplies
4. Farming, herblore, secondaries
5. Skilling tools and skilling outfits
6. Raw and processed resources
7. Slayer, boss loot, clue loot, collection items
8. Cosmetics, holiday, capes, outfits
9. Junk, quest leftovers, duplicate/redundant items

Bank Architect already has a close 10-tab structure. The main improvement is not more top-level categories; it is better ordering inside each category and splitting the review bucket into clearer review lanes.

### Ironman-specific patterns

Ironman banks carry more retained items than mains, so a single "sell it" or value-first cleanup model is wrong.

Useful ironman tendencies:

- Keep combat styles near each other because progression gear is uneven.
- Keep teleports, rune pouch support, jewellery, and diary utilities close to the top.
- Keep herblore and farming near each other because herb runs, secondaries, seeds, compost, and potions form one workflow.
- Treat POH, STASH, Seed Vault, spice rack, and other storage as advisory destinations, not automatic cleanup.
- Keep rare or annoying-to-reobtain quest/progression items in review instead of declaring them trash.

### Main/PvM patterns

Main and PvM banks appear more willing to group by activity value and upgrade path.

Useful tendencies:

- Gear often dominates early tabs.
- Spec weapons, switches, capes, jewellery, and ammo are often kept near core gear.
- Supplies are often arranged by potions, food, boosts, and restore/prayer support.
- Loot and alchables are more likely to be grouped as sell/value review.

For Bank Architect this should be an optional preset difference later, not the default ironman logic.

### Skiller patterns

Skiller banks usually benefit from cleaner resource and tool ordering.

Useful tendencies:

- Tools before raw materials.
- Raw materials before processed materials.
- Outfit pieces together.
- Near-term training supplies should be easy to scan.

### Cleanup patterns

Cleanup is not one category. It should become a review tab with sublanes:

- quest leftovers;
- clue and STASH candidates;
- cosmetics and collection;
- holiday/event items;
- POH/costume room candidates;
- redundant low-tier gear;
- burnt/junk/one-off items;
- alch/value review for mains;
- keep-review for ironmen.

## Dev-only Pixelmatch Pipeline Proposal

Folder shape:

```text
tools/research/banktabs/
  input-screenshots/
  output-crops/
  output-matches/
  reviewed-patterns/
```

Pipeline:

1. Manually save public screenshots into `input-screenshots`.
2. Crop the bank area manually at first; automate only after the format is stable.
3. Detect the 8-column bank grid and crop individual slots.
4. Match each slot crop against local RuneLite item sprites/cache.
5. Output top item candidates with confidence, never directly into production rules.
6. Review matches manually.
7. Store only pattern notes and aggregate counts.
8. Convert repeated, reviewed patterns into original Bank Architect sort rules.

Possible reviewed output:

```text
source_id	tab_type	slot_group	pattern	confidence	review_note
manual-001	gear	first_rows	melee/range/mage mixed	high	iron banks often keep all gear in one tab
manual-002	supplies	left_to_right	potions before food	medium	main PvM banks commonly prioritize potions
manual-003	review	end_tab	cosmetics plus quest leftovers	high	needs separate review lanes
```

## Production Rule Ideas

### Combat Gear

Sort inside gear by:

1. weapons;
2. offhands and shields;
3. helmets;
4. bodies;
5. legs;
6. boots;
7. gloves;
8. capes;
9. jewellery;
10. ammo and utility gear.

For ironman, keep melee/ranged/magic in one large gear tab by default. A later preset can split styles for PvM-main accounts.

### Teleports, Runes, Jewellery

Sort inside teleports by:

1. account utilities and diary/achievement items;
2. rune pouch and rune support;
3. teleport jewellery;
4. teleport books and tablets;
5. scrolls and one-off teleports;
6. niche transport items.

### Potions, Food, PvM Supplies

Sort inside supplies by:

1. prayer/restore/sustain;
2. combat boosts;
3. antifire/antipoison/utility;
4. food by practical tier;
5. niche PvM supplies;
6. unfinished or review-only consumables.

Potion dose ordering can later become a setting because users disagree on visual layout.

### Farming and Herblore

Sort inside farming/herblore by:

1. seeds by farming use;
2. saplings;
3. compost and farm tools;
4. grimy herbs;
5. clean herbs;
6. secondaries;
7. unfinished potions.

### Resources

Sort inside resources by:

1. mining/smithing;
2. woodcutting/fletching;
3. crafting;
4. runecrafting;
5. hunter/fishing/cooking raw materials;
6. processed outputs.

### Review

Replace a vague cleanup pile with review lanes:

1. external storage candidate;
2. quest leftover;
3. clue/STASH candidate;
4. cosmetic/collection;
5. holiday/event;
6. redundant gear;
7. junk/burnt;
8. alch/value review;
9. unknown-safe-review.

## Roadmap Fit

Recommended new step:

`C5b - Dev-only BankTabs Pattern Research`

- collect hand-picked screenshots;
- write manual pattern notes;
- prototype local grid cropper;
- prototype local sprite matcher;
- review matches;
- convert only aggregate insights into original rules.

Recommended production step after that:

`C5c - Pattern-informed Subsorting Rules`

- improve category-specific sorters;
- split cleanup/review lanes;
- add tests with synthetic bank snapshots;
- keep the plugin read-only and manual-action-only.

## Open Questions For Product Taste

- Should ironman default keep all gear in one large tab, with style grouping only inside the tab?
- Should main/PvM presets split gear more aggressively by combat style?
- Should potion dose ordering be compact by type or visual by dose rows?
- Should review tab prioritize "store elsewhere" first or "junk/remove" first?
- Should Bank Architect show review lanes as labels inside the popup, or only as sort order?

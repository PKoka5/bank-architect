# Main account preset: plan

## Later owner decisions

The local implementation and verification are recorded in
`../testing/main-preset-2026-10-05.md`. The owner subsequently changed the Main
potion-dose order to **4 -> 3 -> 2 -> 1**, requested the existing BIS combat
matrix, and confirmed that Ironman's apparent change came from selecting a
custom layout. There is now one preset chooser in the header, with immutable
Ironman/Main defaults and automatically saved custom copies. These later
decisions supersede the ascending dose order in the original plan below.

Prepared on 2026-10-05 against released source
`a0519b05f18b164995235dd440f48637b45ed067` (0.8.1).
The owner selected **All-round Main**: everyday play, PvM, Slayer, skilling and
clues. The owner subsequently requested template data as the primary source for
measuring common grouping patterns and designing our own categories/algorithm.
The owner then selected the core Main destination and ordering policies below.
Those choices take precedence over the earlier exploratory placement proposals.
This document is a proposal for implementation, not a working Main preset.
No production changes, commit or publication accompany this plan.

## Goal

Offer an original Main account preset alongside the existing Ironman preset.
Both use the same item facts, tags, set definitions, sorters, dense layouts and
manual guide. A preset changes default grouping and selected placement policies;
personal layouts and corrections remain editable.

Main should help a player find usable gear and supplies quickly, retain useful
skilling workflows, and inspect loot separately from possessions they use.
Tradeability alone does not determine an item's destination. A tradeable weapon
can be combat gear and a tradeable seed can be Farming.

## Working definition and approved core rules

For this product, an **All-round Main bank** supports daily play, PvM/Slayer,
skilling and clues, with quick access to useful possessions and recognizable
item families. The player chooses Main explicitly. Bank size, expensive gear or
a tab called Main must not determine account type.

Start with independently reviewed item roles, before deciding destinations:
combat gear/ammunition, consumables, travel/magic supplies, tools/functional
outfits, training materials/ingredients, clues/cosmetics/collections and
quest/uncertain items. These are analysis dimensions, not fixed tabs. An item
may have several relevant roles; the default home needs a documented reason,
and an explicit personal assignment takes precedence.

The owner approved these **Main-only defaults** on 2026-10-05:

| Core destination | Approved content and order |
| --- | --- |
| Teleports | All runes, teleport tablets and reviewed teleport items together, with recognizable item/charge families. |
| Supplies | All food and finished potions. Each exact potion family stays together in ascending dose order **1 -> 2 -> 3 -> 4**, read left to right. |
| Farming / Herblore | Stage-first runs: **grimy herbs -> clean herbs -> Farming seeds -> unfinished potions**. Keep each run contiguous; do not reuse Ironman recipe rows. |
| Combat | All combat styles together with recognizable gear/set groups. **Saturated heart (27641)** has its default home here, in combat utility. |

Within the herb runs, use a consistent species order where applicable. Other
Farming/Herblore ingredients remain recognizable groups; their detailed order
can be refined later. Farming seeds do not include weapon/tool crystal seeds
or teleport seeds merely because their names contain "seed". Reviewed food
identity determines the food group; unfinished potions retain ingredient identity.

Only present entries are placed. Missing doses or family members create no
fillers. Long stage runs can continue onto further rows while remaining
contiguous. Personal assignments, captured layouts and Keep current order have
priority. Ironman keeps its existing routing, recipe/stage and dose behavior.

The Saturated heart choice is a Main destination policy, not a change to its
shared reusable-boost facts or a claim that it is equippable. The owner did not
specify Imbued heart's default destination in this decision.

The physical tab order and remaining tool/material/clue/cosmetic/quest/storage
combinations remain open. Further template research can refine these details
and verify usability without holding up the approved core choices.

The bank alone does not reveal frequency of use, intended sale/alch, future
training plans or possessions stored elsewhere. Repeated template placement
can suggest a default; it cannot establish the player's intention. In
particular, tradeability, quantity and ownership of stronger gear do not
establish that an item is unwanted.

For undecided details, record the competing choices, suitable complete-template
support as **x/y**, progression/activity differences, missing evidence and
concrete placement cases. Apply independent item facts to tools, outfits and
cosmetics even if a template contains a classification mistake. The output is
an original category/policy proposal that works with the player's actual bank,
including small banks and incomplete families.

The present four complete inputs support a pilot comparison. The nine partial
candidates guide sample selection and qualitative questions; full observations
are required before extending the numeric denominator. Complete the midgame
and compact-bank comparison before claiming broad support. Owner-selected
defaults do not depend on a majority claim.

The [first role report](../research/main-role-groups-2026-10-05.md) now defines
eleven original analytical groups and eighteen selected-ID comparisons, checked
independently. These are not eleven required tabs. The evidence supports testing
recognizable supplies and functional sets, and keeping ingredients distinct from
ready items. Law/Nature runes share a tab with the house tablet in 1/3 eligible
complete inputs; that historical observation remains unchanged. The owner has
chosen combined Teleports regardless of that small-sample result.

## 1. Research Main workflows and define placement examples

Build a reviewed cohort of 12-20 public Main community templates across
early/mid/endgame, small/large banks and mixed PvM/skilling/clue goals. Compare
their measurements with real-player banks and explanations. This is the planned
research cohort; that full comparison has not yet been completed. Do not infer account type from
wealth or equipment alone. Record explicitly declared account types, player
goals and evidence quality.

Use the owner's requested community-template data as statistical observations.
Selected public item/tab observations can be measured in the development-only,
git-ignored research cache. Keep third-party implementation and individual
layouts out of production; AGENTS.md's originality rule remains in force.
Version only aggregate conclusions and independently designed Bank Architect
rules. Do not recreate an individual template's positions or ship its data.

Record each cohort member's public ID/URL, account evidence, date, source/layout
hashes, progression/focus and author evidence. "Main tab" does not establish a
Main account. Keep ambiguous account types, Pures and dedicated Skillers out of
the primary All-round Main sample. Collapse identical layouts and inspect author
or revision clusters so one popular source does not receive multiple votes.

Use equal independent-source votes first; compare popularity weights separately.
Count grouping support only among templates containing the relevant items or
family members. Report **x/y eligible templates** for same-tab grouping, adjacency,
family cohesion, split families and dose direction. Missing items are not votes
against a grouping. Physical tab indexes are not consistent semantic labels.
Partial page summaries can discover candidates but cannot establish full
item-presence or positional denominators. Our existing registry labels alone
cannot independently validate a proposed category.

Compare:

- Items players want in quick access versus their normal home.
- How ready-to-use potion doses relate to ingredients and unfinished potions.
- Gear sidegrades, spec weapons, ammunition and functional skilling outfits.
- Resources kept for training versus drops the player intends to sell.
- Clue items, quest items, cosmetic sets and collection trophies.
- Differences between progression stages and personal storage choices.

The July cache contains 21 complete selected public imports, including four
explicit Main templates: **8, 14, 26, 55**. These are four distinct layout hashes,
predominantly late/endgame; they form an initial cohort, not a majority study.
The July public metadata snapshot contains 20 explicit Main-title candidates out
of 188 gallery templates, so exact Main coverage is 4/20 historical candidates.
Existing all-input analyzer results mix account types and are not Main results.
The analyzers now support explicit selection with `-RepoIds "8,14,26,55"`;
use that filter and separate output paths for the initial Main pass.

The first selected-cohort pass is recorded in
[the Main template baseline](../research/main-template-baseline-2026-10-05.md):
four templates, 39 tabs and 3,942 positive placements. The added co-presence
denominators were independently checked. It remains an exploratory baseline,
not a completed majority/category comparison.

The [additional template search](../research/main-template-candidates-2026-10-05.md)
found nine more explicitly Main-titled public indexed pages, giving 13 sourced
candidates including the original four. Exact-input coverage remains four:
partial summaries cannot extend positional measurements. Prioritize IDs **222,
270, 114 and 268** for complete observations: an explicitly midgame Main, a smaller
bank of unknown progression and two compact maxed Main banks. The summaries show
both combined and separate combat-style grouping. The owner has selected
combined Combat; compare candidates to refine its internal set/style groups.

The live gallery/sitemap could not be fetched reliably today; indexed public
detail summaries are available for discovery. Refresh candidate metadata and
obtain complete selected observations through an allowed public workflow before
extending exact measurements. Do not claim complete current gallery coverage.
Two accessible player discussions illustrate useful questions, not a universal
Main bank layout:

- [A player selling their loot tab after maxing](https://www.reddit.com/r/BankTabs/comments/11ktper/today_i_maxed_sold_my_loot_tab_and_heres_the/)
  discusses combining supplies, keeping lower potion doses and future goals.
- [A casual player's bank organisation request](https://www.reddit.com/r/BankTabs/comments/1sjmfkn/bank_sorting_help/)
  describes buying unused items to fill example layouts. Comments disagree on
  some keep/sell decisions. Our layout continues to use only bank entries the
  player actually has and does not invent fillers or a shopping list.

Deliverable: a small evidence table and placement cases with item IDs, intended
roles, Main default, Ironman default and any explicit uncertainty. Validate game
facts against supported RuneLite metadata or pinned authoritative game sources;
community opinions alone do not establish that an item is useless.

## 2. Choose original destinations and placement policies

The four approved core destinations and their ordering policies are fixed design
requirements for the first Main preset. Other destination combinations remain
provisional. Use item facts, further evidence and usability checks to refine
internal grouping, retaining personal overrides.

Starting proposal, to refine using phase 1:

The table is a destination inventory, not approved physical tab numbering.
Only the four marked core destinations are owner-approved. A distinct loot
destination and the remaining combinations still need explicit intent policies
and design refinement.

| Destination | Default purpose | Status |
| --- | --- | --- |
| Main | Currency and reviewed frequently used utilities | Provisional |
| Combat | All combat styles, ammunition, special-attack items and Saturated heart | Approved core |
| Supplies | All food and finished potion families, doses 1 -> 2 -> 3 -> 4 | Approved core |
| Teleports | All runes, teleport tablets and reviewed teleport items | Approved core |
| Tools | Skilling tools, functional outfits and containers | Provisional |
| Materials | Skilling materials by activity/production family | Provisional |
| Farming / Herblore | Grimy, clean, Farming seeds and unfinished potion runs; other ingredients | Approved core |
| Loot | Reviewed loot and items the player assigns for sale | Provisional |
| Clues / cosmetics | Clues, cosmetic sets and collection trophies | Provisional |
| Quest / storage | Quest items, uncertain items and storage review | Provisional |

These are logical blueprint destinations; players can rearrange tags/tabs.
The Main account preset and its Main bank destination must be distinguished in
UI wording. The existing BankPresets.MAIN scaffold is a starting point, not an
approved final taxonomy.

Core policies above are approved. Additional proposed Main policies:

- Preserve gear sets, combat styles and useful sidegrades. Quantity or ownership
  of a stronger item alone does not turn gear into sell/alch candidates.
- Keep functional outfits with tools; cosmetic outfits with cosmetics. Resolve
  set membership using the shared reviewed item-ID catalog.
- Group raw/processed resources by their use, not just their saleability.
- Keep review wording neutral. The bank does not reveal whether a stack was
  bought for training or received as loot; personal assignments resolve that
  ambiguity. Do not infer sell intent or promise the item is safe to discard.
- Continue using manual corrections, blueprint editing, Save current bank and
  Keep current order. Explicit player choices take precedence.

Known shared classification reports (Ghrazi rapier, imbued/saturated hearts and
random-event camouflage) must be checked before reusing the catalog in Main.
Those reports were triaged but were not fixed in the 0.8.1 release. Correct any
confirmed facts once in the shared classification layer, with regressions;
avoid creating different underlying item facts per preset.

## 3. Make the shared engine support real presets

Current blockers verified in the repository:

- BankTags binds semantic tags to Ironman category keys. Most Main scaffold
  categories have no tags, and tagFor can throw for them.
- PresetCategoryMapper.mapMain uses only broad ItemCategory values and never
  fills cosmetics-outfits automatically.
- The plugin/layout model hardcodes Ironman. BankAnalysis fixes the preset in
  its constructor, and BankAnalysisRequest does not carry a selected preset.
- Profiles, corrections and orders lack preset identity. BAv1 share codes also
  omit it.

Separate stable item-role/tag identities from each preset's destination mapping.
Keep legacy tag IDs compatible. Define preset defaults and policy differences
with compact data or shared methods; do not duplicate the Ironman classifier,
sorters, editor or guide for Main. Reuse the existing family/set catalogs.

Capture the selected preset coherently with every analysis request. On a preset
change, invalidate older analysis callbacks and reset the guide session; update
blueprint editing context and caches together. A result for Ironman cannot later
replace the Main preview.

Implementation files likely include BankPresets/BankTags/PresetCategoryMapper,
BankLayoutPlan, BankAnalysis/BankAnalysisRequest, plugin/config/model/sidebar
wiring and any genuinely preset-dependent preview policy. Inspect their callers
before editing; this is a scope map, not a requirement to rewrite every file.

Core-policy extension points verified after the owner decision:

- `PresetCategoryMapper.mapMain` currently receives only `ItemCategory`. Use
  item-aware reviewed routing for Main, including the exact Saturated heart
  destination, without reclassifying its shared facts as equippable gear.
- Pass preset policy through `BankOrganizationPreviewBuilder` into category
  sorting/layout. `SupplyItemSorter` and `PotionDoseSemanticRuleSet` currently
  both use descending doses; Main needs ascending order in both while retaining
  current Ironman behavior and personal order overrides.
- `HerbloreItemSorter.layoutByKind` is useful groundwork, but its run order and
  appended Farming layout do not implement the approved Main stage blocks.
  Keep Farming seeds in their stage and preserve dense, present-only groups.
- Reuse reviewed teleport/rune families and gear/set grouping. Give the heart a
  combat-utility position rather than leaving it in a generic tail.
- Resolve preset-aware tag destinations, options and storage before exposing
  Main; changing scaffold category labels alone does not make a usable preset.

## 4. Preserve user work when switching

Use one shared storage mechanism with stable preset identity. Keep the active
plan/profile, named layouts, item and block orders, captured routes, item
placement corrections and Keep current order choices per preset. Shared display
preferences can remain global.

- Existing installations remain on Ironman.
- Legacy data and BAv1 codes belong to Ironman; retain original data during
  migration and do not silently reinterpret it as Main.
- The first Main selection creates its own default state.
- Ironman -> Main -> Ironman restores the exact previous personal layout.
- The same profile name can exist independently in both presets.
- Add preset identity to a new share-code version. Validate before changing
  stored state; reject unsupported future formats without overwriting them.
- An import for another supported preset clearly identifies its account preset
  and cannot overwrite the current preset's work silently.
- Bank capture and Keep current order continue to preserve placeholders,
  duplicate occurrences and logical destination choices.

Expose the Ironman/Main selector after routing, storage and coherent analysis
work together. Skiller/PvP/PvM remain unavailable until their own policies are
ready; their future support should use this same mechanism.

## 5. Test, then review in game

Meaningful acceptance cases:

1. A Main preview with its actual default layout routes every bank entry exactly
   once, preserves quantities/placeholder occurrences and stays dense.
2. Small, empty-category, medium and large Main banks complete manual guidance
   in both rearrange modes without stalls or invalid locks.
3. Gear/cosmetic/functional sets, potion doses, skilling materials, loot ambiguity
   and the confirmed shared classification fixes match the reviewed cases.
   Main specifically combines runes/teleports, keeps every finished-dose family
   in 1 -> 2 -> 3 -> 4 order, uses grimy/clean/seeds/unfinished stage blocks, and
   places Saturated heart in Combat. Test missing variants and personal overrides.
4. Legacy Ironman state survives migration, round-trip switching and restart,
   including identical profile names, edits, captures and Keep current order.
5. Old/new share codes retain their preset and flags; malformed or future codes
   leave stored state intact.
6. Switching during a running analysis or an open editor cannot apply stale data.
7. Bank capture, incoming/outgoing guidance, saved order restoration, progress
   and overlay colors agree in both presets.
8. Existing Ironman regression tests and all four fixed simulation baselines stay
   unchanged. Add representative Main simulations without replacing Ironman ones.

After automated checks, perform the owner in-game round: select Main, analyse,
organise, close/reopen, make a personal correction, capture the bank, keep a tab's
current order, save/reload and switch back to Ironman.

## Review size and release

The highest calibrated local estimate for 0.8.1 is **197,370**, leaving **2,630**
below 200,000. This is not the official Hub count.

Start the engine work with behavior-preserving simplification to create room.
Use **195,000 or lower** as a local planning target for the finished Main release,
so the feature does not consume the entire estimated margin. The standing
requirement remains an official Hub submission strictly below 200,000.
Measure after meaningful Java changes and again before publication; audit tests,
resources and other files outside the estimator separately. Never save tokens by
dropping functionality, necessary tests or shortening stripped comments.

Target Main as **0.9.0**, subject to passing the test round and release checks.
Update the bundled notice so players see the preset choice and what it changes.
Publishing requires the owner's explicit go-ahead. Skiller and PvP follow in
separate tested updates.

## Next action

The core Main policies are now approved. Define physical tab order and remaining
destinations, then implement the compact shared preset mechanism with these
Main-specific rules. More complete template inputs can refine the remaining
details and simulations; they do not reopen the owner's core decisions.

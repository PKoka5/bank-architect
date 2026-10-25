# Skill activity groups

25 October 2026. Local work above published v0.9.8 / `675ccbd`.
The owner authorized automatic skill grouping, including fish barrels and log
baskets, after sharing the Reddit feedback. No release or Git operation is
authorized. This is the first activity-group slice; selectable combat-style
tags and user-created named categories remain separate future work.

## Result and the owner's Frequently Used preference

The automatic packed Tools layout retains its owned outfit columns and places
matching equipment around them. Mining, Woodcutting, Fishing and Thieving are
explicit quality cases. Existing tool families also cover Farming, Construction,
Crafting, Hunter, Firemaking, Cooking, Herblore, Runecrafting and Sailing, with
new Smithing and Agility equipment families.

- Mining includes the reviewed pickaxes, mining gloves, celestial rings/signets,
  Mining capes and coal/gem bag forms.
- Woodcutting includes axes/felling axes, Woodcutting capes, Forestry kit, both
  log basket and Forestry basket forms, and cape pouch.
- Fishing includes the owned Angler/Spirit angler pieces, Fishing capes, fishing
  tools, tackle box and both forms of fish barrel/fish sack barrel.
- Thieving includes Rogue outfit, Thieving capes, lockpicks and reviewed Oak,
  Willow and Maple blackjack variants.
- Skill-specific containers no longer share the generic utility-container
  family merely for being containers. Looting bag forms and Soul bearer retain
  that generic family. Goldsmith gauntlets belong to Smithing rather than Mining.

The owner subsequently requested both Frequently Used and skill-group behavior
through a settings checkbox. The existing **Frequently used > Gather frequently
used items** checkbox already expresses that preference and is reused:

- On retains the existing Ironman quick-access selection, including its best
  owned axe/pickaxe and normal Frequently Used utilities.
- Off retains the items' normal categories, where the Tools layout groups them
  with their matching skill outfits and equipment.

The existing preset defaults and profile persistence are unchanged. Main keeps
its existing default without automatic Frequently Used gathering. Personal item
categories always win. No additional setting, profile field or duplicate bank
item is introduced. The setting and Tools layout descriptions now explain the
relationship to skill grouping.

## Layout and ownership

`ToolOutfitSemanticRuleSet.groupSkills` builds from the already planned real
items. It fixes every present outfit piece and the existing Slayer/cannon cells.
The largest owned compatible outfit variant anchors a skill; complete colour
variants and compatible partial variants keep their existing outfit ownership.

Each skill first reserves up to four free continuation cells below that outfit.
Its remaining owned tools are then placed near the middle of that activity's
occupied column. This avoids turning a large tool family into an excessively
tall column or leaving a cape at the distant end. Outfits with matching tools
receive the nearby space before tool families with no present outfit.
Unanchored short families use a contiguous run that fits a physical row when
available. Other real items fill the remaining cells in their existing order.
The pass creates no empty cells or missing outfit parts and never changes a
category, destination, quantity or placeholder state.

Spatial grouping is intentionally limited to the automatic packed Tools layout:

- items moved to other tags/tabs remain in those destinations;
- explicit block arrangements retain the preceding arrangement behavior;
- List retains its chosen reading-order behavior;
- saved physical item order and accepted blueprints still override the proposal;
- repeated physical copies use the existing dense/semantic path rather than
  applying a one-ID/one-cell spatial assumption. Their stacks are conserved.

The cannon/Slayer placement is retained as a reserved region. Shared items get
one primary skill in the reviewed table and appear once in the physical bank.
This is a local blueprint/manual-guidance change, with no game action or network
operation added to the plugin.

## Reviewed data and provenance

`tool-layout-families.tsv` now declares the skill beside each outfit family.
Existing exact outfit IDs and order remain intact, including the four later
Prospector/Forestry/Graceful/Smiths variants. Tool families have reviewed usable
IDs, charged/uncharged tool forms, container states and cape variants. Canonical
identities were checked against the existing developer Wiki infobox snapshot;
it does not establish an unseen live page revision. Dummy cache IDs such as
25583/25586 and 28141/28144 are deliberately not adopted as usable variants.

The supporting Wiki search results checked in this session include:

- [Inventory Expansion](https://oldschool.runescape.wiki/w/Inventory_Expansion),
  which identifies fishing and Woodcutting containers and their functions;
- [Container items](https://oldschool.runescape.wiki/w/Container_items), for the
  distinctions between skill-specific bags and other containers;
- [Skilling equipment](https://oldschool.runescape.wiki/w/Skilling_equipment) and
  [Mining enhancers](https://oldschool.runescape.wiki/w/Mining_enhancers), for
  mining equipment/accessory relationships.

The closed/open Forestry basket IDs **28143 / 28145** previously classified as
SKILLING material. Two exact canonical overrides now classify them as TOOL /
resource-container. The explicit development compiler regenerates the checked-in
33,766-record runtime registry. Exhaustive source/compiled equality and stale
artifact checks pass. No classification-derivation code or raw input is added
back to the runtime JAR.

`OrderedItemFamiliesTest` consciously updates only the Tools data fingerprint
and associated skill-prefixed variant keys. The four explicit variant-membership
assertions remain. The other bundled-table fingerprints are unchanged. New
tests also validate unique primary tool-skill membership, registered IDs and the
deliberate exclusion of dummy identities.

`AcceptedBlueprint.RULES` advances from 3 to **4**. Older known-format snapshots
retain their saved targets; the latest proposal is available separately.

## Verification

**2,342 tests pass**, with zero failures/errors/skips. The 20 new activity cases
cover both presets, including where applicable:

- complete 105-item mixed kits with vertical outfits, the four core equipment
  continuation cells and bounded proximity of remaining skill equipment;
- small and partial mixed-variant outfits, both container states, shuffled
  inputs, varied quantities and withdrawn placeholders;
- the Frequently Used checkbox's two Ironman behaviors and explicit personal
  Frequently Used assignments;
- moved container tags and personally reassigned outfits;
- saved and previously accepted physical order, Fixed Alch and Crafting cape's
  teleport function;
- repeated physical stacks/placeholders and exact multiset conservation;
- primary-skill table identity and non-overlap checks.

Existing outfit colour/partial-column, cannon/Slayer, catalog, profile and other
regression tests remain active. The mixed rerouting test now checks the generic
Looting bag/Soul bearer run separately from the retained Herb sack/Seed box;
its dense permutation, quantity and conservation checks remain intact.
The direct semantic test consciously expects Goldsmith gauntlets outside Mining
and fishing container forms earlier in their reviewed skill-family order.

The complete offline build succeeds in **38 seconds**, with no compiler warning
in its log. All **1,950** scenario banks complete: 150 standard plus 1,800
multi-seed cases. All four normalized simulation reports match the saved
pre-change reports and pass the existing fingerprints without modification.
This complements the targeted tests; the simulation sample is not every bank.

Logs:

- `build/tmp/skill-groups-quality-2026-10-25.log`;
- `build/tmp/skill-groups-full-2026-10-25.log`;
- `build/tmp/skill-groups-size-2026-10-25.log`.

Text/HTML and rendered Tools PNGs are under `build/reports/skill-groups/` for
IRONMAN and MAIN. Visual review confirms the owned skillcapes/core equipment
below their outfits, nearby barrels/baskets/blackjacks, and the retained
cannon/Slayer cluster. These are developer simulations; a fresh owner in-game
test of this feature remains outstanding.

## Budget and artifact

The highest final main-Java estimate is **194,457**, giving **5,543** estimated
review tokens of margin. Other estimates: 191,225 / 194,173 / 191,502.
Calibration remains the maintainer's 200,414 at
`def1e856e101ff0e57dd96adccab2cf1d886b076`. This is not the official Hub count.
Resource/data changes, generated facts, tests, config descriptions and report
output were reviewed separately; no dependency or packaging change is added.

The local development JAR retains its v0.9.8 filename: **923,287 bytes**, SHA-256
`e989afe90a67f41bf5bb6351e2a7f3ad4abe12a16fb3143f0d92fda466509e85`.
It has not been published or submitted to the Hub.

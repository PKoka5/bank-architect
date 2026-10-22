# Wiki strategy and Alch audit: weapons, staves and tools

Research date: 22 October 2026 (Europe/Paris).

## Scope and decision

The current `alch-candidates.tsv` contains **100 unique reviewed candidate IDs**,
including IDs supplied by its four elemental-staff families. Membership permits
evaluation; it does not mean that the item always belongs in Alch.

The split is **60 armour/jewellery IDs** and **40 IDs documented here**:

| Group | Count |
|---|---:|
| Ordinary weapons in the existing slash-stock policy | 9 |
| Other ordinary weapons | 4 |
| Dragon weapon variants protected by the existing special-attack guard | 9 |
| Explicit owner-approved Dragon halberd exception | 1 |
| Four standard elemental-staff families | 12 |
| Plain Battlestaff | 1 |
| Tools | 4 |
| **Total covered by this document** | **40** |

For the 60-ID cross-check: 10 helmets, 10 bodies, 6 metal leg pieces, 6 shields,
7 d'hide pieces, 15 Mystic pieces, 1 Granite legs and 5 jewellery items.

**This research does not change those 40 IDs or their existing routing policy.**
The current production pilot is restricted to seven existing d'hide body/chaps
candidates: `1135,2493,2495,2497,2499,2501,2503`. For those pieces, a progression
tier alone must not bypass full equipment-stat dominance. Green d'hide chaps
`1099` are not added to the candidate list.

The previously approved treatment of ordinary obsolete Rune/Adamant weapons is
intentionally retained. The contextual findings below are reviewed follow-up
questions, not new protections or replacement rules in this pilot.

## Sources and limits

Primary game-information sources are the OSRS Wiki pages linked below. They were
reviewed through indexed page content on the research date; direct page retrieval
was restricted by the Wiki's robots policy. **Exact Wiki revision IDs were not
captured for these observations.** No revision number or complete strategy-table
snapshot is inferred from a search result's crawl date.

The repository's exact-ID registry and reviewed Alch declarations supply the
local ID mapping. Source pages confirm capabilities and recipes; they do not
supply an automatic global replacement hierarchy.

- [Attack types](https://oldschool.runescape.wiki/w/Attack_types)
- [Rune sword](https://oldschool.runescape.wiki/w/Rune_sword)
- [Rune dagger](https://oldschool.runescape.wiki/w/Rune_dagger)
- [Abyssal whip](https://oldschool.runescape.wiki/w/Abyssal_whip)
- [Melee training](https://oldschool.runescape.wiki/w/Pay-to-play_Melee_training)
- [Rune mace](https://oldschool.runescape.wiki/w/Rune_mace)
- [Bone mace](https://oldschool.runescape.wiki/w/Bone_mace)
- [Scurrius' spine](https://oldschool.runescape.wiki/w/Scurrius%27_spine)
- [Elemental staff](https://oldschool.runescape.wiki/w/Elemental_staff)
- [Staves](https://oldschool.runescape.wiki/w/Staves)
- [Mystic staff](https://oldschool.runescape.wiki/w/Mystic_staff)
- [Fire battlestaff](https://oldschool.runescape.wiki/w/Fire_battlestaff)
- [Mystic steam staff](https://oldschool.runescape.wiki/w/Mystic_steam_staff)
- [Mystic smoke staff](https://oldschool.runescape.wiki/w/Mystic_smoke_staff)
- [Smoke Battlestaff Buff](https://oldschool.runescape.wiki/w/Update:Smoke_Battlestaff_Buff)

The existing [recommended-equipment audit](osrs-wiki-recommended-equipment-audit.md)
already treats Wiki recommendations as review evidence, not a Gear whitelist.
An item's absence from those tables does not demonstrate that it is disposable.

## Existing weapon policies: all 23 candidate IDs

### Existing ordinary slash-stock policy: 9 IDs

| ID | Item |
|---|---|
| 1213 | Rune dagger |
| 1289 | Rune sword |
| 1303 | Rune longsword |
| 1317 | Adamant 2h sword |
| 1319 | Rune 2h sword |
| 1331 | Adamant scimitar |
| 1333 | Rune scimitar |
| 1371 | Adamant battleaxe |
| 1373 | Rune battleaxe |

The existing exact successor list is Dragon scimitar `4587`, Abyssal whip `4151`,
Abyssal tentacle `12006` and Abyssal whip (or) `26482`. A recorded successor, including a
bank placeholder, can prove the existing ordinary-stock replacement policy
without requiring complete stat dominance. These successor IDs are not all
themselves Alch candidates.

This policy expresses an owner-approved general-use preference. It does **not**
claim that every successor replaces every attack option or training stance.

Reviewed follow-ups:

- Rune sword `1289` and Rune dagger `1213` have Stab options. The Whip has only
  Slash attacks, so Whip ownership alone does not prove Stab coverage.
- Rune 2h sword `1319` and Rune battleaxe `1373` also offer Crush. A Slash upgrade
  does not, by itself, prove coverage of that secondary option.
- The Whip lacks a dedicated aggressive Strength-training stance. A stronger
  general Slash weapon is not automatically an equivalent training tool.

These observations are not applied as new vetoes here. A future contextual rule
needs an explicit product decision about primary versus secondary attack roles,
then reviewed compatible replacements and bank-level regression tests. It must
not silently reverse the existing ordinary-Rune-stock policy.

### Other ordinary weapons: 4 IDs

| ID | Item | Current review boundary |
|---|---|---|
| 1247 | Rune spear | Not in the ordinary slash-stock shortcut. |
| 1347 | Rune warhammer | Not in the ordinary slash-stock shortcut. |
| 1432 | Rune mace | Not in the shortcut; a dominating owned replacement can still qualify. |
| 3202 | Rune halberd | Not covered by the explicit Dragon halberd exception. |

Their ordinary evaluation still requires the existing category, metadata,
protection and replacement evidence. Higher weighted scores are not sufficient
replacement proof.

The Rune mace has a concrete additional use: Rune mace `1432` plus Scurrius'
spine `28798` can produce Bone mace `28792`. Equipment-stat dominance does not
represent this recipe. A possible future contextual protection would preserve
the ingredient when a spine is recorded and no Bone mace is recorded. That is
**not implemented in this pilot**. The player can already assign the item manually.

If such a protection is added later, test both fresh and existing sessions:
automatic Alch session retention currently enters the builder as an override.
Depositing a new recipe trigger must not let a remembered automatic Alch choice
bypass a new contextual protection. Actual manual choices must retain priority.

### Special-attack protection: 9 Dragon variants

| IDs | Item |
|---|---|
| 1215, 1231, 5680, 5698 | Dragon dagger, including the three poison variants |
| 1249 | Dragon spear |
| 1305 | Dragon longsword |
| 1377 | Dragon battleaxe |
| 1434 | Dragon mace |
| 4587 | Dragon scimitar |

The existing bundled special-attack list protects these before generic
replacement checks. Their presence in the reviewed candidate list does not
disable that protection. Preserve this distinction when researching stronger
gear: a new primary weapon does not demonstrate that a special attack is obsolete.

Dragon scimitar `4587` already remains protected even when a Whip is recorded;
the training-stance observation does not require an additional rule in this pilot.

### Explicit owner exception: 1 ID

Ordinary Dragon halberd `3204` is explicitly approved as Alch stock when the Alch
option is enabled and its HA value is positive. This exact exception runs before
the general special-attack guard. It includes its bank placeholder under the
withdrawal fix. Manual assignments and saved physical placements still win.
Do not extend this exception to other halberds, variants or namesakes.

## Existing staff policies: all 13 candidate IDs

### Standard elemental chains: 12 IDs

| Rune supply | Basic staff | Battlestaff | Mystic staff |
|---|---:|---:|---:|
| Air | 1381 | 1397 | 1405 |
| Water | 1383 | 1395 | 1403 |
| Earth | 1385 | 1399 | 1407 |
| Fire | 1387 | 1393 | 1401 |

Each existing chain permits a higher member of the **same element** to replace a
lower member. The last recorded staff in a lone chain remains a keeper, regardless
of stock quantity. Recorded placeholders supply the same exact role evidence.

A stronger powered, ancient-casting or different-element staff is not accepted
by this chain merely because its combat score is higher. Infinite rune supply
is a capability, not an equipment-stat axis. The chain also does not establish
that the player meets every successor's wield requirements; this audit adds no
skill, inventory or equipment reads.

### Plain Battlestaff: 1 ID

Plain Battlestaff `1391` is a reviewed candidate ID, but the current classifier
assigns it to crafting materials. It is not a member of any elemental-supply
chain. Its current material placement therefore does not become an Alch decision
solely through list membership.

The Scurrius recipe also uses a plain Battlestaff to create a Bone staff. That
supports retaining its distinct material meaning; it is not a reason to import
all elemental battlestaves into the material category.

### Combination staves: follow-up reference, not candidate additions

| Rune supply | Battlestaff | Mystic staff |
|---|---:|---:|
| Lava: fire and earth | 3053 | 3054 |
| Mud: water and earth | 6562 | 6563 |
| Steam: water and fire | 11787 | 11789 |
| Smoke: air and fire | 11998 | 12000 |
| Mist: air and water | 20730 | 20733 |
| Dust: air and earth | 20736 | 20739 |

These twelve IDs are **not** additions to the reviewed candidate list. Decorative
forms are also outside this pilot. The existing 100-ID membership remains fixed.

Their dual rune supply means a single elemental staff cannot be assumed to
replace them. Smoke staves additionally have a standard-spell accuracy/damage
passive that a plain equipment-stat vector does not fully express. Any future
combination-staff rule must check retained capabilities and passives explicitly;
neither price nor a generic Magic rank proves redundancy.

This records a coverage limitation, not a demonstrated live-bank failure for
every combination staff. No automatic replacement expansion is made here.

## Existing tool policies: all 4 candidate IDs

| ID | Item | Current policy |
|---|---|---|
| 1275 | Rune pickaxe | Exact higher functional pickaxe tier required. |
| 1357 | Adamant axe | Exact higher functional axe tier required. |
| 1359 | Rune axe | Exact higher functional axe tier required. |
| 21028 | Dragon harpoon | Existing special-attack protection applies. |

The pickaxe/axe comparisons use the repository's reviewed functional-tier lists,
including recorded placeholders. Equal-tier ornaments do not constitute an
upgrade; unrelated or unsupported broken/inactive forms are not inferred from
their names. Do not use general combat scores or a boss equipment table to
replace a skilling tool's functional progression.

## Invariants and future work

- Keep automatic Alch gathering opt-in through its existing option.
- Preserve manual tags, legacy category choices and captured physical layouts.
- Preserve canonical IDs, zero placeholder quantities and physical bank slots.
- Apply the same reviewed replacement policy to Main and Ironman.
- Retain existing unknown/unreviewed stock quantity, real-ownership and value
  thresholds. This audit introduces no GE eligibility or GE-driven veto.
- Use Wiki strategies as contextual evidence, never as a global tier import or
  proof that an unlisted item has no use.
- A future multi-role rule must define which roles it covers, how requirements
  and set dependencies are handled, and how automatic session retention reacts
  to new contextual facts. Missing evidence must remain explicit.

All information is bundled or used during development review. The plugin gains
no runtime network calls, live Wiki import, telemetry, automated bank actions,
or inventory/equipment access from this work.

This document is a research record, not a claim that its deferred follow-ups have
been implemented or tested. Production validation for the seven-piece d'hide
pilot is recorded separately by the implementing agent.

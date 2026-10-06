# Main role groups and first placement evidence

Prepared on 2026-10-05 after the owner approved defining item groups inside tabs
before choosing which groups share a destination. This is original research and
a policy proposal, not an implemented Main preset.

## Subsequent owner decisions

The owner subsequently fixed these Main defaults: combined Teleports containing
all runes, teleport tablets and reviewed teleport items; Supplies containing all
food and finished potion families in **1 -> 2 -> 3 -> 4** dose order; combined
Combat with **Saturated heart (27641)**; and Farming/Herblore grouped by stage as
**grimy -> clean -> Farming seeds -> unfinished potions**, instead of Ironman
recipe rows. Personal assignments/orders remain authoritative.

These choices supersede earlier proposals below that put the heart near supplies
or leave combined Combat/Teleports undecided. The eighteen measured comparisons
remain unchanged historical evidence, not approvals or vetoes of owner choices.
See [the approved core rules](../plans/main-preset-2026-10-05.md) for current policy.

## Scope

Complete inputs remain public templates **8, 14, 26 and 55**, cached in July.
See [the baseline](main-template-baseline-2026-10-05.md) for sources and limits.
The [nine extra candidates](main-template-candidates-2026-10-05.md) have partial
summaries and are excluded from numeric support. Four different layout hashes
do not establish independent designers. The sample is biased toward late/endgame.

The 3,942 positive template positions include **21 Bank fillers** (ID 20594).
They are not 3,942 genuine bank items. No filler appears in the probes below.

## Original analytical groups

Separate **role**, **family/state**, **contextual uses** and **personal placement**.
An item can serve several activities while retaining one default home. These
eleven research groups do not require eleven tabs and are not new production
tags or replacements for existing stable tag IDs.

| Role | Relationships to preserve | Example cases from our own catalog |
| --- | --- | --- |
| Combat equipment | Style, set, weapon/accessory role and useful alternatives | Selected melee/ranged/magic torso variants; rapier review cases |
| Ready ammunition | Type, compatible use and finished state | Dragon arrow 11212; distinguish unfinished broad bolts 11876 |
| Ready supplies | Food and finished potion families/doses; reusable boosts retain their factual identity | Prayer potion 2434/139/141/143; cooked foods; Main places Saturated heart 27641 in Combat |
| Spell supplies | Rune families and rune containers | Law 563 and Nature 561; Rune pouch 12791 |
| Travel and access | Destination/access purpose and charge/state family | House tablet 8013; Ring of dueling 2552 |
| Activity equipment | Tools, functional outfit sets, containers and reusable equipment | Pickaxes/Prospector; Eye outfit, pouch and lantern |
| Production inputs | Activity/workflow, raw/processed state, ingredients and unfinished products | Ranarr herb/seed; ore/bar; unfinished potion 101 |
| Currency and reward tokens | Currency identity and activity context | Coins 995 and Platinum token 13204 |
| Clue objectives | Actual objectives, scroll/casket family and dedicated helpers | Clue scrolls; secondary clue uses remain on functional items |
| Appearance and collection | Reviewed cosmetic set and collection identity | Cosmetic sets, ornament kits and trophies |
| Quest-specific and uncertain | Reviewed quest purpose/stage where known; unresolved status | Ghostspeak amulet 552; no inferred disposal claims |

Raw `Category` labels do not establish these roles. Our reviewed overrides
identify unfinished broad bolts as `ammo-component` and Ring of dueling as
`teleport`, despite their raw labels. Usage tags preserve the Spade's
Farming/clue uses and Emerald ring's clue requirement without selecting a tab.

Independent fact checks support the functional distinction: the
[Raiments of the Eye](https://oldschool.runescape.wiki/w/Reiments_of_the_eye)
have a Runecrafting effect, the
[Prospector kit](https://oldschool.runescape.wiki/w/XP_clothing)
has a Mining experience effect, and the
[Magic boost table](https://oldschool.runescape.wiki/w/Magic_Attack)
describes Saturated heart's reusable boost. Its approved Main destination is
Combat, preserving that factual identity; it is not a potion dose. These indexed
sources do not establish current bank ownership or personal need.

Frequently Used, intended sale/alch, personal collection and current activity
are player choices. Tradeability, quantity, another weapon or a secondary
clue/quest tag does not establish them. Captures, manual assignments and Keep
current order choices must take precedence over defaults. This research does
not change existing routing or persistence.

## Reproducible comparisons

[main-role-probes.json](../../tools/research/community-templates/main-role-probes.json)
defines eighteen original selected-ID comparisons, not exhaustive role lists.
[measure-role-probes.ps1](../../tools/research/community-templates/measure-role-probes.ps1)
reads only selected normalized inputs, without broad category labels or output
coordinates. Results stay in the ignored research cache.

- Both sides must have at least one selected ID present. Missing sides do not
  vote against grouping.
- A repeated relevant ID excludes that template as ambiguous. Report
  co-presence and excluded counts separately.
- Identical layout hashes receive one observation. Author/revision clusters
  still require review.
- **Any shared tab** means at least one observed member from each side shares a
  tab. **All observed together** requires every present selected member from
  both sides to occupy one tab.

Neither measure proves adjacency, all family members/variants, item function,
usage frequency or sale intention. Denominators are eligible complete templates,
not player-population votes. None of these eighteen comparisons had a repeated
relevant ID.

Probe SHA256:
`A2FFEE7089CDB6F065364049A3CEC21362011456643DBF134407477536E50926`.
The ignored output records source-layout, registry, probe and analyzer hashes at
`tools/research/community-templates/cache/main-2026-10-05/role-probe-analysis.json`.

| Selected comparison | Any shared tab / eligible | All observed together / eligible |
| --- | ---: | ---: |
| Coins and Platinum token | 3 / 3 | 3 / 3 |
| Selected melee/ranged torso variants | 3 / 3 | 3 / 3 |
| Selected ranged/magic torso variants | 3 / 3 | 3 / 3 |
| Cooked Anglerfish and Karambwan | 4 / 4 | 4 / 4 |
| Prayer potion four-dose and observed lower doses | 2 / 2 | 2 / 2 |
| Saturated heart and selected cooked foods | 4 / 4 | 4 / 4 |
| Saturated heart and observed Prayer doses | 3 / 3 | 3 / 3 |
| Law and Nature runes | 4 / 4 | 4 / 4 |
| Law/Nature runes and house tablet | 1 / 3 | 1 / 3 |
| House and Varrock tablets | 3 / 3 | 3 / 3 |
| Ordinary/ornamented cannon parts and Law/Nature runes | 2 / 4 | 2 / 4 |
| Selected pickaxes and Prospector variants | 4 / 4 | 2 / 4 |
| Eye outfit tops and boots | 4 / 4 | 4 / 4 |
| Ordinary Colossal pouch states and Eye boots | 3 / 4 | 3 / 4 |
| Abyssal lantern states and Eye boots | 4 / 4 | 3 / 4 |
| Ranarr herb states and Ranarr seed | 2 / 3 | 2 / 3 |
| Ranarr herb states and finished Prayer potion | 0 / 3 | 0 / 3 |
| Selected pickaxes and iron ore/bar | 2 / 4 | 1 / 4 |

Mining probes include explicitly named Dragon, Crystal, Infernal and 3rd age
variants, without claiming all pickaxes or everyday usage. Different probe
scopes can change support; one pair cannot validate an entire category.
Coins separately occur once in Main in **4/4** inputs. This supports a provisional
currency home, not putting every activity token in Main.

## Current direction and placement cases

- **Currency:** start Coins/Platinum together in quick access; other reward
  currencies need activity context.
- **Combat:** approved combined home with distinct style/set groups and
  Saturated heart. Other templates can refine internal grouping.
- **Ready supplies:** approved all-food and finished-potion home, with each
  exact family ordered 1 -> 2 -> 3 -> 4. Historical lower-dose evidence is 2/2.
- **Runes/travel:** distinct functional groups share the approved Teleports
  destination. The historical 1/3 rune/house result does not change that choice.
- **Activity equipment:** preserve functional sets and associated equipment.
  Eye tops/boots support cohesion; mining and lantern probes show split variants.
- **Inputs:** approved Main Farming/Herblore stage runs: grimy, clean, Farming
  seeds and unfinished potions. Other materials/ingredient details remain open.
- **Loot:** preserve functional facts and apply explicit player intent.

The four core destinations above follow the owner's decision, not automatic
rules derived from eighteen comparisons. Physical tab order and remaining
destinations still need design work. Small banks, incomplete families and
personal corrections remain valid. Source template order must not be copied.

Concrete acceptance cases for the implementation review:

1. Ready arrows retain ammunition identity; unfinished bolts remain inputs.
2. Main finished doses retain their family in 1 -> 2 -> 3 -> 4 order; unfinished
   potions remain in the approved stage block. Preserve Ironman behavior.
3. Eye pieces retain functional set membership. Missing pieces, pouch or lantern
   must not create filler slots or a shopping requirement.
4. Wearable transport routes by reviewed travel use; a secondary clue requirement
   cannot pull every weapon/tool into clues.
5. A Spade retains its Farming/clue uses with one chosen home. Materials retain
   production use unless explicitly assigned for sale.
6. Cosmetics need reviewed set/functional facts. Known rapier, heart and camo
   classification reports remain shared-catalog prerequisites; this research
   does not claim they are fixed.
7. Unresolved items remain visible exactly once with neutral review wording.

## Verification and next step

The real-cohort run completed eighteen comparisons across four distinct layouts.
Independent read-only recomputation matched every result and the final probe
hash, without using broad category labels. Synthetic inputs verified missing
members, split groups, relevant duplicate IDs, identical-layout deduplication,
repeated selection and rejection of overlapping sides without writing a result.
PowerShell syntax passed. Production Java/resources and build configuration did
not change; a RuneLite build does not validate research denominators.

Complete prioritized inputs **222, 270, 114 and 268** when normal public import
access is available. Rerun comparisons and review source clusters to refine
remaining details in [the Main plan](../plans/main-preset-2026-10-05.md); the
approved core defaults no longer depend on obtaining a template majority.
Preserve the below-200,000 Hub requirement when production implementation begins.

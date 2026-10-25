# Reviewed general Melee candidates

Developer research, 24 October 2026. These files do not load in the plugin and
do not change placement or Alch policy. The implementation owner reviews each
group before transferring a compact decision to production data.

## Evidence and limits

`helm-review.tsv` records the core helm review. `candidate-review.tsv` records
the remaining nine slots' candidate inventory and unresolved decisions. A row can group exact cosmetic or
functional-state IDs, but it must not infer an adjacent ID is another wearable
variant: the local registry also contains notes, placeholders, dummies, beta and
Last Man Standing definitions. In particular Blood Moon helm `29028` and `29047`
are new and used, whereas `29029` and `29030` are not additional usable states.

The existing exact-ID progression research was captured on 17 July 2026 and has
revision-pinned URLs. This research reuses its identity facts, not its tiers as
an all-purpose ranking. The local Wiki infobox snapshot under
`../wiki-recommended-equipment/cache/infobox-item.bucket.json` distinguishes
canonical new/used/broken/charged IDs. It is an ignored developer cache and no
source revision is inferred from the snapshot. Live Wiki item pages were
checked through web search on 24 October 2026; direct page fetches were blocked
by robots.txt. When no revision number was returned, `checked` is an observation
date, not a revision identifier.

## Selection contract

- The primary column is general offensive Melee, not a target-specific DPS set.
- A specialist function can coexist with general candidacy. Oathplate and
  Inquisitor remain eligible for their real raw offensive bonuses; being
  slash/crush-focused alone must not make them lose to Neitiznot. Conditional
  set effects are not activated by their presence in the candidate list.
- Within one comparable group use Strength, best melee accuracy, Prayer and
  defence, with stable exact ID last. A reviewed rank is a complete-group
  fallback if relevant stats are missing; never switch comparison mode pairwise.
- Neck policy explicitly prefers the all-round Fury family over the budget
  strength amulet, despite the latter's greater Strength bonus. That is a
  product choice, not a claim that Fury always deals more damage.
- Specialist role does not mean rubbish or Alch. Keep the set/weapon together
  if it is not needed as the primary fallback. Never enable task, monster,
  weapon-type, set or charge effects merely because an item is owned.
- Broken, damaged or uncharged nonfunctional items are recorded but must not
  defeat a usable candidate. A bank placeholder of a usable ID still counts.
- This is a review list, not a complete list of every wearable cosmetic. Missing
  IDs remain visible through existing classification; absence is never Alch
  evidence. Beta/LMS/unobtainable variants are excluded deliberately.

## Critical freshness issue

The general training table still shows old Inquisitor helm stats (+4 Strength,
+8 crush, negative stab/slash), while the current item/set pages and the
22 July 2026 update show +6 Strength, +10 crush and zero stab/slash. Therefore
do not copy its stale numeric table into fixtures or rank calculators.

- [Inquisitor's great helm](https://oldschool.runescape.wiki/w/Inquisitor%27s_great_helm)
- [Gear changes](https://oldschool.runescape.wiki/w/Update:Summer_Sweep-Up_Gear_%26_PvM_Changes)
- [Training table](https://oldschool.runescape.wiki/w/Pay-to-play_Melee_training)

The user-linked [Melee progression guide](https://oldschool.runescape.wiki/w/Guide:Melee_Gear_Progression)
could not be fetched in this run. The live item pages and existing pinned
research are the evidence actually used, rather than an unverified claim that
the complete guide was read.

## Implemented development checkpoint

The compact runtime table now has 624 exact IDs across all ten positions. The
seven-slot audit and its held/excluded entries are documented in
[ARMOUR-READINESS.md](ARMOUR-READINESS.md). `weapon-offhand-decisions.tsv` contains
the owner's general one-handed navigation policy and explicit fallback states.
It supersedes provisional weapon decisions in `candidate-review.tsv`.

Rebuild the compact table deliberately after reviewing a source change:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tools/research/melee-bis/compile-primary-candidates.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File tools/research/melee-bis/compile-primary-candidates.ps1 -Check
```

The compiler makes no network call and does not contain bank-dependent logic.
It skips held armour decisions and the explicitly excluded temporary item.
Failed [Emir's Arena](https://oldschool.runescape.wiki/w/Emir%27s_Arena) ornament
rewards `26708/26710` are excluded even though normal cache pages exist.
Permanent Frozen/Volcanic whips, ornamental Rune scimitars and normal/locked
Ghommal's Avernic defenders retain their reviewed base family's preference.
The `-Check` form never rewrites stale output. Runtime loading checks every
format field, exact-ID uniqueness, all ten slot declarations and functional
state constraints before publishing a candidate map.

The primary weapon preference is an original layout policy, refined on
25 October 2026. Weapons compare navigation band before general/fallback kind;
armour and accessories still compare kind first. In descending preference:

- top fast one-handers (40);
- Fang, an explicit general high-defence/stab navigation choice (35);
- mature Abyssal dagger/Whip/Tentacle one-handers (30);
- reviewed advanced two-handed backups (24): Soulreaper, charged Obligator,
  charged Scythe, Noxious halberd and Abyssal bludgeon;
- earlier one-handers (10-22), including Zombie axe, Hasta, Lance and Cudgel;
- the remaining conditional/special-attack fallbacks and weaker or unconfirmed
  uncharged states (0).

This removes the blanket budget-one-hander preference while retaining the
owner's mature general one-handed weapon plus defender setup. It does not rank
boss DPS: Fang has five-tick attacks and target-dependent stab accuracy effects;
Whip and dagger have four-tick attacks and different attack styles. Advanced
two-handers use their reviewed fallback ranks, without comparing raw Strength
across their different speeds, size effects, stacks or styles. Uncharged Scythe
and unconfirmed uncharged Obligator retain their previous fallback band.
See [the reviewed refinement](../../../docs/testing/melee-weapons-2026-10-25.md)
for sources, explicit expectations and verification.

Real weapon stats compare only within one band with equal, positive,
verified attack speed. Missing, zero or inconsistent speed uses that band's
whole reviewed order. Specialist fallback does not apply target, stack, set or
special-attack effects. A two-handed or unreviewed primary weapon omits the
primary Melee shield; the physical offhand is retained and remains available.

Only Avernic/Dragon defenders receive the explicit accuracy preference over
Dragonfire shield. Lower defenders compare actual raw bonuses in the ordinary
offhand group, so Bronze defender cannot win just because it is a defender.

The July [released gear update](https://oldschool.runescape.wiki/w/Update:Summer_Sweep-Up_Gear_%26_PvM_Changes)
confirms current Mace/Rapier/Saeldor bonuses and that Soulreaper stacks no longer
drain HP. The provisional inventory's HP-trade-off note is stale. Current
[Emberlight](https://oldschool.runescape.wiki/w/Emberlight) has only 13 raw
Strength; its demonbane effect is not a general raw-stat upgrade. Current
[Obligator](https://oldschool.runescape.wiki/w/Obligator_(uncharged)) has distinct
3/5-tick styles, so the table deliberately has no single comparison speed.

Ranged/Magic core candidate construction is unchanged. A reviewed Melee row
does not seize their body/legs. Exact reviewed items which otherwise name-resolve
to `OTHER` can enter the Melee pool without stats; actual metadata still decides
their equipment slot. Shared accessories retain single physical ownership and
Melee-first reservation, so a changed Melee winner may release an accessory to
another style. Tests distinguish those intended effects from comparator changes.

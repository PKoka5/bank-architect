# Candidate review status

24 October 2026. Only developer research files were added. No existing runtime
catalogue, production source, test, cache or baseline was edited by this research.

## Identity validation

The two TSVs have 145 groups / 334 unique exact IDs, covering head, body, legs,
cape, neck, hands, feet, ring, weapon and offhand. They have no missing registry
IDs and no repeated exact IDs within either file. All except the two newly
released Fractured Archive IDs resolve in the cached canonical Wiki infobox
snapshot after excluding beta, LMS, unobtainable and Deadman pages. The new
Obligator/Rondache IDs instead have pinned source evidence already recorded in
`docs/testing/fractured-archive-items-2026-10-23.md`.

Infobox cache SHA-256:
`964ebcea0d2e313e8bbddbb18154bc69d24ad5b228820d767ed6c28527144e44`.

This establishes identity and state, not every combat stat or every preference
band. `fallback_rank` values are research proposals. Metal ladders, distinct
base/imbued rings and mixed special-attack groups still need exact per-item
ranks or deliberate exclusion; one grouped row's number is not an adequate
runtime comparator for those families.

## Helm proof vectors

| Item | Stab/slash/crush attack | Strength | Prayer | Stab/slash/crush/magic/ranged defence |
| --- | --- | ---: | ---: | --- |
| Helm of Neitiznot 10828 | 0 / 0 / 0 | 3 | 3 | 31 / 29 / 34 / 3 / 30 |
| Guthan's helm 4724, 4904–4907 | 0 / 0 / 0 | 0 | 0 | 55 / 58 / 54 / -1 / 62 |
| Neitiznot faceguard 24271 | 0 / 0 / 0 | 6 | 3 | 36 / 34 / 38 / 3 / 34 |
| Torva full helm 26382 | 0 / 0 / 0 | 8 | 1 | 59 / 60 / 62 / -2 / 57 |
| Oathplate helm 30750, 30777 | 0 / 10 / 0 | 6 | 0 | 50 / 72 / 45 / 0 / 50 |
| Inquisitor's great helm 24419 | 0 / 0 / 10 | 6 | 1 | 19 / 10 / 21 / 0 / 12 |

These are item-page/set-page facts checked by live Wiki search on the review
date, not values reconstructed from the new selector. Links are in
`helm-review.tsv`. Search returned no current revision number for these pages;
none is invented. Undamaged and positive-durability Barrows variants have the
same combat stats; their broken forms must not inherit usable candidacy.

The root agreed that a specialist function can coexist with general candidacy.
Accordingly Oathplate/Inquisitor raw Strength upgrades cannot lose to Neitiznot
just because they also have slash/crush roles. Under the selected tuple:

1. Torva wins over all these alternatives by Strength.
2. Inquisitor wins over Oathplate by Prayer after Strength and best melee
   attack tie; this is a deterministic all-round layout decision, not a claim
   that crush is better than slash.
3. Oathplate beats faceguard by attack after Strength ties; the faceguard's
   greater Prayer does not precede accuracy in the chosen tuple.
4. Neitiznot beats Guthan by Strength, although Guthan has more defence.
5. Only Guthan remains a useful fallback; no set-healing effect is assumed.

## Decisions still needed for the other nine slots

- **Offhand:** defender vs Dragonfire/Obsidian shield needs an explicit band.
  Strength alone is insufficient because attack penalties and defence/prayer
  trade-offs differ. Two-handed primary compatibility must be enforced.
- **Weapons:** no generic STR ranking is proposed. Fast one-handers, slower
  one-handers, two-handers, target size, stacks, charge state and monster/task
  effects need reviewed roles. Scythe uncharged remains usable with weaker stats;
  Serp uncharged cannot be re-equipped at all. Do not conflate those states.
- **Neck:** the owner's approved all-round Fury-over-strength-amulet choice is
  a separate preference band, not a reversed factual Strength stat.
- **Mixed-style pieces:** Mixed hide and Blue/Eclipse Moon can have usable raw
  Melee bonuses but current Ranged/Magic reservations must remain unchanged for
  the head pilot. Broader allocation needs separately reviewed fixtures.
- **Cosmetic expansion:** not all metal/god/ornamented items, Slayer recolours,
  defensive shields or starter RFD variants are expanded here. The inventory
  explicitly records gaps. Their absence cannot make them automatic Alch.
- **Current stats:** old training tables contain stale Inquisitor values. The
  Soulreaper base search snippet was also stale (+121 Strength) while its current
  ornament/item table showed +125. Verify current base before transferring a
  weapon fixture; ornament equality alone is not a reliable timestamp.

This work provides the helm slice's independent source data and a broader review
inventory. It does not declare the full ten-slot Melee rollout complete.

## Permanent-game head audit

The production `organize/melee-primary-helms.tsv` was compared back to the
research table after implementation: exactly the same 77 IDs, no additional
aliases or cache dummies. No temporary-only ID was found in that set. Some are
intentionally inactive but are real bankable repair/charge states.

Zombie helmet `30321` is a permanent item released with the 6 November 2024
Curse of Arrav update. Its current [item page](https://oldschool.runescape.wiki/w/Zombie_helmet)
describes repairing the Armoured zombie drop from Zemouregal's Fort; the
[broken item](https://oldschool.runescape.wiki/w/Broken_zombie_helmet) is `30324`.
It is not an Echo/League-only reward. By contrast the separate hands inventory
originally included Sunlit bracers `30386`; live removal history identified that
as temporary, so it is now explicitly excluded in the research data.

Known non-live or non-bank forms intentionally omitted include old Torva
appearance `30302`, Blood Moon LMS `29848`, Inquisitor LMS `27195`, Guthan LMS
`23638`, Oathplate LMS `33462`, note/placeholder neighbours and interface/dummy
definitions. Ordinary quest/cosmetic reward armour can still be real combat
equipment; neither the word "quest" nor the word "cosmetic" alone is an
exclusion test.

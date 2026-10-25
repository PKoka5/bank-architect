# Seven-slot implementation inventory

24 October 2026. No production source, tests, build or runtime resources were
changed by this research. The head pilot is audited separately in
`REVIEW-STATUS.md`; weapons and offhands are outside this table.

`armour-decisions.tsv` expands to **393 explicit per-ID decisions**:

| Slot | Decisions |
| --- | ---: |
| Body | 117 |
| Legs | 120 |
| Cape | 26 |
| Neck | 34 |
| Hands | 22 |
| Feet | 35 |
| Ring | 39 |

There are 142 general candidates (`kind=2`), 223 fallbacks (`kind=1`), 27
inactive repair states (`kind=0`) and one explicitly excluded temporary item
(`kind=-1`). **Do not transfer kind -1 to the current production loader.** It is
an audit record, not a new valid runtime kind. Every exact ID resolves in both
the repository registry and the versioned Wiki infobox cache. No adjacent-ID,
note, placeholder, alias or arbitrary name fallback expands this table.

## Meaning and ready subset

`kind` follows the pilot's general/fallback/inactive distinction. `band` is an
additional proposed preference within a kind; it matters only for neck. The
fallback rank is per exact ID and only applies when the relevant comparison
group lacks complete stats. Different cosmetic/charge states with identical
raw combat bonuses intentionally share a rank, with exact ID as final stable
tie-break. Base and imbued rings, metal tiers/pieces, Ardougne cloak tiers and
inactive forms are separate decisions rather than a shared group rank.

**387 rows are ready for implementation fixtures.** That is source/decision
readiness, not passing production tests. It includes the inactive/excluded
records, so it is not a count of 387 usable candidates. Six rows are explicitly
held in the `readiness` column:

- `24037`, `24040`, `24043`, `24046`: Dragonstone body, legs, boots and gauntlets
  are genuine permanent items, but complete raw-stat tie checks against their
  ordinary relatives remain open. Do not silently equate every Dragonstone
  piece with the same-tier metal item.
- `11128`, `23240`: Berserker necklace and ornament. The weapon-conditional
  damage, negative accuracy and +7 raw Strength require a neck/weapon preference
  decision. The current fallback proposal is visible, not a settled claim that
  it should always lose to a Glory or always win by Strength.

The developer generator is repeatable:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tools/research/melee-bis/expand-armour-review.ps1
```

It reads only the reviewed candidate inventory, local registry and cached Wiki
identity data, rejects duplicate/unresolved IDs and writes this developer TSV.
It makes no network call or runtime edit. Repeated runs produced the same
SHA-256 `38a233ef3573c297c701aedfaaf934e9378d5747deaf62c57271ce0669c408df`.

## Important resolved distinctions

- **Neck:** proposed premium band 20 contains Rancour, Torture and Fury family;
  budget band 10 contains Strength, Glory, Power and Amulet of the Damned. Thus
  Fury defeats the strength amulet by the approved all-round band, without
  falsifying +8 versus +10 Strength. Within each band compare the actual stats.
  The current primitive pilot's kind/rank pair needs a deliberate band extension
  before this is transferred; do not encode the trade-off as made-up Strength.
- **Feet:** only Avernic `31091/31094/31095/31097` have the primordial upgrade
  and +6 Strength. Base and non-pr forms `31088/31092/31093/31096` have +4, so
  Primordial `13239` (+5) beats those under the general offensive tuple. Aranea
  has +4 and +1 Prayer, while Dragon boots have +4 and zero Prayer. These are
  deliberate test cases, not a name-based Avernic-always-wins shortcut.
- **Hands:** Granite gloves have +7 Strength/+9 crush, Regen has +7/+8 general
  accuracy and Adamant gloves have +7/+7. Rune gloves (+8) and Dragon gloves
  (+9) remain above those. Combat bracelet's +7 attack beats Mithril gloves'
  +6 after both tie at +6 Strength.
- **Rings:** Brimstone is +4 Strength and +4 accuracy, not +5 accuracy. Bellator
  has +6 Strength/+20 slash. Every permanent imbue provider is explicitly
  represented; base Warrior/Tyrannical/Treasonous rings are not equal to imbued.
- **Charges:** Ring of shadows `28327/28329` has the same raw combat stats in
  charged and uncharged forms; charges enable teleports. Echo boots remain
  usable with their raw stats when recoil charges run out. Blood Fury becomes
  an ordinary Fury at depletion rather than retaining a phantom healing bonus.
  These differ from uncharged Serp, which cannot be re-equipped.
- **Variants:** includes normal/locked Fire and Infernal capes, their max forms,
  Fighter torso/ornaments/locked states, all listed broken/mangled repair forms,
  Dragon armour gold/crimson ornaments, Dragon boots gold/crimson, Obsidian cape
  recolour, plain/gold/trim/heraldic metal armour, god Rune pieces and all eight
  Avernic treads forms. This is still not every wearable item in the game.
- **Temporary exclusions:** Sunlit bracers `30386` are League-only and were
  removed after Grid Master on 12 November 2025. Wrapped RFD gloves are Arena
  gameplay items, so `27110/27111/27112` are deliberately absent. A canonical
  Wiki page plus numeric ID does not alone establish a permanent bank item.

## Remaining coverage and policy limits

- Steel/quest/skilling gauntlets, Karamja and Shayzien reward gloves, skillcapes,
  broad cosmetic capes, niche prayer necklaces, Fremennik sea boots and some
  defensive/cosmetic sets are not fully expanded. Existing classified Combat
  items must remain visible; absence is neither evidence of non-gear nor Alch.
- Blue/Eclipse Moon and Mixed hide raw Melee bonuses are recorded with state
  distinctions, but the existing Melee/Ranged/Magic candidate allocation stays
  authoritative. This table does not authorize stealing another style's gear.
- Static fallback ranks are controlled layout preferences when facts are
  incomplete, not a DPS model or a claim that every defensive sidegrade is
  totally ordered by real target performance. Complete runtime facts use the
  agreed tuple. Remaining full-stat fixture work can still uncover a rank
  adjustment; it must be reviewed explicitly.
- Some source rows use prior pinned research or cached state facts; the
  `evidence` field distinguishes these from a live item-table observation. The
  checked date is not an invented Wiki revision number.
- No new candidate, tier, primary absence or source table rank is disposal
  evidence. Personal categories, Fixed/Flexible Alch decisions and accepted
  blueprints keep their existing precedence.

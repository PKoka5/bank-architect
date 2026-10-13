# Mixed midgame and endgame Combat ownership

## Scope

The user asked whether the improved Combat placement also works for a very full tab mixing endgame and midgame equipment. The existing endgame report contained 102 source entries, including 94 Combat items and eight cannon components. Its 238-entry stress fixture primarily added all 144 Barrows condition IDs; neither case exercised the new ring pool with several older rings.

`MixedProgressionCombatTest` combines the normalized 85-item reported bank with the 102-entry endgame fixture. Five IDs overlap, producing **182 actual distinct source identities**. Frozen endgame vectors win on those overlapping identities. Both Main and Ironman use the public preview builder, the production catalog and default options, without classification overrides. `ItemValueSource.NONE` keeps this placement test independent of changing prices; live monetary Alch thresholds are outside this test. No armour or weapon is forced into Combat by a test override.

A second ownership case adds actual Venator, Magus and Bellator rings, producing **185 source entries and 11 rings**. Their equipment vectors are frozen from the public RuneLite item-stats snapshot captured on 2026-10-12, SHA256 `99cdcb7514b2cf48655094d94e502d95d4ac708e328b25579d21511c4db3f1b8`. Venator has +10 Ranged attack and +2 Ranged strength; Magus has +15 Magic attack and +2% Magic damage (20 tenths); Bellator has +20 slash attack and +6 Melee strength. No runtime networking is used.

## Independently specified expectations

- Primary Torva, Masori and Ancestral columns retain their explicit targets, including capes, boots, necklaces, weapons, shields and gloves.
- Max Avernic treads go to Melee. A separate ordinary pair is available to Ranged and its frozen +15 attack/+2 strength beats the owned Pegasians' +12/+1. Magic retains Eternal boots. Shared physical identities are not duplicated.
- In the 182-entry case, Ultor stays at cell 72 and seven extra rings occupy cells 73 through 79.
- In the 185-entry case, Ultor, Venator and Magus complete the three columns at cells 72, 73 and 74. The eight extra rings occupy the complete row at cells 80 through 87, directly below them.
- Every retained member of the 14 independently listed armour families must stay in canonical **vertical** order, with an eight-cell step. A compact run or rectangle does not satisfy this sufficiently populated case. Families include Oathplate, Inquisitor, Justiciar, Bandos, Dharok, Verac, Armadyl, Crystal, Virtus, Sunfire, Mixed hide, Proselyte, Monk robes and Mystic robes.
- Shuffled input, withdrawn primary gear and ring placeholders preserve the entire target order and item destinations. Every quantity, physical entry and placeholder survives exactly once.

The expected primary targets and ring cells are authored explicitly. Test expectations do not call the production selection or packing helpers. Casting staves use independently specified Magic colors in the report, rather than deriving their display color from their larger raw Melee bonuses.

## Results

Java 11 offline focused run: **26 tests, zero failures, errors or skips**. This includes the six new mixed-ownership tests, both presets' existing endgame quality checks and the all-Barrows-condition checks. No production code changed in this follow-up.

| Ownership | Ironman | Main |
| --- | --- | --- |
| 182 distinct source items | 174 Combat, 8 Tools | 173 Combat, 1 Teleports, 8 Tools |
| 185 with all three primary rings | 177 Combat, 8 Tools | 176 Combat, 1 Teleports, 8 Tools |

Main retains its existing Lunar staff teleport destination. No item is routed to Alch in these two no-price fixtures; this does not predict monetary Alch routing in a live bank. Reported single-preview timings were about 9-10 milliseconds, not a representative performance benchmark.

Actual generated and visually inspected Ironman reports:

- `build/reports/combat-quality/IRONMAN-mixed-midgame-endgame-182.png`
- `build/reports/combat-quality/IRONMAN-mixed-complete-rings-185.png`

Main equivalents, HTML grids and sorted destination TSVs are in the same directory. Rendering performs conservation checks; the independent JUnit assertions additionally enforce the exact columns, vertical armour, rings and stable withdrawals.

## Initial organization limitation, resolved by the follow-up

The large seven-item special-attack weapon block does not fit in the five free cells to the right of the three long primary columns. It stays together at cells 80-86 in the 182-entry case and 88-94 in the complete-ring case, after the ring area. Ordinary Melee weapons are much earlier. Ranged bow alternatives likewise appear in a separate later block. Functional families remain intact, but all weapons of a style do not form one physical region. This is the next visual organization improvement; passing these tests does not certify an ideal layout for every possible bank.

The approved [weapon-group follow-up](weapon-groups-and-item-fixes-2026-10-13.md) resolves this long-block limitation by offering a row-flow shape beside the primary columns. Specials now occupy 27-31 and 35-36; large bow alternatives occupy 43-47 and 51-53. Current render files reflect that follow-up, and the corresponding exact targets are independently asserted.

These are storage-placement assertions. They do not validate boss-specific loadouts, equipping all items in a column, player equipment requirements, every game item or future equipment stats. Existing sparse and many-condition cases may intentionally use compact family geometry.

Only a development test and this verification note were added. The plugin runtime, production resources, dependencies, JAR and the preceding main-Java estimate of 197799 remain unchanged. Test/doc changes are outside that estimator's main-Java scope and must be inspected separately before a later Hub submission. Nothing was committed, pushed or published.

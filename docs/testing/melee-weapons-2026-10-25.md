# Melee weapon preference refinement

25 October 2026. Local development above published v0.9.8 / `675ccbd`.
No commit, push, release or Plugin Hub update is authorized.
The owner's successful bank test covered the preceding layout. This refinement
has automated and visual review; it still needs a fresh in-game observation.

## Problem and resulting choices

The initial candidate selector put every reviewed general one-handed weapon
ahead of every specialist fallback. Even a Bronze scimitar could displace an
owned charged Scythe. Separate weapon bands also put Fang below Whip, Tentacle
and Abyssal dagger without an explicit reviewed reason for that placement.

Only weapon selection now compares the reviewed navigation band before the
general/fallback kind. Armour and accessory comparisons are unchanged.
The explicit order, from higher to lower preference, is:

| Preference | Reviewed weapons |
| --- | --- |
| 40 | Inquisitor's mace, Ghrazi rapier, Blade of saeldor |
| 35 | Osmumten's fang, normal and ornamented |
| 30 | Abyssal tentacle, Whip and Abyssal dagger families |
| 24 | Soulreaper, charged Obligator, charged Scythe, Noxious halberd, Abyssal bludgeon |
| 10-22 | Earlier one-handers, including Zombie axe, Hasta, Lance, Cudgel and Rune/Dragon progression |
| 0 | Remaining special-attack/conditional fallbacks and weaker or unconfirmed uncharged states |

The mature general one-handed weapon plus defender remains the preferred
layout. An advanced two-handed backup can now replace earlier one-handers.
Examples, with every displaced item retained:

- Bronze/Rune/Dragon scimitar plus charged Scythe: Scythe is primary; no
  simultaneously wearable Melee defender is proposed.
- Cudgel/Zombie axe/Hasta/Lance plus a reviewed advanced two-hander: the latter
  is primary. Calling these one-handers established would have preserved the
  original blanket-preference problem at a slightly later progression stage.
- Whip plus Scythe: Whip and the owned defender remain primary.
- Fang plus Whip/Tentacle/Abyssal dagger: Fang is primary by the stated general
  navigation policy. Top fast one-handers still take precedence over Fang.
- Uncharged Scythe and unconfirmed uncharged Obligator retain their previous
  specialist fallback group; charge-state facts are not inferred from cosmetics.
- Godswords, maul, claws, Barrows weapons and other conditional/special-attack
  alternatives do not inherit the advanced group merely for being two-handed.

These are layout choices, not universal DPS comparisons. No target, player
levels, attack style, active stacks, enemy size or special attack is assumed.
Stats still compare only within the same reviewed kind/band and compatible,
verified positive attack speed. Missing/inconsistent stats choose one complete
group fallback mode. Advanced two-handed families stay in reviewed-rank mode;
their different mechanics are never reduced to a raw Strength contest.

## Evidence and limits

Wiki search results checked on 24 October 2026, with existing exact-ID and
independent frozen item facts. Direct page fetches were restricted; no unseen
page revision is claimed. The live sources actually inspected support these
functional distinctions rather than the policy's numeric band labels:

- [Fang](https://oldschool.runescape.wiki/w/Osmumten%27s_fang): five-tick attacks;
  additional accuracy checks apply to Stab and differ inside Tombs of Amascut.
  Its effects need target context, so its position here is an explicit choice.
- [Whip](https://oldschool.runescape.wiki/w/Abyssal_whip) and
  [Abyssal dagger](https://oldschool.runescape.wiki/w/Abyssal_dagger_%28p%2B%2B%29):
  four-tick attacks with different attack types and training options. Their
  shared speed permits the existing within-group raw bonus comparison but
  does not prove universal dominance against every target.
- [Soulreaper](https://oldschool.runescape.wiki/w/Soulreaper_axe) and the
  [July gear update](https://oldschool.runescape.wiki/w/Update%3ASummer_Sweep-Up_Gear_%26_PvM_Changes):
  strong two-handed normal attacks; active stacks are not assumed.
- [Scythe](https://oldschool.runescape.wiki/w/Scythe_of_Vitur): extra hits depend
  on target size, and uncharged raw bonuses differ. Only charged usable IDs
  receive the advanced backup preference.
- [Noxious halberd](https://oldschool.runescape.wiki/w/Noxious_halberd) and
  [Bludgeon](https://oldschool.runescape.wiki/w/Abyssal_bludgeon): usable normal
  attack weapons with different speeds and styles, not merely special attacks.
- [Obligator](https://oldschool.runescape.wiki/w/Obligator) and the
  [raid weapon explanation](https://oldschool.runescape.wiki/w/Update%3AThe_Fractured_Archive_-_Everything_You_Need_To_Know):
  the reviewed charged raid weapon receives an explicit advanced role, but its
  three/five-tick styles keep the existing unknown-single-speed sentinel.
  Uncharged effects are insufficiently confirmed and receive no new preference.

## Implementation and retained priorities

- `MeleePrimarySelector.java`: swaps the first two comparison keys only for
  weapon slot 8. Completeness groups still use the original kind plus band.
  Placement explanations describe the advanced backup or one-handed layout
  preference and say that damage varies by target.
- `weapon-offhand-decisions.tsv`: six group decisions change (Fang plus five
  advanced families). The explicit generator reproduces the runtime table:
  still **307 groups / 624 exact IDs**, with no broad name inference.
- `AcceptedBlueprint.RULES` advances from 2 to 3. Older accepted snapshots
  remain usable and retain their saved targets. Review exposes the fresh
  proposal without automatically replacing a saved bank layout.
- Fixed/Flexible Alch, disposal scores, exact classification facts, personal
  tags, saved item orders and the Ranged/Magic comparators are unchanged.

## Verification

**2,322 tests pass**, with zero failures, errors or skips across 201 suites.
The new `MeleeWeaponPreferenceTest` contains 14 cases, including:

- explicit early/intermediate versus advanced pairs, both Fang appearances,
  retained top fast weapon preference and charged/uncharged distinctions;
- the complete budget weapon progression, all permutations of a three-role
  example and shuffled mixed-family ordering;
- missing stats across/in the same group, unrelated armour isolation,
  placeholders, quantities and complete physical item conservation;
- defender suppression for a two-handed choice and restoration for a mature
  one-handed replacement, with the unused shield/weapon retained.

Three existing midgame fixtures consciously replace the expected primary Whip
with Fang. The remainder-region expectation now includes Whip and Dragon
scimitar together, while the remaining dagger stays in the Stab group.
All previous conservation, geometry, reservation, saved/accepted-order and
other-style checks remain. No frozen factual equipment vector changed.

The full offline build succeeds in 58 seconds. All **1,950** scenario banks
complete (150 standard, 1,800 multi-seed). All four normalized report files are
byte-identical to the saved pre-refinement reports, and the existing baseline
hashes pass without changes. This does not imply every possible bank was
sampled; the new targeted tests cover the otherwise sparse weapon combinations.

The compiler emits an unchecked/unsafe operations note for
`IronmanBankArchitectPanel.java`, which this refinement does not edit. It is a
nonfatal compiler note; no test or build failure occurred.

Logs:

- `build/tmp/melee-weapon-tests-2026-10-24.log` (initial successful unit run);
- `build/tmp/melee-weapon-build-2026-10-25.log` (full build and simulation gate);
- `build/tmp/melee-weapon-size-2026-10-25.log` (local estimator).

The explicit candidate generator's `-Check` passes. Visual inspection of
`build/reports/combat-quality/IRONMAN-reported-midgame.png` confirms Fang in the
complete vertical Melee column, Whip/Dragon scimitar together to its right and
the retained ring cluster. Main and endgame expectations pass in their fixtures.

## Budget and artifact

Highest local main-Java estimate: **193,238**, giving **6,762** estimated tokens
of margin below 200,000. The four estimates are
189,938 / 193,006 / 190,163 / 193,238, still calibrated to the maintainer's
200,414 at `def1e856e101ff0e57dd96adccab2cf1d886b076`.
These are not the official Hub count. Resource changes, tests, development
generator input and documentation were inspected separately; no packaging or
runtime dependency change was added in this refinement.

The local development artifact still has the v0.9.8 filename. It contains
262 entries / 920,203 bytes, SHA-256:
`72bbfe0b6c585ab89b83c83766f3b97ce9fb57206b433a040d9f9e6fb735eec0`.
The compiled registry and candidate table are present. The four development-only
classification derivation helpers, RegistryCompiler and five raw derivation
input resources remain absent. This artifact has not been published.

## Follow-up: dropdown compiler warning removed

Later on 25 October, the owner authorized cleaning up the compiler warning.
Both `nameTheClosedChooser` overloads in `IronmanBankArchitectPanel.java` now
preserve their dropdown's type parameter. The renderer is obtained as
`ListCellRenderer<? super T>` without an unchecked cast. No warning suppression
or persistent compiler-option change was added.

Compilation with `-Xlint:unchecked` succeeds without the earlier warning.
All **76 tests across five selected suites** pass: sidebar rendering/settings,
Main preset, header actions and comparison/accepted-blueprint lifecycle.
The existing closed/open dropdown-label behavior test remains active. No new
test that mirrors this type-only implementation was added.
Log: `build/tmp/panel-warning-fixed-2026-10-25.log`.

The updated local JAR builds successfully, 920,222 bytes, SHA-256
`772afb8132a457c1a594f10a3bbef6c3720507c6beb6a2ad4709a182f72ba44a`.
This supersedes the artifact above; nothing was published.
The updated highest estimate is **193,242**, with **6,758** estimated tokens
of margin (other estimates: 189,942 / 193,010 / 190,167). This remains a local
estimate, not an official Hub count. Log:
`build/tmp/panel-warning-fixed-size-2026-10-25.log`.

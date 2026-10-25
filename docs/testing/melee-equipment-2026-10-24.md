# Reviewed Melee equipment checkpoint

24 October 2026. Local development above published `675ccbd` / v0.9.8.
No commit, push, branch change, release or Plugin Hub update is authorized.
This records the original equipment checkpoint. The two weapon policy points
below were subsequently addressed in
`docs/testing/melee-weapons-2026-10-25.md`; its results supersede the weapon
choices, current rule number, final build, token estimate and artifact below.
The owner has tested the current layout in their own bank and reports that it
looks better and good for that bank, while explicitly noting the small sample.
This is acceptance of that observed layout, not universal Melee sign-off.

## Owner test and remaining review

On 24 October 2026 the owner reported: "heb het getest met mijn bank. ziet er
beter uit. natuurlijk heb ik maar een kleine sample size maar voor mij ziet het
er goed uit". The message supplies no new export, item inventory or comparison
case; do not infer additional coverage from it. No production change followed
this feedback.

The early, midgame, mixed and endgame fixtures below provide complementary
automated evidence. Passing tests verify their stated expectations; they do not
settle the following policy questions already identified in
`docs/plans/melee-slot-order-review.md`:

- General one-handed candidates always precede specialist fallbacks, so even
  a Bronze scimitar can precede an owned Scythe/Soulreaper/Obligator. The
  one-handed preference needs a more appropriate boundary.
- Weapon preference bands place Abyssal dagger/Whip/Tentacle above Fang without
  comparing stats across those bands. This remains an explicit navigation
  policy to review, not a verified universal damage ranking.

These points were open at this checkpoint and prompted the next refinement.
The owner's reported in-game test predates that refinement and does not certify
its changed weapon choices. No release is authorized.

## Implemented behavior

The Melee primary column has reviewed exact candidates for head, body, legs,
cape, neck, gloves, boots, ring, weapon and offhand. Its purpose is a general
offensive setup from owned bank items and placeholders, not universal boss DPS.

- Reviewed general candidates precede tank/conditional fallbacks. Raw Strength,
  best melee attack, Prayer, defence and stable ID compare compatible armour.
  Guthan no longer defeats Neitiznot through its old tier/defence score.
- Per-slot/kind/preference-band completeness determines one comparison mode for
  the whole group. Missing relevant stats use the reviewed order; there is no
  pairwise numeric/rank switch or invented zero vector.
- The owner chose a general one-handed weapon with defender, with Scythe and
  other specialists separately retained. Curated weapon bands are navigation
  choices. Live weapon stats compare only within an equal, positive, verified
  attack-speed group. Missing/zero/inconsistent speed uses the whole group rank.
  Specialist fallback does not apply target, stack, set or special effects.
- Fury/Torture/Rancour receive the explicit all-round neck preference above
  the strength-only budget amulet. Only Avernic/Dragon defenders receive the
  explicit accuracy preference over Dragonfire shield; lower defenders do not.
- A two-handed or unreviewed-handedness primary omits the Melee offhand target.
  The shield remains present, and the next real target compacts without blanks.
  With no primary weapon, a valid owned offhand can still be displayed.
- The explanation retains original category evidence first and adds the actual
  general-selection basis. It describes amulet/defender/weapon preferences in
  ordinary language, without exposing numeric implementation bands.
- Ranged/Magic comparison rules and core body/legs pools are unchanged. Exact
  reviewed items otherwise detected as `OTHER` can enter Melee only with the
  correct slot and a permitted primary role. Missing stats cannot promote an
  explicitly support-only role. Shared accessories remain owned once, Melee
  first; a new Melee winner can release an accessory to another style.
- General disposal scores, Pareto dominance, Fixed/Flexible Alch, manual
  categories and saved/accepted target precedence remain independent.
  `AcceptedBlueprint.RULES` advances to 2; older accepted targets remain usable.

## Exact data and exclusions

The bundled `melee-primary-candidates.tsv` has **307 groups / 624 unique IDs**.
It records usable variants and inactive repair/charge states separately.
The explicit developer generator and its `-Check` command are documented in
`tools/research/melee-bis/README.md`; the check does not rewrite stale output.

Reviewed variant tests include all normal/locked Ghommal's Avernic defenders,
Frozen/Volcanic/ornamented whips, gilded/god Rune scimitars, Bounty ornaments,
Barrows durability, Torva/Oathplate appearances and all Avernic treads upgrades.
Broken anchor is inactive. Failed Arena ornament rewards 26708/26710 and the
temporary Sunlit bracers are not new primary candidates.

Six explicitly held armour decisions remain outside the reviewed map: four
Dragonstone pieces and two Berserker necklaces. Restricted functional BH
upgrades and niche equipment outside the researched families remain unreviewed.
They retain existing visibility/legacy fallback; absence is never Alch evidence.
This is not a claim that every wearable item in the game has been audited.

The current July gear release was checked against actual item facts rather than
stale training summaries. Inquisitor armour remains eligible for its raw
offensive stats as well as its specialist role. Emberlight's demon effect is not
treated as unconditional Strength. Obligator has differing 3/5-tick styles and
therefore no single verified comparison-speed value in this table. Evidence
and checked dates are in the developer review files, with pinned revisions where
actually available; direct Wiki fetch restrictions were not bypassed.

## Tests and readable results

**2,308 tests pass**, including 18 helm cases, 28 equipment cases and three
candidate-data cases. Original classification derivation tests remain active.
New coverage includes all ten positions, mixed midgame/endgame ownership,
functional states, whole-group fallback/transitivity, physical copies,
placeholders, exact variants, shared-accessory allocation, specialist retention,
unknown handedness, personal choices and accepted layouts in both presets.

Old geometry fixtures now use a reviewed Torva primary where a fake high-stat
helm previously stood. Slot coverage uses a verified one-handed weapon rather
than assuming an unknown synthetic weapon permits a shield. Independent
endgame expectations now choose Mace instead of Scythe, and the reported
midgame fixture chooses Whip instead of Fang. Displaced weapons remain checked
in their secondary groups. No conservation, quantity, placeholder or geometry
gate was removed; frozen factual vectors were not changed.

HTML/PNG/text results under `build/reports/combat-quality/` include:

- `IRONMAN-early-melee-fallback` and `MAIN-early-melee-fallback`;
- `IRONMAN-reported-midgame` and `MAIN-reported-midgame`;
- `IRONMAN-endgame` and `MAIN-endgame`;
- mixed midgame/endgame, condition variants and incomplete-set cases.

Visual inspection confirms compact owned fallbacks for a small bank; complete
primary Melee columns for populated banks; specialist weapons together; and
vertical remaining armour families. Reports are developer simulations, not an
owner in-game result or a DPS certificate.

## Simulation delta review

All **1,950 scenario banks complete**: 150 standard and 1,800 multi-seed cases.
No unsupported plan, build error, blocked guidance, stall or nontermination.
Compared with the saved pre-rollout reports, only two standard report rows change:

| Seed / scenario | Total moves | Swaps / minimum swaps at sorting start |
| --- | --- | --- |
| 20260737 / RANDOM_TABS | 116 -> 117 | 45 -> 46 |
| 20260754 / RANDOM_TABS | 103 -> 102 | 43 -> 42 |

All remaining fields and rows are identical. New Melee targets can change the
swap cycles of an already partitioned bank. The delta is neutral in aggregate
and both routes end in verified COMPLETE. Classification/cleanup reports remain
byte-identical after the existing line-ending normalization.

The single reviewed `report.tsv` fingerprint changes deliberately from
`6403583c57f42c8fca16a296af54d92571d5597f3cdc773fb6061a4497a97ec1` to
`1513eb2c32720fad85c061a868d8248400c9c44b7df3d71f379409f4406eb223`.
The other three remain:

```text
cleanup-review.tsv           00c1f0eac9310e3ec869fe7c29aebdf4e3aa85c800b02adc01afd124f84f1fdf
aggregate/cleanup-review.tsv ba33e9144824c0e1d0e9671f3933385f6dd6605f9b60bcbfc7ab43794a013edd
aggregate/metadata.tsv       b4eb3aa665c848f66a832f7cd0b8ecb5d64079ee6b13a2e3c3d26dd175048431
```

## Review budget and static facts

Ahead-of-time compilation preserves all **33,766** registry records, including
records outside the simulator's 32,609-item universe. Exhaustive tests compare
every ordered ID/name/category/subcategory/tag/workflow field against the
retained original derivation. The four derivation helpers and five raw inputs
are development/test-only; the runtime reads exact compiled facts. This removes
repeated runtime rule derivation without changing bank-dependent decisions.
See `tools/research/melee-bis/COMPILED-REGISTRY.md` for the explicit regeneration
workflow, stale checks, required runtime dependency validation and failure cases.

Highest final main-Java estimate: **193,184**, or **6,816** estimated margin.
The four estimates are 189,878 / 192,952 / 190,103 / 193,184, calibrated against
the maintainer's 200,414 at `def1e856e101ff0e57dd96adccab2cf1d886b076`.
This stays below the plan's 195,000 local working target. It is not an official
Hub count and does not count resources, tests or build changes. Those were
reviewed separately, including all compiled facts, new exact candidate data,
test-only source movement, packaging exclusions and the deliberate one-hash
baseline update. A clean build alone cannot certify the token threshold.

## Final gate and artifact

`./gradlew build --offline --console=plain` succeeds with all four reviewed
fingerprints at the final gate (14 seconds; unit tests reused the preceding
successful 2,308-case execution). Log:
`build/tmp/melee-equipment-final-gate-2026-10-24.log`.
The explicit candidate generator's `-Check` also passes and `git diff --check`
is clean.

The local development JAR keeps the existing v0.9.8 filename; it is not a new
published release. It has 920,078 bytes / 262 entries, SHA-256:
`e979c76dbb353c84495fae107a4526eb98d8de9788b61111210054b8432495ae`.
Inspection confirms the compiled registry and primary-candidate table are
present, while derivation/test classes and all five development-only raw inputs
are absent. No runtime dependency or automatic game action was added.

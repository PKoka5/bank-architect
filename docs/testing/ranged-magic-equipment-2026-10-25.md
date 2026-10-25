# Ranged/Magic primary equipment checkpoint — 25 October 2026

Local development after the owner accepted the optional combat-group in-game
test. The owner chose to finish Ranged/Magic before the complete release and
supplied progression/reference links. No publication, commit or push is made.

## Implementation

`PrimaryGearSelector` replaces the Melee-only helper with one shared comparator
and separate required local tables. Melee keeps its 624-ID policy, comparable
groups, numeric priorities and exact-ID tie-break. Ranged adds **427 exact IDs /
161 groups**; Magic adds **442 IDs / 151 groups**, spanning all ten equipment
positions, functional/cosmetic states and explicit exclusions. These totals
include excluded primary states and shared accessories, not only usable items.

Reviewed Ranged/Magic candidates precede the existing unreviewed style fallback.
Actual style power, accuracy, Prayer and defence decide comparable candidates;
equal vectors retain existing family/placement tie-breaks. Incomplete candidate
stats switch an entire slot/band to its reviewed order. Weapon preference bands
separate different functions; their rank is not universal target DPS.

The primary allocator respects handedness for all three styles. Tbow, Bowfa,
Ascension crossbows, Shadow and Zorya's tome release their primary offhand;
ordinary compatible one-handed weapons retain it. Alternative shields/books
stay available. Crystal per-piece bow synergy is applied only with a selected
usable matching bow and owned active pieces. Placeholders preserve active IDs.
Uncharged powered weapons and inactive Crystal forms do not acquire usable
states merely from being owned. Imbued Slayer helmets use raw bonuses without
a task multiplier, and general Mage helmets such as Farseer remain eligible.

Farseer and other reviewed non-Melee core armour no longer fall into a Melee
primary just because a name fallback matches "helm". Mixed-hide pieces explicitly
reviewed for both styles retain Melee-first physical allocation. Shared items
still appear once. Non-offensive support rings remain secondary to Ranged/Magic,
without an Alch decision. Elysian/Spectral now have an explicit support-defence
family; otherwise they could scatter when the new primary offhands were released.
Optional combat tags also reuse exclusive Ranged/Magic membership when stats
are absent, while shared Fury-like items remain in Combat Gear. Crystal pairing
appends its role explanation while preserving the original placement provenance.

Necklace of Fangs **34401** was absent from the registry snapshot and is now a
small exact supplement in `StaticItemCatalog` (10 entries total). No broad
registry refresh, note-ID guessing or runtime network operation was introduced.

Personal item choices, saved physical orders, accepted snapshots and Fixed/
Flexible Alch keep their existing priority. `AcceptedBlueprint.RULES` advances
from 5 to **6**; older accepted targets are retained rather than rewritten.

## Source and test review

The owned Wiki/RuneLite evidence, secondary-reference inaccuracies, compiler,
audit tables and condition boundaries are described in
[the research record](../../tools/research/ranged-magic-bis/README.md). The
[readable slot progression](../plans/ranged-magic-slot-order-review.md) accompanies
the exact tables. The explicit offline compiler passes `-Check`. Nothing invokes
it from the production plugin or silently regenerates it during a normal build.

The new Ranged/Magic tests cover reviewed slot identities/exclusions, early to
endgame armour, damage-before-defence, missing-stat groups, both handedness
outcomes, charge states, Crystal pairing, conditional gear, Fangs, shared items,
shuffled inputs and withdrawn placeholders. Two changed fixed simulations have
dedicated completion/minimum-swap checks and exported review targets.

Existing tests intentionally update these expectations:

- Two-handed primary setups no longer include incompatible offhands; released
  shields are secondary in the independent quality fixture, with stat vectors
  unchanged. Their shorter primary columns shift later hands/ring rows.
- The reported midgame bank uses its charged Swamp trident instead of its
  uncharged Eye of Ayak. Inactive Bowfa/Crystal and Shadow fixtures now check
  retention and usable alternatives rather than charged effects.
- The older Ranged-head fixture has no dedicated Magic helmet: its imbued Slayer
  helmet can supply raw Magic +3 there while Archer retains the Ranged head.
- Geometry fixtures use actual reviewed primary IDs with controlled vectors.
  Filler rings have no reviewed Ranged/Magic offensive role, rather than supplying
  zero synthetic stats for real Archers/Seers/Brimstone rings and assuming those
  explicit identities were generic filler.
- Personal placement provenance remains the explanation prefix when a new
  primary-role explanation follows it. The specific Alch witness remains tested.

## Fixed simulation difference reviewed

All **1,950 scenario banks complete**. Only two of 150 standard report rows change:

| RANDOM_TABS seed | Items | Total moves before → after | Swaps and minimum before → after |
|---|---:|---:|---:|
| 20260718 | 34 | 56 → 57 | 23 → 24 |
| 20260737 | 71 | 117 → 114 | 46 → 43 |

The first sampled target includes the reviewed Hydra Slayer helmet and Ice
ancient sceptre; the second includes reviewed Mystic hat (or), Imbued Zamorak
cape and Crystal body. Their snapshots were exported to
`build/reports/ranged-magic/{seed}-gear.txt` and `{seed}-blueprint.txt`. Both reach
the verified final layout with exactly the independently computed minimum
swaps. No distribution, transfer, tab, collapse, create, return or outcome count
changes; net swaps decrease by two. This is a change in desired order, not a
claim that the guidance algorithm itself was optimized.

The three other normalized reports remain byte-equivalent:

- cleanup: `00c1f0eac9310e3ec869fe7c29aebdf4e3aa85c800b02adc01afd124f84f1fdf`;
- aggregate cleanup: `ba33e9144824c0e1d0e9671f3933385f6dd6605f9b60bcbfc7ab43794a013edd`;
- aggregate metadata: `b4eb3aa665c848f66a832f7cd0b8ecb5d64079ee6b13a2e3c3d26dd175048431`.

After reviewing these exact differences, only the standard report expectation
in `build.gradle` changes from `1513eb2c...` to
`33a46d530a97e390521c34059dd8da816df5efb838e41450dc67902929df5cee`.
Original reports are preserved under `build/tmp/ranged-magic-before-2026-10-25/`.

## Build, budget and limitations

The highest local main-Java review estimate is **196,237**, leaving **3,763**
estimated tokens. Other estimates are 193,085 / 195,908 / 193,410. Calibration
remains the maintainer's 200,414 at `def1e856e101ff0e57dd96adccab2cf1d886b076`.
This is not the official Hub count. New resource tables, support-role data,
development tools/tests and the deliberate simulation expectation were inspected
separately. No dependency or network capability was added.

The final complete offline build passes in **54 seconds**, with **2,376 tests**,
zero failures/errors/skips and all four reviewed simulation checks passing.
No compiler warning appears in the log. `git diff --check` also passes. The
older accepted-blueprint test now substitutes the current rules number rather
than a stale hardcoded value, so its version-3 compatibility assertion remains
effective after the rules update.

Visual review of the fully populated endgame and 182-item mixed Main bank
confirms the three complete available gear columns, grouped alternative shields,
retained vertical armour sets and the ring block. The previously scattered
Elysian/Spectral pair is adjacent in the 102-item vertical-space fixture. The
other fixed endgame, partial, colour/condition and duplicate-stack tests remain
enabled, including their preservation and deterministic-order assertions.

The development JAR retains its v0.9.8 filename: **934,356 bytes / 266 entries**,
SHA-256 `d3abec10e5f0bbb8975077a5b0487fd50fb6f1d17a9347f7a3bbf6961d720f2d`.
It contains the shared helper and all three candidate tables; the old Melee-only
helper, development compiler/classification helpers and five raw derivation
inputs are absent. The compiled classification registry remains unchanged.
HEAD remains `675ccbd`; no release, commit, push or Hub submission was made.

Primary logs are `build/tmp/ranged-magic-full-2026-10-25.log`,
`ranged-magic-simulation-review-2026-10-25.log` and
`ranged-magic-size-2026-10-25.log`. Large, mixed and condition-variant rendered
fixtures are under `build/reports/combat-quality/`. These checks complement,
not replace, owner in-game testing of this new Ranged/Magic behavior.

# General Ranged head selection - 22 October 2026

Local development checkpoint above published 0.9.6 source
`d0c4348921d2ca71883f2dd9a5bcadaa4c1c015d`. Includes the earlier
[Wiki strategy Alch pilot](wiki-strategy-alch-2026-10-22.md) and selected GitHub fixes.

## Reproduced problem and scoped correction

The user confirmed owning an imbued Slayer helmet and an Archer helm. The actual
85-item midgame fixture contains Archer helm 3749 and imbued variant 26674.
Both have progression tier 3. The old general head comparison adds the entire
defensive sum to offensive score: Archer totals 660 and Slayer (i) totals 741.
That selects the more defensive/task-oriented helmet despite lower Ranged attack.

Primary Ranged head comparisons now use Ranged strength, then Ranged attack,
before the existing tier/defence/family tie-breakers. This is a general navigation
policy, not target-specific DPS simulation. Other slots, styles, Alch dominance
and global tier scoring retain their earlier rules. The existing Bowfa/Crystal
selection still takes contextual precedence. Slayer gear stays available; no
current task or equipped gear is read. Manual and captured physical orders win.

The exact `archer helm` name fallback is also registered as Ranged when equipment
metadata is absent. With normal supported stats, pure Melee headgear is already
ineligible for the Ranged head candidate pool. No fabricated empty bank cells are
introduced: a loose item in an unused physical grid cell is not a selected BIS item.

## Primary sources

- [Archer helm](https://oldschool.runescape.wiki/w/Archer_helm): ID 3749, +6
  Ranged attack, no Ranged strength; defensive vector 6/8/10/6/6.
- [Slayer helmet (i)](https://oldschool.runescape.wiki/w/Slayer_helmet_%28i%29):
  +3 Ranged attack and defensive vector 30/32/27/10/30; additional offensive
  effects require the relevant Slayer target.
- [Ordinary Slayer helmet](https://oldschool.runescape.wiki/w/Slayer_helmet):
  Ranged attack -2 and Magic attack -6. It does not inherit the imbued vector.

These facts were checked through Wiki indexed content on the review date and
the repository's frozen RuneLite vectors. No exact current Wiki revision is
claimed for these three indexed observations. No runtime Wiki query is added.

## Verification

- Java 11 offline full build: **1,954 tests**, zero failures, errors or skips.
- **1,950 scenario banks completed**, all four simulation fingerprints unchanged.
- New `RangedHeadSelectionTest`: 18 parameterized cases across Main and Ironman.
  Covers Archer vs ordinary Slayer and imbued IDs 11865/25177/26674, Torva,
  Neitiznot/Berserker exclusions, eight shuffle/placeholder trials, absence of a
  Ranged helmet, stronger Ranged headgear, damage-before-accuracy precedence,
  missing equipment metadata and saved/captured physical order.
- Bowfa/Crystal and Twisted bow/Masori retain their previous contextual behavior.
- Three old fixture expectations now select Archer instead of Slayer (i), with
  the Slayer item still conserved as secondary gear. The original test vectors
  and all other primary column cells are unchanged.
- Viewed the regenerated `IRONMAN-full-combat-columns-85.png`: its first three
  cells are Neitiznot, Archer and Farseer. Full columns, item conservation and
  family grouping assertions pass in Main, Ironman, midgame and endgame fixtures.
- `git diff --check` passes. No new compiler warning observed; the existing
  sidebar unchecked-operation note remains.

Logs: `build/tmp/ranged-head-build-2026-10-22.log` and
`build/tmp/ranged-head-review-size-2026-10-22.log`.

Highest local main-Java review estimate: **197,140**, with **2,860 estimated
tokens of headroom**. Other estimates: 195,117 / 196,786 (cl100k) and 195,461
(o200k preserved). Calibration remains the official 200,414 at
`def1e856e101ff0e57dd96adccab2cf1d886b076`; this is not the official Hub count.
The one name-fallback resource token, tests and documents were reviewed separately.

Development JAR: 1,115,926 bytes, 264 entries, no test/research/simulation/tooling
or documentation entries. SHA-256:
`2e124000ef80e8471b9369ae647ae7138c5ad53288fd837a370a88586ca70a1b`.
It still uses the 0.9.6 filename and differs from the published artifact.

No commit, push, merge or publication performed. In-game verification remains.

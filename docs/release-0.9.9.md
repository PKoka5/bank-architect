# Bank Architect 0.9.9

Release prepared 25 October 2026, above published 0.9.8 source
`675ccbd0e708ed6ea3396f0f35ff54ad96dfea21`. The owner authorized publication
after the Melee, Ranged, Magic and Void in-game checks. This record supersedes
the development-only publication status in the linked checkpoints.

## Player-facing changes

- Reviewed primary equipment candidates for all ten slots in Melee, Ranged and
  Magic. Comparable stats choose within reviewed progression groups; missing
  stats use reviewed fallback ranks. General Melee prefers one-handed weapons
  and defenders, while specialist weapons remain available. Two-handed weapons
  release incompatible offhands. Charged states and active Crystal combinations
  have specific handling. These are general gear choices, not boss DPS rankings.
- Multi-piece Void kits stay together, including Elite, ornament, locked and
  compatible mixed variants. The reproduced 94-item bank retains its six Void
  placeholders together without changing its Melee primary column.
- Tools, outfits, accessories, skillcapes and containers group by skill. The
  existing Gather frequently used items setting controls automatic shortcuts.
  Optional Melee, Ranged and Magic Gear tags are available through Tab Layout;
  shared items appear once and explicit assignments retain priority.
- Review changes compares destinations and relative order, distinguishes new
  and absent items, and shows placement reasons. Keep this proposal saves a
  per-profile layout and creates a Custom when starting from Main or Ironman.
  New items append in their matching tab; absent IDs retain saved records.
  Personal setting changes pause the kept proposal. Remember reference only
  establishes a comparison without fixing the layout.
- Flexible Alch explanations identify recorded replacements, including
  placeholders, and distinguish progression from full-stat comparison.
  Fixed-list and retained decisions keep their own reasons.

The first sidebar opening after upgrading shows What's new: 0.9.9. Dismissal
is remembered for this version; Updates retains all fifteen release entries.
Every real bank move remains manual. No runtime network call, dependency,
automation, inventory/equipment read or new permission is introduced.

## Validation and scope

Final offline Java 11 `gradlew build` passed **2,388 tests**, with zero failures,
errors or skipped cases, and **1,950 completed scenario banks**. All four reviewed
simulation fingerprints pass. The Ranged/Magic policy consciously changed two
standard-report swap counts (+1 and -3) during development; the other three
fingerprints remained unchanged. The Void fix retains all four final baselines.
The final release build reports no compiler warning. `git diff --check` passes.

Notice/history were rendered and visually checked at 204/225-pixel widths.
First opening after 0.9.8, dismissal, restart and history navigation pass.
The owner accepted the combined in-game gear checks, including the Void fix;
this is not a claim of live testing with every bank or all endgame items.

Classification derivation now runs only in explicit offline development tooling.
The checked-in runtime registry preserves **33,766 records**, verified against
the source derivation. Four derivation helpers moved to test sources; five raw
input tables are excluded from the JAR. Required runtime resources fail closed
when malformed. This reduces repeated runtime code without removing reviewed
classification. The candidate tables contain 624 Melee, 427 Ranged and 442 Magic
identities, including reviewed unavailable/excluded states and shared items.

Resources, tests, build changes and research tools were inspected separately
from the main-Java estimator. The production artifact includes compiled facts,
reviewed candidate tables and local update notes. It excludes test classes,
derivation helpers, raw input tables, research tools/caches, private exports,
documents and simulation output. The reproduced bank fixture contains only
Combat item IDs and placeholder flags, with positive quantities normalized.

Detailed checkpoints:

- [Blueprint review](testing/blueprint-review-2026-10-23.md),
  [accepted blueprint](testing/accepted-blueprint-2026-10-24.md) and
  [Alch explanations](testing/alch-explanations-2026-10-24.md).
- [Melee equipment](testing/melee-equipment-2026-10-24.md) and
  [weapon policy](testing/melee-weapons-2026-10-25.md).
- [Skill groups](testing/skill-activity-groups-2026-10-25.md) and
  [optional style tags](testing/optional-combat-tags-2026-10-25.md).
- [Ranged/Magic equipment](testing/ranged-magic-equipment-2026-10-25.md) and
  [Void coherence](testing/void-set-coherence-2026-10-25.md).
- [Compiled-registry workflow](../tools/research/melee-bis/COMPILED-REGISTRY.md).

## Review-token budget

Calibration remains the maintainer-reported **200,414** for
`def1e856e101ff0e57dd96adccab2cf1d886b076`, reported 30 September 2026.
The merged 0.9.8 request contains no newer numeric count.

| Tokenizer / normalization | Local estimate |
| --- | ---: |
| cl100k / preserved | 193,279 |
| cl100k / collapsed | 196,090 |
| o200k / preserved | 193,615 |
| o200k / collapsed | **196,430** |

The highest leaves **3,570 estimated tokens** below 200,000. These estimates
cover main Java only; the official tokenizer, preprocessing and input scope
remain unknown. Separate scope inspection does not turn this into an official
count. Strictly fewer than 200,000 official tokens remains a standing requirement.

Logs: `build/tmp/release-0.9.9-final-build-2026-10-25.log` and
`build/tmp/release-0.9.9-review-size-2026-10-25.log`.

Artifact: `build/libs/ironman-bank-architect-0.9.9.jar`, **935,504 bytes**,
**266 entries**, SHA-256
`e8a9b9d7da303b724adb9764d51a0067758bd4473325cd9ce9aa4dcb45ad55d4`.

## Publication

The release tag will be `v0.9.9`. The GitHub release and new Plugin Hub request
must refer to that exact source revision. Plugin Hub availability follows
upstream review and merge; a GitHub release alone does not update installations.
Larger 2.0 roadmap work and user-defined named groups are deferred.

# Bank Architect 0.9.3

Prepared on **10 October 2026** above published 0.9.2 source
`3b9012e360ff4a23c591f7eda722b81dc2d41a90`.
[Hub update 18098](https://github.com/runelite/plugin-hub/pull/18098) is merged;
the Hub manifest references that same revision. The owner explicitly authorized
publication of the combined changes on 10 October 2026.

## Player-facing changes

- Combat uses vertical style columns for the selected best gear, with other
  owned sets arranged vertically around them. List/grid layouts share reviewed
  family coverage; cannon parts stay grouped. Exact gear slots, progression,
  unusual names, casting roles and recent armour variants have broader coverage.
- Reviewed frontline priorities keep Torva/general Strength gear and raid
  weapons ahead of specialist alternatives. Slash, stab, crush and other weapon
  roles retain separate groups; existing Barrows/Moon weapons stay with their
  armour sets. Crystal armour accompanies a selected Bowfa.
- Avernic treads, Eye of ayak, Confliction gauntlets, amulet of rupture and
  reviewed inactive/cosmetic variants have explicit coverage. Placeholder and
  reversed-input cases retain their intended Combat arrangement.
- Finished arrows, bolts, darts and cannonballs default to Slayer & Boss Loot
  in separate ordered groups, with Alch gathered afterward. Player assignments,
  relocated groups and saved/captured blueprints keep priority.
- Dense Combat preview packing avoids needless heap insertions while preserving
  equal-score handling and existing search limits. General placement priorities
  are separate from automatic Alch replacement evidence.
- Bundled **What's new** notes now identify 0.9.3. They appear after an earlier
  release was dismissed and stay dismissed after closing/reopening or restarting.

All real bank moves remain manual. No dependency changes, runtime downloads,
telemetry, game actions or inventory/equipment reads were added.

## Validation

- Final offline `build`: **1,611 JUnit cases**, zero failures/errors/skips;
  **150 fixed and 1,800 aggregate simulations completed**. All four deliberately
  reviewed simulation fingerprints pass.
- **20 Combat preview simulations** pass, including three 251-ID endgame variants
  in both presets, banks without raid weapons, historical exports and the latest
  owner export. The latest export has 825 complete unique IDs and 12 placeholders;
  independent structural review found no duplicates, invalid slots, scattered
  ammo families, broken vertical gear sets or separated cannon parts.
- Endgame expectations independently check armour/weapon positions, specialist
  retention, manual choices and placeholder stability. The owner confirmed the
  current-bank blueprint looks improved; endgame gear was validated through
  representative simulations rather than an owner live endgame bank.
- The release run builds endgame previews in 950-1,024 ms locally, versus
  5.1-5.6 seconds for the earlier smaller fixture. These are individual timings,
  not a controlled benchmark or live client frame-time measurement.
- Existing release-notice lifecycle/render tests pass for 0.9.2 to 0.9.3. The
  204px-wide rendered notice was visually inspected; text and dismissal fit.
- No compiler/test warning in the final build. `git diff --check` passes;
  Git's LF/CRLF notices concern line-ending normalization.

Details: [gear audit](testing/combat-gear-audit-2026-10-09.md),
[layout quality](testing/combat-layout-quality-2026-10-09.md),
[vertical placement](testing/combat-vertical-layout-2026-10-10.md),
[ammo grouping](testing/ammo-darts-bolts-grouping-2026-10-10.md), and
[endgame roles and baseline review](testing/endgame-combat-roles-2026-10-10.md).
Private exports, research caches and generated preview JSON remain outside
source control and the plugin JAR.

## Review-token budget

The estimator was rerun after version/update-notice preparation. Calibration
remains the official **200,414** count for
`def1e856e101ff0e57dd96adccab2cf1d886b076`, reported on 30 September 2026.

| Tokenizer / normalization | Local estimate |
| --- | ---: |
| cl100k, preserved whitespace | 195,972 |
| cl100k, collapsed whitespace | 197,174 |
| o200k, preserved whitespace | 196,009 |
| o200k, collapsed whitespace | **197,232** |

The highest leaves **2,768 estimated tokens** below 200,000. This is an estimate;
the official Hub tokenizer and input scope are unknown. Bundled resource,
test, tooling, Gradle/version and documentation changes outside main Java were
inspected separately. Five unchanged fallback name groups moved from repeated
Java strings to the existing names resource. Keep the strict-under-200,000
requirement and recheck the exact committed source before submitting.

## Verified artifact and submission

- JAR: `build/libs/ironman-bank-architect-0.9.3.jar`.
- Size: **1,104,601 bytes**, **270 entries**.
- SHA-256: `52beb8a0c9732e480cd0e999a2cfaa67cacc88fce5548a193f53d532f03c5982`.
- Contains production classes/properties/catalogs, including the new 2,286-byte
  role table. No test, simulator or research classes are bundled.

The published tag is `v0.9.3`; the GitHub release and Hub pull request record its
actual source revision. The Hub submission changes only `plugins/bank-architect`
to that revision. Client distribution follows Hub review and merging.

The current [Hub update instructions](https://github.com/runelite/plugin-hub/blob/master/README.md)
and [rejected-feature guidance](https://github.com/runelite/runelite/wiki/Rejected-or-Rolled-Back-Features)
were refreshed before submission. No new gameplay automation or rejected
features were introduced.

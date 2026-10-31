# Issue #50: shield components belong with Combat

31 October 2026. Local fix for [#50](https://github.com/PKoka5/bank-architect/issues/50),
also covering the two matching ordinary shield components. Not published;
no GitHub reply, issue closure, commit, push or Hub update was performed.

## Reviewed identities and function

| Ordinary ID | Component | Supported source |
| --- | --- | --- |
| 11286 | Draconic visage | [OSRS Wiki](https://oldschool.runescape.wiki/w/Draconic_visage): not equipable; used to create a dragonfire shield. |
| 21637 | Wyvern visage | [Ancient wyvern shield creation](https://oldschool.runescape.wiki/w/Ancient_wyvern_shield#Creation): used with an elemental shield to create the shield. |
| 22006 | Skeletal visage | [OSRS Wiki](https://oldschool.runescape.wiki/w/Skeletal_visage): not equipable; used to create a dragonfire ward. |

Ordinary IDs and distinct Cert/Placeholder definitions were checked against the
cached official RuneLite API 1.13.4 source `net/runelite/api/gameval/ItemID.java`.
The normal/cert/placeholder triples are 11286/11287/16834,
21637/21638/21639 and 22006/22007/22008. These relations were not inferred from
adjacent IDs or similar names. Direct Wiki retrieval for Wyvern visage was
blocked; the shield creation page was available through search. Wiki checks are
dated observations, not revision-pinned snapshots or price evidence.

## Small policy change

- Three canonical classification overrides now describe the ordinary components
  as `UNIQUE / equipment-upgrade`, with an `equipment-upgrade` usage tag.
- An exact-ID preset route places those three items in Combat for Main and
  Ironman. No other equipment-upgrade family was rerouted.
- Existing `support-*` role handling excludes them from primary equipment;
  `support-upgrade` facts reuse that path. They are not wearable shields and
  cannot serve as evidence that real wearable gear is redundant.
- The functional tag protects Flexible Alch. They are absent from the Fixed
  list. Explicit player assignments, including a deliberate Alch assignment,
  retain priority. No GE-price check or new setting was added.
- Raw note/placeholder definitions keep their previous catalog entries. The
  bank reader normalizes real placeholder observations to the ordinary ID and
  retains quantity zero. The normal component route then applies.
- Supported kept blueprints and captured physical positions remain usable.
  Accepted targets retain their tags/order; captured positions may refresh their
  automatic tag. Rule generation advances from 11 to 12 without invalidating
  supported older snapshots.

`compileItemRegistry` was run explicitly. A UTF-8 byte-aware comparison against
the verified pre-fix table confirms **exactly three changed records**, with no
new IDs or unrelated name changes. Three independently reviewed role rows pin
both presets' expected tags/destinations. No runtime downloads were introduced.

## Tests and reviewed simulation change

`VisagePlacementTest` supplies 12 parameterized tests across both presets:
ordinary classification, all Gear layouts, both Alch modes, stacks/placeholders,
synthetic erroneous positive stats, exclusion from primary setups, personal
assignments, moved tags, old kept/captured positions and nonmatching raw variants.
One bank-reader test uses the three actual placeholder constants. Synthetic
vectors stress policy; they do not claim real equipment bonuses for components.

The first full build passed its tests and completed all 1,950 simulated banks,
then correctly failed the frozen-report gate. Before changing either reference,
all four reports were compared with the saved pre-fix outputs:

- Standard `report.tsv` and `cleanup-review.tsv`: byte-identical.
- Aggregate cleanup review: exactly the row `22006 / Skeletal visage /
  SKELETAL_VISAGE / 12` disappeared. It is no longer incorrectly assigned to
  Cleanup in those twelve sampled banks; no other row changed.
- Aggregate metadata: distinct cleanup IDs 9,257 -> 9,256 and cleanup occurrences
  46,782 -> 46,770. All remaining fields, including simulation parameters, match.
- Aggregate outcomes: 1,800 completed; zero unsupported plans, build errors,
  blocked advisors, stalled or nonterminating banks. All 150 standard banks
  completed as well.

Only the two explained aggregate fingerprints in `build.gradle` were updated:

| Report | Previous hash | Reviewed new hash |
| --- | --- | --- |
| `aggregate/cleanup-review.tsv` | `ba33e9144824c0e1d0e9671f3933385f6dd6605f9b60bcbfc7ab43794a013edd` | `2fd497cca8aca3ce276a77046053003c3484f6786656d662a5b7e638dae51215` |
| `aggregate/metadata.tsv` | `b4eb3aa665c848f66a832f7cd0b8ecb5d64079ee6b13a2e3c3d26dd175048431` | `89d005930a195e33236265ceb51c09ec85a993c7b8a073d0c55bdcb9c8bf70fe` |

## Final verification

- Full Java 11 offline build passes: **2,618 tests**, zero failures/errors/skips,
  **1,950 completed scenario banks** and all four reviewed fingerprints passing.
- Highest local main-Java estimate **196,396**, leaving **3,604 estimated tokens**.
  Other estimates: 193,240 / 196,053 / 193,584. The 195,000 target still needs
  **1,396** estimated reduction.
- Calibration is unchanged: maintainer 200,414 at
  `def1e856e101ff0e57dd96adccab2cf1d886b076`. Official tokenizer/scope are
  unknown; data, build reference changes and tests were inspected separately.
- The JAR includes the corrected three runtime records and excludes
  `VisagePlacementTest`. No compiler warning; whitespace checks pass.
- Development JAR: **938,473 bytes**, SHA-256
  `be443abe7b1d2fd91e7bd2413fb6246f8046a8c78310233042c6896ea35dc126`.
  It still carries development version 0.9.9 and does not replace the published
  artifact. The earlier two-directory reproduction proof predates this fix;
  final source-commit clean-build validation remains required before publishing.

Logs under `build/tmp/`: `issue-50-registry.log`, `issue-50-focused.log`,
`issue-50-full.log` (reviewed baseline failure), `issue-50-full-final.log` and
`issue-50-size.log`. Pre-fix reports: `issue-50-simulation-before/`.
Issue #50 can be referenced as locally fixed/pending release; it has not been
closed as distributed. The separate unconfirmed shield/defender report in #48
was not changed or claimed resolved.
